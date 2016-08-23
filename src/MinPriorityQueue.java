import java.util.HashMap;
import java.util.Set;
import java.util.Map.Entry;

//Minimum priority queue data structure
//structure of priority queue was adapted from http://algs4.cs.princeton.edu/44sp/IndexMinPQ.java.html
public class MinPriorityQueue{
    private HeapEntry[] pq; //store entries (node and its current shortest path estimate) at indices from 1 to N
    private int N; //number of items in priority queue
    private HashMap<Vertex, Integer> reversePQ; //maps heap values to heap indexes

    public MinPriorityQueue(Set<Entry<Vertex, Integer>> entries) {
        N = entries.size();
        pq = new HeapEntry[N + 1]; //pq[0] is unused, heap is from pq[1] through pq[N]
        int i=0; //heap array index
        reversePQ=new HashMap<Vertex, Integer>();

        for(Entry<Vertex, Integer> e: entries){
            HeapEntry he= new HeapEntry(e.getKey(), e.getValue());
            pq[i+1]=he;

            //keep the index of each vertex in pq in a hashmap
            reversePQ.put(e.getKey(), i+1);

            i++;
        }

        //restore min heap property after populating the heap
        for (int k = N/2; k >= 1; k--) {
            sink(k);
        }

        //check if min heap maintains invariants
        assert isMinHeap();
    }

    public boolean isEmpty() {
        return N == 0;
    }

    public HeapEntry extractMin() {
        //before extracting, exchange the keys at root (smallest value) with one at the end of the array
        exch(1, N);
        HeapEntry minEntry = pq[N--];
        sink(1);

        assert isMinHeap();

        return minEntry;
    }


    public void decreaseKey(Vertex srcNode, Integer newShortestDistance) {
        //find the relevant heap entry in the array
        int index = reversePQ.get(srcNode);

        HeapEntry he= pq[index];
        he.setNewDistance(newShortestDistance);

        swim(index);
    }


    //helper functions to restore the heap invariant
    private void swim(int k) {
        while (k > 1 && greater(k/2, k)) {
            exch(k, k/2); //exchange keys at positions k (child) and k/2 (parent)
            k = k/2;
        }
    }

    private void sink(int k) {
        while (2*k <= N) {
            int j = 2*k;
            if (j < N && greater(j, j+1)) j++;
            if (!greater(k, j)) break;
            exch(k, j);
            k = j;
        }
    }


    //helper functions for compares and swaps
    private boolean greater(int i, int j) {
        return (pq[i].compareTo(pq[j])) > 0;
    }

    private void exch(int i, int j) {
        HeapEntry swap = pq[i];
        pq[i] = pq[j];
        reversePQ.put(pq[j].getNode(),i);
        pq[j] = swap;
        reversePQ.put(swap.getNode(), j);
    }

    // is pq[1..N] a min heap?
    private boolean isMinHeap() {
        return isMinHeap(1);
    }

    // is subtree of pq[1..N] rooted at k a min heap?
    private boolean isMinHeap(int k) {
        if (k > N) return true;
        int left = 2*k, right = 2*k + 1;
        if (left  <= N && greater(k, left))  return false;
        if (right <= N && greater(k, right)) return false;
        return isMinHeap(left) && isMinHeap(right);
    }
}
