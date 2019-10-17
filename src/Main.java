import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main (String args[]) {

        BufferedImage bufferedImage = null;

        for (int i = 3; i < 4; i++) {
            try { bufferedImage = ImageIO.read(new File("set" + i + "_s.png")); }
            catch (IOException e) { e.printStackTrace(); }
            RGBImage im = new RGBImage(bufferedImage);

            ArrayList<Line> lines = im.getLines(15, 5);
            ArrayList<Parallelogram> parallelograms = im.getPossibleCards(lines, 20);
            BWImage parallelogram_im = im.drawParallelograms(parallelograms);

            int a = 0;
            for (Parallelogram p: parallelograms) {
                RGBImage possibleCard = im.parallelogramToRectangle(p, 100, 100);

                BufferedImage outImage = possibleCard.toImage();
                try {
                    File outputfile = new File("card" + a + ".png");
                    ImageIO.write(outImage, "png", outputfile);
                }
                catch (IOException e) { e.printStackTrace(); }
                a++;
            }

            BufferedImage outImage = parallelogram_im.toImage();
            try {
                File outputfile = new File("parallelograms" + i + ".png");
                ImageIO.write(outImage, "png", outputfile);
            }
            catch (IOException e) { e.printStackTrace(); }

            /*outImage = edges.toImage();
            try {
                File outputfile = new File("edges" + i + ".png");
                ImageIO.write(outImage, "png", outputfile);
            }
            catch (IOException e) { e.printStackTrace(); }*/
        }
    }
}
