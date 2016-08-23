public class Edge {
    private final Vertex source;
    private final Vertex destination;
    private int weight; //represents distance between src and dest vertices of this edge
    private boolean[] flags; //flag to indicate there is s.p. to particular region from edge's src vertex over edge e
    private int edgeFlagArraySize; //varies depending on the graph partitioning type used

    //constructor
    public Edge(Vertex source, Vertex destination, int weight) {

        this.source = source;
        this.destination = destination;
        this.weight = weight;
    }

    public void setEdgeFlagArraySize(int size){
        this.edgeFlagArraySize=size;
        this.flags =new boolean[edgeFlagArraySize]; //by default all flags are set to 0
    }

    //from
    public Vertex getSource() {
        return source;
    }

    //to
    public Vertex getDestination() {
        return destination;
    }

    public int getWeight() {
        return weight;
    }

    public void reWeight(int newWeight) {
        if(newWeight >=0)
            this.weight = newWeight;
    }

    //by default this method sets the flag to true
    public void setFlag(int region){
        if(region>=0 && region<edgeFlagArraySize){
            flags[region]=true;
        }
    }

    public void setFlag(int region, boolean value){
        if(region>=0 && region<edgeFlagArraySize){
            flags[region]=value;
        }
    }


    public boolean isFlagged(Vertex v, int target_region){
        if(v.equals(source) && flags[target_region]==true)
            return true;
        return false;
    }

    public boolean[] getAllFlags(){
        return flags;
    }

    //generate reverse of this edge but with the same edge weight
    public Edge generateReverseEdge(){
        Vertex src=this.getSource();
        Vertex dest=this.getDestination();

        Edge revEdge=new Edge(dest, src, this.weight);
        return revEdge;
    }

    @Override
    public String toString() {
        return source.toString() + "->" + destination.toString() + ": " + getWeight();
    }
}
