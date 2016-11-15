/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hu.akarnokd.rxjava;

import java.util.Comparator;

import rx.Observable;
import rx.functions.Func1;

public final class MathObservable<T> {

    private final Observable<T> o;

    private MathObservable(Observable<T> o) {
        this.o = o;
    }

    public static <T> MathObservable<T> from(Observable<T> o) {
        return new MathObservable<>(o);
    }

    /**
     * Returns an Observable that emits the single item emitted by the source Observable with the maximum
     * numeric value. If there is more than one item with the same maximum value, it emits the last-emitted of
     * these.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/max.png" alt="">
     *
     * @param <T> the self-comparable type
     * @param source
     *            an Observable to scan for the maximum emitted item
     * @return an Observable that emits this maximum item
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-max">RxJava Wiki: max()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211837.aspx">MSDN: Observable.Max</a>
     */
    public static <T extends Comparable<? super T>> Observable<T> max(Observable<T> source) {
        return OperatorMinMax.max(source);
    }

    /**
     * Returns an Observable that emits the single numerically minimum item emitted by the source Observable.
     * If there is more than one such item, it returns the last-emitted one.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/min.png" alt="">
     *
     * @param <T> the self-comparable type
     * @param source
     *            an Observable to determine the minimum item of
     * @return an Observable that emits the minimum item emitted by the source Observable
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229715.aspx">MSDN: Observable.Min</a>
     */
    public static <T extends Comparable<? super T>> Observable<T> min(Observable<T> source) {
        return OperatorMinMax.min(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Doubles emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.png" alt="">
     *
     * @param source
     *            the source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Doubles emitted by the source
     *         Observable
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Double> sumDouble(Observable<Double> source) {
        return OperatorSum.sumDoubles(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Floats emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.png" alt="">
     *
     * @param source
     *            the source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Floats emitted by the source
     *         Observable
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Float> sumFloat(Observable<Float> source) {
        return OperatorSum.sumFloats(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Integers emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.png" alt="">
     *
     * @param source
     *            source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Integers emitted by the source
     *         Observable
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Integer> sumInteger(Observable<Integer> source) {
        return OperatorSum.sumIntegers(source);
    }

    /**
     * Returns an Observable that emits the sum of all the Longs emitted by the source Observable.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.png" alt="">
     *
     * @param source
     *            source Observable to compute the sum of
     * @return an Observable that emits a single item: the sum of all the Longs emitted by the
     *         source Observable
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public static Observable<Long> sumLong(Observable<Long> source) {
        return OperatorSum.sumLongs(source);
    }

    /**
     * Returns an Observable that emits the maximum item emitted by the source Observable, according to the
     * specified comparator. If there is more than one item with the same maximum value, it emits the
     * last-emitted of these.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/max.png" alt="">
     *
     * @param comparator
     *            the comparer used to compare items
     * @return an Observable that emits the maximum item emitted by the source Observable, according to the
     *         specified comparator
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-max">RxJava Wiki: max()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh211635.aspx">MSDN: Observable.Max</a>
     */
    public Observable<T> max(Comparator<? super T> comparator) {
        return OperatorMinMax.max(o, comparator);
    }

    /**
     * Returns an Observable that emits the minimum item emitted by the source Observable, according to a
     * specified comparator. If there is more than one such item, it returns the last-emitted one.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/min.png" alt="">
     *
     * @param comparator
     *            the comparer used to compare elements
     * @return an Observable that emits the minimum item emitted by the source Observable according to the
     *         specified comparator
     * @throws IllegalArgumentException
     *             if the source is empty
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-min">RxJava Wiki: min()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/hh229095.aspx">MSDN: Observable.Min</a>
     */
    public Observable<T> min(Comparator<? super T> comparator) {
        return OperatorMinMax.min(o, comparator);
    }

    /**
     * Returns an Observable that extracts a Double from each of the items emitted by the source Observable via
     * a function you specify, and then emits the sum of these Doubles.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.f.png" alt="">
     *
     * @param valueExtractor
     *            the function to extract a Double from each item emitted by the source Observable
     * @return an Observable that emits the Double sum of the Double values corresponding to the items emitted
     *         by the source Observable as transformed by the provided function
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumDouble()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Double> sumDouble(Func1<? super T, Double> valueExtractor) {
        return OperatorSum.sumAtLeastOneDoubles(o.map(valueExtractor));
    }

    /**
     * Returns an Observable that extracts a Float from each of the items emitted by the source Observable via
     * a function you specify, and then emits the sum of these Floats.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.f.png" alt="">
     *
     * @param valueExtractor
     *            the function to extract a Float from each item emitted by the source Observable
     * @return an Observable that emits the Float sum of the Float values corresponding to the items emitted by
     *         the source Observable as transformed by the provided function
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumFloat()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Float> sumFloat(Func1<? super T, Float> valueExtractor) {
        return OperatorSum.sumAtLeastOneFloats(o.map(valueExtractor));
    }

    /**
     * Returns an Observable that extracts an Integer from each of the items emitted by the source Observable
     * via a function you specify, and then emits the sum of these Integers.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.f.png" alt="">
     *
     * @param valueExtractor
     *            the function to extract an Integer from each item emitted by the source Observable
     * @return an Observable that emits the Integer sum of the Integer values corresponding to the items emitted
     *         by the source Observable as transformed by the provided function
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumInteger()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Integer> sumInteger(Func1<? super T, Integer> valueExtractor) {
        return OperatorSum.sumAtLeastOneIntegers(o.map(valueExtractor));
    }

    /**
     * Returns an Observable that extracts a Long from each of the items emitted by the source Observable via a
     * function you specify, and then emits the sum of these Longs.
     * <p>
     * <img width="640" src="https://raw.github.com/wiki/ReactiveX/RxJava/images/rx-operators/sum.f.png" alt="">
     *
     * @param valueExtractor
     *            the function to extract a Long from each item emitted by the source Observable
     * @return an Observable that emits the Long sum of the Long values corresponding to the items emitted by
     *         the source Observable as transformed by the provided function
     * @see <a href="https://github.com/ReactiveX/RxJava/wiki/Mathematical-and-Aggregate-Operators#wiki-suminteger-sumlong-sumfloat-and-sumdouble">RxJava Wiki: sumLong()</a>
     * @see <a href="http://msdn.microsoft.com/en-us/library/system.reactive.linq.observable.sum.aspx">MSDN: Observable.Sum</a>
     */
    public Observable<Long> sumLong(Func1<? super T, Long> valueExtractor) {
        return OperatorSum.sumAtLeastOneLongs(o.map(valueExtractor));
    }
}
