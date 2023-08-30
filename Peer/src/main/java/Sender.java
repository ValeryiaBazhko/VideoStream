import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;

public class Sender extends Thread {

    private String ip;
    private int port;
    private DatagramSocket socket;
    private BlockingQueue<String> outQueue;


    public Sender(String ip, int port, BlockingQueue<String> outQueue) {
        this.ip = ip;
        this.port = port;
        this.outQueue = outQueue;
        try {
            this.socket = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void run() {

        String message = null;
        DatagramPacket packet = null;
        while (true) {
            try {
                message = outQueue.take();
                //System.out.println("Message sent: " + message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                packet = new DatagramPacket(
                        message.getBytes(),
                        message.getBytes().length,
                        Inet4Address.getByName(ip),
                        port
                );
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
