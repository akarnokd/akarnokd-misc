package hu.akarnokd.rxjava2;

import java.math.BigInteger;

import org.junit.Test;

public class MultiplyDigits {

    @Test
    public void test() {
        BigInteger number = new BigInteger("277777788888899");
        BigInteger ten = new BigInteger("10");

        System.out.println(number);
        
        int j = 1;

        while (number.compareTo(ten) > 0) {
            
            String s = number.toString();
            
            BigInteger newNum = new BigInteger(s.substring(0, 1));
            
            for (int i = 1; i < s.length(); i++) {
                newNum = newNum.multiply(new BigInteger(s.substring(i, i + 1)));
            }
            
            System.out.print(j++);
            System.out.print(": ");
            System.out.println(newNum);
            
            number = newNum;
        }
    }
}
