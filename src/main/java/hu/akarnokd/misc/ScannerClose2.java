package hu.akarnokd.misc;

import java.util.Scanner;

public class ScannerClose2 {
    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String code = "password123";
        String input;
        int i;
        for (i=0; i < 3; i++)
        {
            System.out.print("Enter Nuclear Launch code: ");
            input = scan.nextLine();
            System.out.println("Launch code: " + input);
            boolean passcheck = code.equals(input);
            if (passcheck == true)
            {
                System.out.println("Accepted");
                System.out.println("Missiles away");
                break;
            }
            else if (input.equals("exit"))
            {
                System.out.println("Exiting");
                break;
            }
            else
            {
                System.out.println("Rejected");
            }
        }
        if (i > 2)
        {
            System.out.println("Maximum tries exceeded");
            System.out.println("Exiting");
        }
        scan.close();
    }
}
