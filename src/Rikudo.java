import java.util.ArrayList;
import java.util.Arrays;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;
import org.sat4j.tools.SolutionCounter;

public class Rikudo {
	
	private Graph graph;
	private ArrayList<ArrayList<Integer>> diamonds;
	private int[] partialMap;
	
	Rikudo(Graph g, ArrayList<ArrayList<Integer>> d, int[] pm) {
		graph = g;
		diamonds = d;
		partialMap = pm;
	}
	
	
	// Returns the ISolver object associated to the Rikudo object
	
	public ISolver solverBuilder() {
		
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
						
						// Case i = 0 and i = n - 1 need to be treated independantly because here there might not be such a v1 or v2 do go with
						
						// Case i = 0
						for (int v2 = 0; v2 < n; v2 ++) {
							if (v2 != v && graph.neighbors(u).contains(v2)) {
								solver.addClause(new VecInt(new int[] {- (0 + n*u + 1), - (1 + n*v2 + 1)}));
							}
						}
						
						// Case i = n - 1
						for (int v1 = 0; v1 < n; v1 ++) {
							if (v1 != v && graph.neighbors(v1).contains(u)) {
								solver.addClause(new VecInt(new int[] {- (n - 2 + n*v1 + 1), - (n - 1 + n*u + 1)}));
							}
						}
						
						// Case 0 < i < n - 1
						for (int v1 = 0; v1 < n; v1 ++) {
							if (v1 != v && graph.neighbors(v1).contains(u)) {
								for (int v2 = 0; v2 < n; v2 ++) {
									if (v2 != v1 && v2 != v && graph.neighbors(u).contains(v2)) {
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
		
		// in case we get the exception for a trivially unsatisfiable solver
		// we create a new unsatisfiable solver to return that won't be detected as trivially unsatisfiable
		} catch (ContradictionException e1) {
			System.out.println("Trivially Unsatisfiable");
			
			ISolver fakeSolver = SolverFactory.newDefault();
			try {
				fakeSolver.addClause(new VecInt(new int[] {1, 2}));
				fakeSolver.addClause(new VecInt(new int[] {-1, -2}));
				fakeSolver.addClause(new VecInt(new int[] {-1, 2}));
				fakeSolver.addClause(new VecInt(new int[] {1, -2}));
			} catch (ContradictionException e2) {
				System.out.println("THIS IS NOT SUPPOSED TO HAPPEN ! IF IT DOES, CALL FOR HELP !");
			}
			return fakeSolver;
		}
		
		return solver;
		
	}
	
	
	public int[] solveSAT() {
		
		ISolver solver = solverBuilder();
		int n = graph.vertexNumber();
		
		try {
			
			if (solver.isSatisfiable()) {
				
				System.out.println("Solution Found");
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
				System.out.println("There is no solution");
				return new int[] {-1};
			}
			
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
			return new int[] {-1};
		}
		
	}
	
	
	public long numberOfSolution() {
		ISolver solver = solverBuilder();
		try {
			if (solver.isSatisfiable()) {
				SolutionCounter sc = new SolutionCounter(solver);
				return sc.countSolutions();	
			} else {
				return 0;
			}
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
			return 0;
		}
	}
	
	
	
	public void isGood() {
		
		ISolver solver = solverBuilder();
		int n = graph.vertexNumber();
		
		try {
			
			if (solver.isSatisfiable()) {
				
				// Providing the number of solution
				SolutionCounter sc = new SolutionCounter(solver);
				long nbSol = sc.countSolutions();
				System.out.println("Number of Solutions: " + nbSol);
				
				// If there is only one solution, we check that removing constraints always leads to more solutions (we check minimality)
				if (nbSol == 1) {
					
					// Removing constraints on the partial mapping
					// We iterate for 0 < k < n - 1 since we can't remove the constraints on the starting and finishing points 
					for (int k = 1; k < n - 1; k ++) {
						if (partialMap[k] != -1) {
							int mem = partialMap[k];
							partialMap[k] = -1;
							if (numberOfSolution() == 1) {
								System.out.println("The problem is not minimal: removing condition " + k + " on the partial mapping still yields only one solution");
								partialMap[k] = mem;
								return;
							}
							partialMap[k] = mem;
						}
					}
					
					// Removing constraints on diamonds
					for (int u = 0; u < n; u ++) {
						for (int v : diamonds.get(u)) {
							if (v > u) {
								diamonds.get(u).remove(new Integer(v));
								if (numberOfSolution() == 1) {
									System.out.println("The problem is not minimal: removing the diamond (" + u + ", " + v + ") still yields only one solution");
									diamonds.get(u).add(v);
									return;
								}
								diamonds.get(u).add(v);
							}
						}
					}
					
					System.out.println("The Problem is minimal");
					
					
				}
				
			} else {
				System.out.println("Number of Solutions: 0");
			}
			
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
		}
		
	}
	
	
	
	public static void main(String[] args) {
		
		Graph g = Graph.gridGraph(3);
		int[] pm = new int[] {0, 3, -1, -1, -1, -1, -1, -1, 8};
		
		ArrayList<ArrayList<Integer>> d = new ArrayList<ArrayList<Integer>>();
		
		d.add(new ArrayList<Integer>(Arrays.asList(3)));
		for (int k = 0; k < 2; k ++) {d.add(new ArrayList<Integer>());};
		d.add(new ArrayList<Integer>(Arrays.asList(0)));
		for (int k = 0; k < 5; k ++) {d.add(new ArrayList<Integer>());};
		
		Rikudo riku = new Rikudo(g, d, pm);
		
		System.out.print("Partial Mapping: ");
		System.out.println(Arrays.toString(pm));
		System.out.print("Diamonds: ");
		System.out.println(d);
		System.out.println();
		
		riku.solveSAT();
		System.out.println();

		riku.isGood();
		
	}
	
}
