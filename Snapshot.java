package algorithm;

import java.util.ArrayList;
import java.util.List;

public class Snapshot {

    // Has this university already recorded its state?
    private boolean snapshotRecorded;

    // Local state of this university
    private String localState;

    // Messages received on channels after recording
    private List<String> channelStates;

    public Snapshot() {

        snapshotRecorded = false;

        localState = "";

        channelStates = new ArrayList<>();

    }

    // ---------------- Getters ----------------

    public boolean isSnapshotRecorded() {
        return snapshotRecorded;
    }

    public String getLocalState() {
        return localState;
    }

    public List<String> getChannelStates() {
        return channelStates;
    }

    // ---------------- Record Local State ----------------

    public void recordLocalState(String state) {

        if (!snapshotRecorded) {

            snapshotRecorded = true;
            localState = state;

            System.out.println("Snapshot Recorded.");
            System.out.println("Local State : " + localState);

        }

    }

    // ---------------- Record Channel State ----------------

    public void recordChannelMessage(String message) {

        if (snapshotRecorded) {

            channelStates.add(message);

        }

    }

    // ---------------- Display Snapshot ----------------

    public void printSnapshot() {

        System.out.println("\n========== SNAPSHOT ==========");

        System.out.println("Recorded : " + snapshotRecorded);

        System.out.println("Local State : " + localState);

        System.out.println("Channel Messages :");

        if (channelStates.isEmpty()) {

            System.out.println("None");

        } else {

            for (String msg : channelStates) {

                System.out.println(msg);

            }

        }

        System.out.println("==============================");

    }

    // ---------------- Reset ----------------

    public void resetSnapshot() {

        snapshotRecorded = false;

        localState = "";

        channelStates.clear();

    }

}