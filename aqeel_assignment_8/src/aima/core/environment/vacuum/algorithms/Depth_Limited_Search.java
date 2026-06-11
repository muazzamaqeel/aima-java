package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;

public class Depth_Limited_Search extends OnlineMazeSearchBase {

    private static final int DEPTH_LIMIT = 100;

    private final Deque<String> frontierStack = new ArrayDeque<>();

    @Override
    protected void onNodeDiscovered(String node,
                                    String parent,
                                    Action action,
                                    int depth,
                                    int cost) {
        if (depth <= DEPTH_LIMIT) {
            frontierStack.push(node);
        }
    }

    @Override
    protected String selectTargetNode(String currentNode, Set<String> visited) {
        while (!frontierStack.isEmpty()) {
            String target = frontierStack.peek();

            if (nodeDepths.getOrDefault(target, Integer.MAX_VALUE) <= DEPTH_LIMIT
                    && isValidTarget(target, currentNode, visited)) {
                return target;
            }

            frontierStack.pop();
        }

        return null;
    }
}