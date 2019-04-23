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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.*;

import ix.*;

/**
 * Shakespeare plays Scrabble with Ix optimized.
 * @author JosÃ©
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