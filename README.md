## Welcome to my Swarm Chat repo

### This project implemented a Java-based distributed chat room  :)

The code is upcoming and updating.

Current description and achivement:  
This swarm chat application allows multiple users to participate in concurrent conversations across multiple chat rooms. Users receive notifications when other users join or leave a chat room, or when messages are sent. To prevent loss of chat messages in the event of unexpected crashes or outages of the user or main server, all chat messages are replicated and stored in a backup. When the user or server comes back online, the chat history is restored.

The application leverages several key distributed system concepts, including Broadcast/Multicast, Mutual Exclusion, Timestamps, Concurrency Control, Replication and Consistency, among others.

**To Run the Chat Room**
1. Install Java environment properly
2. Go to the project folder
3. Compile all the Java files  
  `$ javac *.java`
4. Run the backup server  
  `$ java ChatRoomBackupServer`
5. Run the main server  
  `$ java ChatRoomServer`
6. Then you can create as many chat room users as you want, make sure the room number is an int and username is no empty.   
  `$ java ChatRoomClient`

### Architecture Overview  
<img src="https://user-images.githubusercontent.com/56367478/227619369-df0f8001-8e67-40ec-8833-e45f87d3c751.jpg" alt="ChatRoom Explain" width="600"/>

### Chat Demo  
<img src="https://user-images.githubusercontent.com/56367478/227622747-acf42617-3463-4e38-93ce-349c6d456149.jpg" alt="chat demo" width="600"/>
