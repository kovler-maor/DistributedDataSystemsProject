import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ListenSocket extends Thread{
    private Pair<Integer,double[]> packet_lv;
    private int listen_port;
    private ServerSocket ss;
    private Socket s;
    private ArrayList<Pair<Integer, double[]>> other_l_vs;
    private ArrayList<Integer> other_lvs_keys;
    private ArrayList<Pair<Integer, ArrayList<Object>>> neighbors;

    public ListenSocket(int listen_port, ArrayList<Pair<Integer, double[]>> other_l_vs, ArrayList<Pair<Integer, ArrayList<Object>>> neighbors) throws IOException {
        this.other_l_vs = other_l_vs;
        this.listen_port = listen_port;
        this.ss = new ServerSocket(listen_port);
        this.neighbors = neighbors;
        this.other_lvs_keys = new ArrayList<>();
    }


    @Override
    public void run(){
        try {
            this.s = this.ss.accept();
            ObjectInputStream in = new ObjectInputStream(this.s.getInputStream());
            this.packet_lv = (Pair<Integer, double[]>) in.readObject();

            //lock
            // check if already exist in other_lvs
            if(!(this.other_lvs_keys.contains(packet_lv.getKey()))){
                this.other_l_vs.add(this.packet_lv);
                this.other_lvs_keys.add(packet_lv.getKey());
            }
            //unlock

            // sent to all my neighbors except v that sent the original packet
            build_sockets_and_send_pair_to_all(this.packet_lv);

        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void build_sockets_and_send_pair_to_all(Pair<Integer, double[]> massage){
        /**
         * @Pair<Integer,double[]> massage -> the massage i want to send
         * this function build all of my neighbors out sockets each time i want to send a massage
         * sends the massage to all and then terminates all sockets
         */

        // build a list of all the out ports i should send to
        int sender = massage.getKey();
        ArrayList<Integer> out_ports = new ArrayList<Integer>();
        for (Pair<Integer, ArrayList<Object>> neighbor : this.neighbors) {
            if(!(neighbor.getKey() == sender)){
                out_ports.add((Integer) neighbor.getValue().get(1));
            }
        }

        // send to all my in ports
        for (int port : out_ports) {
            try {
                // create new sockets
                Socket out_socket = new Socket("localhost", port);
                ObjectOutputStream out = new ObjectOutputStream(out_socket.getOutputStream());

                // send an object to the server
                out.writeObject(massage);

                // close the sockets
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

    public Pair<Integer,double[]> getPacket_lv(){
        return this.packet_lv;
    }
}
