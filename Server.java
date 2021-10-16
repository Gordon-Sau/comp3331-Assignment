

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import src.Server.ClientThread;

public class Server {
    public static void main(String[] args) throws IOException{
        if (args.length != 3) {
            System.out.println("===== Error usage: java Server SERVER_PORT BLOCK_DURATION TIMEOUT =====");
        }
        Map<String, String> credentials = new ConcurrentHashMap<>();
        Scanner myReader = new Scanner(new File(".", "credentials.txt"));
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] credentialTuple = data.split(" ");
            credentials.put(credentialTuple[0], credentialTuple[1]);
        }
        myReader.close();

        int serverPort = Integer.parseInt(args[0]);
        int blockDuration = Integer.parseInt(args[1]);
        int timeout = Integer.parseInt(args[2]);
        ServerSocket welcomeSocket = new ServerSocket(serverPort);
        System.out.println("server is running");

        while (true) {
            Socket clientSocket = welcomeSocket.accept();
            ClientThread clientThread = new ClientThread(clientSocket, credentials);
            clientThread.start();
        }

    }
}
