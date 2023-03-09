import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomBackupServer {
    private static final int BACKUP_SERVER_PORT = 9001;
    private Map<String, List<String>> chatRoomHistoryMsgs;

    public ChatRoomBackupServer() {
        this.chatRoomHistoryMsgs = new HashMap<>();
    }

    public static void main(String[] args) {
        ChatRoomBackupServer backupServer = new ChatRoomBackupServer();
        backupServer.start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(BACKUP_SERVER_PORT)) {
            System.out.println("Backup server is running");
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(new BackupServerHandler(socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class BackupServerHandler implements Runnable {
        private final Socket socket;
        private ObjectInputStream inputStream;
        private ObjectOutputStream outputStream;

        public BackupServerHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());
                outputStream = new ObjectOutputStream(socket.getOutputStream());
                Object object = inputStream.readObject();
                if (object instanceof String && object.equals("getHistory")) {
                    // Handling the request from MainServer to obtain the history data
                    int size = chatRoomHistoryMsgs.size();
                    outputStream.writeObject(chatRoomHistoryMsgs);
                    System.out.println("send back to main server as MAP size: " + size);
                    outputStream.flush();
                } else if (object instanceof Map) {
                    // Handling the backup data sent by MainServer
                    @SuppressWarnings("unchecked")
                    Map<String, List<String>> backupData = (Map<String, List<String>>) object;
                    System.out.println("updating backup MAP size is " + backupData.size());
                    chatRoomHistoryMsgs.putAll(backupData);
                } 
            } catch (SocketException e) {
                if (!socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
