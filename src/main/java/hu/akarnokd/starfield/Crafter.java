package hu.akarnokd.starfield;

import java.awt.Robot;
import java.awt.event.*;

public class Crafter {

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        
        robot.setAutoDelay(15);
        
        int delay = 15;
        for (int i = 0; i < delay; i++) {
            System.out.println("Begin in " + (delay - i));
            Thread.sleep(1000);
        }
        
        for (int i = 0; i < 1500; i++) {
            robot.keyPress(KeyEvent.VK_E);
            robot.keyRelease(KeyEvent.VK_E);

            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);

            robot.keyPress(KeyEvent.VK_E);
            robot.keyRelease(KeyEvent.VK_E);
            
            System.out.println("Iteration: " + i);
        }
    }
}
