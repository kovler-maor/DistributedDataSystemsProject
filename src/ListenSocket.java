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


    public ListenSocket(int listen_port, ArrayList<Pair<Integer,
            ArrayList<Object>>> neighbors, int number_of_nodes,
                        double[][] graph_matrix) throws IOException {

        this.ss = new ServerSocket(listen_port);
        this.neighbors = neighbors;
        this.number_of_nodes = number_of_nodes;
        this.graph_matrix = graph_matrix;
        this.isClose = false;
        this.stop_forwarding = false;
    }

    @Override
    public void run() {
        try {
            this.isClose = false;
            Socket s = this.ss.accept();
            System.out.println("Connection");

            while (true) {

                ObjectInputStream objectInputStream = new ObjectInputStream(s.getInputStream());
                Object object = objectInputStream.readObject();
                if (object instanceof Pair) {

                    // get the packet
                    Pair<Integer, double[]> packet_lv = (Pair<Integer, double[]>) object;
                    int id = packet_lv.getKey();
                    this.graph_matrix[id - 1] = packet_lv.getValue();

                    // sent to all my neighbors except v that sent the original packet
                    while (this.all_send_sockets.isEmpty()){}
                    this.num_of_massage_to_send = this.all_send_sockets.size();
                    if (!this.stop_forwarding) {
                        forward(packet_lv, ss.getLocalPort());
                    }

                } else {
                    System.out.println(object);
                    s.close();
                    this.ss.close();
                    this.isClose = true;
                    System.out.println("Connection Closed");
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
        while (this.all_send_sockets.isEmpty()){}
        for (SendSocket send_socket : this.all_send_sockets) {
            if (send_socket.getSend_port() != sender_listen_port) {
                if(!(send_socket.getSocket().isClosed()))
                    send_socket.send(massage);
            }
            this.num_of_massage_to_send--;
        }
    }


}
