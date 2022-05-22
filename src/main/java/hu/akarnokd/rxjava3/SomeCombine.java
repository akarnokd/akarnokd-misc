package hu.akarnokd.rxjava3;

import io.reactivex.rxjava3.core.*;
import java.util.*;
import java.util.stream.Stream;

public class SomeCombine {

    public static void main(String[] args) {
        record Item(int id, boolean added, boolean deleted) { }

        Single<List<Item>> remoteListSource = Single.just(List.of(
                new Item(0, false, false),
                new Item(1, false, false),
                new Item(2, false, true),
                new Item(3, true, false),
                new Item(4, true, false)
        ));
        Single<List<Item>> localListSource = Single.just(List.of(
                new Item(0, false, false),
                new Item(1, true, false),
                new Item(2, false, false),
                new Item(3, false, false)
        ));

        Single.zip(remoteListSource, localListSource, (remoteList, localList) -> {

            Set<Integer> deleted = new HashSet<>();
            remoteList.stream()
                .filter(item -> item.deleted)
                .forEach(item -> deleted.add(item.id));
            localList.stream()
                .filter(item -> item.deleted)
                .forEach(item -> deleted.add(item.id));

            Set<Integer> duplicate = new HashSet<>();
            List<Item> result = new ArrayList<>();

            Stream.concat(remoteList.stream(), localList.stream())
            .filter(item -> !deleted.contains(item.id))
            .forEach(item -> {
                 if (duplicate.contains(item.id)) {
                     if (item.added) {
                         item = new Item(Collections.max(duplicate) + 1, true, false);
                         duplicate.add(item.id);
                         result.add(item);
                     }
                 } else {
                     duplicate.add(item.id);
                     result.add(item);
                 }
            });

            return result;
        })
        .flattenAsFlowable(v -> v)
        .subscribe(System.out::println);

    }
}
