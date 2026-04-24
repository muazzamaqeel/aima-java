package aqeel.osm.agent.online;

import aima.core.agent.impl.DynamicPercept;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.map.MoveToAction;
import aima.core.search.framework.problem.OnlineSearchProblem;
import aimax.osm.routing.MapAdapter;
import aima.core.environment.map.MapFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MyAgent extends SimpleAgent<DynamicPercept, MoveToAction> {

    private String previousState = null;
    private final OnlineSearchProblem<String, MoveToAction> problem;
    private final String goal;
    private final MapAdapter map;
    // GLOBAL memory (learning)
    private final Map<String, Double> penalty = new HashMap<>();
    public MyAgent(OnlineSearchProblem<String, MoveToAction> problem,
                   String goal,
                   MapAdapter map) {
        this.problem = problem;
        this.goal = goal;
        this.map = map;
    }

    /**
     * The decision process of the agent works as follows:
     *
     * 1. Determine the current state (where the agent is).
     * 2. Check if the current state is the goal:
     *    - If yes, stop and return no action.
     * 3. Retrieve all possible actions from the current state.
     * 4. Handle edge case:
     *    - If no actions are available, return no action.
     *
     * 5. For each possible action:
     *    - Determine the resulting next state.
     *    - Estimate how close this state is to the goal (heuristic).
     *    - Add a penalty if the state has been visited frequently (memory).
     *    - Add a large penalty if the action leads back to the previous state.
     *
     * 6. Select the action with the lowest total score.
     *
     * 7. Update learning:
     *    - Increase the penalty for the current state to discourage revisiting.
     *
     * 8. Store the current state as the previous state.
     *
     * 9. Return the selected action wrapped in an Optional.
     */

    @Override
    public Optional<MoveToAction> act(DynamicPercept percept) {
        String current = (String) percept.getAttribute("in");
        // Goal check
        if (problem.testGoal(current)) {
            return Optional.empty();
        }
        List<MoveToAction> actions = problem.getActions(current);
        // Safety check (avoid crash)
        if (actions == null || actions.isEmpty()) {
            return Optional.empty();
        }
        MoveToAction bestAction = null;
        double bestScore = Double.MAX_VALUE;

        for (MoveToAction action : actions) {
            String next = action.getToLocation();
            // heuristic (distance to goal)
            double score = MapFunctions.getSLD(next, goal, map);
            // learned penalty (memory)
            score += penalty.getOrDefault(next, 0.0);
            // strongly avoid going backwards
            if (next.equals(previousState)) {
                score += 500;
            }
            if (score < bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }
        // LEARNING STEP
        // increase penalty slowly → smoother learning
        penalty.put(current, penalty.getOrDefault(current, 0.0) + 2.0);
        previousState = current;
        return Optional.of(bestAction);
    }
}