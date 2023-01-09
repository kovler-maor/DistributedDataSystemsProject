import java.io.*;
import java.net.*;
import java.util.*;

public class ListenSocket implements Runnable{

    public ServerSocket ss;

    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors;

    public int number_of_nodes;

    public ArrayList<SendSocket> all_send_sockets;

    public double[][] graph_matrix;

    private int num_of_massage_to_send;

    public boolean isClose;
    public boolean stop_forwarding;
    public boolean forwarding_is_closed;



    public ListenSocket(int listen_port, ArrayList<Pair<Integer,
            ArrayList<Object>>> neighbors, int number_of_nodes,
                        double[][] graph_matrix) throws IOException {
        this.ss = new ServerSocket(listen_port);
        this.neighbors = neighbors;
        this.number_of_nodes = number_of_nodes;
        this.graph_matrix = graph_matrix;
        this.isClose = false;
        this.stop_forwarding = false;
        this.forwarding_is_closed = false;
    }

    @Override
    public void run() {
        try {
            this.isClose = false;
            Socket s = this.ss.accept();

            while (true) {

                ObjectInputStream objectInputStream = new ObjectInputStream(s.getInputStream());
                Object object = objectInputStream.readObject();
                if (object instanceof Pair) {
                    // get the packet
                    Pair<Integer, double[]> packet_lv = (Pair<Integer, double[]>) object;
                    int id = packet_lv.getKey();

                    // update matrix
                    this.graph_matrix[id - 1] = packet_lv.getValue();

                    // sent to all my neighbors except v that sent the original packet
                    while (this.all_send_sockets == null){}
                    this.num_of_massage_to_send = this.all_send_sockets.size();

                    if (!this.stop_forwarding) {
                        forward(packet_lv, ss.getLocalPort());
                    } else {
                        this.forwarding_is_closed = true;
                    }

                } else {
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
        int sender_listen_port = 0;
        for(Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            if(neighbor.getValue().get(2).equals(sender_send_port)){
                sender_listen_port = (int) neighbor.getValue().get(1);
            }
        }

        while (this.all_send_sockets == null){}
        List<SendSocket> copy_all_send_sockets = new ArrayList<>(this.all_send_sockets);
        for (SendSocket send_socket : copy_all_send_sockets) {
            if (send_socket.getSend_port() != sender_listen_port) {
                if(!(send_socket.getSocket().isClosed())) {
                    send_socket.send(massage);
                }
            }
            this.num_of_massage_to_send--;
        }
    }


    public void forward_for_last_time() throws IOException, InterruptedException {
        // forward to all sockets that the network
        // is about to be closed so they need to stop and
        // change forwarding_is_closed to true
        if(!this.forwarding_is_closed) {
            double[] d = new double[2];
            Integer i = -5;
            Pair<Integer, double[]> close_your_forward = new Pair(i, d);
            forward(close_your_forward, this.ss.getLocalPort());
        }
        this.forwarding_is_closed = true;
    }


}
