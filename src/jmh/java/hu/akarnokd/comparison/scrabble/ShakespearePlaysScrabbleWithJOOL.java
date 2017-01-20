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

import org.jooq.lambda.Seq;
import org.openjdk.jmh.annotations.*;

import hu.akarnokd.comparison.IterableSpliterator;

/**
 * Shakespeare plays Scrabble with JOOL.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithJOOL extends ShakespearePlaysScrabble {

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

        // Function to compute the score of a given word
        Function<Integer, Seq<Integer>> scoreOfALetter = letter -> Seq.of(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        Function<Entry<Integer, LongWrapper>, Seq<Integer>> letterScore =
                entry ->
                    Seq.of(
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ) ;

        Function<String, Seq<Integer>> toIntegerIx =
                string -> Seq.seq(IterableSpliterator.of(string.chars().boxed().spliterator())) ;

        // Histogram of the letters in a given word
        Function<String, Seq<HashMap<Integer, LongWrapper>>> histoOfLetters =
                word -> {
                    HashMap<Integer, LongWrapper> map = new HashMap<>();
                    return toIntegerIx.apply(word)
                                .map(value ->
                                        {
                                            LongWrapper newValue = map.get(value) ;
                                            if (newValue == null) {
                                                newValue = () -> 0L ;
                                            }
                                            map.put(value, newValue.incAndSet()) ;
                                            return map;
                                        }
                                )
                                .skip(Long.MAX_VALUE)
                                .append(map)
                                ;
                }
        ;

        // number of blanks for a given letter
        Function<Entry<Integer, LongWrapper>, Seq<Long>> blank =
                entry ->
                    Seq.of(
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ) ;

        // number of blanks for a given word
        Function<String, Seq<Long>> nBlanks =
                word -> {
                    long[] sum = { 0L };
                    return histoOfLetters.apply(word)
                                .flatMap(map -> Seq.seq(() -> map.entrySet().iterator()))
                                .flatMap(blank)
                                .map(v -> sum[0] += v)
                                .skip(Long.MAX_VALUE)
                                .append(0L)
                                .map(v -> sum[0])
                                ;
                }
        ;


        // can a word be written with 2 blanks?
        Function<String, Seq<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .flatMap(l -> Seq.of(l <= 2L)) ;

        // score taking blanks into account letterScore1
        Function<String, Seq<Integer>> score2 =
                word -> {
                    int[] sum = { 0 };
                    return histoOfLetters.apply(word)
                                .flatMap(map -> Seq.seq(() -> map.entrySet().iterator()))
                                .flatMap(letterScore)
                                .map(v -> sum[0] += v)
                                .skip(Long.MAX_VALUE)
                                .append(0)
                                .map(v -> sum[0])
                                ;
                }
        ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Seq<Integer>> first3 =
                word -> Seq.seq(IterableSpliterator.of(word.chars().boxed().limit(3).spliterator())) ;
        Function<String, Seq<Integer>> last3 =
                word -> Seq.seq(IterableSpliterator.of(word.chars().boxed().skip(3).spliterator())) ;


        // Stream to be maxed
        Function<String, Seq<Integer>> toBeMaxed =
            word -> Seq.of(first3.apply(word), last3.apply(word))
                        .flatMap(observable -> observable) ;

        // Bonus for double letter
        Function<String, Seq<Integer>> bonusForDoubleLetter =
            word -> {
                int[] max = { 0 };
                return toBeMaxed.apply(word)
                            .flatMap(scoreOfALetter)
                            .map(v -> max[0] = Math.max(max[0], v))
                            .skip(Long.MAX_VALUE)
                            .append(0)
                            .map(v -> max[0]);
            };

        // score of the word put on the board
        Function<String, Seq<Integer>> score3 =
            word ->
                {
                    int[] sum = { 0 };
                    return Seq.of(
                            score2.apply(word),
                            score2.apply(word),
                            bonusForDoubleLetter.apply(word),
                            bonusForDoubleLetter.apply(word),
                            Seq.of(word.length() == 7 ? 50 : 0)
                    )
                    .flatMap(observable -> observable)
                    .map(v -> sum[0] += v)
                    .skip(Long.MAX_VALUE)
                    .append(0)
                    .map(v -> {
                        return sum[0];
                    });
                }
        ;

        Function<Function<String, Seq<Integer>>, Seq<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> {
                    TreeMap<Integer, List<String>> map = new TreeMap<Integer, List<String>>(Comparator.reverseOrder());
                    return Seq.seq(() -> shakespeareWords.iterator())
                                    .filter(scrabbleWords::contains)
                                    .filter(word -> checkBlanks.apply(word).findFirst().orElse(false))
                                    .map(word -> {
                                                Integer key = score.apply(word).findFirst().orElse(0);
                                                List<String> list = map.get(key) ;
                                                if (list == null) {
                                                    list = new ArrayList<>() ;
                                                    map.put(key, list) ;
                                                }
                                                list.add(word) ;
                                                return map;
                                            }
                                    )
                                    .skip(Long.MAX_VALUE)
                                    .append(map)
                                    ;
                }
        ;

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                buildHistoOnScore.apply(score3)
                    .flatMap(map -> Seq.seq(() -> map.entrySet().iterator()))
                    .take(3)
                    .scanLeft(new ArrayList<Entry<Integer, List<String>>>(), (list, entry) -> {
                        list.add(entry);
                        return list;
                    })
                    .findLast().orElse(new ArrayList<>()) ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithJOOL s = new ShakespearePlaysScrabbleWithJOOL();
        s.init();
        System.out.println(s.measureThroughput());
    }
}