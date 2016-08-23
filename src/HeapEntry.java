//heap entry used by minimum priority queue data structure
public class HeapEntry implements Comparable<HeapEntry>{
    private final Vertex node; //node in the graph
    private Integer d; //shortest path estimate

    public HeapEntry(Vertex node, Integer d) {
        this.node = node;
        this.d = d;
    }

    public Vertex getNode() {
        return node;
    }

    public void setNewDistance(Integer d) {
        this.d = d;
    }

    @Override
    public int compareTo(HeapEntry other) {
        return Integer.valueOf(this.d).compareTo(other.d);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HeapEntry heapEntry = (HeapEntry) o;

        if (d != heapEntry.d) return false;
        return node.equals(heapEntry.node);

    }

    @Override
    public int hashCode() {
        int result = node.hashCode();
        result = 31 * result + d;
        return result;
    }
}
