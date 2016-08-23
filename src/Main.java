import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.Properties;

public class Main {

    //variables shared project wide
    public static int ALGORITHM;
    public static int GRAPH_PARTITIONING;
    public static int EDGE_FLAG_CALCULATION;
    public static int ROWS, COLUMNS;
    public static int MAX_VERTICES_PER_QUADRANT;
    public static int MAX_VERTICES_PER_REGION;

    public static void main(String[] args) {

        Scanner scanner1=null; //read graph files (with .gr extension)
        Scanner scanner2=null; //read coordinate files (with .co extension)
        Scanner scanner3=null; //read problem specification file (with .p2p extension)
        List<Query> p2pQueries; //array of p2p query pairs (either local or random pairs)
        Properties prop=new Properties();
        InputStream input = null; //read configuration file (config.txt)
        File graphFile=null;
        File coordFile=null;
        File probFile=null;

        try {
            graphFile = new File(args[0]); //file which contains data about nodes, edges, edge weights
            coordFile=new File(args[1]); //file which contains data about node coordinates
            probFile=new File(args[2]); //file which contains data about p2p queries from src to dest nodes
            input = new FileInputStream("config.txt"); //in the root folder of a project

            if (graphFile.exists())
                scanner1 = new Scanner(graphFile);

            if (coordFile.exists())
                scanner2 = new Scanner(coordFile);

            if (probFile.exists())
                scanner3 = new Scanner(probFile);

            //load config file
            prop.load(input);

        }catch (IOException ex) {
            throw new IllegalArgumentException("Could not open file");
        }finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //read config file contents
        //if config file is deleted, not accessible or corresponding property is removed, then default value is read
        ALGORITHM= Integer.parseInt(prop.getProperty("algorithm","0"));
        GRAPH_PARTITIONING=Integer.parseInt(prop.getProperty("partitioning_type","0"));
        EDGE_FLAG_CALCULATION=Integer.parseInt(prop.getProperty("edge_flag_calc_method","0"));
        ROWS=Integer.parseInt(prop.getProperty("rows","5"));
        COLUMNS=Integer.parseInt(prop.getProperty("columns","5"));
        MAX_VERTICES_PER_QUADRANT=Integer.parseInt(prop.getProperty("max_vertices_per_quadrant","6"));
        MAX_VERTICES_PER_REGION=Integer.parseInt(prop.getProperty("max_vertices_per_region","4"));


        //read file which contains data about nodes, edges and edge weight and build a graph
        Graph g = new Graph(scanner1);

        //read .co file which contains data about node coordinates
        //enrich nodes in the graph with coordinates
        g.addNodeCoordinates(scanner2);

        //generate reverse graph to be used by bidirectional algorithms
        Graph revGraph=g.generateReverseGraph();

        //read queries from file
        p2pQueries = loadQueries(scanner3);

        System.out.println("Queries read from file");
        System.out.println("Printing graph...");
        System.out.println(g.toString());

        List<StatsForQuerying> qryStatsList=new ArrayList<>();
        StatsForPreprocessing prepStats=null; //preprocessing is done only once

        for(Query q: p2pQueries){

            //throw an IndexOutOfBoundsException unless 1 <= vertexId <= V
            if (q.getSrcVertex() < 1 || q.getSrcVertex() > g.V())
                throw new IndexOutOfBoundsException("Source vertex " + q.getSrcVertex() + " is not between 1 and " + g.V());

            if (q.getDestVertex() < 1 || q.getDestVertex() > g.V())
                throw new IndexOutOfBoundsException("Destination vertex " + q.getDestVertex() + " is not between 1 and " + g.V());

            Vertex src=g.getVertex(q.getSrcVertex()); //src vertex
            Vertex dest=g.getVertex(q.getDestVertex()); //dest vertex

            //compute shortest paths according to the specified algorithm
            switch (ALGORITHM){
                case 0:{
                    Dijkstra algo0 = new Dijkstra(g, src, dest, q.getId());
                    //algo0.printShortestPath(g, src, dest);
                    qryStatsList.add(algo0.getStatsForQuery());
                    break;
                }
                case 1:{
                    DijkstraBidirectional algo1 = new DijkstraBidirectional(g, revGraph, src, dest, q.getId());
                    //algo1.printShortestPath(g, src, dest);
                    qryStatsList.add(algo1.getStatsForQuery());
                    break;
                }
                case 2:{
                    DijkstraForGeoNets algo2 = new DijkstraForGeoNets(g, src, dest, q.getId());
                    //algo2.printShortestPath(g, src, dest);
                    qryStatsList.add(algo2.getStatsForQuery());
                    break;
                }
                case 3:{
                    DijkstraWithEdgeFlags algo3 = new DijkstraWithEdgeFlags(g, src, dest, q.getId());
                    //algo3.printShortestPath(g, src, dest);
                    if(!algo3.getStatsForPrep().isReadFromFile()) {
                        prepStats = algo3.getStatsForPrep();
                    }
                    qryStatsList.add(algo3.getStatsForQuery());
                    break;
                }
                case 4:{
                    DijkstraBidirectionalWithEdgeFlags algo4 = new DijkstraBidirectionalWithEdgeFlags(g, revGraph, src, dest, q.getId());
                    //algo4.printShortestPath(g, src, dest);
                    if(!algo4.getStatsForPrep().isReadFromFile()) {
                        prepStats = algo4.getStatsForPrep();
                    }
                    qryStatsList.add(algo4.getStatsForQuery());
                    break;
                }

            } //end switch
        } //end for

        //calculate statistics and generate performance report files
        calculateStats(p2pQueries.size(), prepStats, qryStatsList, g, graphFile.getName());

    } //end method main


