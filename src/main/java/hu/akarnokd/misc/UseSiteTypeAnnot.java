package hu.akarnokd.misc;

import java.lang.annotation.*;

import io.reactivex.Observable;
import io.reactivex.functions.Function;

public class UseSiteTypeAnnot<T> {

    public <U, R> Observable<T> map(Function<@NN T, @NN R> mapper) {
        return null;
    }
}

@Target({ElementType.TYPE_USE, ElementType.TYPE_PARAMETER})
@interface NN {
}
