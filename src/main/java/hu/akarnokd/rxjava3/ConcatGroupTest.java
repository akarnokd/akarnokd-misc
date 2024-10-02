package hu.akarnokd.rxjava3;

import java.util.ArrayList;

import java.util.*;

import io.reactivex.rxjava3.core.Observable;

public class ConcatGroupTest {

    static class Employee {

        public Integer departmentId;

        public String name;

        public Employee(Integer departmentId, String name) {
            this.departmentId = departmentId;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Employee{" + "departmentId=" + departmentId + ", name='" + name + '\'' + '}';
        }

    }

    public static void main(String[] args) {
        List<Employee> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            list.add(new Employee(i % 5, String.valueOf(i)));
        }
        Observable<Employee> values = Observable.fromIterable(list);
        Observable.concat(values.groupBy(e -> e.departmentId))
                .subscribe(System.out::println);
        
        values.groupBy(e -> e.departmentId)
        .flatMapSingle(v -> v.toList())
        .concatMapIterable(v -> v)
        .subscribe(System.out::println);

        Observable.<String>create(e -> { });

        //Observable<String>.create(e -> { });

    }

}