package vacuum;

import aima.core.agent.impl.SimpleAgent;

public class RowVacuumAgent extends SimpleAgent<RowVacuumPercept, RowVacuumAction> {

    public RowVacuumAgent() {
        super(new RowVacuumAgentProgram());
    }
}