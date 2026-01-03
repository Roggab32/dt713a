package graph;

import java.util.*;

public class GraphEnvironment {
    private final Map<String, GraphNode> nodes;
    private final List<Integer> availableColors;
    
    public GraphEnvironment(int maxColors) {
        this.nodes = new HashMap<>();
        this.availableColors = new ArrayList<>();
        for (int i = 0; i < maxColors; i++) {
            availableColors.add(i);
        }
    }
    
    public GraphNode getNode(String nodeId) {
        return nodes.get(nodeId);
    }
    
    public Collection<GraphNode> getAllNodes() {
        return new ArrayList<>(nodes.values());
    }
    
    public void addNode(String nodeId) {
        if (!nodes.containsKey(nodeId)) {
            nodes.put(nodeId, new GraphNode(nodeId));
        }
    }
    
    public void addEdge(String node1Id, String node2Id) {
        GraphNode node1 = nodes.get(node1Id);
        GraphNode node2 = nodes.get(node2Id);
        if (node1 != null && node2 != null) {
            node1.addNeighbor(node2Id);
            node2.addNeighbor(node1Id);
        }
    }
    
    public List<Integer> getAvailableColors() {
        return new ArrayList<>(availableColors);
    }
    
    public int getMaxColors() {
        return availableColors.size();
    }
    
    public boolean hasConflict(String nodeId) {
        GraphNode node = nodes.get(nodeId);
        if (node == null || !node.isColored()) {
            return false;
        }
        
        for (String neighborId : node.getNeighborIds()) {
            GraphNode neighbor = nodes.get(neighborId);
            if (neighbor != null && neighbor.isColored() && neighbor.getColor() == node.getColor()) {
                return true;
            }
        }
        return false;
    }
    
    public boolean isFullyColored() {
        for (GraphNode node : nodes.values()) {
            if (!node.isColored()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean isValidColoring() {
        for (GraphNode node : nodes.values()) {
            if (hasConflict(node.getId())) {
                return false;
            }
        }
        return true;
    }
    
    public int countConflicts() {
        int conflicts = 0;
        for (GraphNode node : nodes.values()) {
            if (hasConflict(node.getId())) {
                conflicts++;
            }
        }
        return conflicts;
    }
    
    // ADD THIS METHOD - it was missing!
    public void printColoring() {
        System.out.println("\n=== Graph Coloring Result ===");
        System.out.println("Total nodes: " + nodes.size());
        System.out.println("Available colors: " + availableColors.size());
        System.out.println("Fully colored: " + isFullyColored());
        System.out.println("Valid coloring: " + isValidColoring());
        System.out.println("Conflicts: " + countConflicts());
        
        System.out.println("\nNode Details:");
        
        // Get nodes sorted by ID
        List<GraphNode> nodeList = new ArrayList<>(nodes.values());
        Collections.sort(nodeList, new Comparator<GraphNode>() {
            public int compare(GraphNode n1, GraphNode n2) {
                return n1.getId().compareTo(n2.getId());
            }
        });
        
        for (GraphNode node : nodeList) {
            System.out.printf("  Node %s: Color %s", 
                node.getId(), 
                node.isColored() ? node.getColor() : "none");
            
            if (hasConflict(node.getId())) {
                System.out.print(" (CONFLICT!)");
            }
            
            System.out.println(" | Neighbors: " + node.getNeighborIds());
        }
    }
    
    public void resetAllColors() {
        for (GraphNode node : nodes.values()) {
            node.resetColor();
        }
    }
}
