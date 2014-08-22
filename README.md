cccsat
======
This README describes those files which are created along with the article:
Solving SAT by an Iterative Version of the Inclusion-Exclusion Principle
You can find 5 files in this directory:
CCC_v1Dot0.java, it is stable, all the test are done with this version.
CCC_v1Dot1.java, it is stable, a new class, ClearClause, is added.
CCC_BigInt_v1Dot2.java, experimental version, it is based on BigInteger.
SATCounter.cs, stable C# version of the CCC algorithm, only for experts.
CCCBomberv1Dot0.java, experimental version, it implements a bombing style CCC algorithm, which might be fast for satisfiable SAT problems.

We describe only how to use CCC_v1Dot0:
Step 1: Compile it with this command:
   java CCC_v1Dot0.java
   Some classes are created.
   
Step 2: Run it with this command:
   javac CCC_v1Dot0 a_cnf_file
   where a_cnf_file is any CNF file in DIMACS format.

Enjoy!
