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

import ix.*;

/**
 * Shakespeare plays Scrabble with Ix optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithIxOpt extends ShakespearePlaysScrabble {

    static Ix<Integer> chars(String word) {
        //return Ix.range(0, word.length()).map(i -> (int)word.charAt(i));
        return Ix.characters(word);
    }

    @SuppressWarnings({ "unchecked", "unused" })
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
        IxFunction<Integer, Integer> scoreOfALetter = letter -> letterScores[letter - 'a'];

        // score of the same letters in a word
        IxFunction<Entry<Integer, MutableLong>, Integer> letterScore =
                entry ->
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ;


        IxFunction<String, Ix<Integer>> toIntegerIx =
                string -> chars(string);

        // Histogram of the letters in a given word
        IxFunction<String, Ix<HashMap<Integer, MutableLong>>> histoOfLetters =
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
        IxFunction<Entry<Integer, MutableLong>, Long> blank =
                entry ->
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ;

        // number of blanks for a given word
        IxFunction<String, Ix<Long>> nBlanks =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> map.entrySet())
                            .map(blank)
                            .sumLong();


        // can a word be written with 2 blanks?
        IxFunction<String, Ix<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        IxFunction<String, Ix<Integer>> score2 =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> map.entrySet())
                            .map(letterScore)
                            .sumInt();

        // Placing the word on the board
        // Building the streams of first and last letters
        IxFunction<String, Ix<Integer>> first3 =
                word -> chars(word).take(3) ;
        IxFunction<String, Ix<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        IxFunction<String, Ix<Integer>> toBeMaxed =
            word -> Ix.concatArray(first3.apply(word), last3.apply(word))
            ;

        // Bonus for double letter
        IxFunction<String, Ix<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .maxInt();

        // score of the word put on the board
        IxFunction<String, Ix<Integer>> score3 =
            word ->
                Ix.concatArray(
                        score2.apply(word),
                        bonusForDoubleLetter.apply(word)
                )
                .sumInt().map(v -> 2 * v + (word.length() == 7 ? 50 : 0));

        IxFunction<IxFunction<String, Ix<Integer>>, Ix<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Ix.from(shakespeareWords)
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
                    .flatMap(map -> map.entrySet())
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
        ShakespearePlaysScrabbleWithIxOpt s = new ShakespearePlaysScrabbleWithIxOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}