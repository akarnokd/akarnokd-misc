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

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.query.ObservableBuilder;
import hu.akarnokd.reactive4java.reactive.Reactive;

/**
 * Shakespeare plays Scrabble with Reactive4Java optimized.
 * @author JosÃ©
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithR4JOpt extends ShakespearePlaysScrabble {

    static ObservableBuilder<Integer> chars(String word) {
        return ObservableBuilder.range(0, word.length()).select(i -> (int)word.charAt(i));
    }

//    @Benchmark
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

//        Schedulers.setDefault(new CurrentThreadScheduler());

        //  to compute the score of a given word
        Func1<Integer, Integer> scoreOfALetter = letter -> letterScores[letter - 'a'];

        // score of the same letters in a word
        Func1<Entry<Integer, MutableLong>, Integer> letterScore =
                entry ->
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ;


        Func1<String, ObservableBuilder<Integer>> toIntegerIx =
                string -> chars(string);

        // Histogram of the letters in a given word
        Func1<String, ObservableBuilder<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> toIntegerIx.invoke(word)
                            .aggregate(
                                new HashMap<>(),
                                (HashMap<Integer, MutableLong> map, Integer value) ->
                                    {
                                        MutableLong newValue = map.get(value) ;
                                        if (newValue == null) {
                                            newValue = new MutableLong();
                                            map.put(value, newValue);
                                        }
                                        newValue.incAndSet();
                                        return map;
                                    }

                            );

        // number of blanks for a given letter
        Func1<Entry<Integer, MutableLong>, Long> blank =
                entry ->
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ;

        // number of blanks for a given word
        Func1<String, ObservableBuilder<Long>> nBlanks =
                word -> histoOfLetters.invoke(word)
                            .selectManyIterable(map -> map.entrySet())
                            .select(blank)
                            .sumLong();


        // can a word be written with 2 blanks?
        Func1<String, ObservableBuilder<Boolean>> checkBlanks =
                word -> nBlanks.invoke(word)
                            .select(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Func1<String, ObservableBuilder<Integer>> score2 =
                word -> histoOfLetters.invoke(word)
                            .selectManyIterable(map -> map.entrySet())
                            .select(letterScore)
                            .sumInt();

        // Placing the word on the board
        // Building the streams of first and last letters
        Func1<String, ObservableBuilder<Integer>> first3 =
                word -> chars(word).take(3) ;
        Func1<String, ObservableBuilder<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        Func1<String, ObservableBuilder<Integer>> toBeMaxed =
            word -> ObservableBuilder.from(Reactive.concat(first3.invoke(word), last3.invoke(word)))
            ;

        // Bonus for double letter
        Func1<String, ObservableBuilder<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.invoke(word)
                        .select(scoreOfALetter)
                        .max();

        // score of the word put on the board
        Func1<String, ObservableBuilder<Integer>> score3 =
            word ->
                ObservableBuilder.from(Reactive.concat(
                        score2.invoke(word),
                        bonusForDoubleLetter.invoke(word)
                ))
                .sumInt()
                .select(v -> 2 * v + (word.length() == 7 ? 50 : 0));

        Func1<Func1<String, ObservableBuilder<Integer>>, ObservableBuilder<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> ObservableBuilder.from(shakespeareWords)
                                .where(scrabbleWords::contains)
                                .where(word -> checkBlanks.invoke(word).first())
                                .aggregate(
                                    new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.invoke(word).first() ;
                                        List<String> list = map.get(key) ;
                                        if (list == null) {
                                            list = new ArrayList<>() ;
                                            map.put(key, list) ;
                                        }
                                        list.add(word) ;
                                        return map;
                                    }
                                );

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                buildHistoOnScore.invoke(score3)
                    .selectManyIterable(map -> map.entrySet())
                    .take(3)
                    .aggregate(
                        new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                            return list;
                        }
                    )
                    .first() ;

//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithR4JOpt s = new ShakespearePlaysScrabbleWithR4JOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}