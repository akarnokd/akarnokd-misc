package hu.akarnokd.reactive;

import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import hu.akarnokd.reactive4java.util.Functions;
import io.reactivex.disposables.*;
import io.reactivex.internal.util.OpenHashSet;
import rx.internal.util.RxThreadFactory;

public final class SingleResourceScheduler extends ResourceScheduler {

    final ExecutorService executor;

    public SingleResourceScheduler() {
        executor = Executors.newSingleThreadExecutor(new RxThreadFactory("RxSingleResourceScheduler"));
    }

    @Override
    public Disposable scheduleDirect(ResourceTask task) {
        DirectTask dt = new DirectTask(task);
        Future<?> f = executor.submit(dt);
        dt.setFuture(f);
        return dt;
    }

    @Override
    public void shutdown() {
        executor.shutdownNow();
    }

    @Override
    public ResourceWorker createWorker() {
        return new SingleResourceWorker();
    }

    final class SingleResourceWorker extends ResourceWorker {

        volatile boolean disposed;

        OpenHashSet<WorkerTask> tasks;

        SingleResourceWorker() {
            this.tasks = new OpenHashSet<>();
        }

        @Override
        public void dispose() {
            if (!disposed) {
                OpenHashSet<WorkerTask> set;
                synchronized (this) {
                    if (disposed) {
                        return;
                    }
                    disposed = true;
                    set = tasks;
                    tasks = null;
                }

                Object[] o = set.keys();
                for (Object e : o) {
                    if (e instanceof WorkerTask) {
                        WorkerTask wt = (WorkerTask) e;
                        wt.dispose();
                    }
                }
            }
        }

        @Override
        public boolean isDisposed() {
            return disposed;
        }

        @Override
        public Disposable schedule(ResourceTask task) {
            if (!disposed) {
                WorkerTask wt = new WorkerTask(task);
                if (add(wt)) {
                    Future<?> f = executor.submit(wt);
                    wt.setFuture(f);
                    return wt;
                }
            }

            task.onCancel();
            return Disposables.disposed();
        }

        boolean add(WorkerTask task) {
            synchronized (this) {
                OpenHashSet<WorkerTask> set = tasks;
                if (set != null) {
                    set.add(task);
                    return true;
                }
            }
            return false;
        }

        void delete(WorkerTask task) {
            synchronized (this) {
                OpenHashSet<WorkerTask> set = tasks;
                if (set != null) {
                    set.remove(task);
                }
            }
        }

        final class WorkerTask
        extends AtomicBoolean
        implements Callable<Void>, Disposable {

            private static final long serialVersionUID = 2906995671043870792L;

            final ResourceTask task;

            final AtomicReference<Future<?>> future;

            WorkerTask(ResourceTask task) {
                this.task = task;
                this.future = new AtomicReference<>();
            }

            @Override
            public Void call() throws Exception {
                if (compareAndSet(false, true)) {
                    try {
                        task.run();
                    } finally {
                        Future<?> f = future.get();
                        if (f != CANCELLED) {
                            future.compareAndSet(f, FINISHED);
                            delete(this);
                        }
                    }
                }
                return null;
            }

            @Override
            public void dispose() {
                if (compareAndSet(false, true)) {
                    Future<?> f = future.getAndSet(CANCELLED);
                    if (f != null) {
                        f.cancel(true);
                    }
                    if (f != FINISHED) {
                        delete(this);
                    }

                    task.onCancel();
                }
            }

            @Override
            public boolean isDisposed() {
                Future<?> f = future.get();
                return f == CANCELLED || f == FINISHED;
            }

            public void setFuture(Future<?> future) {
                if (this.future.compareAndSet(null, future)) {
                    Future<?> f = this.future.get();
                    if (f == CANCELLED) {
                        future.cancel(true);
                    }
                }
            }
        }

    }

    static final FutureTask<Object> CANCELLED;
    static final FutureTask<Object> FINISHED;
    static {
        CANCELLED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        CANCELLED.cancel(false);
        FINISHED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
        FINISHED.cancel(false);
    }

    static final class DirectTask
    extends AtomicBoolean
    implements Callable<Void>, Disposable {

        private static final long serialVersionUID = 2906995671043870792L;

        final ResourceTask task;

        final AtomicReference<Future<?>> future;

        DirectTask(ResourceTask task) {
            this.task = task;
            this.future = new AtomicReference<>();
        }

        @Override
        public Void call() throws Exception {
            if (compareAndSet(false, true)) {
                try {
                    task.run();
                } finally {
                    Future<?> f = future.get();
                    if (f != CANCELLED) {
                        future.compareAndSet(f, FINISHED);
                    }
                }
            }
            return null;
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                Future<?> f = future.getAndSet(CANCELLED);
                if (f != null) {
                    f.cancel(true);
                }

                task.onCancel();
            }
        }

        @Override
        public boolean isDisposed() {
            Future<?> f = future.get();
            return f == CANCELLED || f == FINISHED;
        }

        public void setFuture(Future<?> future) {
            if (this.future.compareAndSet(null, future)) {
                Future<?> f = this.future.get();
                if (f == CANCELLED) {
                    future.cancel(true);
                }
            }
        }
    }
}
