import java.io.*;
import java.net.*;
import java.util.*;

// This is the Node class which represents a single router in our network.
public class Node extends Thread {

    private int id; // ID of the router.

    public ArrayList<Pair<Integer, ArrayList<Object>>> neighbors = new ArrayList<>(); // A list of pairs which contains all the neighbors of the node.

    private double[] l_v; // A list which contains this node l_v

    public volatile ArrayList<Pair<Integer, double[]>> other_l_vs = new ArrayList<>(); // A list of all other nodes l_vs and IDs

    private double[][] graph_matrix; // Output of any iteration

    private int number_of_nodes;


    public Node(int id) {
        this.id = id;
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

    @Override
    public void run() {
        /**
         * this function override Thread "run" and implement all of the
         * link state routing algorithm from the prospective of one node v in graph G
         */

        // lock 1

        //build lv
        build_l_v();
        // start by build my lv
        Pair<Integer, double[]> my_lv_massage = new Pair(this.id, this.l_v); //build my massage

        // now start listen to all ports
        try {
            build_all_listen_sockets(my_lv_massage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // send my lv to all other x in G
        build_sockets_and_send_pair_to_all(my_lv_massage);

        // now we have all the data and can build graph_matrix
        build_graph_matrix();

        // unlock 1

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
        for (int j = 0; j < this.number_of_nodes; j++) {
            if (only_ids.contains(j + 1)) {
                for (ArrayList<Object> neighbor : neighbors_ids_weights) {
                    if ((int) neighbor.get(0) == (j + 1)) {
                        this.l_v[j] = (double) neighbor.get(1);
                    }
                }
            }
        }
    }


    public void build_all_listen_sockets(Pair<Integer, double[]> my_lv_massage) throws IOException {
        /**
         * @Pair<Integer, double[]> my_lv_massage
         * this function will build all my in sockets and listen to all of them
         * it will stop once my node have gotten all the data he needs
         * which means stop when the size of "this.other_lvs == (n-1)
         * the function also sends my_lv, using the "build_sockets_and_send_pair_to_all" function
         * it needs to be only after a node set up all of his in sockets so that
         * the code will not get stuck on all node just sending and waiting for connection
         *** "my_lv_massage" should be send just once in the first iteration
         */

        // build a list of all the in ports i should listen to
        ArrayList<Integer> in_ports = new ArrayList<Integer>();
        ArrayList<ListenSocket> all_listen_sockets = new ArrayList<ListenSocket>();

        for (Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            in_ports.add((Integer) neighbor.getValue().get(2));
        }

        while (true) { // run until i have all my data - all 'lvs' of the graph

            // listen to all my in ports
            for (int port : in_ports) {
                try {
                    ListenSocket listen_socket = new ListenSocket(port, this.other_l_vs, this.neighbors);
                    listen_socket.start();
                    all_listen_sockets.add(listen_socket);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            build_sockets_and_send_pair_to_all(my_lv_massage);
            //check if you have all data(all_lvs)
            if (this.other_l_vs.size() == (this.number_of_nodes - 1)) {
                break;
            }
        }
        // close all listen sockets
        for (ListenSocket socket : all_listen_sockets) {
            socket.close_sockets();
        }
    }


    public void build_sockets_and_send_pair_to_all(Pair<Integer, double[]> massage) {
        /**
         * @Pair<Integer,double[]> massage -> the massage i want to send
         * this function build all of my neighbors out sockets each time i want to send a massage
         * sends the massage to all and then terminates all sockets
         */
        // build a list of all the out ports i should send to
        ArrayList<Integer> out_ports = new ArrayList<Integer>();
        for (Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            out_ports.add((Integer) neighbor.getValue().get(1));
        }

        // send to all my in ports
        for (int port : out_ports) {
            try {
                // create new sockets
                Socket out_socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(out_socket.getOutputStream());

                // send an object to the server
                out.writeObject(massage);

                // close the sockets
                out.close();
                out_socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public int get_node_id() {
        /**
         * This function returns the node's id
         * @return id
         */
        return this.id;
    }


    public void set_number_of_nodes(int number_of_nodes) {
        this.number_of_nodes = number_of_nodes;
    }


    public void build_graph_matrix() {
        /**
         * This function builds the graph matrix from all given l_vs
         * l_vs is the value of all other ArrayList<Pair<Integer, double[]>> other_l_vs
         */
        //fill everything with -1
        this.graph_matrix = new double[this.number_of_nodes][this.number_of_nodes];
        for (int i = 0; i < this.number_of_nodes; i++) {
            for (int j = 0; j < this.number_of_nodes; j++) {
                this.graph_matrix[i][j] = -1;
            }
        }

        //fill correct places with propre weights
        // my data
        for(int col = 0; col < this.number_of_nodes; col++){
            this.graph_matrix[this.id - 1][col] = this.l_v[col];
        }
        // other's data
        for (Pair<Integer, double[]> l_v : this.other_l_vs) {
            int row = l_v.getKey() - 1;
            for (int col = 0; col < this.number_of_nodes; col++) {
                this.graph_matrix[row][col] = l_v.getValue()[col];
            }
        }
    }


    public void print_graph() {
        /**
         * print the graph matrix
         */
        for (int i = 0; i < this.graph_matrix.length; i++) {
            for (int j = 0; j < this.graph_matrix.length; j++) {
                if (j == this.graph_matrix.length - 1) {
                    System.out.println(graph_matrix[i][j]);
                } else {
                    System.out.println(graph_matrix[i][j] + ", ");
                }
            }
        }
    }

}
