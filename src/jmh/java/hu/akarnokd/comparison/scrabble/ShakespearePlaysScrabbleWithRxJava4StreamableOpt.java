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
import java.util.function.*;
import java.util.stream.*;

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.rxjava4.StreamableCharSequence;
import io.reactivex.rxjava4.core.*;
import io.reactivex.rxjava4.functions.Function;

/// Shakespeare plays Scrabble with RxJava 4 Streamable optimized.
///
/// # Oprimitation progress
///
/// i9 275HX, 32GB LPDDR5 6400MT CL52, Windows 25H2, JDK 26.0.1
///
/// | Step | Time (ms) | Improvement | vs Baseline |
/// |------|------|-------------:|-------------:|
/// | Baseline | 42,5119 | - | - |
/// | Avoid `whenComplete` usage in `collect` | 37,566 | -11,65% | - |
/// | IndexableSource in `collect` | 35,815 | -4,66% | -15,77% |
/// | concatIterable optimizations | 31,971 | -10,73%  | -24,81% |
/// | map fusions | 30,623 | -4,22% | -27,98% |
/// | flattenAsFlowable deferred opt | 27,999 | -8,57%  | -34,15% |
/// | MutableLong in the bench | 26,159 | -6,57% | -38,48% |
/// | lastOrError opt | 25,164 | -3,80% | -40,82% |
/// | custom  `Collector` summers | 24,885 | -1,11% | -41,47% |
/// | skip optimizations | 24,791  | -0,38% | -41,69% |
/// | filter opt, filter+map fusion, lastOrError opt | 23,566 |  -4,94% | -44,58% |
/// | slow path collect optimization  |  22,392 | -4,98% | -47,34% |
/// | CharStreamer EnumerableSource marker | 21,843 | -2,45% | -48,63% |
/// | custom max collector |  20,369 | -6,75% | -52,09% |
/// | skip sync bias | 19,827 | -2,66% | -53,37% |
///
/// @author José
/// @author akarnokd
public class ShakespearePlaysScrabbleWithRxJava4StreamableOpt extends ShakespearePlaysScrabble {
    static Streamable<Integer> chars(String word) {
//        return Flowable.range(0, word.length()).map(i -> (int)word.charAt(i));
//        return StringFlowable.characters(word);
//        return Streamable.range(0, word.length()).map(i -> (int)word.charAt(i));
        return new StreamableCharSequence(word);
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
    @Fork(value = 1, jvmArgs = {
            "-XX:MaxInlineLevel=20"
//            , "-XX:+UnlockDiagnosticVMOptions",
//            , "-XX:+PrintAssembly",
//            , "-XX:+TraceClassLoading",
//            , "-XX:+LogCompilation"
    })
    public List<Entry<Integer, List<String>>> measureThroughput() throws Throwable {

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


        Function<String, Streamable<Integer>> toIntegerStreamable =
                string -> chars(string);

        // Histogram of the letters in a given word
        Function<String, Single<Map<Integer, MutableLong>>> histoOfLetters =
                word -> toIntegerStreamable.apply(word)
                            .collect(MutableLongMapCounter.INSTANCE)
                            .lastOrError();

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
        Function<String, Streamable<Long>> nBlanks =
                word -> histoOfLetters.apply(word).flattenAsStreamable(
                                map -> map.entrySet()
                        )
                        .map(blank)
                        .collect(SumLongCollector.INSTANCE)
                    ;


        // can a word be written with 2 blanks?
        Function<String, Streamable<Boolean>> checkBlanks =
                word -> nBlanks.apply(word)
                            .map(l -> l <= 2L) ;

        // score taking blanks into account letterScore1
        Function<String, Streamable<Integer>> score2 =
                word -> histoOfLetters.apply(word).flattenAsStreamable(
                            map -> map.entrySet()
                        )
                        .map(letterScore)
                        .collect(SumIntCollector.INSTANCE)
                    ;

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, Streamable<Integer>> first3 =
                word -> chars(word).take(3) ;
        Function<String, Streamable<Integer>> last3 =
                word -> chars(word).skip(3) ;


        // Stream to be maxed
        Function<String, Streamable<Integer>> toBeMaxed =
            word -> Streamable.concat(List.of(first3.apply(word), last3.apply(word)))
            ;

        // Bonus for double letter
        Function<String, Streamable<Integer>> bonusForDoubleLetter =
            word -> toBeMaxed.apply(word)
                        .map(scoreOfALetter)
                        .collect(MaxCollector.INSTANCE)
                        //.mapOptional(v -> v)
                        ;

        // score of the word put on the board
        Function<String, Streamable<Integer>> score3 =
            word ->
                Streamable.concat(
                    List.of(
                            score2.apply(word),
                            bonusForDoubleLetter.apply(word)
                        )
                )
                .collect(SumIntCollector.INSTANCE)
                .map(v -> v * 2 + (word.length() == 7 ? 50 : 0))
                ;

        Function<Function<String, Streamable<Integer>>, Single<TreeMap<Integer, List<String>>>> buildHistoOnScore =
                score -> Streamable.fromIterable(shakespeareWords)
                                .filter(scrabbleWords::contains)
                                .filter(word -> checkBlanks.apply(word).blockingFirst())
                                .collect(new ReverseTreeMapListCollector(word -> score.apply(word).blockingFirst()))
                                .lastOrError();

        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                    buildHistoOnScore.apply(score3).flattenAsStreamable(
                            map -> map.entrySet()
                    )
                    .take(3)
                    .collect(Collectors.toList())
                    .blockingFirst() ;

        return finalList2 ;
    }

    static final class MutableLongMapCounter
    implements Collector<Integer, Map<Integer, MutableLong>, Map<Integer, MutableLong>> {

        static final MutableLongMapCounter INSTANCE = new MutableLongMapCounter();

        private MutableLongMapCounter() { }

        @Override
        public Supplier<Map<Integer, MutableLong>> supplier() {
            return HashMap::new;
        }

        @Override
        public BiConsumer<Map<Integer, MutableLong>, Integer> accumulator() {
            return (m, v) -> {
                m.computeIfAbsent(v, _ -> new MutableLong()).incAndSet();
            };
        }

        @Override
        public BinaryOperator<Map<Integer, MutableLong>> combiner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.function.Function<Map<Integer, MutableLong>, Map<Integer, MutableLong>> finisher() {
            return v -> v;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return EnumSet.of(Characteristics.UNORDERED);
        }
    }

    static final class SumIntCollector
    implements Collector<Integer, MutableLong, Integer> {

        static final SumIntCollector INSTANCE = new SumIntCollector();

        private SumIntCollector() { }

        @Override
        public Supplier<MutableLong> supplier() {
            return MutableLong::new;
        }

        @Override
        public BiConsumer<MutableLong, Integer> accumulator() {
            return (m, v) -> m.add(v);
        }

        @Override
        public BinaryOperator<MutableLong> combiner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.function.Function<MutableLong, Integer> finisher() {
            return m -> (int)m.get();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return null;
        }
    }
    static final class SumLongCollector
    implements Collector<Long, MutableLong, Long> {

        static final SumLongCollector INSTANCE = new SumLongCollector();

        private SumLongCollector() { }

        @Override
        public Supplier<MutableLong> supplier() {
            return MutableLong::new;
        }

        @Override
        public BiConsumer<MutableLong, Long> accumulator() {
            return (m, v) -> m.add(v);
        }

        @Override
        public BinaryOperator<MutableLong> combiner() {
            throw new UnsupportedOperationException();
        }

        @Override
        public java.util.function.Function<MutableLong, Long> finisher() {
            return m -> m.get();
        }

        @Override
        public Set<Characteristics> characteristics() {
            return null;
        }
    }

    record ReverseTreeMapListCollector(Function<String, Integer> valueMapper)
    implements Collector<String, TreeMap<Integer, List<String>>, TreeMap<Integer, List<String>>> {

        @Override
        public Supplier<TreeMap<Integer, List<String>>> supplier() {
            return () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder());
        }

        @Override
        public BiConsumer<TreeMap<Integer, List<String>>, String> accumulator() {
            return (m, v) -> {
                Integer result;

                try {
                    result = valueMapper.apply(v);
                } catch (Throwable ex) {
                    result = 0;
                }
                m.computeIfAbsent(result, _ -> new ArrayList<String>()).add(v);
            };
        }

        @Override
        public BinaryOperator<TreeMap<Integer, List<String>>> combiner() {
            return null;
        }

        @Override
        public java.util.function.Function<TreeMap<Integer, List<String>>, TreeMap<Integer, List<String>>> finisher() {
            return v -> v;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return null;
        }
    }

    record MaxCollector() implements Collector<Integer, int[], Integer> {

        static final MaxCollector INSTANCE = new MaxCollector();

        @Override
        public Supplier<int[]> supplier() {
            return () -> new int[1];
        }

        @Override
        public BiConsumer<int[], Integer> accumulator() {
            return (m, i) -> {
                m[0] = Math.max(m[0], i);
            };
        }

        @Override
        public java.util.function.Function<int[], Integer> finisher() {
            return m -> m[0];
        }

        @Override
        public Set<Characteristics> characteristics() {
            return null;
        }

        @Override
        public BinaryOperator<int[]> combiner() {
            return null;
        }
    }

    public static void main(String[] args) throws Throwable {
        ShakespearePlaysScrabbleWithRxJava4StreamableOpt s = new ShakespearePlaysScrabbleWithRxJava4StreamableOpt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}