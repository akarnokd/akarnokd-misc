package hu.akarnokd.misc;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ImgRotate {

    public static void main(String[] args) throws Exception {
        BufferedImage source = ImageIO.read(new File("c:/users/akarnokd/Downloads/imgrot.jpeg"));
        
        int w = source.getWidth();
        int h = source.getHeight();
        double angle = 30;
        if (angle == 90) {
            w = source.getHeight();
            h = source.getWidth();
        }
        
        BufferedImage dest = new BufferedImage(w / 4, h / 4, BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g = dest.createGraphics();
        g.rotate(Math.toRadians(angle));
        int dx = w / 4;
        int dy = h / 4;
        
        g.drawImage(source, dx, 0, w / 4, h / 4, null);
        
        g.dispose();
        
        ImageIO.write(dest, "png", new File("c:/users/akarnokd/Downloads/imgrot_out.png"));
    }
}
