package hu.akarnokd.theplanetcrafter;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

public class HumbleMap {

    record POI(int x, int z, String name, int color, boolean above) { }

    public static void main(String[] args) throws Throwable {
        BufferedImage bimg = new BufferedImage(4000, 4000, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bimg.createGraphics();

        
        
        List<POI> pois = List.of(
                new POI(171, 149, "Dolomite", 0xFF0000, false),
                new POI(1330, 716, "Dolomite", 0xFF0000, false),
                new POI(-323, -538, "Bauxite", 0x00FF00, false),
                new POI(980, 105, "Bauxite", 0x00FF00, false),
                new POI(1297, -1834, "Uranite", 0x606060, false),
                new POI(441, 1147, "Uranite", 0x606060, false),
                new POI(177, -776, "Sulfur", 0xFFFF00, false),
                new POI(82, 696, "Osmium", 0x0000FF, false),
                new POI(723, 626, "Zeolite", 0xB0B0B0, false),
                new POI(810, -1239, "Obsidian", 0x202020, false),
                new POI(-152, 123, "Landing", 0x000000, false),
                new POI(362, 202, "Base 1", 0x000000, true),
                new POI(-541, 299, "Alloy", 0xFFCC00, false),
                new POI(-1202, 189, "Cobalt", 0x0000CF, false)
                
                /*
                new POI(254, 120, "Dolomite", 0xFF0000, false),
                new POI(-86, -1177, "Bauxite", 0x00FF00, false),
                new POI(-1065, 231, "Uranite", 0x606060, false),
                new POI(177, -776, "Sulfur", 0xFFFF00, false),
                new POI(82, 696, "Osmium", 0x0000FF, false),
                new POI(742, 626, "Zeolite", 0xB0B0B0, false),
                new POI(955, -1404, "Obsidian", 0x202020, false),
                new POI(-152, 123, "Landing", 0x000000, false),
                new POI(362, 202, "Base 1", 0x000000, true)
                */
                /*
                new POI(-86, -1395, "Iridium", 0xFF0000),
                new POI(1979, -946, "Uranium", 0x00FF00),
                new POI(938, 95, "Aluminium", 0x606060),
                new POI(177, -776, "Sulfur", 0xFFFF00),
                new POI(50, 669, "Osmium", 0x0000FF),
                new POI(973, 1679, "Alloy", 0xFFCC00),
                new POI(742, 626, "Zeolite", 0xB0B0B0),
                new POI(881, -1441, "Obsidian", 0x202020),
                new POI(-152, 123, "Landing", 0x000000),
                new POI(362, 202, "Base 1", 0x000000)
                */
        );

        int cx = bimg.getWidth() * 3 / 5;
        int cy = bimg.getHeight() * 4 / 10;
        g2.setColor(Color.white);
        g2.fillRect(0, 0, bimg.getWidth(), bimg.getHeight());

        int spotSize = 75;
        int fontSize = 75;

        g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));

        g2.setStroke(new BasicStroke(3));
        g2.setColor(Color.gray);
        for (int x = -4000; x < 4000; x += 200) {
            g2.drawLine(cx + x, 0, cx + x, 4000);
            g2.drawString("" + (-x), cy + x, cx + 700);
        }
        for (int y = -4000; y < 4000; y += 200) {
            g2.drawLine(0, cy + y, 4000, cy + y);
            g2.drawString("" + (-y), fontSize, cx+ y);
        }

        g2.setStroke(new BasicStroke(6));
        g2.setColor(Color.black);
        g2.drawLine(cy + 0, 0, cy + 0, 4000);
        g2.drawLine(0, cx + 0, 4000, cx + 0);

        for (var poi : pois) {
            g2.setColor(new Color(poi.color));
            g2.fillRect(cy - poi.z - spotSize / 2, cx - poi.x - spotSize / 2 , spotSize, spotSize);

            g2.setColor(Color.black);
            var str = poi.name ;
            var strw = g2.getFontMetrics().stringWidth(str);
            var str2 = "[ " + poi.x + " * " + poi.z + " ]";
            var strw2 = g2.getFontMetrics().stringWidth(str2);

            if (poi.above) {
                g2.setColor(Color.white);
                
                g2.fillRect(cy - poi.z - strw / 2, cx - poi.x - 0 * spotSize / 2 + -3 * fontSize, strw, fontSize);
                g2.fillRect(cy - poi.z - strw2 / 2, cx - poi.x - 0 *  spotSize / 2 + -2 * fontSize, strw2, fontSize);
    
                g2.setColor(Color.black);
                g2.drawString(str, cy - poi.z - strw / 2, cx - poi.x - 0 * spotSize / 2 + -2* fontSize);
                g2.drawString(str2, cy - poi.z - strw2 / 2, cx - poi.x - 0 * spotSize / 2 + -1 * fontSize);
            } else {
                g2.setColor(Color.white);
    
                g2.fillRect(cy - poi.z - strw / 2, cx - poi.x + spotSize / 2 + 0 * fontSize, strw, fontSize);
                g2.fillRect(cy - poi.z - strw2 / 2, cx - poi.x + spotSize / 2 + 0 * fontSize + fontSize, strw2, fontSize);
    
                g2.setColor(Color.black);
                g2.drawString(str, cy - poi.z - strw / 2, cx - poi.x + spotSize / 2 + fontSize);
                g2.drawString(str2, cy - poi.z - strw2 / 2, cx - poi.x + spotSize / 2 + fontSize + fontSize);
            }
        }

        g2.dispose();
        ImageIO.write(bimg, "png", new File("c:\\users\\akarnokd\\downloads\\HumbleMap.png"));
    }
}
