package hu.akarnokd.rxjava;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.*;

import org.junit.*;
import org.mockito.*;

import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.*;

public class ATest {

    private interface Gateway {

        Observable<List<Item>> getItems();
    }

    static class Item {

        final int id;

        Item(int id) {
            this.id = id;
        }
    }

    @Mock private Gateway memory;
    @Mock private Gateway disk;
    @Mock private Gateway cloud;

    private TestScheduler ioScheduler;
    private TestScheduler uiScheduler;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        ioScheduler = Schedulers.test();
        uiScheduler = Schedulers.test();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void interestingTest() {
        TestSubscriber subscriber = new TestSubscriber();

        when(memory.getItems()).thenReturn(Observable.just(new ArrayList<>()));
        when(disk.getItems()).thenReturn(Observable.just(new ArrayList<>()));
        when(cloud.getItems()).thenReturn(Observable.just(new ArrayList<>()));

        List<Item> defaultList = new ArrayList<>();
        Observable.concat(
                memory.getItems(),
                disk.getItems()
                        .filter(items -> items != null && !items.isEmpty())
                        .observeOn(uiScheduler),
                cloud.getItems()
                        .filter(items -> items != null && !items.isEmpty())
                        .subscribeOn(ioScheduler)
                        .observeOn(uiScheduler)
        )
                .firstOrDefault(defaultList, items -> items != null && !items.isEmpty())
                .subscribe(subscriber);

        //uiScheduler.advanceTimeBy(0, TimeUnit.NANOSECONDS); // comment this and test will fail
        ioScheduler.triggerActions();
        uiScheduler.triggerActions();
        ioScheduler.triggerActions();
        uiScheduler.triggerActions();

        assertTrue(subscriber.getOnNextEvents().get(0) == defaultList);
    }
}