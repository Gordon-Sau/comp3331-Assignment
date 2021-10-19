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
                // TODO: broadcast precense
                System.out.println(clientThread.username + "logged in");
                clientThread.setState(new NormalState(clientThread));
            } else {
                if (numWrongLogin < 2) {
                    try {
                        clientThread.outToClient.write("wrongpassword\n");
                        clientThread.outToClient.flush();
                        numWrongLogin++;
                        // stay in this state
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                } else {
                    try {
                        clientThread.outToClient.write("block\n");
                        clientThread.outToClient.flush();

                        // TODO: block the client for BLOCK_DURATION seconds
                        clientThread.blockUser();
                        // quit the client
                        clientThread.disconnect();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
        }
    }
}