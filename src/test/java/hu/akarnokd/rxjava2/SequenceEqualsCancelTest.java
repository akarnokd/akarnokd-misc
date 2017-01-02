package hu.akarnokd.rxjava2;

import org.junit.*;

import hu.akarnokd.rxjava2.subjects.*;
import io.reactivex.*;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.subjects.PublishSubject;

public class SequenceEqualsCancelTest {

    @Test
    public void flowable() {
        PublishProcessor<Integer> pp1 = PublishProcessor.create();
        PublishProcessor<Integer> pp2 = PublishProcessor.create();
        
        Flowable.sequenceEqual(pp1, pp2)
        .test()
        .cancel();

        Assert.assertFalse(pp1.hasSubscribers());
        Assert.assertFalse(pp2.hasSubscribers());
    }

    @Test
    public void observable() {
        PublishSubject<Integer> pp1 = PublishSubject.create();
        PublishSubject<Integer> pp2 = PublishSubject.create();
        
        Observable.sequenceEqual(pp1, pp2)
        .test()
        .cancel();

        Assert.assertFalse(pp1.hasObservers());
        Assert.assertFalse(pp2.hasObservers());
    }

    @Test
    public void single() {
        SingleSubject<Integer> pp1 = SingleSubject.create();
        SingleSubject<Integer> pp2 = SingleSubject.create();
        
        Single.equals(pp1, pp2)
        .test()
        .cancel();

        Assert.assertFalse(pp1.hasObservers());
        Assert.assertFalse(pp2.hasObservers());
    }

    @Test
    public void maybe() {
        MaybeSubject<Integer> pp1 = MaybeSubject.create();
        MaybeSubject<Integer> pp2 = MaybeSubject.create();
        
        MaybeSubject.sequenceEqual(pp1, pp2)
        .test()
        .cancel();

        Assert.assertFalse(pp1.hasObservers());
        Assert.assertFalse(pp2.hasObservers());
    }

//    @Test
//    public void completable() {
//        CompletableSubject pp1 = CompletableSubject.create();
//        CompletableSubject pp2 = CompletableSubject.create();
//        
//        CompletableSubject.equals(pp1, pp2)
//        .test()
//        .cancel();
//
//        Assert.assertFalse(pp1.hasObservers());
//        Assert.assertFalse(pp2.hasObservers());
//    }
    

//    @Test
//    public void solo() {
//        SoloProcessor<Integer> pp1 = SoloProcessor.create();
//        SoloProcessor<Integer> pp2 = SoloProcessor.create();
//        
//        SoloProcessor.equals(pp1, pp2)
//        .test()
//        .cancel();
//
//        Assert.assertFalse(pp1.hasSubscribers());
//        Assert.assertFalse(pp2.hasSubscribers());
//    }

//    @Test
//    public void perhaps() {
//        PerhapsProcessor<Integer> pp1 = PerhapsProcessor.create();
//        PerhapsProcessor<Integer> pp2 = PerhapsProcessor.create();
//        
//        PerhapsProcessor.equals(pp1, pp2)
//        .test()
//        .cancel();
//
//        Assert.assertFalse(pp1.hasSubscribers());
//        Assert.assertFalse(pp2.hasSubscribers());
//    }


//    @Test
//    public void nono() {
//        NonoProcessor pp1 = NonoProcessor.create();
//        NonoProcessor pp2 = NonoProcessor.create();
//        
//        NonoProcessor.equals(pp1, pp2)
//        .test()
//        .cancel();
//
//        Assert.assertFalse(pp1.hasSubscribers());
//        Assert.assertFalse(pp2.hasSubscribers());
//    }

}
