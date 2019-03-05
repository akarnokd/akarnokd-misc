package hu.akarnokd.rxjava2;

public class ThrowsSubclass {

    interface F {
        void m() throws Throwable;
    }
    
    public static void main(String[] args) {
        new F() {
            @Override
            public void m() throws Exception {
                
            }
        };
    }
}
