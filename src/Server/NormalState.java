package src.Server;

import src.Server.Server.ClientThread;

public class NormalState extends ServerState {
    public NormalState(ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        synchronized(clientThread) {
            System.out.println(clientThread.username + ' ' + message);
        }
    }
}
