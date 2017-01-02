package hu.akarnokd.enumerables;

import java.util.function.Predicate;

final class IEFilter<T> implements IEnumerable<T> {

    final IEnumerable<T> source;
    
    final Predicate<? super T> predicate;

    public IEFilter(IEnumerable<T> source, Predicate<? super T> predicate) {
        this.source = source;
        this.predicate = predicate;
    }
    
    @Override
    public IEnumerator<T> enumerator() {
        return new FilterEnumerator<>(source.enumerator(), predicate);
    }
    
    static final class FilterEnumerator<T> extends BasicEnumerator<T> {
        
        final IEnumerator<T> source;
        
        final Predicate<? super T> predicate;

        public FilterEnumerator(IEnumerator<T> source, Predicate<? super T> predicate) {
            this.source = source;
            this.predicate = predicate;
        }
        
        @Override
        public boolean moveNext() {
            IEnumerator<T> src = source;
            Predicate<? super T> pr = predicate;

            while (src.moveNext()) {
                T t = src.current();
                if (pr.test(t)) {
                    value = t;
                    return true;
                }
            }
            value = null;
            return false;
        }
    }
}
