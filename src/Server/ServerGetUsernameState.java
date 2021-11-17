package src.Server;

import java.time.LocalDateTime;

public class ServerGetUsernameState extends ServerState {
    public ServerGetUsernameState(Server.ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        String[] splitMsg = message.split(" ");
        if (splitMsg[0].equals("username")) {
            clientThread.username = splitMsg[1];

            // check credentials to see if the user is new
            if (clientThread.getCredentials().containsKey(splitMsg[1])) {

                // check if the user is blocked and remove if the time has been passed
                // if blocked send "block\n" and close the connections
                if (clientThread.getBlockedLogins().containsKey(clientThread.username)) {
                    if (clientThread.getBlockedLogins().get(clientThread.username).isBefore(LocalDateTime.now())) {
                        clientThread.getBlockedLogins().remove(clientThread.username);
                    } else {
                        // send "block\n" and disconnect
                        if (!clientThread.writeToClient("block\n") ) {
                            return;
                        }
                        clientThread.disconnect();
                        return;
                    }
                }

                // check whether the user has already logged in (using connections)
                if (clientThread.getConnections().containsKey(clientThread.username)) {
                    clientThread.username = null;
                    if (!clientThread.writeToClient("loggedin\n") ) {
                        return;
                    }
                    return;
                }

                // asks for the password
                if (!clientThread.writeToClient("password?\n")) {
                    return;
                }
                clientThread.setState(new ServerAuthentication(clientThread));

            } else {
                if (!clientThread.writeToClient("newuser\n") ) {
                    return;
                }
                clientThread.setState(new ServerNewUser(clientThread));
            }
        }
    }
}
