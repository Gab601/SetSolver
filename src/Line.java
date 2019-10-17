public class Line {
    public Point p1;
    public Point p2;

    public Line() {
        p1 = new Point();
        p2 = new Point();
    }

    public Line(Point p1, Point p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    public double length() {
        return Math.sqrt((p1.x-p2.x)*(p1.x-p2.x) + (p1.y-p2.y)*(p1.y-p2.y));
    }

    public boolean equals(Line l) {
        if (this.p1.equals(l.p1) && this.p2.equals(l.p2)) {
            return true;
        }
        else if (this.p1.equals(l.p2) && this.p2.equals(l.p1)) {
            return true;
        }
        else {
            return false;
        }
    }

    public boolean sharesEndpointWith(Line l) {
        if (this.p1.equals(l.p1) ||  this.p2.equals(l.p2) || this.p1.equals(l.p2) || this.p2.equals(l.p1)) {
            return true;
        }
        else {
            return false;
        }
    }
}
