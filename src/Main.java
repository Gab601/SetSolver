import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Main {
    public static void main (String args[]) {

        BufferedImage bufferedImage = null;

        for (int i = 1; i < 5; i++) {
            try { bufferedImage = ImageIO.read(new File("set" + i + "_s.png")); }
            catch (IOException e) { e.printStackTrace(); }
            RGBImage im = new RGBImage(bufferedImage);


            BWImage lines = im.getLines((float)0.1, 4, (float)0.01);
            //BWImage corners = im.getCorners((float)0.1, 4);

            BufferedImage outImage = lines.toImage();
            try {
                File outputfile = new File("lines" + i + ".png");
                ImageIO.write(outImage, "png", outputfile);
            }
            catch (IOException e) { e.printStackTrace(); }
        }
    }
}
