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

import hu.akarnokd.rxjava2.math.MathFlowable;
import hu.akarnokd.rxjava2.string.StringFlowable;
import io.reactivex.*;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * Shakespeare plays Scrabble with RxJava 2 parallel.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithRxJava2ParallelOpt extends ShakespearePlaysScrabble {

    static Flowable<Integer> chars(String word) {
//        return Flowable.range(0, word.length()).map(i -> (int)word.charAt(i));
        return StringFlowable.characters(word);
    }

    final Scheduler scheduler = Schedulers.computation(); // = new WeakParallelScheduler();

    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
        iterations = 5
    )
    @Measurement(
        iterations = 5, time = 1
    )
    @Fork(1)
    @SuppressWarnings("unused")
    public List<Entry<Integer, List<String>>> measureThroughput() throws Exception {

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
                word -> MathFlowable.sumLong(histoOfLetters.apply(word)
                            .toFlowable()
                            .flatMapIterable(map -> map.entrySet())
                            .map(blank)
                            )
                            ;


        // can a word be written with 2 blanks?
        Function<String, Flowable<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, Flowable<Integer>> score2 =
                word -> MathFlowable.sumInt(histoOfLetters.apply(word)
                            .toFlowable()
                            .flatMapIterable(map -> map.entrySet())
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
//                Flowable.fromArray(
//                        score2.apply(word),
//                        score2.apply(word),
//                        bonusForDoubleLetter.apply(word),
//                        bonusForDoubleLetter.apply(word),
//                        Flowable.just(word.length() == 7 ? 50 : 0)
//                )
//                .flatMap(Flowable -> Flowable)
                MathFlowable.sumInt(Flowable.concat(
                        score2.apply(word).map(v -> v * 2),
                        bonusForDoubleLetter.apply(word).map(v -> v * 2),
                        Flowable.just(word.length() == 7 ? 50 : 0)
                )
                ) ;

        Function<Function<String, Flowable<Integer>>, Flowable<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score ->
                Flowable.fromIterable(shakespeareWords)
                .parallel()
                .runOn(scheduler)
                .filter(scrabbleWords::contains)
                .filter(word -> checkBlanks.apply(word).blockingFirst())
                .collect(
                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                    (TreeMap<Integer, List<String>> map, String word) -> {
                        Integer key = score.apply(word).blockingFirst();
                        List<String> list = map.get(key) ;
                        if (list == null) {
                            list = new ArrayList<>() ;
                            map.put(key, list) ;
                        }
                        list.add(word) ;
                    }
                )
                .reduce((m1, m2) -> {
                    for (Map.Entry<Integer, List<String>> e : m2.entrySet()) {
                        List<String> list = m1.get(e.getKey());
                        if (list == null) {
                            m1.put(e.getKey(), e.getValue());
                        } else {
                            list.addAll(e.getValue());
                        }
                    }
                    return m1;
                });

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                buildHistoOnScore.apply(score3)
                    .flatMapIterable(map -> map.entrySet())
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
        ShakespearePlaysScrabbleWithRxJava2ParallelOpt s = new ShakespearePlaysScrabbleWithRxJava2ParallelOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}