package hu.akarnokd.enumerables;

final class IEJust<T> implements IEnumerable<T> {

    final T value;
    
    IEJust(T value) {
        this.value = value;
    }
    
    @Override
    public IEnumerator<T> enumerator() {
        return new JustEnumerator<>(value);
    }
    
    static final class JustEnumerator<T> extends BasicEnumerator<T> {
        
        boolean once;
        
        JustEnumerator(T value) {
            this.value = value;
        }
        
        @Override
        public boolean moveNext() {
            if (!once) {
                once = true;
                return true;
            }
            value = null;
            return false;
        }
    }
}
