import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class ChatRoomClient extends JFrame {
    private static final String MAIN_SERVER_HOSTNAME = "localhost";
    private static final int MAIN_SERVER_PORT = 8888;
    private JTextField messageField;
    private JTextArea messageArea;
    private PrintWriter printWriter;
    private BufferedReader bufferedReader;
    public ChatRoomClient(String roomId, String username, Socket socket) throws IOException {
        // Create I/O objects
        printWriter = new PrintWriter(socket.getOutputStream(), true);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
        // Setup the chat window by JFrame
        setTitle("Chat Room - " + roomId + " - " + username);
        // Setting the action when the window is closed, 
        // setting it to JFrame.EXIT_ON_CLOSE here means that closing the window will end the entire program's execution.
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // Sending a request to leave the chat room.
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                String time = formatter.format(date);
                printWriter.println("LEAVE " + time + " " + roomId + " " + username);
                try {
                    printWriter.close();
                    bufferedReader.close(); 
                    socket.close();       
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        setSize(400, 400);      // chat window size
        
        // Create a text area messageArea and set it to read-only.
        messageArea = new JTextArea();
        messageArea.setEditable(false);
        // Wrap the text area, which enables the scrolling functionality.
        JScrollPane scrollPane = new JScrollPane(messageArea);
        // Set the display policy of the vertical scroll bar of the text area, here it is set to always display.
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        // Add the text area, scroll bar, and horizontal layout panel to the chat window.
        add(scrollPane, BorderLayout.CENTER);
        // Create a text input field.
        messageField = new JTextField();
        // Create a "Send" button
        JButton sendButton = new JButton("Send");
        // Set hit Enter to trigger "Send" button
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendButton.doClick();
            }
        });
        
        // Add an event listener to the button, which triggers the "send message" event when the user clicks on it.
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                // Get the user input message from the text field.
                String message = messageField.getText();
                // Check if there are any sensitive words
                if (isValidMessage(message)){
                    // Message Timestamp
                    Date date = new Date();
                    SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                    String time = formatter.format(date);
                    printWriter.println("MSG " + time + " " + roomId + " "  + message);
                } else {
                    messageArea.append("Warning: sentitive words included!!!" + "\n");
                }
                // Reset the input text field.
                messageField.setText("");
            }
        });
        // Create a panel
        JPanel bottomPanel = new JPanel();
        // Set the layout manager of bottomPanel to BoxLayout and set it to use horizontal layout
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        // Add the message input field and send button to the panel bottomPanel
        bottomPanel.add(messageField);
        bottomPanel.add(sendButton);
        // Add the panel to the bottom of the chat room client window
        add(bottomPanel, BorderLayout.SOUTH);
        // Display the window
        setVisible(true);

        // Notify the server that a new user has joined
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String time = formatter.format(date);
        printWriter.println("JOIN " + time + " " + roomId + " " + username);

        // Used to listen for incoming messages
        new Thread(new Receiver()).start();
    }
    
    private boolean isValidMessage(String message) {
        String[] words = message.split(" ");
        for (String w : words) {
            if (w.equals("bad")) return false;      // just an example
        }
        return true;
    }
    
    // Receive chat messages sent by the server in a separate thread
    private class Receiver implements Runnable {               
        public void run() {
            try {
                String inputLine;
                while ((inputLine = bufferedReader.readLine()) != null) {
                    messageArea.append(inputLine + "\n");
                }                
            } catch (SocketException e) {
                dispose();
            } catch (IOException e) {
                dispose();
                e.printStackTrace();
            } 
        }
    }

    public static void main(String[] args) {
        // make sure if the roomId is valid (integer)
        String roomId = JOptionPane.showInputDialog("Enter chat room number:");
        if (roomId == null) {
            System.exit(0);
        }
        while (roomId.trim().length() == 0 || !isInteger(roomId)) {
            roomId = JOptionPane.showInputDialog("Enter chat room number:");
            if (roomId == null) {
                System.exit(0);
            }
        }
        // make sure if the username is valid (not null or empty)
        String username = JOptionPane.showInputDialog("Enter your username:");
        if (username == null) {
            System.exit(0);
        }
        while (username.trim().length() == 0) {
            username = JOptionPane.showInputDialog("Enter your username:");
            if (username == null) {
                System.exit(0);
            }
        }

        try {
            // Connect to the server port on the local machine
            Socket socket = new Socket(MAIN_SERVER_HOSTNAME, MAIN_SERVER_PORT);
            // Create a new chat user and window
            new ChatRoomClient(roomId, username, socket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
