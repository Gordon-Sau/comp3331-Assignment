package src.Server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.Set;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Server {
    private static int serverPort;
    private static int blockDuration;

    // when there is no message received in timeout
    // disconnect and log the user out
    private static int timeout;

    // in-memory copy of the credential.txt
    private static Map<String, String> credentials = new ConcurrentHashMap<>();

    // write to the credential.txt file
    private static FileWriter credentialsWriter;

    // add when 3 invalid logins
    // check whether the user is blocked(in the key or current time > time) for each login
    // remove the username if he/she is no longer blocked
    private static Map<String, LocalDateTime> blockedLogins = new ConcurrentHashMap<>();

    // add to connections if the user has logged in
    private static Map<String, ClientThread> connections = new ConcurrentHashMap<>();

    // add to the blacklist when the user blocks another users
    // the blocker is in the value, the one who got blocked is in the key
    private static Map<String, Set<String>> blackLists = new ConcurrentHashMap<>();

    // if online and have read the message(after valid login), remove the user from
    // the map
    // if become offline, add back with an empty list as the value
    // NOTE: can also be used to check if one is offline
    private static Map<String, List<String>> unreadMessages = new ConcurrentHashMap<>();

    // history (stores login time for each users when they have logged in
    // successfully(correct password))
    private static Map<String, LocalDateTime> history = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.out.println("===== Error usage: java Server SERVER_PORT BLOCK_DURATION TIMEOUT =====");
            return;
        }

        // store credentials
        // and create unreadMessages for all users in the credentials.txt
        Scanner myReader = new Scanner(new File(".", "credentials.txt"));
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] credentialTuple = data.split(" ");
            credentials.put(credentialTuple[0], credentialTuple[1]);
            unreadMessages.put(credentialTuple[0], new ArrayList<>());
        }
        myReader.close();

        credentialsWriter = new FileWriter(new File(".", "credentials.txt"), true);

        // create welcomeSocket
        serverPort = Integer.parseInt(args[0]);
        blockDuration = Integer.parseInt(args[1]);
        timeout = Integer.parseInt(args[2]) * 1000;
        ServerSocket welcomeSocket = new ServerSocket(serverPort);
        System.out.println("server is running");

        // create clientSocket and ClientThread
        while (true) {
            Socket clientSocket = welcomeSocket.accept();
            ClientThread clientThread = new ClientThread(clientSocket);
            clientThread.start();
        }
    }

    public static class ClientThread extends Thread {
        private final Socket clientSocket;
        private ServerState state;
        public String username;
        public BufferedReader inFromClient;
        public BufferedWriter outToClient;
        private boolean isConnecting = true;

        // the peer username when the client being asked for private permission
        // it should be null, if the client is not asking for permission
        // this only works if only one peer is trying to connect (which is fine in this assignment)
        // if multiple peers are trying to connect, a set of peer's username is needed
        private String privatePermissionPeerUsername;

        ClientThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        public Socket getSocket() {
            return clientSocket;
        }

        public int getBlockDuration() {
            return blockDuration;
        }

        public int getTimeoutLength() {
            return timeout;
        }

        public Map<String, String> getCredentials() {
            return credentials;
        }

        public Map<String, LocalDateTime> getBlockedLogins() {
            return blockedLogins;
        }

        public Map<String, ClientThread> getConnections() {
            return connections;
        }

        public Map<String, Set<String>> getBlackList() {
            return blackLists;
        }

        public Map<String, List<String>> getUnreadMessages() {
            return unreadMessages;
        }

        public void setState(ServerState state) {
            synchronized (this) {
                this.state = state;
            }
        }

        public boolean writeToClient(String message) {
            try {
                synchronized(outToClient) {
                    outToClient.write(message);
                    outToClient.flush();
                }
                return true;
            } catch (IOException e) {
                // e.printStackTrace();
                disconnect();
                return false;
            }
        }

        public boolean writeToClientBuffered(String message) {
            try {
                outToClient.write(message);
                return true;
            } catch (IOException e) {
                // e.printStackTrace();
                disconnect();
                return false;
            }
        }

        public boolean writeToClientFlush() {
            try {
                outToClient.flush();
                return true;
            } catch (IOException e) {
                // e.printStackTrace();
                disconnect();
                return false;
            }
        }

        public boolean sendUnreadMessages() {
            boolean isSent;
            synchronized (outToClient) {
                for (String unreadMessage : getUnreadMessages().get(username)) {
                    if (!writeToClientBuffered("unread " + unreadMessage + '\n')) {
                        return false;
                    }
                }
                isSent = writeToClientFlush();
            }
            if (isSent) {
                unreadMessages.remove(username);
            }
            return isSent;
        }

        public void appendCredential(String str) {
            synchronized (credentialsWriter) {
                try {
                    credentialsWriter.append(str);
                    credentialsWriter.flush();
                } catch (IOException e) {
                    System.out.println("fail to write to credentials.txt");
                    // e.printStackTrace();
                }
            }
        }

        public void disconnect() {
            synchronized (this) {
                this.isConnecting = false;
            }
        }

        public void blockUser() {
            blockedLogins.put(username, LocalDateTime.now().plusSeconds(blockDuration));
        }

        // get online users except the user and the blockers
        public List<String> getOnlineUsers() {
            Set<String> blockers = blackLists.get(username);
            if (blockers == null) {
                return connections.keySet().stream()
                    .filter(user-> !user.equals(username))
                    .collect(Collectors.toList());
            }
            synchronized(blockers) {
                return connections.keySet().stream()
                    .filter(user-> 
                        !user.equals(username) && 
                        !blockers.contains(user))
                    .collect(Collectors.toList());
            }
        }

        public void recordLoginHistory() {
            history.put(username, LocalDateTime.now());
        }

        public List<String> getHistorySince(int seconds) {
            // TODO:
            LocalDateTime since = LocalDateTime.now().minusSeconds(seconds);
            Set<String> blockers = blackLists.get(username);
            if (blockers == null) {
                return history.entrySet().stream()
                    .filter(entry-> 
                        !entry.getKey().equals(username) && 
                        since.isBefore(entry.getValue()))
                    .map(entry->entry.getKey())
                    .collect(Collectors.toList());
            }
            synchronized(blockers) {
                return history.entrySet().stream()
                    .filter(entry-> 
                        !entry.getKey().equals(username) && 
                        !blockers.contains(entry.getKey()) && 
                        since.isBefore(entry.getValue()))
                    .map(entry->entry.getKey())
                    .collect(Collectors.toList());
            }
        }

        public boolean blacklist(String blacklistUsername) {
            if (!credentials.containsKey(blacklistUsername)) {
                return false;
            }
            if (blacklistUsername.equals(username)) {
                return false;
            }
            synchronized(blackLists) {
                Set<String> blockers = blackLists.get(blacklistUsername);

                if (blockers == null) {
                    Set<String> blockerSet = ConcurrentHashMap.newKeySet();
                    blockerSet.add(username);
                    blackLists.put(blacklistUsername, blockerSet);
                } else {
                    synchronized(blockers) {
                        blockers.add(username);
                    }
                }
            }
            return true;
        }

        public boolean unblacklist(String unblacklistUsername) {
            Boolean success;
            Set<String> blockers = blackLists.get(unblacklistUsername);
            if (blockers == null) {
                return false;
            }

            synchronized(blockers) {
                success = blockers.remove(username);
            }

            if (success) {
                synchronized(blackLists) {
                    blockers = blackLists.get(unblacklistUsername);
                    synchronized(blockers) {
                    if (blockers.size() == 0) {
                            blackLists.remove(unblacklistUsername);
                        }
                    }
                }
            }
            return success;
        }

        public Set<String> getBlockers() {
            return blackLists.get(username);
        }

        public List<ClientThread> getAllUnblacklistedConnections() {
            Set<String> blockers = blackLists.get(username);
            if (blockers == null) {
                return connections.entrySet().stream()
                .filter(connection->!connection.getKey().equals(username))
                .map(connection->connection.getValue())
                .collect(Collectors.toList());
            }
            synchronized(blockers) {
                return connections.entrySet().stream()
                    .filter(connection->
                        !connection.getKey().equals(username) &&
                        !blockers.contains(connection.getKey()))
                    .map(connection->connection.getValue())
                    .collect(Collectors.toList());
            }
        }

        public boolean isBlacklisted(String receiverUsername) {
            Set<String> blockers = blackLists.get(username);
            if (blockers == null) return false;
            if (blockers.contains(receiverUsername)) {
                return true;
            }
            return false;
        }

        public void writeConnectedOrWriteUnread(String username, String message1, String message2) {
            synchronized(connections) {
                if (connections.containsKey(username)) {
                    connections.get(username).writeToClient(message1);
                } else {
                    // write to unread
                    writeToUnreadMessages(username, message2);
                }
            }
        }

        public void writeToUnreadMessages(String username, String message) {
            synchronized(unreadMessages) {
                List<String> unreadMessagesOfTheUser = unreadMessages.get(username);
                if (unreadMessagesOfTheUser == null) {
                    unreadMessages.put(username, new ArrayList<>(Arrays.asList(message)));
                } else {
                    synchronized(unreadMessagesOfTheUser) {
                        unreadMessagesOfTheUser.add(message);
                    }
                }
            }
        }

        synchronized public String getPrivatePermissionPeerUsername() {
            return privatePermissionPeerUsername;
        }
    
        synchronized public void setPrivatePermissionPeerUsername(String privatePermissionPeerUsername) {
            this.privatePermissionPeerUsername = privatePermissionPeerUsername;
        }

        @Override
        public void run() {
            super.run();
            // get input and output stream
            DataInputStream dataInputStream = null;
            DataOutputStream dataOutputStream = null;
            try {
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
            } catch (Exception e) {
                try {
                    if (dataInputStream != null) {
                        dataInputStream.close();
                    }
                    if (dataOutputStream != null) {
                        dataOutputStream.close();
                    }
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                    return;
                }
                // e.printStackTrace();
                return;
            }

            // get reader and writer
            inFromClient = new BufferedReader(new InputStreamReader(dataInputStream));
            outToClient = new BufferedWriter(new OutputStreamWriter(dataOutputStream));

            state = new GetUsernameState(this);

            String message;
            while (isConnecting) {
                // set timeout
                try {
                    clientSocket.setSoTimeout(timeout);
                } catch (SocketException e) {
                    // e.printStackTrace();
                    break;
                }

                try {
                    // server only read from client
                    message = inFromClient.readLine();
                } catch (SocketTimeoutException e) {
                    writeToClient("timeout\n");
                    // send privatedecline to the other user if this users timout during asking for private permission
                    if (this.state instanceof ServerNormalState) {
                        String peerUsername = getPrivatePermissionPeerUsername();
                        if (peerUsername != null) {
                            // send privatedecline to the peer client if the peer client is online
                            ClientThread otherThread = getConnections().get(peerUsername);
                            if (otherThread != null) {
                                otherThread.writeToClient("privatedecline " + this.username + " offline\n");
                            }
                        }
                    }
                    break;
                } catch (IOException e) {
                    // e.printStackTrace();
                    break;
                }

                if (message != null) {
                    System.out.println(message);
                    // every message from the client is sent to the state to
                    // decide what to do next
                    state.receiveMessage(message);
                } else {
                    // the client close the connection
                    break;
                }
            }

            // remove from connections and add to unreadMessages
            synchronized(connections) {
                if (username != null && connections.containsKey(username)) {
                    connections.remove(username);
                    // broadcast presence: username log out
                    for (ClientThread otherThread: getAllUnblacklistedConnections()) {
                        otherThread.writeToClient("presence " + username + " log out\n");
                    }
                }
            }

            synchronized(unreadMessages) {
                if (username != null && !unreadMessages.containsKey(username)) {
                    unreadMessages.put(username, new ArrayList<String>());
                }
            }

            try {
                inFromClient.close();
                outToClient.close();
                clientSocket.close();
            } catch (Exception e) {
                // e.printStackTrace();
                return;
            }

        }
    }
}
