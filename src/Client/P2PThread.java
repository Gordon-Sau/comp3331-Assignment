package src.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class P2PThread extends Thread {
    public String peerUsername;
    public BufferedReader inFromPeer;
    public BufferedWriter outToPeer;
    public Socket socket;
    public Client.ClientObj client;

    public P2PThread(Client.ClientObj client, String peerUsername, Socket socket) throws IOException {
        this.client = client;
        this.peerUsername = peerUsername;
        this.socket = socket;
        inFromPeer = new BufferedReader(new InputStreamReader(new DataInputStream(socket.getInputStream())));
        outToPeer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(socket.getOutputStream())));
    }

    public void run() {
        super.run();

        while (true) {
            String message = null;
            try {
                message = inFromPeer.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("cannot read from peer " + peerUsername);
                System.out.println("close the connection");
                try {
                    socket.close();
                } catch (IOException ioE) {
                    ioE.printStackTrace();
                    System.out.println("cannot close the socket");
                    System.out.println("exit program");
                    System.exit(1);
                }
                return;
            }

            if (message != null) {
                receivePeerMsg(message);
            } else {
                System.out.println("lost connection to the peer " + peerUsername);
                System.out.println("exit program");
                System.exit(1);
            }
        }
    }

    public void receivePeerMsg(String message) {
        // TODO
    }

}
