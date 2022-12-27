import java.util.*;
import java.io.*;
import java.net.*;
public class ExManager {
    private String path;
    private ArrayList<Node> list_of_nodes;
    // your code here

    public ExManager(String path) {
        this.path = path;
        // your code here
    }

    public Node get_node(int id) {
        // your code here
    }

    public int getNum_of_nodes() {
        return this.num_of_nodes;
    }

    public void update_edge(int id1, int id2, double weight) {
        //your code here
    }

    public void read_txt() throws IOException {
        /**
         * This function reads from file and creates the nodes and their neighbors.
         */
        FileReader fr = new FileReader(this.path);
        BufferedReader br = new BufferedReader(fr);
        String line;
        Node node = null;
        while ((line = br.readLine()) != "stop") {
            String[] words = line.split("\\s+");
            ArrayList<Integer> nodes_ids = get_nodes_ids();
            if (!nodes_ids.contains(Integer.parseInt(words[0]))) {
                node = new Node(Integer.parseInt(words[0]));
            } else {
                node = get_node(Integer.parseInt(words[0]));
            }
            for (int i = 1; i < words.length; i += 4) {
                int neighbor_id = Integer.parseInt(words[i]);
                int neighbor_weight = Integer.parseInt(words[i + 1]);
                int neighbor_send_port = Integer.parseInt(words[i + 2]);
                int neighbor_listen_port = Integer.parseInt(words[i + 3]);

                node.add_neighbor(neighbor_id, neighbor_weight, neighbor_send_port, neighbor_listen_port);
            }
        }
    }

    public void start() {
        // your code here
    }

    private ArrayList<Integer> get_nodes_ids() {
        /**
         * This function takes the list_of_nodes attribute of the ExManager class and
         * returns an ArrayList of all the unique IDs of those Nodes.
         * @return ids_list_of_nodes
         */
        ArrayList<Integer> ids_list_of_nodes = null;
        for (Node node : this.list_of_nodes) {
            ids_list_of_nodes.add(node.get_node_id());
        }
        return ids_list_of_nodes;
    }

    private Node get_node(Integer id) {
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

    }

}
