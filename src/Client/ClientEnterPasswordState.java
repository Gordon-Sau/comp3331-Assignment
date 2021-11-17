package src.Client;

public class ClientEnterPasswordState extends ClientState {
    private ClientLoginState loginState;
    public ClientEnterPasswordState(Client.ClientObj client, ClientLoginState prevLoginState) {
        super(client);
        this.loginState = prevLoginState;
    }

    @Override
    synchronized public void receiveCmd(String message) {
        client.writeToServerNoExcept("password " + message + "\n");
        client.setState(loginState);
    }
}
