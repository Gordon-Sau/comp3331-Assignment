package src.Server;

import src.Server.Server.ClientThread;

public class ServerNormalState extends ServerState {
    public ServerNormalState(ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        synchronized(clientThread) {
            System.out.println(clientThread.username + ' ' + message);
        }
    }
}
