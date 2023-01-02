import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ListenSocket extends Thread{
    private int listen_port;
    private ServerSocket ss;
    private Socket s;
    private ArrayList<Pair<Integer, double[]>> other_l_vs;
    private ArrayList<Integer> other_lvs_keys;
    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors;
    public int number_of_nodes;


    public ListenSocket(int listen_port, ArrayList<Pair<Integer, double[]>> other_l_vs, ArrayList<Pair<Integer, ArrayList<Object>>> neighbors, int number_of_nodes, ArrayList<Integer> other_lvs_keys) throws IOException {
        this.other_l_vs = other_l_vs;
        this.listen_port = listen_port;
        this.ss = new ServerSocket(listen_port);
        this.neighbors = neighbors;
        this.number_of_nodes = number_of_nodes;
        this.other_lvs_keys = other_lvs_keys;
    }


    @Override
    public void run(){
        while (true){
            try {
                this.s = this.ss.accept();
                ObjectInputStream in = new ObjectInputStream(this.s.getInputStream());
                Pair<Integer, double[]> packet_lv = (Pair<Integer, double[]>) in.readObject();

                // check if already exist in other_lvs
//                ReentrantLock lock = new ReentrantLock();
//                lock.lock();
                if (!(this.other_lvs_keys.contains(packet_lv.getKey()))) {
                    this.other_l_vs.add(packet_lv);
                    this.other_lvs_keys.add(packet_lv.getKey());
                    // if we got all data increment node_finish_counter
                    if (this.other_l_vs.size() == this.number_of_nodes - 1) {
                        ExManager.increment_node_finish_counter();
                    }
                }
                // sent to all my neighbors except v that sent the original packet
                forward_packet(packet_lv);

//                lock.unlock();

            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void forward_packet(Pair<Integer, double[]> massage){
        /**
         * @Pair<Integer,double[]> massage -> the massage i want to send
         * this function build all of my neighbors out sockets each time i want to send a massage
         * sends the massage to all and then terminates all sockets
         */

        // build a list of all the out ports i should send to
        int sender = massage.getKey();
        ArrayList<Integer> out_ports = new ArrayList<Integer>();
        for (Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
//            if(!(neighbor.getKey() == sender)){
            out_ports.add((Integer) neighbor.getValue().get(1));
//            }
        }

        // send to all my in ports
        for (int port : out_ports) {
            try {
                // create new sockets
//                System.out.println("Open Socket with sending port: " + port);
                Socket out_socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(out_socket.getOutputStream());

                // send an object to the server
                out.writeObject(massage);
//                System.out.println("node with listen port: "+ this.listen_port +" Forwarding Massage from: " + sender + " on sending port: " + port);

                // close the sockets
//                System.out.println("Close Socket with sending port: " + port);
                out.close();
                out_socket.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void close_sockets() throws IOException {
        this.s.close();
        this.ss.close();
    }


    public int getListen_port() {
        return listen_port;
    }
}
