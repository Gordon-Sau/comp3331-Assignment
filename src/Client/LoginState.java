package src.Client;

import java.io.IOException;

public class LoginState extends ClientState {
    public LoginState(Client.ServerCommunication serverThread) {
        super(serverThread);
    }

    @Override
    public void receiveMessage(String message) throws IOException{
        if (message.equals("password?")) {
            System.out.print("password: ");
            String send = client.cmdReader.readLine();
            client.outToServer.write("password " + send + "\n");
            client.outToServer.flush();
        } else if (message.equals("newuser")) {
            System.out.print("You are a new user.\ncreate a new password: ");
            String send = client.cmdReader.readLine();
            System.out.println("send new password");
            client.outToServer.write("newpassword " + send + "\n");
            client.outToServer.flush();
        } else if (message.equals("wrongpassword")) {
            System.out.println("invalid password");
            String send = client.cmdReader.readLine();
            client.outToServer.write("password " + send + "\n");
            client.outToServer.flush();;
        } else if (message.equals("welcome")) {
            client.setState(new NormalState(client));
        } else if (message.equals("block")) {
            System.out.println("you are blocked");
            System.out.println("exit program");
            System.exit(1);
        }
    }
}
