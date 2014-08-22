import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.math.BigInteger;

/**
 * Count Clear Clauses SAT Solver, Java version, based on BigInteger, v1.2
 * Free for any purpose, but please cite our contribution.
 * See also: Solving SAT by an Iterative Version of the Inclusion-Exclusion Principle
 */

class global {
	public static int numOfBits = 100;
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
	}
}


public class CCC_BigInt_v1Dot2
{
        public static void main(String[] args)
        {
        	long startTime = System.currentTimeMillis();
        	long endTime;
        	System.out.println("SATCounter: Count Clear Clauses SAT Solver");

            if (args.length != 1)
            {
            	System.out.println("Start it with a DIMACS file as a parameter.");
            }
            else
            {
            	String fileName = args[0];
            	ClauseSet s = new ClauseSet(fileName);
            	Clause solution = SATCounter.SolveFrom0(s);
            	if (solution == null) {
            		endTime = System.currentTimeMillis(); 
            		System.out.println("Unsatisfiable!");
            	}
            	else {
            		endTime = System.currentTimeMillis();
            		System.out.println("Satisfiable! A solution is:");
            		System.out.println(solution);
            	}
                System.out.println("CPU time is " + (endTime - startTime)/1000.0f + " s");
            }
        }
}

    class SATCounter
    {
        public static Clause SolveFrom0(ClauseSet s)
        {
        	ClearClause counter = new ClearClause(global.numOfBits);
        	//long counter2 = 0;
        	//System.out.println("biginteger counter: " + counter);
        	//System.out.println("biginteger counter mask: " + counter.mask);
        	//System.out.println("biginteger counter bits: " + counter.bits);
        	//System.out.println("counter2: " + counter2);

            List<Clause> subsumers = s.SubsumersOfClearClause(counter);
            while (!subsumers.isEmpty())
            {
                int maxNumberOfX = 0;
                for (Clause subsumer: subsumers){
                    int currentNumberOfX = subsumer.NumberOfXFromRight();
                    if (currentNumberOfX > maxNumberOfX)
                    {
                    	maxNumberOfX = currentNumberOfX;
                    	//System.out.println("subsumer: " + subsumer);
                    }
                }
                BigInteger toAdd = BigInteger.ZERO.setBit(maxNumberOfX);
                
                //System.out.println("biginteger toAdd: " + toAdd);
                //System.out.println("biginteger counter mask: " + counter.mask);
            	//System.out.println("biginteger counter bits: " + counter.bits);
	        	//counter2 += toAdd.longValue();
	        	//System.out.println("counter2: " + counter2);

				counter.add(toAdd);
	        	//System.out.println("biginteger counter: " + counter);
	        	//System.out.println("biginteger counter bits: " + counter.bits.bitCount());
	        	//System.out.println("counter2: " + counter2);

                if (counter.testBit(global.numOfBits)) {
                    return null;
                }   
                subsumers = s.SubsumersOfClearClause(counter);
            }
            return counter.Negate();
        }
    }
    class ClauseSet
    {
        List<Clause> clauses;
        public ClauseSet(String fileName)
        {
            clauses = new ArrayList<Clause>();
            
            try{
            	BufferedReader file = new BufferedReader(new FileReader(fileName));
            	String clause = file.readLine();
            	while (clause != null)
            	{
            		if (clause.length() > 0 &&
        				clause.charAt(0) != '0' && 
        				clause.charAt(0) != 'c' &&
        				clause.charAt(0) != 'p' &&
        				clause.charAt(0) != '%') AddCNFClause(clause);
        			if (clause.length() > 0 && clause.charAt(0) == 'p') global.setNumberOfBits(clause);
            		clause = file.readLine();
            	}
            	file.close();
            }catch(IOException e){System.out.println(e);}
        }
        public void AddCNFClause(String cnfClause)
        {
            int i = 0;
            Clause c = new Clause();
            while (i < cnfClause.length())
            {
                while (cnfClause.charAt(i) == ' ') i++;
                String lit = "";
                while (i < cnfClause.length() && cnfClause.charAt(i) != ' ')
                {
                    lit += cnfClause.charAt(i);
                    i++;
                }
                int literal = Integer.parseInt(lit);
                if (literal == 0) break;
                c.SetLiteralZeroBased(literal);
            }
			clauses.add(c);
        }
        
        public List<Clause> SubsumersOf(Clause c)
        {
            ArrayList<Clause> subsumers = new ArrayList<Clause>();
			for (Clause clause : clauses)
            {
                if (clause.Subsumes(c)) subsumers.add(clause);
            }
            return subsumers;
        }
        public List<Clause> SubsumersOfClearClause(ClearClause c)
        {
            ArrayList<Clause> subsumers = new ArrayList<Clause>();
			for (Clause clause : clauses)
            {
                if (c.isSubsumedBy(clause)) subsumers.add(clause);
            }
            return subsumers;
        }
    }
    class Clause
    {
        protected BigInteger mask, bits;
        public Clause() { mask = BigInteger.ZERO; bits = BigInteger.ZERO; }

        public void SetLiteralZeroBased(int literal)
        {
            int index = global.numOfBits - Math.abs(literal);
            mask = mask.setBit(index);
            if (literal > 0) bits = bits.setBit(index);
        }
        public Clause Negate() {
            Clause c = new Clause();
            c.mask = this.mask;
            c.bits = this.bits.not();
            c.bits = c.bits.and(c.mask);
            return c;
        }
        public boolean Subsumes(Clause b) {
            if (mask.compareTo(b.mask) > 0) return false;
            BigInteger cMask = mask.and(b.mask);
            BigInteger bitsAndcMask = bits.and(cMask);
            BigInteger b_bitsAndcMask = b.bits.and(cMask);
            return bitsAndcMask.compareTo(b_bitsAndcMask) == 0 ? true : false;
        }
        public int NumberOfXFromRight() {
        	//System.out.println("NumberOfXFromRight");
        	//System.out.println("mask: " + mask);
        	//System.out.println("bits: " + bits);
        	//System.out.println("this: " + this);
        	//System.out.println("mask.getLowestSetBit(): " + mask.getLowestSetBit());
        	
            return mask.getLowestSetBit();
        }
        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();	
            for (int i = global.numOfBits-1; i >= 0; i--) {
                if (mask.testBit(i)) {
                    int literal = global.numOfBits - i;
                    if (bits.testBit(i)) s.append(literal);
                    else s.append(-literal);
                    s.append(' ');
                }
            }
            s.append('0');
            return s.toString();
        }
    }
    class ClearClause extends Clause
    {
    	final int numOfBits;
    	public ClearClause(int numOfVariables)
    	{
    		super();
    		numOfBits = numOfVariables+1;
    		mask = BigInteger.ONE.shiftLeft(numOfBits);
    		mask = mask.subtract(BigInteger.ONE);
    	}
    	public void add(BigInteger toAdd)
    	{
    		bits = bits.add(toAdd);
    	}
    	public boolean testBit(int n)
    	{
    		return bits.testBit(n);
    	}
    	public boolean isSubsumedBy(Clause b) {
            //BigInteger cMask = b.mask;
            //BigInteger bitsAndcMask = bits.and(cMask);
            //BigInteger b_bitsAndcMask = b.bits.and(cMask);
            //return bitsAndcMask.compareTo(b_bitsAndcMask) == 0 ? true : false;
    		BigInteger bitsAndbMask = bits.and(b.mask);
            return bitsAndbMask.compareTo(b.bits) == 0 ? true : false;
        }
    }
