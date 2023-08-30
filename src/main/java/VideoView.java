import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

public class VideoView extends JPanel {

    private BlockingQueue<Frame> frames;
    private Java2DFrameConverter converter = new Java2DFrameConverter();
    private BufferedImage bufferedImage;
    private boolean myVideo;
    private boolean streaming;

    public VideoView(BlockingQueue<Frame> frames, boolean myVideo) {
        this.frames = frames;
        this.myVideo = myVideo;
        this.streaming = true; // Initially, set streaming to true to start streaming

        this.setPreferredSize(AppConstants.DEFAULT_VIDEO_SIZE);

        new Thread(() -> {
            while (true) {
                try {
                    Frame frame = frames.take();
                    if (frame != null) {
                        if (streaming) {
                            bufferedImage = converter.convert(frame);
                            repaint(); // this will call paintComponent
                        } else {
                            bufferedImage = null; // Set bufferedImage to null when streaming is stopped
                            repaint(); // this will call paintComponent and display the black frame
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        if (bufferedImage != null) {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.drawImage(bufferedImage, 0, 0, getWidth(), getHeight(), null);
        } else if (streaming) {
            // If streaming is not stopped, but bufferedImage is null, update with black frame
            updateWithBlackFrame(g2d);
        }
    }

    public void updateWithBlackFrame() {
        streaming = !streaming; // Toggle streaming state
        if (streaming) {
            // If streaming is resumed, set bufferedImage to null to start displaying received frames again
            bufferedImage = null;
        }
        repaint(); // this will call paintComponent and display the appropriate frame
    }

    private void updateWithBlackFrame(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
