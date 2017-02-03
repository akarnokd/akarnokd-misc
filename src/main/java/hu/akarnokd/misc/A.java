package hu.akarnokd.misc;

import java.util.function.Function;

public class A {
    
    interface C<T> {
        
    }
    
    interface O<T> {
        Object r(C<T> s);
    }
    
    static <T, R> O<R> m(O<T> source, Function<T, O<R>> mapper) {
        return o -> {
            class D {
                class E {
                    
                }
                
                E e = new E();
            }

            D d = new D();
            
            return d.e;
        };
    }
    
    public static void main(String[] args) {
        m(null, null);
    }
}
