package hu.akarnokd.misc;

public class Rotator {

    static final int UP = 0;
    static final int RIGHT = 1;
    static final int DOWN = 2;
    static final int LEFT = 3;

    static int state(int q1, int q2, int q3, int q4) {
        return (q1 << 6) | (q2 << 4) | (q3 << 2) | q4;
    }
    static boolean isDone(int state) {
        return state == 0;
    }

    static int apply(int state, int rotator) {
        int q1 = state >> 6;
        int q2 = (state >> 4) & 3;
        int q3 = (state >> 2) & 3;
        int q4 = (state) & 3;

        switch (rotator) {
        case 0:
            q1 = (q1 + 1) & 3;
            q3 = (q3 + 1) & 3;
            break;
        case 1:
            q2 = (q2 + 1) & 3;
            q4 = (q4 + 1) & 3;
            break;
        case 2:
            q1 = (q1 + 1) & 3;
            q2 = (q2 + 1) & 3;
            break;
        default:
            q3 = (q3 + 1) & 3;
            q4 = (q4 + 1) & 3;
        }
        return state(q1, q2, q3, q4);
    }

    public static void main(String[] args) {
        int initial = state(UP, DOWN, UP, UP);
        for (int i = 0; i <= Integer.MAX_VALUE; i++) {
            if ((i & (1024 * 1024 - 1)) == 0) {
                System.out.println(Integer.toString(i, 2) + " - " + i);
            }
            int s = initial;
            for (int j = 0; j < 32; j += 2) {
                int m = (i >> j) & 3;

                s = apply(s, m);

                if (isDone(s)) {
                    for (int k = 0; k <= j; k++) {
                        System.out.println((i >> k) & 3);
                    }
                    return;
                }

            }
        }
        System.out.println("Not found!");
    }
}
