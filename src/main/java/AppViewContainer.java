import com.google.common.eventbus.EventBus;
import org.bytedeco.javacv.Frame;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

public class AppViewContainer extends JPanel {

    private boolean stopped;

    private EventBus eventBus;
    private BlockingQueue<Frame> frames, framesReceived;
    private JPanel videoViewContainer, controlsContainer, btnContainer;
    private VideoView myVideoView, otherVideoView;
    private JButton startNextBtn, stopBtn;
    private ChatBox chatBox;


    public AppViewContainer(EventBus eventBus, BlockingQueue<Frame> frames, BlockingQueue<Frame> framesReceived) {
        this.eventBus = eventBus;
        this.frames = frames;
        this.framesReceived = framesReceived;
        this.stopped = false;
        this.setLayout(new BorderLayout());
        //this.setPreferredSize(AppConstants.DEFAULT_CONTAINER_DIMENSION);


        videoViewContainer = new JPanel();
        BorderLayout videoBorderLayout = new BorderLayout();
        videoBorderLayout.setHgap(5);
        videoViewContainer.setLayout(videoBorderLayout);
        videoViewContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        myVideoView = new VideoView(frames, true);
        myVideoView.setBackground(Color.BLUE);
        otherVideoView = new VideoView(framesReceived, false);
        otherVideoView.setBackground(Color.BLACK);

        videoViewContainer.add(myVideoView, BorderLayout.WEST);
        videoViewContainer.add(otherVideoView, BorderLayout.EAST);


        controlsContainer = new JPanel();
        BorderLayout controlsBorderLayout = new BorderLayout();
        setLayout(controlsBorderLayout);

        btnContainer = new JPanel();
        GridLayout btnGridLayout = new GridLayout();
        btnGridLayout.setColumns(2);
        btnContainer.setLayout(btnGridLayout);
        btnContainer.setBackground(Color.RED);
        btnContainer.setPreferredSize(AppConstants.DEFAULT_CHAT_CONTAINER_DIMENSION);


        EventHandler eventHandler = new EventHandler();


        startNextBtn = new JButton("Start");
        startNextBtn.addActionListener(eventHandler);
        stopBtn = new JButton("Stop");
        stopBtn.addActionListener(eventHandler);
        btnContainer.add(startNextBtn, BorderLayout.WEST);
        btnContainer.add(stopBtn, BorderLayout.EAST);


        chatBox = new ChatBox();
        chatBox.setPreferredSize(AppConstants.DEFAULT_CHAT_CONTAINER_DIMENSION);

        controlsContainer.add(btnContainer, BorderLayout.WEST);
        controlsContainer.add(chatBox, BorderLayout.EAST);


        this.add(videoViewContainer, BorderLayout.NORTH);
        this.add(controlsContainer, BorderLayout.SOUTH);
    }

    public void appendChatMessage(String message, String name){
        chatBox.appendMessage(message,name);
    }


    private class EventHandler implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource().equals(startNextBtn)) {
                eventBus.post(new Event(EventType.START_BTN));
                if(stopped) {
                    otherVideoView.updateWithBlackFrame();
                    stopped = false;
                }
            } else if (e.getSource().equals(stopBtn)) {
                eventBus.post(new Event(EventType.STOP_BTN));
                if(!stopped) {
                    otherVideoView.updateWithBlackFrame();
                    stopped = true;
                }
            }
        }
    }
}
