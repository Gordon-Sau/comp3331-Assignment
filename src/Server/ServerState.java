package src.Server;
public abstract class ServerState {
    protected Server.ClientThread clientThread;

    public ServerState(Server.ClientThread clientThread) {
        this.clientThread = clientThread;
    }

    abstract public void receiveMessage(String message);
}
