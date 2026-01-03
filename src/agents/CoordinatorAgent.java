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
    
    public CoordinatorAgent(GraphEnvironment environment) {
        this.environment = environment;
        this.messageBus = MessageBus.getInstance();
        this.registeredAgents = new HashSet<>();
    }
    
    @Override public String getAgentId() { return "coordinator"; }
    @Override public boolean isRunning() { return running; }
    
    @Override
    public void start() {
        if (!running) {
            running = true;
            agentThread = new Thread(this, "Coordinator");
            agentThread.start();
            System.out.println("Coordinator: Started monitoring");
        }
    }
    
    @Override
    public void stop() {
        running = false;
        if (agentThread != null) {
            agentThread.interrupt();
        }
        System.out.println("Coordinator: Stopped");
    }
    
    @Override
    public void run() {
        int iteration = 0;
        while (running && iteration < 100) {
            iteration++;
            
            try {
                // Check if we have a valid coloring
                if (environment.isFullyColored() && environment.isValidColoring()) {
                    System.out.println("\nCoordinator: SUCCESS! Valid coloring achieved in " + iteration + " iterations");
                    printResults();
                    stopAllAgents();
                    break;
                }
                
                // Print progress every 10 iterations
                if (iteration % 10 == 0) {
                    printProgress(iteration);
                }
                
                Thread.sleep(200);
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        if (iteration >= 100) {
            System.out.println("\nCoordinator: Reached maximum iterations");
            printResults();
        }
    }
    
    private void printProgress(int iteration) {
        int colored = 0;
        int conflicts = 0;
        
        for (GraphNode node : environment.getAllNodes()) {
            if (node.isColored()) colored++;
            if (environment.hasConflict(node.getId())) conflicts++;
        }
        
        System.out.printf("Coordinator [Iter %d]: %d/%d colored, %d conflicts%n",
            iteration, colored, environment.getAllNodes().size(), conflicts);
    }
    
    private void printResults() {
        System.out.println("\n=== FINAL RESULTS ===");
        System.out.println("Valid coloring: " + environment.isValidColoring());
        System.out.println("All nodes colored: " + environment.isFullyColored());
        
        // Show each node's color
        for (GraphNode node : environment.getAllNodes()) {
            System.out.printf("  %s: Color %s%s%n",
                node.getId(),
                node.isColored() ? node.getColor() : "none",
                environment.hasConflict(node.getId()) ? " (CONFLICT)" : ""
            );
        }
        
        // Show color distribution
        Map<Integer, List<String>> colorGroups = new HashMap<>();
        for (GraphNode node : environment.getAllNodes()) {
            if (node.isColored()) {
                colorGroups.computeIfAbsent(node.getColor(), k -> new ArrayList<>())
                          .add(node.getId());
            }
        }
        
        System.out.println("\nColor distribution:");
        for (Map.Entry<Integer, List<String>> entry : colorGroups.entrySet()) {
            System.out.println("  Color " + entry.getKey() + ": " + entry.getValue());
        }
    }
    
    private void stopAllAgents() {
        for (String agentId : registeredAgents) {
            Message stopMsg = new Message("coordinator", "STOP", "Experiment complete");
            messageBus.sendMessage(agentId, stopMsg);
        }
    }
    
    @Override
    public void onMessage(Message message) {
        if ("REGISTER".equals(message.getType())) {
            String agentId = (String) message.getContent();
            registeredAgents.add(agentId);
        }
    }
    
    public void registerAgent(String agentId) {
        Message msg = new Message(agentId, "REGISTER", agentId);
        messageBus.sendMessage("coordinator", msg);
    }
}
