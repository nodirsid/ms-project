import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Stack;

//Dijkstra'a algorithm based on edge flags
//structure of Dijkstra's algorithm was adapted from http://algs4.cs.princeton.edu/44sp/DijkstraSP.java.html
public class DijkstraBidirectionalWithEdgeFlags{

    private Set<Vertex> settledNodes; //Set S - nodes v for which we know that shortest path estimate is equal to the shortest path distance

    private MinPriorityQueue minPQ_f; //nodes in priority queue for forward search
    private MinPriorityQueue minPQ_b; //nodes in priority queue for backward search
    private Map<Vertex, Vertex> parent_f; //parent nodes for forward search
    private Map<Vertex, Vertex> parent_b; //parent nodes for backward search
    private Map<Vertex, Integer> d_f; //shortest path estimate for forward
    private Map<Vertex, Integer> d_b; //shortest path estimate for backward
    private Vertex commonVertex; //the vertex where forward and backward Dijkstra algorithms meet

    //stats
    private StatsForQuerying statsQry;
    private StatsForPreprocessing statsPrep;
    private int noOfNodeScans=0;
    private int noOfArcScans=0;
    private int noOfDistImprovements=0;  //number of effective relax operations
    private long qryStartTime, qryEndTime;

    //g - graph with weighted edges
    //v - source vertex
    public DijkstraBidirectionalWithEdgeFlags(Graph g, Graph revGraph, Vertex src, Vertex dest, int queryId){

        System.out.println("Bidirectional Dijkstra's algorithm with edge flags is running..."+ queryId);

        d_f=new HashMap<Vertex, Integer>(); //distance vector - shortest distance estimate from source to a particular vertex
        d_b=new HashMap<Vertex, Integer>(); //distance vector - shortest distance estimate from destination to a particular vertex
        parent_f=new HashMap<Vertex, Vertex>(); //parent vector
        parent_b=new HashMap<Vertex, Vertex>();
        settledNodes=new HashSet<Vertex>();

        //stats
        statsPrep=new StatsForPreprocessing();

        g.preprocess(statsPrep);
        revGraph.preprocess(statsPrep);

        //SHORTEST PATH CALCULATION STEP
        int target_region=dest.getRegion(); //get the region ID where destination node belongs to (to be used by forward Dijkstra algorithm)
        int source_region=src.getRegion(); //get the region ID where destination node belongs to (to be used by backward Dijkstra algorithm)

        //initialization
        initialization(g,src, dest);

        //dump all vertices to priority queue
        minPQ_f=new MinPriorityQueue(d_f.entrySet());
        minPQ_b=new MinPriorityQueue(d_b.entrySet());

        //stats
        statsQry=new StatsForQuerying();

        //mark start of query
        qryStartTime=System.nanoTime();
        while(!minPQ_f.isEmpty() && !minPQ_b.isEmpty()){
            //operations for forward Dijkstra's algorithm
            HeapEntry he_f= minPQ_f.extractMin();
            noOfNodeScans++; //increment the number of node scans

            Vertex vertex_f=he_f.getNode();

            //check if extracted node is the meeting point
            if(settledNodes.contains(vertex_f)) {
                commonVertex = vertex_f;
                break; //forward and backward Dijkstra algorithms meet at vertex_f
            }
            else {
                settledNodes.add(vertex_f);
            }

            //relaxation
            //selectively apply relax operation for each vertex in the adjacency list
            for(Edge e: g.getAdjList(vertex_f)){

                if(!e.isFlagged(vertex_f, target_region)) continue; //skip edges which cannot be on the shortest path to the destination node

                relax(e, true);

                noOfArcScans++;
            }



            //operations for backward Dijkstra's algorithm
            HeapEntry he_b= minPQ_b.extractMin();
            noOfNodeScans++; //increment the number of node scans

            Vertex vertex_b=he_b.getNode();
            if(settledNodes.contains(vertex_b)) {
                commonVertex = vertex_b;
                break; //forward and backward Dijkstra algorithms meet at vertex_b
            }
            else {
                settledNodes.add(vertex_b);
            }

            //relaxation
            //selectively apply relax operation for each vertex in the adjacency list
            for(Edge e: revGraph.getAdjList(vertex_b)){

                if(!e.isFlagged(vertex_b, source_region)) continue; //skip edges which cannot be on the shortest path to the source node

                relax(e, false);

                noOfArcScans++;
            }
        } //end while

        //mark end of query
        qryEndTime=System.nanoTime();

        statsQry.setQryExecTime(qryEndTime-qryStartTime);
        statsQry.setNoOfNodesScanned(noOfNodeScans);
        statsQry.setNoOfArcsScanned(noOfArcScans);
        statsQry.setNoOfDistImprovements(noOfDistImprovements);
    }


    //initialization of relaxation technique
    private void initialization(Graph g, Vertex src, Vertex dest){

        //populate distance vector d with nodes from graph g, set each node's shortest path estimate to infinity
        for(Vertex v: g.getVertices()){
            if(v != null) {
                d_f.put(v, (int) Double.POSITIVE_INFINITY);
                d_b.put(v, (int) Double.POSITIVE_INFINITY);
            }
        }
        d_f.put(src,0); //distance to the source vertex is set to zero
        d_b.put(dest,0); //distance to the destination vertex is set to zero

        //populate the parent array with nodes
        for(Vertex v: g.getVertices()){
            if(v!= null) {
                parent_f.put(v, null);
                parent_b.put(v, null);
            }
        }
    }

