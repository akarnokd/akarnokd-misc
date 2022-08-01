package hu.akarnokd.misc;

import java.util.Arrays;

public class BubblyVowels {
    public static String[] sortByVowels(String... a) {
        String[] copy = a.clone();

        int[] numVowels = new int[copy.length];

        for(int i = 0; i < copy.length; i++) {
            int count = 0;
            for(int j = 0; j < copy[i].length(); j++) {
                char ch = copy[i].charAt(j);
                if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
                count++;
                }
            }
            numVowels[i] = count;
        }
        for (int c = 0; c < copy.length - 1; c++) {
            for(int d = 0; d < copy.length - c - 1; d++) {
                if (numVowels[d] > numVowels[d+1]) {
                    int swapInt = numVowels[d];
                    numVowels[d] = numVowels[d+1];
                    numVowels[d+1] = swapInt;
                    
                    String swapString = copy[d];
                    copy[d] = copy[d+1];
                    copy[d+1] = swapString;
                }
            }
        }
        return copy;
    }
    
    public static void main(String[] args) {
        var r = sortByVowels("aaa", "aa", "a");
        System.out.println(Arrays.toString(r));
    }
}
