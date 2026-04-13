package vacuum;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.vacuum.VacuumEnvironment;
import aima.core.environment.vacuum.VacuumPercept;

import java.util.Optional;

public class MyVacuumAgent extends SimpleAgent<VacuumPercept, Action> {

    public MyVacuumAgent() {
        super(new Program());
    }

    static class Program implements AgentProgram<VacuumPercept, Action> {

        private boolean reachedLeft = false;
        private boolean reachedRight = false;
        private Action direction = VacuumEnvironment.ACTION_MOVE_LEFT;

        @Override
        public Optional<Action> apply(VacuumPercept p) {

            if (p.getCurrState() == VacuumEnvironment.LocationState.Dirty) {
                return Optional.of(VacuumEnvironment.ACTION_SUCK);
            }

            Boolean canMoveLeft = (Boolean) p.getAttribute("left");
            Boolean canMoveRight = (Boolean) p.getAttribute("right");

            if (canMoveLeft != null && !canMoveLeft) {
                reachedLeft = true;
                direction = VacuumEnvironment.ACTION_MOVE_RIGHT;
            }

            if (canMoveRight != null && !canMoveRight) {
                reachedRight = true;
                direction = VacuumEnvironment.ACTION_MOVE_LEFT;
            }

            if (reachedLeft && reachedRight) {
                return Optional.empty();
            }

            if (direction == VacuumEnvironment.ACTION_MOVE_LEFT) {
                return Optional.of(VacuumEnvironment.ACTION_MOVE_LEFT);
            } else {
                return Optional.of(VacuumEnvironment.ACTION_MOVE_RIGHT);
            }
        }
    }
}