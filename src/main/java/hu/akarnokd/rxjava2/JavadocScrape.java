package hu.akarnokd.rxjava2;

import java.io.*;
import java.nio.file.*;

import com.google.api.client.util.Charsets;

public class JavadocScrape {

    public static void main(String[] args) throws Exception {
        String s = new String(Files.readAllBytes(Paths.get("..\\RxJava\\src\\main\\java\\io\\reactivex\\Flowable.java")), Charsets.UTF_8);
        
        int fidx = 1;
        int j = 0;
        @SuppressWarnings("resource")
        PrintWriter out = new PrintWriter(new FileWriter("javadoc-" + fidx + ".txt"));

        //out.println("<html><head><title>JavaDoc text only</title></head><body>");
        int lineCount = 0;
        //out.println("<textarea rows='10' cols='200'>");
        for (;;) {
            int idx = s.indexOf("/**", j);
            if (idx < 0) {
                break;
            }
            
            int jdx = s.indexOf("*/", idx + 2);
            if (jdx < 0) {
                System.err.println("No end comment tag?!");
                break;
            }
            
            String doc = s.substring(idx + 3, jdx);
            doc = doc.replace("<p>", "");
            doc = doc.replace("</p>", "");
            doc = doc.replace("<br>", "");
            doc = doc.replace("</br>", "");
            doc = doc.replace("<br/>", "");
            doc = doc.replace("<dl>", "");
            doc = doc.replace("</dl>", "");
            doc = doc.replace("<dd>", "");
            doc = doc.replace("</dd>", "");
            doc = doc.replace("<dt>", "");
            doc = doc.replace("</dt>", "");
            doc = doc.replace("<b>", "");
            doc = doc.replace("</b>", "");
            doc = doc.replace("</a>", "");
            doc = doc.replace("<strong>", "");
            doc = doc.replace("</strong>", "");
            doc = doc.replace("<em>", "");
            doc = doc.replace("</em>", "");
            doc = doc.replace("<code>", "");
            doc = doc.replace("</code>", "");
            doc = doc.replace("@param", "Parameter: ");
            doc = doc.replace("@return", "Returns: ");
            doc = doc.replace("@see", "See: ");
            //doc = doc.replace("@link", "Link: ");
            doc = doc.replace("@throws", "Throws: ");

            // extract @code body

            int kk = 0;
            for (;;) {
                int jj = doc.indexOf("{@", kk);
                if (jj < 0) {
                    break;
                }
                int nn = doc.indexOf(" ", jj + 2);
                int mm = doc.indexOf("}", jj + 2);
                
                doc = doc.substring(0, jj) + doc.substring(nn + 1, mm) + doc.substring(mm + 1);

                kk = mm + 1;
            }
            
            for (String line : doc.split("\n")) {
                line = line.trim();
                if (line.startsWith("*")) {
                    line = line.substring(1);
                }
                
                // remove images
                int k = line.indexOf("<img ");
                if (k >= 0) {
                    int m = line.indexOf(">", k + 4);
                    line = line.substring(0, k) + line.substring(m + 1);
                }
                
                // remove anchors
                k = line.indexOf("<a ");
                if (k >= 0) {
                    int m = line.indexOf(">", k + 4);
                    line = line.substring(0, k) + line.substring(m + 1);
                }
                
                line = line.trim();
                if (line.length() != 0) {
                    out.print(line);
                    out.print(' ');
                    lineCount++;
                }
            }
            out.println();
//                out.println("======================================================================");
            out.println();
            j = jdx + 2;
            if (lineCount > 1000) {
                //out.println("</textarea><br/><br/>");
                //out.println("<textarea rows='10' cols='200'>");
                lineCount = 0;
                out.close();
                fidx++;
                out = new PrintWriter(new FileWriter("javadoc-" + fidx + ".txt"));
            }
        }
            //out.println("</textarea></body></html>");
        out.close();
    }
}
