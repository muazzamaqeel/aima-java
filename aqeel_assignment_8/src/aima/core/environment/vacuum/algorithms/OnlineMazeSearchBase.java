/*

1. Read the current percept.
2. Learn which neighboring squares exist.
3. Build a small internal graph of discovered nodes.
4. Remember visited/discovered nodes.
5. Plan a path to the next target node.
6. Return the next movement action.

 */



package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;
import aima.core.environment.vacuum.VacuumPercept;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;

import static aima.core.environment.vacuum.MazeVacuumEnvironment.*;

public abstract class OnlineMazeSearchBase implements MazeSearchAlgorithm {

    private final List<Action> plannedActions = new ArrayList<>();

    protected final Set<String> discoveredNodes = new HashSet<>();
    protected final Map<String, Integer> nodeDepths = new HashMap<>();
    protected final Map<String, Integer> nodeCosts = new HashMap<>();
    protected final Map<String, Long> discoveryOrders = new HashMap<>();
    protected final Map<String, Map<String, Action>> graph = new HashMap<>();

    private long discoveryCounter = 0;

    @Override
    public final Action selectAction(VacuumPercept percept,
                                     int x,
                                     int y,
                                     Set<String> visited,
                                     Stack<Action> backtrackStack) {

        String currentNode = key(x, y);

        observeCurrentNode(percept, x, y, currentNode);

        if (!plannedActions.isEmpty()) {
            Action nextAction = plannedActions.remove(0);

            if (canMove(percept, nextAction)) {
                return nextAction;
            }

            plannedActions.clear();
        }

        String targetNode = selectTargetNode(currentNode, visited);

        if (targetNode == null) {
            return null;
        }

        List<Action> path = findLowestCostPath(currentNode, targetNode);

        if (path.isEmpty()) {
            markTargetAsNotUseful(targetNode);
            return null;
        }

        plannedActions.addAll(path);

        Action nextAction = plannedActions.remove(0);

        if (canMove(percept, nextAction)) {
            return nextAction;
        }

        plannedActions.clear();
        return null;
    }

    private void observeCurrentNode(VacuumPercept percept, int x, int y, String currentNode) {
        if (!discoveredNodes.contains(currentNode)) {
            discoveredNodes.add(currentNode);
            nodeDepths.put(currentNode, 0);
            nodeCosts.put(currentNode, 0);
            discoveryOrders.put(currentNode, discoveryCounter++);
        }

        observeMove(percept, currentNode, x, y, ACTION_MOVE_UP, x, y - 1);
        observeMove(percept, currentNode, x, y, ACTION_MOVE_RIGHT, x + 1, y);
        observeMove(percept, currentNode, x, y, ACTION_MOVE_DOWN, x, y + 1);
        observeMove(percept, currentNode, x, y, ACTION_MOVE_LEFT, x - 1, y);
    }

    private void observeMove(VacuumPercept percept,
                             String currentNode,
                             int currentX,
                             int currentY,
                             Action action,
                             int nextX,
                             int nextY) {

        if (!canMove(percept, action)) {
            return;
        }

        String nextNode = key(nextX, nextY);

        addEdge(currentNode, nextNode, action);
        addEdge(nextNode, currentNode, opposite(action));

        int currentDepth = nodeDepths.getOrDefault(currentNode, 0);
        int nextDepth = currentDepth + 1;

        int currentCost = nodeCosts.getOrDefault(currentNode, 0);
        int nextCost = currentCost + getMoveCost(action);

        if (!discoveredNodes.contains(nextNode)) {
            discoveredNodes.add(nextNode);
            nodeDepths.put(nextNode, nextDepth);
            nodeCosts.put(nextNode, nextCost);
            discoveryOrders.put(nextNode, discoveryCounter++);

            onNodeDiscovered(nextNode, currentNode, action, nextDepth, nextCost);
        } else {
            if (nextDepth < nodeDepths.getOrDefault(nextNode, Integer.MAX_VALUE)) {
                nodeDepths.put(nextNode, nextDepth);
            }

            if (nextCost < nodeCosts.getOrDefault(nextNode, Integer.MAX_VALUE)) {
                nodeCosts.put(nextNode, nextCost);
            }
        }
    }

    private void addEdge(String fromNode, String toNode, Action action) {
        graph.putIfAbsent(fromNode, new HashMap<>());
        graph.get(fromNode).put(toNode, action);
    }

