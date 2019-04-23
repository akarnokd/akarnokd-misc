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

import java.util.Set;

import org.openjdk.jmh.annotations.*;

import hu.akarnokd.comparison.Util;

@State(Scope.Benchmark)
public class ShakespearePlaysScrabble {

    static class MutableLong {
        long value;
        long get() {
            return value;
        }

        MutableLong set(long l) {
            value = l;
            return this;
        }

        MutableLong incAndSet() {
            value++;
            return this;
        }

        MutableLong add(MutableLong other) {
            value += other.value;
            return this;
        }
    }

    interface Wrapper<T> {
        T get() ;

        default Wrapper<T> set(T t) {
            return () -> t ;
        }
    }

    interface IntWrapper {
        int get() ;

        default IntWrapper set(int i) {
            return () -> i ;
        }

        default IntWrapper incAndSet() {
            return () -> get() + 1 ;
        }
    }

    interface LongWrapper {
        long get() ;

        default LongWrapper set(long l) {
            return () -> l ;
        }

        default LongWrapper incAndSet() {
            return () -> get() + 1L ;
        }

        default LongWrapper add(LongWrapper other) {
            return () -> get() + other.get() ;
        }
    }

    public static final int [] letterScores = {
    // a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p,  q, r, s, t, u, v, w, x, y,  z
       1, 3, 3, 2, 1, 4, 2, 4, 1, 8, 5, 1, 3, 1, 1, 3, 10, 1, 1, 1, 1, 4, 4, 8, 4, 10} ;

    public static final int [] scrabbleAvailableLetters = {
     // a, b, c, d,  e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
        9, 2, 2, 1, 12, 2, 3, 2, 9, 1, 1, 4, 2, 6, 8, 2, 1, 6, 4, 6, 4, 2, 2, 1, 2, 1} ;


    public Set<String> scrabbleWords;
    public Set<String> shakespeareWords;

    @Setup
    public void init() {
        scrabbleWords = Util.readScrabbleWords() ;
        shakespeareWords = Util.readShakespeareWords() ;
    }

}