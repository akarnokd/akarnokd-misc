package hu.akarnokd.rxjava;

import java.lang.reflect.Method;
import java.util.*;

import com.google.common.collect.*;

import it.unimi.dsi.fastutil.objects.*;

public final class MethodDiff {

    private MethodDiff() { }

    static String rename(String s) {
        if ("dooncompleted".equals(s)) {
            return "dooncomplete";
        }
        if ("doonunsubscribe".equals(s)) {
            return "dooncancel";
        }
        if ("extend".equals(s)) {
            return "to";
        }
        return s;
    }

    static String replacepackage(String s) {
        return s.replace("rx.Observable", "Rx1")
                .replace("io.reactivex.Flowable", "Rx2")
                .replace("org.reactivestreams.Publisher", "Orp");
    }

    public static void main(String[] args) {
        Object2IntMap<String> rx1 = new Object2IntOpenHashMap<>();
        Multimap<String, Method> rx1m = HashMultimap.create();
        {
            Method[] ms = rx.Observable.class.getMethods();

            for (Method m : ms) {
                if (!m.isAnnotationPresent(Deprecated.class)) {
                    String s = rename(m.getName().toLowerCase());
                    int n = rx1.getInt(s);
                    rx1.put(s, n + 1);
                    rx1m.put(s, m);
                }
            }
        }

        Object2IntMap<String> rx2 = new Object2IntOpenHashMap<>();
        Multimap<String, Method> rx2m = HashMultimap.create();
        {
            Method[] ms = io.reactivex.Flowable.class.getMethods();

            for (Method m : ms) {
                if (!m.isAnnotationPresent(Deprecated.class)) {
                    String s = m.getName().toLowerCase();
                    int n = rx2.getInt(s);
                    rx2.put(s, n + 1);
                    rx2m.put(s, m);
                }
            }
        }

        List<String> methods = new ArrayList<>(rx1.keySet());
        Collections.sort(methods);

        Comparator<Method> comp = (a, b) -> {
            int c = Integer.compare(a.getParameterCount(), b.getParameterCount());
            if (c == 0) {
                return a.toGenericString().compareToIgnoreCase(b.toGenericString());
            }
            return c;
        };

        for (String e : methods) {

            Integer old = rx1.getOrDefault(e, -1);
            Integer overloads = rx2.getOrDefault(e, -1);

            if (overloads == -1) {
                System.out.print(e);
                System.out.print("(");
                System.out.print(old);
                System.out.println(")");
                List<Method> c1 = new ArrayList<>(rx1m.get(e));

                Collections.sort(c1, comp);
                for (Method m : c1) {
                    System.out.print("    ");
                    System.out.println(replacepackage(m.toGenericString()));
                }
                System.out.println();
            } else
            if (overloads < old) {

                System.out.print(e);
                System.out.print("  ");
                System.out.print(overloads);
                System.out.print("/");
                System.out.println(old);
                List<Method> c1 = new ArrayList<>(rx1m.get(e));

                Collections.sort(c1, comp);
                for (Method m : c1) {
                    System.out.print("    ");
                    System.out.println(replacepackage(m.toGenericString()));
                }
                System.out.println("    ----");

                List<Method> c2 = new ArrayList<>(rx2m.get(e));
                Collections.sort(c2, comp);
                for (Method m : c2) {
                    System.out.print("    ");
                    System.out.println(replacepackage(m.toGenericString()));
                }
                System.out.println();
            }
        }
    }
}
