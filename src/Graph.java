import java.awt.Point;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

//Graph class contains vertices, edges and edge weights necessary to run
//Structure of Graph class was adapted from http://algs4.cs.princeton.edu/44sp/EdgeWeightedDigraph.java.html

public class Graph {
    private int V; //number of vertices in graph
    private int E; //number of edges in graph
    private List<Edge>[] adjList; //list of adjacency lists for the entire graph - array of adjacency lists
    private List<Vertex> vertices;
    private List<Region> regions;
    private Set<Vertex> boundaryNodes;
    private boolean reverseGraph;
    private boolean preProcessed;

    //initialize graph by reading data about nodes, edges and edge weights
    public Graph(Scanner in) {
        try{
            buildGraph(in);
            System.out.println("Graph initialization complete");
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

    //generate new clone of graph which is a deep copy of original graph, but reversed edges with the same edge weights
    private Graph(Graph g){
        this.V=g.V();
        this.E=g.E();
        this.initializeGraph();

        for(Edge e: g.getEdges()){
            this.addEdge(e.generateReverseEdge());
        }

        reverseGraph=true;
    }


    public Graph generateReverseGraph(){
        return new Graph(this);
    }

    //read .gr file from input stream
    private void buildGraph(Scanner in){
        //read first character in line
        char c=in.next().charAt(0);

        while(in.hasNextLine()){

            switch (c){
                case 'c': { //line containing comments
                    //skip the line
                    in.nextLine();

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }
                case 'p': { //line containing the size of graph
                    //read the definition of a problem
                    in.next(); //read the word 'sp'
                    int noOfVertices = in.nextInt(); //get the number of vertices
                    if (noOfVertices < 0) throw new IllegalArgumentException("Number of vertices must be nonnegative");
                    this.V = noOfVertices;

                    int noOfEdges = in.nextInt(); //get the number of edges
                    if (noOfEdges < 0) throw new IllegalArgumentException("Number of edges must be nonnegative");
                    this.E = noOfEdges;

                    //initialize the graph by adding vertices to the graph and adj. list for each vertex
                    initializeGraph();

                    //skip to the next line
                    in.nextLine();

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }
                case 'a': { //the line containing information about edges
                    //read the edge information
                    Vertex src1 = vertices.get(in.nextInt());
                    Vertex dest1 = vertices.get(in.nextInt());
                    int weight1 = in.nextInt();

                    Edge directedEdge = new Edge(src1, dest1, weight1);
                    addEdge(directedEdge);

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }
            }//end switch
        } //end while

        reverseGraph=false;
    }


    //initialize the graph by adding vertices
    private void initializeGraph(){

        this.adjList= (ArrayList<Edge>[]) new ArrayList[V+1]; //array of adjacency lists
        this.vertices=new ArrayList<Vertex>();
        this.boundaryNodes=new HashSet<Vertex>();

        for (int vertexId =0; vertexId <= V; vertexId++) {

            if(vertexId==0){
                vertices.add(vertexId,null); //insert dummy vertex to the 0th index of the arraylist
            }else {

                //insert the vertex with id "vertexId" at position "vertexId" of arraylist
                vertices.add(vertexId, new Vertex(vertexId, "" + vertexId));

                //create an adjacency list for each vertex
                adjList[vertexId] = new ArrayList<Edge>();
            }
        }

        //collection of regions
        this.regions= new ArrayList<Region>();
    }


    //add edge to the graph
    //all edges are assumed to be directed
    private void addEdge(Edge e) {
        Vertex v = e.getSource();
        Vertex w = e.getDestination();

        //validate vertices
        validateVertex(v.getId());
        validateVertex(w.getId());

        //build adjacency list
        adjList[v.getId()].add(e);
    }

    //throw an exception unless 1 <= vertexId <= V
    private void validateVertex(int vertexId) {
        if (vertexId < 1 || vertexId > V)
            throw new IndexOutOfBoundsException("vertex " + vertexId + " is not between 1 and " + V);
    }

    //throw an exception unless 180.000000 W <= longitude <= 180.000000 E
    private void validateLongitude(int longitude){
        if(longitude < -180000000 || longitude > 180000000)
            throw new IndexOutOfBoundsException("longitude " + longitude + " is not between 180 deg West and 180 deg East");
    }

    //throw an exception unless 90.000000 S <= latitude <= 90.000000 N
    private void validateLatitude(int latitude){
        if(latitude < -90000000 || latitude > 90000000)
            throw new IndexOutOfBoundsException("latitude " + latitude + " is not between 90 deg South and 90 deg North");
    }


    public int V() {
        return V;
    }

    public int E() {
        return E;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public Vertex getVertex(int vertexId){
        return vertices.get(vertexId);
    }

    private List<Region> getRegions(){
        return regions;
    }


    //returns the list of all edges in the graph
    public Iterable<Edge> getEdges() {
        ArrayList<Edge> list = new ArrayList<Edge>();
        for (Vertex v: getVertices()) {
            if(v != null){
                for (Edge e: getAdjList(v)) {
                    list.add(e);
                }
            }
        }
        return list;
    }


    //returns the list of edges adjacent to a vertex v
    public Iterable<Edge> getAdjList(Vertex v){
        if(v!=null){
            return adjList[v.getId()];
        }

        return null;
    }

    //returns the pointer to specific edge
    private Edge getEdge(int srcNodeId, int destNodeId){
        Vertex srcNode=vertices.get(srcNodeId);
        Vertex destNode=vertices.get(destNodeId);

        for(Edge e: getAdjList(srcNode)){
            if(e.getSource().equals(srcNode) && e.getDestination().equals(destNode))
                return e;
        }

        return null;
    }

    //returns the string representation of this graph
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append("Vertices: " + V + "   Edges: " + E + "\n\n");

        for (Vertex v: getVertices()) {

            if(v != null){
                s.append(v.toString()+ "   ");
                s.append("Adj list: ");
                for (Edge e : getAdjList(v)) {
                    s.append(e.toString() + "   ");
                }
                s.append("\n");
            }
        }

        return s.toString();
    }


    //define regions using rectangular grid method
    //return the number of regions generated
    private int rectangularPartitioning(){

        int highestLat= -90000000; //top
        int lowestLat= 90000000; //bottom

        int highestLon=-180000000; //right
        int lowestLon=180000000;  //left

        //iterate through each node of the graph to calculate the top-left coordinate (l,t) and
        //bottom-right coordinate of (r,b) of the bounding box
        for(Vertex v: getVertices()){
            if(v !=null){
                int lon=v.getLongitude();
                int lat=v.getLatitude();

                if(lon>highestLon)
                    highestLon=lon;
                if(lon<lowestLon)
                    lowestLon=lon;
                if(lat>highestLat)
                    highestLat=lat;
                if(lat<lowestLat)
                    lowestLat=lat;
            }
        }

        //calculate the wigth (w) and height (h) of the bounding box
        int w= Math.abs(highestLon-lowestLon)+10; // right-left (offset of 10 is used to include outlier nodes)
        int h= Math.abs(highestLat-lowestLat)+10; //top-bottom (offset of 10 is used to include outlier nodes)

        //define the dimensions of the bounding box (x by y)
        int rows=Main.ROWS; // x
        int cols=Main.COLUMNS; // y

        //start the calculation of regions from bottom-left
        int l = lowestLon-1; //offset of 1 is used to include outlier nodes
        int b=lowestLat-1; // offset of 1 is used to include outlier nodes

        //start the id of the region
        int region_id=0;

        for(int i=0;i<rows; i++){
            for(int j=0;j<cols;j++){
                //define the bottom-left and top-right points of the region
                int p1_x=l+i*(w/rows);
                int p1_y=b+j*(h/cols);
                Point p1= new Point(p1_x, p1_y);

                int p2_x=l+(i+1)*(w/rows);
                int p2_y=b+(j+1)*(h/cols);
                Point p2=new Point(p2_x, p2_y);

                Region region=new Region(region_id, p1, p2);

                regions.add(region);

                //increment the region id
                region_id++;
            }
        }

        allocateNodesToRegions();

        System.out.println("Rectangular graph partitioning complete");

        return regions.size();
    }


    //enrich the vertices in the graph with coordinate information
    public void addNodeCoordinates(Scanner in) {
        //read first byte in line
        char c=in.next().charAt(0);

        //iterate through all nodes and add lon/lat data
        while(in.hasNextLine()){
            switch (c){
                case 'c': case 'p': {
                    //skip the line
                    in.nextLine();

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }

                case 'v': {
                    //read the node information
                    int nodeId = in.nextInt();
                    validateVertex(nodeId);

                    Vertex v = vertices.get(nodeId);
                    int lon = in.nextInt();
                    int lat = in.nextInt();

                    validateLongitude(lon);
                    validateLatitude(lat);

                    v.setLongitude(lon);
                    v.setLatitude(lat);

                    //read the byte in the next line
                    if (in.hasNext())
                        c = in.next().charAt(0);

                    break;
                }
            }//end switch
        }

        System.out.println("Node enrichment with coordinates complete");
    }


    private void allocateNodesToRegions() {
        //iterate through the nodes to allocate them into regions
        for(Vertex v: getVertices()){
            if(v != null){
                for(Region r: getRegions()){

                    //check if node v is within region r
                    if(!v.isAssignedToRegion() && belongsToRegion(v,r)){
                        v.setRegion(r.getRegionId()); //set node's region

                        r.addVertex(v); //add node to the region
                    }
                }
            }
        }
    }


    //check if vertex is in region
    private boolean belongsToRegion(Vertex vertex, Region region){
        int x=vertex.getLongitude();
        int y=vertex.getLatitude();

        int l=(int)region.getP1().getX();
        int r=(int)region.getP2().getX();

        int b=(int)region.getP1().getY();
        int t=(int)region.getP2().getY();

        if(x>=l && x<=r && y>=b && y<=t)
            return true;
        else
            return false;
    }

    //identify nodes which are incident to at least one interface edge
    //graph has to be partitioned into regions and nodes have to be allocated into regions prior to identifying the boundary nodes
    private void identifyBoundaryNodes(){
        for(Edge e: getEdges()){

            Vertex v=e.getSource();
            Vertex w=e.getDestination();

            //if the edge connects two nodes which belong to different regions
            if(v.getRegion()!=w.getRegion()){
                //set these nodes of edge as a boundary node
                v.setBoundaryNode();
                boundaryNodes.add(v);

                w.setBoundaryNode();
                boundaryNodes.add(w);
            }
        }
    }

    //get the list of boundary nodes
    private Set<Vertex> getBoundaryNodes(){
        return boundaryNodes;
    }


    //return the number of regions
    private int quadTreePartitioning(){

        int highestLat= -90000000; //top
        int lowestLat= 90000000; //bottom

        int highestLon=-180000000; //right
        int lowestLon=180000000;  //left

        //iterate through each node of the graph to calculate the top-left coordinate (l,t) and
        //bottom-right coordinate of (r,b) of the bounding box
        for(Vertex v: getVertices()){
            if(v !=null){
                int lon=v.getLongitude();
                int lat=v.getLatitude();

                if(lon>highestLon)
                    highestLon=lon;
                if(lon<lowestLon)
                    lowestLon=lon;
                if(lat>highestLat)
                    highestLat=lat;
                if(lat<lowestLat)
                    lowestLat=lat;
            }
        }

        //bottom-left
        int l = lowestLon-1; //offset of 1 is used to include outlier nodes
        int b=lowestLat-1; // offset of 1 is used to include outlier nodes
        int r=highestLon+10;
        int t=highestLat+10;

        //create quad-tree data structure
        QuadTree qt = new QuadTree((double)l/1000000, (double)b/1000000, (double)r/1000000, (double)t/1000000);
        for(Vertex v: vertices)
        {
            if(v != null){
                qt.insert(v); //add nodes to quad-tree
            }
        }

        //get list of quadrants
        List<Quadrant> quadrants=qt.getQuadrants();
        int region_id=0;

        //define regions based on quadrants
        for(Quadrant q: quadrants){

            //create new regions for each quadrant
            Region region=new Region(region_id);

            for(Vertex v: q.getVertices()){
                if(!v.isAssignedToRegion()){
                    v.setRegion(region.getRegionId()); //set node's region

                    region.addVertex(v);
                }
            }

            regions.add(region);

            region_id++;
        }

        System.out.println("Quad-tree based graph partitioning complete");

        return regions.size();
    }


    //graph partitioning base don kd-tree data structure
    private int kdTreePartitioning(){

        ArrayList<Vertex> listOfVertices=new ArrayList<>();
        for(Vertex v: getVertices()){
            if(v!=null)
                listOfVertices.add(v);
        }

        //create kd-tree data structure
        KdTree kdTree = new KdTree(listOfVertices);
        List<KdTree.KdNode> nodes=kdTree.getLeafNodes();

        int region_id=0;

        //define regions based on kd-tree nodes
        for(KdTree.KdNode node: nodes){

            //create new regions for each quadrant
            Region region=new Region(region_id);

            for(Vertex v: node.getVertices()){
                if(!v.isAssignedToRegion()){
                    v.setRegion(region.getRegionId()); //set node's region

                    region.addVertex(v);
                }
            }

            regions.add(region);

            region_id++;
        }

        System.out.println("Kd-tree based graph partitioning complete");

        return regions.size();
    }




    //calculate edge flags by applying Dijkstra to every node of a reverse graph
    private int naiveEdgeFlagCalculation(){

        int noOfPrepNodeScans=0;

        //iterate through each region
        for(Region r: this.getRegions()){
            //visit nodes in each region
            for(Vertex root: r.getVertices()){

                //each root node constitutes for one node to be preprocessed
                noOfPrepNodeScans++;

                //calculate a tree of shortest paths from root to all other nodes
                Graph reverseGraph=generateReverseGraph();
                Dijkstra sp=new Dijkstra(reverseGraph, root, noOfPrepNodeScans); //calculate shortest path tree from root to all other vertices
                int target_region=r.getRegionId(); //acquire root's region
                Map<Vertex, Integer> distanceVector = sp.getDistances();

                //iterate through all edges
                //inspect all edges after each tree calculation
                for (Edge e : this.getEdges()) {
                    Vertex edgeSrc = e.getSource();
                    Vertex edgeDest = e.getDestination();

                    int distToEdgeSrc=distanceVector.get(edgeSrc); //distance from root to src node of an edge
                    int distToEdgeDest=distanceVector.get(edgeDest); //distance from root to dest node of an edge

                    if (distToEdgeSrc - distToEdgeDest == e.getWeight()) {
                        e.setFlag(target_region);
                    }
                }
            }
        } //end for

        return noOfPrepNodeScans;
    }



    //calculate edge flags by applying Dijkstra to only boundary nodes of a reverse graph
    private int enhancedEdgeFlagCalculation(){

        int noOfPrepNodeScans=0;
        System.out.println("Number of boundary nodes: " + getBoundaryNodes().size());

        //iterate through boundary nodes
        for(Vertex root: getBoundaryNodes()){

            //each root node constitutes as one prep node scan
            noOfPrepNodeScans++;

            //calculate a tree of shortest paths from root to all other nodes
            Graph reverseGraph=generateReverseGraph();
            Dijkstra sp = new Dijkstra(reverseGraph, root, noOfPrepNodeScans);
            int target_region = root.getRegion();
            Map<Vertex, Integer> distanceVector = sp.getDistances();

            //iterate through all edges of the original graph
            for (Edge e : this.getEdges()) {
                Vertex edgeSrc = e.getSource();
                Vertex edgeDest = e.getDestination();

                int distToEdgeSrc=distanceVector.get(edgeSrc); //distance from root to src node of an edge
                int distToEdgeDest=distanceVector.get(edgeDest); //distance from root to dest node of an edge

                if (distToEdgeSrc - distToEdgeDest == e.getWeight()) {
                    e.setFlag(target_region);
                }

                //check if the dest node of the edge belong to the target region
                if(edgeSrc.getRegion() == edgeDest.getRegion() && edgeDest.getRegion()==target_region){
                    e.setFlag(target_region);
                }
            }
        } //end for

        return noOfPrepNodeScans;
    }



    //read edge flags from data
    private void readEdgeFlagsFromFile(File edgeFlagData){
        try{
            //try reading the file from disk
            Scanner sc=new Scanner(edgeFlagData);
            while(sc.hasNextLine()){

                int srcVertexId=sc.nextInt();
                int destVertexId=sc.nextInt();
                Edge e=this.getEdge(srcVertexId, destVertexId);

                String edgeFlags=sc.next();
                String[] edgeFlag=edgeFlags.split("");
                for(int i=0; i<edgeFlag.length; i++){
                    //variable i corresponds to the region id
                    boolean val=false;
                    if(edgeFlag[i].equals("1")) val=true;
                    e.setFlag(i,val);
                }
            }

            System.out.println("Edge flag data is read from disk");

        }catch(FileNotFoundException ex){
            ex.printStackTrace();
        }
    }



    //write edge flags into file
    private void writeEdgeFlagsIntoFile(File edgeFlagData){
        //write preprocessed data to disk
        try{
            //create new file
            edgeFlagData.createNewFile();
            // creates a FileWriter Object
            FileWriter writer = new FileWriter(edgeFlagData);

            // Writes the content to the file
            //iterate through the edges
            boolean firstLine=true;
            for(Edge e: this.getEdges()){
                if(!firstLine)
                    writer.write("\n");

                writer.write(e.getSource().getId()+ "\t" + e.getDestination().getId()+"\t");
                for(boolean val: e.getAllFlags()){
                    writer.write(val?"1":"0");
                }

                firstLine=false;
            }

            writer.flush();
            writer.close();

            System.out.println("Edge flag data is written to disk");

        }catch (IOException ex){
            ex.printStackTrace();
        }
    }


    //partition the graph in to regions
    private void partitionGraph(){
        int noOfRegions=0;

        switch(Main.GRAPH_PARTITIONING){
            case 0: {
                noOfRegions= this.rectangularPartitioning(); //rectangular graph partitioning
                break;
            }
            case 1:{
                noOfRegions= this.quadTreePartitioning(); //quad tree partitioning
                break;
            }
            case 2:{
                noOfRegions= this.kdTreePartitioning(); //kd tree partitioning
                break;
            }
        }

        //set the size of array which contains the edge flags (should be equal to number of regions)
        for(Edge e: this.getEdges()){
            e.setEdgeFlagArraySize(noOfRegions);
        }
    }


    //calculate edge flags
    private int calculateEdgeFlags(){

        int noOfPrepNodeScans=0;

        //calculate edge flags
        switch (Main.EDGE_FLAG_CALCULATION){
            case 0:
                noOfPrepNodeScans= this.naiveEdgeFlagCalculation();
                break;
            case 1:
                identifyBoundaryNodes();
                noOfPrepNodeScans= this.enhancedEdgeFlagCalculation();
                break;
        }

        return noOfPrepNodeScans;
    }


    //preprocessing operation
    public int preprocess(StatsForPreprocessing statsPrep){
        int noOfPrepNodeScans=0;
        File edgeFlagData=null;
        if(!reverseGraph)
            edgeFlagData=new File(".//input//edgeFlagData.txt");
        else
            edgeFlagData=new File(".//input//edgeFlagDataRev.txt");

        //if the graph is NOT preprocessed
        if(!preProcessed){
            if(edgeFlagData.exists()){
                partitionGraph();
                readEdgeFlagsFromFile(edgeFlagData);
                preProcessed=true;
                statsPrep.setReadFromFile(true);
            }else{ //if the graph is NOT preprocessed but there is no edge flag data available, then do preprocessing and write edge flag data into file system

                //step 1: partition the graph into regions
                partitionGraph();

                //step 2: calculate edge flags
                noOfPrepNodeScans=calculateEdgeFlags();

                //write preprocessed data to disk
                writeEdgeFlagsIntoFile(edgeFlagData);
                preProcessed=true;
                statsPrep.setReadFromFile(false);
            }
        }


        return noOfPrepNodeScans;
    }
}
