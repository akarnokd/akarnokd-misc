package hu.akarnokd.misc;

import java.util.Arrays;
import java.util.regex.Pattern;

public class SplitNKeep {

    public static void main(String[] args) {
        String currency = "$"; // from your method

        String escapedCurrency = Pattern.quote(currency);

        String[] result = "$5".split(escapedCurrency, 2);
        result[0] = currency;

        System.out.println(Arrays.toString(result));
    }
}
