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
import java.util.concurrent.*;

import org.openjdk.jmh.annotations.*;

import com.typesafe.config.*;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.japi.function.Function;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;
import hu.akarnokd.comparison.ReactiveStreamsImplsAsync;

/**
 * Shakespeare with Akka-Stream Optimized.
 * 
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithAkkaStreamOpt extends ShakespearePlaysScrabble {

    ActorSystem actorSystem;

    ActorMaterializer materializer;

    @Setup
    public void setup() {

        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImplsAsync.class, "/akka-streams.conf").resolve();
        actorSystem = ActorSystem.create("sys", cfg);
        materializer = ActorMaterializer.create(actorSystem);

    }

    @SuppressWarnings("deprecation")
    @TearDown
    public void teardown() {
        actorSystem.shutdown();
    }

    static Source<Integer, NotUsed> chars(String word) {
        return Source.from(ix.Ix.characters(word));
    }

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
                                    .flatMapConcat((String word) ->
                                        checkBlanks.apply(word)
                                        .filter(v -> v)
                                        .map(v -> word)
                                    )
                                    .flatMapConcat(word -> score.apply(word)
                                            .map(key -> {
                                                List<String> list = map.get(key) ;
                                                if (list == null) {
                                                    list = new ArrayList<>() ;
                                                    map.put(key, list) ;
                                                }
                                                list.add(word) ;
                                                return word;
                                            }))
                                    .drop(Long.MAX_VALUE)
                                    .concat(Source.single(map));
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
        ShakespearePlaysScrabbleWithAkkaStreamOpt s = new ShakespearePlaysScrabbleWithAkkaStreamOpt();
        s.init();
        s.setup();
        try {
            System.out.println(s.measureThroughput());
        } finally {
            s.teardown();
        }

    }
}