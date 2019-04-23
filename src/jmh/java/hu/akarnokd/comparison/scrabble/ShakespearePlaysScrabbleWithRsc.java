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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import hu.akarnokd.comparison.IterableSpliterator;
import rsc.publisher.Px;


/**
 * Shakespeare plays Scrabble with Reactive-Streams-Commons.
 * @author JosÃ©
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithRsc extends ShakespearePlaysScrabble {
    @SuppressWarnings("unused")
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

        //  to compute the score of a given word
        Function<Integer, Px<Integer>> scoreOfALetter = letter -> Px.just(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        Function<Entry<Integer, LongWrapper>, Px<Integer>> letterScore =
                entry ->
                    Px.just(
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ) ;

        Function<String, Px<Integer>> toIntegerPx =
                string -> Px.fromIterable(IterableSpliterator.of(string.chars().boxed().spliterator())) ;

        // Histogram of the letters in a given word
        Function<String, Px<HashMap<Integer, LongWrapper>>> histoOfLetters =
                word -> toIntegerPx.apply(word)
                            .collect(
                                () -> new HashMap<>(),
                                (HashMap<Integer, LongWrapper> map, Integer value) ->
                                    {
                                        LongWrapper newValue = map.get(value) ;
                                        if (newValue == null) {
                                            newValue = () -> 0L ;
                                        }
                                        map.put(value, newValue.incAndSet()) ;
                                    }

                            ) ;

        // number of blanks for a given letter
        Function<Entry<Integer, LongWrapper>, Px<Long>> blank =
                entry ->
                    Px.just(
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ) ;

        // number of blanks for a given word
        Function<String, Px<Long>> nBlanks =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> Px.fromIterable(() -> map.entrySet().iterator()))
                            .flatMap(blank)
                            .reduce(Long::sum) ;


        // can a word be written with 2 blanks?
        Function<String, Px<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .flatMap(l -> Px.just(l <= 2L)) ;

        // score taking blanks into account letterScore1
        Function<String, Px<Integer>> score2 =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> Px.fromIterable(() -> map.entrySet().iterator()))
                            .flatMap(letterScore)
                            .reduce(Integer::sum) ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Px<Integer>> first3 =
                word -> Px.fromIterable(IterableSpliterator.of(word.chars().boxed().limit(3).spliterator())) ;
        Function<String, Px<Integer>> last3 =
                word -> Px.fromIterable(IterableSpliterator.of(word.chars().boxed().skip(3).spliterator())) ;


        // Stream to be maxed
        Function<String, Px<Integer>> toBeMaxed =
            word -> Px.fromArray(first3.apply(word), last3.apply(word))
                        .flatMap(Px -> Px) ;

        // Bonus for double letter
        Function<String, Px<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .flatMap(scoreOfALetter)
                        .reduce(Integer::max) ;

        // score of the word put on the board
        Function<String, Px<Integer>> score3 =
            word ->
                Px.fromArray(
                        score2.apply(word),
                        score2.apply(word),
                        bonusForDoubleLetter.apply(word),
                        bonusForDoubleLetter.apply(word),
                        Px.just(word.length() == 7 ? 50 : 0)
                )
                .flatMap(Px -> Px)
                .reduce(Integer::sum) ;

        Function<Function<String, Px<Integer>>, Px<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Px.fromIterable(() -> shakespeareWords.iterator())
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
                buildHistoOnScore.apply(score3)
                    .flatMap(map -> Px.fromIterable(() -> map.entrySet().iterator()))
                    .take(3)
                    .collect(
                        () -> new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                        }
                    )
                    .blockingFirst() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithRsc s = new ShakespearePlaysScrabbleWithRsc();
        s.init();
        System.out.println(s.measureThroughput());
    }
}