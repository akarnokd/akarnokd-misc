package hu.akarnokd.rxjava3;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class MarblesAddBackground {

    public static void main(String[] args) throws Exception {
        String dir = "c:\\Users\\akarnokd\\git\\RxJavaWiki\\images\\rx-operators\\";
        File fdir = new File(dir);
        int count = 0;

        for (File f : fdir.listFiles()) {
            if (f.getName().endsWith(".png") && !f.getName().endsWith("v3.png")) {

                BufferedImage image = ImageIO.read(f);

                int width = image.getWidth();
                int height = image.getHeight();

                boolean hasTransparency = false;
                outer:
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        if ((image.getRGB(j, i) & 0xFF000000) != 0xFF000000) {
                            hasTransparency = true;
                            break outer;
                        }
                    }
                }

                if (hasTransparency) {
                    System.out.println("Found image with transparency: " + f.getName());

                    BufferedImage bimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2 = bimg.createGraphics();
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, width, height);

                    g2.drawImage(image, 0, 0, null);

                    String fullName = f.getAbsolutePath();
                    int idx = fullName.lastIndexOf(".png");
                    fullName = fullName.substring(0, idx) + ".v3.png";
                    System.out.println("    saving as " + fullName);

                    ImageIO.write(bimg, "png", new File(fullName));
                    count++;
                }
            }
        }

        System.out.println("------------");
        System.out.println("Total: " + count);
    }
}
