/*
 * Copyright (C) 2015 José Paumard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package hu.akarnokd.comparison.scrabble;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.comparison.IterableSpliterator;
import io.reactivex.*;
import io.reactivex.Observable;
import io.reactivex.functions.Function;

/**
 * Shakespeare plays Scrabble with RxJava 2 Observable.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithRxJava2Observable extends ShakespearePlaysScrabble {
    @SuppressWarnings({ "unchecked", "unused" })
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
    public List<Entry<Integer, List<String>>> measureThroughput() throws Exception {

        // Function to compute the score of a given word
        Function<Integer, Observable<Integer>> scoreOfALetter = letter -> Observable.just(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        Function<Entry<Integer, LongWrapper>, Observable<Integer>> letterScore =
                entry ->
                    Observable.just(
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ) ;

        Function<String, Observable<Integer>> toIntegerObservable =
                string -> Observable.fromIterable(IterableSpliterator.of(string.chars().boxed().spliterator())) ;

        // Histogram of the letters in a given word
        Function<String, Single<HashMap<Integer, LongWrapper>>> histoOfLetters =
                word -> toIntegerObservable.apply(word)
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
        Function<Entry<Integer, LongWrapper>, Observable<Long>> blank =
                entry ->
                    Observable.just(
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ) ;

        // number of blanks for a given word
        Function<String, Maybe<Long>> nBlanks =
                word -> histoOfLetters.apply(word)
                            .flatMapObservable(map -> Observable.fromIterable(() -> map.entrySet().iterator()))
                            .flatMap(blank)
                            .reduce(Long::sum) ;


        // can a word be written with 2 blanks?
        Function<String, Maybe<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .flatMap(l -> Maybe.just(l <= 2L)) ;

        // score taking blanks into account letterScore1
        Function<String, Maybe<Integer>> score2 =
                word -> histoOfLetters.apply(word)
                            .flatMapObservable(map -> Observable.fromIterable(() -> map.entrySet().iterator()))
                            .flatMap(letterScore)
                            .reduce(Integer::sum) ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Observable<Integer>> first3 =
                word -> Observable.fromIterable(IterableSpliterator.of(word.chars().boxed().limit(3).spliterator())) ;
        Function<String, Observable<Integer>> last3 =
                word -> Observable.fromIterable(IterableSpliterator.of(word.chars().boxed().skip(3).spliterator())) ;


        // Stream to be maxed
        Function<String, Observable<Integer>> toBeMaxed =
            word -> Observable.just(first3.apply(word), last3.apply(word))
                        .flatMap(observable -> observable) ;

        // Bonus for double letter
        Function<String, Maybe<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .flatMap(scoreOfALetter)
                        .reduce(Integer::max) ;

        // score of the word put on the board
        Function<String, Maybe<Integer>> score3 =
            word ->
                Observable.fromArray(
                        score2.apply(word),
                        score2.apply(word),
                        bonusForDoubleLetter.apply(word),
                        bonusForDoubleLetter.apply(word),
                        Maybe.just(word.length() == 7 ? 50 : 0)
                )
                .flatMap(observable -> observable.toObservable())
                .reduce(Integer::sum) ;

        Function<Function<String, Maybe<Integer>>, Single<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Observable.fromIterable(() -> shakespeareWords.iterator())
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).blockingGet())
                                .collect(
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.apply(word).blockingGet() ;
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
                    .flatMapObservable(map -> Observable.fromIterable(() -> map.entrySet().iterator()))
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
}