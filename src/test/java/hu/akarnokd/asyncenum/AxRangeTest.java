/*
 * Copyright 2016 David Karnok
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See
 * the License for the specific language governing permissions and limitations under the License.
 */

package hu.akarnokd.asyncenum;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class AxRangeTest {

    @Test
    public void emptyRange() {

        Ax<Integer> source = Ax.range(1, 0);

        TestAsyncConsumer<Integer> tc = new TestAsyncConsumer<>(source.enumerator());

        tc.consumeAwaitAll(5, TimeUnit.SECONDS)
        .assertNoValues()
        .assertNoErrors()
        .assertCompleted()
        ;
    }

    @Test
    public void justRange() {
        Ax<Integer> source = Ax.range(1, 1);

        TestAsyncConsumer<Integer> tc = new TestAsyncConsumer<>(source.enumerator());

        tc.consumeAwaitAll(5, TimeUnit.SECONDS)
        .assertValue(1)
        .assertNoErrors()
        .assertCompleted()
        ;
    }

    @Test
    public void smallRange() {
        Ax<Integer> source = Ax.range(1, 10);

        TestAsyncConsumer<Integer> tc = new TestAsyncConsumer<>(source.enumerator());

        tc.consumeAwaitAll(5, TimeUnit.SECONDS)
        .assertValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
        .assertNoErrors()
        .assertCompleted()
        ;

    }

    @Test
    public void longRange() {
        for (int i = 100; i <= 1_000_000; i *= 10) {
            System.out.println("longRange >> " + i);
            Ax<Integer> source = Ax.range(1, i);

            TestAsyncConsumer<Integer> tc = new TestAsyncConsumer<>(source.enumerator());

            tc.consumeAwaitAll(5, TimeUnit.SECONDS)
            .assertValueCount(i)
            .assertNoErrors()
            .assertCompleted()
            ;
        }
    }
}
