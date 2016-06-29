package hu.akarnokd.rxreactor;

import java.lang.reflect.*;
import java.util.*;

import reactor.core.publisher.Flux;
import rx.Observable;

public class RxReactorDiff {

    static void dumpClass(Class<?> cl) {
        
        Method[] m = cl.getMethods();
        
        Comparator<Method> c = (a, b) -> {
            int d = Modifier.isStatic(a.getModifiers()) ? 0 : 1;
            int e = Modifier.isStatic(b.getModifiers()) ? 0 : 1;
            int f = Integer.compare(d, e);
            if (f == 0) {
                f = a.getName().compareTo(b.getName());
                if (f == 0) {
                    f = a.toString().compareTo(b.toString());
                }
            }
            return f;
        };
        
        Arrays.sort(m, c);

        int count = 0;

        for (Method a : m) {
            if (a.getDeclaringClass() == cl) {
                if (Modifier.isStatic(a.getModifiers())) {
                    System.out.print("static ");
                }
                String s = a.toString();
                String str = "rx.Observable.";
                int i = s.indexOf(str);
                if (i >= 0) {
                    s = s.substring(i + str.length());
                }
                str = "reactor.core.publisher.Flux.";
                i = s.indexOf(str);
                if (i >= 0) {
                    s = s.substring(i + str.length());
                }
                
                s = s.replaceAll("java\\.util\\.concurrent\\.", "");
                s = s.replaceAll("java\\.util\\.concurrent\\.", "");
                
                System.out.println(s);
                count++;
            }
        }
        
        System.out.println("---");
        System.out.println(count);
    }
    
    public static void main(String[] args) {
        dumpClass(Observable.class);
        System.out.println("--");
        dumpClass(Flux.class);
    }
}
