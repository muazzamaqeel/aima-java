package vacuum;

import aima.core.agent.Action;
import aima.core.agent.Agent;
import aima.core.environment.vacuum.VacuumPercept;

public class Main {

    public static void main(String[] args) {

        // ✅ CORRECT constructor call
        RowVacuumEnvironment env = new RowVacuumEnvironment(8);

        // ❌ WRONG before: RowVacuumAgent
        // ✅ CORRECT:
        Agent<VacuumPercept, Action> agent = new MyVacuumAgent();

        env.addAgent(agent);

        env.step(100);

        // ❌ WRONG before: getPerformance()
        // ✅ CORRECT:
        System.out.println("Performance: " +
                env.getPerformanceMeasure(agent));
    }
}