import java.util.ArrayList;

/**
 * This is the Node class which represents a single router in our net work.
 */
public class Node extends Thread {
    private int id; //The id of the router

    /*A list of pairs which contains all the neighbors of the node */
    private ArrayList<Pair<Integer,int[]>> neighbors;

    private int[][] adj_matrix; //This is the matrix for the link state routing algorithm

    /**
     * This function returns the node's id
     * @return id
     */
    int get_node_id() {return this.id;}

    /**
     * This function returns the node's neighbors list
     * @return neighbors
     */
    ArrayList<Pair<Integer, int[]>> get_neighbors() {return this.neighbors;}

    /**
     * This function returns the node's matrix
     * @return adj_matrix
     */
    int[][] get_adj_matrix() {return this.adj_matrix;}

//    void Node


}
