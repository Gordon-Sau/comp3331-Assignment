package src.Client;


public class StartState extends ClientState {
    public StartState(Client.ClientObj client) {
        super(client);
    }

    @Override
    synchronized public void receiveCmd(String message) {
        client.writeToServerNoExcept("username " + message + "\n");
        client.setState(new LoginState(client));
    }

}
