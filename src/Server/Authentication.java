package src.Server;

import src.Server.Server.ClientThread;

public class Authentication extends ServerState {
    private int numWrongLogin = 0;

    public Authentication(Server.ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        String[] splitMsg = message.split(" ");
        if (splitMsg[0].equals("password") && splitMsg.length == 2) {
            if (clientThread.getConnections().containsKey(clientThread.username)) {
                clientThread.writeToClient("loggedin\n");
                clientThread.username = null;
                clientThread.setState(new GetUsernameState(clientThread));
                return;
            }
            if (splitMsg[1].equals(clientThread.getCredentials().get(clientThread.username))) {
                if (!clientThread.writeToClient("welcome\n") ) {
                    return;
                }

                // update connections
                clientThread.getConnections().put(clientThread.username, clientThread);
                clientThread.recordLoginHistory();
                // broadcast presence if not blocked
                // System.out.println(clientThread.username + "log in");
                for (ClientThread otherThread: clientThread.getAllUnblacklistedConnections()) {
                    otherThread.writeToClient("presence " + clientThread.username + " log in\n");
                }

                // send unread messages
                clientThread.sendUnreadMessages();

                clientThread.setState(new ServerNormalState(clientThread));
            } else {
                if (numWrongLogin < 2) {
                    numWrongLogin++;
                    if (!clientThread.writeToClient("wrongpassword\n")) {
                        return;
                    }
                } else {
                    if (!clientThread.writeToClient("block\n")) {
                        return;
                    }
                    // block the client for BLOCK_DURATION seconds
                    clientThread.blockUser();
                    // quit the client
                    clientThread.disconnect();
                }
            }
        }
    }
}