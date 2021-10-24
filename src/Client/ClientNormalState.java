package src.Client;

public class ClientNormalState extends ClientState{
    public ClientNormalState(Client.ClientObj client) {
        super(client);
    }
    
    @Override
    synchronized public void receiveCmd(String message) {
        String[] splitMsg = message.split(" ");
        if (splitMsg.length >= 3 && splitMsg[0].equals("message")) {
            client.writeToServerNoExcept(message + '\n');
        } else if (splitMsg.length == 2 && splitMsg[0].equals("broadcast")) {
            client.writeToServerNoExcept(message + '\n');
        } else if (message.equals("whoelse")) {
            client.writeToServerNoExcept("whoelse\n");
        } else if (splitMsg.length == 2 && splitMsg[0].equals("whoelsesince")) {
            client.writeToServerNoExcept("whoelsesince " + splitMsg[1] + '\n');
        } else if (splitMsg.length == 2 && splitMsg[0].equals("block")) {
            client.writeToServerNoExcept("blacklist " + splitMsg[1] + '\n');
        } else if (splitMsg.length == 2 && splitMsg[0].equals("unblock")) {
            client.writeToServerNoExcept("unblacklist " + splitMsg[1] + '\n');
        } else if (message.equals("logout")) {
            client.writeToServerNoExcept("logout\n");
        } else if (splitMsg.length == 2 && splitMsg[0].equals("startprivate")) {
            client.writeToServerNoExcept("startprivate " + splitMsg[1] + '\n');
        } else if (splitMsg.length == 3 && splitMsg[0].equals("private")) {
            // TODO: send to the other user
            // send a probe to the server
            client.writeToServerNoExcept("private\n");
        } else if (splitMsg.length == 2 && splitMsg[0].equals("stopprivate")) {
            client.writeToServerNoExcept("stopprivate " + splitMsg[1] + '\n');
        } else {
            System.out.println("invalid operation");
        }
    }

    @Override
    public synchronized void receiveServer(String message) {
        String[] splitMsg = message.split(" ");
        if (message.equals("failmessage")) {
            System.out.println("you cannot send to this user");
        } else if (splitMsg[0].equals("failbroadcast")) {
            System.out.println("you cannot send the message to these users:");
            for (int i = 1; i < splitMsg.length; i++) {
                System.out.println(splitMsg[i]);
            }
        } else if (splitMsg[0].equals("unread")) {
            System.out.print("unread:");
            System.out.println(message.split(" ", 2)[1]);
        } else if (splitMsg[0].equals("message")) {
            System.out.print("message: ");
            System.out.println(message.split(" ", 2)[1]);
        } else if (splitMsg[0].equals("whoelse")) {
            System.out.println("These are the users currently online:");
            for (int i = 1; i < splitMsg.length; i++) {
                System.out.println(splitMsg[i]);
            }
        } else if (message.equals("whoelsesince")) {
            System.out.println("These are the users");
            for (int i = 1; i < splitMsg.length; i++) {
                System.out.println(splitMsg[i]);
            }
        } else if (message.equals("successblock")) {
            System.out.println("block successfully");
        } else if (message.equals("failblock")) {
            System.out.println("You cannot block this user");
        } else if (message.equals("successunblock")) {
            System.out.println("unblock successfully");
        } else if (message.equals("failunblock")) {
            System.out.println("cannot unblock the user");
        } else if (message.equals("successlogout")) {
            System.out.println("log out");
            System.out.println("exit program");
            System.exit(0);
        } else if (splitMsg[0].equals("presence")) {
            // x log in or
            // x log out
            System.out.println(message.split(" ", 2)[1]);
        }
        // TODO: p2p

        super.receiveServer(message);
    }
}
