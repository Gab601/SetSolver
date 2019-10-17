public class Point {
    public int x;
    public int y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Point p) {
        if (this.x == p.x && this.y == p.y) {
            return true;
        }
        else {
            return false;
        }
    }

    public Point plus(Point p) {
        return new Point(this.x+p.x, this.y+p.y);
    }

    public Point minus(Point p) {
        return new Point(this.x-p.x, this.y-p.y);
    }

    public Point times(double factor) {
        return new Point((int)(this.x*factor), (int)(this.y*factor));
    }
}
