import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendSocket {

    private final int send_port;

    private Socket s;

    private static final Lock sent_lock = new ReentrantLock();


    public SendSocket(int port){
        this.send_port = port;
        try {
            this.s = new Socket("localHost", this.send_port);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void send(Pair<Integer, double[]> massage){

        if (!ExManager.network_is_closing) {
            try {
                sent_lock.lock();
                if ((!(this.s.isClosed()))) {
                    ObjectOutputStream out = new ObjectOutputStream(this.s.getOutputStream());
                    out.writeObject(massage);
                    out.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                sent_lock.unlock();
            }
        }
    }


    public void send_close_massage() throws IOException {
        try {
            sent_lock.lock();
            Object close_massage = 0;
            ObjectOutputStream out = new ObjectOutputStream(this.s.getOutputStream());
            out.writeObject(close_massage);
            out.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sent_lock.unlock();
        }
    }


    public int getSend_port() {
        return send_port;
    }

}
