package hu.akarnokd.enumerables;

import java.util.Iterator;

final class IEFromIterable<T> implements IEnumerable<T> {

    final Iterable<T> source;

    IEFromIterable(Iterable<T> source) {
        this.source = source;
    }

    @Override
    public IEnumerator<T> enumerator() {
        return new FromIterableEnumerator<>(source.iterator());
    }

    static final class FromIterableEnumerator<T> extends BasicEnumerator<T> {

        final Iterator<T> source;

        FromIterableEnumerator(Iterator<T> source) {
            this.source = source;
        }

        @Override
        public boolean moveNext() {
            if (source.hasNext()) {
                value = source.next();
                return true;
            }
            value = null;
            return false;
        }
    }
}
