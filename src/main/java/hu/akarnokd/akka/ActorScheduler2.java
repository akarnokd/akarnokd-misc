package hu.akarnokd.akka;

import java.util.HashSet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import akka.actor.*;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.EmptyDisposable;

public class ActorScheduler2
extends io.reactivex.Scheduler {

    final ActorRef actor;

    final ActorSystem system;

    public ActorScheduler2(ActorSystem system, boolean freshWorker) {
        this.actor = system.actorOf(Props.create(ActorExecutor.class));
        this.system = freshWorker ? system : null;
    }

    @Override
    public Disposable scheduleDirect(Runnable task) {
        DirectRunnable dr = new DirectRunnable(task);
        actor.tell(dr, ActorRef.noSender());
        return dr;
    }

    @Override
    public Worker createWorker() {
        if (system == null) {
            return new ActorWorker(actor, null);
        }
        return new ActorWorker(system.actorOf(Props.create(ActorExecutor.class)), system);
    }


    static final class ActorWorker extends Worker {

        final ActorRef actor;

        final ActorSystem ctx;

        HashSet<WorkerRunnable> tasks;

        ActorWorker(ActorRef actor, ActorSystem ctx) {
            this.actor = actor;
            this.tasks = new HashSet<>();
            this.ctx = ctx;
        }

        @Override
        public Disposable schedule(Runnable task) {
            WorkerRunnable wr = new WorkerRunnable(task, this);

            synchronized (this) {
                HashSet<WorkerRunnable> set = tasks;
                if (set == null) {
                    return EmptyDisposable.INSTANCE;
                }
                set.add(wr);
            }

            actor.tell(wr, ActorRef.noSender());

            return wr;
        }

        @Override
        public void dispose() {
            HashSet<WorkerRunnable> set;

            synchronized (this) {
                set = tasks;
                tasks = null;
            }

            if (set != null) {
                for (WorkerRunnable wr : set) {
                    wr.delete();
                }
            }

            if (ctx != null) {
                ctx.stop(actor);
            }
        }

        void delete(WorkerRunnable run) {
            synchronized (this) {
                HashSet<WorkerRunnable> set = tasks;
                if (set == null) {
                    return;
                }
                set.remove(run);
            }
        }

        @Override
        public boolean isDisposed() {
            return false;
        }

        @Override
        public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
            throw new UnsupportedOperationException();
        }
    }

    static final class DirectRunnable
    extends AtomicBoolean implements Runnable, Disposable {
        private static final long serialVersionUID = -8208677295345126172L;

        final Runnable run;

        DirectRunnable(Runnable run) {
            this.run = run;
        }

        @Override
        public void run() {
            if (!get()) {
                run.run();
            }
        }

        @Override
        public void dispose() {
            set(true);
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }

    static final class WorkerRunnable
    extends AtomicBoolean implements Runnable, Disposable {
        private static final long serialVersionUID = -1760219254778525714L;

        final Runnable run;

        final ActorWorker parent;

        WorkerRunnable(Runnable run, ActorWorker parent) {
            this.run = run;
            this.parent = parent;
        }

        @Override
        public void run() {
            if (!get()) {
                try {
                    run.run();
                } finally {
                    if (compareAndSet(false, true)) {
                        parent.delete(this);
                    }
                }
            }
        }

        @Override
        public void dispose() {
            if (compareAndSet(false, true)) {
                parent.delete(this);
            }
        }

        public void delete() {
            set(true);
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }

//    public static void main(String[] args) throws Exception {
//        Config cfg = ConfigFactory.parseResources(ReactiveStreamsImpls.class, "/akka-streams.conf");
//        ActorSystem actorSystem = ActorSystem.create("sys", cfg);
//
//        ActorRef actor = actorSystem.actorOf(Props.create(ActorExecutor.class));
//
//        Runnable run = () -> System.out.println("Hello world!");
//        actor.tell(run, ActorRef.noSender());
//
//        Thread.sleep(1000);
//
//        actorSystem.terminate();
//    }

    @SuppressWarnings("deprecation")
    static final class ActorExecutor extends UntypedActor {

        @Override
        public void onReceive(Object message) throws Exception {
            Runnable r = (Runnable)message;

            r.run();
        }
    }
}
