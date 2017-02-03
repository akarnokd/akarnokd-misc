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
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Observable;
import hu.akarnokd.reactive4java.query.ObservableBuilder;
import hu.akarnokd.reactive4java.util.Functions;

/**
 * Shakespeare plays Scrabble with Reactive4Java.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithR4J extends ShakespearePlaysScrabble {

    @SuppressWarnings({ "unchecked", "unused" })
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
        iterations = 5
    )
    @Measurement(
        iterations = 5
    )
    @Fork(1)
    public List<Entry<Integer, List<String>>> measureThroughput() throws InterruptedException {

//        Schedulers.setDefault(new CurrentThreadScheduler());

        // Function to compute the score of a given word
        Func1<Integer, ObservableBuilder<Integer>> scoreOfALetter = letter -> ObservableBuilder.from(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        Func1<Entry<Integer, LongWrapper>, ObservableBuilder<Integer>> letterScore =
                entry ->
                    ObservableBuilder.from(
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ) ;

        Func1<String, ObservableBuilder<Integer>> toIntegerIx =
                string -> ObservableBuilder.from(IterableSpliterator.of(string.chars().boxed().spliterator())) ;

        // Histogram of the letters in a given word
        Func1<String, ObservableBuilder<HashMap<Integer, LongWrapper>>> histoOfLetters =
                word -> toIntegerIx.invoke(word)
                            .aggregate(new HashMap<>(), 
                                (HashMap<Integer, LongWrapper> map, Integer value) ->
                                    {
                                        LongWrapper newValue = map.get(value) ;
                                        if (newValue == null) {
                                            newValue = () -> 0L ;
                                        }
                                        map.put(value, newValue.incAndSet()) ;
                                        return map;
                                    }

                            )
                            .takeLast(1)
                            ;

        // number of blanks for a given letter
        Func1<Entry<Integer, LongWrapper>, ObservableBuilder<Long>> blank =
                entry ->
                    ObservableBuilder.from(
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ) ;

        // number of blanks for a given word
        Func1<String, ObservableBuilder<Long>> nBlanks =
                word -> histoOfLetters.invoke(word)
                            .selectMany((Func1<HashMap<Integer, LongWrapper>, Observable<Entry<Integer, LongWrapper>>>)map -> ObservableBuilder.from(() -> map.entrySet().iterator()))
                            .selectMany(blank)
                            .sumLong();


        // can a word be written with 2 blanks?
        Func1<String, ObservableBuilder<Boolean>> checkBlanks =
                word -> nBlanks.invoke(word)
                            .selectMany((Func1<Long, Observable<Boolean>>)l -> ObservableBuilder.from(l <= 2L)) ;

        // score taking blanks into account letterScore1
        Func1<String, ObservableBuilder<Integer>> score2 =
                word -> histoOfLetters.invoke(word)
                            .selectMany((Func1<HashMap<Integer, LongWrapper>, Observable<Entry<Integer, LongWrapper>>>)map -> ObservableBuilder.from(() -> map.entrySet().iterator()))
                            .selectMany(letterScore)
                            .sumInt();

        // Placing the word on the board
        // Building the streams of first and last letters
        Func1<String, ObservableBuilder<Integer>> first3 =
                word -> ObservableBuilder.from(IterableSpliterator.of(word.chars().boxed().limit(3).spliterator())) ;
        Func1<String, ObservableBuilder<Integer>> last3 =
                word -> ObservableBuilder.from(IterableSpliterator.of(word.chars().boxed().skip(3).spliterator())) ;


        // Stream to be maxed
        Func1<String, ObservableBuilder<Integer>> toBeMaxed =
            word -> ObservableBuilder.from(first3.invoke(word), last3.invoke(word))
                        .selectMany(Functions.identity()) ;

        // Bonus for double letter
        Func1<String, ObservableBuilder<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.invoke(word)
                        .selectMany(scoreOfALetter)
                        .max();

        // score of the word put on the board
        Func1<String, ObservableBuilder<Integer>> score3 =
            word ->
                ObservableBuilder.from(
                        score2.invoke(word),
                        score2.invoke(word),
                        bonusForDoubleLetter.invoke(word),
                        bonusForDoubleLetter.invoke(word),
                        ObservableBuilder.from(word.length() == 7 ? 50 : 0)
                )
                .selectMany(Functions.identity())
                .sumInt() ;

        Func1<Func1<String, ObservableBuilder<Integer>>, ObservableBuilder<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> ObservableBuilder.from(() -> shakespeareWords.iterator())
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
                                ) 
                                .takeLast(1);

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                buildHistoOnScore.invoke(score3)
                    .selectMany((Func1<TreeMap<Integer, List<String>>, Observable<Entry<Integer, List<String>>>>)map -> ObservableBuilder.from(() -> map.entrySet().iterator()))
                    .take(3)
                    .aggregate(
                        new ArrayList<Entry<Integer, List<String>>>(),
                        (ArrayList<Entry<Integer, List<String>>> list, Entry<Integer, List<String>> entry) -> {
                            list.add(entry) ;
                            return list;
                        }
                    )
                    .takeLast(1)
                    .first() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithR4J s = new ShakespearePlaysScrabbleWithR4J();
        s.init();
        System.out.println(s.measureThroughput());
    }
}