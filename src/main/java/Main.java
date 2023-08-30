import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameGrabber;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.swing.*;
import javax.swing.text.StyledDocument;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

    private static EventBus eventBus;
    private static Thread graberThread;
    private static Streamer streamer;
    private static boolean streamRunning = false;
    private static BlockingQueue<Frame> framesStreamer;
    private static ChatBox chatBox;
    private static BlockingQueue<byte[]> linesAudio;

    public Main(EventBus eventBus, ChatBox chatBox) {
        this.eventBus = eventBus;
        this.chatBox = chatBox;
        eventBus.register(this);
    }

    public static void main(String[] args) {
        eventBus = new EventBus();
        chatBox = new ChatBox();
        Main mainInstance = new Main(eventBus, chatBox);

        BlockingQueue<Frame> frames = new LinkedBlockingQueue<>(100);
        framesStreamer = new LinkedBlockingQueue<>(100);
        BlockingQueue<Frame> framesReceived = new LinkedBlockingQueue<>(100);


        OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);
        grabber.setImageWidth(480);
        grabber.setImageHeight(360);
        try {
            grabber.start();
        } catch (FrameGrabber.Exception e) {
            System.out.println("Unable to start the grabber");
        }

        // thread responsible for grabbing the frames and putting them in the frames queue
        graberThread = new Thread(() -> {
            Frame capturedFrame;
            while (true) {
                try {
                    capturedFrame = grabber.grab();
                    if (!frames.offer(capturedFrame)) {
                        System.out.println("frames queue is full");
                    }
                    if (!framesStreamer.offer(capturedFrame) && streamRunning) {
                        System.out.println("streamer queue is full");
                    }
                } catch (Exception e) {
                    System.out.println("Unable to grab frame");
                }
            }
        });
        graberThread.start();


        StreamReceiver streamReceiver = new StreamReceiver(6000,5000, framesReceived);
        streamReceiver.start();

        // starting the ui
        SwingUtilities.invokeLater(() -> new AppView(eventBus, frames, framesReceived));
    }

    @Subscribe
    private void processEvent(Event event) {
        switch (event.getEventType()) {
            case START_BTN:
                streamer = new Streamer(framesStreamer, linesAudio, "192.168.1.102", 5000,6000);
                streamer.start();
                streamRunning = true;
                break;
            case STOP_BTN:
                streamRunning = false;
                streamer.stopStreamer();
                framesStreamer.clear();
                chatBox.clear();
                break;
        }
    }
}