    protected abstract void onNodeDiscovered(String node,
                                             String parent,
                                             Action action,
                                             int depth,
                                             int cost);

    protected abstract String selectTargetNode(String currentNode,
                                               Set<String> visited);

    protected boolean isValidTarget(String node, String currentNode, Set<String> visited) {
        return node != null
                && !node.equals(currentNode)
                && !visited.contains(node)
                && findLowestCostPath(currentNode, node).size() > 0;
    }

    protected void markTargetAsNotUseful(String node) {
        // Kept for subclasses if they need it later.
    }

    protected int getPathCost(String startNode, String targetNode) {
        List<Action> path = findLowestCostPath(startNode, targetNode);

        if (path.isEmpty()) {
            return Integer.MAX_VALUE;
        }

        int cost = 0;

        for (Action action : path) {
            cost += getMoveCost(action);
        }

        return cost;
    }

    protected List<Action> findLowestCostPath(String startNode, String targetNode) {
        List<Action> result = new ArrayList<>();

        if (startNode.equals(targetNode)) {
            return result;
        }

        PriorityQueue<PathNode> queue = new PriorityQueue<>((a, b) -> Integer.compare(a.cost, b.cost));
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> parents = new HashMap<>();

        queue.add(new PathNode(startNode, 0));
        distances.put(startNode, 0);

        while (!queue.isEmpty()) {
            PathNode current = queue.poll();

            if (current.node.equals(targetNode)) {
                break;
            }

            if (current.cost > distances.getOrDefault(current.node, Integer.MAX_VALUE)) {
                continue;
            }

            Map<String, Action> neighbors = graph.get(current.node);

            if (neighbors == null) {
                continue;
            }

            for (Map.Entry<String, Action> entry : neighbors.entrySet()) {
                String nextNode = entry.getKey();
                Action action = entry.getValue();

                int newCost = current.cost + getMoveCost(action);

                if (newCost < distances.getOrDefault(nextNode, Integer.MAX_VALUE)) {
                    distances.put(nextNode, newCost);
                    parents.put(nextNode, current.node);
                    queue.add(new PathNode(nextNode, newCost));
                }
            }
        }

        if (!parents.containsKey(targetNode)) {
            return result;
        }

        List<String> nodes = new ArrayList<>();
        String current = targetNode;

        while (!current.equals(startNode)) {
            nodes.add(0, current);
            current = parents.get(current);
        }

        String previous = startNode;

        for (String node : nodes) {
            Action action = graph.get(previous).get(node);

            if (action != null) {
                result.add(action);
            }

            previous = node;
        }

        return result;
    }

    protected boolean canMove(VacuumPercept percept, Action action) {
        if (action == ACTION_MOVE_UP) {
            return Objects.equals(percept.getAttribute(ATT_CAN_MOVE_UP), true);
        } else if (action == ACTION_MOVE_RIGHT) {
            return Objects.equals(percept.getAttribute(ATT_CAN_MOVE_RIGHT), true);
        } else if (action == ACTION_MOVE_DOWN) {
            return Objects.equals(percept.getAttribute(ATT_CAN_MOVE_DOWN), true);
        } else if (action == ACTION_MOVE_LEFT) {
            return Objects.equals(percept.getAttribute(ATT_CAN_MOVE_LEFT), true);
        }

        return false;
    }

    protected int getMoveCost(Action action) {
        if (action == ACTION_MOVE_RIGHT) {
            return 1;
        } else if (action == ACTION_MOVE_DOWN) {
            return 2;
        } else if (action == ACTION_MOVE_UP) {
            return 3;
        } else if (action == ACTION_MOVE_LEFT) {
            return 4;
        }

        return Integer.MAX_VALUE;
    }

    protected String key(int x, int y) {
        return x + "," + y;
    }

    protected Action opposite(Action action) {
        if (action == ACTION_MOVE_UP) {
            return ACTION_MOVE_DOWN;
        } else if (action == ACTION_MOVE_DOWN) {
            return ACTION_MOVE_UP;
        } else if (action == ACTION_MOVE_LEFT) {
            return ACTION_MOVE_RIGHT;
        } else if (action == ACTION_MOVE_RIGHT) {
            return ACTION_MOVE_LEFT;
        }

        return null;
    }

    private static class PathNode {
        private final String node;
        private final int cost;

        private PathNode(String node, int cost) {
            this.node = node;
            this.cost = cost;
        }
    }
}