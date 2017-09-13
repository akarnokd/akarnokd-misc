package hu.akarnokd.akka;

import java.util.HashSet;
import java.util.concurrent.atomic.AtomicBoolean;

import akka.actor.*;
import reactor.core.*;

public class ActorScheduler
implements reactor.core.scheduler.Scheduler {

    final ActorRef actor;

    public ActorScheduler(ActorSystem system) {
        this.actor = system.actorOf(Props.create(ActorExecutor.class));
    }

    @Override
    public Disposable schedule(Runnable task) {
        DirectRunnable dr = new DirectRunnable(task);
        actor.tell(dr, ActorRef.noSender());
        return dr;
    }

    @Override
    public Worker createWorker() {
        return new ActorWorker(actor);
    }


    static final class ActorWorker implements Worker {

        final ActorRef actor;

        HashSet<WorkerRunnable> tasks;

        ActorWorker(ActorRef actor) {
            this.actor = actor;
            this.tasks = new HashSet<>();
        }

        @Override
        public Disposable schedule(Runnable task) {
            WorkerRunnable wr = new WorkerRunnable(task, this);

            synchronized (this) {
                HashSet<WorkerRunnable> set = tasks;
                if (set == null) {
                    return Disposables.disposed();
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
