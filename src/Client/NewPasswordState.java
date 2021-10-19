package src.Client;

public class NewPasswordState extends ClientState {
    private LoginState loginState;
    public NewPasswordState(Client.ClientObj client, LoginState prevLoginState) {
        super(client);
        this.loginState = prevLoginState;
    }

    @Override
    synchronized public void receiveCmd(String message) {
        client.writeToServerNoExcept("newpassword " + message + "\n");
        client.setState(loginState);
    }

}
