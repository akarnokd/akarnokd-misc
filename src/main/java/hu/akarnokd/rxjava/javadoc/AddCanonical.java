package hu.akarnokd.rxjava.javadoc;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

import com.google.api.client.util.Charsets;

/**
 * Add "link rel='canonical' to the javadoc htmls of RxJava 1"
 */
public final class AddCanonical {

    private AddCanonical() { }

    static final Map<String, String> canonicals = new HashMap<>(); 
    
    static void init() {
        // ----------------------------------------------------------------------------------

        canonicals.put("/rx/Observable.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html");
        canonicals.put("/rx/Observable.OnSubscribe.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableOnSubscribe.html");
        canonicals.put("/rx/Observable.Operator.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableOperator.html");
        canonicals.put("/rx/Observable.Tranformer.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableTransformer.html");
        canonicals.put("/rx/Subscriber.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableSubscriber.html");
        canonicals.put("/rx/Emitter.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableEmitter.html");

        // ----------------------------------------------------------------------------------

        canonicals.put("/rx/Observer.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Observer.html");
        canonicals.put("/rx/Subscription.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposable.html");
        canonicals.put("/rx/Notification.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Notification.html");

        canonicals.put("/rx/Scheduler.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Scheduler.html");
        canonicals.put("/rx/Scheduler.Worker.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Scheduler.Worker.html");
        canonicals.put("/rx/scheduler/Schedulers.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/schedulers/Schedulers.html");
        canonicals.put("/rx/scheduler/TestScheduler.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/schedulers/TestScheduler.html");
        canonicals.put("/rx/scheduler/TimeInterval.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/schedulers/Timed.html");
        canonicals.put("/rx/scheduler/Timestamped.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/schedulers/Timed.html");
        canonicals.put("/rx/scheduler/TrampolineScheduler.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/schedulers/Schedulers.html#trampoline%28%29");

        canonicals.put("/rx/plugins/RxJavaPlugins.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/plugins/RxJavaPlugins.html");
        canonicals.put("/rx/plugins/RxJavaHooks.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/plugins/RxJavaPlugins.html");
        canonicals.put("/rx/plugins/RxJavaCompletableExecutionHook.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/plugins/RxJavaPlugins.html");
        canonicals.put("/rx/plugins/RxJavaSingleExecutionHook.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/plugins/RxJavaPlugins.html");
        canonicals.put("/rx/plugins/RxJavaObservableExecutionHook.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/plugins/RxJavaPlugins.html");
        canonicals.put("/rx/plugins/RxJavaSchedulersHook.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/plugins/RxJavaPlugins.html");

        canonicals.put("/rx/BackpressureOverflow.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/BackpressureOverflowStrategy.html");
        canonicals.put("/rx/BackpressureOverflow.Strategy.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/BackpressureOverflowStrategy.html");

        canonicals.put("/rx/Emitter.BackpressureMode.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/BackpressureStrategy.html");

        canonicals.put("/rx/Producer.html", 
                "http://www.reactive-streams.org/reactive-streams-1.0.0-javadoc/org/reactivestreams/Subscription.html");

        canonicals.put("/rx/annotations/Beta.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/annotations/Beta.html");

        canonicals.put("/rx/annotations/Experimental.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/annotations/Experimental.html");

        canonicals.put("/rx/exceptions/Exceptions.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/exceptions/Exceptions.html");
        canonicals.put("/rx/exceptions/CompositeException.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/exceptions/CompositeException.html");
        canonicals.put("/rx/exceptions/MissingBackpressureException.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/exceptions/MissingBackpressureException.html");
        canonicals.put("/rx/exceptions/OnErrorNotImplementedException.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/exceptions/OnErrorNotImplementedException.html");

        canonicals.put("/rx/functions/Action0.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Action.html");
        canonicals.put("/rx/functions/Action1.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html");
        canonicals.put("/rx/functions/Action2.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BiConsumer.html");
        canonicals.put("/rx/functions/ActionN.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Consumer.html");
        canonicals.put("/rx/functions/Cancellable.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Cancellable.html");

        canonicals.put("/rx/functions/Func0.html", 
                "https://docs.oracle.com/javase/6/docs/api/java/util/concurrent/Callable.html");
        canonicals.put("/rx/functions/Func1.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function.html");
        canonicals.put("/rx/functions/Func2.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/BiFunction.html");
        canonicals.put("/rx/functions/Func3.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function3.html");
        canonicals.put("/rx/functions/Func4.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function4.html");
        canonicals.put("/rx/functions/Func5.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function5.html");
        canonicals.put("/rx/functions/Func6.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function6.html");
        canonicals.put("/rx/functions/Func7.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function7.html");
        canonicals.put("/rx/functions/Func8.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function8.html");
        canonicals.put("/rx/functions/Func9.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function9.html");
        canonicals.put("/rx/functions/FuncN.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/functions/Function.html");

        canonicals.put("/rx/observables/ConnectableObservable.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/flowables/ConnectableFlowable.html");
        canonicals.put("/rx/observables/GroupedObservable.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/flowables/GroupedFlowable.html");
        canonicals.put("/rx/observables/BlockingObservable.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Flowable.html#blockingIterable%28%29");

        canonicals.put("/rx/observables/SyncOnSubscribe.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/FlowableEmitter.html");

        canonicals.put("/rx/observers/TestObserver.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/observers/TestObserver.html");
        canonicals.put("/rx/observers/TestSubscriber.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/subscribers/TestSubscriber.html");
        canonicals.put("/rx/observers/AsyncCompletableSubscriber.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/observers/DisposableCompletableObserver.html");
        canonicals.put("/rx/observers/SafeSubscriber.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/subscribers/SafeSubscriber.html");
        canonicals.put("/rx/observers/SerializedObserver.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/observers/SerializedObserver.html");
        canonicals.put("/rx/observers/SerializedSubscriber.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/subscribers/SerializedSubscriber.html");

        canonicals.put("/rx/subjects/AsyncSubject.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/processors/AsyncProcessor.html");
        canonicals.put("/rx/subjects/PublishSubject.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/processors/PublishProcessor.html");
        canonicals.put("/rx/subjects/ReplaySubject.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/processors/ReplayProcessor.html");
        canonicals.put("/rx/subjects/BehaviorSubject.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/processors/BehaviorProcessor.html");
        canonicals.put("/rx/subjects/UnicastSubject.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/processors/UnicastProcessor.html");
        canonicals.put("/rx/subjects/SerializedSubject.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/processors/FlowableProcessor.html#toSerialized%28%29");

        canonicals.put("/rx/subscriptions/BooleanSubscription.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposables.html#empty%28%29");
        canonicals.put("/rx/subscriptions/CompositeSubscription.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/CompositeDisposable.html");
        canonicals.put("/rx/subscriptions/MultipleAssignmentSubscription.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/SerialDisposable.html");
        canonicals.put("/rx/subscriptions/SingleAssignmentSubscription.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/SerialDisposable.html");
        canonicals.put("/rx/subscriptions/RefCountSubscription.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/SerialDisposable.html");
        canonicals.put("/rx/subscriptions/Subscriptions.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/disposables/Disposables.html");

        // ----------------------------------------------------------------------------------
        
        canonicals.put("/rx/Single.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html");
        canonicals.put("/rx/Single.OnSubscribe.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleOnSubscribe.html");
        canonicals.put("/rx/Single.Operator.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleOperator.html");
        canonicals.put("/rx/Single.Tranformer.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleTransformer.html");
        canonicals.put("/rx/SingleSubscriber.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/SingleObserver.html");
        canonicals.put("/rx/singles/BlockingSingle.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Single.html#blockingGet%28%29");

        // ----------------------------------------------------------------------------------

        canonicals.put("/rx/Completable.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/Completable.html");
        canonicals.put("/rx/Completable.OnSubscribe.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableOnSubscribe.html");
        canonicals.put("/rx/Completable.Operator.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableOperator.html");
        canonicals.put("/rx/Completable.Tranformer.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableTransformer.html");
        canonicals.put("/rx/CompletableSubscriber.html", 
                "http://reactivex.io/RxJava/2.x/javadoc/io/reactivex/CompletableObserver.html");

        // ----------------------------------------------------------------------------------
        
        int failed = 0;
        for (String urls : canonicals.values()) {
            System.out.print("Checking: " + urls);
            try {
                URL u = new URL(urls);
                u.openStream().close();
                System.out.println(" -> Success");
                Thread.sleep(100);
            } catch (IOException | InterruptedException ex) {
                System.err.println(ex);
                failed++;
            }
        }
        
        if (failed != 0) {
            throw new RuntimeException("Some url's don't connect!: " + failed);
        }
    }

