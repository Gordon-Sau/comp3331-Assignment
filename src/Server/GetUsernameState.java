package src.Server;

public class GetUsernameState extends ServerState {
    public GetUsernameState(Server.ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        String[] splitMsg = message.split(" ");
        if (splitMsg[0].equals("username")) {
            clientThread.username = splitMsg[1];

            // TODO: check blocked and remove if the time has been passed
            // if blocked send "block\n" and close the connections
            if (clientThread.getBlockedLogins().containsKey(splitMsg[1])) {
            }

            // TODO: check whether the user has already logged in (using connections)

            if (clientThread.getCredentials().containsKey(splitMsg[1])) {
                try {
                    clientThread.outToClient.write("password?\n");
                    clientThread.outToClient.flush();
                    clientThread.setState(new Authentication(clientThread));
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            } else {
                try {
                    clientThread.setState(new NewUser(clientThread));
                    clientThread.outToClient.write("newuser\n");
                    clientThread.outToClient.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }
}
