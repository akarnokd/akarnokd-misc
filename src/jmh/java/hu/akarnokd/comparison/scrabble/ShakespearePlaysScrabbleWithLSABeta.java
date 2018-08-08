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
import com.annimon.stream.function.*;
import com.annimon.stream.*;

import org.openjdk.jmh.annotations.*;

/**
 * Shakespeare plays Scrabble with Java Streams.
 * @author José
 */
public abstract class ShakespearePlaysScrabbleWithLSABeta extends ShakespearePlaysScrabble {


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
    public List<Entry<Integer, List<String>>> measureThroughput() {

        // Function to compute the score of a given word
        IntUnaryOperator scoreOfALetter = letter -> letterScores[letter - 'a'];

        // score of the same letters in a word
        ToIntFunction<Entry<Integer, Long>> letterScore =
                entry ->
                    letterScores[entry.getKey() - 'a'] *
                    Integer.min(
                        entry.getValue().intValue(),
                        scrabbleAvailableLetters[entry.getKey() - 'a']
                    );


        // Histogram of the letters in a given word
        Function<String, Map<Integer, Long>> histoOfLetters =
                word -> chars(word).boxed()
                            .collect(
                                Collectors.groupingBy(
                                    (Integer v) -> v,
                                    Collectors.counting()
                                )
                            );

        // number of blanks for a given letter
        ToLongFunction<Entry<Integer, Long>> blank =
                entry ->
                    Long.max(
                        0L,
                        entry.getValue() -
                        scrabbleAvailableLetters[entry.getKey() - 'a']
                    );

        // number of blanks for a given word
        Function<String, Long> nBlanks =
                word -> Stream.of(histoOfLetters.apply(word)
                            .entrySet())
                            .mapToLong(blank)
                            .sum();

        // can a word be written with 2 blanks?
        Predicate<String> checkBlanks = word -> nBlanks.apply(word) <= 2;

        // score taking blanks into account
        Function<String, Integer> score2 =
                word -> Stream.of(histoOfLetters.apply(word)
                            .entrySet())
                            .mapToInt(letterScore)
                            .sum();

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, IntStream> first3 = word -> chars(word).limit(3);
        Function<String, IntStream> last3 = word -> chars(word).skip(Integer.max(0, word.length() - 4));

        // Stream to be maxed
        Function<String, IntStream> toBeMaxed =
            word -> Stream.of(first3.apply(word), last3.apply(word))
                          .flatMapToInt(v -> v);

        // Bonus for double letter
        ToIntFunction<String> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .max()
                        .orElse(0);

        // score of the word put on the board
        Function<String, Integer> score3 =
            word ->
               2 * (score2.apply(word) + bonusForDoubleLetter.applyAsInt(word))
               + (word.length() == 7 ? 50 : 0);

        Function<Function<String, Integer>, Map<Integer, List<String>>> buildHistoOnScore =
                score -> buildShakerspeareWordsStream()
                                .filter(scrabbleWords::contains)
                                // .filter(canWrite)    // filter out the words that needs blanks
                                .filter(checkBlanks) // filter out the words that needs more than 2 blanks
                                .collect(
                                   Collectors.groupingBy(
                                      score,
                                      () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                      Collectors.toList()
                                   )
                                );


        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList =
                Stream.of(buildHistoOnScore.apply(score3).entrySet()
                    )
                    .limit(3)
                    .collect(Collectors.toList()) ;

//        System.out.println(finalList) ;

        return finalList ;
    }

    IntStream chars(String s) {
        return IntStream.range(0, s.length()).map(i -> s.charAt(i));
    }

    abstract Stream<String> buildShakerspeareWordsStream() ;
}