package src.Server;

import src.Server.Server.ClientThread;

public class NewUser extends ServerState {
    public NewUser(Server.ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        String[] splitMsg = message.split(" ");
        if (splitMsg[0].equals("newpassword")) {
            clientThread.getCredentials().put(clientThread.username, splitMsg[1]);
            clientThread.appendCredential(clientThread.username + " " + splitMsg[1] + "\n");
            System.out.println("new user");

            // update connections
            clientThread.getConnections().put(clientThread.username, clientThread);

            // TODO: broadcast presence if not blocked
            // System.out.println(clientThread.username + "logged in");
            for (ClientThread otherThread: clientThread.getAllUnblacklistedConnections()) {
                otherThread.writeToClient("presence " + clientThread.username + " log in\n");
            }

            if (!clientThread.writeToClient("welcome\n") ) {
                return;
            }

            clientThread.setState(new ServerNormalState(clientThread));
        }
    }
}
