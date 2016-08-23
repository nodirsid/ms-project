import java.util.ArrayList;

//Quadrant class to be used by quad-tree data structure
public class Quadrant {

    private double x; //bottom-left coordinate
    private double y; //bottom-left coordinate
    private double w; //width of a quadrant
    private double h; //height of a quadrant
    private Quadrant parent; //parent of node
    private ArrayList<Vertex> vertices; //list of vertices in this quadrant
    private QuadrantType quadrantType = QuadrantType.EMPTY;
    private Quadrant nw;
    private Quadrant ne;
    private Quadrant sw;
    private Quadrant se;

    //construct new quadrant
    public Quadrant(double x, double y, double w, double h, Quadrant parent) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.parent = parent;
        vertices=new ArrayList<>();
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getW() {
        return w;
    }

    public double getH() {
        return h;
    }

    public void addVertex(Vertex v) {
        if(v != null) {
            this.vertices.add(v);
        }else{ //if null vertex is added, then set the vertices to null as well
            this.vertices=null;
        }
    }

    public ArrayList<Vertex> getVertices() {
        return this.vertices;
    }

    public void setQuadrantType(QuadrantType newType) {
        this.quadrantType = newType;
    }

    public QuadrantType getQuadrantType() {
        return this.quadrantType;
    }


    public void setNw(Quadrant nw) {
        this.nw = nw;
    }

    public void setNe(Quadrant ne) {
        this.ne = ne;
    }

    public void setSw(Quadrant sw) {
        this.sw = sw;
    }

    public void setSe(Quadrant se) {
        this.se = se;
    }

    public Quadrant getNe() {
        return ne;
    }

    public Quadrant getNw() {
        return nw;
    }

    public Quadrant getSw() {
        return sw;
    }

    public Quadrant getSe() {
        return se;
    }
}

enum QuadrantType {
    EMPTY,
    LEAF,
    POINTER
}
