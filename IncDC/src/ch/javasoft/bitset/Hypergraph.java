package ch.javasoft.bitset;

import evidenceset.IEvidenceSet;
import predicates.sets.PredicateBitSet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Set;

public class Hypergraph extends BitSet implements Cloneable {
    private int numOfPredicates;
    private ArrayList<PredicateBitSet> edges;

    public Hypergraph(int predicateNum, int edgeNum) {
        numOfPredicates = predicateNum;
        edges = new ArrayList<>();
        for (int i = 0; i < edgeNum; ++i) {
            LongBitSet bs=new LongBitSet(predicateNum);
            PredicateBitSet predicateBitSet=new PredicateBitSet(bs);
            //PredicateBitset也做了改动
//            PredicateBitSet bs = new PredicateBitSet(predicateNum);
//            edges.add(bs);
            edges.add(predicateBitSet);
        }
    }
    public Hypergraph(int predicateNum/*, int edgeNum, */, Set<PredicateBitSet> iEvidenceSet) {
        numOfPredicates = predicateNum;
        edges = new ArrayList<>();
        for (PredicateBitSet predicate : iEvidenceSet) {

            PredicateBitSet predicateBitSet=new PredicateBitSet(predicate);
//            System.out.println(predicateBitSet.toString());
            //PredicateBitset也做了改动
//            PredicateBitSet bs = new PredicateBitSet(predicateNum);
//            edges.add(bs);
            edges.add(predicateBitSet);
        }
    }
    public Hypergraph(ArrayList<PredicateBitSet> edges) {
        if (edges.size() > 0) {
            numOfPredicates = edges.get(0).size();
//            numOfPredicates = edges.get(0).length();
        } else {
            numOfPredicates = 0;
        }
        this.edges = new ArrayList<>();
        for (PredicateBitSet bs : edges) {
//            this.edges.add(bs.clone());
            this.edges.add(bs);
        }
    }


    public Hypergraph(String pathName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(pathName));

        edges = new ArrayList<>();
        ArrayList<ArrayList<Integer>> edgeByIndices = new ArrayList<>();
        int maxVertext = 0;
        int numOfEdges = 0;

        while (true) {
            String line = br.readLine();
            if (line == null) break;
            numOfEdges++;
            ArrayList<Integer> arr = new ArrayList<>();
            String[] s = line.split(" ");
            for (String ss : s) {
                int vertexIndex = Integer.parseInt(ss);
                arr.add(vertexIndex);
                maxVertext = Math.max(maxVertext, vertexIndex);
            }
            edgeByIndices.add(arr);
        }

        numOfPredicates = maxVertext + 1;
        for (int i = 0; i < numOfEdges; ++i) {
            BitSet edge = new BitSet(numOfPredicates);
            PredicateBitSet predicateEdge=new PredicateBitSet((IBitSet) edge);
            edges.add(predicateEdge);
//            edges.add(edge);
        }

        for (int e = 0; e < numOfEdges; ++e) {
            for (Integer i : edgeByIndices.get(e)) {
                edges.get(e).set(i);
            }
        }
    }

    public Hypergraph clone() {
        Hypergraph cloned = (Hypergraph) super.clone();
        cloned.edges = (ArrayList) edges.clone();
        return cloned;
    }

    public int num_predicates() {
        return numOfPredicates;
    }

    public int num_edges() {
        return edges.size();
    }

    //    public ArrayList<IBitSet> getEdges() {
//        return edges;
//    }
    public ArrayList<PredicateBitSet> getEdges() {return edges;}

    public void add_edge(/*BitSet*/PredicateBitSet edge, int v/*, boolean testSimplicity*/) {
//        if (testSimplicity) {
//            ;
//        }

        if (v < edges.size()) edges.set(v, edge);
        else edges.add(edge);

        if (numOfPredicates == 0) numOfPredicates = edges.size();
//        else if (numOfVertex != edge.size()) {
//            System.out.println("Attempted to add edge of invalid size!");
//        }
    }

    private PredicateBitSet edges_containing_vertex(int v) {
        int n = num_edges();
//        BitSet re = new BitSet(n);
        LongBitSet re=new LongBitSet(n);
        PredicateBitSet res=new PredicateBitSet(re);
        for (int edgeIndex = 0; edgeIndex < n; ++edgeIndex) {
//            if (edges.get(edgeIndex).get(v)) re.set(edgeIndex);
            if (edges.get(edgeIndex).getBitset().toBitSet().get(v)) res.setBitset(edgeIndex);
        }
        return res;
    }

    public Hypergraph transpose() {
        Hypergraph T = new Hypergraph(num_edges(), num_predicates());
        for (int v = 0; v < numOfPredicates; ++v) {
            T.add_edge(edges_containing_vertex(v), v/*, false*/);
        }
//        for(PredicateBitSet p:T.getEdges())
//            System.out.println(p);
        return T;
    }


//    public static void main(String[] args) throws IOException {
//        Hypergraph H = new Hypergraph("src/example.dat");
//        System.out.println(H.num_verts() + " " + H.num_edges());
//    }
}
