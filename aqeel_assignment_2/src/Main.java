import vacuum.*;

public class Main {

    public static void main(String[] args) {

        boolean[] dirt = {true, false, true, true, false, true};
        int start = 2;

        RowVacuumEnvironment env = new RowVacuumEnvironment(dirt, start);
        RowVacuumAgent agent = new RowVacuumAgent();

        env.addAgent(agent);

        int steps = 0;

        while (!env.isDone() && steps < 50) {
            env.step();
            steps++;
        }

        System.out.println("Steps: " + steps);
        System.out.println("Performance: " + env.getPerformance());
    }
}