import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ListenSocket implements Runnable{

    public ServerSocket ss;

    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors;

    public int number_of_nodes;

    public ArrayList<SendSocket> all_send_sockets;

    public double[][] graph_matrix;

    public Lock matrix_lock;



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
//                System.out.println("socket: "+ this.ss.getLocalPort()+ " is here!");
                ObjectInputStream objectInputStream = new ObjectInputStream(s.getInputStream());

                Object object = objectInputStream.readObject();
                if (object instanceof Pair) {
                    // get the packet
                    Pair<Integer, double[]> packet_lv = (Pair<Integer, double[]>) object;
                    int id = packet_lv.getKey();

                    // update matrix
//                    this.matrix_lock.lock();
                    this.graph_matrix[id - 1] = packet_lv.getValue();
//                    this.matrix_lock.unlock();

                    // sent to all my neighbors except v that sent the original packet
                    while (this.all_send_sockets == null) {
                        Thread.sleep(1);
                    }

                    if (!this.forwarding_is_closed) {
                        forward(packet_lv, ss.getLocalPort());
                    }
                    if (check_full_matrix()){
                        this.forwarding_is_closed = true;
                    }

//                    } else {
//                        this.forwarding_is_closed = true;
//                        packet_lv.setKey(-4);
//                        forward(packet_lv, ss.getLocalPort());
////                        System.out.println("Socket number: "+ this.ss.getLocalPort()+ " forwarding_is_closed is True");
//                    }
                } else {
                    s.close();
                    this.ss.close();
                    this.isClose = true;
                    break;
                }
            }

//            }
//            if(!this.ss.isClosed()) {
//                s.close();
//                this.ss.close();
//                this.isClose = true;
//            }


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
//                if(!(send_socket.getSocket().isClosed())) {
                    send_socket.send(massage);
//                }
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
