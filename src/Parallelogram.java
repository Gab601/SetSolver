public class Parallelogram {
    public Point point0; //Point between points 1 and 2
    public Point point1;
    public Point point2;
    public Point point3;
    public double angle;

    public Parallelogram() {
        point0 = new Point();
        point1 = new Point();
        point2 = new Point();
        point3 = getLastPoint();
        angle = getAngle();
    }

    public Parallelogram(Line l1, Line l2) {
        if (l1.p1.equals(l2.p1)) {
            point0 = new Point(l1.p1.x, l1.p1.y);
            point1 = new Point(l1.p2.x, l1.p2.y);
            point2 = new Point(l2.p2.x, l2.p2.y);
        }
        else if (l1.p1.equals(l2.p2)){
            point0 = new Point(l1.p1.x, l1.p1.y);
            point1 = new Point(l1.p2.x, l1.p2.y);
            point2 = new Point(l2.p1.x, l2.p1.y);
        }
        else if (l1.p2.equals(l2.p1)){
            point0 = new Point(l1.p2.x, l1.p2.y);
            point1 = new Point(l1.p1.x, l1.p1.y);
            point2 = new Point(l2.p2.x, l2.p2.y);
        }
        else if (l1.p2.equals(l2.p2)){
            point0 = new Point(l1.p2.x, l1.p2.y);
            point1 = new Point(l1.p1.x, l1.p1.y);
            point2 = new Point(l2.p1.x, l2.p1.y);
        }
        else {
            point0 = new Point();
            point1 = new Point();
            point2 = new Point();
        }
        point3 = getLastPoint();
        angle = getAngle();
    }

    public Point getLastPoint() {
        return point1.plus(point2).minus(point0);
    }

    public Line[] getLines() {
        Line[] lines = new Line[4];
        lines[0] = new Line(point0, point1);
        lines[1] = new Line(point1, point3);
        lines[2] = new Line(point3, point2);
        lines[3] = new Line(point2, point0);
        return lines;
    }

    public double getAngle() {
        Point v1 = point1.minus(point0);
        Point v2 = point2.minus(point0);
        double dot = v1.x*v2.x + v1.y*v2.y;
        double v1_mag = Math.sqrt(v1.x*v1.x + v1.y*v1.y);
        double v2_mag = Math.sqrt(v2.x*v2.x + v2.y*v2.y);
        double angle = Math.acos(dot/(v1_mag*v2_mag));
        return angle*180/Math.PI;
    }
}
