import java.util.ArrayList;

public class Node extends Thread {
    private int id;
    private ArrayList<Pair<Integer,int[]>> neighbors;

    private int[][] adj_matrix;

    int get_node_id() {return this.id;}

    ArrayList<Pair<Integer, int[]>> get_neighbors() {return this.neighbors;}

    int[][] get_adj_matrix() {return this.adj_matrix;}

//    void Node


}