    //relaxation
    private void relax(Edge e, boolean isForward){
        Vertex src = e.getSource();
        Vertex dest = e.getDestination();

        if(isForward){ //relax operation of forward search
            if (d_f.get(dest) > d_f.get(src) + e.getWeight()) {
                d_f.put(dest, d_f.get(src) + e.getWeight());
                noOfDistImprovements++; //increment the no of distance improvements

                minPQ_f.decreaseKey(dest, d_f.get(dest));
                parent_f.put(dest, src);
            }
        }else{ //relax op of backward search
            if (d_b.get(dest) > d_b.get(src) + e.getWeight()) {
                d_b.put(dest, d_b.get(src) + e.getWeight());
                noOfDistImprovements++; //increment the no of distance improvements

                minPQ_b.decreaseKey(dest, d_b.get(dest));
                parent_b.put(dest, src);
            }
        }

    }


    //get shortest path estimate from source vertex to vertex v
    private int getShortestPathEstimateForward(Vertex v){
        return d_f.get(v);
    }

    //get shortest path estimate from destination vertex to vertex v
    private int getShortestPathEstimateBackward(Vertex v){
        return d_b.get(v);
    }

    //is there a path between source vertex and vertex v?
    private boolean hasPathToForward(Vertex v) {
        return d_f.get(v) < (int)Double.POSITIVE_INFINITY;
    }

    //is there a path between destination vertex and vertex v?
    private boolean hasPathToBackward(Vertex v) {
        return d_b.get(v) < (int)Double.POSITIVE_INFINITY;
    }

    //returns the path between source vertex and vertex v, or null if no such path exists
    private Iterable<Edge> getPathToForward(Graph g, Vertex v) {
        if (!hasPathToForward(v)) return null;

        Stack<Edge> path = new Stack<Edge>();

        for (Vertex parentV = parent_f.get(v); parentV != null; v=parentV, parentV = parent_f.get(parentV)) {

            for(Edge e: g.getEdges()){
                if(e.getSource().equals(parentV) && e.getDestination().equals(v)){
                    path.push(e);
                    break;
                }
            }
        }
        return path;
    }


    //returns the path between destination vertex and vertex v, or null if no such path exists
    private Iterable<Edge> getPathToBackward(Graph g, Vertex v) {
        if (!hasPathToBackward(v)) return null;

        Stack<Edge> path = new Stack<Edge>();

        for (Vertex parentV = parent_b.get(v); parentV != null; v=parentV, parentV = parent_b.get(parentV)) {

            for(Edge e: g.getEdges()){
                if(e.getSource().equals(parentV) && e.getDestination().equals(v)){
                    path.push(e);
                    break;
                }
            }
        }
        return path;
    }


    //get graph partitioning method used
    private String getGraphPartitioningMethod(){
        String partitioingMethod=null;
        switch(Main.GRAPH_PARTITIONING){
            case 0:
                partitioingMethod="Rectangular";
                break;
            case 1:
                partitioingMethod="Quad tree based";
                break;
            case 2:
                partitioingMethod="Kd tree based";
                break;
        }

        return partitioingMethod;
    }


    //get edge flag calculation method used
    private String getEdgeFlagCalculationMethod(){
        String edgeFlagCalculationMethod=null;
        switch(Main.EDGE_FLAG_CALCULATION){
            case 0:
                edgeFlagCalculationMethod="Naive";
                break;
            case 1:
                edgeFlagCalculationMethod="Enhanced";
                break;
        }

        return edgeFlagCalculationMethod;
    }


    //print the shortest path from source vertex to destination vertex
    public void printShortestPath(Graph g, Vertex src, Vertex dest){

        String forwardPath="";
        String backwardPath="";

        // print shortest path
        if (hasPathToForward(commonVertex) && hasPathToBackward(commonVertex)) {
            System.out.println();
            System.out.println("Source: " + src.toString());
            System.out.println("Destination: " + dest.toString());
            System.out.println("Meeting point: " + commonVertex.toString());
            System.out.println("Shortest path estimate from src to mp: " + getShortestPathEstimateForward(commonVertex));
            System.out.println("Shortest path estimate from dest to mp: " + getShortestPathEstimateBackward(commonVertex));
            System.out.println("Total shortest path estimate from src to dest: " + (getShortestPathEstimateForward(commonVertex) + getShortestPathEstimateBackward(commonVertex)));
            System.out.println("Graph partitioning method: " + getGraphPartitioningMethod());
            System.out.println("Edge flag calculation method: " + getEdgeFlagCalculationMethod());
            System.out.println("Number of preprocessed nodes: " + statsPrep.getNoOfNodesScanned());
            System.out.println("Total number of effective relax operations: " + noOfDistImprovements);

            for (Edge e : getPathToForward(g,commonVertex)) {
                forwardPath =e+ "\n" + forwardPath;
            }
            System.out.println("Shortest path from source to meeting point:\n" + forwardPath);


            for (Edge e : getPathToBackward(g,commonVertex)) {
                backwardPath =e+ "\n" + backwardPath;
            }
            System.out.println("Shortest path from destination to meeting point:\n" + backwardPath);

        }
        else {
            System.out.printf("%s to %s         no path\n", src.toString(), dest.toString());
        }
    }

    public StatsForQuerying getStatsForQuery(){
        return statsQry;
    }

    public StatsForPreprocessing getStatsForPrep(){
        return statsPrep;
    }
}
