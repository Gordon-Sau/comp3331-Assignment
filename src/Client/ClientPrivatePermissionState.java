package src.Client;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientPrivatePermissionState extends ClientNormalState {
    private String otherUsername;
    public ClientPrivatePermissionState(Client.ClientObj client, String otherUsername) {
        super(client);
        this.otherUsername = otherUsername;
    }

    @Override
    synchronized public void receiveCmd(String message) {
        if (message.equals("y")) {
            ServerSocket serverSocket;
            ClientP2PThread p2pThread;
            try {
                // open socket
                serverSocket = new ServerSocket(0);
                int socketPort = serverSocket.getLocalPort();
                // send privateaccept
                client.writeToServerNoExcept("privateaccept " + otherUsername + ' ' + socketPort + '\n');
                // wait for the connection from the peer
                Socket peerSocket = serverSocket.accept();
                // start a thread that receive private message and store it in the p2pconnections map
                p2pThread = new ClientP2PThread(client, otherUsername, peerSocket);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error occured. Cannot create private connection with " + otherUsername);
                client.setState(new ClientNormalState(client));
                return;
            }
            System.out.println("connected to " + otherUsername);
            p2pThread.start();
            client.p2pConnections.put(otherUsername, p2pThread);

            // close the seversocket
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                // do nothing
            }
            client.setState(new ClientNormalState(client));
        } else if (message.equals("n")) {
            // send privatedecline
            client.writeToServerNoExcept("privatedecline " + otherUsername + '\n');
            client.setState(new ClientNormalState(client));
        } else {
            // ask the user to input again
            System.out.println("please input again. (y/n)");
        }
    }
}
