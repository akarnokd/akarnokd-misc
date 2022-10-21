package hu.akarnokd.misc;

import java.util.Scanner;

public class ScannerNext {

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        
        try {
            System.out.println(scan.nextInt());
        } catch (Exception ex) {
            System.out.println("Wrong input");
            System.out.println(scan.next());
        }
    }
}
