package hu.akarnokd.misc;

public class Interfaceoverride {

    interface A {
        void f(String o);
    }
    
    static class B implements A {
        @Override
        public void f(String o) {
            
        }
    }
}
