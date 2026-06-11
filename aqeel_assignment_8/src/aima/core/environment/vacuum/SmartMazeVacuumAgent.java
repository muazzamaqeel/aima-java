package aima.core.environment.vacuum;

import aima.core.agent.Action;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.vacuum.VacuumEnvironment.LocationState;
import aima.core.environment.vacuum.VacuumPercept;
import aima.core.environment.vacuum.algorithms.Depth_First_Search;
import aima.core.environment.vacuum.algorithms.MazeSearchAlgorithm;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.Stack;

import static aima.core.environment.vacuum.MazeVacuumEnvironment.*;

/**
 * This vacuum agent tries to clean up a checkerboard-like world of squares. Percepts inform
 * the agent about the state of the current square and about possible next movement directions.
 * A simple random walk strategy is used.
 *
 * @author Ruediger Lunde
 */
public class SmartMazeVacuumAgent extends SimpleAgent<VacuumPercept, Action> {

    private final String algorithm;
    private final MazeSearchAlgorithm searchAlgorithm;

    private Action lastMoveAction;

    private int x = 0;
    private int y = 0;

    private final Set<String> visited = new HashSet<>();
    private final Stack<Action> backtrackStack = new Stack<>();

    public SmartMazeVacuumAgent() {
        this("Depth_First_Search");
    }

    public SmartMazeVacuumAgent(String algorithm) {
        this.algorithm = algorithm;
        this.searchAlgorithm = createSearchAlgorithm(algorithm);
    }

    @Override
    public Optional<Action> act(VacuumPercept percept) {
        visited.add(key(x, y));

        Action action;
        if (percept.getCurrState() == LocationState.Dirty) {
            action = ACTION_SUCK;
        } else {
            action = searchAlgorithm.selectAction(percept, x, y, visited, backtrackStack);

            if (action == null) {
                return Optional.empty();
            }

            if (action == ACTION_MOVE_UP ||
                    action == ACTION_MOVE_DOWN ||
                    action == ACTION_MOVE_LEFT ||
                    action == ACTION_MOVE_RIGHT) {
                updatePosition(action);
                lastMoveAction = action;
            }
        }
        return Optional.of(action);
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