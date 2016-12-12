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

import static com.aol.cyclops.control.ReactiveSeq.fromIterable;
import static com.aol.cyclops.control.ReactiveSeq.fromString;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import com.aol.cyclops.control.ReactiveSeq;

/**
 * Shakespeare plays Scrabble with Ix optimized.
 * @author José
 * @author akarnokd
 * @johnmcclean-aol
 */
public class ShakespearePlaysScrabbleWithCyclopsReactOpt extends ShakespearePlaysScrabble {

    static ReactiveSeq<Integer> chars(String word) {
        return ReactiveSeq.fromString(word);
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
    public List<Entry<Integer, List<String>>> measureThroughput() throws InterruptedException {
                                               
         // Histogram of the letters in a given word
        Function<String, ReactiveSeq<Map.Entry<Integer, Long>>> histoOfLetters =
                word -> fromIterable(fromString(word).foldInt(i->i,s->s.<HashMap<Integer, Long>>collect(
                                                            () -> new HashMap<Integer,Long>(),
                                                            (HashMap<Integer, Long> map, int value) ->
                                                            {
                                                                Long newValue = map.get(value) ;
                                                                if (newValue == null) {
                                                                    newValue = 0L ;
                                                                }
                                                                map.put(value, newValue+1) ;
                                                            }, (a,b)->{}

                                                       
                            )).entrySet());

    
        // number of blanks for a given word
        Function<String, ReactiveSeq<Long>> nBlanks =
                word -> histoOfLetters.apply(word)
                                      .coflatMap(s->s.sumLong(e ->Long.max(0L,e.getValue() -scrabbleAvailableLetters[e.getKey() - 'a'])));

        // can a word be written with 2 blanks?
        Predicate<String> checkBlanks = word -> nBlanks.apply(word).single() <= 2;

        // score taking blanks into account
        Function<String, ReactiveSeq<Integer>> score2 = word -> histoOfLetters.apply(word)
                                                                              .coflatMap(s -> s.sumInt(entry -> letterScores[entry.getKey() - 'a']
                                                                                                                 * Integer.min(entry.getValue().intValue(),
                                                                                                                   scrabbleAvailableLetters[entry.getKey() - 'a'])));

        // Placing the word on the board
        // Building the streams of first and last letters
        Function<String, ReactiveSeq<Integer>> first3 = word -> fromString(word).ints(i->i,s->s.limit(3));
        Function<String, ReactiveSeq<Integer>> last3 = word -> fromString(word).ints(i->i,s->s.skip(Integer.max(0, word.length() - 4)));

        // Stream to be maxed
        Function<String, ReactiveSeq<Integer>> toBeMaxed =
            word -> first3.apply(word).ints(i->i,s1-> last3.apply(word).foldInt(i->i,s2->IntStream.concat(s1, s2)));
                       

        // Bonus for double letter
        Function<String,ReactiveSeq<Integer>> bonusForDoubleLetter =
            word ->  ReactiveSeq.ofInts(toBeMaxed.apply(word)
                                                 .foldInt(i->i,s-> s.map(letter -> letterScores[letter - 'a'])
                                                                    .reduce(0,(a,b)->a>b?a:b)));

        // score of the word put on the board
           
                    
        Function<String, ReactiveSeq<Integer>> score3 = word -> ReactiveSeq.of(score2.apply(word).single()
                                                                               + bonusForDoubleLetter.apply(word).single()
                                                                               + score2.apply(word).single()
                                                                               + bonusForDoubleLetter.apply(word).single()
                                                                               + (word.length() == 7 ? 50 : 0));
        
              
       Function<Function<String, ReactiveSeq<Integer>>, ReactiveSeq<Map<Integer, List<String>>>> buildHistoOnScore =
                        score -> ReactiveSeq.fromIterable(shakespeareWords)
                                        .filter(t->scrabbleWords.contains(t) && checkBlanks.test(t))                                       
                                        .coflatMap(s->s.collect(
                                           Collectors.groupingBy(
                                              score.andThen(ReactiveSeq::single),
                                              () -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()),
                                              Collectors.toList()
                                           )
                                        ));
        
                
                
        // best key / value pairs
      List<Entry<Integer, List<String>>> finalList2 =
                        buildHistoOnScore.apply(score3)
                                        .flatMap(m->m.entrySet().stream())
                                        .limit(3)
                                        .collect(Collectors.toList());
                                      
        


        
        return finalList2 ;
    }

    public static void main(String[] args) throws Exception {
        
        ShakespearePlaysScrabbleWithCyclopsReactOpt s = new ShakespearePlaysScrabbleWithCyclopsReactOpt();
        s.init();
        Long time = System.currentTimeMillis();
        for(int i=0;i<100;i++){
            System.out.println(s.measureThroughput());
        }
        System.out.println( System.currentTimeMillis()-time);
    }
}
