package hu.akarnokd.enumerables;

final class IESumInt<T> implements IEnumerable<Integer> {

    final IEnumerable<T> source;
    
    IESumInt(IEnumerable<T> source) {
        this.source = source;
    }
    
    @Override
    public IEnumerator<Integer> enumerator() {
        return new SumIntEnumerator<>(source.enumerator());
    }
    
    static final class SumIntEnumerator<T> extends BasicEnumerator<Integer> {
        
        final IEnumerator<T> source;

        boolean once;

        SumIntEnumerator(IEnumerator<T> source) {
            this.source = source;
        }

        @Override
        public boolean moveNext() {
            if (!once) {
                once = true;
                IEnumerator<T> src = source;
                int c = 0;
                if (src.moveNext()) {
                    c += ((Number)src.current()).intValue();
                    while (src.moveNext()) {
                        c += ((Number)src.current()).intValue();
                    }
                    value = c;
                    return true;
                }
            }
            value = null;
            return false;
        }
    }
}
