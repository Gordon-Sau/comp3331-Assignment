package src.Client;


import java.net.*;

import java.io.*;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("===== Error usage: java Client SERVER_PORT =====");
            return;
        }

        int serverPort = Integer.parseInt(args[0]);
        Socket serverConnection = new Socket("127.0.0.1", serverPort);
        ServerCommunication serverCommunication = new ServerCommunication(serverConnection);
        serverCommunication.start();
    }

    public static class ServerCommunication {
        public final Socket serverConnection;
        public BufferedReader inFromServer;
        public BufferedWriter outToServer;
        public BufferedReader cmdReader;
        private ClientState state;

        public ServerCommunication(Socket serverConnection) {
            this.serverConnection = serverConnection;
        }

        public void start() {
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

            // send username
            System.out.print("username: ");
            String username;
            try {
                username = cmdReader.readLine();
                outToServer.write("username " + username + "\n");
                outToServer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            state = new LoginState(this);

            // TODO: can read from both command line and server
            // need to rely on the state to choose the message (using select)
            String message;
            while (true) {
                try {
                    message = inFromServer.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }

                if (message != null) {
                    try {
                        state.receiveMessage(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }
                } else {
                    break;
                }
            }

            // TODO: may not know what should be closed 
            // rely on the stat to close
            try {
                dataInputStream.close();
                dataOutputStream.close();
                serverConnection.close();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        public void setState(ClientState state) {
            this.state = state;
        }

    }
}
