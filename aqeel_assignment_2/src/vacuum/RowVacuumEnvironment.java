package vacuum;

import aima.core.agent.Agent;
import aima.core.environment.vacuum.VacuumEnvironment;
import aima.core.environment.vacuum.VacuumPercept;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class RowVacuumEnvironment extends VacuumEnvironment {

    public RowVacuumEnvironment(int size) {
        super(generateLocations(size), generateStates(size));
    }

    private static List<String> generateLocations(int size) {
        List<String> locations = new ArrayList<>();

        for (int i = 0; i < size; i++) {
            locations.add(String.valueOf((char) ('A' + i)));
        }

        return locations;
    }

    private static LocationState[] generateStates(int size) {
        LocationState[] states = new LocationState[size];
        Random rand = new Random();

        for (int i = 0; i < size; i++) {
            states[i] = rand.nextBoolean()
                    ? LocationState.Dirty
                    : LocationState.Clean;
        }

        return states;
    }

    @Override
    public VacuumPercept getPerceptSeenBy(Agent<?, ?> agent) {

        String loc = getAgentLocation(agent);
        int x = getX(loc);

        boolean canMoveLeft = x > 1;
        boolean canMoveRight = x < getXDimension();

        VacuumPercept percept = new VacuumPercept(
                loc,
                getLocationState(loc)
        );

        // Add extra information
        percept.setAttribute("left", canMoveLeft);
        percept.setAttribute("right", canMoveRight);

        return percept;
    }
}