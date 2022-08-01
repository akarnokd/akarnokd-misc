package hu.akarnokd.misc;

import java.util.Arrays;
import java.util.Scanner;

public class Scansort {
    public static void insertionSort(int array[]) {
        int arrayLength = array.length;

        for (int i = 1; i < arrayLength; i++) {
            int key = array[i];
            int j = i - 1;

            while (j >= 0 && key < array[j]) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }
    
    public static void main(String args[]) {

        int length = 10; //arraylength
        int k= 0;
        int array[] = new int[length];

        Scanner input = new Scanner(System.in);
        System.out.print("Please write the array elements : ");

        for (k = 0; k < array.length; k++){
            array[k] = input.nextInt();
        }

        insertionSort(array);

        System.out.print ("Sorted array : ");
        System.out.println(Arrays.toString(array));
    }
}
