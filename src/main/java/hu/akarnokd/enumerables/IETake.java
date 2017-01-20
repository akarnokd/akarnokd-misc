package hu.akarnokd.enumerables;

final class IETake<T> implements IEnumerable<T> {

    final IEnumerable<T> source;

    final int n;

    IETake(IEnumerable<T> source, int n) {
        this.source = source;
        this.n = n;
    }

    @Override
    public IEnumerator<T> enumerator() {
        return new TakeEnumerator<>(source.enumerator(), n);
    }

    static final class TakeEnumerator<T> implements IEnumerator<T> {

        final IEnumerator<T> source;

        int n;

        TakeEnumerator(IEnumerator<T> source, int n) {
            this.source = source;
            this.n = n;
        }

        @Override
        public boolean moveNext() {
            int i = n;
            if (i != 0) {
                if (source.moveNext()) {
                    n = i - 1;
                    return true;
                }
                i = 0;
            }
            return false;
        }

        @Override
        public T current() {
            return source.current();
        }
    }
}
