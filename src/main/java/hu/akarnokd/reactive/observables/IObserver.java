package hu.akarnokd.reactive.observables;

public interface IObserver<T> {

    void onSubscribe(IDisposable d);
    
    void onNext(T element);
    
    void onError(Throwable cause);
    
    void onComplete();
}