    //generate performance report file
    private static void calculateStats(
            int noOfQrys,
            StatsForPreprocessing prepStats,
            List<StatsForQuerying> qryStatsList,
            Graph g,
            String graphFileName){

        File perfReportForPreproc = null;
        File perfReportForQueries=null;
        String perfReportForPreprocFileName="";
        String perfReportForQueriesFileName="";

        //metrics to measure preprocessing
        long prepExecTime=0;
        int noOfNodesScanned=0;

        if(prepStats != null){
            prepExecTime=prepStats.getPrepExecTime();
            if(!prepStats.isReadFromFile()){
                noOfNodesScanned=prepStats.getNoOfNodesScanned();
            }
        }

        //metrics to measure queries
        long ttlQryExecTime=0; double avgQryExecTime;
        int ttlNoOfNodesScanned=0; double avgNoOfNodesScanned;
        int ttlNoOfArcsScanned=0; double avgNoOfArcsScanned;
        int ttlNoOfDistImprovements=0; double avgNoOfDistImprovements;

        for(StatsForQuerying st: qryStatsList){
            ttlQryExecTime += st.getQryExecTime();
            ttlNoOfNodesScanned +=st.getNoOfNodesScanned();
            ttlNoOfArcsScanned +=st.getNoOfArcsScanned();
            ttlNoOfDistImprovements +=st.getNoOfDistImprovements();
        }

        avgQryExecTime= (double)ttlQryExecTime/noOfQrys;
        avgNoOfNodesScanned= (double)ttlNoOfNodesScanned/noOfQrys;
        avgNoOfArcsScanned= (double)ttlNoOfArcsScanned/noOfQrys;
        avgNoOfDistImprovements= (double)ttlNoOfDistImprovements/noOfQrys;

        switch(ALGORITHM){
            case 0:{
                perfReportForQueriesFileName=graphFileName.substring(0, graphFileName.indexOf('.'))+"_Dijkstra.p2p.q.res";
                perfReportForQueries=new File(".//results//"+perfReportForQueriesFileName);
                break;
            }
            case 1:{
                perfReportForQueriesFileName=graphFileName.substring(0, graphFileName.indexOf('.'))+"_BidirectDijkstra.p2p.q.res";
                perfReportForQueries=new File(".//results//"+perfReportForQueriesFileName);
                break;
            }
            case 2:{
                perfReportForQueriesFileName=graphFileName.substring(0, graphFileName.indexOf('.'))+"_DijkstraForGeoNets.p2p.q.res";
                perfReportForQueries=new File(".//results//"+perfReportForQueriesFileName);
                break;
            }
            case 3:{
                perfReportForPreprocFileName=graphFileName.substring(0, graphFileName.indexOf('.'))+"_DijkstraWithEdgeFlags.p2p.p.res";
                perfReportForQueriesFileName=graphFileName.substring(0, graphFileName.indexOf('.'))+"_DijkstraWithEdgeFlags.p2p.q.res";
                perfReportForPreproc=new File(".//results//"+perfReportForPreprocFileName);
                perfReportForQueries=new File(".//results//"+perfReportForQueriesFileName);
                break;
            }
            case 4:{
                perfReportForPreprocFileName=graphFileName.substring(0, graphFileName.indexOf('.'))+"_BidirectDijkstraWithEdgeFlags.p2p.p.res";
                perfReportForQueriesFileName=graphFileName.substring(0, graphFileName.indexOf('.'))+"_BidirectDijkstraWithEdgeFlags.p2p.q.res";
                perfReportForPreproc=new File(".//results//"+perfReportForPreprocFileName);
                perfReportForQueries=new File(".//results//"+perfReportForQueriesFileName);
                break;
            }
        }

        //write preprocessing stats into file
        writeToFile(perfReportForPreproc, prepExecTime, noOfNodesScanned, g);

        //write querying stats into file
        writeToFile(perfReportForQueries, avgQryExecTime, avgNoOfNodesScanned, avgNoOfArcsScanned, avgNoOfDistImprovements, g);
    }


