import java.util.ArrayList;
import java.util.List;

public class UniversityNode {

    private final int siteId;

    private final int totalSites;

    private final Client client;

    private RaymondAlgorithm raymond;

    private final Snapshot snapshot;

    private final SnapshotManager snapshotManager;

    private int parent;

    private final List<Integer> children;

    private boolean started;

    private boolean treeReady;

    /*
     * NOTE: RaymondAlgorithm's constructor needs a UniversityNode, and
     * UniversityNode used to need a fully built RaymondAlgorithm -> circular
     * dependency. Fixed with two-phase construction: build the node first
     * (raymond left unset), build RaymondAlgorithm(node, client) next, then
     * call node.setRaymond(raymond) before the server thread starts.
     */
    public UniversityNode(int siteId,
                          int totalSites,
                          Client client,
                          Snapshot snapshot) {

        this.siteId = siteId;

        this.totalSites = totalSites;

        this.client = client;

        this.raymond = null;

        this.snapshot = snapshot;

        this.snapshotManager = new SnapshotManager(this, client, snapshot, totalSites);

        this.children = new ArrayList<>();

        this.parent = -1;

        this.started = false;

        this.treeReady = false;
    }

    public void setRaymond(RaymondAlgorithm raymond) {
        this.raymond = raymond;
    }

    public int getSiteId() {
        return siteId;
    }

    public int getTotalSites() {
        return totalSites;
    }

    public int getParent() {
        return parent;
    }

    public List<Integer> getChildren() {
        return children;
    }

    public void setTree(int parent,
                        List<Integer> childList) {

        this.parent = parent;

        children.clear();

        children.addAll(childList);

        Logger.log(siteId,
                "Tree updated. Parent = "
                        + parent
                        + " Children = "
                        + children);
    }

    public void processMessage(Message message) {

        switch (message.getType()) {

            case REQUEST:

                snapshotManager.recordIfChannelOpen(message);

                raymond.handleRequest(message);

                break;

            case TOKEN:

                snapshotManager.recordIfChannelOpen(message);

                raymond.handleToken(message);

                break;

            case MARKER:

                snapshotManager.handleMarker(message);

                break;

            case TREE_CONFIG:

                receiveTree(message);

                break;

            case TOKEN_OWNER:

                raymond.initialize(
                        Integer.parseInt(message.getPayload()));

                break;

            case START:

                started = true;

                Logger.log(siteId,
                        "SYSTEM STARTED");

                break;

            case PING:

                Logger.log(siteId,
                        "PING received from University "
                                + message.getSenderId());

                break;
        }
    }

    private void receiveTree(Message message) {

        TreeManager tree =
                new TreeManager(totalSites);

        tree.deserialize(message.getPayload());

        setTree(
                tree.getParent(siteId),
                tree.getChildren(siteId));

        treeReady = true;

        Logger.log(siteId,
                "Tree configuration received.");
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isTreeReady() {
        return treeReady;
    }

    public Client getClient() {
        return client;
    }

    public RaymondAlgorithm getRaymond() {
        return raymond;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public SnapshotManager getSnapshotManager() {
        return snapshotManager;
    }
}