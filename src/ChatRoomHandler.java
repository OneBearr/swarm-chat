import java.io.*;
import java.net.Socket;
import java.net.SocketException;

public class ChatRoomHandler implements Runnable {
    private final ChatRoomServer server;
    private final PrintWriter printWriter;
    private final BufferedReader bufferedReader;
    private final Socket clienSocket;
    private ChatRoom chatRoom;
    private String inputLine;
    private String command;
    private String time;
    private String roomId;
    private String username;

    public ChatRoomHandler(ChatRoomServer server, Socket clientSocket) throws IOException {
        this.server = server;
        this.clienSocket = clientSocket;
        this.printWriter = new PrintWriter(clientSocket.getOutputStream(), true);
        this.bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }

    public void run() {
        try {
            while ((inputLine = bufferedReader.readLine()) != null) {
                String[] tokens = inputLine.split(" ");
                command = tokens[0];
                time = tokens[1];
                roomId = tokens[2];
                if (command.equals("JOIN")) {           // New user joined the chat room
                    username = tokens[3];
                    chatRoom = server.getChatRoom(roomId);
                    chatRoom.addUser(username, printWriter);
                    chatRoom.broadcast(username, " joined room " + roomId, time);
                    System.out.println(time + " " + username + " joined room " + roomId);
                } else if (command.equals("LEAVE")) {   // user left the chat room
                    username = tokens[3];
                    int numPeople = chatRoom.removeUser(username);
                    if (numPeople <= 0) {
                        server.removeChatRoom(roomId);
                    }
                    chatRoom.broadcast(username, " left room " + roomId, time);
                    System.out.println(time + " " + username + " left room " + chatRoom.getId());
                } else if (command.equals("MSG")) {     // user sent a message
                    String msg = inputLine.substring(command.length() + time.length() + roomId.length() + 3);
                    chatRoom.broadcast(username, msg, time);
                    System.out.println(time + " at room " + chatRoom.getId() + ", " + username + ": " + msg);
                }
            }
        } catch (SocketException e) {
            if (!clienSocket.isClosed()) {
                try {
                    clienSocket.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
