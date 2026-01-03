package main;

import graph.GraphEnvironment;
import agents.SimpleColoringAgent;
import agents.CoordinatorAgent;

import java.util.*;
import java.util.concurrent.*;

public class VerboseGraphColoring {
    
    private static GraphEnvironment createComplexExperimentGraph() {
        System.out.println("\n" + "█".repeat(80));
        System.out.println("█               DISTRIBUTED GRAPH COLORING EXPERIMENT                █");
        System.out.println("█                  (Multi-Agent System Simulation)                   █");
        System.out.println("█".repeat(80));
        
        // Create a challenging but interesting graph
        GraphEnvironment env = new GraphEnvironment(4); // 4 colors available
        
        System.out.println("\nGRAPH CONSTRUCTION:");
        System.out.println("-".repeat(80));
        
        // Create 8 nodes arranged in two interconnected clusters
        String[] nodes = {"Alpha", "Bravo", "Charlie", "Delta", 
                         "Echo", "Foxtrot", "Golf", "Hotel"};
        
        for (String node : nodes) {
            env.addNode(node);
            System.out.println("  Added node: " + node);
        }
        
        System.out.println("\nEDGE CONSTRUCTION (Creating connections):");
        System.out.println("-".repeat(80));
        
        // Cluster 1: Alpha, Bravo, Charlie, Delta (tightly connected)
        String[] cluster1 = {"Alpha", "Bravo", "Charlie", "Delta"};
        for (int i = 0; i < cluster1.length; i++) {
            for (int j = i + 1; j < cluster1.length; j++) {
                env.addEdge(cluster1[i], cluster1[j]);
                System.out.println("  " + cluster1[i] + " ↔ " + cluster1[j] + 
                                 " (Cluster 1 internal)");
            }
        }
        
        // Cluster 2: Echo, Foxtrot, Golf, Hotel
        String[] cluster2 = {"Echo", "Foxtrot", "Golf", "Hotel"};
        for (int i = 0; i < cluster2.length; i++) {
            for (int j = i + 1; j < cluster2.length; j++) {
                env.addEdge(cluster2[i], cluster2[j]);
                System.out.println("  " + cluster2[i] + " ↔ " + cluster2[j] + 
                                 " (Cluster 2 internal)");
            }
        }
        
        // Cross-cluster connections (creating a bridge)
        env.addEdge("Alpha", "Echo");
        env.addEdge("Bravo", "Foxtrot");
        env.addEdge("Charlie", "Golf");
        env.addEdge("Delta", "Hotel");
        
        System.out.println("\n  Alpha ↔ Echo    (Cross-cluster bridge)");
        System.out.println("  Bravo ↔ Foxtrot (Cross-cluster bridge)");
        System.out.println("  Charlie ↔ Golf  (Cross-cluster bridge)");
        System.out.println("  Delta ↔ Hotel   (Cross-cluster bridge)");
        
        // Add some extra connections for complexity
        env.addEdge("Alpha", "Golf");
        env.addEdge("Charlie", "Hotel");
        
        System.out.println("  Alpha ↔ Golf    (Extra complexity)");
        System.out.println("  Charlie ↔ Hotel (Extra complexity)");
        
        System.out.println("\nGRAPH STATISTICS:");
        System.out.println("-".repeat(80));
        System.out.println("  Total nodes: 8");
        System.out.println("  Total edges: 22");
        System.out.println("  Available colors: 4");
        System.out.println("  Minimum colors required (chromatic number): 4");
        
        System.out.println("\n" + "█".repeat(80));
        System.out.println("█              EXPERIMENT SETUP COMPLETE                           █");
        System.out.println("█".repeat(80));
        
        return env;
    }
    
