import java.util.*;
import java.io.*;
import java.net.*;

/**
 * This is the Node class which represents a single router in our net work.
 */
public class Node extends Thread {
    private int id; // The id of the router.
    private ArrayList<Pair<Integer,int[]>> neighbors; // A list of pairs which contains all the neighbors of the node.
    private int[][] adj_matrix; //This is the matrix for the link state routing algorithm.

    public Node(int id){
        this.id = id;
    }

    public void add_neighbor(int neighbor_id, int neighbor_weight, int neighbor_send_port, int neighbor_listen_port){
        /**
         * This function adds a new neighbor to the node's neighbors list.
         * @int neighbor_id -> the neighbor's id
         * @int neighbor_weight the -> the edge's weight
         * @int neighbor_send_port -> the sending port
         * @int neighbor_listen_port -> the receiving port
         */
        Integer key = neighbor_id;
        int[] value = new int[] {neighbor_weight, neighbor_send_port, neighbor_listen_port};
        Pair pair = new Pair<>(key,value);
        this.neighbors.add(pair);
    }
    int get_node_id() {
        /**
         * This function returns the node's id
         * @return id
         */
        return this.id;
    }

    ArrayList<Pair<Integer, int[]>> get_neighbors() {
        /**
         * This function returns the node's neighbors list
         * @return neighbors
         */
        return this.neighbors;
    }

    int[][] get_adj_matrix() {
        /**
         * This function returns the node's matrix
         * @return adj_matrix
         */
        return this.adj_matrix;
    }

}
