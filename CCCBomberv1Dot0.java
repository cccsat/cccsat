import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.*;
/**
 * Count Clear Clauses Bomber SAT Solver, Java version, v1.0
 * Free for any purpose, but please cite our contribution.
 * See also: Solving SAT by an Iterative Version of the Inclusion-Exclusion Principle/

//this is the main class, it calls the solver
public class CCCBomberv1Dot0 {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		long endTime;
		System.out.println("SATCounter: Count Clear Clauses SAT Solver");
		if (args.length != 1) {
			System.out.println("Start it with a DIMACS file as a parameter.");
			System.out.println("The DIMACS file may not contain more than 62 variables.");
		} else {
			String fileName = args[0];
			ClauseSet s = new ClauseSet(fileName);
			Clause solution = SATCounter.SolveWithBombs(s); // it calls the solver
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
 * This class implements the CCC Bomber algorithm.
 * It starts many counters instead of one.
 * The more bomb the better chance that one of them is lucky, so finds a solution quickly,
 * but the overhead is also bigger.
 * This class is not optimized.
 * Use only for experimenting the algorithm.
 * It has been tested on lot of SAT problem instances, bit still it might contain errors.
 * Change minBombStep, and maxSizeOfBombs to have different number of bombs.
 * Enjoy!
 */
class SATCounter {
	// This function implements the optimized CCC algorithm.
	public static Clause SolveWithBombs(ClauseSet s) {
		ArrayList<Long> bombs = new ArrayList<Long>();
		ArrayList<ClearClause> counters = new ArrayList<ClearClause>();
		int numOfBits = global.getNumOfBits();
		long currentBomb = 0L;
		//System.out.println("toreach: " + (1L << numOfBits));
		//System.out.println("my_bomb: " + currentBomb);
		
		
		// *******************************************
		// change this line to set the number of bombs
		// *******************************************
		int minBombStep = numOfBits - 10;
		
		Random rnd = new Random();
		while(currentBomb < (1L << numOfBits)) {
			bombs.add(currentBomb);
			counters.add(Clause.ToClause(currentBomb));
			
			// *******************************************
			// change this line to set the number of bombs
			// *******************************************
			int maxSizeOfBombs = 4;
			
			int newBomb = rnd.nextInt(maxSizeOfBombs+1)+minBombStep;
			
			
			currentBomb += 1L << newBomb;
			//System.out.println("toreach: " + (1L << numOfBits));
			//System.out.println("my_bomb: " + currentBomb);
		}
		System.out.println("Number of bombs: " + bombs.size());
		// this will stop the last bomb
		bombs.add(1L << numOfBits); 
		
		
		boolean[] solved = new boolean[counters.size()];
		for(int i=0; i<solved.length; i++) { solved[i] = false; }
		int numberOfNotYedSolvedOnes = counters.size();
		while(numberOfNotYedSolvedOnes != 0)
		{
			for (int i=0; i<counters.size();  i++) {
				if (solved[i]) continue;
				//System.out.println("i: " + i);
				ClearClause currentCounter = counters.get(i);
				List<Clause> subsumers = s.SubsumersOfClearClause(currentCounter);
				//System.out.println("counter: " + currentCounter.ToLong());
				//System.out.println("#subsumers: " + subsumers.size());
				if (subsumers.isEmpty()) return currentCounter.Negate(); // solution is found, we return it
				byte maxNumberOfX = 0;
				for (Clause subsumer : subsumers) { // selects the best subsumer
					byte currentNumberOfX = subsumer.NumberOfXFromRight();
					if (currentNumberOfX > maxNumberOfX) maxNumberOfX = currentNumberOfX;
				}
				long toAdd = 1L << maxNumberOfX;
				currentCounter.add(toAdd);
				//System.out.println("toAdd: " + toAdd);
				//System.out.println("counter: " + currentCounter.ToLong());
				if (currentCounter.isGreaterOrEqu(bombs.get(i+1))){
					//System.out.println("This one is solved!");
					solved[i] = true; // this one is solved
					numberOfNotYedSolvedOnes--;
				}
				//System.out.println("numberOfNotYedSolvedOnes: " + numberOfNotYedSolvedOnes);
			}
		}
		return null;
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
				if (clause.length() > 0 && clause.charAt(0) == 'p') global.setNumberOfBits(clause);
				clause = file.readLine();
			}
			file.close();
		} catch (IOException e) { System.out.println(e); System.exit(-1); }
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
			if (clause.Subsumes(c))
				subsumers.add(clause);
		}
		return subsumers;
	}
	// it does the same as SubsumersOf, but a bit cheaper
	public List<Clause> SubsumersOfClearClause(ClearClause c) {
		ArrayList<Clause> subsumers = new ArrayList<Clause>();
		for (Clause clause : clauses) {
			if (c.isSubsumedBy(clause))
				subsumers.add(clause);
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
	public Clause() { 
		mask = 0;
		bits = 0;
	}
	// sets a literal, if literal is -5, it means we have to set the 5.-th variable to be negative.
	public void SetLiteralZeroBased(int literal) {
		int index = global.getNumOfBits() - Math.abs(literal);
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
		for (int i = 0; i < global.getNumOfBits(); i++) {
			if ((mask & bitMask) == 0) xCounter++;
			else break;
			bitMask <<= 1;
		}
		return xCounter;
	}
	// converts a long into clause, it creates always a clear clause 
	public static ClearClause ToClause(long id) {
		ClearClause c = new ClearClause();
		c.bits = id;
		return c;
	}
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		long indexMask = 1L << global.getNumOfBits() - 1;
		for (int i = global.getNumOfBits() - 1; i >= 0; i--) {
			if ((mask & indexMask) > 0) {
				int literal = global.getNumOfBits() - i;
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
/**
 * Represent a clear clause.
 * It has some extra methods.
 * The method isSubsumedBy is cheaper than the method Subsumes
 */
class ClearClause extends Clause {
	public ClearClause() {
		super();
		mask = Long.MAX_VALUE;
	}
	// adds toADD to the bits
	public void add(long toAdd) {
		bits += toAdd;
	}
	// tests a bit
	public boolean testBit(int n) {
		return (bits & (1L << n)) > 0;
	}
	// checks whether this clear clause is subsumed by the input clause or not
	// it is a cheaper method than Subsumes in the Clause class
	public boolean isSubsumedBy(Clause b) {
		long bitsAndbMask = bits & b.mask;
		return bitsAndbMask == b.bits;
	}
	public boolean isGreaterOrEqu(long value) { return bits >= value; }
	public long ToLong() { return bits; }
	
}
//serves as the global access point for number of bits which read from the p line of DIMACS
class global {
	private static int numOfBits = 62; // may not be bigger than 62
	public static int getNumOfBits() { return numOfBits; }
	static void setNumberOfBits(String pLine)
	{
		int i = 2;
     while (pLine.charAt(i) == ' ') i++;
     while (i < pLine.length() && pLine.charAt(i) != ' ') i++; 
     while (pLine.charAt(i) == ' ') i++;
     String numberOfVariables = "";
     while (i < pLine.length() && pLine.charAt(i) != ' ') {
     	numberOfVariables += pLine.charAt(i);
         i++;
     }
     numOfBits = Integer.parseInt(numberOfVariables);
     if (numOfBits > 62)
     {
     	System.out.println("The DIMACS file may not contain more than 62 variables.");
     	System.out.println("See the line which starts with p. The first number is the number of variables.");
     	System.exit(-1);
     }
	}
}
