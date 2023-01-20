import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;


public class ListenSocket implements Runnable{

    public ServerSocket ss;

    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors;

    public int number_of_nodes;

    public ArrayList<SendSocket> all_send_sockets;

    public double[][] graph_matrix;

    public Lock matrix_lock;

    private ArrayList<Integer> ids_of_nodes_who_sent;

    public ObjectInputStream objectInputStream;

    public boolean isClose;

    public boolean stop_forwarding;

    public boolean forwarding_is_closed;


    public ListenSocket(int listen_port, ArrayList<Pair<Integer,
            ArrayList<Object>>> neighbors, int number_of_nodes, double[][] graph_matrix) throws IOException {
        this.ss = new ServerSocket(listen_port);
        this.neighbors = neighbors;
        this.number_of_nodes = number_of_nodes;
        this.graph_matrix = graph_matrix;
        this.isClose = false;
        this.stop_forwarding = false;
        this.forwarding_is_closed = false;
        this.ids_of_nodes_who_sent = new ArrayList<>();
    }

    @Override
    public void run() {
        try {
            this.isClose = false;
            Socket s = this.ss.accept();

            while (true) {
                this.objectInputStream = new ObjectInputStream(s.getInputStream());

                Object object = objectInputStream.readObject();
                if (object instanceof Pair) {

                    // get the packet
                    Pair<Integer, double[]> packet_lv = (Pair<Integer, double[]>) object;
                    int id = packet_lv.getKey();

                    // continue if i saw that packet already
                    if(this.ids_of_nodes_who_sent.contains(id)){
                        continue;
                    }
                    this.ids_of_nodes_who_sent.add(id);

                    // update matrix
                    this.graph_matrix[id - 1] = packet_lv.getValue();

                    // wait until all_send_sockets initialised
                    while (this.all_send_sockets == null) {
                        Thread.sleep(1);
                    }

                    // sent to all my neighbors except v that sent the original packet
                    // sent stop the massage after the one that makes my matrix full
                    if (!this.forwarding_is_closed) {
                        forward(packet_lv, ss.getLocalPort());
                    }
                    if (check_full_matrix()){
                        this.forwarding_is_closed = true;
                    }

                // if i got here that means that the massage that i got is "close massage"
                } else {
                    this.objectInputStream.close();
                    s.close();
                    this.ss.close();
                    this.isClose = true;
                    break;
                }
            }

        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void forward(Pair<Integer, double[]> massage, int sender_send_port) throws IOException, InterruptedException {
        // find out the sender's listen port
        int sender_listen_port = 0;
        for(Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            if(neighbor.getValue().get(2).equals(sender_send_port)){
                sender_listen_port = (int) neighbor.getValue().get(1);
            }
        }

        while (this.all_send_sockets == null){}
        // forward to all that are not the sender
        List<SendSocket> copy_all_send_sockets = new ArrayList<>(this.all_send_sockets);
        for (SendSocket send_socket : copy_all_send_sockets) {
            if (send_socket.getSend_port() != sender_listen_port) {
                    send_socket.send(massage);

            }
        }
    }


    public boolean check_full_matrix(){
        for (double[] inner_list : this.graph_matrix){
            if (inner_list[0] == -2.0){
                return false;
            }
        }
        return true;
    }

}
