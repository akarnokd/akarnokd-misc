package hu.akarnokd.theplanetcrafter;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.io.File;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class LarvaeMap {

    record Area(String name, double x, double y, double z, double w, double h, double d, String... larvae) { }
    
    public static void main(String[] args) throws Throwable {
        var img = ImageIO.read(new File("c:/users/akarnokd/git/ThePlanetCrafterMods/CheatMinimap/map_lush.png"));
        
        var spawns = List.<Area>of(
                new Area("Bassins", 631.6, 66.9, 1562.5, 215.9, 56.7, 265.3, "Butterfly11Larvae"),
                new Area("Mont", 1482.0, 83.0, 2173.0, 433.8, 70.7, 429.5, "Butterfly11Larvae", "Butterfly13Larvae"),
                new Area("Waterfalls", -520.0, 151.0, 678.0, 398.0, 82.1, 419.4, "Butterfly11Larvae", "Butterfly12Larvae", "Butterfly13Larvae"),
                new Area("GrandCanyon", 1907.0, 61.0, 201.0, 516.6, 77.7, 541.6, "Butterfly11Larvae"),
                new Area("Caves", 832.0, 39.0, 1089.0, 175.9, 65.0, 149.3, "Butterfly14Larvae"),
                new Area("Dunes", 1391.0, 52.0, 1268.0, 310.0, 56.7, 321.4, "Butterfly11Larvae"),
                new Area("BlackDesert", -179.0, 83.0, -679.0, 551.4, 70.7, 548.7, "Butterfly14Larvae"),
                new Area("Cave-Grotte", 267.0, 126.4, 1520.0, 108.0, 42.9, 160.2, "Butterfly12Larvae"),
                new Area("Under-ShroomRiver", -205.0, -39.0, 477.5, 270.0, 35.6, 398.9, "Butterfly15Larvae"),
                new Area("Crater", -352.0, 119.0, 1466.0, 360.8, 130.7, 314.1, "Butterfly11Larvae", "Butterfly13Larvae"),
                new Area("OrangeDesert", 821.0, 83.0, -842.0, 279.2, 65.7, 238.6, "Butterfly13Larvae"),
                new Area("CavesTop", 825.0, 170.7, 1012.9, 64.1, 25.2, 103.4, "Butterfly11Larvae", "Butterfly12Larvae", "Butterfly13Larvae", "Butterfly14Larvae")
        );
        
        var labels = Map.<String, String>of(
                "Butterfly11Larvae", "- Nere (11)",
                "Butterfly12Larvae", "- Lorpen (12)",
                "Butterfly13Larvae", "- Fiorente (13)",
                "Butterfly14Larvae", "- Alben (14)",
                "Butterfly15Larvae", "- Futura (15)"
        );
        
        var cx = 400;
        var cz = 800;
        var px0 = cx - 2000;
        var pz0 = cz + 2000;
        
        var dx = img.getWidth() / 4000d;
        var dz = img.getHeight() / 4000d;
        
        Graphics2D g2 = img.createGraphics();
        
        for (var s : spawns) {
            
            double x = (s.x - px0) * dx;
            double y = (pz0 - s.z) * dz;
            double w = s.w * dx;
            double h = s.d * dz;
            
            var back1 = new Color(32, 32, 32, 128);
            var back2 = new Color(32, 32, 32, 192);
            var front = new Color(255, 255, 255, 255);
            
            g2.setColor(back1);
            g2.fillRect((int)(x - w / 2), (int)(y - h / 2), (int)w, (int)h);
            g2.setColor(front);
            g2.drawRect((int)(x - w / 2), (int)(y - h / 2), (int)w, (int)h);
            
            // g2.setColor(new Color(0, 0, 0, 255));
            
            var fs = g2.getFont().getSize();
            
            var tx = (int)(x - w / 2);
            var ty = (int)(y - h / 2);
            if ("CavesTop".equals(s.name)) {
                ty += h / 2 + 4 + fs + fs;
            } else {
                ty -= (fs + 3) * s.larvae.length;
            }
            
            
            g2.setFont(g2.getFont().deriveFont(Font.BOLD));
            var tw = g2.getFontMetrics().stringWidth(s.name);
            g2.setColor(back2);
            
            g2.fillRect(tx - 1, ty - fs - 1, tw + 2, fs + 2);
            
            g2.setColor(front);
            g2.drawString(s.name, tx, ty);
            g2.setFont(g2.getFont().deriveFont(Font.PLAIN));
            
            for (var n : s.larvae) {
                ty += fs + 2;
                var m = labels.get(n);
                tw = g2.getFontMetrics().stringWidth(m);
                
                g2.setColor(back2);
                
                g2.fillRect(tx - 1, ty - fs - 1, tw + 2, fs + 2);
                
                g2.setColor(front);

                g2.drawString(m, tx, ty);
            }
        }
        
        g2.dispose();
        
        ImageIO.write(img, "PNG", new File("c:/users/akarnokd/git/ThePlanetCrafterMods/CheatMinimap/map_lush_larvae.png"));
    }
}
