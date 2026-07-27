import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerThread extends Thread {
    private final UniversityNode node;
    private final int port;

    public ServerThread(UniversityNode node, int port) {
        this.node = node;
        this.port = port;
    }

    /*
     * Deliberately single-threaded: one message is accepted and fully
     * processed before the next accept() call. This guarantees messages
     * sent by the same peer in sequence (e.g. coordinator's TREE_CONFIG
     * then TOKEN_OWNER) are also processed in that order here, and it
     * avoids two RaymondAlgorithm/SnapshotManager calls racing each other
     * on separate threads. The tradeoff: a 5-second critical section will
     * delay new inbound connections from being accepted, but they queue in
     * the OS backlog rather than being dropped.
     */
    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Logger.log(node.getSiteId(),
                    "SERVER STARTED ON PORT " + port);
            while (true) {
                Socket socket = serverSocket.accept();

                try (ObjectInputStream in =
                             new ObjectInputStream(socket.getInputStream())) {
                    Message message = (Message) in.readObject();

                    Logger.received(node.getSiteId(), message);

                    node.processMessage(message);

                } catch (IOException | ClassNotFoundException e) {
                    Logger.log(node.getSiteId(),
                            "ERROR HANDLING MESSAGE : " + e.getMessage());

                } finally {

                    socket.close();
                }
            }

        } catch (IOException e) {

            Logger.log(node.getSiteId(),
                    "SERVER ERROR : " + e.getMessage());
        }
    }
}