package src.Client;

import java.io.IOException;

public abstract class ClientState {
    protected Client.ServerCommunication client;
    public ClientState(Client.ServerCommunication client) {
        this.client = client;
    }

    abstract public void receiveMessage(String message) throws IOException;
}
