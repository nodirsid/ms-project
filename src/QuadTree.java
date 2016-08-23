import java.util.List;
import java.util.ArrayList;

public class QuadTree{

    private Quadrant root;

    public QuadTree(double minX, double minY, double maxX, double maxY) {
        this.root = new Quadrant(minX, minY, maxX - minX, maxY - minY, null);
    }

    public void insert(Vertex vertex) {
        this.insert(this.root, vertex);
    }


    public List<Quadrant> getQuadrants() {
        final List<Quadrant> list = new ArrayList<Quadrant>();
        this.traverse(this.root, list);
        return list;
    }


    public void traverse(Quadrant quadrant, List<Quadrant> list) {
        switch (quadrant.getQuadrantType()) {
            case LEAF:
            case EMPTY:
                list.add(quadrant);
                break;

            case POINTER:
                this.traverse(quadrant.getNe(), list);
                this.traverse(quadrant.getSe(), list);
                this.traverse(quadrant.getSw(), list);
                this.traverse(quadrant.getNw(), list);
                break;
        }
    }


    private void insert(Quadrant quadrant, Vertex vertex) {
        switch (quadrant.getQuadrantType()) {
            case EMPTY: {
                quadrant.addVertex(vertex);
                if(quadrant.getVertices().size()>=Main.MAX_VERTICES_PER_QUADRANT){
                    quadrant.setQuadrantType(QuadrantType.LEAF);
                }
                break;
            }
            case LEAF: {
                this.split(quadrant);
                this.insert(quadrant, vertex);
                break;
            }
            case POINTER: {
                Quadrant childQuadrant=this.getChildQuadrant(quadrant,vertex);
                this.insert(childQuadrant, vertex);
                break;
            }
            default:
                throw new IllegalArgumentException("Invalid quadrant type in parent");
        }
    }


    private void split(Quadrant quadrant) {
        ArrayList<Vertex> oldVertices = quadrant.getVertices();
        quadrant.addVertex(null); //set the quadrant's vertices to null

        quadrant.setQuadrantType(QuadrantType.POINTER);

        double x = quadrant.getX();
        double y = quadrant.getY();
        double hw = quadrant.getW() / 2;
        double hh = quadrant.getH() / 2;

        //split into four
        quadrant.setNw(new Quadrant(x, y, hw, hh, quadrant));
        quadrant.setNe(new Quadrant(x + hw, y, hw, hh, quadrant));
        quadrant.setSw(new Quadrant(x, y + hh, hw, hh, quadrant));
        quadrant.setSe(new Quadrant(x + hw, y + hh, hw, hh, quadrant));

        for(Vertex v: oldVertices){
            this.insert(quadrant, v);
        }
    }


    private Quadrant getChildQuadrant(Quadrant parent, Vertex vertex) {
        double x=(double)vertex.getLongitude()/1000000;
        double y=(double)vertex.getLatitude()/1000000;

        double mx = parent.getX() + parent.getW() / 2;
        double my = parent.getY() + parent.getH() / 2;
        if (x < mx) {
            return y < my ? parent.getNw() : parent.getSw();
        } else {
            return y < my ? parent.getNe() : parent.getSe();
        }
    }
}