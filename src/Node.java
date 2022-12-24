import java.util.ArrayList;

public class Node extends Thread {
    private int id;
    private ArrayList<Pair<Integer,int[]>> neighbors;

    private int[][] adj_matrix;

    int getId() {return this.id;}

    ArrayList<Pair<Integer, int[]>> getNeighbors() {return this.neighbors;}

    int[][] getAdjMatrix() {return this.adj_matrix}

    void Node


}
