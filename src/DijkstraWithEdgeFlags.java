import java.util.Set;
import java.util.Map;
import java.util.Stack;
import java.util.HashMap;
import java.util.HashSet;

//Dijkstra's algorithm based on edge flags
//structure of Dijkstra's algorithm was adapted from http://algs4.cs.princeton.edu/44sp/DijkstraSP.java.html
public class DijkstraWithEdgeFlags{

    private Set<Vertex> settledNodes; //Set S - nodes v for which we know that shortest path estimate is equal to the shortest path distance
    private MinPriorityQueue minPQ; //nodes in priority queue
    private Map<Vertex, Vertex> parent; //parent nodes
    private Map<Vertex, Integer> d; //shortest path estimate

    //stats
    private StatsForQuerying statsQry;
    private StatsForPreprocessing statsPrep;
    private int noOfNodeScans=0;
    private int noOfArcScans=0;
    private int noOfDistImprovements=0; //number of effective relax operations
    private long qryStartTime, qryEndTime;
    private int noOfPrepNodeScans=0;
    private long prepStartTime, prepEndTime;

    //g - graph with weighted edges
    //v - source vertex
    public DijkstraWithEdgeFlags(Graph g, Vertex src, Vertex dest, int queryId){

        System.out.println("Dijkstra's algorithm with edge flags is running..." + queryId);

        d=new HashMap<Vertex, Integer>(); //distance vector - shortest distance estimate from source to a particular vertex
        parent=new HashMap<Vertex, Vertex>(); //parent vector
        settledNodes=new HashSet<Vertex>();

        //PRE-PROCESSING STEP
        System.out.println("Preprocessing started");

        //stats
        statsPrep=new StatsForPreprocessing();

        //mark start of prep
        prepStartTime=System.nanoTime();

        noOfPrepNodeScans= g.preprocess(statsPrep);

        //mark end of prep
        prepEndTime=System.nanoTime();
        statsPrep.setPrepExecTime(prepEndTime-prepStartTime);
        statsPrep.setNoOfNodesScanned(noOfPrepNodeScans);

        System.out.println("Preprocessing ended");


        //SHORTEST PATH CALCULATION STEP
        //get the region ID where destination node belongs to
        int target_region=dest.getRegion();

        //initialization
        initialization(g,src);

        //dump all vertices to priority queue
        minPQ=new MinPriorityQueue(d.entrySet());

        //stats
        statsQry=new StatsForQuerying();

        //mark start of query
        qryStartTime=System.nanoTime();
        while(!minPQ.isEmpty()){
            HeapEntry he= minPQ.extractMin();
            noOfNodeScans++; //increment the number of node scans

            Vertex vertex=he.getNode();
            settledNodes.add(vertex);

            //check if extracted node is destination node
            if(vertex.equals(dest)){
                break; //end the computation
            }

            //relaxation
            //selectively apply relax operation for each vertex in the adjacency list
            for(Edge e: g.getAdjList(vertex)){

                if(!e.isFlagged(vertex, target_region)) continue; //skip edges which cannot be on the shortest path to the destination node

                relax(e);

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
    private void initialization(Graph g, Vertex src){

        //populate distance vector d with nodes from graph g, set each node's shortest path estimate to infinity
        for(Vertex v: g.getVertices()){
            if(v != null)
                d.put(v, (int)Double.POSITIVE_INFINITY);
        }
        d.put(src,0); //distance to the source vertex is set to zero

        //populate the parent array with nodes
        for(Vertex v: g.getVertices()){
            if(v!= null)
                parent.put(v, null);
        }
    }

    //relaxation
    private void relax(Edge e){
        Vertex src = e.getSource();
        Vertex dest = e.getDestination();

        if (d.get(dest) > d.get(src) + e.getWeight()) {
            d.put(dest, d.get(src) + e.getWeight());
            noOfDistImprovements++; //increment the no of distance improvements

            minPQ.decreaseKey(dest, d.get(dest));
            parent.put(dest, src);
        }
    }

    //get shortest path estimate from source vertex to vertex v
    private int getShortestPathEstimate(Vertex v){
        return d.get(v);
    }

    //is there a path between source vertex and vertex v?
    private boolean hasPathTo(Vertex v) {
        return d.get(v) < (int)Double.POSITIVE_INFINITY;
    }

    //returns the path between source vertex and vertex v, or null if no such path exists
    private Iterable<Edge> getPathTo(Graph g, Vertex v) {
        if (!hasPathTo(v)) return null;

        Stack<Edge> path = new Stack<Edge>();

        for (Vertex parentV = parent.get(v); parentV != null; v=parentV, parentV = parent.get(parentV)) {

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
        String partitioningMethod=null;
        switch(Main.GRAPH_PARTITIONING){
            case 0:
                partitioningMethod="Rectangular";
                break;
            case 1:
                partitioningMethod="Quad tree based";
                break;
            case 2:
                partitioningMethod="Kd tree based";
                break;
        }

        return partitioningMethod;
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

        String path="";

        // print shortest path
        if (hasPathTo(dest)) {
            System.out.println();
            System.out.println("Source: " + src.toString());
            System.out.println("Destination: " + dest.toString());
            System.out.println("Shortest path estimate: " + getShortestPathEstimate(dest));
            System.out.println("Graph partitioning method: " + getGraphPartitioningMethod());
            System.out.println("Edge flag calculation method: " + getEdgeFlagCalculationMethod());
            System.out.println("Number of preprocessed nodes: " + noOfPrepNodeScans);
            System.out.println("Total number of effective relax operations: " + noOfDistImprovements);

            for (Edge e : getPathTo(g,dest)) {
                path =e+ "\n" + path;
            }
            System.out.println("Shortest path:\n" + path);

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
