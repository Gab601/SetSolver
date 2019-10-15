import java.awt.*;
import java.awt.image.BufferedImage;

public class FeatureImage {

    protected float[] image_data;
    protected int height;
    protected int width;

    FeatureImage(BufferedImage image) {
        height = image.getHeight();
        width = image.getWidth();
        image_data = new float[height*width*3];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                set(x, y, 0, (float)color.getRed()/255);
                set(x, y, 1, (float)color.getGreen()/255);
                set(x, y, 2, (float)color.getBlue()/255);
            }
        }
    }

    FeatureImage(int width, int height, float[] image_data) {
        this.height = height;
        this.width = width;
        this.image_data = new float[image_data.length];

        for (int i = 0; i < image_data.length; i++) {
            this.image_data[i] = image_data[i];
        }
    }

    FeatureImage(int width, int height) {
        this.height = height;
        this.width = width;
        this.image_data = new float[height*width];
    }

    FeatureImage() {
        this.height = 1;
        this.width = 1;
        this.image_data = new float[1];
    }

    public float get(int x, int y, int channel) {
        if (x < 0) { x = 0; }
        if (x >= width) { x = width - 1; }
        if (y < 0) { y = 0; }
        if (y >= height) { y = height - 1; }

        return image_data[x+width*y+width*height*channel];
    }

    public void set(int x, int y, int channel, float color) {
        if (x < 0) { x = 0; }
        if (x >= width) { x = width - 1; }
        if (y < 0) { y = 0; }
        if (y >= height) { y = height - 1; }
        image_data[x+width*y+width*height*channel] = color;
    }

    public void set(int x, int y, float[] color) {
        for (int c = 0; c < color.length; c++) {
            set(x, y, c, color[c]);
        }
    }

    public void set(int x, int y, float color) {
        set(x, y, 0, color);
    }

    public float get(int i) {
        return image_data[i];
    }

    public void set(int i, float value) {
        image_data[i] = value;
    }
}