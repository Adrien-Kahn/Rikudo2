import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
	
	
	//Task 3 : SAT Solving
	
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
	
	
	//Task 3 : exploring
	
	private void exploring(int s, int t, int[] explored, ArrayList<Integer> path, int n) {
		// we begin to see if there are any constraints that need to be fulfilled during the iteration
		
		ArrayList<Integer> neigh = graph.neighbors(s);
		int i = path.size();
		
		if (i == n-1) {					// t must be the end of the path : for the rest of the algorithm we can assume that we haven't reached the end of the path
			if (neigh.contains(t)){
				explored[t] = 1;
				path.add(t);
			}
			return;
		}
		
		if (partialMap[i] != -1) { 			// we verify whether the lambda constraint can be fulfilled and continue exploring if so
			int v = partialMap[i];
			if (neigh.contains(v)) {
				explored[v] = 1;
				path.add(v);
				exploring(v,t,explored,path,n);
				if (path.size() != n) {
					explored[v] = 0;
					path.remove(path.size()-1);	
				}
			}
			return;
		}
		
		
		ArrayList<Integer> d = diamonds.get(s);  // a vertex can appear at most twice in the set of diamonds, or the problem is unsolvable
		
		if (d.size() == 2) {				// if the vertex is part of two diamonds, then we know that its predecessor and successor are one of two values
			int v1 = d.get(0);
			int v2 = d.get(1);
			if (v1 == t || v2 == t) return; 				// impossible to fulfill because it means t needs to be a predecessor or a successor of the vertex and we haven't reached the end yet
			if (explored[v1] == 0 && explored[v2] == 1) {
				explored[v1] = 1;
				path.add(v1);
				exploring(v1,t,explored,path,n);
				if (path.size() != n) {
					explored[v1] = 0;
					path.remove(path.size()-1);	
				}
			}
			if (explored[v2] == 0 && explored[v1] == 1) {
				explored[v2] = 1;
				path.add(v2);
				exploring(v2,t,explored,path,n);	
				if (path.size() != n) {
					explored[v2] = 0;
					path.remove(path.size()-1);	
				}
			}
			return;
		}
		
		
		if (d.size() == 1) { 					// if we don't come from the other vertex of the diamond, then we necessarily need to go there
			int v1 = d.get(0);
			if (v1 == t) return;					// unsatisfiable because t is either a predecessor or a successor and we haven't reached the end yet
			if (explored[v1] == 1 && path.get(i-1) != v1) {
				return;
			}
			if (explored[v1] == 0) {
				explored[v1] = 1;
				path.add(v1);
				exploring(v1,t,explored,path,n);
				if (path.size() != n) {
					explored[v1] = 0;
					path.remove(path.size()-1);	
				}
				return;
			}
		}
	
		// if none of the constraints condition our exploration, we use the general exploring method by looking at each of the unexplored neighbors
		
		
		for (int v : neigh) {
			
			if (v != t && explored[v] == 0) {		// we haven't reached the end yet : we can't choose target to explore																
				explored[v] = 1;
				path.add(v);
				exploring(v,t,explored,path,n);
				if (path.size() == n) break; // determines whether the path + v can be completed into an hamiltonian path
				explored[v] = 0; 
				path.remove(path.size()-1);
			}
			
		}
	}
	
	public ArrayList<Integer> solveBacktracking(int s, int t){
		
		// initialization
		int n = graph.vertexNumber();
		int[] explored = new int[n+1];
		explored[s] = 1;
		ArrayList<Integer> path = new ArrayList<Integer>();
		path.add(s);
		
		// exploring the graph with the auxiliary method
		exploring(s,t, explored, path, n);
		
		//result
		if (path.size() == n) {System.out.println("Congrats you have found a solution"); System.out.println(path.toString());return path;}
		System.out.println("No solution here sir"); return new ArrayList<Integer>();
	}

	
	
	//Task 4
	
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
	
	
	
	public boolean isGood() {
		
		int n = graph.vertexNumber();
		long nbSol = numberOfSolution();
		System.out.println("Number of Solutions: " + nbSol);
			
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
						return(false);				// a lambda condition can be removed
					}
					partialMap[k] = mem;
				}
			}
					
			// Removing constraints on diamonds
			for (int u = 0; u < n; u ++) {
				for (int v : new ArrayList<>(diamonds.get(u))) {
					if (v > u) {
						diamonds.get(u).remove((Object)v);
						diamonds.get(v).remove((Object)u);
						if (numberOfSolution() == 1) {
							System.out.println("The problem is not minimal: removing the diamond (" + u + ", " + v + ") still yields only one solution");
							diamonds.get(u).add(v);
							diamonds.get(v).add(u);
								return(false);  		// a diamond condition can be removed
						}
						diamonds.get(u).add(v);
						diamonds.get(v).add(u);
					}
				}
			}
					
			System.out.println("The Problem is minimal");
			return(true);			//the problem is minimal
			
		}
		return(false);   			// there are more than one solution or no solutions
				
	}
	
	
	// Task 5
	
	private void makesGood() { // removes unnecessary conditions to a rikudo with a unique solution	: it functions similarily to isGood with a few differences
		int n = graph.vertexNumber();
		
		for (int k = 1; k < n - 1; k ++) {
			
			if (partialMap[k] != -1) {
				int mem = partialMap[k];
				partialMap[k] = -1;
				if (numberOfSolution() != 1) {
					partialMap[k] = mem;
				}
			}
		}
				
		// Removing constraints on diamonds
		for (int u = 0; u < n; u ++) {
			for (int v : new ArrayList<>(diamonds.get(u))) {
				if (v > u) {
					diamonds.get(u).remove((Object) v);
					diamonds.get(v).remove((Object)u);
					if (numberOfSolution() != 1) {
						diamonds.get(u).add(v);
						diamonds.get(v).add(u);
					}

				}
			}
		}
	}
	
	
	public static Rikudo createRikudo (Graph g, int s, int t) {
		// Definition of the variables
		ArrayList<Integer> path = g.hamiltonianBacktracking(s, t);
		int n = g.vertexNumber();
		int[] lambda = new int[n];
		lambda[0] = s;
		lambda[n-1] = t;
		ArrayList<ArrayList<Integer>> diamonds = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < n; i++) {
			lambda[i] = -1;
			diamonds.add(new ArrayList<Integer>());	
		}
		lambda[0] = s;
		lambda[n-1] = t;
		Rikudo riku = new Rikudo(g, diamonds, lambda);
		
		// Putting constraints until our path is the unique solution
		Random r = new Random();
		
		long nbSol = riku.numberOfSolution();

		while (nbSol != 1) {
			if (r.nextInt(2) == 0) {      // we add a lambda constraint
				int i = r.nextInt(n);
				if (riku.partialMap[i] == -1) {
					riku.partialMap[i] = path.get(i);
					nbSol = riku.numberOfSolution();
				}
			}
			else {						// we add a diamond constraint
				int i = r.nextInt(n-1);
				int v1 = path.get(i);
				int v2 = path.get(i+1);
				if (!riku.diamonds.get(v1).contains(v2)) {
					riku.diamonds.get(v1).add(v2);
					riku.diamonds.get(v2).add(v1);
					nbSol = riku.numberOfSolution();
				}
			}
			
		}
		
		
		// Removing solutions until the information is minimal
		
		riku.makesGood();
		
		
		// returning a minimal rikudo puzzle
		return(riku);
		
		//
	}
	
	
	
	
	
	
	public static void main(String[] args) {
		
		/*
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
		
		Graph gg2 = Graph.gridGraph(3);
				
		ArrayList<ArrayList<Integer>> E = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> diamond1 = new ArrayList<Integer>();
		ArrayList<Integer> diamond2 = new ArrayList<Integer>();
		diamond1.add(8);
		diamond2.add(5);

		for (int i = 0; i<5 ; i++) {E.add(new ArrayList<Integer>());}

		E.add(diamond1);
		E.add(new ArrayList<Integer>());
		E.add(new ArrayList<Integer>());
		E.add(diamond2);
		
		
		int[] lambda = new int[9];
		for (int i = 0; i < 9; i++) {lambda[i] = -1; }
		lambda[3] = 7;
		
		Rikudo riku2 = new Rikudo(gg2, E, lambda);
		
		riku2.solveBacktracking(0, 8);
		*/
		
		Graph gg2 = Graph.gridGraph(3);
		
		Rikudo riku = createRikudo(gg2, 0, 8);
		
		System.out.print("Partial Mapping: ");
		System.out.println(Arrays.toString(riku.partialMap));
		System.out.print("Diamonds: ");
		System.out.println(riku.diamonds);
		
		

		
	}

}