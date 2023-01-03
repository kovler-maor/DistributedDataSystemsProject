import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SendSocket extends Thread{
    private final int send_port;
    private Socket s;
    private Pair<Integer, double[]> massage;
    private static final Lock sent_lock = new ReentrantLock();


    public SendSocket(int port){
        this.send_port = port;
        try {
            this.s = new Socket("localHost", this.send_port);
        } catch (Exception e){
            e.printStackTrace();
        }
    }



    public void send(){
        try {
            sent_lock.lock();
            ObjectOutputStream out = new ObjectOutputStream(this.s.getOutputStream());
            // send an object to the server
            System.out.println("node with send port: " + this.send_port);
            out.writeObject(this.massage);
            out.flush();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            sent_lock.unlock();
        }
    }

    public void setMassage(Pair<Integer, double[]> massage) {
        this.massage = massage;
    }

    public int getSend_port() {
        return send_port;
    }
}
