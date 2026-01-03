package main;

import graph.GraphEnvironment;
import agents.SimpleColoringAgent;
import agents.CoordinatorAgent;

import java.util.*;
import java.util.concurrent.*;

public class VerboseGraphColoring {
    
    public static GraphEnvironment createComplexExperimentGraph() {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("DISTRIBUTED GRAPH COLORING - MULTI-AGENT SYSTEM");
        System.out.println("=".repeat(70));
        
        // Create a challenging graph
        GraphEnvironment env = new GraphEnvironment(4); // 4 colors available
        
        // Create 8 nodes arranged in two interconnected clusters
        String[] nodes = {"Alpha", "Bravo", "Charlie", "Delta", 
                         "Echo", "Foxtrot", "Golf", "Hotel"};
        
        for (String node : nodes) {
            env.addNode(node);
        }
        
        // Cluster 1: Alpha, Bravo, Charlie, Delta (tightly connected)
        String[] cluster1 = {"Alpha", "Bravo", "Charlie", "Delta"};
        for (int i = 0; i < cluster1.length; i++) {
            for (int j = i + 1; j < cluster1.length; j++) {
                env.addEdge(cluster1[i], cluster1[j]);
            }
        }
        
        // Cluster 2: Echo, Foxtrot, Golf, Hotel
        String[] cluster2 = {"Echo", "Foxtrot", "Golf", "Hotel"};
        for (int i = 0; i < cluster2.length; i++) {
            for (int j = i + 1; j < cluster2.length; j++) {
                env.addEdge(cluster2[i], cluster2[j]);
            }
        }
        
        // Cross-cluster connections (creating a bridge)
        env.addEdge("Alpha", "Echo");
        env.addEdge("Bravo", "Foxtrot");
        env.addEdge("Charlie", "Golf");
        env.addEdge("Delta", "Hotel");
        
        // Add some extra connections for complexity
        env.addEdge("Alpha", "Golf");
        env.addEdge("Charlie", "Hotel");
        
        System.out.println("Graph created with 8 nodes in two interconnected clusters");
        System.out.println("Total edges: 22");
        System.out.println("Available colors: 4");
        System.out.println("Minimum colors required (chromatic number): 4");
        System.out.println("=".repeat(70) + "\n");
        
        return env;
    }
    
