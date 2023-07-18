import org.bytedeco.javacv.*;
import org.bytedeco.javacv.Frame;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.IplImage;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaPlayerComponent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Main {
    private static final String broadcastAddress = "192.168.0.12:5000";
    private static EmbeddedMediaPlayerComponent mediaPlayerComponent;


    public static void main(String[] args) {
        int width = 1000;
        int height = 550;

        Dimension mainFrameDimensions = new Dimension(width,height);
        Dimension mainPanelDimensions = new Dimension(width/2,height/2);
        Dimension chatPanelDimensions = new Dimension(width/2, height/4);
        Dimension btnDimensions = new Dimension(width/4, height/4);


        JFrame frame = new JFrame("Ome TV");
        BorderLayout mainLayout = new BorderLayout();
        frame.setLayout(mainLayout);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setResizable(false);
        frame.setSize(mainFrameDimensions);
        frame.setPreferredSize(mainFrameDimensions);

        mediaPlayerComponent = new EmbeddedMediaPlayerComponent();


        // streamers
        VideoStreamer myVideoStreamer = new VideoStreamer();
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.out.println("Closing the frame...");
                myVideoStreamer.stop();
                frame.dispose();
            }
        });




        JPanel myPanel = new JPanel();
        myPanel.setLayout(new BorderLayout());
        myPanel.setSize(mainPanelDimensions);
        myPanel.setPreferredSize(mainPanelDimensions);
        myPanel.setBackground(Color.RED);








        JPanel guestPanel = new JPanel();
        guestPanel.setLayout(new BorderLayout());
        guestPanel.setSize(mainPanelDimensions);
        guestPanel.setPreferredSize(mainPanelDimensions);

        guestPanel.add(mediaPlayerComponent.videoSurfaceComponent(), BorderLayout.CENTER);



        JPanel chatPanel = new JPanel();
        chatPanel.setBackground(Color.GREEN);
        chatPanel.setSize(chatPanelDimensions);
        chatPanel.setPreferredSize(chatPanelDimensions);


        JPanel controlsPanel = new JPanel();
        controlsPanel.setLayout(new BorderLayout());
        controlsPanel.setBackground(Color.ORANGE);
        controlsPanel.setSize(chatPanelDimensions);
        controlsPanel.setPreferredSize(chatPanelDimensions);


        JButton startNextBtn = new JButton("Start");
        startNextBtn.addActionListener(actionEvent -> {
            try {
                myVideoStreamer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        startNextBtn.setBackground(new Color(172, 218, 91, 255));
        startNextBtn.setSize(btnDimensions);
        startNextBtn.setPreferredSize(btnDimensions);
        JButton stopBtn = new JButton("Stop");
        stopBtn.setBackground(new Color(248, 85, 85));
        stopBtn.setSize(btnDimensions);
        stopBtn.setPreferredSize(btnDimensions);


        controlsPanel.add(startNextBtn, BorderLayout.WEST);
        controlsPanel.add(stopBtn, BorderLayout.EAST);


        myPanel.add(controlsPanel, BorderLayout.SOUTH);
        guestPanel.add(chatPanel, BorderLayout.SOUTH);

        frame.add(myPanel, BorderLayout.WEST);
        frame.add(guestPanel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);





        SwingUtilities.invokeLater(() -> {
            mediaPlayerComponent.mediaPlayer().media().play("udp://@" + broadcastAddress); // replace with your IP and port
        });
    }



}
