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
import java.util.stream.Collectors;

import org.openjdk.jmh.annotations.*;

import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.Function;

/**
 * Shakespeare plays Scrabble with RxJava 4 Flowable optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithRxJava4StreamableOpt extends ShakespearePlaysScrabble {
    static Streamable<Integer> chars(String word) {
//        return Flowable.range(0, word.length()).map(i -> (int)word.charAt(i));
//        return StringFlowable.characters(word);
        return Streamable.range(0, word.length()).map(i -> (int)word.charAt(i));
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
        Function<Entry<Integer, Long>, Integer> letterScore =
                entry ->
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                entry.getValue().intValue(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ;


        Function<String, Streamable<Integer>> toIntegerStreamable =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, Single<Map<Integer, Long>>> histoOfLetters =
                word -> toIntegerStreamable.apply(word)
                            .collect(Collectors.groupingBy(v -> v, Collectors.counting())
                            ).lastOrError();

        // number of blanks for a given letter
        Function<Entry<Integer, Long>, Long> blank =
                entry ->
                        Long.max(
                            0L,
                            entry.getValue() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ;

        // number of blanks for a given word
        Function<String, Streamable<Long>> nBlanks =
                word -> histoOfLetters.apply(word).flattenAsStreamable(
                                map -> map.entrySet()
                        )
                        .map(blank)
                        .collect(Collectors.summarizingLong(v -> v))
                        .map(ss -> ss.getSum())

                    ;


        // can a word be written with 2 blanks?
        Function<String, Streamable<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, Streamable<Integer>> score2 =
                word -> histoOfLetters.apply(word).flattenAsStreamable(
                            map -> map.entrySet()
                        )
                        .map(letterScore)
                        .collect(Collectors.summarizingInt(v -> v))
                        .map(ss -> (int)ss.getSum())
                    ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Streamable<Integer>> first3 =
                word -> chars(word).take(3) ;
        Function<String, Streamable<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        Function<String, Streamable<Integer>> toBeMaxed =
            word -> Streamable.concat(List.of(first3.apply(word), last3.apply(word)))
            ;

        // Bonus for double letter
        Function<String, Streamable<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .collect(Collectors.maxBy(Comparator.naturalOrder()))
                        .mapOptional(v -> v)
                        ;

        // score of the word put on the board
        Function<String, Streamable<Integer>> score3 =
            word ->
                Streamable.concat(
                    List.of(
                            score2.apply(word),
                            bonusForDoubleLetter.apply(word)
                        )
                )
                .collect(Collectors.summingInt(v -> v))
                .map(v -> v * 2 + (word.length() == 7 ? 50 : 0))
                ;

        Function<Function<String, Streamable<Integer>>, Single<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Streamable.fromIterable(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).blockingFirst())
                                .collect(Collectors.groupingBy(
                                        word -> {
                                            try {
                                                return score.apply(word).blockingFirst();
                                            } catch (Throwable e) {
                                                e.printStackTrace();
                                                return 0;
                                            }
                                        },
                                        () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                        Collectors.toList()
                                )).lastOrError();

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

        return finalList2 ;
    }

    public static void main(String[] args) throws Throwable {
        ShakespearePlaysScrabbleWithRxJava4StreamableOpt s = new ShakespearePlaysScrabbleWithRxJava4StreamableOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}