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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import hu.akarnokd.rxjava2.*;

import java.util.function.Function;
import reactor.core.publisher.*;

/**
 * Shakespeare plays Scrabble with Reactor optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithReactor3Opt extends ShakespearePlaysScrabble {

    static Flux<Integer> chars(String word) {
        //return Flux.range(0, word.length()).map(i -> (int)word.charAt(i));
        return new FluxCharSequence(word);
    }

    @SuppressWarnings("unused")
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


        Function<String, Flux<Integer>> toIntegerFlux =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, Mono<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> toIntegerFlux.apply(word)
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

                            );

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
        Function<String, Mono<Long>> nBlanks =
                word -> Rx2Math.sumLong(histoOfLetters.apply(word)
                            .flatMapIterable(map -> map.entrySet())
                            .map(blank)
                            );


        // can a word be written with 2 blanks?
        Function<String, Mono<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, Mono<Integer>> score2 =
                word -> Rx2Math.sumInt(histoOfLetters.apply(word)
                            .flatMapIterable(map -> map.entrySet())
                            .map(letterScore)
                            );

        // Placing the word on the board
        // Building the Fluxs of first and last letters
        Function<String, Flux<Integer>> first3 =
                word -> chars(word).take(3) ;
        Function<String, Flux<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Flux to be maxed
        Function<String, Flux<Integer>> toBeMaxed =
            word -> Flux.concat(first3.apply(word), last3.apply(word))
            ;

        // Bonus for double letter
        Function<String, Mono<Integer>> bonusForDoubleLetter =
            word -> Rx2Math.maxInt(toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        );

        // score of the word put on the board
        Function<String, Mono<Integer>> score3 =
            word ->
                Rx2Math.sumInt(Flux.concat(
                        score2.apply(word),
                        bonusForDoubleLetter.apply(word)
                )
                ).map(v -> 2 * v + (word.length() == 7 ? 50 : 0));

        Function<Function<String, Mono<Integer>>, Mono<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Flux.fromIterable(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).block())
                                .collect(
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.apply(word).block() ;
                                        List<String> list = map.get(key) ;
                                        if (list == null) {
                                            list = new ArrayList<>() ;
                                            map.put(key, list) ;
                                        }
                                        list.add(word) ;
                                    }
                                );

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
                    .block();


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithReactor3Opt s = new ShakespearePlaysScrabbleWithReactor3Opt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}