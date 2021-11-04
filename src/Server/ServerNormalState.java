package src.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import src.Server.Server.ClientThread;

public class ServerNormalState extends ServerState {
    public ServerNormalState(ClientThread clientThread) {
        super(clientThread);
    }

    @Override
    public void receiveMessage(String message) {
        System.out.println(clientThread.username + ' ' + message);

        String[] splitMsg = message.split(" ");
        if (splitMsg.length >= 3 && splitMsg[0].equals("message")) {
            String otherUsername = splitMsg[1];
            if (clientThread.username.equals(otherUsername) || 
                !clientThread.getCredentials().containsKey(otherUsername)
                || clientThread.isBlacklisted(otherUsername)
            ) {
                clientThread.writeToClient("failmessage\n");
            } else {
                String sendMessage = clientThread.username + ": " + message.split(" ", 3)[2];
                clientThread.writeConnectedOrWriteUnread(otherUsername, "message " + sendMessage + '\n', sendMessage);
                // no failmessage means success
            }
            
        } else if (splitMsg.length >= 2 && splitMsg[0].equals("broadcast")) {
            Set<String> blockers = clientThread.getBlockers();
            if (blockers == null) {
                for (Map.Entry<String, ClientThread> entry: clientThread.getConnections().entrySet()) {
                    // don't send back to the client
                    if (clientThread.username.equals(entry.getKey())) {
                        continue;
                    }

                    String sendMessage = clientThread.username + ": " + message.split(" ", 2)[1];
                    entry.getValue().writeToClient("message " + sendMessage + '\n');
                }
            } else {
                List<String> failUsers = new ArrayList<>();
                synchronized(blockers) {
                    for (Map.Entry<String, ClientThread> entry: clientThread.getConnections().entrySet()) {
                        // don't send back to the client
                        if (clientThread.username.equals(entry.getKey())) {
                            continue;
                        }
                        if (!blockers.contains(entry.getKey())) {
                            // send broadcast
                            // same as sending message
                            String sendMessage = clientThread.username + ": " + message.split(" ", 2)[1];
                            entry.getValue().writeToClient("message " + sendMessage + '\n');
                        } else {
                            failUsers.add(entry.getKey());
                        }
                    }
                }
                if (failUsers.size() > 0) {
                    // send all the failUsers back to the client
                    clientThread.writeToClient("failbroadcast " + String.join(" ", failUsers) + '\n');
                }
            }
        } else if (message.equals("whoelse")) {
            List<String> onlineUsernames = clientThread.getOnlineUsers();
            clientThread.writeToClient("whoelse " + String.join(" ", onlineUsernames) + '\n');
        } else if (splitMsg.length == 2 && splitMsg[0].equals("whoelsesince")) {
            List<String> usernames = clientThread.getHistorySince(Integer.parseInt(splitMsg[1]));
            clientThread.writeToClient("whoelsesince " + String.join(" ", usernames) + '\n');
        } else if (splitMsg.length == 2 && splitMsg[0].equals("blacklist")) {
            if (clientThread.blacklist(splitMsg[1])) {
                clientThread.writeToClient("successblock\n");
            } else {
                clientThread.writeToClient("failblock\n");
            }
        } else if (splitMsg.length == 2 && splitMsg[0].equals("unblacklist")) {
            if (clientThread.unblacklist(splitMsg[1])) {
                clientThread.writeToClient("successunblock\n");
            } else {
                clientThread.writeToClient("failunblock\n");
            }
        } else if (message.equals("logout")) {
            clientThread.disconnect();
            return;
        } else if (splitMsg.length == 2 && splitMsg[0].equals("startprivate")) {
            String otherUsername = splitMsg[1];
            // check if the other user blocked this user
            // if no ask if the other user accept the request
        } else if (message.equals("private")) {
            // do nothing, just a probe to reset timer
        } else if (splitMsg.length == 2 && splitMsg[0].equals("stopprivate")) {
            // do nothing here, just a probe to reset timer
            // should be handled by the peer
            // tell the peer to close the connection
        } else if (splitMsg.length == 2 && splitMsg[0].equals("privatedecline")) {
            // format: privatedecline otherusername
            // get the writer to the other user
            // send that this user decline
        } else if (splitMsg.length == 4 && splitMsg[0].equals("privateaccept")) {
            // format: privateaccept otherusername ip port
            // get the writer to the other user
            // send that this user accept and give the ip and port to the other user
        }
    }
}
