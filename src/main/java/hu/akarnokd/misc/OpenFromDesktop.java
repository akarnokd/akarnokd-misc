package hu.akarnokd.misc;

import java.io.File;

import javax.imageio.ImageIO;

public class OpenFromDesktop {

    public static void main(String[] args) throws Exception {
        ImageIO.read(new File("c:/users/akarnokd/desktop/image.png"));
    }
}
