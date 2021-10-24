package src.Client;

public class LoginState extends ClientState {

    public LoginState(Client.ClientObj client) {
        super(client);
    }

    @Override
    synchronized public void receiveCmd(String message) {
        // this state does not accept any command line input
    }

    @Override
    synchronized public void receiveServer(String message) {
        if (message.equals("password?")) {
            System.out.print("password: ");
            client.setState(new EnterPasswordState(client, this));

        } else if (message.equals("newuser")) {
            System.out.print("You are a new user.\ncreate a new password: ");
            client.setState(new NewPasswordState(client, this));

        } else if (message.equals("wrongpassword")) {
            System.out.println("invalid password");
            client.setState(new EnterPasswordState(client, this));

        } else if (message.equals("welcome")) {
            System.out.println("welcome");
            client.setState(new ClientNormalState(client));

        } else if (message.equals("block")) {
            System.out.println("you are blocked");
            System.out.println("exit program");
            System.exit(0);
        } else if (message.equals("loggedin")) {
            System.out.println("This user has already logged in");
            System.out.print("username: ");
            client.setState(new StartState(client));

        }else {
            super.receiveServer(message);
        }
    }
}
