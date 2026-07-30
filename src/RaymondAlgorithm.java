import java.util.LinkedList;
import java.util.Queue;

public class RaymondAlgorithm {

    private final UniversityNode node;

    private final Client client;

    private final Queue<Integer> requestQueue;

    private int holder;

    private boolean hasToken;

    private boolean usingToken;

    public RaymondAlgorithm(UniversityNode node,
                            Client client) {

        this.node = node;

        this.client = client;

        requestQueue = new LinkedList<>();

        holder = -1;

        hasToken = false;

        usingToken = false;
    }

    /*
     * Called once after coordinator broadcasts
     * initial token owner.
     */
    public void initialize(int tokenHolder) {

        if (tokenHolder == node.getSiteId()) {

            hasToken = true;

            holder = node.getSiteId();

        } else {

            hasToken = false;

            holder = node.getParent();
        }

        Logger.log(node.getSiteId(),
                "Holder = " + holder +
                " Token = " + hasToken);
    }

    /*
     * User presses
     * Request Accelerator
     */
    public synchronized void requestCriticalSection() {

        Logger.request(node.getSiteId());

        if (!requestQueue.contains(node.getSiteId()))
            requestQueue.offer(node.getSiteId());

        assignPrivilege();

        makeRequest();
    }

    /*
     * Receive REQUEST message.
     */
    public synchronized void handleRequest(Message message) {

        int requester = message.getSenderId();

        if (!requestQueue.contains(requester))
            requestQueue.offer(requester);

        assignPrivilege();

        makeRequest();
    }

    /*
     * Receive TOKEN.
     */
    public synchronized void handleToken(Message message) {

        hasToken = true;

        holder = node.getSiteId();

        Logger.log(node.getSiteId(),
                "TOKEN RECEIVED");

        assignPrivilege();
    }

    /*
     * Raymond rule:
     * Send REQUEST upwards.
     */
    private void makeRequest() {

        if (hasToken)
            return;

        if (requestQueue.isEmpty())
            return;

        if (holder == node.getSiteId())
            return;

        Message request =
                new Message(
                        MessageType.REQUEST,
                        node.getSiteId(),
                        holder,
                        "REQUEST");

        client.send(request);

        holder = node.getSiteId();

    }

    /*
     * Raymond rule:
     * Pass token if needed.
     */
    private void assignPrivilege() {

        if (!hasToken)
            return;

        if (usingToken)
            return;

        if (requestQueue.isEmpty())
            return;

        int next = requestQueue.peek();

        if (next == node.getSiteId()) {

            requestQueue.poll();

            executeCriticalSection();

        } else {

            requestQueue.poll();

            hasToken = false;

            holder = next;

            Message token =
                    new Message(
                            MessageType.TOKEN,
                            node.getSiteId(),
                            next,
                            "TOKEN");

            client.send(token);

            Logger.log(node.getSiteId(),
                    "TOKEN SENT TO University "
                            + next);
        }
    }

    /*
     * Simulated AI Accelerator
     */
    private void executeCriticalSection() {

        usingToken = true;

        Logger.enterCS(node.getSiteId());

        try {

            Thread.sleep(60000);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();
        }

        Logger.exitCS(node.getSiteId());

        usingToken = false;

        assignPrivilege();
    }

    /*
     * Print state
     */
    public void printState() {

        System.out.println();

        System.out.println("------ Raymond State ------");

        System.out.println("Holder      : " + holder);

        System.out.println("Has Token   : " + hasToken);

        System.out.println("Using Token : " + usingToken);

        System.out.println("Queue       : " + requestQueue);

        System.out.println("---------------------------");
    }

    /*
     * One-line snapshot of this site's Raymond state, used by
     * SnapshotManager when recording local state for Chandy-Lamport.
     */
    public synchronized String stateSummary() {

        return "Holder=" + holder
                + ", HasToken=" + hasToken
                + ", UsingToken=" + usingToken
                + ", Queue=" + requestQueue;
    }

}