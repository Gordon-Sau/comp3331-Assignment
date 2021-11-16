package src.Client;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;

public class ClientNormalState extends ClientState{
    public ClientNormalState(Client.ClientObj client) {
        super(client);
    }
    
    @Override
    synchronized public void receiveCmd(String message) {
        String[] splitMsg = message.split(" ");
        if (splitMsg.length >= 3 && splitMsg[0].equals("message")) {
            client.writeToServerNoExcept(message + '\n');
        } else if (splitMsg.length >= 2 && splitMsg[0].equals("broadcast")) {
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
            // tell all p2p peers to close the connection and close all p2p connections as well
            for (Map.Entry<String, P2PThread> peerThreadEntry: new ArrayList<>(client.p2pConnections.entrySet())) {
                // send stopprivate to the peers
                peerThreadEntry.getValue().writeToPeerNoExcept("stopprivate\n");
                client.p2pConnections.remove(peerThreadEntry.getKey());
            }
            client.writeToServerNoExcept("logout\n");

            System.out.println("log out");
            System.out.println("exit program");
            System.exit(0);
            // exit will close all the thread and free all the resources
        } else if (splitMsg.length == 2 && splitMsg[0].equals("startprivate")) {
            if (client.p2pConnections.containsKey(splitMsg[1])) {
                System.out.println("You have already connected to " + splitMsg[1]);
                return;
            }
            client.writeToServerNoExcept("startprivate " + splitMsg[1] + '\n');
        } else if (splitMsg.length >= 3 && splitMsg[0].equals("private")) {
            String privateMessage = message.split(" ", 3)[2];
            P2PThread peerThread = client.p2pConnections.get(splitMsg[1]);
            if (peerThread == null) {
                // error message
                System.out.println("you cannot send message to " + splitMsg[1] + " as you have not startprivate");
            } else {
                peerThread.writeToPeerNoExcept("private " + privateMessage + '\n');
            }

            // send a probe to the server
            client.writeToServerNoExcept("private\n");
        } else if (splitMsg.length == 2 && splitMsg[0].equals("stopprivate")) {
            // send a probe to the server
            client.writeToServerNoExcept("stopprivate\n");
            // search the thread and close the socket
            P2PThread peerThread = client.p2pConnections.get(splitMsg[1]);
            if (peerThread == null) {
                // if does not have such connection
                // print error message
                System.out.println("no private connection with " + splitMsg[1]);
            } else {
                // set stopped
                peerThread.setStopped(true);
                try {
                    peerThread.writeToPeerNoExcept("stopprivate\n");
                    // when the socket is closed, an error will be thrown in the P2PThread
                    // reference: https://stackoverflow.com/a/3421617
                    peerThread.socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("error occured when closing the socket");
                }
                client.p2pConnections.remove(splitMsg[1]);
            }
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
            System.out.print("unread message: ");
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
        } else if (splitMsg[0].equals("presence")) {
            // x log in or
            // x log out
            System.out.println(message.split(" ", 2)[1]);

        } else if (splitMsg[0].equals("privateaccept")) {
            System.out.println(message); // debug
            // accept private
            // format: privateaccept username ip port
            // connect to the peer
            try {
                Socket peerSocket = new Socket(splitMsg[2], Integer.parseInt(splitMsg[3]));
                // create a new thread to receive the message from the peer
                P2PThread peerThread = new P2PThread(client, splitMsg[1], peerSocket);
                // put the thread in the p2p connections
                client.p2pConnections.put(splitMsg[1], peerThread);
                peerThread.start();
                System.out.println("connected to " + splitMsg[1]);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("fail to connect to " + splitMsg[1]);
            }
        } else if (splitMsg[0].equals("privatedecline")) {
            // decline private
            // format: privatedecline peername error
            String peerName = splitMsg[1];
            String errorStr = splitMsg[2];
            // print error message
            if (errorStr.equals("self")) {
                System.out.println("you cannot start private message to yourself");
            } else if (errorStr.equals("blocked")) {
                System.out.println(peerName + " has blocked you");
            } else if (errorStr.equals("offline")) {
                System.out.println(peerName + " is not online");
            } else if (errorStr.equals("reject")) {
                System.out.println(peerName + " decline to have private connection");
            }
        } else if (splitMsg[0].equals("askprivatepermission")) {
            // create private asking state
            client.setState(new PrivatePermissionState(client, splitMsg[1]));
            // ask user to type y/n
            System.out.println("Do you accept private connection with " + splitMsg[1] + "? (y/n)");
            // the logic is in the privateCreate state
        }

        super.receiveServer(message);
    }
}
