package src.Client;

public class NormalState extends ClientState{
    public NormalState(Client.ClientObj client) {
        super(client);
    }
    
    @Override
    synchronized public void receiveCmd(String message) {
        System.out.println(message);
        client.writeToServerNoExcept(message + '\n');
    }
}
