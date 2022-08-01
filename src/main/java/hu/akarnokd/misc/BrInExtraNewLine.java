package hu.akarnokd.misc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BrInExtraNewLine {

    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        in.readLine();
        System.out.println("Enter value:");
        String s = in.readLine();
        System.out.println((int)(s.charAt(0)));
    }
}
