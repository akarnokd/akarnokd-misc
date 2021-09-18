package hu.akarnokd.misc;

interface C<T> { }

class Klass<T> implements C<T> {
    Klass(java.util.function.Consumer<T> consumer) { }
}

public class InferenceError<T> {

    public <E extends C<T>> E method(E e) {
        return e;
    }

    static void n() {
        /*
        InferenceError<Long> p = new InferenceError<>();
        java.util.List<Long> list = new java.util.ArrayList<>();
        //p.method(new Klass<>(list::add));
         * 
         */
    }
}


