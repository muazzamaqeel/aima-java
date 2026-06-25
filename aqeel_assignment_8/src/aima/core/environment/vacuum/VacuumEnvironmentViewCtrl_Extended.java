package aima.core.environment.vacuum;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.agent.Environment;
import aima.core.agent.impl.AbstractEnvironment;
import aima.core.environment.vacuum.VacuumEnvironment.LocationState;
import aima.gui.fx.views.VacuumEnvironmentViewCtrl;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Extended vacuum environment view controller.
 *
 * This class is only responsible for visualization:
 * - different agent colors
 * - agent orientation
 * - suck animation
 * - layered dirt labels in hybrid mode
 *
 * Important:
 * This class must not change the cleaning state.
 * Cleaning logic belongs to the agent/model, not the view.
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
            SquareButton btn = getSquareButton(vEnv.getX(loc), vEnv.getY(loc));

            btn.getLabel().setText("");
            btn.getPane().getChildren().clear();

            if (vEnv.getLocationState(loc) == null) {
                btn.getLabel().setText("X");
            } else if (VacuumLayeredDirtManager.isInitialized()) {
                addLayeredDirtLabels(btn, loc);
            } else if (vEnv.getLocationState(loc) == LocationState.Dirty) {
                addNormalDirtLabel(btn);
            } else if (vEnv.getLocationState(loc) == LocationState.Clean) {
                btn.getLabel().setText("");
            }
        }

        for (Agent agent : vEnv.getAgents()) {
            String loc = vEnv.getAgentLocation(agent);
            SquareButton btn = getSquareButton(vEnv.getX(loc), vEnv.getY(loc));

            Double orientation = agentOrientations.get(agent);

            if (orientation == null) {
                orientation = 90.0;
            }

            btn.getPane().getChildren().add(getAgentSymbol(agent, orientation));
        }
    }

    private void addNormalDirtLabel(SquareButton btn) {
        Label dirtLabel = new Label("Dirty");
        dirtLabel.setTextFill(Color.DARKRED);
        dirtLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        btn.getPane().getChildren().add(dirtLabel);
    }

    private void addLayeredDirtLabels(SquareButton btn, String location) {
        ArrayList<Integer> remainingLayers = VacuumLayeredDirtManager.getRemainingDirtLayers(location);
        Collections.sort(remainingLayers);

        VBox dirtBox = new VBox(1);
        dirtBox.setAlignment(Pos.CENTER);

        for (Integer agentIndex : remainingLayers) {
            Label dirtLabel = new Label("Dirty");
            dirtLabel.setTextFill(getAgentColor(agentIndex));
            dirtLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: bold;");
            dirtBox.getChildren().add(dirtLabel);
        }

        if (!dirtBox.getChildren().isEmpty()) {
            btn.getPane().getChildren().add(dirtBox);
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
        return getAgentColor(getAgentIndex(agent));
    }

    private Color getAgentColor(int index) {
        if (index == 0) {
            return Color.RED;
        } else if (index == 1) {
            return Color.DARKBLUE;
        } else {
            return Color.DARKGREEN;
        }
    }

    private int getAgentIndex(Agent agent) {
        if (VacuumLayeredDirtManager.isInitialized()) {
            return VacuumLayeredDirtManager.getAgentIndex(agent);
        }

        if (!agentIndexes.containsKey(agent)) {
            agentIndexes.put(agent, agentIndexes.size());
        }

        return agentIndexes.get(agent);
    }
}