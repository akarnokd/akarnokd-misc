/*
 * Copyright 2015-2017 David Karnok
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

package hu.akarnokd.queue;

import org.junit.*;

public class SpscLongArrayQueueTest {
    @Test
    public void simpleUse() {
        boolean[] hasValue = new boolean[1];

        SpscLongArrayQueue q = new SpscLongArrayQueue(32);

        for (int i = 1; i <= 32; i++) {
            Assert.assertTrue("" + i + ": ", q.offer(i));
        }

        Assert.assertFalse(q.offer(33));

        for (int i = 1; i <= 32; i++) {
            Assert.assertFalse(q.isEmpty());
            Assert.assertTrue(q.hasValue());
            Assert.assertEquals(i, q.peek());
            Assert.assertEquals(i, q.peek(hasValue));
            Assert.assertTrue(hasValue[0]);
            Assert.assertEquals(i, q.poll());
        }

        Assert.assertTrue(q.isEmpty());
        Assert.assertFalse(q.hasValue());
        Assert.assertEquals(0, q.peek());
        Assert.assertEquals(0, q.poll());

        Assert.assertEquals(0, q.peek(hasValue));
        Assert.assertFalse(hasValue[0]);

        Assert.assertEquals(0, q.poll(hasValue));
        Assert.assertFalse(hasValue[0]);
    }
}
