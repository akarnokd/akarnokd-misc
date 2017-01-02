package hu.akarnokd.enumerables;

import java.util.function.Function;

final class IEMap<T, R> implements IEnumerable<R> {

    final IEnumerable<T> source;
    
    final Function<? super T, ? extends R> mapper;
    
    IEMap(IEnumerable<T> source, Function<? super T, ? extends R> mapper) {
        this.source = source;
        this.mapper = mapper;
    }
    
    @Override
    public IEnumerator<R> enumerator() {
        return new MapEnumerator<>(source.enumerator(), mapper);
    }
    
    static final class MapEnumerator<T, R> extends BasicEnumerator<R> {
        
        final IEnumerator<T> source;
        
        final Function<? super T, ? extends R> mapper;

        public MapEnumerator(IEnumerator<T> source, Function<? super T, ? extends R> mapper) {
            this.source = source;
            this.mapper = mapper;
        }

        @Override
        public boolean moveNext() {
            if (source.moveNext()) {
                value = mapper.apply(source.current());
                return true;
            }
            value = null;
            return false;
        }
    }
}
