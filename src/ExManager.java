import java.util.*;
import java.io.*;
import java.util.concurrent.locks.ReentrantLock;

public class ExManager {
    private String path;
    private ArrayList<Node> list_of_nodes;
    private static int node_finish_counter = 0;

    private static ReentrantLock lock = new ReentrantLock();

    public ExManager(String path) {
        this.path = path;
        this.list_of_nodes = new ArrayList<Node>();
    }

    public void read_txt() throws IOException {
        /**
         * This function reads from file and creates the nodes and their neighbors.
         */

        FileReader fr = new FileReader(this.path);
        BufferedReader br = new BufferedReader(fr);
        String line;
        Node node = null;
        boolean first_round = true;

        // read line by line, split, and use the data to build nodes and neighbors
        while (true) {
            line = br.readLine();
            if(line.equals("stop")){
                break;
            }
            String[] words = line.split("\\s+");

            if(first_round){
                first_round = false;
                node = new Node(Integer.parseInt(words[0]));
                this.list_of_nodes.add(node);

            } else {
                ArrayList<Integer> nodes_ids = get_nodes_ids();
                if (!nodes_ids.contains(Integer.parseInt(words[0]))) {
                    node = new Node(Integer.parseInt(words[0]));
                    this.list_of_nodes.add(node);
                } else {
                    node = get_node(Integer.parseInt(words[0]));
                }
            }

            // build the neighbor list for given node
            for (int i = 1; i < words.length; i += 4) {
                int neighbor_id = Integer.parseInt(words[i]);
                double neighbor_weight = Double.parseDouble(words[i + 1]);
                int neighbor_send_port = Integer.parseInt(words[i + 2]);
                int neighbor_listen_port = Integer.parseInt(words[i + 3]);
                node.add_neighbor(neighbor_id, neighbor_weight, neighbor_send_port, neighbor_listen_port);
            }
        }
    }


    public void start() {
        /**
         * This function starting the link state routing algorithm for all the nodes in the the graph
         */
        // inform nodes of the graph size (they need it to build the graph_matrix attribute)
        send_to_all_number_of_nodes();

        // open all listen ports of every node
        try {
            open_all_listen_ports();
        } catch (Exception e){
            e.printStackTrace();
        }

        // call the run() function for all the nodes in G
        run_all_nodes();

        // get here only if all nodes have build their graph matrix already
        int number_of_nodes = getNum_of_nodes();
        while (true){
            try {
                Thread.sleep(5000);
                if(node_finish_counter == number_of_nodes){break;}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        terminate();
    }


    public void send_to_all_number_of_nodes(){
        int number_of_nodes = getNum_of_nodes();
        for(Node node: this.list_of_nodes){
            node.set_number_of_nodes(number_of_nodes);
        }
    }


    public void open_all_listen_ports() throws IOException {
        for(Node node: this.list_of_nodes){
            node.build_all_listen_sockets();
        }
    }


    public void run_all_nodes(){
        for(Node node: this.list_of_nodes){
            node.start();
        }
    }


    public void update_edge(int id1, int id2, double weight) {
        /**
         * This method gets two nodes who hava an edge between them,and an edge weight, and updates the weight
         * @int id1 -> the first node's id
         * @int id2 -> the second node's id
         * @double weight -> the new weight
         */
        Node first_node = get_node(id1);
        first_node.update_neighbor_weight(id2, weight);
        Node second_node = get_node(id2);
        second_node.update_neighbor_weight(id1, weight);
    }


    private ArrayList<Integer> get_nodes_ids() {
        /**
         * This function takes the list_of_nodes attribute of the ExManager class and
         * returns an ArrayList of all the unique IDs of those Nodes.
         * @return ids_list_of_nodes
         */
        ArrayList<Integer> ids_list_of_nodes = new ArrayList<Integer>();
        for (Node node : this.list_of_nodes) {
            ids_list_of_nodes.add(node.get_node_id());
        }
        return ids_list_of_nodes;
    }


    public Node get_node(Integer id) {
        /**
         * This function takes the list_of_nodes attribute of the ExManager class and
         * returns a node with the unique ID that been given.
         * @int id -> node id
         * @return node -> an already existing node with the given ID.
         */
        try {
            Node wanted_node = null;
            for (Node node : this.list_of_nodes) {
                if (id.intValue() == node.get_node_id()) {
                    wanted_node = node;
                    break;
                }
            }
            return wanted_node;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;

    }


    public int getNum_of_nodes() {
        return this.list_of_nodes.toArray().length;
    }


    public static void increment_node_finish_counter(){
        lock.lock();
        try {
            System.out.println("locked");
            System.out.println("increment node_finish_counter by 1..");
            node_finish_counter++;
        } finally {
            System.out.println("unlocked");
            lock.unlock();
        }
    }


    public void terminate() {
        for (Node node : this.list_of_nodes) {
            try {
                System.out.println("Terminate for node: " + node.get_node_id());
                node.close_all_listen_sockets();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

}
