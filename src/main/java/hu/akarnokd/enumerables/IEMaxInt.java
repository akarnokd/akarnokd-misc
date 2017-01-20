package hu.akarnokd.enumerables;

final class IEMaxInt<T> implements IEnumerable<Integer> {

    final IEnumerable<T> source;

    IEMaxInt(IEnumerable<T> source) {
        this.source = source;
    }

    @Override
    public IEnumerator<Integer> enumerator() {
        return new MaxIntEnumerator<>(source.enumerator());
    }

    static final class MaxIntEnumerator<T> extends BasicEnumerator<Integer> {

        final IEnumerator<T> source;

        boolean once;

        MaxIntEnumerator(IEnumerator<T> source) {
            this.source = source;
        }

        @Override
        public boolean moveNext() {
            if (!once) {
                once = true;
                IEnumerator<T> src = source;
                int c = 0;
                if (src.moveNext()) {
                    c = ((Number)src.current()).intValue();
                    while (src.moveNext()) {
                        c = Math.max(c, ((Number)src.current()).intValue());
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
