import java.util.ArrayList;

public class Point {
	
	public double x;
	public double y;
	
	Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
    @Override
    public String toString() { 
        return String.format("(" + xint() + ", " + yint() + ")"); 
    }
	
	// Returns the distance between p1 and p2
	static double distance(Point p1, Point p2) {
		return Math.sqrt((p2.x - p1.x)*(p2.x - p1.x) + (p2.y - p1.y)*(p2.y - p1.y));
	}
	
	// Returns whether p1 = p2 with caracteristic distance d (to account for calculation errors)
	static boolean isEqual(Point p1, Point p2, double d) {
		return (distance(p1, p2) < 0.1 * d);
	}
	
	// Returns whether p belong to al (in the sense of isEqual)
	static boolean isInArray(Point p, ArrayList<Point> al, double d) {
		for (Point pa : al) {
			if (isEqual(p, pa, d)) {
				return true;
			}
		}
		return false;
	}
	
	// Returns coordinates as integers
	public int xint() {
		return (int) x;
	}
	
	public int yint() {
		return (int) y;
	}
	
	
}
