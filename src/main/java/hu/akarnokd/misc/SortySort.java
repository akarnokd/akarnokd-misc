package hu.akarnokd.misc;

import java.util.*;

public class SortySort {
    public static String stringSorter(List<String> stringList, String sortingOrder) {
        if ((stringList == null) || (stringList.isEmpty())) {
            return "Given stringList or sortingOrder is empty, null or blank space";
        }
        if ((stringList.size() == 1)) {
            return "The list contains an empty or blank space value";
        }
        if (sortingOrder.equals("asc")) {
            Collections.sort(stringList);
            return stringList.toString();
        }
        if (sortingOrder.equals("desc")) {
            Collections.sort(stringList, Collections.reverseOrder());
            return stringList.toString();
        }
        return " ";
    }

    public static void main(String[] args) {

        List<String> list = new ArrayList<>();
        Scanner scan = new Scanner(System.in);
        while (scan.hasNext()) {
            if (list.size() <= 2) {

                String k = scan.nextLine();
                list.add(k);
            } else {
                break;
            }
        }
        String order = scan.next();

        System.out.println(stringSorter(list, order));

    }
}
