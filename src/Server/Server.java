package src.Server;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private static Map<String, Set<String>> blackLists = new ConcurrentHashMap<>();

    // if online and have read the message(after valid login), remove the user from the map
    // if become offline, add back with an empty list as the value
    // NOTE: can also be used to check if one is offline
    private static Map<String, List<String>> unreadMessages = new ConcurrentHashMap<>();

    // TODO: history (stores login time for each users when they have logged in successfully(correct password))

    public static void main(String[] args) throws IOException{
        if (args.length != 3) {
            System.out.println("===== Error usage: java Server SERVER_PORT BLOCK_DURATION TIMEOUT =====");
        }

        // store credentials
        Scanner myReader = new Scanner(new File(".", "credentials.txt"));
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] credentialTuple = data.split(" ");
            credentials.put(credentialTuple[0], credentialTuple[1]);
        }
        myReader.close();

        credentialsWriter = new FileWriter(new File(".", "credentials.txt"), true);

        // create welcomeSocket
        serverPort = Integer.parseInt(args[0]);
        blockDuration = Integer.parseInt(args[1]);
        timeout = Integer.parseInt(args[2]);
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
            this.state = state;
        }

        public void appendCredential(String str) {
            synchronized(credentialsWriter) {
                try {
                    credentialsWriter.append(str);
                    credentialsWriter.flush();
                } catch (IOException e) {
                    System.out.println("fail to write to credentials.txt");
                    e.printStackTrace();
                }
            }
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
                e.printStackTrace();
                return;
            }

            // get reader and writer
            inFromClient = new BufferedReader(new InputStreamReader(dataInputStream));
            outToClient = new BufferedWriter(new OutputStreamWriter(dataOutputStream));

            state = new GetUsernameState(this);

            String message;
            while (true) {
                try {
                    // server only read from client
                    message = inFromClient.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (message != null) {
                    // every message from the client is sent to the state to
                    // decide what to do next
                    state.receiveMessage(message);
                    // TODO: return boolean to indicate we should close the connection
                } else {
                    // the client close the connection
                    break;
                }
            }

            try {
                inFromClient.close();
                outToClient.close();
                clientSocket.close();
                // TODO: remove from connections and add to unreadMessages
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        }
    }
}
