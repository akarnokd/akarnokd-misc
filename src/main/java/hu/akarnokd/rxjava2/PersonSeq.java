package hu.akarnokd.rxjava2;

import java.util.*;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class PersonSeq {

    public static class Person {
        private String name = null;
        private String address = null;
        private int age;

        private Optional<String> optName= null;
        private Optional<Integer> optAge= null;
        private Optional<String> optAddress = null;

        public Person(String name, Integer age, String address) {
            this.optName = Optional.ofNullable(name);
            this.optAge = Optional.ofNullable(age);
            this.optAddress = Optional.ofNullable(address);
        }

        public Optional<String> getName() {
            return optName;
        }

        public void setName(String name) {
            this.optName = Optional.ofNullable(name);
        }

        public Optional<String> getAddress() {
            return this.optAddress;
        }

        public void setAddress(String address) {
            this.optAddress = Optional.ofNullable(address);
        }

        public Optional<Integer> getAge() {
            return this.optAge;
        }

        public void setAge(int age) {
            this.optAge = Optional.ofNullable(age);
        }
    }

    private static List<List<Person>> getPersons() {
        return Arrays.asList(
                Arrays.asList(new Person("Sanna1", 59, "EGY"), new Person(null, 59, "EGY"), new Person("Sanna3", 59, null)),
                Arrays.asList(new Person("Mohamed1", 59, "EGY"), new Person(null, 59, "EGY")),
                Arrays.asList(new Person("Ahmed1", 44, "QTR"), new Person("Ahmed2", 44, "QTR"), new Person(null, null, "QTR")),
                Arrays.asList(new Person("Fatma", 29, "KSA")),
                Arrays.asList(new Person("Lobna", 24, "EGY")));
    }
    public static void main(String[] args) {
        Observable<List<Person>> observables = 
                Observable.fromIterable(getPersons());
                observables
                //.subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .concatMap(list->Observable.fromIterable(list)
                        .map(p->p.getName()
                                .map(r->r.toUpperCase()).orElse("NULL_VALUE")))
                .observeOn(Schedulers.io())
                .blockingSubscribe(new Observer<String>() {

                    @Override
                    public void onComplete() {
                        // TODO Auto-generated method stub
                        System.out.println("onComplete: ");
                    }

                    @Override
                    public void onError(Throwable arg0) {
                        // TODO Auto-generated method stub
                        System.out.println("onError: ");
                    }

                    @Override
                    public void onNext(String arg0) {
                        // TODO Auto-generated method stub
                        System.out.println("onNext: ");
                    }

                    @Override
                    public void onSubscribe(Disposable arg0) {
                        // TODO Auto-generated method stub
                        System.out.println("onSubscribe: ");
                    }
                });
            }
}
