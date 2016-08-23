import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Stack;

//Bidirectional Dijkstr'a shortest path algorithm
//structure of Dijkstra's algorithm was adapted from http://algs4.cs.princeton.edu/44sp/DijkstraSP.java.html
public class DijkstraBidirectional{

    private Set<Vertex> settledNodes; //Set S - nodes v for which we know that shortest path estimate is equal to the shortest path distance

    private MinPriorityQueue minPQ_f; //nodes in priority queue for forward search
    private MinPriorityQueue minPQ_b; //nodes in priority queue for backward search

    private Map<Vertex, Vertex> parent_f; //parent nodes for forward search
    private Map<Vertex, Vertex> parent_b; //parent nodes for backward search

    private Map<Vertex, Integer> d_f; //shortest path estimate for forward
    private Map<Vertex, Integer> d_b; //shortest path estimate for backward

    private Vertex commonVertex; //the vertex where forward and backward Dijkstra algorithms meet

    //stats
    private StatsForQuerying stats;
    private int noOfNodeScans=0;
    private int noOfArcScans=0;
    private int noOfDistImprovements=0; //number of effective relax operations
    private long qryStartTime, qryEndTime;


    //g - graph with weighted edges
    //v - source vertex
    public DijkstraBidirectional(Graph g, Graph revGraph, Vertex src, Vertex dest, int queryId){
        System.out.println("Bidirectional Dijkstra's algorithm running..." + queryId);

        d_f=new HashMap<Vertex, Integer>(); //distance vector - shortest distance estimate from source to a particular vertex
        d_b=new HashMap<Vertex, Integer>(); //distance vector - shortest distance estimate from destination to a particular vertex
        parent_f=new HashMap<Vertex, Vertex>(); //parent vector
        parent_b=new HashMap<Vertex, Vertex>();
        settledNodes=new HashSet<Vertex>();

        //initialization
        initialization(g,src, dest);

        //dump all vertices to priority queue
        minPQ_f=new MinPriorityQueue(d_f.entrySet());
        minPQ_b=new MinPriorityQueue(d_b.entrySet());

        //stats
        stats=new StatsForQuerying();

        //mark start of query
        qryStartTime=System.nanoTime();
        while(!minPQ_f.isEmpty() && !minPQ_b.isEmpty()){

            //operation for forward Dijkstra's algorithm
            HeapEntry he_f= minPQ_f.extractMin();
            noOfNodeScans++; //increment the number of node scans

            Vertex vertex_f=he_f.getNode();
            if(settledNodes.contains(vertex_f)) {
                commonVertex = vertex_f;
                break; //forward and backward Dijkstra algorithms meet at vertex_f
            }
            else {
                settledNodes.add(vertex_f);
            }

            //relaxation
            //for each vertex in the adjacency list apply relax operation
            for(Edge e: g.getAdjList(vertex_f)){

                relax(e, true);

                noOfArcScans++;
            }


            //operation for backward Dijkstra's algorithm
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
            //for each vertex in the adjacency list apply relax operation
            for(Edge e: revGraph.getAdjList(vertex_b)){
                relax(e, false);

                noOfArcScans++;
            }
        } //end while

        //mark end of query
        qryEndTime=System.nanoTime();

        stats.setQryExecTime(qryEndTime-qryStartTime);
        stats.setNoOfNodesScanned(noOfNodeScans);
        stats.setNoOfArcsScanned(noOfArcScans);
        stats.setNoOfDistImprovements(noOfDistImprovements);
    }


    //initialization of relaxation technique
    private void initialization(Graph g, Vertex src, Vertex dest){

        //populate distance vector d with nodes from graph g, set each node's shortest path estimate to infinity
        for(Vertex v: g.getVertices()){
            if(v != null){
                d_f.put(v, (int)Double.POSITIVE_INFINITY);
                d_b.put(v, (int)Double.POSITIVE_INFINITY);
            }
        }
        d_f.put(src,0); //distance to the source vertex is set to zero
        d_b.put(dest,0);

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

        if(isForward){
            if (d_f.get(dest) > d_f.get(src) + e.getWeight()) {
                d_f.put(dest, d_f.get(src) + e.getWeight());
                noOfDistImprovements++; //increment the no of distance improvements

                minPQ_f.decreaseKey(dest, d_f.get(dest));
                parent_f.put(dest, src);
            }
        }else{ //backward search
            if (d_b.get(dest) > d_b.get(src) + e.getWeight()) {
                d_b.put(dest, d_b.get(src) + e.getWeight());
                noOfDistImprovements++; //increment the no of distance improvements

                minPQ_b.decreaseKey(dest, d_b.get(dest));
                parent_b.put(dest, src);
            }
        }
    }

    //get shortest path estimate from source vertex to vertex v (used by forward Dijkstra)
    private int getShortestPathEstimateForward(Vertex v){
        return d_f.get(v);
    }

    //get shortest path estimate from destination vertex to vertex v (used by backward Dijkstra)
    private int getShortestPathEstimateBackward(Vertex v){
        return d_b.get(v);
    }

    //is there a path between source vertex and vertex v?
    private boolean hasPathToForward(Vertex v) {
        return d_f.get(v) < (int)Double.POSITIVE_INFINITY;
    }

    //is there a path between source vertex and vertex v?
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
                    //push the edge to the stack
                    path.push(e);

                    break;
                }
            }
        }
        return path;
    }


    //returns the path between source vertex and vertex v, or null if no such path exists
    private Iterable<Edge> getPathToBackward(Graph g, Vertex v) {
        if (!hasPathToBackward(v)) return null;

        Stack<Edge> path = new Stack<Edge>();

        for (Vertex parentV = parent_b.get(v); parentV != null; v=parentV, parentV = parent_b.get(parentV)) {

            for(Edge e: g.getEdges()){
                if(e.getSource().equals(parentV) && e.getDestination().equals(v)){
                    //push the edge to the stack
                    path.push(e);

                    break;
                }
            }
        }
        return path;
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
        return stats;
    }
}
