import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CountDownLatch;

// This is the Node class which represents a single router in our network.
public class Node extends Thread {

    private int id; // ID of the router.

    public ArrayList<Pair<Integer, ArrayList<Object>>> neighbors = new ArrayList<>(); // A list of pairs which contains all the neighbors of the node.

    private double[] l_v; // A list which contains this node l_v

    private double[][] graph_matrix; // Output of any iteration

    public int number_of_nodes;

    public ArrayList<ListenSocket> all_listen_sockets = new ArrayList<ListenSocket>();

    public ArrayList<SendSocket> all_send_sockets = new ArrayList<SendSocket>();


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

        //build lv
        build_l_v();

        // init - add my lv
        this.graph_matrix[this.id-1] = this.l_v;

        // start by build my lv
        Pair<Integer, double[]> my_lv_massage = new Pair(this.id, this.l_v); //build my massage

        // send my lv to all other x in G
        try {
            send_pair_to_all(my_lv_massage);

            // wait until i have my full graph_matrix
            while (!check_full_matrix()){
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // now we have all the data and can finish for node n.
        ExManager.latch.countDown();


    }

    public void init_empty_graph_matrix(){
        this.graph_matrix = new double[this.number_of_nodes][this.number_of_nodes];
    }


    public boolean check_full_matrix(){
        for (double[] inner_list : this.graph_matrix){
            if (inner_list[0] == 0.0){
                return false;
            }
        }
        return true;
    }


    public void all_sockets_ready_to_stop(){
        for (ListenSocket listen_socket : this.all_listen_sockets) {
            listen_socket.not_ready_to_stop = false;
        }
    }


    public void forward_sent_to_listen_sockets(){
        for (ListenSocket listen_socket : this.all_listen_sockets) {
            listen_socket.all_send_sockets = this.all_send_sockets;
        }
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
            double weight = (double) neighbor.getValue().get(0);
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


    public void build_all_listen_sockets() throws IOException {
        /**
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

        for (Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            in_ports.add((Integer) neighbor.getValue().get(2));
        }

        // listen to all my in ports
        for (int port : in_ports) {
            try {
//                System.out.println("Open Socket with listen port: " + port  + " from node: " + this.id);
                ListenSocket listen_socket = new ListenSocket(port, this.neighbors, this.number_of_nodes, this.graph_matrix);
                listen_socket.start();
                this.all_listen_sockets.add(listen_socket);
                ExManager.dec_network_is_ready();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void build_all_send_sockets() throws IOException {
        /**
         * @Pair<Integer, double[]> my_lv_massage
         * this function will build all my out sockets
         */

        // build a list of all the in ports i should listen to
        ArrayList<Integer> out_ports = new ArrayList<Integer>();

        for (Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            out_ports.add((Integer) neighbor.getValue().get(1));
        }

        // listen to all my in ports
        for (int port : out_ports) {
//            System.out.println("Open with send port Socket: " + port  + " from node: " + this.id);
            SendSocket send_socket = new SendSocket(port);
            this.all_send_sockets.add(send_socket);
            ExManager.dec_network_is_ready();
        }
        forward_sent_to_listen_sockets();
    }


    public void send_pair_to_all(Pair<Integer, double[]> massage) throws InterruptedException {
        /**
         * @Pair<Integer,double[]> massage -> the massage i want to send
         * this function build all of my neighbors out sockets each time i want to send a massage
         * sends the massage to all and then terminates all sockets
         */

        // send to all my in ports
        for (SendSocket send_port : this.all_send_sockets) {
            try {
                // create new sockets
//                System.out.println("My massage send on port: " + send_port.getSend_port());
                send_port.send(massage);

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



    public void print_graph() {
        /**
         * print the graph matrix
         */
//        System.out.println("Graph of node: " + this.id);
        for (int i = 0; i < this.graph_matrix.length; i++) {
            for (int j = 0; j < this.graph_matrix.length; j++) {
                if (j == this.graph_matrix.length - 1) {
                    System.out.println(graph_matrix[i][j]);
                } else {
                    System.out.print(graph_matrix[i][j] + ", ");
                }
            }
        }
    }


}
