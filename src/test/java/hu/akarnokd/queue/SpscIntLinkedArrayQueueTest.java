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

public class SpscIntLinkedArrayQueueTest {
    @Test
    public void triggerGrowth() {
        SpscIntLinkedArrayQueue q = new SpscIntLinkedArrayQueue(16);

        boolean[] b = new boolean[1];

        for (int j = 0; j < 3; j++) {

            for (int i = 0; i < 31; i++) {
                Assert.assertTrue(q.offer(i));
            }

            for (int i = 0; i < 31; i++) {
                Assert.assertEquals(i, q.peek(b));
                Assert.assertTrue(b[0]);
                Assert.assertEquals(i, q.poll(b));
                Assert.assertTrue(b[0]);
            }

            Assert.assertEquals(0, q.peek(b));
            Assert.assertFalse(b[0]);
            Assert.assertEquals(0, q.poll(b));
            Assert.assertFalse(b[0]);

            Assert.assertTrue(q.isEmpty());
        }
    }
}
