import java.io.IOException;
import java.net.*;
import java.util.concurrent.BlockingQueue;

public class Receiver extends Thread {

    private int port;
    private DatagramSocket socket;
    private BlockingQueue<String> inQueue;

    public Receiver(int port, BlockingQueue<String> inQueue) {
        this.port = port;
        this.inQueue = inQueue;
        try {
            this.socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Message received: " + message);
                inQueue.offer(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
