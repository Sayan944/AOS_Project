package common;

import java.util.*;

public class Config {

    public static final int TOTAL_NODES = 5;

    private static final int[] PORTS = {
        0, 5001, 5002, 5003, 5004, 5005
    };

    
    private static final int[] PARENT = {
        0, 0, 1, 1, 1, 2
    };

    public static int getPort(int id) {
        return PORTS[id];
    }

    public static int getParent(int id) {
        return PARENT[id];
    }

    /*
     * Finds the immediate neighbour from nodeId
     * towards the initial token holder.
     *
     * This allows the token to initially be at
     * U1, U2, U3, U4 or U5.
     */
    public static int getInitialHolder(
            int nodeId,
            int tokenHolder) {

        if (nodeId == tokenHolder) {
            return nodeId;
        }

        Map<Integer, List<Integer>> graph =
                new HashMap<>();

        for (int i = 1; i <= TOTAL_NODES; i++) {
            graph.put(i, new ArrayList<>());
        }

        // U1-U2
        addEdge(graph, 1, 2);

        // U1-U3
        addEdge(graph, 1, 3);

        // U1-U4
        addEdge(graph, 1, 4);

        // U2-U5
        addEdge(graph, 2, 5);

        Queue<Integer> queue =
                new LinkedList<>();

        Map<Integer, Integer> previous =
                new HashMap<>();

        queue.add(nodeId);
        previous.put(nodeId, -1);

        while (!queue.isEmpty()) {

            int current = queue.poll();

            if (current == tokenHolder) {
                break;
            }

            for (int next : graph.get(current)) {

                if (!previous.containsKey(next)) {

                    previous.put(next, current);

                    queue.add(next);
                }
            }
        }

        /*
         * Walk backwards from tokenHolder
         * until we reach nodeId.
         */
        int current = tokenHolder;

        int previousNode = previous.get(current);

        while (previousNode != nodeId
                && previousNode != -1) {

            current = previousNode;

            previousNode = previous.get(current);
        }

        return current;
    }

    private static void addEdge(
            Map<Integer, List<Integer>> graph,
            int a,
            int b) {

        graph.get(a).add(b);
        graph.get(b).add(a);
    }
}