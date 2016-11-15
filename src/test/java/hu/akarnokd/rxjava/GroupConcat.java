package hu.akarnokd.rxjava;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import rx.Observable;
import rx.observables.GroupedObservable;

public final class GroupConcat {
    private GroupConcat() { }
    static final class AppInfo {
        String name;
        LocalDate date;
        @Override
        public String toString() {
            return name + " @ " + date;
        }
    }
    public static void main(String[] args) {
        System.setProperty("rx.ring-buffer.size", "16");

        List<AppInfo> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 3; j++) {
                AppInfo ai = new AppInfo();
                ai.name = i + " - " + j;
                ai.date = LocalDate.of(2016, 3, i + 1);
                list.add(ai);
            }
        }

        Observable<GroupedObservable<String, AppInfo>> o = Observable.from(list)
        .groupBy(v -> v.date.format(DateTimeFormatter.ofPattern("MM/yyyy")));

        Observable.concat(o)
        .subscribe(System.out::println);
    }
}
