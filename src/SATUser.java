import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
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
			solver.addClause(new VecInt(new int[] {1}));
		} catch (ContradictionException e1) {
			e1.printStackTrace();
		}
		// Print parameters of the problem
		System.out.println("Number of variables: " + solver.nVars());
		System.out.println("Number of constraints: " + solver.nConstraints());
		// Solve the problem
		try {
			if (solver.isSatisfiable()) {
				System.out.println("Satisfiable problem!");
				int[] solution = solver.model();
				System.out.println("Solution: " + solution[0] + " " + solution[1]);
			} else {
				System.out.println("Unsatisfiable problem!");
			}
		} catch (TimeoutException e) {
			System.out.println("Timeout, sorry!");
		}
	}
}
