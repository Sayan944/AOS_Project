package network;

import common.Config;
import common.Message;

import java.io.PrintWriter;
import java.net.Socket;

public class Client {

    public static void sendMessage(int receiverId, Message message) {

        try {
            Socket socket = new Socket("localhost", Config.getPort(receiverId));

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(message.toString());

            System.out.println("Sent -> University " + receiverId + " : " + message);

            out.close();
            socket.close();

        } catch (Exception e) {
            System.out.println("Unable to send message to University "
                    + receiverId);
        }
    }
}