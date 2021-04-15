package hu.akarnokd.misc;

import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

public class Recolor {

    public static void main(String[] args) throws IOException {
        File f = new File("c:\\Users\\akarnokd\\git\\digiprime-pcp-webservice\\src\\main\\webapp\\bar-chart-h.png ");
        BufferedImage img = ImageIO.read(f);
        
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color = img.getRGB(x, y);
                int newColor = (color & 0xFF000000) | (0xA0A2A2);
                img.setRGB(x, y, newColor);
            }
        }
        
        ImageIO.write(img, "png", f);
    }
}
