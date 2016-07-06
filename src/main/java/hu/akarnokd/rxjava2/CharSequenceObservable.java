package hu.akarnokd.rxjava2;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.*;
import io.reactivex.disposables.*;

public final class CharSequenceObservable extends Observable<Integer> {

    final CharSequence string;
    
    public CharSequenceObservable(CharSequence string) {
        this.string = string;
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        Disposable d = new BooleanDisposable();
        
        observer.onSubscribe(d);
        
        CharSequence s = string;
        int len = s.length();
        
        for (int i = 0; i < len; i++) {
            if (d.isDisposed()) {
                return;
            }
            observer.onNext((int)s.charAt(i));
        }
        if (d.isDisposed()) {
            return;
        }
        observer.onComplete();
    }
    
    static final class BooleanDisposable extends AtomicBoolean implements Disposable {
        /** */
        private static final long serialVersionUID = -4762798297183704664L;

        @Override
        public void dispose() {
            lazySet(true);
        }

        @Override
        public boolean isDisposed() {
            return get();
        }
    }
}
