package hu.akarnokd.misc;

public class FoBar {
    public interface Foo<T> {
        
        default boolean isEmpty() {
            return false; //dummy value real implementation is not relevant for the problem
        }
    }
    public interface Foo2<T> {
        
        default boolean isEmpty() {
            return false; //dummy value real implementation is not relevant for the problem
        }
    }
    
    public abstract class Bar<T> implements Foo<T> {
    }

    public final class BarImpl extends Bar<Character> implements CharSequence, Foo2<Character> { //typical diamond problem
        @Override
        public boolean isEmpty() { //needed to resolve diamond problem
           return super.isEmpty()
                   && Foo2.super.isEmpty()
                   && CharSequence.super.isEmpty();
        }

        @Override
        public int length() {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public char charAt(int index) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            // TODO Auto-generated method stub
            return null;
        }
    }
}
