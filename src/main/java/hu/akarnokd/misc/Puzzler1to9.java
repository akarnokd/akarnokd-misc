package hu.akarnokd.misc;

import java.util.Arrays;

public class Puzzler1to9 {
    public static void printAllPermutations(
            int n, int[] elements, char delimiter) {
        if (n == 1) {
            printArray(elements, delimiter);
        } else {
            for (int i = 0; i < n - 1; i++) {
                printAllPermutations(n - 1, elements, delimiter);
                if (n % 2 == 0) {
                    swap(elements, i, n - 1);
                } else {
                    swap(elements, 0, n - 1);
                }
            }
            printAllPermutations(n - 1, elements, delimiter);
        }
    }

    private static void printArray(int[] input, char delimiter) {
        var a = input[0];
        var b = input[1];
        var c = input[2];
        var d = input[3];
        var e = input[4];
        var f = input[5];
        var g = input[6];
        var h = input[7];
        var i = input[8];
        
        if ((a * 10 + b) * c == d * 10 + e && d * 10 + e + f * 10 + g == h * 10 + i) {
            System.out.print(Arrays.toString(input));
            System.out.println();
        }
    }

    private static void swap(int[] input, int a, int b) {
        int tmp = input[a];
        input[a] = input[b];
        input[b] = tmp;
    }

    public static void main(String[] args) {
        int[] input = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
        printAllPermutations(input.length, input, ',');
    }
}
