package significant.minesweeperbotjava;

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/**
 *
 * @author tompp
 */
public class ScreenCapture {
    private Rectangle captureArea;
    
    public ScreenCapture(int x, int y, int width, int height){
        this.captureArea = new Rectangle(x, y, width, height);
     
    }
    
    public BufferedImage capture() throws AWTException {
        Robot robot = new Robot();
        return robot.createScreenCapture(captureArea);
    }
    
    public void saveCapture (BufferedImage image, String fileName) throws Exception{
        File outputfile = new File(fileName);
        ImageIO.write(image, "png", outputfile);
        System.out.println("Screenshot saved: " + fileName);
    }
}
