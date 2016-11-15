package hu.akarnokd.asyncenum;

import java.util.concurrent.*;

import rx.subscriptions.CompositeSubscription;

public final class AxObserveOn<T> extends Ax<T> {

    final IAsyncEnumerable<? extends T> source;

    final Executor executor;

    public AxObserveOn(IAsyncEnumerable<? extends T> source, Executor executor) {
        this.source = source;
        this.executor = executor;
    }

    @Override
    public IAsyncEnumerator<T> enumerator() {
        return new AxObserveOnEnumerator<>(source.enumerator(), executor);
    }

    static final class AxObserveOnEnumerator<T> implements IAsyncEnumerator<T> {
        final IAsyncEnumerator<? extends T> enumerator;

        final Executor executor;

        AxObserveOnEnumerator(IAsyncEnumerator<? extends T> enumerator, Executor executor) {
            this.enumerator = enumerator;
            this.executor = executor;
        }

        @Override
        public T current() {
            return enumerator.current();
        }

        @Override
        public CompletionStage<Boolean> moveNext(CompositeSubscription token) {
            return enumerator.moveNext(token).thenApplyAsync(v -> v, executor);
        }
    }
}
