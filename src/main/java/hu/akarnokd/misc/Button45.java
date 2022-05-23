package hu.akarnokd.misc;

import java.awt.Robot;
import java.awt.event.MouseEvent;

public class Button45 {

    public static void main(String[] args) throws Exception {
        Robot robot = new Robot();
        
        Thread.sleep(5000);
        
        robot.mousePress(MouseEvent.getMaskForButton(4));
        
        Thread.sleep(200);

        robot.mouseRelease(MouseEvent.getMaskForButton(4));

        Thread.sleep(5000);
        
        robot.mousePress(MouseEvent.getMaskForButton(5));
        
        Thread.sleep(200);

        robot.mouseRelease(MouseEvent.getMaskForButton(5));
    }
}
