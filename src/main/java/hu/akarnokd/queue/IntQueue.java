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

public interface IntQueue {

    boolean offer(int value);

    default int offer(int[] values) {
        return offer(values, 0, values.length);
    }
    default int offer(int[] values, int start, int count) {
        int e = 0;
        int i = start;
        while (count-- != 0) {
            if (!offer(values[i])) {
                return e;
            }
            i++;
            e++;
        }
        return e;
    }

    default int peek(boolean[] hasValue) {
        long v = peek();
        hasValue[0] = (v & 0x1_0000_0000L) != 0L;
        return (int)v;
    }

    long peek();

    default int poll(boolean[] hasValue) {
        long v = poll();
        hasValue[0] = (v & 0x1_0000_0000L) != 0L;
        return (int)v;
    }

    long poll();

    default int poll(int[] values) {
        return poll(values, 0, values.length);
    }

    default int poll(int[] values, int start, int count) {
        boolean[] hasValue = new boolean[1];

        int i = start;
        int e = 0;

        while (count-- != 0) {
            int v = poll(hasValue);
            if (!hasValue[0]) {
                break;
            }
            values[i] = v;
            i++;
            e++;
        }

        return e;
    }

    void clear();

    int size();

    boolean isEmpty();
}
