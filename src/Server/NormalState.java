package src.Server;

import src.Server.Server.ClientThread;

public class NormalState extends ServerState {
    public NormalState(ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        System.out.println(message);
    }
}
