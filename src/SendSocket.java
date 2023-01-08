import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendSocket {

    private final int send_port;

    private Socket s;

    private static final Lock sent_lock = new ReentrantLock();

    private boolean stop_sending_massages = false;




    public SendSocket(int port){
        this.send_port = port;
        try {
            this.s = new Socket("localHost", this.send_port);
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void send(Pair<Integer, double[]> massage){
        if (!stop_sending_massages) {
            try {
                sent_lock.lock();
                if ((!(s.isClosed()))) {
                    ObjectOutputStream out = new ObjectOutputStream(this.s.getOutputStream());
                    // send an object to the server
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

    public int getSend_port() {
        return send_port;
    }


    public Socket getSocket() {
        return s;
    }

    public void stop_sending_massages(){
        this.stop_sending_massages = true;
    }

    public void start_sending_massages(){
        this.stop_sending_massages = false;
    }


}