    private static void writeToFile(
            File file,
            long prepExecTime,
            int noOfNodesScanned,
            Graph g){

        if(file != null){
            try{
                //create new file
                file.createNewFile();

                FileWriter writer = new FileWriter(file);
                String fileName=file.getName();
                DecimalFormat df = new DecimalFormat(".#");

                writer.write("c performance result of preprocessing operations\n");
                writer.write("c g - graph configuration\n");
                writer.write("c t - time spent to preprocessing (in milliseconds)\n");
                writer.write("c v - number of nodes scanned during preprocessing\n");
                writer.write("c\n");
                writer.write("c\n");
                writer.write("p res sp p2p p " + fileName.substring(0, fileName.indexOf('.'))+ "\n");
                writer.write("g " + g.V() + " " + g.E() + "\n");
                writer.write("t " + df.format(prepExecTime/1000000d) + "\n");
                writer.write("v " + noOfNodesScanned + "\n");
                writer.flush();
                writer.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }

    private static void writeToFile(
            File file,
            double avgQryExecTime,
            double avgNoOfNodesScanned,
            double avgNoOfArcsScanned,
            double avgNoOfDistImprovements,
            Graph g){

        if(file != null){
            try{
                //create new file
                file.createNewFile();

                FileWriter writer = new FileWriter(file);
                String fileName=file.getName();
                DecimalFormat df = new DecimalFormat(".#");

                writer.write("c performance result of querying operations\n");
                writer.write("c g - graph configuration\n");
                writer.write("c t - time spent to preprocessing (in milliseconds)\n");
                writer.write("c v - number of nodes scanned\n");
                writer.write("c e - number of edges scanned\n");
                writer.write("c i - number of distance improvements (aka number of effective relax operations)\n");
                writer.write("c\n");
                writer.write("c\n");
                writer.write("p res sp p2p q " + fileName.substring(0, fileName.indexOf('.'))+ "\n");
                writer.write("g " + g.V() + " " + g.E() + "\n");
                writer.write("t " + df.format(avgQryExecTime/1000000) + "\n");
                writer.write("v " + df.format(avgNoOfNodesScanned) + "\n");
                writer.write("e " + df.format(avgNoOfArcsScanned) + "\n");
                writer.write("i " + df.format(avgNoOfDistImprovements) + "\n");
                writer.flush();
                writer.close();
            }catch (IOException ex){
                ex.printStackTrace();
            }
        }
    }


    //read queries
    //return the list of queries
    private static List<Query> loadQueries(Scanner in){

        int noOfQrys = 0;
        int queryId=0;
        List<Query> p2pQueries=null;

        //read first byte in line
        char c=in.next().charAt(0);

        //iterate through queries
        while(in.hasNextLine()){
            switch (c){
                case 'c':{
                    //skip the line with comments
                    in.nextLine();

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }

                case 'p':{
                    //read the definition of a problem
                    in.next(); //read the word 'aux'
                    in.next(); //read the word 'sp'
                    in.next(); //read the word 'p2p'
                    noOfQrys = in.nextInt(); //get the number of vertices
                    p2pQueries=new ArrayList<Query>(noOfQrys);

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }

                case 'q': {
                    //read the query information
                    int srcVertex = in.nextInt();
                    int destVertex = in.nextInt();

                    p2pQueries.add(new Query(queryId++, srcVertex, destVertex));

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }
            }//end switch
        }

        return p2pQueries;
    }
}
