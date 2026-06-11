package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;
import aima.core.environment.vacuum.VacuumPercept;

import java.util.Objects;
import java.util.Set;
import java.util.Stack;

import static aima.core.environment.vacuum.MazeVacuumEnvironment.*;

public class Breadth_First_Search implements MazeSearchAlgorithm {

    @Override
    public Action selectAction(VacuumPercept percept,
                               int x,
                               int y,
                               Set<String> visited,
                               Stack<Action> backtrackStack) {

        Action action;

        if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_RIGHT), true) && !wasVisited(visited, x + 1, y)) {
            action = ACTION_MOVE_RIGHT;
            backtrackStack.push(opposite(action));
        } else if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_DOWN), true) && !wasVisited(visited, x, y + 1)) {
            action = ACTION_MOVE_DOWN;
            backtrackStack.push(opposite(action));
        } else if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_LEFT), true) && !wasVisited(visited, x - 1, y)) {
            action = ACTION_MOVE_LEFT;
            backtrackStack.push(opposite(action));
        } else if (Objects.equals(percept.getAttribute(ATT_CAN_MOVE_UP), true) && !wasVisited(visited, x, y - 1)) {
            action = ACTION_MOVE_UP;
            backtrackStack.push(opposite(action));
        } else if (!backtrackStack.isEmpty()) {
            action = backtrackStack.pop();
        } else {
            return null;
        }

        return action;
    }

    private boolean wasVisited(Set<String> visited, int x, int y) {
        return visited.contains(key(x, y));
    }

    private String key(int x, int y) {
        return x + "," + y;
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