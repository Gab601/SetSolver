import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

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

    public BWImage getCorners(int search_radius, int local_max_radius) {
        BWImage edges = this.boxBlur(2).getEdges().binary((float)0.03);
        BWImage out = new BWImage(width, height);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (edges.get(x, y) > 0) {
                    boolean[] lines = new boolean[360];
                    for (int theta = 0; theta < 360; theta++) {
                        int dx = (int)(search_radius*Math.cos(theta*6.283/360));
                        int dy = (int)(search_radius*Math.sin(theta*6.283/360));
                        if (edges.isLine(new Point(x, y), new Point(x+dx, y+dy))) {
                            lines[theta] = true;
                        }
                        else {
                            lines[theta] = false;
                        }
                    }
                    for (int theta = 0; theta < 360; theta++) {
                        if (lines[theta] && lines[(theta+90)%360]) {
                            int max = theta;
                            int min = (theta+90)%360;
                            while (lines[max]) {
                                max = (max+1)%360;
                            }
                            while (lines[min]) {
                                min = (min+359)%360;
                            }
                            out.set(x, y, (float)((max-min+360)%360)/360);
                        }
                    }
                }
            }
        }
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int dx = -local_max_radius; dx <= local_max_radius; dx++) {
                    for (int dy = -local_max_radius; dy <= local_max_radius; dy++) {
                        if (out.get(x, y) < out.get(x+dx, y+dy) && out.get(x, y) > 0) {
                            out.set(x, y, 0);
                        }
                    }
                }
            }
        }
        return out;
    }

    public ArrayList<Line> getLines(int corner_radius, int local_max_radius) {
        BWImage edges = this.boxBlur(2).getEdges().binary((float)0.05).boxBlur(2);
        BWImage corners = this.getCorners( corner_radius, local_max_radius);
        ArrayList<Line> lines = new ArrayList<>();

        //Get a list of pixels that have positive value
        int num_corners = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (corners.get(x, y) > 0) {
                    num_corners++;
                }
            }
        }
        Point[] corner_list = new Point[num_corners];
        int index = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (corners.get(x, y) > 0) {
                    corner_list[index] = new Point(x, y);
                    index++;
                }
            }
        }

        for (Point corner1: corner_list) {
            for (Point corner2: corner_list) {
                int dx = corner2.x-corner1.x;
                int dy = corner2.y-corner1.y;
                if ((dx != 0 || dy != 0) && dx >= 0) {
                    Line l = new Line(corner1, corner2);
                    if (edges.isLine(l) && l.length() > corner_radius) {
                        lines.add(l);
                    }
                }
            }
        }
        return lines;
    }

    public BWImage drawLines(ArrayList<Line> lines) {
        BWImage out = new BWImage(this.width, this.height);
        for (Line l: lines) {
            out.drawLine(l, 1);
        }
        return out;
    }

    public ArrayList<Parallelogram> getPossibleCards(ArrayList<Line> lines, double angle_cutoff) {
        ArrayList<Parallelogram> parallelograms = new ArrayList<>();
        for (Line line1: lines) {
            for (Line line2: lines) {
                if (line1.sharesEndpointWith(line2) && !line1.equals(line2)) {
                    Parallelogram p = new Parallelogram(line1, line2);
                    if (p.angle < (90+angle_cutoff) && p.angle > (90-angle_cutoff)) {
                        parallelograms.add(new Parallelogram(line1, line2));
                    }
                }
            }
        }
        return parallelograms;
    }

    public BWImage drawParallelograms(ArrayList<Parallelogram> parallelograms) {
        BWImage out = new BWImage(this.width, this.height);
        for (Parallelogram parallelogram: parallelograms) {
            for (Line line: parallelogram.getLines()) {
                out.drawLine(line, 1);
                out.set(parallelogram.point0, (float)0.5);
            }
        }
        return out;
    }

    public RGBImage parallelogramToRectangle(Parallelogram parallelogram, int width, int height) {
        RGBImage out = new RGBImage(width, height);
        if (width > height && parallelogram.getLines()[0].length() > parallelogram.getLines()[1].length() || width <= height && parallelogram.getLines()[0].length() <= parallelogram.getLines()[1].length()) {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    Point bottom_point = parallelogram.point2.times((1 - (double) x / width)).plus(parallelogram.point3.times(((double) x / width)));
                    Point top_point = parallelogram.point0.times((1 - (double) x / width)).plus(parallelogram.point1.times(((double) x / width)));
                    Point final_point = bottom_point.times(1 - (double) y / height).plus(top_point.times((double) y / height));
                    out.set(x, y, this.get(final_point));
                }
            }
        }
        else {
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < width; y++) {
                    Point bottom_point = parallelogram.point3.times((1-(double)x/width)).plus(parallelogram.point1.times(((double)x/width)));
                    Point top_point = parallelogram.point2.times((1-(double)x/width)).plus(parallelogram.point0.times(((double)x/width)));
                    Point final_point = bottom_point.times(1-(double)y/height).plus(top_point.times((double)y/height));
                    out.set(x, y, this.get(final_point));
                }
            }
        }
        return out;
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

    public float[] get(Point p) {
        return get(p.x, p.y);
    }

    public void set(Point p, float[] color) {
        set(p.x, p.y, color);
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