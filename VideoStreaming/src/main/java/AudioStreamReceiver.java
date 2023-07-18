import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AudioStreamReceiver {
    private static final int PORT = 1234; // replace with your port

    public static void main(String[] args) {
        byte[] buffer = new byte[65535];

        try (DatagramSocket socket = new DatagramSocket(PORT)) {

            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);

                // The audio data is now in 'packet.getData()'.
                // You would need to decode this data to play or process it as audio.
                System.out.println("Received packet, length: " + packet.getLength());
            }
        } catch (Exception e) {
            System.err.println("Error receiving packet: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
