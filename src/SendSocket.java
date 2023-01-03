import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendSocket extends Thread{
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
        try {
            sent_lock.lock();
            ObjectOutputStream out = new ObjectOutputStream(this.s.getOutputStream());
            // send an object to the server
            out.writeObject(massage);
            out.flush();

        } catch (Exception e){
            e.printStackTrace();
        } finally {
            sent_lock.unlock();
        }
    }

    public int getSend_port() {
        return send_port;
    }
}
