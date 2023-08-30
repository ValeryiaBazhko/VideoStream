import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;

public class StreamReceiver extends Thread {
    private final int audioPort;
    private final int videoPort;
    private boolean running;
    private BlockingQueue<Frame> frameQueue;

    public StreamReceiver(int audioPort, int videoPort, BlockingQueue<Frame> frameQueue) {
        this.audioPort = audioPort;
        this.videoPort = videoPort;
        this.frameQueue = frameQueue;
        running = false;
    }

    @Override
    public void run() {
        running = true;

        // Start the audio receiver thread
        Thread audioReceiverThread = new Thread(() -> {
            try (DatagramSocket audioSocket = new DatagramSocket(audioPort)) {
                AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
                float rate = 44100.0f;
                int channels = 2;
                int sampleSize = 16;
                boolean bigEndian = true;
                AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);

                DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                if (!AudioSystem.isLineSupported(info)) {
                    System.out.println("Line matching " + info + " is not supported");
                    return;
                }

                SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();

                byte[] audioData = new byte[4096];
                DatagramPacket audioPacket = new DatagramPacket(audioData, audioData.length);

                while (running) {
                    audioSocket.receive(audioPacket);
                    line.write(audioPacket.getData(), 0, audioPacket.getLength());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        audioReceiverThread.start();

        // Start the video receiver thread
        Thread videoReceiverThread = new Thread(() -> {
            try (DatagramSocket videoSocket = new DatagramSocket(videoPort)) {
                while (running) {
                    SortedMap<Integer, byte[]> packets = new TreeMap<>();  // sorted map to hold the packets
                    int totalPackets = -1;  // the total number of packets
                    int receivedPackets = 0;  // the number of received packets

                    while (running) {
                        byte[] buf = new byte[65507];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        videoSocket.receive(packet);

                        ByteBuffer packetBuffer = ByteBuffer.wrap(buf);

                        int packetNumber = packetBuffer.getInt();
                        if (totalPackets == -1) {
                            totalPackets = packetBuffer.getInt();
                        }

                        int dataSize = packet.getLength() - 8;
                        byte[] packetData = new byte[dataSize];
                        packetBuffer.get(packetData, 0, dataSize);  // use the actual dataSize, not packet.getLength() - 8

                        packets.put(packetNumber, packetData);

                        receivedPackets++;

                        if (receivedPackets == totalPackets) {
                            break;
                        }
                    }

                    // Combine all the packets into a single byte array
                    int totalSize = packets.values().stream().mapToInt(a -> a.length).sum();
                    ByteBuffer imageBuffer = ByteBuffer.allocate(totalSize);

                    for (byte[] packetData : packets.values()) {
                        imageBuffer.put(packetData);
                    }

                    byte[] imageBytes = imageBuffer.array();

                    // Convert the byte array to a BufferedImage
                    BufferedImage bufferedImage = null;
                    try {
                        bufferedImage = ImageIO.read(new ByteArrayInputStream(imageBytes));
                    } catch (javax.imageio.IIOException e) {
                        System.out.println("Invalid image input");
                    }

                    if (bufferedImage == null) {
                        //System.out.println("Failed to create BufferedImage");
                    } else {
                        //System.out.println("BufferedImage type: " + bufferedImage.getType());
                        // Convert the BufferedImage to a Frame
                        Java2DFrameConverter converter = new Java2DFrameConverter();
                        Frame frame = converter.convert(bufferedImage);

                        // Add the frame to the queue
                        if (frame != null) {
                            frameQueue.offer(frame);
                            //System.out.println("new frame received");
                        } else {
                            //System.out.println("Failed to convert image to frame");
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        videoReceiverThread.start();
    }

    public void stopRunning() {
        running = false;
    }
}
