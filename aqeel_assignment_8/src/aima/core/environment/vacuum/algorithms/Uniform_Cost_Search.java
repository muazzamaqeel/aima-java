package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;

import java.util.Set;

public class Uniform_Cost_Search extends OnlineMazeSearchBase {

    @Override
    protected void onNodeDiscovered(String node,
                                    String parent,
                                    Action action,
                                    int depth,
                                    int cost) {
        // UCS selects targets dynamically by lowest path cost.
    }

    @Override
    protected String selectTargetNode(String currentNode, Set<String> visited) {
        String bestTarget = null;
        int bestCost = Integer.MAX_VALUE;
        long bestOrder = Long.MAX_VALUE;

        for (String node : discoveredNodes) {
            if (isValidTarget(node, currentNode, visited)) {
                int pathCost = getPathCost(currentNode, node);
                long order = discoveryOrders.getOrDefault(node, Long.MAX_VALUE);

                if (pathCost < bestCost || (pathCost == bestCost && order < bestOrder)) {
                    bestTarget = node;
                    bestCost = pathCost;
                    bestOrder = order;
                }
            }
        }

        return bestTarget;
    }
}