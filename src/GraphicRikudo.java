
import java.util.ArrayList;


public class GraphicRikudo {
	
	private Rikudo rikudo;
	private ArrayList<Point> coordinates;
	private BinaryImage im;
	
	// This constructor builds a GraphicRikudo that fills the black area of the image with hexagons which centers are separated by a distance d
	// nbCalls specifies the number of calls to explore in greatest path
	// If it is negative, then no upper bound is imposed on the number of calls
	GraphicRikudo(BinaryImage im1, int xs, int ys, double d, int nbCalls) {
		
		System.out.println("Initiating path search...");
		if (nbCalls < 0) {
			coordinates = im1.greatestPath(xs, ys, d);
		} else {
			coordinates = im1.greatestPath(xs, ys, d, nbCalls);
		}
		System.out.println("Path search done");
		System.out.println("Path length: " + coordinates.size());
		System.out.println();

		
		System.out.println("Initiating graph construction...");
		int n = coordinates.size();
		
		ArrayList<ArrayList<Integer>> adjacencyList = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> path = new ArrayList<Integer>();

		for (int i = 0; i < n; i++) {
			adjacencyList.add(new ArrayList<Integer>());
			path.add(i);
		}
		
		for (int i = 0; i < n-1; i++) {
			for (int j = i+1; j < n; j++) {
				Point p1 = coordinates.get(i);
				Point p2 = coordinates.get(j);
				if (Point.distance(p1,p2) - d < 0.1d) {
					adjacencyList.get(i).add(j);
					adjacencyList.get(j).add(i);
				}
			}
		}
			
		Graph g = new Graph(adjacencyList);
		System.out.println("Graph construction done");
		System.out.println();


				
		System.out.println("Initiating rikudo construction...");
		rikudo = Rikudo.createRikudoPath(g,path);
		System.out.println("Rikudo construction done");
		System.out.println();
		
		
		System.out.println("Initiating image construction...");
		im = new BinaryImage(im1.getWidth(),im1.getHeight());
		
		int w = (int) Math.ceil(d/40);
		int r = (int) Math.ceil(d/2) + w;
		int c = (int) Math.ceil(r/6);
		
		for (Point p : coordinates) {
			im.drawHexagon(p.xint(),p.yint(), r, w);
		}
		
		
		int[] lambda = rikudo.partialMap;
		
		for (int i = 0; i < n; i++) {
			if (lambda[i] != -1) {
				Point p = coordinates.get(i);
				im.drawInt(i, p.xint(), p.yint(), r, r);
			}
		}
		
		ArrayList<ArrayList<Integer>> E = rikudo.diamonds;
		
		for (int u = 0; u < n-1; u++) {
			for (int v = u+1; v < n; v++ ) {
				if (E.get(u).contains(v)) {
					Point p1 = coordinates.get(u);
					Point p2 = coordinates.get(v);
					double x = (p1.x + p2.x)/2;
					double y = (p1.y + p2.y)/2;
					im.drawDiamond((int)x,(int)y,c, c);
					}
			}
		}
		System.out.println("Image construction done");
		System.out.println();
		
	
	}

	
	public void showRikudo() {
		new ImageViewer(im, "Rikudo");
	}
	
	
	public static void main(String[] args) {
		
		/*
		BinaryImage im = new BinaryImage(800);
		im.fillAreaBlack(200, 200, 400);
		
		GraphicRikudo riku = new GraphicRikudo(im, 400, 400, 100);
		
		riku.showRikudo();
		*/
		
		BinaryImage imX = new BinaryImage("X.png");
		GraphicRikudo rikuX = new GraphicRikudo(imX, 40, 40, 30, 1000);
				
		rikuX.showRikudo();
		
		new ImageViewer(imX);
		
	}

}