package src.Client;

import java.io.IOException;

public class NormalState extends ClientState{
    public NormalState(Client.ServerCommunication client) {
        super(client);
    }
    @Override
    public void receiveMessage(String message) throws IOException {
        client.outToServer.write(message);
        client.outToServer.flush();
    }
    
}
