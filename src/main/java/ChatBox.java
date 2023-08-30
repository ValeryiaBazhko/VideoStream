import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class ChatBox extends JPanel {

    private JTextPane textPane;
    private JTextField inputField;
    private JButton sendBtn;
    private StyledDocument document;
    private Style userMessageStyle;
    private Style otherUserMessageStyle;

    boolean welcome;
    String welcomeMsg = "Welcome to the chat!";

    public ChatBox() {
        this.setLayout(new BorderLayout());

        textPane = new JTextPane();
        textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textPane);
        this.add(scrollPane, BorderLayout.CENTER);

        document = textPane.getStyledDocument();

        userMessageStyle = textPane.addStyle("UserMessageStyle", null);
        StyleConstants.setForeground(userMessageStyle, Color.BLUE);

        otherUserMessageStyle = textPane.addStyle("OtherUserMessageStyle", null);
        StyleConstants.setForeground(otherUserMessageStyle, Color.BLACK);

        JPanel inputPanel = new JPanel(new BorderLayout());

        inputField = new JTextField();
        inputPanel.add(inputField, BorderLayout.CENTER);

        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    sendMessage();
                }
            }
        });

        sendBtn = new JButton("Send");
        sendBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String message = inputField.getText();
                if (!message.isEmpty()) {
                    sendMessage();
                }
            }
        });
        inputPanel.add(sendBtn, BorderLayout.EAST);

        this.add(inputPanel, BorderLayout.SOUTH);
        welcome = true;
        appendMessage(welcomeMsg, null);
    }

    public void appendMessage(String message, String userName) {
        try {
                if (userName == null) {
                    document.insertString(document.getLength(), message + "\n", null);
                } else {
                    Style style = userName.equals("You") ? userMessageStyle : otherUserMessageStyle;
                    document.insertString(document.getLength(), userName + ": " + message + "\n", style);
                }
            } catch(Exception e){
                e.printStackTrace();
            }
        }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (welcome) {
            try {
                document.remove(0, welcomeMsg.length());
                welcome = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (!message.isEmpty()) {
            appendMessage(message, "You");
            inputField.setText("");
        }
    }


    public JTextPane getTextPane() {
        return textPane;
    }

    public StyledDocument getDocument() {
        return document;
    }

    public void setDocument(StyledDocument document) {
        this.document = document;
    }

    public void clear() {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            doc.remove(0, doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
}
