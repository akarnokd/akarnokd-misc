package hu.akarnokd.rxjava2;

import java.util.concurrent.*;

import io.reactivex.Scheduler;
import io.reactivex.disposables.*;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.disposables.SequentialDisposable;
import io.reactivex.internal.schedulers.RxThreadFactory;
import io.reactivex.plugins.RxJavaPlugins;

public final class WeakParallelScheduler extends Scheduler {

    final ExecutorService[] executors;
    
    static final Scheduler timed;
    
    static {
        timed = io.reactivex.schedulers.Schedulers.single();
    }
    
    int n;
    
    
    public WeakParallelScheduler() {
        this(Runtime.getRuntime().availableProcessors());
    }
    
    public WeakParallelScheduler(int parallelism) {
        ExecutorService[] execs = new ExecutorService[parallelism];
        
        for (int i = 0; i < parallelism; i++) {
            execs[i] = Executors.newSingleThreadExecutor(new RxThreadFactory("RxWeakParallelScheduler"));
        }
        
        executors = execs;
    }
    
    ExecutorService get() {
        ExecutorService[] execs = executors;
        int idx = n;
        if (idx + 1 == execs.length) {
            n = 0;
        } else {
            n = idx + 1;
        }
        
        return execs[idx];
    }
    
    @Override
    public Disposable scheduleDirect(Runnable run) {
        ExecutorService exec = get();
        return Disposables.fromFuture(exec.submit(run));
    }
    
    @Override
    public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
        SequentialDisposable inner = new SequentialDisposable();
        SequentialDisposable outer = new SequentialDisposable(inner);
        
        inner.replace(timed.scheduleDirect(() -> {
            outer.replace(scheduleDirect(run));
        }, delay, unit));
        
        return outer;
    }
    
    @Override
    public Worker createWorker() {
        return new WeakParallelWorker(get());
    }
    
    static final class WeakParallelWorker extends Worker {
        final ExecutorService exec;
        
        volatile boolean cancelled;
        
        public WeakParallelWorker(ExecutorService exec) {
            this.exec = exec;
        }
        
        @Override
        public Disposable schedule(Runnable run) {
            if (cancelled) {
                return Disposables.disposed();
            }
            
            WeakTask wt = new WeakTask(run);
            exec.submit(wt);
            return wt;
        }
        
        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            if (cancelled) {
                return Disposables.disposed();
            }
            SequentialDisposable inner = new SequentialDisposable();
            SequentialDisposable outer = new SequentialDisposable(inner);
            
            inner.replace(timed.scheduleDirect(() -> {
                outer.replace(schedule(run));
            }, delay, unit));
            
            return outer;
        }
        
        @Override
        public boolean isDisposed() {
            return cancelled;
        }
        
        @Override
        public void dispose() {
            cancelled = true;
        }
        
        final class WeakTask implements Callable<Void>, Disposable {
            final Runnable run;
            
            volatile boolean disposed;
            
            public WeakTask(Runnable run) {
                this.run = run;
            }
            
            @Override
            public Void call() throws Exception {
                if (!disposed && !cancelled) {
                    try {
                        run.run();
                    } catch (Throwable ex) {
                        Exceptions.throwIfFatal(ex);
                        RxJavaPlugins.onError(ex);
                    }
                }
                return null;
            }
            
            @Override
            public void dispose() {
                disposed = true;
            }
            
            @Override
            public boolean isDisposed() {
                return disposed;
            }
        }
    }
}
