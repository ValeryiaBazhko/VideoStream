import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import javax.swing.*;

public class VideoPlayer {

    private static String broadcastAddress = "192.168.0.12:5000";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Video Player");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.setBounds(100, 100, 600, 400);

            EmbeddedMediaPlayerComponent mediaPlayerComponent = new EmbeddedMediaPlayerComponent();
            frame.setContentPane(mediaPlayerComponent);

            frame.setVisible(true);

            mediaPlayerComponent.mediaPlayer().media().play("udp://@" + broadcastAddress); // replace with your IP and port
        });
    }
}
