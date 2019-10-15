import java.awt.*;
import java.awt.image.BufferedImage;

public class BWImage extends FeatureImage {
    BWImage(BufferedImage image) {
        height = image.getHeight();
        width = image.getWidth();
        image_data = new float[height*width];
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                set(x, y, 0, (float)color.getRed()/255);
            }
        }
    }

    BWImage(int width, int height, float[] image_data) {
        super(width, height, image_data);
    }

    BWImage(int width, int height) {
        super(width, height);
    }

    BWImage() {
        super();
    }

    public BWImage applyFilter(BWImage filter) {
        BWImage out = new BWImage(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float value = 0;
                for (int dx = -(filter.width-1)/2; dx <= (filter.width-1)/2; dx++) {
                    for (int dy = -(filter.height-1)/2; dy <= (filter.height-1)/2; dy++) {
                        float filter_value = filter.get(dx+(filter.width-1)/2, dy+(filter.height-1)/2);
                        value += get(x+dx, y+dy) * filter_value;
                    }
                }
                out.set(x, y, value);
            }
        }
        return out;
    }

    public float get(int x, int y) {
        return get(x, y, 0);
    }

    public BufferedImage toImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float c = Math.max(0, Math.min(1, get(x, y)));
                Color color = new Color(c, c, c);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    public BWImage plus(BWImage bwImage) {
        BWImage out = new BWImage(width, height);
        for (int i = 0; i < image_data.length; i++) {
            out.set(i, this.get(i) + bwImage.get(i));
        }
        return out;
    }

    public BWImage times(BWImage bwImage) {
        BWImage out = new BWImage(width, height);
        for (int i = 0; i < image_data.length; i++) {
            out.set(i, this.get(i) * bwImage.get(i));
        }
        return out;
    }

    public BWImage boxBlur(int radius) {
        BWImage filter = new BWImage(radius*2+1, radius*2+1);
        for (int x = 0; x < filter.width; x++) {
            for (int y = 0; y < filter.height; y++) {
                filter.set(x, y, (float)1/((radius*2+1)*(radius*2+1)));
            }
        }
        return this.applyFilter(filter);
    }

    public void drawLine(Pixel p1, Pixel p2, float value) {
        int dx = p2.x-p1.x;
        int dy = p2.y-p1.y;
        if (Math.abs(dx) > Math.abs(dy)) {
            if (dx>0) {
                for (int x = 0; x <= dx; x++) {
                    int y = x*dy/dx;
                    this.set(p1.x+x, p1.y+y, value);
                }
            }
            else {
                for (int x = 0; x <= -dx; x++) {
                    int y = x*dy/dx;
                    this.set(p1.x-x, p1.y-y, value);
                }
            }
        }
        else {
            if (dy>0) {
                for (int y = 0; y <= dy; y++) {
                    int x = y*dx/dy;
                    this.set(p1.x+x, p1.y+y, value);
                }
            }
            else {
                for (int y = 0; y <= -dy; y++) {
                    int x = y*dx/dy;
                    this.set(p1.x-x, p1.y-y, value);
                }
            }
        }
    }

    public BWImage cutoff(float cutoff) {
        BWImage out = new BWImage(width, height, image_data);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (get(x, y) < cutoff) {
                    out.set(x, y, 0);
                }
            }
        }
        return out;
    }
}
