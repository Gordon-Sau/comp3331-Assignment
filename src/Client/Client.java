package src.Client;


import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.*;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("===== Error usage: java Client SERVER_PORT =====");
            return;
        }

        int serverPort = Integer.parseInt(args[0]);
        Socket serverConnection = new Socket("127.0.0.1", serverPort);
        ClientObj serverCommunication = new ClientObj(serverConnection);
        serverCommunication.start();
    }

    public static class ClientObj {
        public final Socket serverConnection;
        public BufferedReader inFromServer;
        public BufferedWriter outToServer;
        public BufferedReader cmdReader;
        public ClientState state;
        public Map<String, P2PThread> p2pConnections = new ConcurrentHashMap<>();

        public ClientObj(Socket serverConnection) {
            this.serverConnection = serverConnection;
        }

        public void start() throws Exception {
            DataInputStream dataInputStream;
            DataOutputStream dataOutputStream;
            try {
                dataInputStream = new DataInputStream(serverConnection.getInputStream());
                dataOutputStream = new DataOutputStream(serverConnection.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            
            inFromServer = new BufferedReader(new InputStreamReader(dataInputStream));
            outToServer = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
            cmdReader = new BufferedReader(new InputStreamReader(System.in));

            ServerReceiveThread serverReceiveThread = new ServerReceiveThread(this);
            serverReceiveThread.start();

            // this thread is the thread that handle input from command line

            state = new StartState(this);
            System.out.print("username: ");

            // read from both command line and send messages to server
            // implement like as a state machine
            String message;
            while (true) {
                message = cmdReadLineNoExcept();

                if (message != null) {
                    synchronized(this) {
                        state.receiveCmd(message);
                    }
                } else {
                    // The client don't write messages any more
                    System.out.println("exit program");
                    System.exit(0);
                }
            }

        }

        public String cmdReadLineNoExcept() {
            try {
                return cmdReader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error: cannot read from command line");
                System.out.println("exit program");
                System.exit(1);
                return null;
            }
        }

        synchronized public void setState(ClientState state) {
            // avoid changing state on two threads at the same time
            this.state = state;
        }

        public void writeToServerNoExcept(String message) {
            try {
                outToServer.write(message);
                outToServer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("lost connection");
                System.exit(1);
            }
        }

    }
    
}
