package hu.akarnokd.misc;

import java.math.BigInteger;

import org.apache.commons.math3.primes.Primes;

public class PrimeCounter {
    public static int countPrimes(int n) {

        int count = 1;

        if (n == 0 || n == 1 || n == 2)
            return 0;
        else
            for (int i = 3; i < n; i++) {

                for (int j = 2; j < i; j++) {
                    if (i % j == 0) {
                        break;
                    } else if (j == i - 1 && i % j != 0) {
                        count++;
                    }
                }
            }
        return count;

    }

    static int countPrimes2(int i) {
        int count = 0;
        for (int k = 2; k < i; k++) {
            if (Primes.isPrime(k)) {
                count++;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        for (int i = 1; i < 30000; i++) {
            int c1 = countPrimes(i);
            int c2 = countPrimes2(i);
            if (c1 != c2) {
                System.out.printf("%d -> %d ~ %d%n", i, c1, c2);
            }
            if (i % 100 == 0) {
                System.out.println(i);
            }
        }
    }
}
