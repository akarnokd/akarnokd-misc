package hu.akarnokd.theplanetcrafter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class GridMap {

    public static void main(String[] args) throws Throwable {
        BufferedImage bimg = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = bimg.createGraphics();

        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(4));

        g2.drawLine(0, 600, 1000, 600);
        g2.drawLine(400, 0, 400, 1000);

        g2.setStroke(new BasicStroke(1));

        for (int x = 0; x < 1000; x += 50) {
            g2.drawLine(0, x, 1000, x);
            g2.drawLine(x, 0, x, 1000);
        }

        ImageIO.write(bimg, "png", new File("c:\\users\\akarnokd\\downloads\\map_grid.png"));
    }
}
