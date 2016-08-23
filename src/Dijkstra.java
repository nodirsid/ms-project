import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.Stack;

//Dijkstra's shortest path algorithm
//structure of Dijkstra's algorithm was adapted from http://algs4.cs.princeton.edu/44sp/DijkstraSP.java.html
public class Dijkstra{

    private Set<Vertex> settledNodes; //Set S - nodes v for which we know that shortest path estimate is equal to the shortest path distance
    private MinPriorityQueue minPQ; //nodes in priority queue
    private Map<Vertex, Vertex> parent; //parent nodes
    private Map<Vertex, Integer> d; //shortest path estimate

    //stats
    private StatsForQuerying stats;
    private int noOfNodeScans=0;
    private int noOfArcScans=0;
    private int noOfDistImprovements=0; //number of effective relax operations
    private long qryStartTime, qryEndTime;


    //g - graph with weighted edges
    //v - source vertex
    public Dijkstra(Graph g, Vertex src, Vertex dest, int queryId){
        System.out.println("Simple Dijkstra's algorithm running..." + queryId);

        d=new HashMap<Vertex, Integer>(); //distance vector - shortest distance estimate from source to a particular vertex
        parent=new HashMap<Vertex, Vertex>(); //parent vector
        settledNodes=new HashSet<Vertex>();

        //initialization
        initialization(g,src);

        //dump all vertices to priority queue
        minPQ=new MinPriorityQueue(d.entrySet());

        //stats
        stats=new StatsForQuerying();

        //mark start of query
        qryStartTime=System.nanoTime();
        while(!minPQ.isEmpty()){
            HeapEntry he= minPQ.extractMin();
            noOfNodeScans++; //increment the number of node scans

            Vertex vertex=he.getNode();
            settledNodes.add(vertex);

            //check if the extracted node is a destination node
            if(vertex.equals(dest))
                break; //the while loop

            //relaxation
            //for each vertex in the adjacency list apply relax operation
            for(Edge e: g.getAdjList(vertex)){
                relax(e);

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


    //overloaded constructor (used by edge flag algorithm)
    //no need to keep stats
    public Dijkstra(Graph g, Vertex src, int id){
        System.out.println("Edge flag calculation using Dijkstra..." + id);

        d=new HashMap<Vertex, Integer>(); //distance vector - shortest distance estimate from source to a particular vertex
        parent=new HashMap<Vertex, Vertex>(); //parent vector
        settledNodes=new HashSet<Vertex>();

        //initialization
        initialization(g,src);

        //dump all vertices to priority queue
        minPQ=new MinPriorityQueue(d.entrySet());

        while(!minPQ.isEmpty()){
            HeapEntry he= minPQ.extractMin();
            Vertex vertex=he.getNode();
            settledNodes.add(vertex);

            //relaxation
            //for each vertex in the adjacency list apply relax operation
            for(Edge e: g.getAdjList(vertex)){
                relax(e);
            }
        }
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
                    //push the edge to the stack
                    path.push(e);

                    break;
                }
            }
        }
        return path;
    }

    //return shortest path estimate vector (used by edge flag algorithm when calculating the edge flags)
    public Map<Vertex,Integer> getDistances(){
        return d;
    }

    //print the shortest path from source vertex to destination vertex
    public void printShortestPath(Graph g, Vertex src, Vertex dest){

        String path="";

        // print shortest path
        if (hasPathTo(dest)) {
            System.out.println("Source: " + src.toString());
            System.out.println("Destination: " + dest.toString());
            System.out.println("Shortest path estimate: " + getShortestPathEstimate(dest));
            System.out.println("Total number of effective relax operations: " + noOfDistImprovements);

            for (Edge e : getPathTo(g,dest)) {
                path =e+ "\n" + path;
            }
            System.out.println("Shortest path from src to dest:\n" + path);

        }
        else {
            System.out.printf("%s to %s         no path\n", src.toString(), dest.toString());
        }
    }

    public StatsForQuerying getStatsForQuery(){
        return stats;
    }
}
