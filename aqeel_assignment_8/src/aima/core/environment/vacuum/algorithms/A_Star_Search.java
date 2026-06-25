package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class A_Star_Search extends OnlineMazeSearchBase {

    private final Set<String> frontierSet = new HashSet<>();
    private final Map<String, Integer> costFromStart = new HashMap<>();

    @Override
    protected void onNodeDiscovered(String node,
                                    String parent,
                                    Action action,
                                    int depth,
                                    int cost) {
        frontierSet.add(node);
        costFromStart.put(node, cost);
    }

    @Override
    protected String selectTargetNode(String currentNode, Set<String> visited) {
        String bestTarget = null;
        int bestScore = Integer.MAX_VALUE;

        for (String target : new HashSet<>(frontierSet)) {
            if (!isValidTarget(target, currentNode, visited)) {
                frontierSet.remove(target);
                continue;
            }

            int g = costFromStart.getOrDefault(target, Integer.MAX_VALUE / 2);
            int h = manhattanDistance(currentNode, target);
            int f = g + h;

            if (f < bestScore) {
                bestScore = f;
                bestTarget = target;
            }
        }

        if (bestTarget != null) {
            frontierSet.remove(bestTarget);
        }

        return bestTarget;
    }

    private int manhattanDistance(String nodeA, String nodeB) {
        int[] a = parseNode(nodeA);
        int[] b = parseNode(nodeB);

        return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]);
    }

    private int[] parseNode(String node) {
        String[] parts = node.split(",");

        return new int[]{
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1])
        };
    }
}