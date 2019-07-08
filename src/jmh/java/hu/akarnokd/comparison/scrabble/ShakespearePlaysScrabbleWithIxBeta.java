/*
 * Copyright (C) 2019 Jos� Paumard
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

import hu.akarnokd.comparison.IterableSpliterator;
import ix.*;

/**
 * Shakespeare plays Scrabble with Ix (slightly modified).
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithIxBeta extends ShakespearePlaysScrabble {

    /*
    Result: 12,690 ±(99.9%) 0,148 s/op [Average]
              Statistics: (min, avg, max) = (12,281, 12,690, 12,784), stdev = 0,138
              Confidence interval (99.9%): [12,543, 12,838]
              Samples, N = 15
                    mean =     12,690 ±(99.9%) 0,148 s/op
                     min =     12,281 s/op
              p( 0,0000) =     12,281 s/op
              p(50,0000) =     12,717 s/op
              p(90,0000) =     12,784 s/op
              p(95,0000) =     12,784 s/op
              p(99,0000) =     12,784 s/op
              p(99,9000) =     12,784 s/op
              p(99,9900) =     12,784 s/op
              p(99,9990) =     12,784 s/op
              p(99,9999) =     12,784 s/op
                     max =     12,784 s/op


            # Run complete. Total time: 00:06:26

            Benchmark                                               Mode  Cnt   Score   Error  Units
            ShakespearePlaysScrabbleWithRxJava.measureThroughput  sample   15  12,690 ± 0,148   s/op

            Benchmark                                              Mode  Cnt       Score      Error  Units
            ShakespearePlaysScrabbleWithRxJava.measureThroughput   avgt   15  250074,776 ± 7736,734  us/op
            ShakespearePlaysScrabbleWithStreams.measureThroughput  avgt   15   29389,903 ± 1115,836  us/op

    */
    @SuppressWarnings({ "unchecked", "unused" })
//    @Benchmark
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

        // Function to compute the score of a given word
        IxFunction<Integer, Ix<Integer>> scoreOfALetter = letter -> Ix.just(letterScores[letter - 'a']) ;

        // score of the same letters in a word
        IxFunction<Entry<Integer, LongWrapper>, Ix<Integer>> letterScore =
                entry ->
                    Ix.just(
                        letterScores[entry.getKey() - 'a'] *
                        Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            )
                    ) ;

        IxFunction<String, Ix<Integer>> toIntegerIx =
                string -> Ix.from(IterableSpliterator.of(string.chars().boxed().spliterator())) ;

        // Histogram of the letters in a given word
        IxFunction<String, Ix<HashMap<Integer, LongWrapper>>> histoOfLetters =
                word -> toIntegerIx.apply(word)
                            .collect(
                                () -> new HashMap<>(),
                                (HashMap<Integer, LongWrapper> map, Integer value) ->
                                    {
                                        LongWrapper newValue = map.get(value) ;
                                        if (newValue == null) {
                                            newValue = () -> 0L ;
                                        }
                                        map.put(value, newValue.incAndSet()) ;
                                    }

                            ) ;

        // number of blanks for a given letter
        IxFunction<Entry<Integer, LongWrapper>, Ix<Long>> blank =
                entry ->
                    Ix.just(
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ) ;

        // number of blanks for a given word
        IxFunction<String, Ix<Long>> nBlanks =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> Ix.from(() -> map.entrySet().iterator()))
                            .flatMap(blank)
                            .sumLong();


        // can a word be written with 2 blanks?
        IxFunction<String, Ix<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .flatMap(l -> Ix.just(l <= 2L)) ;

        // score taking blanks into account letterScore1
        IxFunction<String, Ix<Integer>> score2 =
                word -> histoOfLetters.apply(word)
                            .flatMap(map -> Ix.from(() -> map.entrySet().iterator()))
                            .flatMap(letterScore)
                            .sumInt();

        // Placing the word on the board
        // Building the streams of first and last letters
        IxFunction<String, Ix<Integer>> first3 =
                word -> Ix.from(IterableSpliterator.of(word.chars().boxed().limit(3).spliterator())) ;
        IxFunction<String, Ix<Integer>> last3 =
                word -> Ix.from(IterableSpliterator.of(word.chars().boxed().skip(3).spliterator())) ;


        // Stream to be maxed
        IxFunction<String, Ix<Integer>> toBeMaxed =
            word -> Ix.fromArray(first3.apply(word), last3.apply(word))
                        .flatMap(observable -> observable) ;

        // Bonus for double letter
        IxFunction<String, Ix<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .flatMap(scoreOfALetter)
                        .maxInt();

        // score of the word put on the board
        IxFunction<String, Ix<Integer>> score3 =
            word ->
                Ix.fromArray(
                        score2.apply(word).map(v -> v * 2),
                        bonusForDoubleLetter.apply(word).map(v -> v * 2),
                        Ix.just(word.length() == 7 ? 50 : 0)
                )
                .flatMap(observable -> observable)
                .sumInt() ;

        IxFunction<IxFunction<String, Ix<Integer>>, Ix<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Ix.from(() -> shakespeareWords.iterator())
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
                    .flatMap(map -> Ix.from(() -> map.entrySet().iterator()))
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
}