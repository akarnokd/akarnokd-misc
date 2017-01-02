package hu.akarnokd.enumerables;

import java.util.Iterator;
import java.util.function.Function;

final class IEFlatMapIterable<T, R> implements IEnumerable<R> {

    final IEnumerable<T> source;
    
    final Function<? super T, ? extends Iterable<? extends R>> mapper;

    public IEFlatMapIterable(IEnumerable<T> source, Function<? super T, ? extends Iterable<? extends R>> mapper) {
        this.source = source;
        this.mapper = mapper;
    }
    
    @Override
    public IEnumerator<R> enumerator() {
        return new FlatMapIterator<>(source.enumerator(), mapper);
    }
    
    static final class FlatMapIterator<T, R> extends BasicEnumerator<R> {
        
        final IEnumerator<T> source;
        
        final Function<? super T, ? extends Iterable<? extends R>> mapper;

        Iterator<? extends R> current;

        boolean done;
        
        FlatMapIterator(IEnumerator<T> source, Function<? super T, ? extends Iterable<? extends R>> mapper) {
            this.source = source;
            this.mapper = mapper;
        }
        
        @Override
        public boolean moveNext() {
            Iterator<? extends R> c = current;
            if (!done) {
                for (;;) {
                    if (c == null) {
                        if (!source.moveNext()) {
                            value = null;
                            current = null;
                            done = true;
                            return false;
                        }
                        
                        c = mapper.apply(source.current()).iterator();
                        current = c;
                    }
                    
                    if (c.hasNext()) {
                        value = c.next();
                        return true;
                    }
                    c = null;
                }
            }
            return false;
        }
    }
}
