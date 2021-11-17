package src.Client;


public class ClientStartState extends ClientState {
    public ClientStartState(Client.ClientObj client) {
        super(client);
    }

    @Override
    synchronized public void receiveCmd(String message) {
        client.writeToServerNoExcept("username " + message + "\n");
        client.setState(new ClientLoginState(client));
    }

}
