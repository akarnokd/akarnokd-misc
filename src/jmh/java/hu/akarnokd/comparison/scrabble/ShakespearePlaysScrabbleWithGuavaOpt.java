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
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Warmup;

import com.google.common.base.*;
import com.google.common.collect.FluentIterable;

import static hu.akarnokd.comparison.FluentIterables.*;

/**
 *
 * @author José
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
        Function<Entry<Integer, MutableLong>, Integer> letterScore =
        		entry -> 
    					letterScores[entry.getKey() - 'a']*
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
                    score2.apply(word).transform(v -> v * 2)
                    .append(bonusForDoubleLetter.apply(word).transform(v -> v * 2))
                    .append(word.length() == 7 ? 50 : 0)
        		);

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
        									list = new ArrayList<String>() ;
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