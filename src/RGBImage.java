import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class RGBImage extends FeatureImage {

    RGBImage(BufferedImage bufferedImage) {
        super(bufferedImage);
    }

    RGBImage(int width, int height, float[] image_data) {
        super(height, width, image_data);
    }

    RGBImage(int width, int height) {
        this.height = height;
        this.width = width;
        this.image_data = new float[height*width*3];
    }

    RGBImage() {
        super();
    }

    public RGBImage applyFilter(BWImage filter) {
        RGBImage out = new RGBImage(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float[] value = new float[3];
                for (int dx = -(filter.width-1)/2; dx <= (filter.width-1)/2; dx++) {
                    for (int dy = -(filter.height-1)/2; dy <= (filter.height-1)/2; dy++) {
                        float filter_value = filter.get(dx+(filter.width-1)/2, dy+(filter.height-1)/2);
                        value[0] += get(x+dx, y+dy)[0] * filter_value;
                        value[1] += get(x+dx, y+dy)[1] * filter_value;
                        value[2] += get(x+dx, y+dy)[2] * filter_value;
                    }
                }
                out.set(x, y, value);
            }
        }
        return out;
    }

    public BWImage getEdges() {
        BWImage v_filter = new BWImage(3, 3, new float[] {1, 1, 1, 0, 0, 0, -1, -1, -1});
        BWImage h_filter = new BWImage(3, 3, new float[] {1, 0, -1, 1, 0, -1, 1, 0, -1});
        BWImage vert = this.applyFilter(v_filter).toBW();
        BWImage hor = this.applyFilter(h_filter).toBW();
        BWImage edges = vert.times(vert).plus(hor.times(hor));
        return edges;
    }

    public BWImage getCorners(float cutoff, int radius) {
        BWImage weights = new BWImage(this.width, this.height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float difference_x = 0;
                float difference_y = 0;
                for (int dx = -radius; dx <= radius; dx++) {
                    for (int dy = -radius; dy <= radius; dy++) {
                        difference_x += Math.abs(get(x+dx, y+dy, 0) - get(x+dx+1, y+dy, 0));
                        difference_x += Math.abs(get(x+dx, y+dy, 1) - get(x+dx+1, y+dy, 1));
                        difference_x += Math.abs(get(x+dx, y+dy, 2) - get(x+dx+1, y+dy, 2));

                        difference_y += Math.abs(get(x+dx, y+dy, 0) - get(x+dx, y+dy+1, 0));
                        difference_y += Math.abs(get(x+dx, y+dy, 1) - get(x+dx, y+dy+1, 1));
                        difference_y += Math.abs(get(x+dx, y+dy, 2) - get(x+dx, y+dy+1, 2));
                    }
                }
                weights.set(x, y, Math.min(difference_x, difference_y)/(radius*radius));
            }
        }
        BWImage out = new BWImage(this.width, this.height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (weights.get(x, y) < weights.get(x+1, y+1) ||
                        weights.get(x, y) < weights.get(x+1, y) ||
                        weights.get(x, y) < weights.get(x+1, y-1) ||
                        weights.get(x, y) < weights.get(x, y+1) ||
                        weights.get(x, y) < weights.get(x, y-1) ||
                        weights.get(x, y) < weights.get(x-1, y+1) ||
                        weights.get(x, y) < weights.get(x-1, y) ||
                        weights.get(x, y) < weights.get(x-1, y-1)) {
                    out.set(x, y, 0);
                }
                else if (weights.get(x, y) < cutoff){
                    out.set(x, y, 0);
                }
                else {
                    out.set(x, y, weights.get(x, y));
                }
            }
        }
        return out;
    }

    public BWImage getLines(float corner_cutoff, int corner_radius, float value_cutoff) {
        BWImage corners = this.boxBlur(3).getCorners(corner_cutoff, corner_radius);
        BWImage edges = this.boxBlur(2).getEdges();
        BufferedImage outImage = edges.toImage();
        try {
            File outputfile = new File("lines" + Math.random() + ".png");
            ImageIO.write(outImage, "png", outputfile);
        }
        catch (IOException e) { e.printStackTrace(); }
        BWImage lines = new BWImage(this.width, this.height);
        int num_corners = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (corners.get(x, y) > 0) {
                    num_corners++;
                }
            }
        }
        Pixel[] corner_list = new Pixel[num_corners];
        int index = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (corners.get(x, y) > 0) {
                    corner_list[index] = new Pixel(x, y, corners.get(x, y));
                    index++;
                }
            }
        }
        for (Pixel corner1: corner_list) {
            for (Pixel corner2: corner_list) {
                int dx = (int)(corner2.x-corner1.x);
                int dy = (int)(corner2.y-corner1.y);
                if ((dx != 0 || dy != 0) && dx >= 0) {
                    float avg_edge = 0;

                    //Calculate the average value along the corner
                    if (Math.abs(dx) > Math.abs(dy)) {
                        for (int x = 0; x <= dx; x++) {
                            int y = x*dy/dx;
                            avg_edge += edges.get(corner1.x+x, corner1.y+y);
                        }
                        avg_edge = avg_edge/Math.abs(dx);
                    }
                    else {
                        if (dy>0) {
                            for (int y = 0; y <= dy; y++) {
                                int x = y*dx/dy;
                                avg_edge += edges.get(corner1.x+x, corner1.y+y);
                            }
                        }
                        else {
                            for (int y = 0; y <= -dy; y++) {
                                int x = y*dx/dy;
                                avg_edge += edges.get(corner1.x-x, corner1.y-y);
                            }
                        }
                        avg_edge = avg_edge/Math.abs(dy);
                    }

                    float value = corner1.value*corner2.value*avg_edge*avg_edge;
                    if (value > value_cutoff) {
                        //System.out.println(corner1.value + " " + corner2.value + " " + avg_edge);
                        lines.drawLine(corner1, corner2, 1);
                    }
                }

            }
        }
        return lines;
    }

    public BWImage toBW() {
        BWImage out = new BWImage(this.width, this.height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float brightness = (this.get(x, y, 0) + this.get(x, y, 0) + this.get(x, y, 0)) / 3;
                out.set(x, y, brightness);
            }
        }
        return out;
    }

    public void set(int x, int y, int channel, float color) {
        image_data[x+width*y+width*height*channel] = color;
    }

    public float[] get(int x, int y) {
        return new float[] {get(x, y, 0), get(x, y, 1), get(x, y, 2)};
    }

    public void set(int x, int y, float[] color) {
        for (int c = 0; c < color.length; c++) {
            set(x, y, c, color[c]);
        }
    }

    public BufferedImage toImage() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                float red = Math.max(0, Math.min(1, get(x, y, 0)));
                float green = Math.max(0, Math.min(1, get(x, y, 1)));
                float blue = Math.max(0, Math.min(1, get(x, y, 2)));
                Color color = new Color(red, green, blue);
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    public RGBImage plus(RGBImage rgbImage) {
        RGBImage out = new RGBImage(width, height);
        for (int i = 0; i < image_data.length; i++) {
            out.set(i, this.get(i) + rgbImage.get(i));
        }
        return out;
    }

    public RGBImage times(RGBImage rgbImage) {
        RGBImage out = new RGBImage(width, height);
        for (int i = 0; i < image_data.length; i++) {
            out.set(i, this.get(i) * rgbImage.get(i));
        }
        return out;
    }

    public RGBImage power(double p) {
        RGBImage out = new RGBImage(width, height);
        for (int i = 0; i < image_data.length; i++) {
            out.set(i, (float)Math.pow(this.get(i), p));
        }
        return out;
    }

    public RGBImage boxBlur(int radius) {
        BWImage filter = new BWImage(radius*2+1, radius*2+1);
        for (int x = 0; x < filter.width; x++) {
            for (int y = 0; y < filter.height; y++) {
                filter.set(x, y, (float)1/((radius*2+1)*(radius*2+1)));
            }
        }
        return this.applyFilter(filter);
    }

    public RGBImage compress(int factor) {
        RGBImage out = new RGBImage(this.width/factor, this.height/factor);
        for (int x = 0; x < out.width; x++) {
            for (int y = 0; y < out.height; y++) {
                out.set(x, y, this.get(x*factor, y*factor));
            }
        }
        return out;
    }
}