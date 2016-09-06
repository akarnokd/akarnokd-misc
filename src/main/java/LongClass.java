import java.lang.reflect.Method;
import java.util.*;

import io.reactivex.Observable;

public class LongClass {
    public static void main(String[] args) {
        System.out.println("public class ManyMethods {");
        
        Method[] ms = Observable.class.getDeclaredMethods();
        
        Set<String> names = new HashSet<>();
        
        for (int i = 0; i < ms.length; i++) {
            if (names.add(ms[i].getName())) {
                System.out.println("    public static int " + ms[i].getName() + "(int i) { return i + " + i + "; }");
            }
        }
        
        System.out.println("    public static void main(String[] args) {");
        int i = 0;
        for (String s : names) {
            if (i % 3 == 0) {
                System.out.println("        " + s + "(0);");
            }
            i++;
        }
        System.out.println("    }");
        
        System.out.println("}");
    }
}
