import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Client {

    private final Config config;
    private final int mySiteId;

    public Client(Config config) {
        this.config = config;
        this.mySiteId = config.getSiteId();
    }

    /**
     * Sends a message to another university.
     */
    public boolean send(Message message) {

        int destination = message.getReceiverId();

        String host = config.getHost(destination);
        int port = config.getPort(destination);

        try (
                Socket socket = new Socket(host, port);
                ObjectOutputStream out =
                        new ObjectOutputStream(socket.getOutputStream())
        ) {

            out.writeObject(message);
            out.flush();

            Logger.sent(mySiteId, message);
            return true;

        } catch (IOException e) {

            Logger.log(mySiteId,
                    "FAILED TO SEND "
                            + message.getType()
                            + " TO University "
                            + destination
                            + " : "
                            + e);
            return false;
        }
    }

}/*
    it creates socket -> creates objectoutputstream , then
    send message, then closes the socket
*/