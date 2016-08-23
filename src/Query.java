//p2p query
public class Query {
    private int id;
    private int srcVertex;
    private int destVertex;

    public Query(int id, int srcVertex, int destVertex) {
        this.id=id;
        this.srcVertex = srcVertex;
        this.destVertex = destVertex;
    }

    public int getId() {
        return id;
    }

    public int getSrcVertex() {
        return srcVertex;
    }

    public int getDestVertex() {
        return destVertex;
    }
}
