package src.Server;

import java.io.IOException;

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
            try {
                clientThread.outToClient.write("welcome\n");
                clientThread.outToClient.flush();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
