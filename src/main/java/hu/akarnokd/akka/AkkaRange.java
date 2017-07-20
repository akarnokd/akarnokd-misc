package hu.akarnokd.akka;

import org.reactivestreams.*;

import com.typesafe.config.*;

import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.*;

public class AkkaRange {

    static Subscriber<Object> println() {
        return new Subscriber<Object>() {

            Subscription s;
            
            int n;
            
            @Override
            public void onSubscribe(Subscription s) {
                this.s = s;
                s.request(5);
            }

            @Override
            public void onNext(Object t) {
                System.out.println(Thread.currentThread().getName() + ": " + t + " - " + (++n));
                if (s != null) {
                    s.cancel();
                    s = null;
                }
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
        Config cfg = ConfigFactory.parseResources(AkkaRange.class, "/akka-streams.conf").resolve();
        ActorSystem actorSystem = ActorSystem.create("sys", cfg);

        ActorMaterializer materializer = ActorMaterializer.create(actorSystem);

        Source<Integer, NotUsed> source = Source.repeat(1)
                .map(v -> v + 1);
        
        Publisher<Integer> p = source.runWith(Sink.asPublisher(AsPublisher.WITH_FANOUT), materializer);

        p.subscribe(println());

        Thread.sleep(1000);

        actorSystem.terminate();
    }
}
