package hu.akarnokd.rxjava2;

import static java.util.Collections.singletonList;

import java.util.*;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.*;

public class BehaviorSubjectCombineLatestTest {

    @Test
    public void test() {
        Subject<Integer> intSource = BehaviorSubject.createDefault(1);

        Subject<List<Observable<Integer>>> mainSubject =
                BehaviorSubject.createDefault(singletonList(intSource));

        TestObserver<List<Integer>> testObserver =
                mainSubject.flatMap(observables ->
                Observable.combineLatest(observables, this::castObjectsToInts)
                        )
                .test();

        List<Observable<Integer>> newValue = new ArrayList<>();
        newValue.add(intSource); // same value as before
        newValue.add(Observable.just(2)); // add another value to this list.

        mainSubject.onNext(newValue);

        // intSource was already '1', but this is just to 'update' it.
        intSource.onNext(1); // COMMENT OUT THIS LINE

        testObserver.assertValueAt(0, singletonList(1));
        testObserver.assertValueAt(1, Arrays.asList(1, 2));
        testObserver.assertValueAt(2, Arrays.asList(1)); // COMMENT OUT THIS LINE
        testObserver.assertValueAt(3, Arrays.asList(1, 2)); // COMMENT OUT THIS LINE
        testObserver.assertValueCount(4); // REPLACE 3 WITH 2
    }

    private List<Integer> castObjectsToInts(Object[] objects) {
        List<Integer> ints = new ArrayList<>(objects.length);
        for (Object object : objects) {
            ints.add((Integer) object);
        }
        return ints;
    }
}
