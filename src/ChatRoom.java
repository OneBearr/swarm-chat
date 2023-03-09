import java.io.PrintWriter;
import java.util.*;

public class ChatRoom {
    private final String id;
    private Map<String, PrintWriter> users;
    private List<String> historyMsgs;

    public ChatRoom(String id) {
        this.id = id;
        this.users = new HashMap<>();
        historyMsgs = new LinkedList<>();
    }

    public String getId() {
        return id;
    }

    public int getUsersNum() {
        return users.size();
    }

    public synchronized void setHistoryMsgs(List<String> hstryMsgs) {
        historyMsgs = hstryMsgs;
    }

    public synchronized List<String> getHistoryMsgs() {
        return historyMsgs;
    }
    
    public synchronized void addUser(String username, PrintWriter out) {
        users.put(username, out);
        int size = historyMsgs.size();
        // sync the lastest 5 msg
        if (size > 0) {
            PrintWriter newUserOut = users.get(username);
            int startIdx = Math.max(0, size - 5);
            for (int i = startIdx; i < size; i++) {
                newUserOut.println(historyMsgs.get(i));
            }
        }
    }

    public synchronized int removeUser(String username) {
        users.remove(username);
        return users.size();
    }

    public synchronized void broadcast(String username, String message, String time) {
        String broadcastMsg = time + " " + username + ": " + message;
        historyMsgs.add(broadcastMsg);
        for (PrintWriter out : users.values()) {
            out.println(broadcastMsg);
        }
    }
}
