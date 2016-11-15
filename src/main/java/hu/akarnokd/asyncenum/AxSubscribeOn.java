package hu.akarnokd.asyncenum;

import java.util.concurrent.*;

import rx.subscriptions.CompositeSubscription;

public final class AxSubscribeOn<T> extends Ax<T> {

    final IAsyncEnumerable<? extends T> source;

    final Executor executor;

    public AxSubscribeOn(IAsyncEnumerable<? extends T> source, Executor executor) {
        this.source = source;
        this.executor = executor;
    }

    @Override
    public IAsyncEnumerator<T> enumerator() {
        AxSubscribeOnEnumerator<T> enumerator = new AxSubscribeOnEnumerator<>(executor);
        executor.execute(() -> {
            IAsyncEnumerator<? extends T> ae = source.enumerator();
            enumerator.setEnumerator(ae);
        });
        return enumerator;
    }

    static final class AxSubscribeOnEnumerator<T> implements IAsyncEnumerator<T> {

        final Executor executor;

        final CompletableFuture<IAsyncEnumerator<? extends T>> onEnumerator;

        AxSubscribeOnEnumerator(Executor executor) {
            this.executor = executor;
            this.onEnumerator = new CompletableFuture<>();
        }

        void setEnumerator(IAsyncEnumerator<? extends T> enumerator) {
            onEnumerator.complete(enumerator);
        }

        @Override
        public CompletionStage<Boolean> moveNext(CompositeSubscription token) {
            return onEnumerator.thenComposeAsync(ae -> ae.moveNext(token), executor);
        }

        @Override
        public T current() {
            IAsyncEnumerator<? extends T> ae = onEnumerator.getNow(null);
            return ae != null ? ae.current() : null;
        }

    }
}
