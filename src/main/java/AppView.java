import com.google.common.eventbus.EventBus;
import org.bytedeco.javacv.Frame;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.util.concurrent.BlockingQueue;


public class AppView extends JFrame {

    private EventBus eventBus;
    private BlockingQueue<Frame> frames, framesReceived;
    private CardLayout cardLayout;
    private JPanel cards;
    private AppViewContainer appViewContainer;


    public AppView(EventBus eventBus, BlockingQueue<Frame> frames, BlockingQueue<Frame> framesReceived){
        this.eventBus = eventBus;
        this.frames = frames;
        this.framesReceived = framesReceived;
        eventBus.register(this);

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width/2-this.getSize().width/2-200, dim.height/2-this.getSize().height/2-100);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setResizable(false);
        this.setTitle("ChatBox");

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        appViewContainer = new AppViewContainer(eventBus, frames, framesReceived);
        cards.add(appViewContainer, "appViewContainer");

        this.setContentPane(cards);

        this.pack();
        this.setVisible(true);
    }
}
