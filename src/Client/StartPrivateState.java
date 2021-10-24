package src.Client;

public class StartPrivateState extends ClientNormalState {
    public StartPrivateState(Client.ClientObj client) {
        super(client);
    }

    @Override
    synchronized public void receiveCmd(String message) {
        // TODO Auto-generated method stub
        super.receiveCmd(message);
    }
}
