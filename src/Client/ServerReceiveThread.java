package src.Client;

import java.io.IOException;

public class ServerReceiveThread extends Thread {
    Client.ClientObj client;
    public ServerReceiveThread(Client.ClientObj client) {
        this.client = client;
    }
    
    @Override
    public void run() {
        super.run();

        while (true) {
            String message = null;
            try {
                message = client.inFromServer.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("cannot read from server");
                System.out.println("exit program");
                System.exit(1);
            }

            if (message != null) {
                synchronized(client) {
                    client.state.receiveServer(message);
                }
            } else {
                System.out.println("timeout");
                System.out.println("lost connection");
                break;
            }
        }
    }
}
