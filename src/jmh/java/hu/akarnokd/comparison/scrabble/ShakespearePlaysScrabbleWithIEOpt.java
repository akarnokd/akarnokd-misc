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
import java.util.function.Function;

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.enumerables.IEnumerable;

/**
 * Shakespeare plays Scrabble with Ix optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithIEOpt extends ShakespearePlaysScrabble {

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


        Function<String, IEnumerable<Integer>> toIntegerIx =
                string -> IEnumerable.characters(string);

        // Histogram of the letters in a given word
        Function<String, IEnumerable<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> toIntegerIx.apply(word)
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
        Function<String, IEnumerable<Long>> nBlanks =
                word -> histoOfLetters.apply(word)
                            .flatMapIterable(map -> map.entrySet())
                            .map(blank)
                            .sumLong();


        // can a word be written with 2 blanks?
        Function<String, IEnumerable<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, IEnumerable<Integer>> score2 =
                word -> histoOfLetters.apply(word)
                            .flatMapIterable(map -> map.entrySet())
                            .map(letterScore)
                            .sumInt();

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, IEnumerable<Integer>> first3 =
                word -> IEnumerable.characters(word).take(3) ;
        Function<String, IEnumerable<Integer>> last3 =
                word -> IEnumerable.characters(word).skip(3) ;


        // Stream to be maxed
        Function<String, IEnumerable<Integer>> toBeMaxed =
            word -> IEnumerable.concatArray(first3.apply(word), last3.apply(word))
            ;

        // Bonus for double letter
        Function<String, IEnumerable<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .maxInt();

        // score of the word put on the board
        Function<String, IEnumerable<Integer>> score3 =
            word ->
//        IEnumerable.concatArray(
//                        score2.apply(word).map(v -> v * 2),
//                        bonusForDoubleLetter.apply(word).map(v -> v * 2),
//                        IEnumerable.just(word.length() == 7 ? 50 : 0)
//                )
//                .sumInt();
        IEnumerable.concatArray(
                score2.apply(word),
                bonusForDoubleLetter.apply(word)
        )
        .sumInt().map(v -> 2 * v + (word.length() == 7 ? 50 : 0));

        Function<Function<String, IEnumerable<Integer>>, IEnumerable<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> IEnumerable.fromIterable(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).first())
                                .collect(
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.apply(word).first() ;
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
                    .flatMapIterable(map -> map.entrySet())
                    .take(3)
                    .collect(
                        () -> new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                        }
                    )
                    .first() ;

//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithIEOpt s = new ShakespearePlaysScrabbleWithIEOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}