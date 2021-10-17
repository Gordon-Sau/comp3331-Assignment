package src.Server;

import java.io.IOException;

public class Authentication extends ServerState {
    private int numWrongLogin = 0;

    public Authentication(Server.ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        String[] splitMsg = message.split(" ");
        if (splitMsg[0].equals("password")) {
            if (splitMsg[1].equals(clientThread.getCredentials().get(clientThread.username))) {
                try {
                    clientThread.outToClient.write("welcome\n");
                    clientThread.outToClient.flush();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                if (numWrongLogin < 3) {
                    try {
                        clientThread.outToClient.write("wrongpassword\n");
                        clientThread.outToClient.flush();
                        numWrongLogin++;
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        clientThread.outToClient.write("block\n");
                        clientThread.outToClient.flush();

                        // TODO: block the client for BLOCK_DURATION seconds

                        // quit the client
                        clientThread.outToClient.close();
                        clientThread.inFromClient.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
}