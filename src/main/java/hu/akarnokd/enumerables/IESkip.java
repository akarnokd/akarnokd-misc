package hu.akarnokd.enumerables;

final class IESkip<T> implements IEnumerable<T> {

    final IEnumerable<T> source;

    final int n;
    
    IESkip(IEnumerable<T> source, int n) {
        this.source = source;
        this.n = n;
    }
    
    @Override
    public IEnumerator<T> enumerator() {
        return new SkipEnumerator<>(source.enumerator(), n);
    }
    
    static final class SkipEnumerator<T> implements IEnumerator<T> {

        final IEnumerator<T> source;

        int n;

        SkipEnumerator(IEnumerator<T> source, int n) {
            this.source = source;
            this.n = n;
        }
        
        @Override
        public boolean moveNext() {
            int i = n;
            if (i != 0) {
                while (i != 0) {
                    if (!source.moveNext()) {
                        n = 0;
                        return false;
                    }
                    i--;
                }
                n = 0;
            }
            return source.moveNext();
        }
        
        @Override
        public T current() {
            return source.current();
        }
    }
}
