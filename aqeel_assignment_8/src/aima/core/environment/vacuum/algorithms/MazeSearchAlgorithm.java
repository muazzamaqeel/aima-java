package aima.core.environment.vacuum.algorithms;

import aima.core.agent.Action;
import aima.core.environment.vacuum.VacuumPercept;

import java.util.Set;
import java.util.Stack;

public interface MazeSearchAlgorithm {

    Action selectAction(VacuumPercept percept,
                        int x,
                        int y,
                        Set<String> visited,
                        Stack<Action> backtrackStack);
}