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

import hu.akarnokd.comparison.IterableSpliterator;
import rx.Observable;
import rx.functions.Func1;

/**
 * Shakespeare plays Scrabble with RxJava 1 (slightly modified).
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithRxJava1Beta extends ShakespearePlaysScrabble {
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

        // Function to compute the score of a given word
        Func1<Integer, Observable<Integer>> scoreOfALetter = letter -> Observable.just(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        Func1<Entry<Integer, LongWrapper>, Observable<Integer>> letterScore =
                entry ->
                    Observable.just(
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ) ;

        Func1<String, Observable<Integer>> toIntegerObservable =
                string -> Observable.from(IterableSpliterator.of(string.chars().boxed().spliterator())) ;

        // Histogram of the letters in a given word
        Func1<String, Observable<HashMap<Integer, LongWrapper>>> histoOfLetters =
                word -> toIntegerObservable.call(word)
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
        Func1<Entry<Integer, LongWrapper>, Observable<Long>> blank =
                entry ->
                    Observable.just(
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ) ;

        // number of blanks for a given word
        Func1<String, Observable<Long>> nBlanks =
                word -> histoOfLetters.call(word)
                            .flatMap(map -> Observable.from(() -> map.entrySet().iterator()))
                            .flatMap(blank)
                            .reduce(Long::sum) ;


        // can a word be written with 2 blanks?
        Func1<String, Observable<Boolean>> checkBlanks =
                word -> nBlanks.call(word)
                            .flatMap(l -> Observable.just(l <= 2L)) ;

        // score taking blanks into account letterScore1
        Func1<String, Observable<Integer>> score2 =
                word -> histoOfLetters.call(word)
                            .flatMap(map -> Observable.from(() -> map.entrySet().iterator()))
                            .flatMap(letterScore)
                            .reduce(Integer::sum) ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Func1<String, Observable<Integer>> first3 =
                word -> Observable.from(IterableSpliterator.of(word.chars().boxed().limit(3).spliterator())) ;
        Func1<String, Observable<Integer>> last3 =
                word -> Observable.from(IterableSpliterator.of(word.chars().boxed().skip(3).spliterator())) ;


        // Stream to be maxed
        Func1<String, Observable<Integer>> toBeMaxed =
            word -> Observable.just(first3.call(word), last3.call(word))
                        .flatMap(observable -> observable) ;

        // Bonus for double letter
        Func1<String, Observable<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.call(word)
                        .flatMap(scoreOfALetter)
                        .reduce(Integer::max) ;

        // score of the word put on the board
        Func1<String, Observable<Integer>> score3 =
            word ->
                Observable.just(
                        score2.call(word).map(v -> v * 2),
                        bonusForDoubleLetter.call(word).map(v -> v * 2),
                        Observable.just(word.length() == 7 ? 50 : 0)
                )
                .flatMap(observable -> observable)
                .reduce(Integer::sum) ;

        Func1<Func1<String, Observable<Integer>>, Observable<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Observable.from(() -> shakespeareWords.iterator())
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.call(word).toBlocking().first())
                                .collect(
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.call(word).toBlocking().first() ;
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
                buildHistoOnScore.call(score3)
                    .flatMap(map -> Observable.from(() -> map.entrySet().iterator()))
                    .take(3)
                    .collect(
                        () -> new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                        }
                    )
                    .toBlocking()
                    .first() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }
}