package aqeel.osm.agent.online;

import aima.core.agent.impl.DynamicPercept;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.map.MoveToAction;
import aima.core.search.framework.problem.OnlineSearchProblem;
import java.util.List;
import java.util.Optional;

public class MyAgent extends SimpleAgent<DynamicPercept, MoveToAction> {
    private String previousState = null;
    private final OnlineSearchProblem<String, MoveToAction> problem;
    public MyAgent(OnlineSearchProblem<String, MoveToAction> problem) {
        this.problem = problem;
    }

    @Override
    public Optional<MoveToAction> act(DynamicPercept percept) {
        String current = (String) percept.getAttribute("in");
        if (problem.testGoal(current)) {
            return Optional.empty();
        }
        List<MoveToAction> actions = problem.getActions(current);
        for (MoveToAction action : actions) {
            String next = action.getToLocation();
            if (!next.equals(previousState)) {
                previousState = current;
                return Optional.of(action);
            }
        }
        previousState = current;
        return Optional.of(actions.get(0));
    }
}