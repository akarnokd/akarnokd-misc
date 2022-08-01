package hu.akarnokd.misc;

import java.util.Scanner;

public class LongestEventSub {
    public static void main(String[] args)
    {
        Scanner sc=new Scanner(System.in);
          int t=sc.nextInt();
          int j=0;
          while(t-->0)
          {
              int n=sc.nextInt();
              int count=0;
              int[] arr=new int[n];
              for(int i=0;i<n;i++)
              {
                  arr[i]=sc.nextInt();
                  
              }
              for(int k=1;k<n;k++)
              {
                  j=k-1;
                  if(arr[j]%2==0 && arr[k]%2==0)
                  {
                      count++;
                  }
                  else
                  {
                      j++;
                      
                  }
              }
              System.out.println(count+1);
            }
    }
}
