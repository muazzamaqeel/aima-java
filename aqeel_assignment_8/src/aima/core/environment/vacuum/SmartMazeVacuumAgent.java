package aima.core.environment.vacuum;

import aima.core.agent.Action;
import aima.core.agent.impl.SimpleAgent;

import java.util.HashSet;
import java.util.Objects;
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

    private Action lastMoveAction;

    private int x = 0;
    private int y = 0;

    private final Set<String> visited = new HashSet<>();
    private final Stack<Action> backtrackStack = new Stack<>();

    @Override
    public Optional<Action> act(VacuumPercept percept) {
        visited.add(key(x, y));

        Action action;
        if (percept.getCurrState() == LocationState.Dirty) {
            action = ACTION_SUCK;
        } else {
            if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_UP), true) && !wasVisited(x, y - 1)) {
                action = ACTION_MOVE_UP;
                backtrackStack.push(opposite(action));
            } else if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_RIGHT), true) && !wasVisited(x + 1, y)) {
                action = ACTION_MOVE_RIGHT;
                backtrackStack.push(opposite(action));
            } else if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_DOWN), true) && !wasVisited(x, y + 1)) {
                action = ACTION_MOVE_DOWN;
                backtrackStack.push(opposite(action));
            } else if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_LEFT), true) && !wasVisited(x - 1, y)) {
                action = ACTION_MOVE_LEFT;
                backtrackStack.push(opposite(action));
            } else if (!backtrackStack.isEmpty()) {
                action = backtrackStack.pop();
            } else {
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

    private String key(int x, int y) {
        return x + "," + y;
    }

    private boolean wasVisited(int x, int y) {
        return visited.contains(key(x, y));
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

    private Action opposite(Action action) {
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
}