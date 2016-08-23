import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class Region {
    private int regionId; //region id based on 0-based index
    private Point p1; //bottom-left coordinate
    private Point p2; //top-right coordinate
    private ArrayList<Vertex> vertices;

    public Region(int regionId, Point p1, Point p2) {
        this.regionId=regionId;
        this.p1 = p1;
        this.p2 = p2;
        this.vertices=new ArrayList<Vertex>();
    }

    public Region(int regionId){
        this.regionId=regionId;
        this.p1=null;
        this.p2=null;
        this.vertices=new ArrayList<Vertex>();
    }

    public int getRegionId() {
        return regionId;
    }

    public Point getP1() {
        return p1;
    }

    public Point getP2() {
        return p2;
    }

    public void addVertex(Vertex vertex){
        if(vertex != null)
            vertices.add(vertex);
    }

    public List<Vertex> getVertices(){
        return vertices;
    }
}
