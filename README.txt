------------------------------------------------------------
* Shortest path algorithms for geographical networks
* https://github.com/nodirsid/ms-project

* Created by:   $Author: Nodirjon Siddikov $
------------------------------------------------------------

Shortest path algorithms for geographical networks
Contents:
- Dijkstra’s algorithm
- Bidirectional version of Dijkstra’s algorithm
- Dijkstra’s algorithm based on heuristics
- Dijkstra’s algorithm with edge flags
- Bidirectional version of Dijkstra’s algorithm with edge flags
- Query generator

August 26, 2016

(C) 2016 Nodirjon Siddikov

-------------------------------------------------------------
REPOSITORY STRUCTURE

  - README.txt: this file

  - src : source code for shortest path algorithms (.java files)

  - input : input data necessary for algorithms to run
            graph file (.gr), coordinate files (.co), p2p query files (.p2p) are stored in this directory

  - results : performance measurement files for each algorithm is generated here

  - out : compiled source code files for algorithms (.class files)

  - config.txt : configuration parameters of algorithms are defined in this file; necessary to run algorithms


-------------------------------------------------------------
SOFTWARE REQUIREMENTS

In order compile and execute algorithms, the following software is necessary:
- Operating system: Windows 10
- Java Software Development Kit (JDK) version 1.8
- Text editing software such as Notepad
- Optional: IntelliJ IDEA development environment

-------------------------------------------------------------
HOW TO COMPILE THE SOLUTION

To compile the solution with IntelliJ IDEA, you have to:

1) open the solution with IntelliJ IDEA development environment
2) choose "Algorithm" from configurations list
3) click play button to execute the code

-------------------------------------------------------------
2) HOW TO GENERATE QUERY FILES

To build the core instances, just type 'make gen'. This will create in
directory ch9/inputs all problem families. Those include both
synthetic and real-world instances. Synthetic instances are built
by the generators located in the ch9/gens directory. Real-world instances
are downloaded from the Challenge server running on www.dis.uniroma1.it.

To run 'make gen', you must have built the package first 
(see section HOW TO COMPILE THE CODE above).

INSTALL NOTES: 

* the platform building process may take a while, especially to generate 
  local query files for p2p problems. Please be patient. 

* to install real-world core families, the 'make gen' command 
  requires that you are connected to the Internet and can run ftp.
  The installer assumes your ftp program can use the http protocol.
  Old versions of ftp do not support http and may not work: for instance, 
  this happens with some releases of Cygwin. In this case, you have to 
  download files manually with an http client from:

  http://www.dis.uniroma1.it/~challenge9/data/USA-road-d
  http://www.dis.uniroma1.it/~challenge9/data/USA-road-t

  Downloaded files should be put in ch9/inputs/USA-road-d and in
  ch9/inputs/USA-road-t.

* core instances require around 11GB of free disk space
  (5.2GB for real-world families USA-road-d and USA-road-t).

IMPORTANT NOTICE: 

Building and running the whole package may require several hours.
If you are only interested in a specific problem, you can generate only 
instances for that problem. If you are only interested in the ss 
problem, you can type:

'make gen_gr' to create the graphs (both synthetic and real-world)
'make gen_ss' to create the synthetic ss instances

If you are only interested in the p2p problem, you can type:

'make gen_gr' to create the synthetic graphs
'make gen_p2p' to create the synthetic p2p instances (this may take a while)

You can refine your control over the building process even further.
For instance, if you wish to generate graphs in the USA-road-d family and 
the related problem instances only, you can go to ch9/scripts and type:

'perl genUSA-road-d.gr.pl'      (download graphs from the server)
'perl genUSA-road-d.ss.pl'      (generate ss problem instances)
'perl genUSA-road-d.rnd.p2p.pl' (generate random query pairs)
'perl genUSA-road-d.loc.p2p.pl' (generate local query pairs)

Please refer to ch9/scripts/README.txt for further information about 
customizing the instance generation.

-------------------------------------------------------------
3) HOW TO RUN THE CORE EXPERIMENTS

To run the benchmark solvers on all core input families, type
'make run'. You must have created all core instances first. 
Performance report files are generated in the ch9/results directory.

If you are only interested in the ss problem, you can type:
'make run_ss'. If you are only interested in the p2p problem, you 
can type: 'make run_p2p'. 

You can refine your control over the running process even further.
For instance, if you wish to run ss experiments on graphs in the 
USA-road-d family only, you can go to ch9/scripts and type:
'perl runUSA-road-d.ss.pl'.

Please refer to ch9/scripts/README.txt for further information about
running only selected experiments.