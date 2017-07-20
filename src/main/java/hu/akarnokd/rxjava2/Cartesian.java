package hu.akarnokd.rxjava2;

    import java.util.*;
    
    import io.reactivex.Observable;
    
    public class Cartesian {
    
        static Observable<int[]> cartesian(Observable<Observable<Integer>> sources) {
            return sources.toList().flatMapObservable(list -> cartesian(list));
        }
        
        static Observable<int[]> cartesian(List<Observable<Integer>> sources) {
            if (sources.size() == 0) {
                return Observable.<int[]>empty();
            }
            Observable<int[]> main = sources.get(0).map(v -> new int[] { v });
            
            for (int i = 1; i < sources.size(); i++) {
                int j = i;
                Observable<Integer> o = sources.get(i).cache();
                main = main.flatMap(v -> {
                    return o.map(w -> {
                        int[] arr = Arrays.copyOf(v, j + 1);
                        arr[j] = w;
                        return arr;
                    });
                });
            }
            
            return main;
        }
        
        public static void main(String[] args) {
            cartesian(Observable.just(Observable.just(0, 1), Observable.just(2, 3), Observable.just(4, 5)))
            .subscribe(v -> System.out.println(Arrays.toString(v)));
        }
    }
