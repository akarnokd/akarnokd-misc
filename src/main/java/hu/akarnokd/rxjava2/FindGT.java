package hu.akarnokd.rxjava2;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import ix.Ix;

public class FindGT {

    public static void main(String[] args) throws Exception {
        StringBuilder b = new StringBuilder();
        Files.walkFileTree(Paths.get("..\\RxJava\\src\\main\\java"), new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file,
                    BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    String s = Ix.from(Files.readAllLines(file)).join("\n").first();

                    int j = 0;
                    for (;;) {
                        int idx = s.indexOf("<code>", j);
                        if (idx < 0) {
                            break;
                        }
                        int jdx = s.indexOf("</code>", idx + 6);

                        int k = idx + 6;
                        for (;;) {
                            int kdx = s.indexOf('>', k);
                            if (kdx < 0) {
                                break;
                            }

                            if (kdx < jdx) {
                                String fn = file.toString().replace(".java", "");
                                fn = fn.replace("\\RxJava\\src\\main\\java\\", "");
                                fn = fn.replace("..", "");
                                fn = fn.replace("\\", ".");
                                b.append("at ")
                                .append(fn)
                                .append(".m(")
                                .append(file.getFileName()).append(":")
                                .append(countLine(s, kdx))
                                .append(")\r\n");
                            } else {
                                break;
                            }
                            k = kdx + 1;
                        }

                        j = jdx + 7;
                    }
                }
                return super.visitFile(file, attrs);
            }
        });
        System.err.println("Found >");
        System.err.println(b);
    }

    static int countLine(String s, int kdx) {
        int c = 1;
        for (int i = kdx; i >= 0; i--) {
            if (s.charAt(i) == '\n') {
                c++;
            }
        }
        return c;
    }
}
