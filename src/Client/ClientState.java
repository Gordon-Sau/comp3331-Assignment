package src.Client;

public abstract class ClientState {
    protected Client.ClientObj client;
    public ClientState(Client.ClientObj client) {
        this.client = client;
    }

    abstract public void receiveCmd(String message);

    synchronized public void receiveServer(String message) {
        if (message.equals("timeout")) {
            System.out.println("timeout");
            System.out.println("exit program");
            System.exit(0);
        }
    }
}
