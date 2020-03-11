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
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.*;

import com.typesafe.config.*;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;

/**
 * Shakespeare with Akka-Stream Optimized.
 * 
 * @author JosÃ©
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithAkkaStreamBeta extends ShakespearePlaysScrabble {

    ActorSystem actorSystem;

    ActorMaterializer materializer;

    @Setup
    public void setup() {

        Config cfg = ConfigFactory.parseResources(ShakespearePlaysScrabbleWithAkkaStreamOpt.class, "/akka-streams.conf").resolve();
        actorSystem = ActorSystem.create("sys", cfg);
        materializer = ActorMaterializer.create(actorSystem);

    }

    @TearDown
    public void teardown() {
        actorSystem.terminate();
    }

    static Source<Integer, NotUsed> chars(String word) {
        return Source.from(ix.Ix.characters(word));
    }

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
    public List<Entry<Integer, List<String>>> measureThroughput() throws Exception {

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


        Function<String, Source<Integer, NotUsed>> toIntegerIx =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, Source<HashMap<Integer, MutableLong>, NotUsed>> histoOfLetters =
                word -> {
                    HashMap<Integer, MutableLong> map = new HashMap<>();
                    return toIntegerIx.apply(word)
                    .map(value -> {
                        MutableLong newValue = map.get(value) ;
                        if (newValue == null) {
                            newValue = new MutableLong();
                            map.put(value, newValue);
                        }
                        newValue.incAndSet();
                        return map;
                    })
                    .drop(Long.MAX_VALUE)
                    .concat(Source.single(map))
                    ;
                };
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
        Function<String, Source<Long, NotUsed>> nBlanks =
                word -> histoOfLetters.apply(word)
                            .mapConcat(map -> map.entrySet())
                            .map(blank)
                            .reduce((a, b) -> a + b);


        // can a word be written with 2 blanks?
        Function<String, Source<Boolean, NotUsed>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, Source<Integer, NotUsed>> score2 =
                word -> histoOfLetters.apply(word)
                            .mapConcat(map -> map.entrySet())
                            .map(letterScore)
                            .reduce((a, b) -> a + b);

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Source<Integer, NotUsed>> first3 =
                word -> chars(word).take(3) ;
        Function<String, Source<Integer, NotUsed>> last3 =
                word -> chars(word).drop(3) ;


        // Stream to be maxed
        Function<String, Source<Integer, NotUsed>> toBeMaxed =
            word -> first3.apply(word).concat(last3.apply(word))
            ;

        // Bonus for double letter
        Function<String, Source<Integer, NotUsed>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .reduce((a, b) -> Math.max(a, b));

        // score of the word put on the board
        Function<String, Source<Integer, NotUsed>> score3 =
            word ->
                score2.apply(word).map(v -> v * 2)
                .concat(bonusForDoubleLetter.apply(word).map(v -> v * 2))
                .concat(Source.single(word.length() == 7 ? 50 : 0))
                .reduce((a, b) -> a + b);

        Function<Function<String, Source<Integer, NotUsed>>, Source<TreeMap<Integer, List<String>>, NotUsed>> buildHistoOnScore =
                score -> {
                    TreeMap<Integer, List<String>> map = new TreeMap<>(Comparator.reverseOrder());
                    return Source.from(shakespeareWords)
                                    .filter(scrabbleWords::contains)
                                    .filter(word -> {
                                        try {
                                            return first(checkBlanks.apply(word));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            return false;
                                        }
                                    })
                                    .map(word -> {
                                        Integer key = first(score.apply(word)) ;
                                        List<String> list = map.get(key) ;
                                        if (list == null) {
                                            list = new ArrayList<>() ;
                                            map.put(key, list) ;
                                        }
                                        list.add(word) ;
                                        return word;
                                    })
                                    .drop(Long.MAX_VALUE)
                                    .map(v -> map)
                                    .concat(Source.single(map))
                                    ;
                } ;

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 = new ArrayList<>();

                first(buildHistoOnScore.apply(score3)
                    .mapConcat(map -> map.entrySet())
                    .take(3)
                    .map(v -> {
                        finalList2.add(v); return v;
                    })
                    .drop(Long.MAX_VALUE)
                    .map(v -> finalList2)
                    .concat(Source.single(finalList2))
                    );


//        System.out.println(finalList2);

        return finalList2 ;
    }

    @SuppressWarnings("unchecked")
    <T> T first(Source<T, ?> source) {
        CompletionStage<T> cs = source.runWith(Sink.head(), materializer);
        CountDownLatch cdl = new CountDownLatch(1);
        Object[] value = { null };
        cs.whenComplete((v, e) -> {
            value[0] = v;
            cdl.countDown();
        });
        try {
            cdl.await();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return (T)value[0];
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithAkkaStreamBeta s = new ShakespearePlaysScrabbleWithAkkaStreamBeta();
        s.init();
        s.setup();
        try {
            System.out.println(s.measureThroughput());
        } finally {
            s.teardown();
        }

        /*
        Options opt = new OptionsBuilder()
                .include(ShakespearePlaysScrabbleWithAkkaStreamBeta.class.getSimpleName())
                .forks(1)
                .warmupIterations(5)
                .warmupTime(TimeValue.seconds(1))
                .measurementIterations(5)
                .measurementTime(TimeValue.seconds(1))
                .timeUnit(TimeUnit.MILLISECONDS)
                .mode(Mode.SampleTime)
                .build();

        new Runner(opt).run();
        */
    }
}