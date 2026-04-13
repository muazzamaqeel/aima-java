package vacuum;

import aima.core.agent.Agent;
import aima.core.agent.impl.AbstractEnvironment;

public class RowVacuumEnvironment extends AbstractEnvironment<vacuum.RowVacuumPercept, RowVacuumAction> {

    private boolean[] dirt;
    private int position;
    private int performance = 0;

    public RowVacuumEnvironment(boolean[] dirt, int startPos) {
        this.dirt = dirt.clone();
        this.position = startPos;
    }

    @Override
    public RowVacuumPercept getPerceptSeenBy(Agent<?, ?> agent) {
        return new vacuum.RowVacuumPercept(
                dirt[position],
                position > 0,
                position < dirt.length - 1
        );
    }

    @Override
    public void execute(Agent<?, ?> agent, RowVacuumAction action) {

        switch (action) {

            case SUCK:
                if (dirt[position]) {
                    dirt[position] = false;
                    performance += 10;
                }
                break;

            case LEFT:
                if (position > 0) {
                    position--;
                    performance -= 1;
                }
                break;

            case RIGHT:
                if (position < dirt.length - 1) {
                    position++;
                    performance -= 1;
                }
                break;

            case NO_OP:
                break;
        }
    }

    @Override
    public boolean isDone() {
        for (boolean d : dirt) {
            if (d) return false;
        }
        return true;
    }

    public int getPerformance() {
        return performance;
    }
}