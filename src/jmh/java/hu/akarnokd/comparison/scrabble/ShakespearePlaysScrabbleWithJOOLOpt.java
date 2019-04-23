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

import org.jooq.lambda.Seq;
import org.openjdk.jmh.annotations.*;

/**
 * Shakespeare plays Scrabble with JOOL.
 * @author JosÃ©
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithJOOLOpt extends ShakespearePlaysScrabble {

    Seq<Integer> chars(String string) {
        return Seq.range(0, string.length()).map(v -> (int)string.charAt(v));
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

        // Function to compute the score of a given word
        Function<Integer, Seq<Integer>> scoreOfALetter = letter -> Seq.of(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        Function<Entry<Integer, MutableLong>, Integer> letterScore =
                entry ->
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ;

        Function<String, Seq<Integer>> charSeq =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, Seq<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> {
                    HashMap<Integer, MutableLong> map = new HashMap<>();
                    return charSeq.apply(word)
                                .map(value ->
                                        {
                                            MutableLong newValue = map.get(value) ;
                                            if (newValue == null) {
                                                newValue = new MutableLong();
                                                map.put(value, newValue);
                                            }
                                            newValue.incAndSet();
                                            return map;
                                        }
                                )
                                .skip(Long.MAX_VALUE)
                                .append(map)
                                ;
                }
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
        Function<String, Seq<Long>> nBlanks =
                word -> {
                    long[] sum = { 0L };
                    return histoOfLetters.apply(word)
                                .flatMap(map -> Seq.seq(map.entrySet()))
                                .map(blank)
                                .map(v -> sum[0] += v)
                                .skip(Long.MAX_VALUE)
                                .append(0L)
                                .map(v -> sum[0])
                                ;
                }
        ;


        // can a word be written with 2 blanks?
        Function<String, Seq<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .flatMap(l -> Seq.of(l <= 2L)) ;

        // score taking blanks into account letterScore1
        Function<String, Seq<Integer>> score2 =
                word -> {
                    int[] sum = { 0 };
                    return histoOfLetters.apply(word)
                                .flatMap(map -> Seq.seq(map.entrySet()))
                                .map(letterScore)
                                .map(v -> sum[0] += v)
                                .skip(Long.MAX_VALUE)
                                .append(0)
                                .map(v -> sum[0])
                                ;
                }
        ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Seq<Integer>> first3 =
                word -> chars(word).take(3) ;
        Function<String, Seq<Integer>> last3 =
                word -> chars(word).skip(3);


        // Stream to be maxed
        Function<String, Seq<Integer>> toBeMaxed =
            word -> Seq.concat(first3.apply(word), last3.apply(word));

        // Bonus for double letter
        Function<String, Seq<Integer>> bonusForDoubleLetter =
            word -> {
                int[] max = { 0 };
                return toBeMaxed.apply(word)
                            .flatMap(scoreOfALetter)
                            .map(v -> max[0] = Math.max(max[0], v))
                            .skip(Long.MAX_VALUE)
                            .append(0)
                            .map(v -> max[0]);
            };

        // score of the word put on the board
        Function<String, Seq<Integer>> score3 =
            word ->
                {
                    int[] sum = { 0 };
                    return Seq.concat(
                            score2.apply(word),
                            bonusForDoubleLetter.apply(word)
                    )
                    .map(v -> sum[0] += v)
                    .skip(Long.MAX_VALUE)
                    .append(0)
                    .map(v -> {
                        return sum[0] * 2 + (word.length() == 7 ? 50 : 0);
                    });
                }
        ;

        Function<Function<String, Seq<Integer>>, Seq<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> {
                    TreeMap<Integer, List<String>> map = new TreeMap<>(Comparator.reverseOrder());
                    return Seq.seq(shakespeareWords)
                                    .filter(scrabbleWords::contains)
                                    .filter(word -> checkBlanks.apply(word).findFirst().orElse(false))
                                    .map(word -> {
                                                Integer key = score.apply(word).findFirst().orElse(0);
                                                List<String> list = map.get(key) ;
                                                if (list == null) {
                                                    list = new ArrayList<>() ;
                                                    map.put(key, list) ;
                                                }
                                                list.add(word) ;
                                                return map;
                                            }
                                    )
                                    .skip(Long.MAX_VALUE)
                                    .append(map)
                                    ;
                }
        ;

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                buildHistoOnScore.apply(score3)
                    .flatMap(map -> Seq.seq((Iterable<Map.Entry<Integer, List<String>>>)() -> map.entrySet().iterator()))
                    .take(3)
                    .scanLeft(new ArrayList<Entry<Integer, List<String>>>(), (list, entry) -> {
                        list.add(entry);
                        return list;
                    })
                    .findLast().orElse(new ArrayList<>()) ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithJOOLOpt s = new ShakespearePlaysScrabbleWithJOOLOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}