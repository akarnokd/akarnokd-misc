package hu.akarnokd.misc;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SimpleDateAdd {

    public static void main(String[] args) {
        String pickupdate = "06-30-2022";
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");

        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(pickupdate));
        } catch (ParseException e){
            e.printStackTrace();
        }

        c.add(Calendar.DAY_OF_MONTH, 3);
        String expirydate= sdf.format(c.getTime());

        System.out.println(expirydate);
    }
}
