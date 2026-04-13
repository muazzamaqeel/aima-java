package vacuum;

import aima.core.agent.AgentProgram;
import java.util.Optional;

public class RowVacuumAgentProgram implements AgentProgram<RowVacuumPercept, RowVacuumAction> {

    private RowVacuumAction direction = RowVacuumAction.RIGHT;
    private boolean leftKnown = false;
    private boolean rightKnown = false;

    @Override
    public Optional<RowVacuumAction> apply(RowVacuumPercept p) {

        if (p.isDirty()) {
            return Optional.of(RowVacuumAction.SUCK);
        }

        if (direction == RowVacuumAction.RIGHT) {
            if (p.canMoveRight()) {
                return Optional.of(RowVacuumAction.RIGHT);
            } else {
                rightKnown = true;
                direction = RowVacuumAction.LEFT;
            }
        }

        if (direction == RowVacuumAction.LEFT) {
            if (p.canMoveLeft()) {
                return Optional.of(RowVacuumAction.LEFT);
            } else {
                leftKnown = true;
                direction = RowVacuumAction.RIGHT;
            }
        }

        if (leftKnown && rightKnown) {
            return Optional.of(RowVacuumAction.NO_OP);
        }

        return Optional.empty();
    }
}