import java.io.*;
import java.net.*;
import java.util.*;

public class ListenSocket extends Thread{

    private ServerSocket ss;

    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors;

    public int number_of_nodes;

    public ArrayList<SendSocket> all_send_sockets;

    public boolean not_ready_to_stop = true;

    public double[][] graph_matrix;

    private int num_of_massage_to_send;


    public ListenSocket(int listen_port, ArrayList<Pair<Integer,
            ArrayList<Object>>> neighbors, int number_of_nodes,
                        double[][] graph_matrix) throws IOException {

        this.ss = new ServerSocket(listen_port);
        this.neighbors = neighbors;
        this.number_of_nodes = number_of_nodes;
        this.graph_matrix = graph_matrix;
    }

    @Override
    public void run() {

        Socket s = null;
        try {
            s = this.ss.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!(ss.isClosed())) {
            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(s.getInputStream());
                Object object = objectInputStream.readObject();
                if (object instanceof Pair) {
                    Pair<Integer, double[]> packet_lv= (Pair<Integer, double[]>) object;
//                    System.out.println("MASSAGE originally from = " + packet_lv.getKey() + " through: " + ss.getLocalPort());
                    int id = packet_lv.getKey();
                    this.graph_matrix[id - 1] = packet_lv.getValue();

                    // sent to all my neighbors except v that sent the original packet
                    this.num_of_massage_to_send = this.all_send_sockets.size();
                    forward(packet_lv, ss.getLocalPort());
                    while (this.num_of_massage_to_send > 0) {
                    }
                }

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void forward(Pair<Integer, double[]> massage, int sender_send_port) throws IOException, InterruptedException {
        int sender_listen_port = 0;
        for(Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            if(neighbor.getValue().get(2).equals(sender_send_port)){
                sender_listen_port = (int) neighbor.getValue().get(1);
            }
        }
        for (SendSocket send_socket : this.all_send_sockets) {
            if (send_socket.getSend_port() != sender_listen_port) {
                if(!(send_socket.getSocket().isClosed()))
                    send_socket.send(massage);
            }
            this.num_of_massage_to_send--;
        }
    }

    public void close() throws IOException {
        this.ss.close();
        ExManager.not_all_sockets_closed--;
    }


}
