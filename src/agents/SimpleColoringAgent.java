package agents;

import graph.GraphEnvironment;
import graph.GraphNode;
import messaging.Message;
import messaging.MessageBus;

import java.util.*;
import java.util.concurrent.*;

public class SimpleColoringAgent implements NodeAgent, Runnable {
    private final String agentId;
    private final GraphEnvironment environment;
    private final MessageBus messageBus;
    private volatile boolean running;
    private Thread agentThread;
    
    private final Set<Integer> neighborColors;
    private int myColor;
    private final Random random;
    
    public SimpleColoringAgent(String agentId, GraphEnvironment environment) {
        this.agentId = agentId;
        this.environment = environment;
        this.messageBus = MessageBus.getInstance();
        this.neighborColors = new HashSet<>();
        this.myColor = -1;
        this.random = new Random();
        this.running = false;
    }
    
    @Override public String getAgentId() { return agentId; }
    @Override public boolean isRunning() { return running; }
    
    @Override
    public void start() {
        if (!running) {
            running = true;
            agentThread = new Thread(this, agentId);
            agentThread.start();
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
        // Run until stopped by coordinator
        while (running) {
            try {
                // 1. Check neighbors' colors
                gatherNeighborColors();
                
                // 2. Choose or adjust color
                if (!environment.getNode(agentId).isColored()) {
                    chooseColor();
                } else if (environment.hasConflict(agentId)) {
                    resolveConflict();
                }
                
                // 3. Broadcast color to neighbors
                broadcastMyColor();
                
                // 4. Process incoming messages
                processMessages();
                
                // Random delay to simulate real asynchrony
                Thread.sleep(80 + random.nextInt(120));
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
    
    private void gatherNeighborColors() {
        GraphNode node = environment.getNode(agentId);
        if (node == null) return;
        
        neighborColors.clear();
        for (String neighborId : node.getNeighborIds()) {
            GraphNode neighbor = environment.getNode(neighborId);
            if (neighbor != null && neighbor.isColored()) {
                neighborColors.add(neighbor.getColor());
            }
        }
    }
    
    private void chooseColor() {
        List<Integer> availableColors = environment.getAvailableColors();
        
        // Find first available color not used by neighbors
        for (int color : availableColors) {
            if (!neighborColors.contains(color)) {
                myColor = color;
                environment.getNode(agentId).setColor(color);
                return;
            }
        }
        
        // If all colors are used by neighbors, pick random
        myColor = availableColors.get(random.nextInt(availableColors.size()));
        environment.getNode(agentId).setColor(myColor);
    }
    
    private void broadcastMyColor() {
        if (myColor == -1) return;
        
        GraphNode node = environment.getNode(agentId);
        if (node == null) return;
        
        for (String neighborId : node.getNeighborIds()) {
            Message msg = new Message(agentId, "COLOR_UPDATE", myColor);
            messageBus.sendMessage(neighborId, msg);
        }
    }
    
    private void resolveConflict() {
        if (!environment.hasConflict(agentId)) return;
        
        List<Integer> availableColors = environment.getAvailableColors();
        
        // Try to find a conflict-free color
        for (int color : availableColors) {
            if (!neighborColors.contains(color) && color != myColor) {
                myColor = color;
                environment.getNode(agentId).setColor(color);
                return;
            }
        }
        
        // If no conflict-free color, reset
        environment.getNode(agentId).resetColor();
        myColor = -1;
    }
    
    private void processMessages() throws InterruptedException {
        Message message;
        while ((message = messageBus.receiveMessage(agentId, 10)) != null) {
            onMessage(message);
        }
    }
    
    @Override
    public void onMessage(Message message) {
        if ("COLOR_UPDATE".equals(message.getType())) {
            int neighborColor = (int) message.getContent();
            neighborColors.add(neighborColor);
        } else if ("STOP".equals(message.getType())) {
            stop();  // Stop when coordinator tells us to
        }
    }
    
    public int getCurrentColor() { return myColor; }
}
