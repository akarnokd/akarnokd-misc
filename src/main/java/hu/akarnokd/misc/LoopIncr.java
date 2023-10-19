package hu.akarnokd.misc;

import java.util.*;

public class LoopIncr {
    public static void main(String[] args) {
        var list = List.of("A", "B", "C", "D");
        var map = new HashMap<String, String>();
        
        for (int i = 0; i < list.size(); i = i++) {
            map.put(list.get(i), list.get(++i));
            i++;
        }
        
        System.out.println(map);
    }
}
