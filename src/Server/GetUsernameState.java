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