    static void process(File directory) throws IOException {
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {

                String name = file.toString().replace('\\', '/');
                if (name.endsWith(".html")) {
                    int idx = name.indexOf("/rx/");
                    String relevantPart = name.substring(idx);
                    
                    String canonical = canonicals.get(relevantPart);

                    if (canonical != null) {
                        appendLink(file, canonical);
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });
    }
    
    static void appendLink(Path file, String url) throws IOException {
        byte[] data = Files.readAllBytes(file);
        
        byte[] alreadyThere = "<link rel=\"canonical\"".getBytes(Charsets.ISO_8859_1);
        
        if (arrayIndexOf(data, alreadyThere, 0) != -1) {
            System.out.printf("File %s has a link-rel-canonical already.%n", file);
        } else {
            byte[] endHead = "</head>".getBytes(Charsets.ISO_8859_1);
            int endHeadIndex = arrayIndexOf(data, endHead, 0);
            if (endHeadIndex < 0) {
                System.out.printf("File %s has no </head>?!%n", file);
            } else {
                System.out.printf("Adding link %s to file %s.%n", url, file);
                byte[] toInsert = ("<link rel=\"canonical\" href=\"" + url + "\"/>").getBytes(Charsets.ISO_8859_1);
                
                byte[] newData = new byte[data.length + toInsert.length];
                System.arraycopy(data, 0, newData, 0, endHeadIndex);
                System.arraycopy(toInsert, 0, newData, endHeadIndex, toInsert.length);
                System.arraycopy(data, endHeadIndex, newData, endHeadIndex + toInsert.length, data.length - endHeadIndex);
                Files.write(file, newData);
            }
        }
    }
    
    static int arrayIndexOf(byte[] source, byte[] toFind, int start) {
        outer:
        for (int i = start; i < source.length - toFind.length; i++) {
            if (source[i] == toFind[0]) {
                for (int j = i + 1; j < i + toFind.length; j++) {
                    if (source[j] != toFind[j - i]) {
                        continue outer;
                    }
                }
                return i;
            }
        }
        return -1;
    }

    public static void main(String[] args) throws IOException {
        init();
        process(new File("..\\RxJava11\\javadoc\\rx"));
        process(new File("..\\RxJava11\\1.x\\javadoc\\rx"));
    }

}
