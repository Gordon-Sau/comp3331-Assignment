package src.Server;

import java.net.*;
import java.io.*;
import java.util.Map;

public class ClientThread extends Thread {
    private final Socket clientSocket;
    private final Map<String, String> credentials;
    public ClientThread(Socket clientSocket, Map<String, String> credentials) {
        this.clientSocket = clientSocket;
        this.credentials = credentials;
    }
    @Override
    public void run() {
        super.run();
        DataInputStream dataInputStream = null;
        DataOutputStream dataOutputStream = null;
        try {
            dataInputStream = new DataInputStream(clientSocket.getInputStream());
            dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        BufferedReader inFromClient = new BufferedReader(new InputStreamReader(dataInputStream));
        BufferedWriter outToClient = new BufferedWriter(new OutputStreamWriter(dataOutputStream));

        String message;
        try {
            message = inFromClient.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String username = null;

        if (message != null) {
            String[] splitMsg = message.split(" ");
            if (splitMsg[0].equals("username")) {
                username = splitMsg[1];

                if (credentials.containsKey(splitMsg[1])) {
                    try {
                        outToClient.write("password?\n");
                        outToClient.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        outToClient.write("newuser\n");
                        outToClient.flush();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
        
        try {
            message = inFromClient.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (message != null) {
            String[] splitMsg = message.split(" ");
            if (splitMsg[0].equals("password")) {
                if (splitMsg[1].equals(credentials.get(username))) {
                    try {
                        outToClient.write("welcome\n");
                        outToClient.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        outToClient.write("wrongpassword\n");
                        outToClient.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } else if (splitMsg[1].equals("newpassword")) {
                credentials.put(username, splitMsg[1]);
                try {
                    outToClient.write("welcome\n");
                    outToClient.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                return;
            }
        }

        try {
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        System.out.println(credentials);
    }
}
