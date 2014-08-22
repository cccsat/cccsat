import java.util.ArrayList;
import java.util.List;
import java.io.*;
/**
 * Count Clear Clauses SAT Solver, Java version, v1.0
 * Free for any purpose, but please cite our contribution.
 * See also: Solving SAT by an Iterative Version of the Inclusion-Exclusion Principle
 */
//serves as the global access point for number of bits
class global {
	public static int numOfBits = 62; // may not be bigger than 62
}
//this is the main class, it calls the solver
public class CCC_v1Dot0 {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		long endTime;
		System.out.println("SATCounter: Count Clear Clauses SAT Solver");
		if (args.length != 1) {
			System.out.println("Start it with a DIMACS file as a parameter.");
			System.out.println("The DIMACS file may not contain more than "+ global.numOfBits + " variables.");
		} else {
			String fileName = args[0];
			ClauseSet s = new ClauseSet(fileName);
			Clause solution = SATCounter.SolveFrom0(s); // it calls the solver
			if (solution == null) {
				endTime = System.currentTimeMillis();
				System.out.println("Unsatisfiable!");
			} else {
				endTime = System.currentTimeMillis();
				System.out.println("Satisfiable! A solution is:");
				System.out.println(solution);
			}
			System.out.println("CPU time is " + (endTime - startTime) / 1000.0f + " s");
		}
	}
}
/**
 * This class implements the optimized CCC algorithm.
 */
class SATCounter {
	// This function implements the optimized CCC algorithm.
	public static Clause SolveFrom0(ClauseSet s) {
		long counter = 0L;
		List<Clause> subsumers = s.SubsumersOf(Clause.ToClause(counter));
		while (!subsumers.isEmpty()) {
			byte maxNumberOfX = 0;
			for (Clause subsumer : subsumers) { // selects the best subsumer
				byte currentNumberOfX = subsumer.NumberOfXFromRight();
				if (currentNumberOfX > maxNumberOfX) maxNumberOfX = currentNumberOfX;
			}
			counter += 1L << maxNumberOfX;
			if (counter > (1L << global.numOfBits)) return null; // there is no solution
			subsumers = s.SubsumersOf(Clause.ToClause(counter));
		}
		return Clause.ToClause(counter).Negate(); // solution is found, we return it
	}
}
/**
 * Represents a set of clause.
 */
class ClauseSet {
	List<Clause> clauses;
	// reads a DIMACS file
	public ClauseSet(String fileName) {
		clauses = new ArrayList<Clause>();
		try {
			BufferedReader file = new BufferedReader(new FileReader(fileName));
			String clause = file.readLine();
			while (clause != null) {
				if (clause.length() > 0 &&
					clause.charAt(0) != '0' && 
					clause.charAt(0) != 'c' &&
					clause.charAt(0) != 'p' &&
					clause.charAt(0) != '%') AddCNFClause(clause);
				clause = file.readLine();
			}
			file.close();
		} catch (IOException e) { System.out.println(e); System.exit(-1);}
	}
	// it adds one line of the DIMACS, which represents a clause, to the clause set
	public void AddCNFClause(String cnfClause) {
		int i = 0;
		Clause c = new Clause();
		while (i < cnfClause.length()) {
			while (cnfClause.charAt(i) == ' ') i++;
			String lit = "";
			while (i < cnfClause.length() && cnfClause.charAt(i) != ' ') {
				lit += cnfClause.charAt(i);
				i++;
			}
			int literal = Integer.parseInt(lit);
			if (literal == 0) break;
			c.SetLiteralZeroBased(literal);
		}
		clauses.add(c);
	}
	// it returns those clauses which subsume the input clause
	public List<Clause> SubsumersOf(Clause c) {
		ArrayList<Clause> subsumers = new ArrayList<Clause>();
		for (Clause clause : clauses) {
			if (clause.Subsumes(c)){ subsumers.add(clause); }
		}
		return subsumers;
	}
}
/**
 * This class represents a clause, a set of literals.
 * Two long, mask and bits, represent one clause as follows: 
 * If the i.-th bit in mask is 0, then the i.-th variable is not present in the clause.
 * If the i.-th bit in mask is 1, and in bits is 0, then the i.-th variable is present as a negative literal.
 * If the i.-th bit in mask is 1, and in bits is 1, then the i.-th variable is present as a positive literal.
 * In a shorter form (mask,bits:literal): (0,0: X), (1,0: -), (1,1:+). The (0,1) combination is not used.
 */
class Clause {
	long mask, bits; // represent a clause
	// a new clause is empty
	public Clause() { mask = 0; bits = 0; }
	// sets a literal, if literal is -5, it means we have to set the 5.-th variable to be negative.
	public void SetLiteralZeroBased(int literal) {
		int index = global.numOfBits - Math.abs(literal);
		long indexMask1 = 1L << index;
		mask = mask | indexMask1;
		if (literal > 0) bits = bits | indexMask1;
	}
	// negate a clause
	public Clause Negate() {
		Clause c = new Clause();
		c.mask = this.mask;
		c.bits = ~this.bits;
		c.bits = c.bits & c.mask; // because the (0,1) combination of mask and bits is not allowed
		return c;
	}
	// checks whether this subsumes the input clause or not
	public boolean Subsumes(Clause b) {
		if (mask > b.mask) return false;
		long cMask = mask & b.mask;
		return (bits & cMask) == (b.bits & cMask);
	}
	// returns the how many variable are not present after the last literal
	public byte NumberOfXFromRight() {
		byte xCounter = 0;
		long bitMask = 1L;
		for (int i = 0; i < global.numOfBits; i++) {
			if ((mask & bitMask) == 0) xCounter++;
			else break;
			bitMask <<= 1;
		}
		return xCounter;
	}
	// converts a long into clause, it creates always a clear clause
	public static Clause ToClause(long id) {
		Clause c = new Clause();
		c.bits = id;
		c.mask = Long.MAX_VALUE;
		return c;
	}
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		long indexMask = 1L << global.numOfBits - 1;
		for (int i = global.numOfBits - 1; i >= 0; i--) {
			if ((mask & indexMask) > 0) {
				int literal = global.numOfBits - i;
				if ((bits & indexMask) > 0) s.append(literal);
				else s.append(-literal);
				s.append(' ');
			}
			indexMask >>= 1;
		}
		s.append('0');
		return s.toString();
	}
}
