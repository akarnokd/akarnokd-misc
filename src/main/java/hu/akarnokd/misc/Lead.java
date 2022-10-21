package hu.akarnokd.misc;

import java.util.Scanner;

public class Lead {
    public static void main (String[] args)
    {
        Scanner sc = new Scanner(System.in);

        Boolean checker=true;
        int lead1=0,lead2=0,lead = 0;
        int rounds = sc.nextInt();
        for(int i=1;i<=rounds;i++){
            int x = sc.nextInt();
            int y = sc.nextInt();
            if(x>y){
                lead1 = x-y;
                if(lead1>=lead){
                    checker=true;
                    lead=lead1;
                }
            }
            else if(y>x){
                lead2 = y-x;
                if(lead2>=lead){
                    checker=false;
                    lead=lead2;
                }
            }
        }
        sc.close();
        if(checker==true){
            System.out.println(1+" "+lead);
        }
        else if(checker==false){
            System.out.println(2+" "+lead);
        }
    }
}
