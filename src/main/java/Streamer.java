import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

public class Streamer extends Thread {
    private String serverIp;
    private int serverVideoPort;
    private int serverAudioPort;
    private BlockingQueue<Frame> videoFrames;
    private BlockingQueue<byte[]> audioQueue;
    private DatagramSocket videoSocket;
    private DatagramSocket audioSocket;
    private boolean running;

    public Streamer(BlockingQueue<Frame> videoFrames, BlockingQueue<byte[]> audioQueue, String serverIp, int serverVideoPort, int serverAudioPort) {
        this.serverIp = serverIp;
        this.serverVideoPort = serverVideoPort;
        this.serverAudioPort = serverAudioPort;
        this.videoFrames = videoFrames;
        this.audioQueue = audioQueue;
        running = false;

        try {
            videoSocket = new DatagramSocket();
            audioSocket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Unable to create datagram socket");
        }
    }

    @Override
    public void run() {
        running = true;

        // Audio settings
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 44100.0f;
        int channels = 2;
        int sampleSize = 16;
        boolean bigEndian = true;
        AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);

        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line matching " + info + " is not supported");
            return;
        }

        try {
            TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
            line.open(format);
            line.start();

            // Initialize audio playback
            byte[] audioData = new byte[4096];

            // Start the audio sender thread
            Thread audioSenderThread = new Thread(() -> {
                try {
                    InetAddress serverAddress = InetAddress.getByName(serverIp);
                    while (running) {
                        int bytesRead = line.read(audioData, 0, audioData.length);
                        if (bytesRead > 0) {
                            DatagramPacket audioPacket = new DatagramPacket(audioData, bytesRead, serverAddress, serverAudioPort);
                            audioSocket.send(audioPacket);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            audioSenderThread.start();

            // Start the video sender thread
            Java2DFrameConverter converter = new Java2DFrameConverter();
            while (running) {
                Frame frame = videoFrames.take();
                if (frame != null) {
                    BufferedImage bufferedImage = converter.convert(frame);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    try {
                        ImageIO.write(bufferedImage, "jpg", baos);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    byte[] videoBytes = baos.toByteArray();

                    int packetSize = 65507 - 8;  // subtract the size of the header
                    int totalPackets = (videoBytes.length + packetSize - 1) / packetSize;  // compute the total number of packets

                    ByteBuffer packetBuffer = ByteBuffer.allocate(packetSize + 8);  // create a ByteBuffer for the packet

                    for (int i = 0; i < videoBytes.length; i += packetSize) {
                        packetBuffer.clear();  // clear the ByteBuffer

                        int actualPacketSize = Math.min(packetSize, videoBytes.length - i);

                        packetBuffer.putInt(i / packetSize);  // put the packet number
                        packetBuffer.putInt(totalPackets);  // put the total number of packets
                        packetBuffer.put(videoBytes, i, actualPacketSize);  // put the actual data

                        byte[] packetBytes = packetBuffer.array();
                        DatagramPacket videoPacket = new DatagramPacket(packetBytes, 8 + actualPacketSize, InetAddress.getByName(serverIp), serverVideoPort);
                        videoSocket.send(videoPacket);
                    }
                }
            }
        } catch (LineUnavailableException | IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            running = false;
            audioSocket.close();
            videoSocket.close();
        }
    }

    public void stopStreamer() {
        running = false;

    }
}
