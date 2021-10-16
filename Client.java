

import java.net.*;

import java.io.*;

public class Client {
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("===== Error usage: java Client SERVER_PORT =====");
            return;
        }
        int serverPort = Integer.parseInt(args[0]);
        Socket serverConnection = new Socket("127.0.0.1", serverPort);
        DataInputStream dataInputStream = new DataInputStream(serverConnection.getInputStream());
        DataOutputStream dataOutputStream = new DataOutputStream(serverConnection.getOutputStream());
        
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(dataInputStream));
        BufferedWriter outToServer = new BufferedWriter(new OutputStreamWriter(dataOutputStream));
        
        System.out.print("username: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        
        // send username
        String username = reader.readLine();
        System.out.println(username);
        
        outToServer.write("username " + username + "\n");
        outToServer.flush();

        String message;
        if ((message = inFromServer.readLine()) != null) {
            System.out.println(message);
            if (message.equals("password?")) {
                System.out.print("password: ");
                String send = reader.readLine();
                outToServer.write("password " + send + "\n");
                outToServer.flush();
            } else if (message.equals("newuser")) {
                System.out.print("create a password: ");
                String send = reader.readLine();
                outToServer.write("newpassword " + send + "\n");
                outToServer.flush();
            } else {
                System.out.println("no reponse");
            }
        }

        serverConnection.close();
    }
}
