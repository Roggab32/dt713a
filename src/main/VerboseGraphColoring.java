package main;

import graph.GraphEnvironment;
import agents.SimpleColoringAgent;
import agents.CoordinatorAgent;

import java.util.*;
import java.util.concurrent.*;

public class VerboseGraphColoring {
    
    public static GraphEnvironment createLargeExperimentGraph() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("DISTRIBUTED GRAPH COLORING - MULTI-AGENT SYSTEM");
        System.out.println("=".repeat(80));
        
        GraphEnvironment env = new GraphEnvironment(5);
        
        String[] nodes = {
            "Alpha", "Bravo", "Charlie", "Delta", 
            "Echo", "Foxtrot", "Golf", "Hotel",
            "India", "Juliett", "Kilo", "Lima",
            "Mike", "November", "Oscar", "Papa"
        };
        
        System.out.print("Creating graph with 16 nodes: ");
        for (String node : nodes) {
            env.addNode(node);
        }
        System.out.println(String.join(", ", nodes));
        
        System.out.println("\nCreating cluster structure...");
        String[] cluster1 = {"Alpha", "Bravo", "Charlie", "Delta"};
        createFullyConnectedCluster(env, cluster1, "Cluster 1");
        String[] cluster2 = {"Echo", "Foxtrot", "Golf", "Hotel"};
        createFullyConnectedCluster(env, cluster2, "Cluster 2");
        String[] cluster3 = {"India", "Juliett", "Kilo", "Lima"};
        createFullyConnectedCluster(env, cluster3, "Cluster 3");
        String[] cluster4 = {"Mike", "November", "Oscar", "Papa"};
        createFullyConnectedCluster(env, cluster4, "Cluster 4");
        
        System.out.println("\nCreating inter-cluster connections (ring topology)...");
        
        connectClusters(env, cluster1, cluster2, "Cluster 1 ↔ Cluster 2");
        connectClusters(env, cluster2, cluster3, "Cluster 2 ↔ Cluster 3");
        connectClusters(env, cluster3, cluster4, "Cluster 3 ↔ Cluster 4");
        connectClusters(env, cluster4, cluster1, "Cluster 4 ↔ Cluster 1");
        
        System.out.println("\nAdding cross-ring connections for increased complexity...");
        env.addEdge("Alpha", "India");
        env.addEdge("Bravo", "Juliett");
        env.addEdge("Charlie", "Kilo");
        env.addEdge("Delta", "Lima");
        env.addEdge("Echo", "Mike");
        env.addEdge("Foxtrot", "November");
        env.addEdge("Golf", "Oscar");
        env.addEdge("Hotel", "Papa");
        
        Random rand = new Random(42);
        System.out.println("\nAdding random edges...");
        for (int i = 0; i < 8; i++) {
            String node1 = nodes[rand.nextInt(nodes.length)];
            String node2 = nodes[rand.nextInt(nodes.length)];
            if (!node1.equals(node2) && !areNeighbors(env, node1, node2)) {
                env.addEdge(node1, node2);
                System.out.printf("  Added random edge: %s ↔ %s%n", node1, node2);
            }
        }
        
        System.out.println("\n" + "-".repeat(80));
        System.out.println("GRAPH STATISTICS:");
        System.out.println("-".repeat(80));
        
        int totalNodes = env.getAllNodes().size();
        int totalEdges = calculateTotalEdges(env);
        double density = calculateDensity(env, totalNodes, totalEdges);
        int maxDegree = calculateMaxDegree(env);
        int minDegree = calculateMinDegree(env);
        double avgDegree = calculateAverageDegree(env, totalEdges, totalNodes);
        
        System.out.println("  Total nodes: " + totalNodes);
        System.out.println("  Total edges: " + totalEdges);
        System.out.printf("  Graph density: %.3f%n", density);
        System.out.println("  Maximum degree: " + maxDegree);
        System.out.println("  Minimum degree: " + minDegree);
        System.out.printf("  Average degree: %.2f%n", avgDegree);
        System.out.println("  Available colors: " + env.getAvailableColors().size());
        
        int chromaticEstimate = estimateChromaticNumber(env, maxDegree);
        System.out.println("  Estimated chromatic number: " + chromaticEstimate + 
                         " (minimum colors needed)");
        
