package hu.akarnokd.fallout76;

import java.awt.*;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.util.*;

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
                            }
                        }
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
            frame.add(text, BorderLayout.CENTER);
            f = new Font(Font.MONOSPACED, Font.PLAIN, 22);
            text.setFont(f);
            
            frame.setVisible(true);
            
            search.addActionListener(e -> {
                text.setText("");
                
                String find = search.getText().trim().toLowerCase();
                
                StringBuilder sb = new StringBuilder();
                if (find.length() > 0) {
                    for (String s : editorIds) {
                        if (s.toLowerCase().contains(find)) {
                            if (sb.length() > 0) {
                                sb.append("\r\n");
                            }
                            sb.append(s);
                        }
                    }
                    
                    if (sb.length() == 0) {
                        sb.append("<not found>");
                    }
                    
                    text.setText(sb.toString());
                }
            });
        });
    }
}
