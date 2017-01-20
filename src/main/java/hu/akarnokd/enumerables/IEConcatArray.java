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

    @Override
    public IEnumerable<Integer> sumInt() {
        return new ConcatArraySumInt<>(sources);
    }

    static final class ConcatArraySumInt<T> implements IEnumerable<Integer> {

        final IEnumerable<? extends T>[] sources;

        ConcatArraySumInt(IEnumerable<? extends T>[] sources) {
            this.sources = sources;
        }

        @Override
        public IEnumerator<Integer> enumerator() {
            return new ConcatArraySumIntEnumerator<>(sources);
        }

        static final class ConcatArraySumIntEnumerator<T> extends BasicEnumerator<Integer> {

            final IEnumerable<? extends T>[] sources;

            boolean once;

            ConcatArraySumIntEnumerator(IEnumerable<? extends T>[] sources) {
                this.sources = sources;
            }

            @Override
            public boolean moveNext() {
                if (!once) {
                    once = true;

                    if (sources.length != 0) {
                        int c = 0;
                        boolean nonEmpty = false;
                        for (IEnumerable<? extends T> s : sources) {
                            IEnumerator<? extends T> en = s.enumerator();
                            if (en.moveNext()) {
                                nonEmpty = true;
                                c += ((Number)en.current()).intValue();
                                while (en.moveNext()) {
                                    c += ((Number)en.current()).intValue();
                                }
                            }
                        }
                        if (nonEmpty) {
                            value = c;
                            return true;
                        }
                    }
                    return false;
                }
                return false;
            }
        }
    }
}
