package hu.akarnokd.enumerables;

final class IESumLong<T> implements IEnumerable<Long> {

    final IEnumerable<T> source;
    
    IESumLong(IEnumerable<T> source) {
        this.source = source;
    }
    
    @Override
    public IEnumerator<Long> enumerator() {
        return new SumLongEnumerator<>(source.enumerator());
    }
    
    static final class SumLongEnumerator<T> extends BasicEnumerator<Long> {
        
        final IEnumerator<T> source;

        boolean once;

        SumLongEnumerator(IEnumerator<T> source) {
            this.source = source;
        }

        @Override
        public boolean moveNext() {
            if (!once) {
                once = true;
                IEnumerator<T> src = source;
                long c = 0;
                if (src.moveNext()) {
                    c += ((Number)src.current()).longValue();
                    while (src.moveNext()) {
                        c += ((Number)src.current()).longValue();
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
