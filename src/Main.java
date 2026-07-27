import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Usage:");
            System.out.println("java Main config/site4.properties");
            return;
        }

        try {

            // Load configuration
            Config config = new Config(args[0]);

            int myId = config.getSiteId();
            int totalSites = config.getTotalSites();
            int coordinatorId = config.getCoordinatorId();

            Logger.initialize(myId);

            Logger.heading("National Supercomputing Consortium");

            Logger.log(myId, "Starting University " + myId);

            // -----------------------------------------------------------
            // Wire up node / raymond / client / snapshot
            // (see UniversityNode.java for why this is two-phase)
            // -----------------------------------------------------------
            Client client = new Client(config);
            Snapshot snapshot = new Snapshot();

            UniversityNode node =
                    new UniversityNode(myId, totalSites, client, snapshot);

            RaymondAlgorithm raymond =
                    new RaymondAlgorithm(node, client);

            node.setRaymond(raymond);

            // Start server
            ServerThread server = new ServerThread(node, config.getPort());

            server.start();

            // Give server time to start
            Thread.sleep(2000);

            Logger.separator();
            Logger.log(myId, "Testing Connections...");
            Logger.separator();

            // Test connection to every other university
            for (int i = 1; i <= totalSites; i++) {

                if (i == myId)
                    continue;

                testConnection(config, myId, i);
            }

            Logger.separator();
            Logger.log(myId, "Connection Test Complete.");
            Logger.separator();

            // -----------------------------------------------------------
            // Only the coordinator collects the tree + initial token
            // holder from the console, then broadcasts them to everyone
            // (including itself, via a direct local call so it doesn't
            // have to round-trip a message to its own socket).
            // -----------------------------------------------------------
            if (myId == coordinatorId) {
                setupTree(client, node, totalSites, myId);
            } else {
                Logger.log(myId,
                        "Waiting for coordinator (University "
                                + coordinatorId
                                + ") to broadcast the tree...");
            }

            runMenu(node, myId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testConnection(Config config, int myId, int target) {

        String host = config.getHost(target);
        int port = config.getPort(target);

        try (Socket socket = new Socket()) {

            socket.connect(
                    new InetSocketAddress(host, port),
                    3000);

            Logger.log(myId,
                    "SUCCESS -> University "
                            + target + " (" + host + ":" + port + ")");

        } catch (IOException e) {

            Logger.log(myId,
                    "FAILED -> University "
                            + target + " (" + host + ":" + port + ")");

            Logger.log(myId, "Reason : " + e.getMessage());
        }
    }

    /*
     * Coordinator-only: reads the Raymond tree (parent of every site) from
     * the console, validates it with TreeManager, then broadcasts
     * TREE_CONFIG -> TOKEN_OWNER -> START to every site in that order.
     */
    private static void setupTree(Client client,
                                  UniversityNode node,
                                  int totalSites,
                                  int myId) {

        Scanner sc = new Scanner(System.in);

        System.out.println();
        System.out.println("===== Coordinator: Define Raymond's Tree =====");
        System.out.println("Enter the parent University ID for each site.");
        System.out.println("Use -1 for whichever site should be the root.");
        System.out.println();

        TreeManager tree = new TreeManager(totalSites);

        for (int i = 1; i <= totalSites; i++) {

            int parent = readInt(sc, "Parent of University " + i + " : ");

            tree.setParent(i, parent);
        }

        tree.buildChildren();

        if (!tree.validateTree()) {

            System.out.println("Invalid tree, please re-enter.");
            System.out.println();

            setupTree(client, node, totalSites, myId);

            return;
        }

        String serializedTree = tree.serialize();

        broadcast(client, node, totalSites, myId,
                new Message(MessageType.TREE_CONFIG, myId, -1, serializedTree));

        int tokenHolder = readInt(sc,
                "Enter the University ID that should start holding the token : ");

        broadcast(client, node, totalSites, myId,
                new Message(MessageType.TOKEN_OWNER, myId, -1, String.valueOf(tokenHolder)));

        broadcast(client, node, totalSites, myId,
                new Message(MessageType.START, myId, -1, "START"));

        System.out.println();
        System.out.println("Tree, token owner, and start signal broadcast complete.");
        System.out.println();
    }

    /*
     * Sends a copy of template (with receiverId filled in per site) to
     * every site, including this one -- delivered locally via
     * node.processMessage() instead of a network round trip to self.
     */
    private static void broadcast(Client client,
                                  UniversityNode node,
                                  int totalSites,
                                  int myId,
                                  Message template) {

        for (int i = 1; i <= totalSites; i++) {

            Message copy = new Message(
                    template.getType(),
                    myId,
                    i,
                    template.getSnapshotId(),
                    template.getPayload());

            if (i == myId) {
                node.processMessage(copy);
            } else {
                client.send(copy);
            }
        }
    }

    private static int readInt(Scanner sc, String prompt) {

        while (true) {

            System.out.print(prompt);

            String line = sc.nextLine().trim();

            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid integer.");
            }
        }
    }

    private static void runMenu(UniversityNode node, int myId) {

        Scanner sc = new Scanner(System.in);

        while (true) {

            System.out.println();
            System.out.println("================ MENU (University " + myId + ") ================");
            System.out.println("1. Request AI Accelerator  (Raymond critical section)");
            System.out.println("2. Print Raymond State");
            System.out.println("3. Initiate Global Snapshot (Chandy-Lamport)");
            System.out.println("4. Print Last Recorded Snapshot");
            System.out.println("5. Exit");
            System.out.print("Choice : ");

            String choice = sc.nextLine().trim();

            switch (choice) {

                case "1":

                    if (!node.isTreeReady()) {
                        System.out.println(
                                "Tree not configured yet -- wait for the coordinator's broadcast.");
                    } else {
                        node.getRaymond().requestCriticalSection();
                    }

                    break;

                case "2":

                    node.getRaymond().printState();

                    break;

                case "3":

                    node.getSnapshotManager().initiateSnapshot();

                    break;

                case "4":

                    node.getSnapshot().printSnapshot();

                    break;

                case "5":

                    Logger.log(myId, "Shutting down.");

                    Logger.close();

                    System.exit(0);

                    break;

                default:

                    System.out.println("Invalid choice.");
            }
        }
    }
}