package hu.akarnokd.akka;

import org.reactivestreams.*;

import com.typesafe.config.*;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;
import akka.stream.scaladsl.Source;
import io.reactivex.Flowable;

public class AkkaHello {

    static Subscriber<Object> println() {
        return new Subscriber<Object>() {

            @Override
            public void onSubscribe(Subscription s) {
                s.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Object t) {
                System.out.println(Thread.currentThread().getName() + ": " + t);
            }

            @Override
            public void onError(Throwable t) {
                t.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println(Thread.currentThread().getName() + ": DONE");
            }
        };
    }
    
    public static void main(String[] args) throws Exception {
        Config cfg = ConfigFactory.parseResources(AkkaHello.class, "/akka-streams.conf").resolve();
        ActorSystem actorSystem = ActorSystem.create("sys", cfg);

        ActorMaterializer materializer = ActorMaterializer.create(actorSystem);

        Integer[] c = { 0 };
        
        Source<Integer, NotUsed> source = Source.fromPublisher(Flowable.fromCallable(() -> c[0]++));
        
        Publisher<Integer> p = source.runWith(Sink.asPublisher(AsPublisher.WITH_FANOUT), materializer);

        p.subscribe(println());

        Thread.sleep(1000);
        
        
        try {
            p.subscribe(println());

            Thread.sleep(1000);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
        
        p = source.runWith(Sink.asPublisher(AsPublisher.WITH_FANOUT), materializer);

        try {
            p.subscribe(println());

            Thread.sleep(1000);
        } catch (Throwable ex) {
            ex.printStackTrace();
        }

        Thread.sleep(1000);
    }
}
