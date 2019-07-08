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

import hu.akarnokd.rxjava.MathObservable;
import rx.Observable;
import rx.functions.Func1;

/**
 * Shakespeare plays Scrabble with RxJava 1 optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithRxJava1Opt extends ShakespearePlaysScrabble {


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

    static Observable<Integer> chars(String word) {
        return Observable.range(0, word.length()).map(i -> (int)word.charAt(i));
    }

    @SuppressWarnings("unused")
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


        Func1<String, Observable<Integer>> toIntegerObservable =
                string -> chars(string);

        // Histogram of the letters in a given word
        Func1<String, Observable<HashMap<Integer, MutableLong>>> histoOfLetters =
                word -> toIntegerObservable.call(word)
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
        Func1<Entry<Integer, MutableLong>, Long> blank =
                entry ->
                        Long.max(
                            0L,
                            entry.getValue().get() -
                            scrabbleAvailableLetters[entry.getKey() - 'a']
                        )
                    ;

        // number of blanks for a given word
        Func1<String, Observable<Long>> nBlanks =
                word -> MathObservable.sumLong(histoOfLetters.call(word)
                            .flatMapIterable(map -> map.entrySet())
                            .map(blank)
                            ) ;


        // can a word be written with 2 blanks?
        Func1<String, Observable<Boolean>> checkBlanks =
                word -> nBlanks.call(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Func1<String, Observable<Integer>> score2 =
                word -> MathObservable.sumInteger(histoOfLetters.call(word)
                            .flatMapIterable(map -> map.entrySet())
                            .map(letterScore)
                            ) ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Func1<String, Observable<Integer>> first3 =
                word -> chars(word).take(3) ;
        Func1<String, Observable<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        Func1<String, Observable<Integer>> toBeMaxed =
            word -> Observable.concat(first3.call(word), last3.call(word))
            ;

        // Bonus for double letter
        Func1<String, Observable<Integer>> bonusForDoubleLetter =
            word -> MathObservable.max(toBeMaxed.call(word)
                        .map(scoreOfALetter)
                        ) ;

        // score of the word put on the board
        Func1<String, Observable<Integer>> score3 =
            word ->
                MathObservable.sumInteger(Observable.concat(
                        score2.call(word),
                        bonusForDoubleLetter.call(word)
                )
                ).map(v -> 2 * v + (word.length() == 7 ? 50 : 0)) ;

        Func1<Func1<String, Observable<Integer>>, Observable<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Observable.from(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.call(word).toBlocking().first())
                                .collect(
                                    () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                    (TreeMap<Integer, List<String>> map, String word) -> {
                                        Integer key = score.call(word).toBlocking().first() ;
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
                buildHistoOnScore.call(score3)
                    .flatMapIterable(map -> map.entrySet())
                    .take(3)
                    .collect(
                        () -> new ArrayList<Entry<Integer, List<String>>>(),
                        (list, entry) -> {
                            list.add(entry) ;
                        }
                    )
                    .toBlocking().first() ;


//        System.out.println(finalList2);

        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithRxJava1Opt s = new ShakespearePlaysScrabbleWithRxJava1Opt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}