package src.Client;

public class ClientNewPasswordState extends ClientState {
    private ClientLoginState loginState;
    public ClientNewPasswordState(Client.ClientObj client, ClientLoginState prevLoginState) {
        super(client);
        this.loginState = prevLoginState;
    }

    @Override
    synchronized public void receiveCmd(String message) {
        client.writeToServerNoExcept("newpassword " + message + "\n");
        client.setState(loginState);
    }

}
