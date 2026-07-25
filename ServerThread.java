package network;

import common.Message;
import node.UniversityNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {

    private int universityId;
    private int port;

    // Reference to UniversityNode
    private UniversityNode node;

    // Tells UniversityNode when the server is ready
    private volatile boolean serverStarted = false;

    // Constructor
    public ServerThread(
            int universityId,
            int port,
            UniversityNode node) {

        this.universityId = universityId;
        this.port = port;
        this.node = node;
    }

    // Check whether server has successfully started
    public boolean isServerStarted() {

        return serverStarted;
    }

    @Override
    public void run() {

        try {

            ServerSocket serverSocket =
                    new ServerSocket(port);

            // Server is now ready
            serverStarted = true;

            System.out.println(
                    "University "
                            + universityId
                            + " is listening on Port "
                            + port
                            + "...");

            while (true) {

                Socket socket =
                        serverSocket.accept();

                BufferedReader in =
                        new BufferedReader(
                                new InputStreamReader(
                                        socket.getInputStream()));

                String received =
                        in.readLine();

                if (received != null) {

                    Message message =
                            Message.fromString(received);

                    System.out.println(
                            "\n--------------------------------");

                    System.out.println(
                            "University "
                                    + universityId
                                    + " received a message");

                    System.out.println(
                            "Type   : "
                                    + message.getType());

                    System.out.println(
                            "Sender : University "
                                    + message.getSenderId());

                    System.out.println(
                            "Data   : "
                                    + message.getData());

                    System.out.println(
                            "--------------------------------");

                    // Pass message to UniversityNode
                    node.handleMessage(message);
                }

                in.close();
                socket.close();
            }

        } catch (Exception e) {

            System.out.println(
                    "Server Error : "
                            + e.getMessage());

        }
    }
}