        System.out.println("=".repeat(80) + "\n");
        
        return env;
    }
    
    private static void createFullyConnectedCluster(GraphEnvironment env, 
                                                   String[] cluster, 
                                                   String clusterName) {
        System.out.printf("  %s (%s): ", clusterName, String.join(", ", cluster));
        int edgesAdded = 0;
        for (int i = 0; i < cluster.length; i++) {
            for (int j = i + 1; j < cluster.length; j++) {
                env.addEdge(cluster[i], cluster[j]);
                edgesAdded++;
            }
        }
        System.out.printf("%d edges (fully connected)%n", edgesAdded);
    }
    
    private static void connectClusters(GraphEnvironment env, 
                                       String[] cluster1, 
                                       String[] cluster2,
                                       String connectionName) {
        System.out.printf("  %s: ", connectionName);
        for (int i = 0; i < Math.min(cluster1.length, cluster2.length); i++) {
            env.addEdge(cluster1[i], cluster2[i]);
        }
        System.out.printf("%d bridge edges%n", Math.min(cluster1.length, cluster2.length));
    }
    
    private static boolean areNeighbors(GraphEnvironment env, String node1, String node2) {
        graph.GraphNode n1 = env.getNode(node1);
        return n1 != null && n1.getNeighborIds().contains(node2);
    }
    
    private static int calculateTotalEdges(GraphEnvironment env) {
        int totalEdges = 0;
        for (graph.GraphNode node : env.getAllNodes()) {
            totalEdges += node.getNeighborIds().size();
        }
        return totalEdges / 2;
    }
    
    private static double calculateDensity(GraphEnvironment env, int nodes, int edges) {
        int maxPossibleEdges = nodes * (nodes - 1) / 2;
        return edges / (double) maxPossibleEdges;
    }
    
    private static int calculateMaxDegree(GraphEnvironment env) {
        int maxDegree = 0;
        for (graph.GraphNode node : env.getAllNodes()) {
            maxDegree = Math.max(maxDegree, node.getNeighborIds().size());
        }
        return maxDegree;
    }
    
    private static int calculateMinDegree(GraphEnvironment env) {
        int minDegree = Integer.MAX_VALUE;
        for (graph.GraphNode node : env.getAllNodes()) {
            minDegree = Math.min(minDegree, node.getNeighborIds().size());
        }
        return minDegree;
    }
    
    private static double calculateAverageDegree(GraphEnvironment env, int edges, int nodes) {
        return (2.0 * edges) / nodes;
    }
    
    private static int estimateChromaticNumber(GraphEnvironment env, int maxDegree) {
        //Simple estimate: chromatic number \le maxDegree + 1 (Brooks' theorem)
        return Math.min(maxDegree + 1, 5);
    }
    
    public static void runExperiment() throws InterruptedException {
        GraphEnvironment env = createLargeExperimentGraph();
        
        CoordinatorAgent coordinator = new CoordinatorAgent(env);
        
        Map<String, SimpleColoringAgent> agents = new HashMap<>();
        System.out.print("Creating agents: ");
        List<String> nodeIds = new ArrayList<>();
        for (graph.GraphNode node : env.getAllNodes()) {
            String nodeId = node.getId();
            nodeIds.add(nodeId);
            SimpleColoringAgent agent = new SimpleColoringAgent(nodeId, env);
            agents.put(nodeId, agent);
            coordinator.registerAgent(nodeId);
        }
        System.out.println(String.join(", ", nodeIds));
        System.out.println("Total agents created: " + agents.size() + "\n");
        
        coordinator.start();
        
        Thread.sleep(800);
        
        System.out.println("Starting agents with staggered delays...\n");
        ExecutorService executor = Executors.newFixedThreadPool(agents.size());
        Random random = new Random();
        
        long startTime = System.currentTimeMillis();
        
        List<Future<?>> futures = new ArrayList<>();
        int agentCounter = 0;
        for (SimpleColoringAgent agent : agents.values()) {
            final int agentNum = ++agentCounter;
            Future<?> future = executor.submit(() -> {
                try {
                    int delay = random.nextInt(800);
                    System.out.printf("Agent %2d/%2d (%s): starting in %dms%n", 
                        agentNum, agents.size(), agent.getAgentId(), delay);
                    Thread.sleep(delay);
                    agent.start();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            futures.add(future);
        }
        
        for (Future<?> future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                // Ignore
            }
        }
        
        System.out.println("\n" + "~".repeat(80));
        System.out.println("ALL AGENTS STARTED - SYSTEM IS NOW RUNNING");
        System.out.println("Waiting for distributed coloring solution...");
        System.out.println("~".repeat(80) + "\n");
        
        int maxWaitSeconds = 60;
        boolean solutionFound = false;
        
        for (int second = 0; second < maxWaitSeconds; second++) {
            Thread.sleep(1000);
            
            if (!coordinator.isRunning()) {
                System.out.println("\nCoordinator terminated - SOLUTION FOUND!");
                solutionFound = true;
                break;
            }
            
            if ((second + 1) % 10 == 0) {
                int colored = 0;
                int conflicts = 0;
                for (graph.GraphNode node : env.getAllNodes()) {
                    if (node.isColored()) colored++;
                    if (env.hasConflict(node.getId())) conflicts++;
                }
                
                System.out.printf("[%02d sec] Status: %d/%d colored, %d conflicts%n",
                    second + 1, colored, env.getAllNodes().size(), conflicts);
            }
            
            if (second == maxWaitSeconds - 1) {
                System.out.println("\n" + "!".repeat(80));
                System.out.println("TIMEOUT REACHED (" + maxWaitSeconds + " seconds)!");
                System.out.println("Stopping experiment manually...");
                System.out.println("!" + " ".repeat(78) + "!");
                coordinator.stop();
            }
        }
        
        int runningAgents = 0;
        for (SimpleColoringAgent agent : agents.values()) {
            if (agent.isRunning()) {
                agent.stop();
                runningAgents++;
            }
        }
        if (runningAgents > 0) {
            System.out.println("Stopped " + runningAgents + " agents that were still running");
        }
        
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        
        executor.shutdown();
        try {
            if (!executor.awaitTermination(3, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        
        printFinalAnalysis(env, agents, elapsedTime, solutionFound);
    }
    
    private static void printFinalAnalysis(GraphEnvironment env, 
                                         Map<String, SimpleColoringAgent> agents,
                                         long elapsedTime,
                                         boolean solutionFound) {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("=                  COMPREHENSIVE FINAL ANALYSIS                =");
        System.out.println("=".repeat(40));
        
        System.out.println("\nEXPERIMENT SUMMARY:");
        System.out.println("-".repeat(80));
        System.out.printf("  Total time: %.2f seconds%n", elapsedTime / 1000.0);
        System.out.println("  Solution found: " + (solutionFound ? "YES" : "NO"));
        System.out.println("  Total agents: " + agents.size());
        
        System.out.println("\nCOLORING RESULTS:");
        System.out.println("-".repeat(80));
        System.out.println("  Valid coloring achieved: " + 
                          (env.isValidColoring() ? "YES" : "NO"));
        System.out.println("  All nodes colored: " + 
                          (env.isFullyColored() ? "YES" : "NO"));
        
        if (!env.isValidColoring()) {
            int conflicts = 0;
            for (graph.GraphNode node : env.getAllNodes()) {
                if (env.hasConflict(node.getId())) conflicts++;
            }
            System.out.println("  Active conflicts: " + conflicts);
        }
        
        Set<Integer> colorsUsed = new HashSet<>();
        Map<Integer, Integer> colorFrequency = new HashMap<>();
        for (graph.GraphNode node : env.getAllNodes()) {
            if (node.isColored()) {
                int color = node.getColor();
                colorsUsed.add(color);
                colorFrequency.put(color, colorFrequency.getOrDefault(color, 0) + 1);
            }
        }
        
        System.out.println("\nCOLOR USAGE ANALYSIS:");
        System.out.println("-".repeat(80));
        System.out.println("  Colors used: " + colorsUsed.size() + 
                          " out of " + env.getAvailableColors().size() + " available");
        
        if (!colorFrequency.isEmpty()) {
            System.out.println("  Color distribution:");
            List<Integer> sortedColors = new ArrayList<>(colorFrequency.keySet());
            Collections.sort(sortedColors);
            for (int color : sortedColors) {
                int count = colorFrequency.get(color);
                double percentage = (count * 100.0) / env.getAllNodes().size();
                System.out.printf("    Color %d: %2d nodes (%5.1f%%) ", color, count, percentage);
                int bars = (int) (percentage / 5);
                for (int i = 0; i < bars; i++) System.out.print("█");
                System.out.println();
            }
        }
        
        System.out.println("\nDETAILED NODE STATUS:");
        System.out.println("-".repeat(80));
        
        List<String> okNodes = new ArrayList<>();
        List<String> conflictNodes = new ArrayList<>();
        List<String> uncoloredNodes = new ArrayList<>();
        
        for (graph.GraphNode node : env.getAllNodes()) {
            String nodeId = node.getId();
            if (!node.isColored()) {
                uncoloredNodes.add(nodeId);
            } else if (env.hasConflict(nodeId)) {
                conflictNodes.add(nodeId + "(color " + node.getColor() + ")");
            } else {
                okNodes.add(nodeId + "(color " + node.getColor() + ")");
            }
        }
        
        Collections.sort(okNodes);
        Collections.sort(conflictNodes);
        Collections.sort(uncoloredNodes);
        
        System.out.println("  ✓ Correctly colored (" + okNodes.size() + "): " + 
                          String.join(", ", okNodes));
        if (!conflictNodes.isEmpty()) {
            System.out.println("  ✗ With conflicts (" + conflictNodes.size() + "): " + 
                             String.join(", ", conflictNodes));
        }
        if (!uncoloredNodes.isEmpty()) {
            System.out.println("  ○ Uncolored (" + uncoloredNodes.size() + "): " + 
                             String.join(", ", uncoloredNodes));
        }
        
        System.out.println("\nGRAPH STRUCTURE RECAP:");
        System.out.println("-".repeat(80));
        System.out.println("  Total nodes: " + env.getAllNodes().size());
        
        int totalEdges = 0;
        int maxDegree = 0;
        String maxDegreeNode = "";
        for (graph.GraphNode node : env.getAllNodes()) {
            int degree = node.getNeighborIds().size();
            totalEdges += degree;
            if (degree > maxDegree) {
                maxDegree = degree;
                maxDegreeNode = node.getId();
            }
        }
        totalEdges /= 2;
        
        System.out.println("  Total edges: " + totalEdges);
        System.out.printf("  Average degree: %.2f%n", (2.0 * totalEdges) / env.getAllNodes().size());
        System.out.println("  Maximum degree: " + maxDegree + " (node " + maxDegreeNode + ")");
        
        System.out.println("\nFINAL COLORING VISUALIZATION:");
        System.out.println("-".repeat(80));
        
        Map<Integer, List<String>> colorGroups = new TreeMap<>();
        for (graph.GraphNode node : env.getAllNodes()) {
            if (node.isColored()) {
                colorGroups.computeIfAbsent(node.getColor(), k -> new ArrayList<>())
                          .add(node.getId());
            }
        }
        
        for (Map.Entry<Integer, List<String>> entry : colorGroups.entrySet()) {
            Collections.sort(entry.getValue());
            System.out.printf("  Color %d (%2d nodes): %s%n", 
                entry.getKey(), entry.getValue().size(), entry.getValue());
        }
        
        if (!uncoloredNodes.isEmpty()) {
            Collections.sort(uncoloredNodes);
            System.out.println("  Uncolored nodes: " + uncoloredNodes);
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("|                EXPERIMENT COMPLETE                       |");
        
        if (env.isValidColoring()) {
            System.out.println("|         SUCCESS                                          |");
        } else if (env.isFullyColored()) {
            System.out.println("|         PARTIAL: All nodes colored but conflicts         |");
            System.out.println("|         remain. More iterations may be needed.           |");
        } else {
            System.out.println("|         INCOMPLETE: System did not converge              |");
            System.out.println("|         within the time limit.                           |");
        }
        
        System.out.printf("|               Time: %.2f seconds                         |%n", elapsedTime / 1000.0);
        System.out.println("=".repeat(60));
    }
    
    public static void main(String[] args) {
        try {
            System.out.println("Initializing Distributed Graph Coloring Experiment...");
            Thread.sleep(1500);
            
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
