import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

//Dijkstra's algorithm which uses Euclidean distance as heuristic
//structure of Dijkstra's algorithm was adapted from http://algs4.cs.princeton.edu/44sp/DijkstraSP.java.html
public class DijkstraForGeoNets{

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
    public DijkstraForGeoNets(Graph g, Vertex src, Vertex dest, int queryId){
        System.out.println("Dijkstra's algorithm for geographical networks is running..." + queryId);

        d=new HashMap<Vertex, Integer>(); //distance vector - shortest path estimate from src vertex to every other vertex
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
                break; //while loop

            //re-weight edges adjacent to the node
            for(Edge e: g.getAdjList(vertex)){
                reWeight(e, dest);
            }

            //relax edges adjacent to the node
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

    //initialization of relaxation technique
    private void initialization(Graph g, Vertex src){

        //populate distance vector d with nodes from graph g
        // set each node's shortest path estimate to infinity
        for(Vertex v: g.getVertices()){
            if(v != null)
                d.put(v, (int)Double.POSITIVE_INFINITY);
        }
        d.put(src,0); //distance to the source vertex is set to zero

        //populate the parent array with nodes
        for(Vertex v: g.getVertices()){
            if(v != null)
                parent.put(v, null); //set the parent of all nodes to null
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

    private void reWeight(Edge e, Vertex dest){
        Vertex begin = e.getSource();
        Vertex end = e.getDestination();

        int beginToDest=dist(begin, dest); //straight line distance from node begin to dest
        int endToDest=dist(end, dest); //straight line distance from node end to dest
        int newWeight = e.getWeight() - beginToDest + endToDest;
        e.reWeight(newWeight);
    }

    //distance calculation code was taken from http://www.geodatasource.com/developers/java
    //distance is returned in unit used by DIMACS
    private int dist(Vertex a, Vertex b){

        //convert lon/lat into x/y coordinate system
        double latA=(double)a.getLatitude() * 0.000001;
        double lonA=(double)a.getLongitude() * 0.000001;

        double latB=(double)b.getLatitude() * 0.000001;
        double lonB=(double)b.getLongitude() * 0.000001;

        double theta = lonA - lonB;
        double dist = Math.sin(deg2rad(latA)) * Math.sin(deg2rad(latB)) +
                Math.cos(deg2rad(latA)) * Math.cos(deg2rad(latB)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515 * 1.609344;  //in kilometers
        dist=dist*1000; //in meters

        //the formula used to ajdust the arc weight is new_cost = (int)(TIGER_Line-Cost * 10 + 0.5) as per DIMACS rules
        int new_cost = (int)(dist * 10 + 0.5);

        return new_cost;
    }

	//this function converts decimal degrees to radians
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

	//this function converts radians to decimal degrees
    private double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
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


    //print the shortest path from source vertex to destination vertex
    public void printShortestPath(Graph g, Vertex src, Vertex dest){

        String path="";

        // print shortest path
        if (hasPathTo(dest)) {
            System.out.println();
            System.out.println("Source: " + src.toString());
            System.out.println("Destination: " + dest.toString());
            System.out.println("Shortest path estimate: " + getShortestPathEstimate(dest));
            System.out.println("Total number of effective relax operations: " + noOfDistImprovements);

            for (Edge e : getPathTo(g,dest)) {
                path =e+ "\n" + path;
            }
            System.out.println("Shortest path: " + path);

        }
        else {
            System.out.printf("%s to %s         no path\n", src.toString(), dest.toString());
        }
    }


    public StatsForQuerying getStatsForQuery(){
        return stats;
    }
}
