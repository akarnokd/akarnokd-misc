/*
 * Copyright (C) 2019 José Paumard
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

import static hu.akarnokd.comparison.FluentIterables.*;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;

/**
 * Shakespeare play Scrabble with Guava optimized.
 * @author JosÃ©
 */
public class ShakespearePlaysScrabbleWithGuavaOpt extends ShakespearePlaysScrabble {

    static FluentIterable<Integer> chars(String word) {
        return new FluentIterable<Integer>() {

            @Override
            public Iterator<Integer> iterator() {
                return new CharIterator();
            }

            final class CharIterator implements Iterator<Integer> {
                int index;

                @Override
                public boolean hasNext() {
                    return index < word.length();
                }

                @Override
                public Integer next() {
                    return (int)word.charAt(index++);
                }
            }
        };
    }

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


        Function<String, FluentIterable<Integer>> toIntegerFluentIterable =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, FluentIterable<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> collect(toIntegerFluentIterable.apply(word)
                            ,
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
        Function<String, FluentIterable<Long>> nBlanks =
                word -> sumLong(histoOfLetters.apply(word)
                            .transformAndConcat(map -> map.entrySet())
                            .transform(blank)
                            );


        // can a word be written with 2 blanks?
        Function<String, FluentIterable<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .transform(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, FluentIterable<Integer>> score2 =
                word -> sumInt(histoOfLetters.apply(word)
                            .transformAndConcat(map -> map.entrySet())
                            .transform(letterScore)
                            );

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, FluentIterable<Integer>> first3 =
                word -> chars(word).limit(3) ;
        Function<String, FluentIterable<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        Function<String, FluentIterable<Integer>> toBeMaxed =
            word -> first3.apply(word).append(last3.apply(word))
            ;

        // Bonus for double letter
        Function<String, FluentIterable<Integer>> bonusForDoubleLetter =
            word -> maxInt(toBeMaxed.apply(word)
                        .transform(scoreOfALetter)
                        );

        // score of the word put on the board
        Function<String, FluentIterable<Integer>> score3 =
            word ->
                sumInt(
                    score2.apply(word)
                    .append(bonusForDoubleLetter.apply(word))
                )
                .transform(v -> 2 * v + (word.length() == 7 ? 50 : 0));

        Function<Function<String, FluentIterable<Integer>>, FluentIterable<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> collect(FluentIterable.from(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).first().get())
                                ,
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.apply(word).first().get() ;
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
                collect(buildHistoOnScore.apply(score3)
                    .transformAndConcat(map -> map.entrySet())
                    .limit(3)
                    ,
                        () -> new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                        }
                    )
                    .first().get() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithGuavaOpt s = new ShakespearePlaysScrabbleWithGuavaOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}