package hu.akarnokd.rxjava2;

import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;

public class PartialCollectTest {

    interface PartialCollection<T, I, A, R> {

        I partialIndex();

        void partialIndex(I newIndex);

        int size();

        T item(int index);

        void dropFront(int count);

        A accumulator();

        void accumulator(A newAccumulator);

        void onNext(R output);
    }

    public static <T, I, A, R> Flowable<R> partialCollect(Consumer<? super PartialCollection<T, I, A, R>> collector) {
        throw new UnsupportedOperationException();
    }
}
