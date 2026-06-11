package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Set;

public class Breadth_First_Search extends OnlineMazeSearchBase {

    private final Queue<String> frontierQueue = new ArrayDeque<>();

    @Override
    protected void onNodeDiscovered(String node,
                                    String parent,
                                    Action action,
                                    int depth,
                                    int cost) {
        frontierQueue.add(node);
    }

    @Override
    protected String selectTargetNode(String currentNode, Set<String> visited) {
        while (!frontierQueue.isEmpty()) {
            String target = frontierQueue.peek();

            if (isValidTarget(target, currentNode, visited)) {
                return target;
            }

            frontierQueue.poll();
        }

        return null;
    }
}