package vacuum;

import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.vacuum.VacuumEnvironment;
import aima.core.environment.vacuum.VacuumPercept;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class MyFinalAgent extends SimpleAgent<VacuumPercept, Action> {

    public MyFinalAgent() {
        super(new Program());
    }

    static class Program implements AgentProgram<VacuumPercept, Action> {

        private Action direction = VacuumEnvironment.ACTION_MOVE_RIGHT;
        private Set<String> visited = new HashSet<>();

        @Override
        public Optional<Action> apply(VacuumPercept p) {

            String loc = p.getCurrLocation();
            visited.add(loc);

            // ✅ FIXED HERE
            if (p.getCurrState() == VacuumEnvironment.LocationState.Dirty) {
                return Optional.of(VacuumEnvironment.ACTION_SUCK);
            }

            if (direction.equals(VacuumEnvironment.ACTION_MOVE_RIGHT)) {
                direction = VacuumEnvironment.ACTION_MOVE_LEFT;
                return Optional.of(VacuumEnvironment.ACTION_MOVE_RIGHT);
            }

            if (direction.equals(VacuumEnvironment.ACTION_MOVE_LEFT)) {
                direction = VacuumEnvironment.ACTION_MOVE_RIGHT;
                return Optional.of(VacuumEnvironment.ACTION_MOVE_LEFT);
            }

            return Optional.empty();
        }
    }
}