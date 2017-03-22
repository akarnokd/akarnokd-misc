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

import org.openjdk.jmh.annotations.*;

/**
 * Shakespeare plays Scrabble with Ix optimized.
 * @author José
 * @author akarnokd
 */
public class ShakespearePlaysScrabbleWithDirect extends ShakespearePlaysScrabble {

    @SuppressWarnings("unused")
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

        TreeMap<Integer, List<String>> treemap = new TreeMap<Integer, List<String>>(Comparator.reverseOrder());
        
        for (String word : shakespeareWords) {
            if (scrabbleWords.contains(word)) {
                HashMap<Integer, MutableLong> wordHistogram = new LinkedHashMap<>();
                for (int i = 0; i < word.length(); i++) {
                    MutableLong newValue = wordHistogram.get((int)word.charAt(i)) ;
                    if (newValue == null) {
                        newValue = new MutableLong();
                        wordHistogram.put((int)word.charAt(i), newValue);
                    }
                    newValue.incAndSet();
                }
                long sum = 0L;
                for (Entry<Integer, MutableLong> entry : wordHistogram.entrySet()) {
                    sum += Long.max(0L, entry.getValue().get() -
                                scrabbleAvailableLetters[entry.getKey() - 'a']);
                }
                boolean b = sum <= 2L;

                if (b) {
                    // redo the histogram?!
//                    wordHistogram = new HashMap<>();
//                    for (int i = 0; i < word.length(); i++) {
//                        MutableLong newValue = wordHistogram.get((int)word.charAt(i)) ;
//                        if (newValue == null) {
//                            newValue = new MutableLong();
//                            wordHistogram.put((int)word.charAt(i), newValue);
//                        }
//                        newValue.incAndSet();
//                    }

                    int sum2 = 0;
                    for (Map.Entry<Integer, MutableLong> entry : wordHistogram.entrySet()) {
                        sum2 += letterScores[entry.getKey() - 'a'] *
                                Integer.min(
                                (int)entry.getValue().get(),
                                scrabbleAvailableLetters[entry.getKey() - 'a']
                            );
                    }
                    int max2 = 0;
                    for (int i = 0; i < 3 && i < word.length(); i++) {
                        max2 = Math.max(max2, letterScores[word.charAt(i) - 'a']);
                    }

                    for (int i = 3; i < word.length(); i++) {
                        max2 = Math.max(max2, letterScores[word.charAt(i) - 'a']);
                    }
                    
                    sum2 += max2;
                    sum2 = 2 * sum2 + (word.length() == 7 ? 50 : 0);
                    
                    Integer key = sum2;
                    
                    List<String> list = treemap.get(key) ;
                    if (list == null) {
                        list = new ArrayList<>() ;
                        treemap.put(key, list) ;
                    }
                    list.add(word) ;
                }
            }
        }
        
        List<Entry<Integer, List<String>>> list = new ArrayList<Entry<Integer, List<String>>>();
        
        int i = 4;
        for (Entry<Integer, List<String>> e : treemap.entrySet()) {
            if (--i == 0) {
                break;
            }
            list.add(e);
        }

        return list;
    }

    public static void main(String[] args) throws Exception {
        ShakespearePlaysScrabbleWithDirect s = new ShakespearePlaysScrabbleWithDirect();
        s.init();
        System.out.println(s.measureThroughput());
    }
}