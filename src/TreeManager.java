/*

 since we're making the tree user inputable ,
 no single university should manually read it,
 instead we're using a TreeManager
 */
import java.util.*;

public class TreeManager {
    private final int totalSites;
    private final int[] parent;
    private final List<Integer>[] children;

    @SuppressWarnings("unchecked")
    public TreeManager(int totalSites) {
        this.totalSites = totalSites;
        parent = new int[totalSites + 1];
        children = new ArrayList[totalSites + 1];

        for (int i = 1; i <= totalSites; i++) {
            children[i] = new ArrayList<>();
        }
    }

    public void setParent(int site, int parentId) {
        parent[site] = parentId;
    }

    public int getParent(int site) {
        return parent[site];
    }

    public List<Integer> getChildren(int site) {
        return children[site];
    }

    public void buildChildren() {
        for (int i = 1; i <= totalSites; i++)
            children[i].clear();

        for (int i = 1; i <= totalSites; i++) {
            if (parent[i] != -1)
                children[parent[i]].add(i);
        }
    }

    public boolean validateTree() {
        int roots = 0;
        int root = -1;
        for (int i = 1; i <= totalSites; i++) {

            if (parent[i] == -1) {
                roots++;
                root = i;
            }
        }
        /*
        assuuming it's an entire connected graph topology, 
        must contain exactly one root   */
        if (roots != 1) {
            System.out.println("Tree must contain exactly one root.");
            return false;
        }

        boolean[] visited = new boolean[totalSites + 1];
        if (hasCycle(root, visited))
            return false;

        for (int i = 1; i <= totalSites; i++) {
            if (!visited[i]) {
                System.out.println("University " + i + " is disconnected.");
                return false;
            }
        }
        return true;
    }

    private boolean hasCycle(int node, boolean[] visited) {

        if (visited[node])
            return true;

        visited[node] = true;
        for (int child : children[node]) {
            if (hasCycle(child, visited))
                return true;
        }
        return false;
    }

    public String serialize() {

        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= totalSites; i++) {

            sb.append(i)
                    .append(":")
                    .append(parent[i]);

            if (i != totalSites)
                sb.append(",");
        }
        return sb.toString();
    }

    public void deserialize(String payload) {
        String[] entries = payload.split(",");
        for (String e : entries) {
            String[] p = e.split(":");

            int site = Integer.parseInt(p[0]);
            int par = Integer.parseInt(p[1]);

            parent[site] = par;
        }
        buildChildren();
    }
}