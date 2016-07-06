package hu.akarnokd.rxjava2;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.*;

public final class CharSequenceObservable extends Observable<Integer> {

    final CharSequence string;
    
    public CharSequenceObservable(CharSequence string) {
        this.string = string;
    }

    @Override
    protected void subscribeActual(Observer<? super Integer> observer) {
        Disposable d = Disposables.empty();
        
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
}
