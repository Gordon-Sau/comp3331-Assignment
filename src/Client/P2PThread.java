package src.Client;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketException;

public class P2PThread extends Thread {
    public String peerUsername;
    public BufferedReader inFromPeer;
    public BufferedWriter outToPeer;
    public Socket socket;
    public Client.ClientObj client;
    private boolean isStopped = false;

    public P2PThread(Client.ClientObj client, String peerUsername, Socket socket) throws IOException {
        this.client = client;
        this.peerUsername = peerUsername;
        this.socket = socket;
        inFromPeer = new BufferedReader(new InputStreamReader(new DataInputStream(socket.getInputStream())));
        outToPeer = new BufferedWriter(new OutputStreamWriter(new DataOutputStream(socket.getOutputStream())));
    }

    public void run() {
        super.run();
        boolean isConnected = true;

        while (isConnected) {
            String message = null;
            try {
                message = inFromPeer.readLine();
            } catch (IOException e) {
                if (!isStopped()) {
                    e.printStackTrace();
                }
                System.out.println("closing the private connection with " + peerUsername);
                if (!socket.isClosed()) {
                    try {
                        socket.close();
                    } catch (IOException ioE) {
                        ioE.printStackTrace();
                        System.out.println("cannot close the socket");
                        System.out.println("exit program");
                        System.exit(1);
                    }
                }
                return;
            }

            if (message != null) {
                isConnected = receivePeerMsg(message);
            } else {
                System.out.println("lost connection to the peer " + peerUsername);
                System.out.println("exit program");
                System.exit(1);
            }
        }
    }

    public boolean receivePeerMsg(String message) {
        // two kinds: private or stopprivate
        String[] splitMessage = message.split(" ", 2);
        if (splitMessage[0].equals("private")) {
            // received message, print the message
            System.out.println("private: " + peerUsername + ": " + splitMessage[1]);
            return true;
        } else if (message.equals("stopprivate")) {
            // close the socket and remove the thread from the p2pconnections map
            // print the connection is closed
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Error occured. Exiting program");
                System.exit(1);
            }
            client.p2pConnections.remove(peerUsername);
            System.out.println("the private connection with " + peerUsername + " is stopped");
            return false;
        }
        return true;
    }

    synchronized public void setStopped(boolean isStopped) {
        this.isStopped = isStopped;
    }

    synchronized public boolean isStopped() {
        return isStopped;
    }

    synchronized public void writeToPeer(String message) throws IOException {
        outToPeer.write(message);
        outToPeer.flush();
    }

    public void writeToPeerNoExcept(String message) {
        try {
            writeToPeer(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
