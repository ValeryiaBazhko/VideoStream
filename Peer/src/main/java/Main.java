import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {


    public static void main(String[] args) {

        int port = 5000;
        String ip = "192.168.1.102";
        BlockingQueue<String> inQueue = new LinkedBlockingQueue<>();
        BlockingQueue<String> outQueue = new LinkedBlockingQueue<>();


        Thread inputThread = new Thread(() -> {
            try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
                while (scanner.hasNextLine()) {
                    String input = scanner.nextLine();
                    outQueue.put(input);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        inputThread.start();


        Sender sender = new Sender(ip, port, outQueue);
        sender.start();


        Receiver receiver = new Receiver(port, inQueue);
        receiver.start();
    }
}