    public static void runExperiment() throws InterruptedException {
        GraphEnvironment env = createComplexExperimentGraph();
        
        // Create coordinator
        CoordinatorAgent coordinator = new CoordinatorAgent(env);
        
        // Create and register all agents
        Map<String, SimpleColoringAgent> agents = new HashMap<>();
        for (graph.GraphNode node : env.getAllNodes()) {
            String nodeId = node.getId();
            SimpleColoringAgent agent = new SimpleColoringAgent(nodeId, env);
            agents.put(nodeId, agent);
            coordinator.registerAgent(nodeId);
        }
        
        // Start coordinator
        coordinator.start();
        
        // Wait a moment before starting agents
        Thread.sleep(500);
        
        // Start all agents in random order
        System.out.println("\nStarting all agents simultaneously...\n");
        ExecutorService executor = Executors.newCachedThreadPool();
        Random random = new Random();
        
        // Record start time
        long startTime = System.currentTimeMillis();
        
        // Start agents with staggered delays
        List<Future<?>> futures = new ArrayList<>();
        for (SimpleColoringAgent agent : agents.values()) {
            Future<?> future = executor.submit(() -> {
                try {
                    // Random delay between 0-400ms to simulate real distributed startup
                    Thread.sleep(random.nextInt(400));
                    agent.start();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            futures.add(future);
        }
        
        // Wait for all agents to start
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        // Wait for coordinator to find solution and stop everything
        System.out.println("\n" + "~".repeat(70));
        System.out.println("All agents started. Waiting for solution...");
        System.out.println("~".repeat(70) + "\n");
        
        // Monitor and wait for completion
        int maxWaitSeconds = 30;
        for (int i = 0; i < maxWaitSeconds * 10; i++) {
            Thread.sleep(100);
            
            // Check if coordinator is still running
            if (!coordinator.isRunning()) {
                System.out.println("\nCoordinator has terminated (solution found).");
                break;
            }
            
            // Check if we've waited too long
            if (i == maxWaitSeconds * 10 - 1) {
                System.out.println("\n" + "!".repeat(70));
                System.out.println("Timeout reached! Stopping experiment manually.");
                System.out.println("!" + " ".repeat(68) + "!");
                coordinator.stop();
            }
        }
        
        // Stop any remaining agents
        for (SimpleColoringAgent agent : agents.values()) {
            if (agent.isRunning()) {
                agent.stop();
            }
        }
        
        // Calculate elapsed time
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        
        // Shutdown executor
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        // Final analysis
        printFinalAnalysis(env, agents, elapsedTime);
    }
    
    private static void printFinalAnalysis(GraphEnvironment env, 
                                         Map<String, SimpleColoringAgent> agents,
                                         long elapsedTime) {
        System.out.println("\n" + "📊".repeat(40));
        System.out.println("📊                      FINAL ANALYSIS                           📊");
        System.out.println("📊".repeat(40));
        
        System.out.println("\nCOLORING STATUS:");
        System.out.println("-".repeat(80));
        
        // Check each node's status
        List<graph.GraphNode> nodes = new ArrayList<>(env.getAllNodes());
        nodes.sort((n1, n2) -> n1.getId().compareTo(n2.getId()));
        
        for (graph.GraphNode node : nodes) {
            String agentId = node.getId();
            
            String status;
            if (!node.isColored()) {
                status = "UNCOLORED";
            } else if (env.hasConflict(agentId)) {
                status = "CONFLICT (color " + node.getColor() + ")";
            } else {
                status = "OK (color " + node.getColor() + ")";
            }
            
            System.out.printf("  %-10s: %-25s%n", agentId, status);
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
        
        // Performance metrics
        System.out.println("\nPERFORMANCE METRICS:");
        System.out.println("-".repeat(80));
        System.out.printf("  Total time: %.2f seconds%n", elapsedTime / 1000.0);
        System.out.println("  Total agents: " + agents.size());
        
        // Count running vs stopped agents
        int runningAgents = 0;
        for (SimpleColoringAgent agent : agents.values()) {
            if (agent.isRunning()) runningAgents++;
        }
        System.out.println("  Agents still running: " + runningAgents);
        
        // Graph statistics
        System.out.println("\nGRAPH STATISTICS:");
        System.out.println("-".repeat(80));
        System.out.println("  Total nodes: " + env.getAllNodes().size());
        
        // Calculate average degree
        int totalEdges = 0;
        for (graph.GraphNode node : env.getAllNodes()) {
            totalEdges += node.getNeighborIds().size();
        }
        double avgDegree = totalEdges / (double) env.getAllNodes().size();
        System.out.printf("  Average node degree: %.2f%n", avgDegree);
        
        // Show the actual coloring
        System.out.println("\nFINAL COLORING:");
        System.out.println("-".repeat(80));
        
        // Group nodes by color
        Map<Integer, List<String>> colorGroups = new TreeMap<>();
        for (graph.GraphNode node : env.getAllNodes()) {
            if (node.isColored()) {
                colorGroups.computeIfAbsent(node.getColor(), k -> new ArrayList<>())
                          .add(node.getId());
            }
        }
        
        if (!colorGroups.isEmpty()) {
            for (Map.Entry<Integer, List<String>> entry : colorGroups.entrySet()) {
                Collections.sort(entry.getValue());
                System.out.printf("  Color %d: %s%n", entry.getKey(), entry.getValue());
            }
        } else {
            System.out.println("  No nodes are colored");
        }
        
        System.out.println("\n" + "🎯".repeat(40));
        System.out.println("🎯                 EXPERIMENT COMPLETE                           🎯");
        
        if (env.isValidColoring()) {
            System.out.println("🎯                ✅ SUCCESS: Valid coloring found!              🎯");
        } else {
            System.out.println("🎯                ⚠️  Partial success - conflicts remain         🎯");
        }
        
        System.out.println("🎯".repeat(40));
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("Initializing Distributed Graph Coloring Experiment...");
            Thread.sleep(1000);
            
            runExperiment();
            
        } catch (InterruptedException e) {
            System.err.println("Experiment was interrupted!");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Error during experiment: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
