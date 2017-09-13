import java.util.*;

import org.junit.Test;

import io.reactivex.Observable;
import rx.schedulers.Schedulers;

public class GroupByConcatMapTest {

    @Test
    public void test2() {
        Observable.range(1, 20)
        .groupBy(v -> v % 3)
        .concatMap(g -> g.toList().toObservable())
        .toList()
        .subscribe(System.out::println);
    }

    @Test
    public void test1() {
        rx.Observable.range(1, 20)
        .groupBy(v -> v % 3)
        .concatMap(g -> g.toList())
        .toList()
        .subscribe(System.out::println);
    }
    
    public static class Shop {
        public int id;
        public String name;
        public List<Coordinate> coordinates = new ArrayList<>();

        @Override
        public String toString() {
            String str = id + " (" + name + ") [ ";
            for (Coordinate coordinate : coordinates) {
                str += (coordinate.toString() + " ");
            }
            str += "]";
            return str;
        }
    }

    public static class Coordinate {
        public String latitude;
        public String longitude;

        public Coordinate(String latitude, String longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        public String toString() {
            return "{" + latitude + ", " + longitude + "}";
        }
    }

    public List<String[]> longTimeFunc() {
        return Arrays.asList(
                new String[] {"1", "Foo", "lat1-1", "long1-1"},
                new String[] {"1", "Foo", "lat1-2", "long1-2"},
                new String[] {"2", "Bar", "lat2-1", "long2-1"},
                new String[] {"1", "Foo", "lat1-3", "long1-3"},
                new String[] {"1", "Foo", "lat1-4", "long1-4"},
                new String[] {"3", "Ping", "lat3-1", "long3-1"},
                new String[] {"2", "Bar", "lat2-2", "long2-2"},
                new String[] {"1", "Foo", "lat1-5", "long1-5"},
                new String[] {"2", "Bar", "lat2-3", "long2-3"},
                new String[] {"2", "Bar", "lat2-4", "long2-4"},
                new String[] {"1", "Foo", "lat1-6", "long1-6"},
                new String[] {"1", "Foo", "lat1-7", "long1-7"},
                new String[] {"3", "Ping", "lat3-2", "long3-2"},
                new String[] {"3", "Ping", "lat3-3", "long3-3"},
                new String[] {"2", "Bar", "lat2-5", "long2-5"},
                new String[] {"3", "Ping", "lat3-4", "long3-4"},
                new String[] {"2", "Bar", "lat2-6", "long2-6"},
                new String[] {"3", "Ping", "lat3-5", "long3-5"},
                new String[] {"2", "Bar", "lat2-7", "long2-7"},
                new String[] {"2", "Bar", "lat2-8", "long2-8"},
                new String[] {"3", "Ping", "lat3-6", "long3-6"},
                new String[] {"2", "Bar", "lat2-9", "long2-9"},
                new String[] {"3", "Ping", "lat3-7", "long3-7"},
                new String[] {"2", "Bar", "lat2-10", "long2-10"},
                new String[] {"2", "Bar", "lat2-11", "long2-11"},

                new String[] {"4", "Dev", "lat4-1", "long4-1"},
                new String[] {"4", "Dev", "lat4-2", "long4-2"},
                new String[] {"4", "Dev", "lat4-3", "long4-3"},
                new String[] {"4", "Dev", "lat4-4", "long4-4"},
                new String[] {"4", "Dev", "lat4-5", "long4-5"}
        );
    }
    
    static void sout(Object o, Object o2) {
        System.out.print(o);
        System.out.print(' ');
        System.out.println(o2);
    }
    
    @Test
    public void so() throws Exception {
        rx.Observable
        .defer(() -> rx.Observable.from(longTimeFunc()))
        .doOnNext(strings -> sout("RX_TAG_FIRST", Arrays.toString(strings)))
        .groupBy(rows -> rows[0])
        .doOnNext(groupedObservable -> sout("RX_TAG_TWO", groupedObservable.getKey()))
        .concatMap(group -> group.collect(Shop::new, (shop, rows) -> {
            shop.id = Integer.parseInt(rows[0]);
            shop.name = rows[1];
            shop.coordinates.add(new Coordinate(rows[2], rows[3]));
        }))
        .doOnNext(shop -> sout("RX_TAG_THREE", String.valueOf(shop)))
        .toList()
        .doOnNext(shops -> sout("RX_TAG_FOUR", String.valueOf(shops)))
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.computation())
        .subscribe(shops -> sout("RX_TAG_SUBSCRIBE", String.valueOf(shops)));
        
        Thread.sleep(1000);
    }
}
