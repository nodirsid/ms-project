import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Kd-tree data structure
//Algorithm kd-tree was adapted from https://www.cse.unr.edu/~bebis/CS302/Handouts/kdtree.pdf
//Structure of kd-tree data structure was partially adapted from https://github.com/phishman3579/java-algorithms-implementation

public class KdTree{

    private KdNode root = null;
    private static final int X_AXIS = 0;
    private static final int Y_AXIS = 1;

    public KdTree(List<Vertex> list) {
        this.root = createNode(list, 0);
    }

    private static KdNode createNode(List<Vertex> list, int depth) {
        if (list == null || list.size() == 0) {
            return null;
        }

        int axis = depth % 2;
        if (axis == X_AXIS)
            Collections.sort(list, Vertex.X_COMPARATOR);
        else //if (axis == Y_AXIS)
            Collections.sort(list, Vertex.Y_COMPARATOR);


        KdNode node = null;
        List<Vertex> less = new ArrayList<Vertex>(list.size());
        List<Vertex> more = new ArrayList<Vertex>(list.size());

        if (list.size() > 0) {
            int medianIndex = list.size() / 2;

            if(list.size()<=Main.MAX_VERTICES_PER_REGION){
                return new KdNode(list);
            }else{

                node= new KdNode();
                for (Vertex vertex: list) {
                    if (vertex.compareTo(depth, list.get(medianIndex))<0) {
                        less.add(vertex);
                    } else {
                        more.add(vertex);
                    }
                }

                node.lesser = createNode(less, depth + 1);
                node.greater = createNode(more, depth + 1);
            }
        }

        return node;
    }


    public List<KdNode> getLeafNodes(){
        return getTree(this.root);
    }

    private List<KdNode> getTree(KdNode root) {
        List<KdNode> list=new ArrayList<KdNode>();

        if (root == null)
            return list;

        if (root.lesser != null) {
            if(root.lesser.vertices != null) {
                list.add(root.lesser);
            }

            list.addAll(getTree(root.lesser));

        }

        if (root.greater != null) {
            if(root.greater.vertices != null){
                list.add(root.greater);
            }

            list.addAll(getTree(root.greater));
        }

        return list;
    }



    public static class KdNode{

        private final List<Vertex> vertices;
        private KdNode lesser = null;
        private KdNode greater = null;

        public KdNode() {
            this.vertices = null;
        }

        public KdNode(List<Vertex> vertices) {
            this.vertices = vertices;
        }

        public List<Vertex> getVertices(){
            return vertices;
        }
    }
}