package hu.akarnokd.misc;

import java.util.*;

public class DigitSwapper {

    public static void main(String[] args) {
        List<Number> results = new ArrayList<>();

        for (long j = 11; j < 16_000_000_000L; j++) {
            long lastDigit = j % 10;
            long shiftedValue = j / 10;
            long remainder = shiftedValue;
            while (remainder > 9) {
                remainder /= 10;
                lastDigit *= 10;
            }
            lastDigit *= 10;

            long newValue = lastDigit + shiftedValue;

            if (j * 2 == newValue) {
                System.out.println(j);
                results.add(j);
            }
            if (j % 10_000_000 == 0) {
                System.out.println(">> " + j);
            }
        }
        System.out.println("---------");
        results.forEach(System.out::println);
    }

}
