package src.Client;

public class PrivatePermissionState extends ClientNormalState {
    public PrivatePermissionState(Client.ClientObj client) {
        super(client);
    }

    @Override
    synchronized public void receiveCmd(String message) {
        //TODO
        if (message.equals("y")) {
            // send privateaccept
        } else if (message.equals("n")) {
            // send privatedecline
        } else {
            // ask the user to input again
        }
    }
}
