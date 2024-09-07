package hu.akarnokd.misc;

public class NPEPuzzle {
    static volatile NPEPuzzle T = new NPEPuzzle();

    int n;

    public static void main(String[] args) {
        while (true) {
            NPEPuzzle a = T, b = T, c = T, d = T;
            a.n = b.n = c.n = d.n;
        }
    }
}
