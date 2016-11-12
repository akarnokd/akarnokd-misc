package hu.akarnokd.rxjava;
public class Example {
    static String str;

    public static void main(String[] args) {
        str = "aaa";

        String local = str;

        str = "ggg";

        System.out.println(local);
        System.out.println(local);
        System.out.println(local);
    }
}