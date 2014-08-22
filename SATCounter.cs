using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Collections;

/// <summary>
/// SATCounter:CCC v1.2
/// SATCounter: Count Clear Clauses SAT Solver
/// version: v1.2, only for expert users
/// It contains lot of experimental methods, like SolveFromMax
/// creation date: 03.05.2013
/// </summary>

namespace SATCounter
{
    class Program
    {
        static int Main(string[] args)
        {
            Console.WriteLine("SATCounter: Count Clear Clauses SAT Solver");
            if (args.Length != 1)
            {
		Console.WriteLine("Start it with a DIMACS file as a parameter.");
		Console.WriteLine("The DIMACS file may not contain more than 64 variables.");
                return -1;
            }
            String fileName = args[0];
            ClauseSet s = new ClauseSet(fileName);
            Clause solution = SATCounter.SolveFrom0(s);
            if (solution == null) Console.WriteLine("Unsatisfiable!");
            else
            {
                Console.WriteLine("Satisfiable! A solution is:");
                Console.WriteLine(solution);
            }
            return 0;
        }
    }
    class SATCounter
    {
        public static Clause SolveFrom0(ClauseSet s)
        {
            ulong numberOfSteps = 0;
            ulong counter = 0;
            Clause c = Clause.ToClause(counter);
            Console.WriteLine("counter: " + c);
            List<Clause> subsumers = s.SubsumersOf(Clause.ToClause(counter));
            while (subsumers.Count != 0)
            {
                byte maxNumberOfX = 0;
                foreach (Clause subsumer in subsumers)
                {
                    Console.WriteLine("subsumer: " + subsumer);
                    byte currentNumberOfX = subsumer.NumberOfXFromRight();
                    if (currentNumberOfX > maxNumberOfX) maxNumberOfX = currentNumberOfX;
                }
                Console.WriteLine("maxNumberOfX: " + maxNumberOfX);
                counter += 1UL << maxNumberOfX;
                numberOfSteps++;
                if (counter == 0)
                {
                    Console.WriteLine("numberOfSteps: " + numberOfSteps);
                    return null;
                }
                c = Clause.ToClause(counter);
                Console.WriteLine("counter: " + counter);
                Console.WriteLine("counter: " + c);
                subsumers = s.SubsumersOf(Clause.ToClause(counter));
            }
            Console.WriteLine("numberOfSteps: " + numberOfSteps);
            return Clause.ToClause(counter).Negate();
        }
        public static Clause SolveFromMax(ClauseSet s)
        {
            ulong numberOfSteps = 0;
            ulong counter = ulong.MaxValue;
            Clause c = Clause.ToClause(counter);
            Console.WriteLine("counter: " + c);
            List<Clause> subsumers = s.SubsumersOf(Clause.ToClause(counter));
            while (subsumers.Count != 0)
            {
                byte maxNumberOfX = 0;
                foreach (Clause subsumer in subsumers)
                {
                    Console.WriteLine("subsumer: " + subsumer);
                    byte currentNumberOfX = subsumer.NumberOfXFromRight();
                    if (currentNumberOfX > maxNumberOfX) maxNumberOfX = currentNumberOfX;
                }
                Console.WriteLine("maxNumberOfX: " + maxNumberOfX);
                counter -= 1UL << maxNumberOfX;
                numberOfSteps++;
                if (counter == ulong.MaxValue)
                {
                    Console.WriteLine("numberOfSteps: " + numberOfSteps);
                    return null;
                }
                c = Clause.ToClause(counter);
                Console.WriteLine("counter: " + counter);
                Console.WriteLine("counter: " + c);
                subsumers = s.SubsumersOf(Clause.ToClause(counter));
            }
            Console.WriteLine("numberOfSteps: " + numberOfSteps);
            return Clause.ToClause(counter).Negate();
        }
        public static Clause Solve(ClauseSet s, ulong from, ulong till)
        {
            ulong counter = from;
            List<Clause> subsumers = s.SubsumersOf(Clause.ToClause(counter));
            while (subsumers.Count != 0)
            {
                byte maxNumberOfX = 0;
                foreach (Clause subsumer in subsumers)
                {
                    byte currentNumberOfX = subsumer.NumberOfXFromRight();
                    if (currentNumberOfX > maxNumberOfX)
                    {
                        maxNumberOfX = currentNumberOfX;
                    }
                }
                counter += 1UL << maxNumberOfX;
                if (counter >= till) //Ha a till = 0, akkor ez így nem jó!
                {
                    return null;
                }
                subsumers = s.SubsumersOf(Clause.ToClause(counter));
            }
            return Clause.ToClause(counter).Negate();
        }
    }
    class ClauseSet
    {
        List<Clause> clauses;
        public ClauseSet(string fileName)
        {
            clauses = new List<Clause>();
            StreamReader file = new StreamReader(
                new FileStream(fileName, FileMode.Open));
            while (!file.EndOfStream)
            {
                string clause = file.ReadLine();
                if (clause[0] != '0' &&
                    clause[0] != 'c' &&
                    clause[0] != 'p' &&
                    clause[0] != '%') AddCNFClause(clause);
            }
        }
        public void AddCNFClause(string cnfClause)
        {
            int i = 0;
            Clause c = new Clause();
            while (i < cnfClause.Length)
            {
                while (cnfClause[i] == ' ') i++;
                string lit = "";
                while (i < cnfClause.Length && cnfClause[i] != ' ')
                {
                    lit += cnfClause[i];
                    i++;
                }
                int literal = Convert.ToInt32(lit);
                if (literal == 0) break;
                c.SetLiteralZeroBased(literal);
            }
            clauses.Add(c);
        }
        public void RemoveClause(Clause c)
        {
            clauses.Remove(c);
        }
        public List<Clause> SubsumersOf(Clause c)
        {
            List<Clause> subsumers = new List<Clause>();
            foreach (Clause clause in clauses)
            {
                if (clause.Subsumes(c)) subsumers.Add(clause);
            }
            return subsumers;
        }
    }
    class Clause
    {
        ulong mask, bits;
        public Clause() { mask = 0; bits = 0; }
        /// <summary>
        /// it works if bits was initialized by 0,
        /// and set bits must not be unset
        /// </summary>
        /// <param name="literal"></param>
        public void SetLiteralZeroBased(int literal)
        {
            int index = 64 - Math.Abs(literal);
            ulong indexMask1 = 1UL << index;
            mask = mask | indexMask1;
            if (literal > 0) bits = bits | indexMask1;
        }
        public void SetLiteral(int literal)
        {
            System.Diagnostics.Debug.Assert(Math.Abs(literal) > 0);
            System.Diagnostics.Debug.Assert(Math.Abs(literal) <= 64);
            int index = 64 - Math.Abs(literal);
            ulong indexMask1 = 1UL << index;
            ulong indexMask0 = ~indexMask1;

            bool wasAlreadySet = (mask & indexMask1) > 0;
            bool wasTrue = (bits & indexMask1) > 0;

            mask = mask | indexMask1;
            if (literal > 0)
            {
                bits = bits | indexMask1;
                if (wasAlreadySet && !wasTrue)
                {
                    mask = mask & indexMask0;
                }
            }
            else
            {
                bits = bits & indexMask0;
                if (wasAlreadySet && wasTrue)
                {
                    mask = mask & indexMask0;
                }
            }
        }
        public bool GetMaskedBit(int index)
        {
            System.Diagnostics.Debug.Assert(Math.Abs(index) >= 0);
            System.Diagnostics.Debug.Assert(Math.Abs(index) < 64);
            ulong indexMask = 1UL << index;
            return (mask & indexMask & bits) > 0 ? true : false;
        }
        public bool GetMaskBit(int index)
        {
            System.Diagnostics.Debug.Assert(Math.Abs(index) >= 0);
            System.Diagnostics.Debug.Assert(Math.Abs(index) < 64);
            ulong indexMask = 1UL << index;
            return (mask & indexMask) > 0 ? true : false;
        }
        /// <summary>
        /// Creates the negated clause of itself.
        /// </summary>
        /// <returns>Negation of this clause.</returns>
        public Clause Negate()
        {
            Clause c = new Clause();
            c.mask = this.mask;
            c.bits = ~this.bits;
            return c;
        }
        public bool Subsumes(Clause b)
        {
            if (mask > b.mask) return false;
            ulong cMask = mask & b.mask;
            return (bits & cMask) == (b.bits & cMask) ? true : false;
        }
        public ulong NumberOfSubsumedClearClauses()
        {
            int xCounter = 0;
            ulong bitMask = 1;
            for (int i = 0; i < 64; i++)
            {
                if ((mask & bitMask) == 0) xCounter++;
                bitMask <<= 1;
            }
            return 1UL << xCounter;
        }
        public ulong NumberOfSubsumedClearClausesFromRight()
        {
            int xCounter = 0;
            ulong bitMask = 1;
            for (int i = 0; i < 64; i++)
            {
                if ((mask & bitMask) == 0) xCounter++;
                else break;
                bitMask <<= 1;
            }
            return 1UL << xCounter;
        }
        public ulong NumberOfSubsumedClearClausesFromLeft()
        {
            int xCounter = 0;
            ulong bitMask = 1UL << 63;
            for (int i = 0; i < 64; i++)
            {
                if ((mask & bitMask) == 0) xCounter++;
                else break;
                bitMask >>= 1; // osztáss
            }
            return 1UL << xCounter;
        }
        public byte NumberOfX()
        {
            byte xCounter = 0;
            ulong bitMask = 1;
            for (int i = 0; i < 64; i++)
            {
                if ((mask & bitMask) == 0) xCounter++;
                bitMask <<= 1;
            }
            return xCounter;
        }
        public byte NumberOfXFromRight()
        {
            byte xCounter = 0;
            ulong bitMask = 1;
            for (int i = 0; i < 64; i++)
            {
                if ((mask & bitMask) == 0) xCounter++;
                else break;
                bitMask <<= 1;
            }
            return xCounter;
        }
        public byte NumberOfXFromLeft()
        {
            byte xCounter = 0;
            ulong bitMask = 1UL << 63;
            for (int i = 0; i < 64; i++)
            {
                if ((mask & bitMask) == 0) xCounter++;
                else break;
                bitMask >>= 1; // osztáss
            }
            return xCounter;
        }
        public List<byte> GetMaskXNonXLengths()
        {
            List<byte> maskXNonXLengths = new List<byte>();
            byte xCounter = 0;
            byte nonxCounter = 0;
            ulong bitMask = 1;
            for (int i = 0; i < 64; i++)
            {
                if ((mask & bitMask) == 0)
                {
                    xCounter++;
                    if (nonxCounter != 0)
                    {
                        maskXNonXLengths.Add(nonxCounter);
                        nonxCounter = 0;
                    }
                }
                else
                {
                    if (xCounter != 0 || bitMask == 1)
                    {
                        maskXNonXLengths.Add(xCounter);
                        xCounter = 0;
                    }
                    nonxCounter++;
                }
                bitMask <<= 1;
            }
            if (xCounter != 0) maskXNonXLengths.Add(xCounter);
            if (nonxCounter != 0) maskXNonXLengths.Add(nonxCounter);
            // it must contain pairs of x, nonx length!
            if (maskXNonXLengths.Count % 2 == 1) maskXNonXLengths.Add(0);
            return maskXNonXLengths;
        }
        public Dictionary<ulong, ulong> GetSubsumedIntervals()
        {
            Dictionary<ulong, ulong> subsumedIntervals = new Dictionary<ulong, ulong>();
            List<byte> maskXNonXLengths = GetMaskXNonXLengths();
            ulong from = bits;
            byte length = 0;
            for (int i = 0; i < maskXNonXLengths.Count; i += 2)
            {
                length += maskXNonXLengths[i];
                ulong until = bits + (1UL << length);
                Console.WriteLine("from: " + from);
                Console.WriteLine("until: " + until);
                subsumedIntervals.Add(from, until);
                length += maskXNonXLengths[i + 1];
                from = bits + (1UL << length);
            }
            return subsumedIntervals;
        }
        public static Clause ToClause(ulong id)
        {
            Clause c = new Clause();
            c.bits = id;
            c.mask = ulong.MaxValue;
            return c;
        }
        public static Clause ToClause(int[] literals)
        {
            Clause c = new Clause();
            c.bits = 0;
            c.mask = 0;
            foreach (int literal in literals)
            {
                c.SetLiteralZeroBased(literal);
            }
            return c;
        }
        public static bool IsNoDiff(Clause a, Clause b)
        {
            ulong cMask = a.mask & b.mask;
            return (a.bits & cMask) == (b.bits & cMask) ? true : false;
        }
        public override string ToString()
        {
            string s = "";
            ulong indexMask = 1UL << 63;
            for (int i = 63; i >= 0; i--)
            {
                if ((mask & indexMask) > 0)
                {
                    int literal = 64 - i;
                    if ((bits & indexMask) > 0) s += literal;
                    else s += -literal;
                    s += ' ';
                }
                indexMask >>= 1;
            }
            s += '0';
            return s;
        }
    }
}
