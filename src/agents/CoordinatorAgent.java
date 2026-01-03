package agents;

import graph.GraphEnvironment;
import graph.GraphNode;
import messaging.Message;
import messaging.MessageBus;

import java.util.*;

public class CoordinatorAgent implements NodeAgent, Runnable {
    private final GraphEnvironment environment;
    private final MessageBus messageBus;
    private volatile boolean running;
    private Thread agentThread;
    private final Set<String> registeredAgents;
    private int iteration;
    
    public CoordinatorAgent(GraphEnvironment environment) {
        this.environment = environment;
        this.messageBus = MessageBus.getInstance();
        this.registeredAgents = new HashSet<>();
        this.iteration = 0;
    }
    
    @Override public String getAgentId() { return "COORDINATOR"; }
    @Override public boolean isRunning() { return running; }
    
    @Override
    public void start() {
        if (!running) {
            running = true;
            agentThread = new Thread(this, "Coordinator");
            agentThread.start();
            System.out.println("COORDINATOR: Experiment started");
            System.out.println("COORDINATOR: Graph has " + environment.getAllNodes().size() + 
                             " nodes, " + environment.getAvailableColors().size() + " colors available");
        }
    }
    
    @Override
    public void stop() {
        running = false;
        if (agentThread != null) {
            agentThread.interrupt();
        }
    }
    
