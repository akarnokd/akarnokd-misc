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

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import ix.Ix;
import rx.functions.Func1;

/**
 *
 * @author José
 */
public class ShakespearePlaysScrabbleWithIxOpt extends ShakespearePlaysScrabble {

	
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
    
    static Ix<Integer> chars(String word) {
        return Ix.range(0, word.length()).map(i -> (int)word.charAt(i));
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
    	Func1<Integer, Integer> scoreOfALetter = letter -> letterScores[letter - 'a'];
            
        // score of the same letters in a word
        Func1<Entry<Integer, MutableLong>, Integer> letterScore =
        		entry -> 
    					letterScores[entry.getKey() - 'a']*
    					Integer.min(
    	                        (int)entry.getValue().get(), 
    	                        (int)scrabbleAvailableLetters[entry.getKey() - 'a']
    	                    )
        	        ;
        
    					
        Func1<String, Ix<Integer>> toIntegerIx = 
        		string -> chars(string);
                    
        // Histogram of the letters in a given word
        Func1<String, Ix<HashMap<Integer, MutableLong>>> histoOfLetters =
        		word -> toIntegerIx.call(word)
        					.collect(
    							() -> new HashMap<Integer, MutableLong>(), 
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
        Func1<String, Ix<Long>> nBlanks = 
        		word -> histoOfLetters.call(word)
        					.flatMap(map -> Ix.from(() -> map.entrySet().iterator()))
        					.map(blank)
        					.sumLong();
        					
                
        // can a word be written with 2 blanks?
        Func1<String, Ix<Boolean>> checkBlanks = 
        		word -> nBlanks.call(word)
        					.map(l -> l <= 2L) ;
        
        // score taking blanks into account letterScore1
        Func1<String, Ix<Integer>> score2 = 
        		word -> histoOfLetters.call(word)
        					.flatMap(map -> Ix.from(map.entrySet()))
        					.map(letterScore)
        					.sumInt();
        					
        // Placing the word on the board
        // Building the streams of first and last letters
        Func1<String, Ix<Integer>> first3 = 
        		word -> chars(word).take(3) ;
        Func1<String, Ix<Integer>> last3 = 
        		word -> chars(word).skip(3) ;
        		
        
        // Stream to be maxed
        Func1<String, Ix<Integer>> toBeMaxed = 
        	word -> Ix.concat(Arrays.asList(first3.call(word), last3.call(word)))
        	;
            
        // Bonus for double letter
        Func1<String, Ix<Integer>> bonusForDoubleLetter = 
        	word -> toBeMaxed.call(word)
        				.map(scoreOfALetter)
        				.max();
            
        // score of the word put on the board
        Func1<String, Ix<Integer>> score3 = 
        	word ->
//        		Ix.fromArray(
//        				score2.call(word), 
//        				score2.call(word), 
//        				bonusForDoubleLetter.call(word), 
//        				bonusForDoubleLetter.call(word), 
//        				Ix.just(word.length() == 7 ? 50 : 0)
//        		)
//        		.flatMap(Ix -> Ix)
                Ix.concat(Arrays.asList(
                        score2.call(word).map(v -> v * 2), 
                        bonusForDoubleLetter.call(word).map(v -> v * 2), 
                        Ix.just(word.length() == 7 ? 50 : 0))
                )
        		.sumInt();

        Func1<Func1<String, Ix<Integer>>, Ix<TreeMap<Integer, List<String>>>> buildHistoOnScore =
        		score -> Ix.from(shakespeareWords)
        						.filter(scrabbleWords::contains)
        						.filter(word -> checkBlanks.call(word).first())
        						.collect(
        							() -> new TreeMap<Integer, List<String>>(Comparator.reverseOrder()), 
        							(TreeMap<Integer, List<String>> map, String word) -> {
        								Integer key = score.call(word).first() ;
        								List<String> list = map.get(key) ;
        								if (list == null) {
        									list = new ArrayList<String>() ;
        									map.put(key, list) ;
        								}
        								list.add(word) ;
        							}
        						) ;
                
        // best key / value pairs
        List<Entry<Integer, List<String>>> finalList2 =
        		buildHistoOnScore.call(score3)
        			.flatMap(map -> Ix.from(map.entrySet()))
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