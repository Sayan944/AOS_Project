import java.util.HashSet;
import java.util.Set;

/*
 * Chandy-Lamport global state recording.
 *
 * Assumes a fully connected network (every site can open a socket to every
 * other site directly, same as Client/ServerThread already do for Raymond's
 * algorithm), so each node has (totalSites - 1) incoming channels: one per
 * other site.
 *
 * Only one snapshot is tracked at a time in this simplified version. If a
 * MARKER for a different (overlapping) snapshot id arrives while one is
 * already in progress, it is logged and ignored rather than merged.
 */
public class SnapshotManager {

    private final UniversityNode node;

    private final Client client;

    private final Snapshot snapshot;

    private final int totalSites;

    private boolean recording;

    private int activeSnapshotId;

    private final Set<Integer> markerReceivedFrom;

    private static int localCounter = 0;

    public SnapshotManager(UniversityNode node,
                           Client client,
                           Snapshot snapshot,
                           int totalSites) {

        this.node = node;

        this.client = client;

        this.snapshot = snapshot;

        this.totalSites = totalSites;

        this.recording = false;

        this.activeSnapshotId = -1;

        this.markerReceivedFrom = new HashSet<>();
    }

    /*
     * Called by whichever site starts the snapshot (any site can initiate,
     * not just the coordinator).
     */
    public synchronized void initiateSnapshot() {

        if (recording) {

            Logger.log(node.getSiteId(),
                    "Snapshot already in progress (#" + activeSnapshotId
                            + "), ignoring new request.");

            return;
        }

        localCounter++;

        activeSnapshotId = (node.getSiteId() * 1000) + localCounter;

        beginRecording(activeSnapshotId);

        broadcastMarker(activeSnapshotId);
    }

    /*
     * Called by UniversityNode when a MARKER message arrives.
     */
    public synchronized void handleMarker(Message message) {

        int from = message.getSenderId();

        int snapshotId = message.getSnapshotId();

        if (!recording) {

            // first marker seen for this snapshot -> record local state
            // and propagate markers on every outgoing channel
            activeSnapshotId = snapshotId;

            beginRecording(snapshotId);

            broadcastMarker(snapshotId);

        } else if (snapshotId != activeSnapshotId) {

            Logger.log(node.getSiteId(),
                    "Ignoring MARKER for snapshot #" + snapshotId
                            + " while snapshot #" + activeSnapshotId
                            + " is still in progress.");

            return;
        }

        // channel from this sender is now considered closed for this snapshot
        markerReceivedFrom.add(from);

        Logger.log(node.getSiteId(),
                "Marker received from University " + from
                        + " (" + markerReceivedFrom.size()
                        + "/" + (totalSites - 1) + " channels closed)");

        if (markerReceivedFrom.size() >= totalSites - 1) {

            finishRecording();
        }
    }

    /*
     * Called by UniversityNode before it hands off a REQUEST/TOKEN message
     * to RaymondAlgorithm. If a snapshot is in progress and the channel this
     * message arrived on hasn't been closed by a MARKER yet, the message is
     * "in flight" from the snapshot's point of view and gets logged as part
     * of that channel's recorded state.
     */
    public synchronized void recordIfChannelOpen(Message message) {

        if (!recording)
            return;

        int from = message.getSenderId();

        if (markerReceivedFrom.contains(from))
            return;

        snapshot.recordChannelMessage(
                "From University " + from
                        + " : " + message.getType()
                        + " (" + message.getPayload() + ")");
    }

    public synchronized boolean isRecording() {
        return recording;
    }

    private void beginRecording(int snapshotId) {

        recording = true;

        markerReceivedFrom.clear();

        snapshot.resetSnapshot();

        String state = (node.getRaymond() == null)
                ? "raymond not initialized yet"
                : node.getRaymond().stateSummary();

        snapshot.recordLocalState(state);

        Logger.snapshotStarted(node.getSiteId(), snapshotId);
    }

    private void broadcastMarker(int snapshotId) {

        for (int i = 1; i <= totalSites; i++) {

            if (i == node.getSiteId())
                continue;

            Message marker =
                    new Message(
                            MessageType.MARKER,
                            node.getSiteId(),
                            i,
                            snapshotId,
                            "MARKER");

            client.send(marker);
        }
    }

    private void finishRecording() {

        recording = false;

        Logger.snapshotCompleted(node.getSiteId(), activeSnapshotId);

        snapshot.printSnapshot();
    }
}