package hu.akarnokd.enumerables;

final class IEConcatArray<T> implements IEnumerable<T> {

    final IEnumerable<? extends T>[] sources;
    
    IEConcatArray(IEnumerable<? extends T>[] sources) {
        this.sources = sources;
    }
    
    @Override
    public IEnumerator<T> enumerator() {
        return new ConcatArrayEnumerator<>(sources);
    }
    
    static final class ConcatArrayEnumerator<T> extends BasicEnumerator<T> {
        
        final IEnumerable<? extends T>[] sources;

        int index;
        
        IEnumerator<? extends T> current;

        boolean done;
        
        ConcatArrayEnumerator(IEnumerable<? extends T>[] sources) {
            this.sources = sources;
        }
        
        @Override
        public boolean moveNext() {
            IEnumerator<? extends T> c = current;
            if (!done) {
                int i = index;
                IEnumerable<? extends T>[] srcs = sources;
                int n = srcs.length;
                for (;;) {
                    if (c == null) {
                        if (i == n) {
                            value = null;
                            current = null;
                            done = true;
                            return false;
                        }
                        
                        c = srcs[i].enumerator();
                        current = c;
                        index = ++i;
                    }
                    
                    if (c.moveNext()) {
                        value = c.current();
                        return true;
                    }
                    c = null;
                }
            }
            return false;
        }
    }
}
