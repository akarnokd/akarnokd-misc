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

package hu.akarnokd.comparison;

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

import java.util.function.Function;
import reactor.rx.Fluxion;

/**
 *
 * @author José
 */
public class ShakespearePlaysScrabbleWithReactor25Opt extends ShakespearePlaysScrabble {

	
    static class LongWrapper {
        long value;
        long get() {
            return value;
        }
        
        LongWrapper set(long l) {
            value = l;
            return this;
        }
        
        LongWrapper incAndSet() {
            value++;
            return this;
        }
        
        LongWrapper add(LongWrapper other) {
            value += other.value;
            return this;
        }
    }
    
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
			ShakespearePlaysScrabbleWithFluxions.measureThroughput  avgt   15   29389,903 ± 1115,836  us/op
    		
    */ 
    
    static Fluxion<Integer> chars(String word) {
        return Fluxion.range(0, word.length()).map(i -> (int)word.charAt(i));
    }
    
    @Benchmark
    @BenchmarkMode(Mode.SampleTime)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Warmup(
		iterations=5
    )
    @Measurement(
    	iterations=5
    )
    @Fork(1)
    public List<Entry<Integer, List<String>>> measureThroughput() throws InterruptedException {

        //  to compute the score of a given word
    	Function<Integer, Integer> scoreOfALetter = letter -> letterScores[letter - 'a'];
            
        // score of the same letters in a word
        Function<Entry<Integer, LongWrapper>, Integer> letterScore =
        		entry -> 
    					letterScores[entry.getKey() - 'a']*
    					Integer.min(
    	                        (int)entry.getValue().get(), 
    	                        (int)scrabbleAvailableLetters[entry.getKey() - 'a']
    	                    )
        	        ;
        
    					
        Function<String, Fluxion<Integer>> toIntegerFluxion = 
        		string -> chars(string);
                    
        // Histogram of the letters in a given word
        Function<String, Fluxion<HashMap<Integer, LongWrapper>>> histoOfLetters =
        		word -> Fluxion.from(toIntegerFluxion.apply(word)
        					.collect(
    							() -> new HashMap<Integer, LongWrapper>(), 
    							(HashMap<Integer, LongWrapper> map, Integer value) -> 
    								{ 
    									LongWrapper newValue = map.get(value) ;
    									if (newValue == null) {
    										newValue = new LongWrapper();
    										map.put(value, newValue);
    									}
    									newValue.incAndSet();
    								}
    								
        					)) ;
                
        // number of blanks for a given letter
        Function<Entry<Integer, LongWrapper>, Long> blank =
        		entry ->
	        			Long.max(
	        				0L, 
	        				entry.getValue().get() - 
	        				scrabbleAvailableLetters[entry.getKey() - 'a']
	        			)
        			;

        // number of blanks for a given word
        Function<String, Fluxion<Long>> nBlanks = 
        		word -> Fluxion.from(histoOfLetters.apply(word)
        					.flatMap(map -> Fluxion.fromIterable(() -> map.entrySet().iterator()))
        					.map(blank)
        					.reduce(Long::sum)) ;
        					
                
        // can a word be written with 2 blanks?
        Function<String, Fluxion<Boolean>> checkBlanks = 
        		word -> nBlanks.apply(word)
        					.map(l -> l <= 2L) ;
        
        // score taking blanks into account letterScore1
        Function<String, Fluxion<Integer>> score2 = 
        		word -> Fluxion.from(histoOfLetters.apply(word)
        					.flatMap(map -> Fluxion.fromIterable(map.entrySet()))
        					.map(letterScore)
        					.reduce(Integer::sum)) ;
        					
        // Placing the word on the board
        // Building the Fluxions of first and last letters
        Function<String, Fluxion<Integer>> first3 = 
        		word -> chars(word).take(3) ;
        Function<String, Fluxion<Integer>> last3 = 
        		word -> chars(word).skip(3) ;
        		
        
        // Fluxion to be maxed
        Function<String, Fluxion<Integer>> toBeMaxed = 
        	word -> Fluxion.concat(first3.apply(word), last3.apply(word))
        	;
            
        // Bonus for double letter
        Function<String, Fluxion<Integer>> bonusForDoubleLetter = 
        	word -> Fluxion.from(toBeMaxed.apply(word)
        				.map(scoreOfALetter)
        				.reduce(Integer::max)) ;
            
        // score of the word put on the board
        Function<String, Fluxion<Integer>> score3 = 
        	word ->
//        		Fluxion.fromArray(
//        				score2.apply(word), 
//        				score2.apply(word), 
//        				bonusForDoubleLetter.apply(word), 
//        				bonusForDoubleLetter.apply(word), 
//        				Fluxion.just(word.length() == 7 ? 50 : 0)
//        		)
//        		.flatMap(Fluxion -> Fluxion)
        Fluxion.from(Fluxion.concat(
                        score2.apply(word), 
                        score2.apply(word), 
                        bonusForDoubleLetter.apply(word), 
                        bonusForDoubleLetter.apply(word), 
                        Fluxion.just(word.length() == 7 ? 50 : 0)
                )
        		.reduce(Integer::sum)) ;

        Function<Function<String, Fluxion<Integer>>, Fluxion<TreeMap<Integer, List<String>>>> buildHistoOnScore =
        		score -> Fluxion.from(Fluxion.fromIterable(shakespeareWords)
        						.filter(scrabbleWords::contains)
        						.filter(word -> checkBlanks.apply(word).toIterable().iterator().next())
        						.collect(
        							() -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()), 
        							(TreeMap<Integer, List<String>> map, String word) -> {
        								Integer key = score.apply(word).toIterable().iterator().next() ;
        								List<String> list = map.get(key) ;
        								if (list == null) {
        									list = new ArrayList<String>() ;
        									map.put(key, list) ;
        								}
        								list.add(word) ;
        							}
        						)) ;
                
        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
                Fluxion.from(buildHistoOnScore.apply(score3)
        			.flatMap(map -> Fluxion.fromIterable(map.entrySet()))
        			.take(3)
        			.collect(
        				() -> new ArrayList<Entry<Integer, List<String>>>(), 
        				(list, entry) -> {
        					list.add(entry) ;
        				}
        			))
        			.toIterable().iterator().next() ;
        			
        
//        System.out.println(finalList2);
        
        return finalList2 ;
    }
    
    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithReactor25Opt s = new ShakespearePlaysScrabbleWithReactor25Opt();
        s.init();
        System.out.println(s.measureThroughput());
    }
}