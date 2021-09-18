package hu.akarnokd.rxjava2;

import java.util.*;

import org.junit.Test;

public class SquareSequenceTest {

    static final HashSet<Integer> squares = new HashSet<>();
    static final int[] squaresInt = new int[1024];

    static {
        for (int i = 1; i < 32; i++) {
            squares.add(i * i);
        }

        for (int i = 1; i <= squaresInt.length; i++) {
            squaresInt[i - 1] = i * i;
        }
    }

//    @Test
    public void test() {
        for (int i = 15; i < 30; i++) {
            System.out.println("--- " + i);
            int[] numbers = new int[i];
            checkLevel(numbers, 0);
        }
    }

    static void checkLevel(int[] numbers, int depth) {
        outer:
        for (int i = 1; i <= numbers.length; i++) {
            for (int j = depth - 1; j >= 0; j--) {
                if (numbers[j] == i) {
                    continue outer;
                }
            }
            numbers[depth] = i;
            if (depth < 1) {
                checkLevel(numbers, depth + 1);
            } else {
                int prev = numbers[depth - 1];
                if (squares.contains(prev + i)) {
                    if (depth + 1 == numbers.length) {
                        System.out.println("Found: " + Arrays.toString(numbers));
                    } else {
                        checkLevel(numbers, depth + 1);
                    }
                }
            }
        }
    }

    @Test
    public void singleJoint() {
        for (int i = 1; i < 1024 * 1024; i++) {
            int idx = Arrays.binarySearch(squaresInt, i);
            if (idx < 0) {
                idx = -idx - 1;
            } else {
                idx++;
            }
            //System.out.print(i + "-> ");
            int found = 0;
            for (int j = idx; j < squaresInt.length; j++) {
                int v = squaresInt[j];
                if (v - i < i) {
                    //System.out.print(v +", ");
                    found++;
                }
            }
            if (found < 2) {
                System.out.print(i + "-> ");
                System.out.print("Next square is further away than any previous number");
                System.out.println();
            }
            //System.out.println();
        }
    }
}
