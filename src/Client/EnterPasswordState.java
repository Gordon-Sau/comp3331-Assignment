package src.Client;

public class EnterPasswordState extends ClientState {
    private LoginState loginState;
    public EnterPasswordState(Client.ClientObj client, LoginState prevLoginState) {
        super(client);
        this.loginState = prevLoginState;
    }

    @Override
    synchronized public void receiveCmd(String message) {
        client.writeToServerNoExcept("password " + message + "\n");
        client.setState(loginState);
    }
}
