package hu.akarnokd.misc;

public class RecordVarargs {

    public static void main(String[] args) {
        record Strings(String... str) { }
        
        System.out.println(new Strings("A", "B"));
    }
}
