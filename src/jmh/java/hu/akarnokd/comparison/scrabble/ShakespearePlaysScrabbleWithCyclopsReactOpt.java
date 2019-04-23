/*
 * Copyright (C) 2019 José Paumard
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package hu.akarnokd.comparison.scrabble;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.*;

import cyclops.stream.ReactiveSeq;

/**
 * Shakespeare plays Scrabble with Ix optimized.
 * @author JosÃ©
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithCyclopsReactOpt extends ShakespearePlaysScrabble {

    static ReactiveSeq<Integer> chars(String word) {
        return ReactiveSeq.range(0, word.length()).map(i -> (int)word.charAt(i));
    }

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
        iterations = 5, time = 1
    )
    @Measurement(
        iterations = 5, time = 1
    )
    @Fork(1)
    public List<Entry<Integer, List<String>>> measureThroughput() throws InterruptedException {

        //  to compute the score of a given word
        Function<Integer, Integer> scoreOfALetter = letter -> letterScores[letter - 'a'];

        // score of the same letters in a word
        Function<Entry<Integer, MutableLong>, Integer> letterScore =
                entry ->
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ;


        Function<String, ReactiveSeq<Integer>> toIntegerIx =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, ReactiveSeq<HashMap<Integer, MutableLong>>> histoOfLetters =
                word ->  toIntegerIx.apply(word)
                            .scanLeft(new HashMap<Integer, MutableLong>(), (map, value) -> {
                                MutableLong newValue = map.get(value) ;
                                if (newValue == null) {
                                    newValue = new MutableLong();
                                    map.put(value, newValue);
                                }
                                newValue.incAndSet();
                                return map;
                            })
                        .takeRight(1);
                 ;

        // number of blanks for a given letter
        Function<Entry<Integer, MutableLong>, Long> blank =
                entry ->
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ;

        // number of blanks for a given word
        Function<String, ReactiveSeq<Long>> nBlanks =
                word -> {
                    return histoOfLetters.apply(word)
                            .flatMapI(map -> map.entrySet())
                            .map(blank)
                            .scanLeft(0L, (a, b) -> a + b)
                            .takeRight(1);
                };


        // can a word be written with 2 blanks?
        Function<String, ReactiveSeq<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, ReactiveSeq<Integer>> score2 =
                word ->
                    histoOfLetters.apply(word)
                    .flatMapI(map -> map.entrySet())
                    .map(letterScore)
                    .scanLeft(0, (a, b) -> a + b)
                    .takeRight(1);
                ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, ReactiveSeq<Integer>> first3 =
                word -> chars(word).take(3) ;
        Function<String, ReactiveSeq<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        Function<String, ReactiveSeq<Integer>> toBeMaxed =
            word -> first3.apply(word).append(last3.apply(word))
            ;

        // Bonus for double letter
        Function<String, ReactiveSeq<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .scanLeft(0, (a, b) -> Math.max(a, b))
                        .takeRight(1)
                        ;

        // score of the word put on the board
        Function<String, ReactiveSeq<Integer>> score3 =
            word ->
                score2.apply(word)
                .append(bonusForDoubleLetter.apply(word))
                .scanLeft(0, (a, b) -> a + b)
                .takeRight(1)
                .map(v -> 2 * v + (word.length() == 7 ? 50 : 0));

        Function<Function<String, ReactiveSeq<Integer>>, ReactiveSeq<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score ->
                    ReactiveSeq.fromIterable(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).firstValue())
                                .scanLeft(new TreeMap<Integer, List<String>>(Comparator.reverseOrder()), (map, word) -> {
                                    Integer key = score.apply(word).firstValue() ;
                                    List<String> list = map.get(key) ;
                                    if (list == null) {
                                        list = new ArrayList<>() ;
                                        map.put(key, list) ;
                                    }
                                    list.add(word) ;
                                    return map;
                                })
                                .takeRight(1);
                ;

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                buildHistoOnScore.apply(score3)
                    .flatMapI(map -> map.entrySet())
                    .take(3)
                    .scanLeft(new ArrayList<Entry<Integer, List<String>>>(), (list, entry) -> {
                        list.add(entry);
                        return list;
                    })
                    .takeRight(1)
                    .firstValue() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithCyclopsReactOpt s = new ShakespearePlaysScrabbleWithCyclopsReactOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}