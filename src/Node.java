import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/**
 * This is the Node class which represents a single router in our net work.
 */
public class Node extends Thread {
    private int id; // The id of the router.
    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors = new ArrayList<>(); // A list of pairs which contains all the neighbors of the node.
    private double[] l_v;// A list which contains this node l_v
    private ArrayList<Pair<Integer, double[]>> other_l_vs = new ArrayList<>(); // A list of all other nodes l_vs
    private double[][] graph_matrix;

    private int number_of_nodes;

    public Node(int id) {
        this.id = id;
    }

    public void set_number_of_nodes(int number_of_nodes){
        this.number_of_nodes = number_of_nodes;
    }

    public void add_neighbor(int neighbor_id, double neighbor_weight, int neighbor_send_port, int neighbor_listen_port) {
        /**
         * This function adds a new neighbor to the node's neighbors list.
         * @int neighbor_id -> the neighbor's id
         * @int neighbor_weight the -> the edge's weight
         * @int neighbor_send_port -> the sending port
         * @int neighbor_listen_port -> the receiving port
         */
        Integer key = neighbor_id;
        ArrayList<Object> value = new ArrayList<>();
        value.add(neighbor_weight);
        value.add(neighbor_send_port);
        value.add(neighbor_listen_port);
        Pair pair = new Pair<>(key, value);
        this.neighbors.add(pair);
    }

    public void update_neighbor_weight(int neighbor_id, double weight) {
        /**
         * This function get one of the node's neighbors id and a weight and updates the edge between them
         * @int neighbor_id
         * @double weight
         */
        for (Pair<Integer, ArrayList<Object>> neighbor : neighbors) {
            if ((int) neighbor.getKey() == neighbor_id) {
                ArrayList<Object> updated_value = neighbor.getValue();
                updated_value.set(0, weight);
                neighbor.setValue(updated_value);
            }
        }
    }

    int get_node_id() {
        /**
         * This function returns the node's id
         * @return id
         */
        return this.id;
    }

    ArrayList<Pair<Integer, ArrayList<Object>>> get_neighbors() {
        /**
         * This function returns the node's neighbors list
         * @return neighbors
         */
        return this.neighbors;
    }


    public void build_l_v() {
        /**
         * This function gets the number of nodes in the network (from ExManager) and builds the node's l_v matrix
         * @int number_of_nodes
         */
        this.l_v = new double[this.number_of_nodes];// establishing l_v
        // creating a matrix which will contain all of the node's neighbors' id and weight
        ArrayList<ArrayList<Object>> neighbors_ids_weights = new ArrayList<>();
        // creating a list of all the ids for later searching purpose
        ArrayList<Integer> only_ids = new ArrayList<>();
        for (Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            int id = neighbor.getKey();
            double weight = neighbor.getKey();
            only_ids.add(id);
            ArrayList<Object> neighbor_id_weight = new ArrayList<>();
            neighbor_id_weight.add(id);
            neighbor_id_weight.add(weight);
            neighbors_ids_weights.add(neighbor_id_weight);
        }
        //fill everything with -1
        for (int i = 0; i < this.number_of_nodes; i++) {
                this.l_v[i] = -1;
            }

        //fill correct places with propre weights
        for(int j = 0; j < this.number_of_nodes; j++){
            if (only_ids.contains(j + 1)) {
                for (ArrayList<Object> neighbor : neighbors_ids_weights) {
                    if ((int) neighbor.get(0) == (j + 1)) {
                        this.l_v[j] = (double) neighbor.get(1);
                    }
                }
           }
        }
    }
    public void add_lv(Pair<Integer, double[]> l_v){
        this.other_l_vs.add(l_v);
    }
    public void build_graph_matrix() {
        /**
         * This function builds the graph matrix from all given l_vs
         * l_vs is the value of all other ArrayList<Pair<Integer, double[]>> other_l_vs
         */
        //fill everything with -1
        for (int i = 0; i < this.number_of_nodes; i++) {
            for (int j = 0; j < this.number_of_nodes; j++){
                this.graph_matrix[i][j] = -1;
            }
        }
        //fill correct places with propre weights
        for(Pair<Integer, double[]> l_v: this.other_l_vs){
            int row = l_v.getKey() - 1;
            for(int col = 0; col < this.number_of_nodes; col++){
                this.graph_matrix[row][col] = l_v.getValue()[col];
            }
        }
    }
    @Override
    public void run(){
        try {
            ArrayList<ServerSocket> input_sockets = new ArrayList<>();
            ArrayList<Socket> output_sockets = new ArrayList<>();
            for(Pair<Integer, ArrayList<Object>> neighbor: this.neighbors){
                int input_port = (int) neighbor.getValue().get(2);
                int output_port = (int) neighbor.getValue().get(1);
                ServerSocket input_sock = new ServerSocket(input_port);
                Socket output_sock = new Socket("localhost", output_port);
                input_sockets.add(input_sock);
                output_sockets.add(output_sock);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


}
