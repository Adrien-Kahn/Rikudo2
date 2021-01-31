import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.tools.SolutionCounter;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.TimeoutException;

public class SATUser {

	public static void main(String[] args) {
		
		
		// Create the solver 
		ISolver solver = SolverFactory.newDefault();
		// Feed the solver using arrays of int in Dimacs format
		try {
			solver.addClause(new VecInt(new int[] {1, 2}));
			solver.addClause(new VecInt(new int[] {-1, -2}));
			solver.addClause(new VecInt(new int[] {-1, 3}));
			solver.addClause(new VecInt(new int[] {1, -3}));
			solver.addClause(new VecInt(new int[] {1, 3}));
			solver.addClause(new VecInt(new int[] {-1, -3}));
		} catch (ContradictionException e1) {
			System.out.println("blblbllb!");
		}
		
		// Solve the problem
		try {
			if (solver.isSatisfiable()) {
				
				SolutionCounter sc = new SolutionCounter(solver);
				System.out.println("Satisfiable problem!");
				System.out.println("Number of solutions:");
				int[] solution = solver.model();
				System.out.println(sc.countSolutions());
				System.out.println(solution);
				
				for (int k = 0; k < solution.length; k ++) {
					System.out.print(solution[k]);
					System.out.print(", ");
				}

			} else {
				System.out.println("Unsatisfiable problem!");
			}
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
		}
	}
}