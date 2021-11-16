package src.Server;

import java.net.InetSocketAddress;
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
        System.out.println(clientThread.username + ' ' + message); // debug

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
            // check if is the user itself
            if (clientThread.username.equals(otherUsername)) {
                clientThread.writeToClient("privatedecline " + otherUsername + " self\n");
                return;
            }
            
            // check if the other user blocked this user
            if (clientThread.isBlacklisted(otherUsername)) {
                // if blocked, send privatedecline directly and return
                clientThread.writeToClient("privatedecline " + otherUsername + " blocked\n");
                return;
            }

            // check if the other user is online
            ClientThread otherThread = clientThread.getConnections().get(otherUsername);
            if (otherThread == null) {
                // if not online, sed privatedecline directly and return
                clientThread.writeToClient("privatedecline " + otherUsername + " offline\n");
                return;
            } else {
                // ask if the other user accept the request
                otherThread.writeToClient("askprivatepermission " + clientThread.username + '\n');
                // set the privatePermissionPeerName of the other user
                otherThread.setPrivatePermissionPeerUsername(clientThread.username);
            }
            
        } else if (message.equals("private")) {
            // do nothing, just a probe to reset timer
        } else if (splitMsg[0].equals("stopprivate")) {
            // do nothing here, just a probe to reset timer
            // should be handled by the peer
            // tell the peer to close the connection
        } else if (splitMsg.length == 2 && splitMsg[0].equals("privatedecline")) {
            // format: privatedecline otherusername
            String otherUsername = splitMsg[1];
            // get the writer to the other user
            if (otherUsername != null) {
                // send privatedecline to the peer client if the peer client is online
                ClientThread otherThread = clientThread.getConnections().get(otherUsername);
                if (otherThread != null) {
                    otherThread.writeToClient("privatedecline " + clientThread.username + " reject\n");
                }
            }
            // send that this user decline
            clientThread.setPrivatePermissionPeerUsername(null);
        } else if (splitMsg.length == 3 && splitMsg[0].equals("privateaccept")) {
            // format: privateaccept otherusername port
            String otherUsername = splitMsg[1];
            // we can get the ip from this socket
            // https://www.baeldung.com/java-client-get-ip-address
            String forwardIP = ((InetSocketAddress)clientThread.getSocket().getRemoteSocketAddress())
                .getAddress().getHostAddress();
            // NOTE: the port of this socket and the port in the message are different
            // send the port in the message
            String forwardPort = splitMsg[2];

            // get the writer to the other user
            // send that this user accept and give the ip and port to the other user
            ClientThread otherThread = clientThread.getConnections().get(otherUsername);
            if (otherThread != null) {
                otherThread.writeToClient(String.format("privateaccept %s %s %s\n", clientThread.username, forwardIP, forwardPort));
            }

            // set back the privatePermissionPeerUsername to null to indicate 
            // that the client is not in the PrivatePermsion state
            clientThread.setPrivatePermissionPeerUsername(null);
        }
    }


}
