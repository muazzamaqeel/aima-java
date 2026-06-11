package aima.core.environment.vacuum;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.Environment;
import aima.core.agent.impl.AbstractEnvironment;
import aima.core.environment.vacuum.VacuumEnvironment.LocationState;
import aima.gui.fx.views.VacuumEnvironmentViewCtrl;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Extended vacuum environment view controller.
 * This class only changes the displayed agent colors.
 * It does not modify the original AIMA view controller.
 */
public class VacuumEnvironmentViewCtrl_Extended extends VacuumEnvironmentViewCtrl {

    private final Set<Agent> agentsInSuckState = new HashSet<>();
    private final Map<Agent, Double> agentOrientations = new HashMap<>();
    private final Map<Agent, Arc> agentSymbols = new HashMap<>();
    private final Map<Agent, Integer> agentIndexes = new HashMap<>();
    private final Function<Action, Double> actionToOrientationFn;

    public VacuumEnvironmentViewCtrl_Extended(StackPane viewRoot) {
        this(viewRoot, action -> {
            if (action == VacuumEnvironment.ACTION_MOVE_LEFT) return 270.0;
            else if (action == VacuumEnvironment.ACTION_MOVE_RIGHT) return 90.0;
            else return null;
        });
    }

    public VacuumEnvironmentViewCtrl_Extended(StackPane viewRoot,
                                              Function<Action, Double> actionToOrientationFn) {
        super(viewRoot, actionToOrientationFn);
        this.actionToOrientationFn = actionToOrientationFn;
    }

    @Override
    public void initialize(AbstractEnvironment<? extends VacuumPercept, ? extends Action> env) {
        agentsInSuckState.clear();
        agentOrientations.clear();
        agentSymbols.clear();
        agentIndexes.clear();

        super.initialize(env);
    }

    @Override
    public void agentActed(Agent<?, ?> agent,
                           VacuumPercept percept,
                           Action action,
                           Environment<?, ?> source) {
        if (action == VacuumEnvironment.ACTION_SUCK) {
            agentsInSuckState.add((Agent) agent);
        } else {
            agentsInSuckState.remove(agent);
        }

        Double orientation = actionToOrientationFn.apply(action);

        if (orientation != null) {
            agentOrientations.put((Agent) agent, orientation);
        }

        super.agentActed(agent, percept, action, source);
    }

    @Override
    protected void update() {
        VacuumEnvironment vEnv = (VacuumEnvironment) env;

        for (String loc : vEnv.getLocations()) {
            var btn = getSquareButton(vEnv.getX(loc), vEnv.getY(loc));

            if (vEnv.getLocationState(loc) == LocationState.Dirty) {
                btn.getLabel().setText("Dirty");
            } else if (vEnv.getLocationState(loc) == LocationState.Clean) {
                btn.getLabel().setText("");
            } else {
                btn.getLabel().setText("X");
            }

            btn.getPane().getChildren().clear();
        }

        for (Agent agent : vEnv.getAgents()) {
            String loc = vEnv.getAgentLocation(agent);
            var btn = getSquareButton(vEnv.getX(loc), vEnv.getY(loc));

            Double orientation = agentOrientations.get(agent);

            if (orientation == null) {
                orientation = 90.0;
            }

            btn.getPane().getChildren().add(getAgentSymbol(agent, orientation));
        }
    }

    private Node getAgentSymbol(Agent agent, double orientation) {
        Arc result = agentSymbols.get(agent);

        if (result == null) {
            result = new Arc();
            result.radiusXProperty().bind(squareSize.multiply(0.75 / 2));
            result.radiusYProperty().bind(squareSize.multiply(0.75 / 2));
            result.setStartAngle(135.0f);
            result.setType(ArcType.ROUND);
            result.setFill(getAgentColor(agent));
            agentSymbols.put(agent, result);
        }

        result.setLength(agentsInSuckState.contains(agent) ? 360.0f : 270.0f);
        result.setRotate(orientation);

        int index = getAgentIndex(agent);

        if (index == 0) {
            result.setTranslateX(-12);
            result.setTranslateY(-12);
        } else if (index == 1) {
            result.setTranslateX(12);
            result.setTranslateY(-12);
        } else {
            result.setTranslateX(0);
            result.setTranslateY(12);
        }

        return result;
    }

    private Color getAgentColor(Agent agent) {
        int index = getAgentIndex(agent);

        if (index == 0) {
            return Color.RED;
        } else if (index == 1) {
            return Color.DARKBLUE;
        } else {
            return Color.DARKGREEN;
        }
    }

    private int getAgentIndex(Agent agent) {
        if (!agentIndexes.containsKey(agent)) {
            agentIndexes.put(agent, agentIndexes.size());
        }

        return agentIndexes.get(agent);
    }
}