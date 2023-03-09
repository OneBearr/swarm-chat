import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class ChatRoomServer {
    private static final String BACKUP_SERVER_HOSTNAME = "localhost";
    private static final int MAIN_SERVER_PORT = 8888;
    private static final int BACKUP_SERVER_PORT = 9001;
    private static final int BACKUP_DELAY_TIME = 3000;      // in ms
    private static final int BACKUP_PERIOD_TIME = 2000;     // in ms
    
    private Map<String, ChatRoom> chatRooms;
    private Map<String, List<String>> chatRoomHistoryMsgs;  // save the history msgs from back-up server 
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;

    public ChatRoomServer() {
        this.chatRooms = new HashMap<>();
        this.chatRoomHistoryMsgs = new HashMap<>();
    }

    public void start() throws IOException {
         // Retrieve backup data from the BackupServer
         getBackupData();

        // Backup data to the BackupServer every k seconds
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                backupData();
            }
        }, BACKUP_DELAY_TIME, BACKUP_PERIOD_TIME);

        // Start listening incoming requests
        try (ServerSocket serverSocket = new ServerSocket(MAIN_SERVER_PORT)) {
            System.out.println("Server started on port " + MAIN_SERVER_PORT);  
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // // A heuristic dynamic socket timeout mechanism
                // int count = Thread.activeCount();
                // if (count < 10) {
                //     clientSocket.setSoTimeout(1000 * 60);
                // } else if (count < 20) {
                //     clientSocket.setSoTimeout(1000 * 30);
                // } else {
                //     clientSocket.setSoTimeout(1000 * 5);
                // }
                // ChatRoomHandler chatRoomHandler = new ChatRoomHandler(this, clientSocket);
                // new Thread(chatRoomHandler).start();
                new Thread(new ChatRoomHandler(this, clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } 
    }

    private void getBackupData() {
        try (Socket socket = new Socket(BACKUP_SERVER_HOSTNAME, BACKUP_SERVER_PORT)) {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            inputStream = new ObjectInputStream(socket.getInputStream());
            System.out.println("Start retrieving bakcup data");
            // Request history data
            outputStream.writeObject("getHistory");
            outputStream.flush();
            // Received the history data from backup server
            Object object = inputStream.readObject();
            // put the history data into chatRoomHistoryMsgs 
            if (object instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, List<String>> backupData = (Map<String, List<String>>) object;
                if (object != null) {
                    System.out.println("Received bakcup map size: " + backupData.size());
                }
                chatRoomHistoryMsgs.putAll(backupData);
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
    // Backup the chat history in the chatRooms to the BackupServer
    private synchronized void backupData() {
        try (Socket socket = new Socket(BACKUP_SERVER_HOSTNAME, BACKUP_SERVER_PORT)) {
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            // Get chat history from the chatRooms and chatRoomHistoryMsgs
            Map<String, List<String>> hstryMsgs = chatRooms.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,                          // Get the key from the original Map
                    entry -> entry.getValue().getHistoryMsgs()  // Get the historyMsgs list from ChatRoom
                ));
            hstryMsgs.putAll(chatRoomHistoryMsgs);
            System.out.println("The size of backup map: " + hstryMsgs.size());
            outputStream.writeObject(hstryMsgs);
            outputStream.flush();
        } catch (IOException e) {
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

    public synchronized ChatRoom getChatRoom(String roomId) {
        ChatRoom chatRoom = chatRooms.get(roomId);
        if (chatRoom == null) {
            chatRoom = new ChatRoom(roomId);
            // Restore the historical data of chatroom by roomId
            if (chatRoomHistoryMsgs.containsKey(roomId)) {
                chatRoom.setHistoryMsgs(chatRoomHistoryMsgs.get(roomId));
                chatRoomHistoryMsgs.remove(roomId);
            }
            chatRooms.put(roomId, chatRoom);
            System.out.println("Created chat room: " + roomId);
        }
        return chatRoom;
    }

    public synchronized void removeChatRoom(String roomId) {
        ChatRoom chatRoom = chatRooms.get(roomId);
        if (chatRoom != null) {
            chatRooms.remove(roomId);
            System.out.println("Removed chat room: " + roomId);
        }
    }

    public static void main(String[] args) {
        ChatRoomServer server = new ChatRoomServer();
        try {
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
