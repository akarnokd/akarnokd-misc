package hu.akarnokd.misc;

import org.apache.commons.codec.binary.Hex;

public class CP037 {

    public static void main(String[] args) throws Exception {
        String hexstring =  Integer.toHexString((short) 5565).trim();
        System.out.println(hexstring);
        byte[] bytes = Hex.decodeHex(hexstring);
        String result = new String(bytes,"CP037");
        System.out.println(result);
    }
}
