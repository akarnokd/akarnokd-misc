package hu.akarnokd.rxjava2;

    import java.util.*;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Predicate;
    
    public class ListGrouping {
    
        static class Group {
            List<String> grouped;
            String groupKey;
        }
    
    
        static boolean groupCE(String t) {
            return "C".equals(t) || "E".equals(t);
        }
    
        static ObservableTransformer<String, Object> group(
                Predicate<? super String> groupCheck) {
            return strings -> 
            Observable.defer(() -> {
                Group gr = new Group();
                return strings
                        .flatMap(t -> {
                            if (gr.grouped != null) {
                                if (!t.equals(gr.groupKey)) {
                                    List<String> g = gr.grouped;
                                    if (groupCheck.test(t)) {
                                        gr.groupKey = t;
                                        gr.grouped = new ArrayList<>();
                                        gr.grouped.add(t);
                                        if (g.size() == 1) {
                                            return Observable.just(g.get(0));
                                        }
                                        return Observable.just(g);
                                    }
                                    gr.groupKey = null;
                                    gr.grouped = null;
                                    if (g.size() == 1) {
                                        return Observable.just(g.get(0), t);
                                    }
                                    return Observable.just(g, t);
                                }
                                gr.grouped.add(t);
                                return Observable.empty();
                            }
                            if (groupCheck.test(t)) {
                                gr.grouped = new ArrayList<>();
                                gr.groupKey = t;
                                gr.grouped.add(t);
                                return Observable.empty();
                            }
                            return Observable.just(t);
                        })
                        .concatWith(Observable.defer(() -> {
                            if (gr.grouped != null) {
                                if (gr.grouped.size() == 1) {
                                    return Observable.just(gr.grouped.get(0));
                                }
                                return Observable.just(gr.grouped);
                            }
                            return Observable.empty();
                        }));
            });
        }
    
        @Test
        public void test() {
            Observable.fromArray("ABCCCDEF".split(""))
            .compose(group(ListGrouping::groupCE))
            .subscribe(System.out::println);
            ;
    
            System.out.println("----");
    
            Observable.fromArray("ABCEFEECCC".split(""))
            .compose(group(ListGrouping::groupCE))
            .subscribe(System.out::println);
            ;
        }
    }
