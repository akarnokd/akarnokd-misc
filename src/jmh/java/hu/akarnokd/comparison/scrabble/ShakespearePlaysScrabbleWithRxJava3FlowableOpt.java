/*
 * Copyright (C) 2019 Jos� Paumard
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

import hu.akarnokd.rxjava3.math.MathFlowable;
import hu.akarnokd.rxjava3.string.StringFlowable;
import io.reactivex.rxjava3.core.*;
import io.reactivex.rxjava3.functions.Function;

/**
 * Shakespeare plays Scrabble with RxJava 3 Flowable optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithRxJava3FlowableOpt extends ShakespearePlaysScrabble {
    static Flowable<Integer> chars(String word) {
//        return Flowable.range(0, word.length()).map(i -> (int)word.charAt(i));
        return StringFlowable.characters(word);
    }

    @SuppressWarnings("unused")
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
        iterations = 5, time = 1
    )
    @Measurement(
        iterations = 5, time = 1
    )
    @Fork(value = 1, jvmArgs = {
            "-XX:MaxInlineLevel=20"
//            , "-XX:+UnlockDiagnosticVMOptions",
//            , "-XX:+PrintAssembly",
//            , "-XX:+TraceClassLoading",
//            , "-XX:+LogCompilation"
    })
    public List<Entry<Integer, List<String>>> measureThroughput() throws Throwable {

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


        Function<String, Flowable<Integer>> toIntegerFlowable =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, Single<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> toIntegerFlowable.apply(word)
                            .collect(
                                () -> new HashMap<>(),
                                (HashMap<Integer, MutableLong> map, Integer value) ->
                                    {
                                        MutableLong newValue = map.get(value) ;
                                        if (newValue == null) {
                                            newValue = new MutableLong();
                                            map.put(value, newValue);
                                        }
                                        newValue.incAndSet();
                                    }

                            ) ;

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
        Function<String, Flowable<Long>> nBlanks =
                word -> MathFlowable.sumLong(
                            histoOfLetters.apply(word).flattenAsFlowable(
                                    map -> map.entrySet()
                            )
                            .map(blank)
                        )
                    ;


        // can a word be written with 2 blanks?
        Function<String, Flowable<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, Flowable<Integer>> score2 =
                word -> MathFlowable.sumInt(
                            histoOfLetters.apply(word).flattenAsFlowable(
                                map -> map.entrySet()
                            )
                            .map(letterScore)
                            ) ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Flowable<Integer>> first3 =
                word -> chars(word).take(3) ;
        Function<String, Flowable<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        Function<String, Flowable<Integer>> toBeMaxed =
            word -> Flowable.concat(first3.apply(word), last3.apply(word))
            ;

        // Bonus for double letter
        Function<String, Flowable<Integer>> bonusForDoubleLetter =
            word -> MathFlowable.max(toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        ) ;

        // score of the word put on the board
        Function<String, Flowable<Integer>> score3 =
            word ->
//                MathFlowable.sumInt(Flowable.concat(
//                        score2.apply(word).map(v -> v * 2),
//                        bonusForDoubleLetter.apply(word).map(v -> v * 2),
//                        Flowable.just(word.length() == 7 ? 50 : 0)
//                  ));
                MathFlowable.sumInt(Flowable.concat(
                    score2.apply(word),
                    bonusForDoubleLetter.apply(word)
                )).map(v -> v * 2 + (word.length() == 7 ? 50 : 0))
//                new FlowableSumIntArray<Integer>(
//                        score2.apply(word),
//                        bonusForDoubleLetter.apply(word)
//                ).map(v -> 2 * v + (word.length() == 7 ? 50 : 0))
                ;

        Function<Function<String, Flowable<Integer>>, Single<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Flowable.fromIterable(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).blockingFirst())
                                .collect(
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.apply(word).blockingFirst() ;
                                        List<String> list = map.get(key) ;
                                        if (list == null) {
                                            list = new ArrayList<>() ;
                                            map.put(key, list) ;
                                        }
                                        list.add(word) ;
                                    }
                                ) ;

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                    buildHistoOnScore.apply(score3).flattenAsFlowable(
                            map -> map.entrySet()
                    )
                    .take(3)
                    .collect(
                        () -> new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                        }
                    )
                    .blockingGet() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithRxJava2FlowableOpt s = new ShakespearePlaysScrabbleWithRxJava2FlowableOpt();
        s.init();
        System.out.println(s.measureThroughput());

        /*
        ShakespearePlaysScrabbleWithRxJava3FlowableOpt s = new ShakespearePlaysScrabbleWithRxJava3FlowableOpt();
        s.init();
        RxSynchronousCoarseProfiler p = new RxSynchronousCoarseProfiler();
        p.start();
        for (int i = 0; i < 100; i++) {
            System.out.println(s.measureThroughput());
        }
        p.clear();

        System.out.println(s.measureThroughput());

        p.stop();

        System.out.println();

        p.print();
        */
    }
}