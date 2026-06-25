package aima.core.environment.vacuum;

import aima.core.agent.Action;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.vacuum.VacuumEnvironment.LocationState;
import aima.core.environment.vacuum.algorithms.Depth_First_Search;
import aima.core.environment.vacuum.algorithms.MazeSearchAlgorithm;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import static aima.core.environment.vacuum.MazeVacuumEnvironment.*;

/**
 * Smart maze vacuum agent.
 *
 * HYBRID_LAYERED:
 * Uses VacuumLayeredDirtManager for layered multi-agent dirt tracking.
 *
 * FULL_ONLINE:
 * Does not use global dirt knowledge.
 * The agent only reacts to the current percept and explores with the selected algorithm.
 */
public class SmartMazeVacuumAgent extends SimpleAgent<VacuumPercept, Action> {

    private final VacuumAgentMode mode;
    private int onlineCleanedCount = 0;

    private final String algorithm;
    private final MazeSearchAlgorithm searchAlgorithm;

    private final int agentIndex;

    private Action lastMoveAction;

    private int x = 0;
    private int y = 0;

    private int stepCount = 0;
    private int moveCount = 0;
    private int suckCount = 0;

    private boolean finished = false;
    private boolean completedOwnDirt = false;
    private boolean noMorePossibleMoves = false;

    private final Set<String> visited = new HashSet<>();
    private final Stack<Action> backtrackStack = new Stack<>();

    public SmartMazeVacuumAgent() {
        this("Depth_First_Search");
    }

    public SmartMazeVacuumAgent(String algorithm) {
        this(algorithm, 0, 0, 0);
    }

    public SmartMazeVacuumAgent(String algorithm, int agentIndex, int startX, int startY) {
        this(algorithm, agentIndex, startX, startY, VacuumAgentMode.HYBRID_LAYERED);
    }

    public SmartMazeVacuumAgent(String algorithm,
                                int agentIndex,
                                int startX,
                                int startY,
                                VacuumAgentMode mode) {
        this.algorithm = algorithm;
        this.agentIndex = agentIndex;
        this.x = startX;
        this.y = startY;
        this.mode = mode;
        this.searchAlgorithm = createSearchAlgorithm(algorithm);
    }

    @Override
    public Optional<Action> act(VacuumPercept percept) {
        finishIfOwnDirtCleaned();

        if (finished) {
            return Optional.empty();
        }

        visited.add(key(x, y));

        String currentLocation = percept.getCurrLocation();
        Action action;

        if (shouldSuckCurrentSquare(percept, currentLocation)) {
            action = ACTION_SUCK;
            suckCount++;

            if (mode == VacuumAgentMode.FULL_ONLINE) {
                onlineCleanedCount++;
            }

        } else {
            action = searchAlgorithm.selectAction(percept, x, y, visited, backtrackStack);

            if (action == null) {
                noMorePossibleMoves = true;
                finished = true;
                return Optional.empty();
            }

            if (isMovementAction(action)) {
                updatePosition(action);
                lastMoveAction = action;
                moveCount++;
            }
        }

        stepCount++;

        finishIfOwnDirtCleaned();

        return Optional.of(action);
    }

    private boolean shouldSuckCurrentSquare(VacuumPercept percept, String currentLocation) {
        if (mode == VacuumAgentMode.HYBRID_LAYERED) {
            if (VacuumLayeredDirtManager.isInitialized()
                    && VacuumLayeredDirtManager.hasDirtForAgent(agentIndex, currentLocation)) {
                VacuumLayeredDirtManager.cleanDirtForAgent(agentIndex, currentLocation);
                return true;
            }

            return !VacuumLayeredDirtManager.isInitialized()
                    && percept.getCurrState() == LocationState.Dirty;
        }

        if (mode == VacuumAgentMode.FULL_ONLINE) {
            return percept.getCurrState() == LocationState.Dirty;
        }

        return false;
    }

    public void finishIfOwnDirtCleaned() {
        if (mode == VacuumAgentMode.HYBRID_LAYERED
                && VacuumLayeredDirtManager.isInitialized()
                && !VacuumLayeredDirtManager.hasRemainingDirtForAgent(agentIndex)) {
            completedOwnDirt = true;
            finished = true;
        }
    }

    public VacuumAgentMode getMode() {
        return mode;
    }

    public int getAgentIndex() {
        return agentIndex;
    }

    public int getStepCount() {
        return stepCount;
    }

    public int getMoveCount() {
        return moveCount;
    }

    public int getSuckCount() {
        return suckCount;
    }

    public int getOnlineCleanedCount() {
        return onlineCleanedCount;
    }

    public int getCleanedCount() {
        if (mode == VacuumAgentMode.HYBRID_LAYERED) {
            return VacuumLayeredDirtManager.getCleanedLayers(agentIndex);
        }

        return onlineCleanedCount;
    }

    public boolean isFinished() {
        return finished;
    }

    public boolean hasCompletedOwnDirt() {
        return completedOwnDirt;
    }

    public boolean hasNoMorePossibleMoves() {
        return noMorePossibleMoves;
    }

    public double getCustomPerformance() {
        return getCleanedCount() * 10.0 - moveCount;
    }

    public double getOriginalStylePerformance() {
        return getCustomPerformance();
    }

    public double getLayeredPerformance() {
        return getCustomPerformance();
    }

    private MazeSearchAlgorithm createSearchAlgorithm(String algorithm) {
        try {
            String className = "aima.core.environment.vacuum.algorithms." + algorithm;
            Class<?> clazz = Class.forName(className);
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            return (MazeSearchAlgorithm) constructor.newInstance();
        } catch (Exception e) {
            return new Depth_First_Search();
        }
    }

    private boolean isMovementAction(Action action) {
        return action == ACTION_MOVE_UP
                || action == ACTION_MOVE_DOWN
                || action == ACTION_MOVE_LEFT
                || action == ACTION_MOVE_RIGHT;
    }

    private String key(int x, int y) {
        return x + "," + y;
    }

    private void updatePosition(Action action) {
        if (action == ACTION_MOVE_UP) {
            y--;
        } else if (action == ACTION_MOVE_DOWN) {
            y++;
        } else if (action == ACTION_MOVE_LEFT) {
            x--;
        } else if (action == ACTION_MOVE_RIGHT) {
            x++;
        }
    }
}