    private static void runVerboseExperiment() throws InterruptedException {
        GraphEnvironment env = createComplexExperimentGraph();
        
        System.out.println("\n\nAGENT INITIALIZATION:");
        System.out.println("=".repeat(80));
        
        // Create coordinator
        CoordinatorAgent coordinator = new CoordinatorAgent(env);
        
        // Create and register all agents
        Map<String, SimpleColoringAgent> agents = new HashMap<>();
        List<String> nodeIds = new ArrayList<>();
        
        for (graph.GraphNode node : env.getAllNodes()) {
            String nodeId = node.getId();
            nodeIds.add(nodeId);
            SimpleColoringAgent agent = new SimpleColoringAgent(nodeId, env);
            agents.put(nodeId, agent);
            coordinator.registerAgent(nodeId);
            
            System.out.println("  Created agent for node: " + nodeId);
        }
        
        System.out.println("\nTotal agents created: " + agents.size());
        
        // Start coordinator
        System.out.println("\n\nSTARTING THE SYSTEM:");
        System.out.println("=".repeat(80));
        coordinator.start();
        
        // Wait a moment before starting agents
        Thread.sleep(1000);
        
        // Start all agents in random order (simulating distributed startup)
        System.out.println("\n\nAGENT ACTIVATION SEQUENCE:");
        System.out.println("-".repeat(80));
        
        ExecutorService executor = Executors.newCachedThreadPool();
        Random random = new Random();
        List<String> startupOrder = new ArrayList<>(nodeIds);
        Collections.shuffle(startupOrder);
        
        for (String nodeId : startupOrder) {
            SimpleColoringAgent agent = agents.get(nodeId);
            
            int delay = random.nextInt(500);
            System.out.printf("  %s will start in %dms%n", nodeId, delay);
            
            executor.submit(() -> {
                try {
                    Thread.sleep(delay);
                    System.out.println("\n  → ACTIVATING: " + nodeId);
                    agent.start();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        System.out.println("\n" + "▷".repeat(80));
        System.out.println("▷                    EXPERIMENT IN PROGRESS                       ▷");
        System.out.println("▷                (Agents will now begin coloring)                 ▷");
        System.out.println("▷".repeat(80) + "\n");
        
        // Let the system run
        Thread.sleep(15000); // 15 seconds runtime
        
        System.out.println("\n\n" + "◼".repeat(80));
        System.out.println("◼                    EXPERIMENT TIME LIMIT REACHED                ◼");
        System.out.println("◼".repeat(80));
        
        // Stop everything
        coordinator.stop();
        agents.values().forEach(agent -> {
            if (agent.isRunning()) {
                agent.stop();
            }
        });
        
        executor.shutdown();
        if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
        
        // Final analysis
        printFinalAnalysis(env, agents);
    }
    
    private static void printFinalAnalysis(GraphEnvironment env, 
                                         Map<String, SimpleColoringAgent> agents) {
        System.out.println("\n\n" + "📊".repeat(40));
        System.out.println("📊                      FINAL ANALYSIS                           📊");
        System.out.println("📊".repeat(40));
        
        System.out.println("\nCOLORING STATUS:");
        System.out.println("-".repeat(80));
        
        // Check each node's status
        for (graph.GraphNode node : env.getAllNodes().stream()
                .sorted(Comparator.comparing(graph.GraphNode::getId))
                .toList()) {
            
            String agentId = node.getId();
            SimpleColoringAgent agent = agents.get(agentId);
            
            String status;
            if (!node.isColored()) {
                status = "UNCOLORED";
            } else if (env.hasConflict(agentId)) {
                status = "CONFLICT (color " + node.getColor() + ")";
            } else {
                status = "OK (color " + node.getColor() + ")";
            }
            
            System.out.printf("  %-10s: %-25s | Steps: %3d%n", 
                agentId, status, agent.getStepCount());
        }
        
        System.out.println("\nSYSTEM METRICS:");
        System.out.println("-".repeat(80));
        System.out.println("  Valid coloring achieved: " + 
                          (env.isValidColoring() ? "✅ YES" : "❌ NO"));
        System.out.println("  All nodes colored: " + 
                          (env.isFullyColored() ? "✅ YES" : "❌ NO"));
        
        if (!env.isValidColoring()) {
            int conflicts = 0;
            for (graph.GraphNode node : env.getAllNodes()) {
                if (env.hasConflict(node.getId())) conflicts++;
            }
            System.out.println("  Active conflicts: " + conflicts);
        }
        
        // Color usage statistics
        Set<Integer> colorsUsed = new HashSet<>();
        for (graph.GraphNode node : env.getAllNodes()) {
            if (node.isColored()) {
                colorsUsed.add(node.getColor());
            }
        }
        
        System.out.println("  Colors used: " + colorsUsed.size() + 
                          " out of " + env.getAvailableColors().size() + " available");
        
        // Agent performance
        System.out.println("\nAGENT PERFORMANCE SUMMARY:");
        System.out.println("-".repeat(80));
        
        int totalSteps = agents.values().stream()
            .mapToInt(SimpleColoringAgent::getStepCount)
            .sum();
        double avgSteps = totalSteps / (double) agents.size();
        
        System.out.printf("  Total agent steps: %d%n", totalSteps);
        System.out.printf("  Average steps per agent: %.1f%n", avgSteps);
        
        // Find most/least active agents
        String mostActive = agents.entrySet().stream()
            .max(Comparator.comparingInt(e -> e.getValue().getStepCount()))
            .map(Map.Entry::getKey)
            .orElse("None");
        
        String leastActive = agents.entrySet().stream()
            .min(Comparator.comparingInt(e -> e.getValue().getStepCount()))
            .map(Map.Entry::getKey)
            .orElse("None");
        
        System.out.println("  Most active agent: " + mostActive + 
                          " (" + agents.get(mostActive).getStepCount() + " steps)");
        System.out.println("  Least active agent: " + leastActive + 
                          " (" + agents.get(leastActive).getStepCount() + " steps)");
        
        System.out.println("\n" + "🎯".repeat(40));
        System.out.println("🎯                 EXPERIMENT COMPLETE                           🎯");
        System.out.println("🎯".repeat(40));
        
        if (env.isValidColoring()) {
            System.out.println("\n✨ SUCCESS: The multi-agent system successfully found a valid coloring!");
        } else {
            System.out.println("\n⚠️  PARTIAL SUCCESS: The system converged but conflicts remain.");
            System.out.println("   This demonstrates the challenge of distributed coordination.");
        }
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("Initializing Distributed Graph Coloring Experiment...\n");
            Thread.sleep(1000);
            
            runVerboseExperiment();
            
        } catch (InterruptedException e) {
            System.err.println("Experiment was interrupted!");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error during experiment: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
