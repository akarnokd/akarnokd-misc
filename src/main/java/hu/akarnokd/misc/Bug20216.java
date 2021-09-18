package hu.akarnokd.misc;

import java.util.function.Consumer;

public class Bug20216 {
    static <T> Consumer<T> compileError() {
        return v -> {
            /*
            class Z implements Consumer<T> {
                @Override
                public void accept(T t) {

                }
            }
            */
        };
    }
}
