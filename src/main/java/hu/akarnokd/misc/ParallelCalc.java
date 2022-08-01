package hu.akarnokd.misc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

public class ParallelCalc {

    public static void main(String[] args) {
        String[] str = { "1", "2" };
        Iterable<String[]> iterb = List.of(str, str, str, str, str); 
        Iterator<String[]> iter = iterb.iterator(); 
        Spliterator<String[]> spliterator = Spliterators.spliteratorUnknownSize(iter, 0);
        
        enum NotValidRowReason {
            NOT_VALID_EMAIL, EMPTY_NAME
        }

        record NotValidRow(String key, String value, NotValidRowReason reason) { }
        
        Pattern SKIP_PATTERN = Pattern.compile("\\.");
        var badQueue = new ConcurrentLinkedQueue<NotValidRow>();
        
        StreamSupport.stream(spliterator, true)
        .filter(tmp -> tmp.length != 2)
        .peek(tmp -> {
            if (tmp[0] == null || !SKIP_PATTERN.matcher(tmp[0]).matches()) {
                badQueue.add(new NotValidRow(tmp[0], tmp[1], NotValidRowReason.NOT_VALID_EMAIL));
            }
            if(tmp[1]==null || tmp[1].isBlank()){
                badQueue.add(new NotValidRow(tmp[0],tmp[1],NotValidRowReason.EMPTY_NAME));
            }
        })
        .toList();
        
        List<NotValidRow> badList = new ArrayList<>(badQueue);
    }
}
