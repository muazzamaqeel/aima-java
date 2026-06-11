package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;

import java.util.Set;

public class Iterative_Deepening_Search extends OnlineMazeSearchBase {

    private int currentLimit = 3;

    @Override
    protected void onNodeDiscovered(String node,
                                    String parent,
                                    Action action,
                                    int depth,
                                    int cost) {
        // IDS keeps all discovered nodes, but only selects nodes within the current limit.
    }

    @Override
    protected String selectTargetNode(String currentNode, Set<String> visited) {
        String target = selectBestTargetInsideCurrentLimit(currentNode, visited);

        while (target == null && hasAnyUnvisitedNodeAboveCurrentLimit(currentNode, visited)) {
            currentLimit += 3;
            target = selectBestTargetInsideCurrentLimit(currentNode, visited);
        }

        return target;
    }

    private String selectBestTargetInsideCurrentLimit(String currentNode, Set<String> visited) {
        String bestTarget = null;
        int bestDepth = -1;
        long bestOrder = -1;

        for (String node : discoveredNodes) {
            int depth = nodeDepths.getOrDefault(node, Integer.MAX_VALUE);

            if (depth <= currentLimit && isValidTarget(node, currentNode, visited)) {
                long order = discoveryOrders.getOrDefault(node, 0L);

                if (depth > bestDepth || (depth == bestDepth && order > bestOrder)) {
                    bestTarget = node;
                    bestDepth = depth;
                    bestOrder = order;
                }
            }
        }

        return bestTarget;
    }

    private boolean hasAnyUnvisitedNodeAboveCurrentLimit(String currentNode, Set<String> visited) {
        for (String node : discoveredNodes) {
            int depth = nodeDepths.getOrDefault(node, Integer.MAX_VALUE);

            if (depth > currentLimit && isValidTarget(node, currentNode, visited)) {
                return true;
            }
        }

        return false;
    }
}