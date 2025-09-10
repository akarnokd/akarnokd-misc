package hu.akarnokd.rxjava3;

import io.reactivex.rxjava3.core.Flowable;

public class GroupByAbandon {

    public static void main(String[] args) {
var eventDTOFlowable = Flowable.just(
    "item1",
    "item2",
    "item3",
    "item4",
    "item5",
    "item6"
);

var groupedFlowables = eventDTOFlowable
        .groupBy(x -> x)
        .flatMapSingle(g -> g.toList())
        .toList()
        .blockingGet()
;
System.out.println(groupedFlowables);
    }
}
