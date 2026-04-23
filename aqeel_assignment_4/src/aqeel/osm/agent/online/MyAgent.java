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

    // ✅ GLOBAL memory (IMPORTANT)
    private final Map<String, Double> penalty = new HashMap<>();

    public MyAgent(OnlineSearchProblem<String, MoveToAction> problem,
                   String goal,
                   MapAdapter map) {
        this.problem = problem;
        this.goal = goal;
        this.map = map;
    }

    @Override
    public Optional<MoveToAction> act(DynamicPercept percept) {

        String current = (String) percept.getAttribute("in");

        // Goal reached
        if (problem.testGoal(current)) {
            return Optional.empty();
        }

        List<MoveToAction> actions = problem.getActions(current);

        MoveToAction bestAction = null;
        double bestScore = Double.MAX_VALUE;

        for (MoveToAction action : actions) {
            String next = action.getToLocation();

            // ✅ heuristic + learned penalty
            double score = MapFunctions.getSLD(next, goal, map)
                    + penalty.getOrDefault(next, 0.0);

            // 🚫 penalize going backwards
            if (next.equals(previousState)) {
                score += 1000;
            }

            if (score < bestScore) {
                bestScore = score;
                bestAction = action;
            }
        }

        // 🔥 LEARNING STEP (VERY IMPORTANT)
        // increase penalty if we are stuck / revisiting
        penalty.put(current, penalty.getOrDefault(current, 0.0) + 10);

        previousState = current;

        return Optional.of(bestAction);
    }
}