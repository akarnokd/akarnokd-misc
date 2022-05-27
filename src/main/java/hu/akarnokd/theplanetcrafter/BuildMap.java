package hu.akarnokd.theplanetcrafter;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

public class BuildMap {

    public static void main(String[] args) throws Exception {
        buildMapOf("barren");
        buildMapOf("lush");
    }
    
    record Coords(int x, int z) { }

    static void buildMapOf(String mode) throws Exception {

        int step = 100;
        int maxZ = 3000;
        int minZ = -1400;
        int maxX = 2400;
        int minX = -1000;
        int defaultY = 500;

        int nHorizontal = (maxZ - minZ) / step;
        int nVertical = (maxX - minX) / step;

        int w = 40;
        int h = 30;
        Map<Coords, Integer> heightMap = new HashMap<>();
        // customize which height to pick from the map photos
        
        // --------------------------------------------------

        BufferedImage bimg = new BufferedImage(w * nHorizontal, h * nVertical, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = bimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int outx = 0;
        for (int z = maxZ; z >= minZ; z-= step) {

            int outy = 0;
            for (int x = maxX; x >= minX; x -= step) {

                System.out.println(mode + " - " + z + " : " + x);
                int y = heightMap.getOrDefault(new Coords(x, z), defaultY);

                BufferedImage image = ImageIO.read(new File("c:\\temp\\" + mode + "\\map_" + z + "_" + x + "_" + y + ".png"));

                int iw = image.getWidth();
                int ih = image.getHeight();
                
                g2.drawImage(image, outx - w / 2, outy - h / 2, outx + w / 2, outy + h / 2, iw / 4, ih / 4, iw * 3 / 4, ih * 3 / 4, null);

                outy += h;
            }

            outx += w;
        }

        ImageIO.write(bimg, "png", new File("c:\\temp\\" + mode + "\\map_" + mode + ".png"));
        System.out.println(mode + " Done");
        System.out.printf("CenterX: %d, CenterZ: %d%n", minX + (maxX - minX) / 2, minZ + (maxZ - minZ) / 2);
    }
}
