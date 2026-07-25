package node;

import algorithm.RaymondAlgorithm;
import algorithm.Snapshot;
import common.Config;
import common.Message;
import network.Client;
import network.ServerThread;

public class UniversityNode {

    private int universityId;

    private RaymondAlgorithm raymond;

    private Snapshot snapshot;

    private ServerThread server;

    /*
     * Used only to make the menu wait until
     * the current request has completed.
     */
    private final Object tokenLock =
            new Object();

    private boolean waitingForToken = false;

    public UniversityNode(
            int universityId,
            int initialTokenHolder) {

        this.universityId =
                universityId;

        boolean initialToken =
                universityId
                        == initialTokenHolder;

        int holder =
                Config.getInitialHolder(
                        universityId,
                        initialTokenHolder);

        raymond =
                new RaymondAlgorithm(
                        universityId,
                        holder,
                        initialToken);

        snapshot =
                new Snapshot();

        server =
                new ServerThread(
                        universityId,
                        Config.getPort(universityId),
                        this);

        server.start();
    }

    // =================================================
    // HANDLE MESSAGE
    // =================================================

    public void handleMessage(
            Message message) {

        switch (message.getType()) {

            case Message.REQUEST:

                receiveRequest(
                        message.getSenderId());

                break;

            case Message.TOKEN:

                receiveToken(
                        message.getSenderId());

                break;

            case Message.MARKER:

                receiveMarker(
                        message.getSenderId());

                break;

            default:

                System.out.println(
                        "Unknown Message.");
        }
    }

    // =================================================
    // REQUEST AI ACCELERATOR
    // =================================================

    public void requestToken() {

        System.out.println(
                "\nUniversity "
                        + universityId
                        + " requesting AI Accelerator...");

        synchronized (tokenLock) {

            waitingForToken = true;
        }

        RaymondAlgorithm.Action action =
                raymond.requestCriticalSection();

        processAction(action);

        /*
         * If we entered CS directly, no waiting.
         */
        if (action.getType()
                == RaymondAlgorithm.ENTER_CS) {

            return;
        }

        /*
         * Otherwise wait until this request
         * has completed its CS.
         */
        synchronized (tokenLock) {

            while (waitingForToken) {

                try {

                    tokenLock.wait();

                } catch (InterruptedException e) {

                    Thread.currentThread()
                            .interrupt();

                    return;
                }
            }
        }
    }

    // =================================================
    // RECEIVE REQUEST
    // =================================================

    private void receiveRequest(
            int sender) {

        RaymondAlgorithm.Action action =
                raymond.receiveRequest(sender);

        processAction(action);
    }

    // =================================================
    // RECEIVE TOKEN
    // =================================================

    private void receiveToken(
            int sender) {

        RaymondAlgorithm.Action action =
                raymond.receiveToken(sender);

        processAction(action);
    }

    // =================================================
    // PROCESS RAYMOND ACTION
    // =================================================

    private void processAction(
            RaymondAlgorithm.Action action) {

        switch (action.getType()) {

            case RaymondAlgorithm.SEND_REQUEST:

                sendRequest(
                        action.getTarget());

                break;

            case RaymondAlgorithm.SEND_TOKEN:

                sendToken(
                        action.getTarget());

                break;

            case RaymondAlgorithm.ENTER_CS:

                enterCriticalSection();

                break;

            case RaymondAlgorithm.NONE:

                break;

            default:

                System.out.println(
                        "Unknown Raymond action.");
        }
    }

    // =================================================
    // SEND REQUEST
    // =================================================

    private void sendRequest(
            int target) {

        System.out.println(
                "REQUEST: U"
                        + universityId
                        + " -> U"
                        + target);

        Client.sendMessage(
                target,
                new Message(
                        Message.REQUEST,
                        universityId));
    }

    // =================================================
    // SEND TOKEN
    // =================================================

    private void sendToken(
            int target) {

        System.out.println(
                "TOKEN: U"
                        + universityId
                        + " -> U"
                        + target);

        Client.sendMessage(
                target,
                new Message(
                        Message.TOKEN,
                        universityId));
    }

    // =================================================
    // ENTER CRITICAL SECTION
    // =================================================

    private void enterCriticalSection() {

        System.out.println(
                "\n*************");

        System.out.println(
                "University "
                        + universityId
                        + " ENTERED CRITICAL SECTION");

        System.out.println(
                "Using AI Accelerator...");

        System.out.println(
                "*************");

        try {

            Thread.sleep(3000);

        } catch (InterruptedException e) {

            Thread.currentThread()
                    .interrupt();
        }

        leaveCriticalSection();
    }

    // =================================================
    // LEAVE CRITICAL SECTION
    // =================================================

    private void leaveCriticalSection() {

        System.out.println(
                "\nUniversity "
                        + universityId
                        + " LEFT Critical Section");

        RaymondAlgorithm.Action action =
                raymond.releaseCriticalSection();

        processAction(action);

        /*
         * Wake up Main.java only AFTER
         * token handling has finished.
         */
        synchronized (tokenLock) {

            waitingForToken = false;

            tokenLock.notifyAll();
        }
    }

    // =================================================
    // SNAPSHOT
    // =================================================

    public void startSnapshot() {

        String state =
                "Holder=U"
                        + raymond.getHolder()
                        + ", Token="
                        + raymond.hasToken()
                        + ", Queue="
                        + raymond.getRequestQueue();

        snapshot.recordLocalState(state);

        System.out.println(
                "Snapshot Started.");
    }

    // =================================================
    // RECEIVE MARKER
    // =================================================

    private void receiveMarker(
            int sender) {

        System.out.println(
                "MARKER received from University "
                        + sender);

        if (!snapshot.isSnapshotRecorded()) {

            startSnapshot();
        }
    }

    // =================================================
    // STATUS
    // =================================================

    public void printStatus() {

        raymond.printStatus();

        snapshot.printSnapshot();
    }
}