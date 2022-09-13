package hu.akarnokd.fallout76.old;

import java.awt.*;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;

import javax.swing.*;

public class EditorIDFinder {

    public static void main(String[] args) throws Exception {
        File file = new File(
                "c:\\Program Files (x86)\\Bethesda.net Launcher\\games\\Fallout76\\Data\\SeventySix.esm");

        Set<String> editorIds = new HashSet<>();
        /*
         * char[4] = 'EDID'
         * uint16 = length
         * uint8[length] = characters
         */
        int i = 1;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {

            MappedByteBuffer mbb = raf.getChannel().map(MapMode.READ_ONLY, 0, raf.length());

            while (mbb.remaining() > 0) {
                if (mbb.get() == 'E') {
                    if (mbb.get() == 'D') {
                        if (mbb.get() == 'I') {
                            if (mbb.get() == 'D') {
                                int len = Short.reverseBytes(mbb.getShort()) & 0xFFFF;
                                byte[] data = new byte[len];
                                mbb.get(data);
                                editorIds.add(new String(data, StandardCharsets.ISO_8859_1));
                                if (i % 100 == 0) {
                                    System.out.println("Records so far: " + i);
                                }
                                i++;
                            } else {
                                mbb.position(mbb.position() - 3);
                            }
                        } else {
                            mbb.position(mbb.position() - 2);
                        }
                    } else {
                        mbb.position(mbb.position() - 1);
                    }
                }
            }
        }

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.setTitle("Find Editor IDs");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1000, 800);

            frame.setLocationRelativeTo(null);

            JTextField search = new JTextField();
            frame.add(search, BorderLayout.PAGE_START);
            Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 22);
            search.setFont(f);

            JTextPane text = new JTextPane();
            JScrollPane scroll = new JScrollPane(text);
            frame.add(scroll, BorderLayout.CENTER);
            f = new Font(Font.MONOSPACED, Font.PLAIN, 22);
            text.setFont(f);

            frame.setVisible(true);

            search.addActionListener(e -> {
                text.setText("");

                String find = search.getText().trim().toLowerCase();
                String[] ands = find.split("\\s+");

                List<String> found = new ArrayList<>();
                if (find.length() > 0) {
                    for (String s : editorIds) {
                        boolean has = true;
                        for (String a : ands) {
                            if (!s.toLowerCase().contains(a)) {
                                has = false;
                                break;
                            }
                        }
                        if (has) {
                            found.add(s);
                        }
                    }

                    if (found.size() == 0) {
                        text.setText("<not found>");
                    } else {
                        Collections.sort(found, String.CASE_INSENSITIVE_ORDER);
                        text.setText(String.join("\r\n", found));
                        text.setCaretPosition(0);
                    }
                }
            });
        });
    }
}
