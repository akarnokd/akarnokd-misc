package hu.akarnokd.enumerables;

import java.util.Objects;
import java.util.function.*;

final class IECollect<T, C> implements IEnumerable<C> {

    final IEnumerable<T> source;

    final Supplier<C> supplier;

    final BiConsumer<C, T> collector;

    public IECollect(IEnumerable<T> source, Supplier<C> supplier, BiConsumer<C, T> collector) {
        this.source = source;
        this.supplier = supplier;
        this.collector = collector;
    }

    @Override
    public IEnumerator<C> enumerator() {
        return new CollectEnumerator<>(source.enumerator(), collector, supplier.get());
    }

    static final class CollectEnumerator<T, C> extends BasicEnumerator<C> {

        final IEnumerator<T> source;

        final BiConsumer<C, T> collector;

        boolean once;

        public CollectEnumerator(IEnumerator<T> source, BiConsumer<C, T> collector, C collection) {
            this.source = Objects.requireNonNull(source);
            this.collector = collector;
            this.value = collection;
        }

        @Override
        public boolean moveNext() {
            if (!once) {
                once = true;
                C c = value;

                IEnumerator<T> src = source;
                BiConsumer<C, T> coll = collector;

                while (src.moveNext()) {
                    coll.accept(c, src.current());
                }

                return true;
            }
            value = null;
            return false;
        }
    }
}
