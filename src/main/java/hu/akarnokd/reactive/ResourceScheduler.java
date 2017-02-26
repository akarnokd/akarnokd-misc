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

package hu.akarnokd.reactive;

import io.reactivex.disposables.Disposable;

/**
 * Represents an abstraction over an asynchronous boundary that
 * supports notifying the tasks about cancellation to allow
 * resource cleanup that would have otherwise happened in run().
 */
public abstract class ResourceScheduler {

    public abstract Disposable scheduleDirect(ResourceTask task);

    public abstract ResourceWorker createWorker();

    public void shutdown() { }

    /**
     * Represents a FIFO worker that executes the submitted tasks in
     * order and honors the resource cleanup requirements of the
     * ResourceTasks submitted.
     */
    public abstract static class ResourceWorker implements Disposable {

        public abstract Disposable schedule(ResourceTask task);

    }

    /**
     * Represents a runnable task that also can receive a notification
     * about it being cancelled before the run() could execute.
     */
    public interface ResourceTask extends Runnable {
        /**
         * Called when the ResourceTask was cancelled before the run()
         * could be executed.
         * <p>
         * Guaranteed to be called in exclusion to run() and at most once.
         */
        void onCancel();
    }
}
