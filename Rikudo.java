import java.util.ArrayList;
import java.util.Arrays;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class Rikudo {
	
	private Graph graph;
	private ArrayList<ArrayList<Integer>> diamonds;
	private int[] partialMap;
	
	Rikudo(Graph g, ArrayList<ArrayList<Integer>> d, int[] pm) {
		graph = g;
		diamonds = d;
		partialMap = pm;
	}
	
	
	public int[] solveSAT() {
		
		int n = graph.vertexNumber();
		ISolver solver = SolverFactory.newDefault();
		
		try {
			
			// The partial mapping is respected
			
			for (int i = 0; i < n; i ++) {
				if (partialMap[i] != -1) {
					solver.addClause(new VecInt(new int[] {i + n*partialMap[i] + 1}));
				}
			}
			
			// Each vertex appears AT LEAST ONCE in the path
			for (int v = 0; v < n; v ++) {
				int[] a = new int[n];
				for (int i = 0; i < n; i ++) {
					a[i] = i + n*v + 1;
				}
				solver.addClause(new VecInt(a));
			}
			
			// Each vertex appears NO MORE THAN ONCE in the path
			for (int v = 0; v < n; v ++) {
				for (int i = 0; i < n; i ++) {
					for (int j = i + 1; j < n; j ++) {
						solver.addClause(new VecInt(new int[] {- (i + n*v + 1), - (j + n*v + 1)}));
					}
				}
			}
			
			// Each index in the path is occupied by AT LEAST ONE vertex
			for (int i = 0; i < n; i ++) {
				int[] a = new int[n];
				for (int v = 0; v < n; v ++) {
					a[v] = i + n*v + 1;
				}
				solver.addClause(new VecInt(a));
			}
			
			// Each index is occupied by NO MORE THAN ONE vertex
			for (int i = 0; i < n; i ++) {
				for (int v = 0; v < n; v ++) {
					for (int u = v + 1; u < n; u ++) {
						solver.addClause(new VecInt(new int[] {- (i + n*v + 1), - (i + n*u + 1)}));
					}
				}
			}
			
			// Consecutive vertices in the path are adjacent in the graph
			for (int i = 0; i < n - 1; i ++) {
				for (int v = 0; v < n; v ++) {
					for (int u = 0; u < n; u ++) {
						if (!graph.neighbors(u).contains(v)) {
							solver.addClause(new VecInt(new int[] {- (i + n*u + 1), - (i + 1 + n*v + 1)}));
						}
					}
				}
			}
			
			// All the diamonds have edges passing through them
			// In order to go only once over each pair (u,v) in diamonds, we consider only (u,v) with u < v
			for (int u = 0; u < n; u ++) {
				for (int v : diamonds.get(u)) {
					if (v > u) {
						for (int v1 = 0; v1 < n; v1 ++) {
							if (v1 != v && graph.neighbors(v1).contains(u)) {
								for (int v2 = 0; v2 < n; v2 ++) {
									if (v2 != v1 && v2 != v && graph.neighbors(u).contains(v2)) {
										
										// Case i = 0
										solver.addClause(new VecInt(new int[] {- (0 + n*u + 1), - (1 + n*v2 + 1)}));
										
										// Case i = n - 1
										solver.addClause(new VecInt(new int[] {- (n - 2 + n*v1 + 1), - (n - 1 + n*u + 1)}));
										
										// Case 0 < i < n - 1
										for (int i = 1; i < n - 1; i ++) {
											solver.addClause(new VecInt(new int[] {- (i - 1 + n*v1 + 1), - (i + n*u + 1), - (i + 1 + n*v2 + 1)}));
										}
										
									}
								}
							}
						}
					}
				}
			}
			
		} catch (ContradictionException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Number of variables: " + solver.nVars());
		System.out.println("Number of constraints: " + solver.nConstraints());
		
		try {
			if (solver.isSatisfiable()) {
				
				System.out.println("Hamiltonian Path Found");
				
				int[] solution = solver.model();				
				int[] path = new int[n];

				for (int k = 0; k < n*n; k ++) {
					if (solution[k] > 0) {
						path[(solution[k] - 1) % n] = (solution[k] - 1) / n;
					}
				}
				
				System.out.println(Arrays.toString(path));
				return path;
				
			} else {
				System.out.println("Unsatisfiable problem!");
				return new int[] {-1};
			}
			
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
			return new int[] {-1};
		}
		
	}
	
	
	public static void main(String[] args) {
		
		Graph g = Graph.gridGraph(3);
		int[] pm = new int[] {0, -1, -1, -1, -1, -1, -1, -1, 8};
		
		ArrayList<ArrayList<Integer>> d = new ArrayList<ArrayList<Integer>>();
		
		for (int k = 0; k < 3; k ++) {d.add(new ArrayList<Integer>());};
		d.add(new ArrayList<Integer>(Arrays.asList(6)));
		for (int k = 0; k < 2; k ++) {d.add(new ArrayList<Integer>());};
		d.add(new ArrayList<Integer>(Arrays.asList(3)));
		for (int k = 0; k < 2; k ++) {d.add(new ArrayList<Integer>());};
		
		Rikudo riku = new Rikudo(g, d, pm);
		
		
		for (int k = 0; k < pm.length; k ++) {System.out.print(pm[k]);};
		System.out.println(d);
		
		
		riku.solveSAT();
		
	}
	
}
