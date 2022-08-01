package hu.akarnokd.misc;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Scaledown {
    static ByteArrayOutputStream rescale(BufferedImage bi) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        return baos;
    }
    public static void main(String[] args) throws Throwable {
        var bi = ImageIO.read(new File("c:\\Users\\akarnokd\\Downloads\\so_20220627.jpg"));
        int originalWidth = bi.getWidth();
        int originalHeight = bi.getHeight();
        int type = bi.getType() == 0? BufferedImage.TYPE_INT_ARGB : bi.getType();

        //rescale 50%
        BufferedImage resizedImage = new BufferedImage(originalWidth/4, originalHeight/4, type);
        Graphics2D g = resizedImage.createGraphics();
        g.drawImage(bi, 0, 0, originalWidth/4, originalHeight/4, null);
        g.dispose();
        g.setComposite(AlphaComposite.Src);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
        ImageIO.write(resizedImage, "jpg", new File("c:\\Users\\akarnokd\\Downloads\\so_20220627_s.jpg"));
    }
}
