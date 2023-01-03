import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ListenSocket extends Thread{
    private int listen_port;

    private ServerSocket ss;

    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors;

    public int number_of_nodes;

    public ArrayList<SendSocket> all_send_sockets;

    private static final Lock sent_lock = new ReentrantLock();

    public boolean not_ready_to_stop = true;

    public int sent_to_all_neighbors;

    public double[][] graph_matrix;


    public ListenSocket(int listen_port, ArrayList<Pair<Integer,
            ArrayList<Object>>> neighbors, int number_of_nodes, double[][] graph_matrix) throws IOException {
        this.listen_port = listen_port;
        this.ss = new ServerSocket(listen_port);
        this.neighbors = neighbors;
        this.number_of_nodes = number_of_nodes;
        this.graph_matrix = graph_matrix;
    }


    @Override
    public void run(){
        while(!(ss.isClosed())){
            try {
                Socket s = this.ss.accept();

                sent_lock.lock();

                //////
                int sender_listen_port = 0;
                for(Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
                    if (neighbor.getValue().get(2).equals(ss.getLocalPort())) {
                        sender_listen_port = (int) neighbor.getValue().get(1);
                    }
                }
                System.out.println("Connection between :" + ss.getLocalPort() + " and: " + sender_listen_port);
                ///////

                ObjectInputStream in = new ObjectInputStream(s.getInputStream());
                Pair<Integer, double[]> packet_lv;
                packet_lv = (Pair<Integer, double[]>) in.readObject();

                System.out.println("MASSAGE originally from = " + packet_lv.getKey() + " through: " + ss.getLocalPort());

                int id = packet_lv.getKey();
                this.graph_matrix[id-1] = packet_lv.getValue();

                // sent to all my neighbors except v that sent the original packet
                forward(packet_lv, ss.getLocalPort());

                sent_lock.unlock();

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    public void forward(Pair<Integer, double[]> massage, int sender_send_port) throws IOException {
        int sender_listen_port = 0;
        for(Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            if(neighbor.getValue().get(2).equals(sender_send_port)){
                sender_listen_port = (int) neighbor.getValue().get(1);
            }
        }
        for (SendSocket send_socket : this.all_send_sockets) {
            if (send_socket.getSend_port() != sender_listen_port) {
                send_socket.setMassage(massage);
                send_socket.send();
            }
        }
    }


}
