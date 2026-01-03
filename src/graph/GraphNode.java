package graph;

import java.util.*;

public class GraphNode {
    private final String id;
    private final List<String> neighborIds;
    private volatile int color;
    private volatile boolean isColored;
    
    public GraphNode(String id) {
        this.id = id;
        this.neighborIds = new ArrayList<>();
        this.color = -1;
        this.isColored = false;
    }
    
    public String getId() { return id; }
    public List<String> getNeighborIds() { return new ArrayList<>(neighborIds); }
    public int getColor() { return color; }
    public boolean isColored() { return isColored; }
    
    public void addNeighbor(String neighborId) {
        if (!neighborIds.contains(neighborId)) {
            neighborIds.add(neighborId);
        }
    }
    
    public void setColor(int color) {
        this.color = color;
        this.isColored = true;
    }
    
    public void resetColor() {
        this.color = -1;
        this.isColored = false;
    }
}
