package hu.akarnokd.misc;

import java.util.Base64;

import scala.util.Random;

public class Base64Gen {

    public static void main(String[] args) {
        int size = 250_000;
        
        byte[] data = new byte[size];
        
        new Random().nextBytes(data);
        
        System.out.println(new String(Base64.getEncoder().encode(data)));
    }
}
