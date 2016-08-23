import java.util.Comparator;

//Vertex class to represent nodes in the graph
public class Vertex {
    final private int id;
    final private String name;
    private int lon; //longitude - x coordinate
    private int lat; //latitude - y coordinate
    private int region; //the region where this vertex belongs to (each vertex must belong to exactly one region)
    private boolean isBoundaryNode;
    private boolean isAssignedToRegion;

    public Vertex(int id, String name) {
        if (id < 0) throw new IndexOutOfBoundsException("Vertex id must be nonnegative integers");

        this.id = id;
        this.name = name;
        this.region=-1; //initially nodes are not allocated to regions
    }

    public Vertex(int id, String name, int lon, int lat) {
        if (id < 0) throw new IndexOutOfBoundsException("Vertex id must be nonnegative integers");

        this.id = id;
        this.name = name;
        this.lon = lon;
        this.lat = lat;
        this.region=-1; //initially nodes are not allocated to regions
    }

    public boolean isAssignedToRegion(){
        return isAssignedToRegion;
    }

    public void setRegion(int region) {
        this.region = region;
        isAssignedToRegion=true;
    }

    public int getRegion() {
        return region;
    }

    public int getId() {
        return id;
    }

    public int getLatitude() {
        return lat;
    }

    public int getLongitude() {
        return lon;
    }

    public void setLongitude(int lon) {
        this.lon = lon;
    }

    public void setLatitude(int lat) {
        this.lat = lat;
    }


    public boolean isBoundaryNode() {
        return isBoundaryNode;
    }

    public void setBoundaryNode(){
        this.isBoundaryNode=true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vertex vertex = (Vertex) o;

        if (id != vertex.id) return false;
        return name.equals(vertex.name);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "v_" + name;
    }

    //compare vertices by values in x-axis
    public static Comparator<Vertex> X_COMPARATOR = new Comparator<Vertex>() {
        public int compare(Vertex v1, Vertex v2) {
            if (v1.getLongitude()  < v2.getLongitude())
                return -1;
            if (v1.getLongitude() > v2.getLongitude())
                return 1;
            return 0;
        }
    };

    //compare vertices by values in y-axis
    public static Comparator<Vertex> Y_COMPARATOR = new Comparator<Vertex>() {
        public int compare(Vertex v1, Vertex v2) {
            if (v1.getLatitude() < v2.getLatitude())
                return -1;
            if (v1.getLatitude() > v2.getLatitude())
                return 1;
            return 0;
        }
    };

    //choose how to compare the vertices (either by x-axis or by y-axis determined by value in depth)
    public int compareTo(int depth, Vertex other) {
        int axis = depth % 2;
        if (axis == 0) //if even depth then compare by x axis
            return X_COMPARATOR.compare(this, other);
        else //if odd then compare by y axis
            return Y_COMPARATOR.compare(this, other);
    }
}
