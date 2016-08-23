------------------------------------------------------------
* Shortest path algorithms for geographical networks
* https://github.com/nodirsid/ms-project

* Created by: Nodirjon Siddikov
------------------------------------------------------------
INTRO

This is a README file for shortest path algorithms which were developed as part of MSc Project titled "Shortest path algorithms for geographical networks". Algorithms are grouped into IntelliJ IDEA solution to simplify organization, compilation, execution and testing of algorithms. The solution contains the following shortest path algorithms:
- Dijkstra’s algorithm
- Bidirectional version of Dijkstra’s algorithm
- Dijkstra’s algorithm for geographical networks (uses heuristics)
- Dijkstra’s algorithm with edge flags
- Bidirectional version of Dijkstra’s algorithm with edge flags
- Query generator


-------------------------------------------------------------
SOLUTION STRUCTURE

- README.txt: this file
- src: source code for shortest path algorithms and their accompanying files (.java files)
- input: input data necessary for algorithms to run; graph file (.gr), coordinate files (.co), p2p query files (.p2p) are stored in this directory
- results: performance report files for each algorithm is generated here; report files are generated for preprocessing (.p2p.p.res files) and querying (.p2p.q.res files) operations
- out: compiled source code files for algorithms and other accompanying Java files (.class files)
- config.txt : configuration parameters of algorithms are defined in this file; necessary to run algorithms
  

-------------------------------------------------------------
SOFTWARE REQUIREMENTS

In order to sucessfully compile and execute algorithms, the following software is necessary:
- Operating system: Windows 10
- Java software development kit (JDK) version 1.8
- Text editing software such as Notepad
- Optional: IntelliJ IDEA development environment; using IntelliJ IDEA is highly recommended, since it helps maintain references to Java libraries and environment variables, and simplifies the compilation, execution and organization of Java files

-------------------------------------------------------------
1) HOW TO COMPILE AND RUN THE SOLUTION

To compile the solution with IntelliJ IDEA, first "Algorithm" should be selected from IntelliJ IDEA configuration dropdown box. This will set Main.java class as startup file of the solution. Once selected, input arguments should be supplied to the class file by typing them into "Program arguments" textbox. The order of arguments are as follows:
1. Graph file (.gr). The road network will be built from edges supplied in this file
2. Node coordinate file (.co). Nodes in the graph will be enriched with longitude/latitude information which are supplied in this file
3. Query file (.p2p). Algorithms start execution by reading query pairs from this file. Each query pair starts new instance of shortest path algorithm.
After providing Main.java with input files, next step is to supply the solution with runtime values through configuration file (config.txt). This file is self-explanatory and lets the user set algorithm type, graph partitioning type, and other parameters. In the end, "Run" button should be hit to start calculation of shortest paths. Necessary configurations for algorithms will be read during runtime from config.txt file. If algorithms sucessfully finish their execution, a set of output files will be produced and stored in "results" directory. These are performance report files which contain values for the metrics (such as query time, number of nodes scanned, etc.) used to compare algorithms. The type of output depends on the type of algorithm. Algorithms which only have querying step produce one file which is performance report for queries (.p2p.q.res files). Algorithms which have both preprocessing and querying steps produce two files: one performance report file for preprocessing (.p2p.p.res files) and one performance report file for queries (.p2p.q.res files). Sample execution of Main.java file from command line looks as follows:

java Main FLA.gr FLA.co FLA_queries.rand.p2p

The execution of algorithms can be further be controlled by changing parameter values in configuration file. For example, to partition a graph with quad trees where each quadrant contain no more than 50 nodes, "max_vertices_per_quadrant" parameter should be set to 50. 

-------------------------------------------------------------
2) HOW TO GENERATE QUERY FILES

Two types of query files can be generated: local and random. To generate either type of query files, first "QueryGenerator" should be selected from IntelliJ IDEA configuration dropdown box. This will set QueryGenerator.java class as startup file of the solution. Once selected, input arguments should be supplied to the class file by typing them into "Program arguments" textbox. The order of arguments are as follows:
1. Query type (0=local query, 1=random query)
2. Number of query pairs (i.e. 1000)
3. Radius (in meters); this parameter is effective when local query pairs need to be generated; all nodes which are within radius of source node are considered to be local
4. Node coordinate file (.co); query pairs will be randomly selected from set of nodes supplied in this file. 
After feeding the QueryGenerator.java with parameters, "Run" button should be hit on IntelliJ IDEA. The generated query pairs will be stored in "input" directory of the solution. For example, local query file for NY graph file will be NY_queries.loc.p2p, and random query pairs for FLA graph will be FLA_queries.rand.p2p. Sample execution of QueryGenerator.java file from command line looks as follows:

java QueryGenerator 0 1000 5000 COL.co
