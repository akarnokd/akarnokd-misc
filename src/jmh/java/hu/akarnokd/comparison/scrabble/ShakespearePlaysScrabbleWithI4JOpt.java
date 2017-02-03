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

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.interactive.Interactive;
import hu.akarnokd.reactive4java.query.IterableBuilder;

/**
 * Shakespeare plays Scrabble with IterableBuilder optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithI4JOpt extends ShakespearePlaysScrabble {

    static IterableBuilder<Integer> chars(String word) {
        return IterableBuilder.range(0, word.length()).select(i -> (int)word.charAt(i));
    }

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

        //  to compute the score of a given word
        Func1<Integer, Integer> scoreOfALetter = letter -> letterScores[letter - 'a'];

        // score of the same letters in a word
        Func1<Entry<Integer, MutableLong>, Integer> letterScore =
                entry ->
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ;


        Func1<String, IterableBuilder<Integer>> toIntegerIterableBuilder =
                string -> chars(string);

        // Histogram of the letters in a given word
        Func1<String, IterableBuilder<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> toIntegerIterableBuilder.invoke(word)
                            .scan(
                                new HashMap<>(),
                                (HashMap<Integer, MutableLong> map, Integer value) ->
                                    {
                                        MutableLong newValue = map.get(value) ;
                                        if (newValue == null) {
                                            newValue = new MutableLong();
                                            map.put(value, newValue);
                                        }
                                        newValue.incAndSet();
                                        return map;
                                    }

                            )
                            .takeLast(1);

        // number of blanks for a given letter
        Func1<Entry<Integer, MutableLong>, Long> blank =
                entry ->
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ;

        // number of blanks for a given word
        Func1<String, IterableBuilder<Long>> nBlanks =
                word -> histoOfLetters.invoke(word)
                            .selectMany(map -> map.entrySet())
                            .select(blank)
                            .sumLong();


        // can a word be written with 2 blanks?
        Func1<String, IterableBuilder<Boolean>> checkBlanks =
                word -> nBlanks.invoke(word)
                            .select(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Func1<String, IterableBuilder<Integer>> score2 =
                word -> histoOfLetters.invoke(word)
                            .selectMany(map -> map.entrySet())
                            .select(letterScore)
                            .sumInt();

        // Placing the word on the board
        // Building the streams of first and last letters
        Func1<String, IterableBuilder<Integer>> first3 =
                word -> chars(word).take(3) ;
        Func1<String, IterableBuilder<Integer>> last3 =
                word -> skip(chars(word), 3) ;


        // Stream to be maxed
        Func1<String, IterableBuilder<Integer>> toBeMaxed =
            word -> first3.invoke(word).concat(last3.invoke(word))
            ;

        // Bonus for double letter
        Func1<String, IterableBuilder<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.invoke(word)
                        .select(scoreOfALetter)
                        .max();

        // score of the word put on the board
        Func1<String, IterableBuilder<Integer>> score3 =
            word ->
                score2.invoke(word).select(v -> v * 2)
                .concat(bonusForDoubleLetter.invoke(word).select(v -> v * 2))
                .concat(Interactive.singleton(word.length() == 7 ? 50 : 0))
                .sumInt();

        Func1<Func1<String, IterableBuilder<Integer>>, IterableBuilder<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> IterableBuilder.from(shakespeareWords)
                                .where(scrabbleWords::contains)
                                .where(word -> checkBlanks.invoke(word).first())
                                .scan(
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
                    .selectMany(map -> map.entrySet())
                    .take(3)
                    .scan(
                        new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                            return list;
                        }
                    )
                    .takeLast(1)
                    .first() ;

//        System.out.println(finalList2);

        return finalList2 ;
    }
    
    <T> IterableBuilder<T> skip(Iterable<T> source, int count) {
        return IterableBuilder.<T>from(() -> {
            Iterator<T> it = source.iterator();
            
            return new Iterator<T>() {
                int c = count;
                @Override
                public boolean hasNext() {
                    while (c != 0) {
                        if (it.hasNext()) {
                            it.next();
                        } else {
                            return false;
                        }
                    }
                    return it.hasNext();
                }

                @Override
                public T next() {
                    if (c != 0 && hasNext()) {
                        return it.next();
                    }
                    throw new NoSuchElementException();
                }
                
            };
        });
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithI4JOpt s = new ShakespearePlaysScrabbleWithI4JOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}