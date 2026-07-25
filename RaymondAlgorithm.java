package algorithm;

import java.util.LinkedList;
import java.util.Queue;

public class RaymondAlgorithm {

    public static final int NONE = 0;
    public static final int SEND_REQUEST = 1;
    public static final int SEND_TOKEN = 2;
    public static final int ENTER_CS = 3;

    private int universityId;

    /*
     * Holder points toward the current token holder.
     */
    private int holder;

    private boolean hasToken;

    private boolean usingToken;

    /*
     * True means this university has already
     * forwarded a REQUEST and is waiting for TOKEN.
     */
    private boolean requestOutstanding;

    /*
     * Local FIFO request queue.
     */
    private Queue<Integer> requestQueue;

    public RaymondAlgorithm(
            int universityId,
            int holder,
            boolean hasToken) {

        this.universityId = universityId;

        this.holder = holder;

        this.hasToken = hasToken;

        this.usingToken = false;

        this.requestOutstanding = false;

        this.requestQueue =
                new LinkedList<>();
    }

    // =================================================
    // ACTION CLASS
    // =================================================

    public static class Action {

        private int type;
        private int target;

        public Action(int type, int target) {

            this.type = type;

            this.target = target;
        }

        public int getType() {
            return type;
        }

        public int getTarget() {
            return target;
        }
    }

    // =================================================
    // REQUEST CRITICAL SECTION
    // =================================================

    public synchronized Action requestCriticalSection() {

        /*
         * Do not add duplicate request.
         */
        if (requestQueue.contains(universityId)) {

            return new Action(NONE, -1);
        }

        /*
         * Add our own request to local queue.
         */
        requestQueue.add(universityId);

        System.out.println(
                "Local Queue of U"
                        + universityId
                        + " : "
                        + requestQueue);

        /*
         * If we already have token and our request
         * is at the head, enter CS.
         */
        if (hasToken
                && !usingToken
                && requestQueue.peek()
                        == universityId) {

            requestQueue.poll();

            usingToken = true;

            return new Action(
                    ENTER_CS,
                    universityId);
        }

        /*
         * If token is elsewhere, only the first
         * outstanding request is forwarded.
         */
        if (!hasToken
                && !requestOutstanding) {

            requestOutstanding = true;

            return new Action(
                    SEND_REQUEST,
                    holder);
        }

        return new Action(
                NONE,
                -1);
    }

    // =================================================
    // RECEIVE REQUEST
    // =================================================

    public synchronized Action receiveRequest(
            int sender) {

        System.out.println(
                "Raymond: REQUEST from U"
                        + sender);

        /*
         * Ignore duplicate request.
         */
        if (requestQueue.contains(sender)) {

            return new Action(
                    NONE,
                    -1);
        }

        boolean wasEmpty =
                requestQueue.isEmpty();

        requestQueue.add(sender);

        System.out.println(
                "Updated Queue of U"
                        + universityId
                        + " : "
                        + requestQueue);

        /*
         * If we have token and are free,
         * send token to the first requester.
         *
         * IMPORTANT:
         * Only do this when the queue was empty.
         * If queue already contained U2, and U3 arrives,
         * U2 must remain first.
         */
        if (hasToken
                && !usingToken
                && wasEmpty) {

            int next =
                    requestQueue.poll();

            hasToken = false;

            holder = next;

            System.out.println(
                    "U"
                            + universityId
                            + " sends TOKEN to U"
                            + next);

            return new Action(
                    SEND_TOKEN,
                    next);
        }

        /*
         * If we do not have token and this was
         * the first request in our queue,
         * forward REQUEST to our holder.
         */
        if (!hasToken
                && wasEmpty
                && !requestOutstanding) {

            requestOutstanding = true;

            return new Action(
                    SEND_REQUEST,
                    holder);
        }

        return new Action(
                NONE,
                -1);
    }

    // =================================================
    // RECEIVE TOKEN
    // =================================================

    public synchronized Action receiveToken(
            int sender) {

        System.out.println(
                "Raymond: TOKEN received from U"
                        + sender);

        hasToken = true;

        /*
         * Previous request has now been satisfied.
         */
        requestOutstanding = false;

        System.out.println(
                "Local Queue of U"
                        + universityId
                        + " : "
                        + requestQueue);

        if (requestQueue.isEmpty()) {

            System.out.println(
                    "No pending requests. U"
                            + universityId
                            + " keeps TOKEN.");

            return new Action(
                    NONE,
                    -1);
        }

        /*
         * If our own request is at the head,
         * enter CS.
         */
        if (requestQueue.peek()
                == universityId) {

            requestQueue.poll();

            usingToken = true;

            return new Action(
                    ENTER_CS,
                    universityId);
        }

        /*
         * Otherwise forward token to the
         * requester at the head.
         */
        int next =
                requestQueue.poll();

        hasToken = false;

        holder = next;

        System.out.println(
                "U"
                        + universityId
                        + " forwards TOKEN to U"
                        + next);

        /*
         * If other requests remain, this node
         * must request the token back from the
         * new holder.
         */
        if (!requestQueue.isEmpty()) {

            requestOutstanding = true;

            System.out.println(
                    "U"
                            + universityId
                            + " still has pending requests.");

            return new Action(
                    SEND_TOKEN,
                    next);
        }

        return new Action(
                SEND_TOKEN,
                next);
    }

    // =================================================
    // RELEASE TOKEN
    // =================================================

    public synchronized Action releaseCriticalSection() {

        usingToken = false;

        System.out.println(
                "U"
                        + universityId
                        + " released the TOKEN.");

        /*
         * No pending requests.
         * Keep token.
         */
        if (requestQueue.isEmpty()) {

            System.out.println(
                    "No pending requests. U"
                            + universityId
                            + " keeps TOKEN.");

            return new Action(
                    NONE,
                    -1);
        }

        /*
         * Give token to next requester.
         */
        int next =
                requestQueue.poll();

        hasToken = false;

        holder = next;

        System.out.println(
                "U"
                        + universityId
                        + " sends TOKEN to U"
                        + next);

        /*
         * If another request remains,
         * ask the new holder for token.
         */
        if (!requestQueue.isEmpty()) {

            requestOutstanding = true;

            System.out.println(
                    "U"
                            + universityId
                            + " sends REQUEST to U"
                            + next
                            + " for remaining queue.");

            return new Action(
                    SEND_TOKEN,
                    next);
        }

        return new Action(
                SEND_TOKEN,
                next);
    }

    // =================================================
    // GETTERS
    // =================================================

    public synchronized int getHolder() {

        return holder;
    }

    public synchronized boolean hasToken() {

        return hasToken;
    }

    public synchronized boolean isUsingToken() {

        return usingToken;
    }

    public synchronized Queue<Integer>
            getRequestQueue() {

        return new LinkedList<>(
                requestQueue);
    }

    // =================================================
    // STATUS
    // =================================================

    public synchronized void printStatus() {

        System.out.println(
                "\n========== Raymond Status ==========");

        System.out.println(
                "University : U"
                        + universityId);

        System.out.println(
                "Holder     : U"
                        + holder);

        System.out.println(
                "Has Token  : "
                        + hasToken);

        System.out.println(
                "Using Token: "
                        + usingToken);

        System.out.println(
                "Queue      : "
                        + requestQueue);

        System.out.println(
                "Request Sent: "
                        + requestOutstanding);

        System.out.println(
                "====================================");
    }
}