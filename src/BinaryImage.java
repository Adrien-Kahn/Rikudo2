import java.awt.*;
import java.awt.image.*;
import javax.swing.JComponent;
import javax.swing.JFrame;

// Manipulation for binary images
public class BinaryImage {

	private int width; // width of the image
	private int height; // height of the image
	private int[] raster; // raster for the image

	// Constructor that instantiates an image of a specified width and height (all pixels are white)
	public BinaryImage(int width, int height) {
		this.width = width;
		this.height = height;
		raster = new int[width * height];
		for (int i = 0; i < width * height; i++)
			raster[i] = 0xFFFFFFFF;
	}

	// Constructor that instantiates a square image of a specified size (all pixels are black)
	public BinaryImage(int size) {
		this(size, size);
	}

	// Constructor that reads an image from a specified file (.png format)
	public BinaryImage(String filename) {
		// System.out.println("Opening image from file " + filename + " ... ");
		java.awt.Image img = Toolkit.getDefaultToolkit().getImage(filename);
		PixelGrabber pg = new PixelGrabber(img, 0, 0, -1, -1, true);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}
		raster = (int[]) pg.getPixels();
		width = pg.getWidth();
		height = pg.getHeight();
		if (width == -1) {
			System.out.println("Error in opening the file " + filename);
			throw new IllegalArgumentException("Error in opening the file " + filename);
		}
	}

	// Produces a printable image from the raster
	public java.awt.Image toImage() {
		ImageProducer ip = new MemoryImageSource(width, height, raster, 0, width);
		return Toolkit.getDefaultToolkit().createImage(ip);
	}

	// Return the width of the image
	public int getWidth() {
		return this.width;
	}

	// Return the height of the image
	public int getHeight() {
		return this.height;
	}

	// Return the size of the image if it is a square, and -1 otherwise
	public int getSize() {
		return (this.width == this.height ? this.width : -1);
	}

	// Set the pixel at position (x,y) to color c
	protected void setPixel(int x, int y, int c) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new IllegalArgumentException("illegal position");
		}
		raster[x + width * y] = c;
	}

	// Test whether a pixel is black or white
	public boolean isBlack(int x, int y) {
		if (x < 0 || x >= width || y < 0 || y >= height) {
			throw new IllegalArgumentException("illegal position");
		}
		return (raster[x + width * y] & 0xFFFFFF) == 0;
	}

	// Blacken the pixel at position (x,y)
	public void toBlack(int x, int y) {
		this.setPixel(x, y, 0xFF000000);
	}

	// Whiten the pixel at position (x,y)
	public void toWhite(int x, int y) {
		this.setPixel(x, y, 0xFFFFFFFF);
	}

	// Fill the pixel of a squared area with black color. The area is defined by its top left corner P=(x,y) and by its size.
	public void fillAreaBlack(int x, int y, int size) {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				toBlack(x + i, y + j);
	}

	// Fill the pixel of a squared area with white color. The area is defined by its top left corner P=(x,y) and by its size.
	public void fillAreaWhite(int x, int y, int size) {
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
				toWhite(x + i, y + j);
	}

	// Test whether all pixels in a given square region are of the same color
	public boolean isConstantColor(int x, int y, int size) {
		if (x < 0 || x + size > width || y < 0 || y + size > height) {
			throw new IllegalArgumentException("illegal position");
		}
		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++) {
				if (raster[x + width * y] != raster[(x + i) + width * (y + j)])
					return false;
			}
		return true;
	}
	
	
	
	// Draws a circle with center (a,b) and radius r
	public void drawCircle(int a, int b, int r) {
		for (int x = a - r; x <= a + r; x ++) {
			for (int y = b - r; y <= b + r; y ++) {
				if ((x - a)*(x - a) + (y - b)*(y - b) <= r*r) {
					toBlack(x,y);
				}
			}
		}
	}
	
	
	// The euclidian distance between two points
	public double distance(int x1, int y1, double x2, double y2) {
		return Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1) + 0.);
	}
	
	public double distance(int x1, int y1, int x2, int y2) {
		return Math.sqrt((x2 - x1)*(x2 - x1) + (y2 - y1)*(y2 - y1));
	}
	
	
	// Draws a line from (x1,y1) to (x2,y2) with width w
	public void drawLine(int x1, int y1, int x2, int y2, int w) {
		
		// Useful stuff
		int xmax = Math.max(x1, x2);
		int xmin = Math.min(x1, x2);
		int ymax = Math.max(y1, y2);
		int ymin = Math.min(y1, y2);
		
		// The circles at the end of the lines
		drawCircle(x1, y1, w);
		drawCircle(x2, y2, w);
		
		// The general method does not work with x1 = x2 or y1 = y2 because of infinite slopes so we deal with it like that:
		if (x1 == x2) {
			for (int x = x1 - w; x <= x1 + w; x ++) {
				for (int y = ymin; y <= ymax; y ++) {
					toBlack(x,y);
				}
			}
			return;
		}
		
		if (y1 == y2) {
			for (int y = y1 - w; y <= y1 + w; y ++) {
				for (int x = xmin; x <= xmax; x ++) {
					toBlack(x,y);
				}
			}
			return;
		}
		
		
		double segLength = distance(x1, y1, x2, y2);
		double dd = Math.sqrt(w*w + segLength*segLength);
		
		// The parameters of d, the line from z1 to z2
		double a = (y2 - y1 + 0.)/(x2 - x1);
		double b = - ((x1 * (y2 - y1 + 0.))/(x2 - x1)) + y1;
		
		for (int x = xmin - w; x <= xmax + w; x ++) {
			for (int y = ymin - w; y <= ymax + w; y ++) {
				
				// The parameters of the line orthogonal to d that passes through (x,y)
				double ao = -1/a;
				double bo = y - ao*x;
				
				// Computing the coordinates of the intersection point
				double xi = (bo - b)/(a - ao);
				double yi = ao*xi + bo;
				
				// Computing the distance between (x,y) and d
				double dist = distance(x, y, xi, yi);
				
				if (dist < w && distance(x, y, x1, y1) < dd && distance(x, y, x2, y2) < dd) {
					toBlack(x,y);
				}
				
			}
		}
		
	}
	
	
	// Draws an hexagon with center (x,y), radius r and width w
	public void drawHexagon(int x, int y, int r, int w) {
		drawLine(x + 1*r, y, (int) (x + 0.5*r), (int) (y + (Math.sqrt(3)*r)/2), w);
		drawLine((int) (x + 0.5*r), (int) (y + (Math.sqrt(3)*r)/2), (int) (x - 0.5*r), (int) (y + (Math.sqrt(3)*r)/2), w);
		drawLine((int) (x - 0.5*r), (int) (y + (Math.sqrt(3)*r)/2), x - 1*r, y, w);
		drawLine(x + 1*r, y, (int) (x + 0.5*r), (int) (y - (Math.sqrt(3)*r)/2), w);
		drawLine((int) (x + 0.5*r), (int) (y - (Math.sqrt(3)*r)/2), (int) (x - 0.5*r), (int) (y - (Math.sqrt(3)*r)/2), w);
		drawLine((int) (x - 0.5*r), (int) (y - (Math.sqrt(3)*r)/2), x - 1*r, y, w);
	}
	
	// Draws a diamond with center (x,y), radius r and width w
	public void drawDiamond(int x, int y, int r, int w) {
		drawLine(x + r, y, x, y + r, w);
		drawLine(x, y + r, x - r, y, w);
		drawLine(x - r, y, x, y - r, w);
		drawLine(x, y - r, x + r, y, w);
	}
	
	
	// Here we build methods for writing numbers
	// Draw[n] draws the number [n] in a rectangle with top left (x,y), width w and height h
	
	public void draw0(int x, int y, int w, int h) {
		drawLine(x + w/5, y, x + w/5, y + h, w/8);
		drawLine(x + w/5, y, x + 4*w/5, y, w/8);
		drawLine(x + 4*w/5, y, x + 4*w/5, y + h, w/8);
		drawLine(x + w/5, y + h, x + 4*w/5, y + h, w/8);
	}
	
	public void draw1(int x, int y, int w, int h) {
		drawLine(x + w/2, y, x + w/2, y + h, w/8);
	}
	
	public void draw2(int x, int y, int w, int h) {
		drawLine(x + w/5, y, x + 4*w/5, y, w/8);
		drawLine(x + w/5, y + h, x + 4*w/5, y + h, w/8);
		drawLine(x + w/5, y + h/2, x + 4*w/5, y + h/2, w/8);
		drawLine(x + 4*w/5, y, x + 4*w/5, y + h/2, w/8);
		drawLine(x + w/5, y + h/2, x + w/5, y + h, w/8);
	}
	
	public void draw3(int x, int y, int w, int h) {
		drawLine(x + w/5, y, x + 4*w/5, y, w/8);
		drawLine(x + w/5, y + h, x + 4*w/5, y + h, w/8);
		drawLine(x + w/5, y + h/2, x + 4*w/5, y + h/2, w/8);
		drawLine(x + 4*w/5, y, x + 4*w/5, y + h, w/8);
	}
	
	public void draw4(int x, int y, int w, int h) {
		drawLine(x + w/5, y, x + w/5, y + h/2, w/8);
		drawLine(x + w/5, y + h/2, x + 4*w/5, y + h/2, w/8);
		drawLine(x + 4*w/5, y, x + 4*w/5, y + h, w/8);
	}
	
	public void draw5(int x, int y, int w, int h) {
		drawLine(x + w/5, y, x + 4*w/5, y, w/8);
		drawLine(x + w/5, y + h, x + 4*w/5, y + h, w/8);
		drawLine(x + w/5, y + h/2, x + 4*w/5, y + h/2, w/8);
		drawLine(x + w/5, y, x + w/5, y + h/2, w/8);
		drawLine(x + 4*w/5, y + h/2, x + 4*w/5, y + h, w/8);
	}
	
	public void draw6(int x, int y, int w, int h) {
		drawLine(x + w/5, y, x + 4*w/5, y, w/8);
		drawLine(x + w/5, y + h, x + 4*w/5, y + h, w/8);
		drawLine(x + w/5, y + h/2, x + 4*w/5, y + h/2, w/8);
		drawLine(x + w/5, y, x + w/5, y + h, w/8);
		drawLine(x + 4*w/5, y + h/2, x + 4*w/5, y + h, w/8);
	}
	
	
	// The general method to draw a digit
	public void drawD(int d, int x, int y, int w, int h) {
		switch (d) {
		case 0:
			draw0(x, y, w, h);
			break;
		case 1:
			draw1(x, y, w, h);
			break;
		case 2:
			draw2(x, y, w, h);
			break;
		case 5:
			draw5(x, y, w, h);
			break;
		}
	}
	
	
	// Draws the integer n in a rectangle centered in (x,y) with width w and height h
	public void drawInt(int n, int x, int y, int w, int h) {
		
		// comute the number of digits
		int digitNumber = (int) Math.log10(n) + 1;
		
		// Define the width of each digit
		int digitWidth = w/digitNumber;
		
		// The starting point of the first digit
		int x0 = x - w/2;
		int y0 = y - h/2;
		
		// Drawing each digit
		for (int k = 0; k < digitNumber; k ++) {
			int d = ((int) (n/Math.pow(10, k))) % 10;
			drawD(d, x0 + (digitNumber - k - 1)*digitWidth, y0, digitWidth, h);
		}
	}
	
	public static void main(String[] args) {
		
		BinaryImage im = new BinaryImage(800);
		
		im.drawHexagon(400, 400, 200, 10);
		im.drawDiamond(400, 230, 30, 30);
		im.draw6(350, 330, 100, 140);
		
		//im.drawInt(25, 400, 400, 200, 200);
		
		new ImageViewer(im, "Test");
		
	}
	
	
	
	
	
}

class ImageViewer extends JFrame {

	private static final long serialVersionUID = -7498525833438154949L;
	static int xLocation = 0;

	public ImageViewer(BinaryImage img) {
		this.setLocation(xLocation, 0);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageComponent ic = new ImageComponent(img);
		add(ic);
		pack();
		setVisible(true);
		xLocation += img.getWidth();
	}

	public ImageViewer(BinaryImage img, String name) {
		this.setTitle(name);
		this.setLocation(xLocation, 0);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		ImageComponent ic = new ImageComponent(img);
		add(ic);
		pack();
		setVisible(true);
		xLocation += img.getWidth();
	}
}

class ImageComponent extends JComponent {

	private static final long serialVersionUID = -7710437354239150390L;
	private BinaryImage img;

	public ImageComponent(BinaryImage img) {
		this.img = img;
		setPreferredSize(new Dimension(img.getWidth(), img.getWidth()));
	}

	public void paint(Graphics g) {
		g.drawImage(img.toImage(), 0, 0, this);
	}
}
