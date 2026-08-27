package significant.minesweeperbotjava;

import java.awt.*;
import java.awt.event.InputEvent;

/**
 *
 * @author tompp
 */
public class ActionExecutor {
    private Robot robot;
    
    public ActionExecutor() {
        try {
            robot = new Robot();
            robot.setAutoDelay(10);
        } catch (AWTException e) {
            throw new RuntimeException("Failed to initialize Robot for automation", e);
        }
    }
    
    public void clickCell(int x, int y) {
        moveAndClick(x, y, InputEvent.BUTTON1_DOWN_MASK);
    }
    
    public void flagCell(int x, int y) {
        moveAndClick(x, y, InputEvent.BUTTON3_DOWN_MASK);
    }
    
    public void activateWindow(int centerX, int centerY) {
        moveAndClick(centerX, centerY, InputEvent.BUTTON1_DOWN_MASK);
        System.out.println("Activated Minesweeper window at x=" + centerX + ", y=" + centerY);
    }
    
    public void moveMouse(int x, int y) {
        robot.mouseMove(x, y);
    }
    
    private void moveAndClick(int x, int y, int buttonMask) {
        robot.mouseMove(x, y);
        robot.delay(5);
        robot.mousePress(buttonMask);
        robot.mouseRelease(buttonMask);
        robot.delay(5);
    }
}
