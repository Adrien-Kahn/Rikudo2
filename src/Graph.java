
import java.util.ArrayList;
import java.util.Arrays;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class Graph {
	
	private ArrayList<ArrayList<Integer>> adjacencyList;
	
	Graph(ArrayList<ArrayList<Integer>> al) {
		adjacencyList = al;
	}
	
	public int vertexNumber() {
		return adjacencyList.size();
	}
	
	public ArrayList<Integer> neighbors(int v) {
		return adjacencyList.get(v);
	}
	
	
	
	
	// Task 1
	
	// x_i,v (the i-th vertex in the path is v) is represented by the integer i + n*v + 1 because for some reason sat4j can't deal with 0
	// Well, actually it makes sense since we couldn't differentiate between 0 and -0
	// Therefore, x_k = x_i,v where i = (k - 1) % n and v = (k - 1) // n
	
	public ISolver solverBuilder(int s, int t) {
		
		int n = vertexNumber();
		ISolver solver = SolverFactory.newDefault();
		
		try {
			
			// Each vertex appears exactly once in the path
			for (int v = 0; v < n; v ++) {
				int[] a = new int[n];
				for (int i = 0; i < n; i ++) {
					a[i] = i + n*v + 1;
				}
				solver.addExactly(new VecInt(a), 1);
			}
			
			// Each index appears exactly once in the path
			for (int i = 0; i < n; i ++) {
				int[] a = new int[n];
				for (int v = 0; v < n; v ++) {
					a[v] = i + n*v + 1;
				}
				solver.addExactly(new VecInt(a), 1);
			}
			
			// Consecutive vertices in the path are adjacent in the graph
			for (int i = 0; i < n - 1; i ++) {
				for (int v = 0; v < n; v ++) {
					for (int u = 0; u < n; u ++) {
						if (!neighbors(u).contains(v)) {
							solver.addClause(new VecInt(new int[] {- (i + n*u + 1), - (i + 1 + n*v + 1)}));
						}
					}
				}
			}
			
			// The first vertex is s
			solver.addClause(new VecInt(new int[] {0 + n*s + 1}));
			
			// The last vertex is t
			solver.addClause(new VecInt(new int[] {n - 1 + n*t + 1}));
		
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
	
	public int[] hamiltonianPath(int s, int t) {
		
		int n = vertexNumber();
		ISolver solver = solverBuilder(s,t);
		
		System.out.println("Number of variables: " + solver.nVars());
		System.out.println("Number of constraints: " + solver.nConstraints());
		
		try {
			if (solver.isSatisfiable()) {
				
				System.out.println("Satisfiable problem!");
				
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
	
	
	public int[] hamiltonianPathOld(int s, int t) {
		
		int n = vertexNumber();
		ISolver solver = SolverFactory.newDefault();
		
		try {
			
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
						if (!neighbors(u).contains(v)) {
							solver.addClause(new VecInt(new int[] {- (i + n*u + 1), - (i + 1 + n*v + 1)}));
						}
					}
				}
			}
			
			// The first vertex is s
			solver.addClause(new VecInt(new int[] {0 + n*s + 1}));
			
			// The last vertex is t
			solver.addClause(new VecInt(new int[] {n - 1 + n*t + 1}));
			
		} catch (ContradictionException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("Number of variables: " + solver.nVars());
		System.out.println("Number of constraints: " + solver.nConstraints());
		
		try {
			if (solver.isSatisfiable()) {
				
				System.out.println("Satisfiable problem!");
				
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
	
	
	
	// Task 2
	
	
	private void exploring(int s, int t, int[] explored, ArrayList<Integer> path, int n) { 	// auxiliary recursive method used to explore the graph in search of an hamiltonian path. s is the last vertex added to the path, t is the goal.
		ArrayList<Integer> neigh = neighbors(s);
		for (int v : neigh) {
			
			if (v == t) {														
				if (path.size() == n-1) {explored[t] = 1; path.add(t); break;} // t is necessarily the last vertex explored
			}
			
			else {if (explored[v] == 0) {																		
				explored[v] = 1;
				path.add(v);
				exploring(v,t,explored,path,n);
				if (path.size() == n) break; // determines whether the path + v can be completed into an hamiltonian path
				explored[v] = 0; 
				path.remove(path.size()-1);
			}}
			
		}
	}
	
	public ArrayList<Integer> hamiltonianBacktracking(int s, int t){
		
		// initialization
		int n = vertexNumber();
		int[] explored = new int[n];
		explored[s] = 1;
		ArrayList<Integer> path = new ArrayList<Integer>();
		path.add(s);
		
		// exploring the graph with the auxiliary method
		exploring(s,t, explored, path, n);
		
		//result
		if (path.size() == n) {System.out.println("Congrats you have found a hamiltonian path"); System.out.println(path.toString());return path;}
		System.out.println("No hamiltonian path here sir"); return new ArrayList<Integer>();
	}
	
	// Remark 2 : variation 1, counting hamiltonian paths
	
	private void exploring_counting(int s, int t, int[] explored, ArrayList<Integer> path, int n, ArrayList<ArrayList<Integer>> mem) { 
		ArrayList<Integer> neigh = neighbors(s);
		for (int v : neigh) {
			
			if (v == t) {
				if (path.size() == n-1) {
				explored[t] = 1; 
				path.add(t);
				mem.add((ArrayList<Integer>)path.clone());
				explored[t] = 0; 
				path.remove(path.size()-1);
			}}
			
			else {if (explored[v] == 0) {	
				explored[v] = 1;
				path.add(v);
				exploring_counting(v,t,explored,path,n,mem);
				explored[v] = 0; 
				path.remove(path.size()-1);
			}}
			
		}	
	}
	
	public ArrayList<ArrayList<Integer>> hamiltonianBacktracking_counting(int s, int t){
		
		// initialization
		int n = vertexNumber();
		int[] explored = new int[n];
		explored[s] = 1;
		ArrayList<Integer> path = new ArrayList<Integer>();
		path.add(s);
		ArrayList<ArrayList<Integer>> mem = new ArrayList<ArrayList<Integer>>();
		
		// exploring the graph with the auxiliary method
		exploring_counting(s,t, explored, path, n, mem );
		
		//result
		System.out.println("Congrats you have found " + mem.size()  + " hamiltonian path"); 
		System.out.println(mem);
		return mem;
	}
	
	
	// Remark 2 : variation 2 : return a hamiltonian path if there are more than a certain number of them
	
	private void exploring_k(int s, int t, int k, int[] explored, ArrayList<Integer> path, int n, ArrayList<ArrayList<Integer>> mem) { 
		ArrayList<Integer> neigh = neighbors(s);
		for (int v : neigh) {
			
			if (v == t) {
				if (path.size() == n-1) {
				explored[t] = 1; 
				path.add(t);
				mem.add((ArrayList<Integer>)path.clone());
				if (mem.size() == k) break;
				explored[t] = 0; 
				path.remove(path.size()-1);
			}}
			
			else {if (explored[v] == 0) {	
				explored[v] = 1;
				path.add(v);
				exploring_k(v,t,k, explored,path,n,mem);
				explored[v] = 0; 
				path.remove(path.size()-1);
			}}
			
		}	
	}
	
	public ArrayList<Integer> hamiltonianBacktracking_k(int s, int t,int k){
		
		// initialization
		int n = vertexNumber();
		int[] explored = new int[n];
		explored[s] = 1;
		ArrayList<Integer> path = new ArrayList<Integer>();
		path.add(s);
		ArrayList<ArrayList<Integer>> mem = new ArrayList<ArrayList<Integer>>();
		// exploring the graph with the auxiliary method
		
		exploring_k(s,t,k,explored, path, n, mem );
		
		//result
		if (mem.size() == k) {
			System.out.println("There are more than " + k + " hamiltonian paths. Here is one example :");
			System.out.println(mem.get(0));
			return mem.get(0);
		}
		else {
			System.out.println("There are less than " + k + " hamiltonian paths");
			return new ArrayList<Integer>();
		}
	}

	
	
	// Useful graphs
	
	
	public static Graph completeGraph(int n) {
		ArrayList<ArrayList<Integer>> al = new ArrayList<ArrayList<Integer>>();
		for (int k = 0; k < n; k ++) {
			ArrayList<Integer> a = new ArrayList<Integer>();
			for (int i = 0; i < n; i ++) {
				if (i!=k) a.add(i); 
			}
			al.add(a);
		}
		return new Graph(al);
	}
	
	
	public static Graph cycleGraph(int n) {
		ArrayList<ArrayList<Integer>> al = new ArrayList<ArrayList<Integer>>();
		for (int k = 0; k < n; k ++) {
			ArrayList<Integer> a = new ArrayList<Integer>();
			a.add((k + 1) % n);
			al.add(a);
		}
		return new Graph(al);
	}
	
	public static Graph gridGraph(int n) {
		ArrayList<ArrayList<Integer>> al = new ArrayList<ArrayList<Integer>>();
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				int k = n*i +j;
				ArrayList<Integer> a = new ArrayList<Integer>();
				if (i > 0) a.add(k-n);
				if (j > 0) a.add(k-1);
				if (j < n-1) a.add(k+1);
				if (i < n-1) a.add(k+n);
				al.add(a);
			}
			
		}
		return new Graph(al);
	}
	
	
	
	
	public static void main(String[] args) {
		
		/*
		ArrayList<Integer> l0 = new ArrayList<Integer>(Arrays.asList(3));
		ArrayList<Integer> l1 = new ArrayList<Integer>(Arrays.asList(0, 2));
		ArrayList<Integer> l2 = new ArrayList<Integer>(Arrays.asList(0, 3));
		ArrayList<Integer> l3 = new ArrayList<Integer>(Arrays.asList(1, 4));
		ArrayList<Integer> l4 = new ArrayList<Integer>(Arrays.asList(0));
		
		ArrayList<ArrayList<Integer>> al  = new ArrayList<ArrayList<Integer>>(Arrays.asList(l0, l1, l2, l3, l4));
		
		System.out.println(al);
		
		Graph g = new Graph(al);
		
		g.hamiltonianPath(1, 0);
		*/
		
		/*
		Graph cg = completeGraph(100);
		System.out.println(cg.adjacencyList);
		cg.hamiltonianPath(5, 8);
		cg.hamiltonianBacktracking(7,6);


		Graph cyg = cycleGraph(100);
		System.out.println(cyg.adjacencyList);
		cyg.hamiltonianPath(7, 6);
		cyg.hamiltonianBacktracking(7, 6);
		
		
		Graph gg1 = gridGraph(4);
		System.out.println(gg1.adjacencyList);
		gg1.hamiltonianBacktracking(0,15);
		gg1.hamiltonianBacktracking_counting(1,15);
		
		Graph gg2 = gridGraph(3);
		gg2.hamiltonianBacktracking(0,8);
		gg2.hamiltonianBacktracking_counting(0,8);
		
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
		
		gg2.hamiltonianBacktracking_rikudo(0, 8, lambda, E);

		*/
		
		/*
		Graph g4 = gridGraph(4);
		g4.hamiltonianPathOld(0, 1);
		*/
		
		
		
		// Evaluate computation time
		
		
		/*
		System.out.println("Start");
				
		ArrayList<Integer> x = new ArrayList<Integer>();
		ArrayList<Double> y = new ArrayList<Double>();
		
		for (int k = 1; k < 8; k ++) {
			
			x.add(k*k);
			
			Graph cg = gridGraph(k);
			long t = System.currentTimeMillis();
			cg.hamiltonianPath(1, 0);
			y.add((System.currentTimeMillis() - t)/1000.);
			
		}
				
		System.out.println(x);
		System.out.println(y);
		*/
		
		
		Graph cg = gridGraph(7);
		long t = System.currentTimeMillis();
		cg.hamiltonianPath(1, 0);
		System.out.println((System.currentTimeMillis() - t)/1000.);
		
		
		

		

		/*
		Graph gg2 = gridGraph(3);
		gg2.hamiltonianBacktracking(0,8);
		gg2.hamiltonianBacktracking_counting(0,8);
		*/
	}

}
