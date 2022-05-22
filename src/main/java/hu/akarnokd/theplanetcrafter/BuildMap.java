package hu.akarnokd.theplanetcrafter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class BuildMap {

    public static void main(String[] args) throws Exception {
        buildMapOf("barren");
        buildMapOf("lush");
    }

    static void buildMapOf(String mode) throws Exception {

        int step = 200;
        int maxZ = 3000;
        int minZ = -1400;
        int maxX = 2400;
        int minX = -1000;

        int nHorizontal = (maxZ - minZ) / step;
        int nVertical = (maxX - minX) / step;

        int w = 80;
        int h = 60;

        BufferedImage bimg = new BufferedImage(w * nHorizontal, h * nVertical, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int outx = 0;
        for (int z = maxZ; z >= minZ; z-= step) {

            int outy = 0;
            for (int x = maxX; x >= minX; x -= step) {

                //System.out.println(mode + " - " + z + " : " + x);

                BufferedImage image = ImageIO.read(new File("c:\\temp\\" + mode + "\\map_" + z + "_" + x + ".png"));

                g2.drawImage(image, outx - w / 2, outy - h / 2, outx + w / 2, outy + h / 2, 0, 0, image.getWidth(), image.getHeight(), null);

                outy += h;
            }

            outx += w;
        }

        ImageIO.write(bimg, "png", new File("c:\\temp\\" + mode + "\\map_" + mode + ".png"));
        System.out.println(mode + " Done");
        System.out.printf("CenterX: %d, CenterZ: %d%n", minX + (maxX - minX) / 2, minZ + (maxZ - minZ) / 2);
    }
}
