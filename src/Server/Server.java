package src.Server;


import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static int serverPort;
    private static int blockDuration;
    private static int timeout;
    private static Map<String, String> credentials = new ConcurrentHashMap<>();
    private static FileWriter credentialsWriter;
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

        public Map<String, String> getCredentials() {
            return credentials;
        }

        public void setState(ServerState state) {
            this.state = state;
        }

        public void appendCredential(String str) {
            synchronized(credentialsWriter) {
                try {
                    credentialsWriter.append(str);
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
                    message = inFromClient.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                if (message != null) {
                    state.receiveMessage(message);
                } else {
                    break;
                }
            }

            try {
                inFromClient.close();
                outToClient.close();
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

        }
    }
}
