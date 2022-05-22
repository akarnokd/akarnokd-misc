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
        int nHorizontal = 6000 / step;
        int nVertical = 4000 / step;

        int w = 80;
        int h = 60;

        BufferedImage bimg = new BufferedImage(w * nHorizontal, h * nVertical, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bimg.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int outx = 0;
        for (int z = 4000; z >= -2000; z-= step) {

            int outy = 0;
            for (int x = 3000; x >= -1000; x -= step) {

                System.out.println(mode + " - " + z + " : " + x);

                BufferedImage image = ImageIO.read(new File("c:\\temp\\" + mode + "\\map_" + z + "_" + x + ".png"));

                g2.drawImage(image, outx - w / 2, outy - h / 2, outx + w / 2, outy + h / 2, 0, 0, image.getWidth(), image.getHeight(), null);

                outy += h;
            }

            outx += w;
        }

        ImageIO.write(bimg, "png", new File("c:\\temp\\" + mode + "\\map_" + mode + ".png"));
        System.out.println(mode + " Done");
    }
}