    @Override
    public void run() {
        System.out.println("COORDINATOR: Beginning monitoring...\n");
        
        while (running && iteration < 200) {
            iteration++;
            
            try {
                // Check if we have a valid coloring
                if (environment.isFullyColored() && environment.isValidColoring()) {
                    System.out.println("\n" + "=".repeat(60));
                    System.out.println("COORDINATOR: PERFECT COLORING FOUND!");
                    System.out.println("COORDINATOR: Iterations: " + iteration);
                    System.out.println("COORDINATOR: Stopping all agents...");
                    System.out.println("=".repeat(60));
                    stopAllAgents();
                    printDetailedResults();
                    break;
                }
                
                // Print detailed progress every 5 iterations
                if (iteration % 5 == 0) {
                    printProgressReport();
                }
                
                // Process registration messages
                processMessages();
                
                Thread.sleep(150);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (iteration >= 200) {
            System.out.println("\n" + "!".repeat(60));
            System.out.println("COORDINATOR: Maximum iterations reached (200)");
            System.out.println("COORDINATOR: Final state:");
            System.out.println("!".repeat(60));
            printDetailedResults();
        }
    }
    
    private void printProgressReport() {
        System.out.println("\nCOORDINATOR: Progress Report - Iteration " + iteration);
        System.out.println("-".repeat(50));
        
        int colored = 0;
        int conflicts = 0;
        Map<Integer, Integer> colorCounts = new HashMap<>();
        
        // Collect statistics
        for (GraphNode node : environment.getAllNodes()) {
            if (node.isColored()) {
                colored++;
                int color = node.getColor();
                colorCounts.put(color, colorCounts.getOrDefault(color, 0) + 1);
                
                // Check conflicts
                boolean hasConflict = false;
                for (String neighborId : node.getNeighborIds()) {
                    GraphNode neighbor = environment.getNode(neighborId);
                    if (neighbor != null && neighbor.isColored() && neighbor.getColor() == color) {
                        hasConflict = true;
                        break;
                    }
                }
                if (hasConflict) conflicts++;
            }
        }
        
        System.out.printf("Colored nodes: %d/%d (%.0f%%)\n", 
            colored, environment.getAllNodes().size(), 
            (colored * 100.0 / environment.getAllNodes().size()));
        System.out.println("Active conflicts: " + conflicts);
        System.out.println("Valid coloring so far: " + (conflicts == 0 ? "YES" : "NO"));
        
        // Show color distribution
        if (!colorCounts.isEmpty()) {
            System.out.print("Color distribution: ");
            List<Integer> colors = new ArrayList<>(colorCounts.keySet());
            Collections.sort(colors);
            for (int color : colors) {
                System.out.printf("Color %d: %d nodes  ", color, colorCounts.get(color));
            }
            System.out.println();
        }
        
        // Show specific nodes with conflicts
        if (conflicts > 0) {
            System.out.println("Nodes with conflicts:");
            for (GraphNode node : environment.getAllNodes()) {
                if (environment.hasConflict(node.getId())) {
                    List<String> conflictingWith = new ArrayList<>();
                    for (String neighborId : node.getNeighborIds()) {
                        GraphNode neighbor = environment.getNode(neighborId);
                        if (neighbor != null && neighbor.isColored() && 
                            neighbor.getColor() == node.getColor()) {
                            conflictingWith.add(neighborId);
                        }
                    }
                    System.out.printf("  %s (Color %d) conflicts with: %s\n",
                        node.getId(), node.getColor(), conflictingWith);
                }
            }
        }
    }
    
    private void printDetailedResults() {
        System.out.println("\nCOORDINATOR: Detailed Node Status");
        System.out.println("-".repeat(50));
        
        // Sort nodes by ID
        List<GraphNode> nodes = new ArrayList<>(environment.getAllNodes());
        Collections.sort(nodes, new Comparator<GraphNode>() {
            public int compare(GraphNode n1, GraphNode n2) {
                return n1.getId().compareTo(n2.getId());
            }
        });
        
        // Print table header
        System.out.printf("%-10s %-8s %-12s %-20s\n", 
            "Node", "Color", "Status", "Neighbors (Colors)");
        System.out.println("-".repeat(50));
        
        // Print each node
        for (GraphNode node : nodes) {
            String colorStr = node.isColored() ? String.valueOf(node.getColor()) : "none";
            String status = environment.hasConflict(node.getId()) ? "CONFLICT" : "OK";
            
            // Build neighbor info string
            StringBuilder neighborInfo = new StringBuilder();
            for (String neighborId : node.getNeighborIds()) {
                GraphNode neighbor = environment.getNode(neighborId);
                if (neighbor != null) {
                    neighborInfo.append(neighborId);
                    if (neighbor.isColored()) {
                        neighborInfo.append("(").append(neighbor.getColor()).append(")");
                    }
                    neighborInfo.append(" ");
                }
            }
            
            System.out.printf("%-10s %-8s %-12s %-20s\n",
                node.getId(), colorStr, status, neighborInfo.toString().trim());
        }
        
        // Summary statistics
        System.out.println("\nCOORDINATOR: Summary");
        System.out.println("-".repeat(50));
        System.out.println("Total nodes: " + environment.getAllNodes().size());
        System.out.println("Nodes colored: " + countColoredNodes());
        System.out.println("Nodes with conflicts: " + countConflicts());
        System.out.println("Valid coloring: " + environment.isValidColoring());
        System.out.println("Experiment iterations: " + iteration);
        
        // Color usage
        Map<Integer, List<String>> colorGroups = new HashMap<>();
        for (GraphNode node : environment.getAllNodes()) {
            if (node.isColored()) {
                colorGroups.computeIfAbsent(node.getColor(), k -> new ArrayList<>())
                          .add(node.getId());
            }
        }
        
        if (!colorGroups.isEmpty()) {
            System.out.println("\nColor usage:");
            List<Integer> colors = new ArrayList<>(colorGroups.keySet());
            Collections.sort(colors);
            for (int color : colors) {
                System.out.printf("  Color %d: %s\n", color, colorGroups.get(color));
            }
        }
    }
    
    private int countColoredNodes() {
        int count = 0;
        for (GraphNode node : environment.getAllNodes()) {
            if (node.isColored()) count++;
        }
        return count;
    }
    
    private int countConflicts() {
        int count = 0;
        for (GraphNode node : environment.getAllNodes()) {
            if (environment.hasConflict(node.getId())) count++;
        }
        return count;
    }
    
    private void stopAllAgents() {
        for (String agentId : registeredAgents) {
            Message stopMsg = new Message("COORDINATOR", "STOP", "Perfect coloring achieved");
            messageBus.sendMessage(agentId, stopMsg);
        }
        System.out.println("\nCOORDINATOR: Stop signals sent to all " + 
                         registeredAgents.size() + " agents");
        
        // Also stop itself after a short delay
        new Thread(() -> {
            try {
                Thread.sleep(500);
                stop();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void processMessages() throws InterruptedException {
        Message message;
        while ((message = messageBus.receiveMessage("COORDINATOR", 10)) != null) {
            onMessage(message);
        }
    }
    
    @Override
    public void onMessage(Message message) {
        if ("REGISTER".equals(message.getType())) {
            String agentId = (String) message.getContent();
            registeredAgents.add(agentId);
            System.out.println("COORDINATOR: Registered agent for node " + agentId);
        }
    }
    
    public void registerAgent(String agentId) {
        Message msg = new Message(agentId, "REGISTER", agentId);
        messageBus.sendMessage("COORDINATOR", msg);
    }
}
