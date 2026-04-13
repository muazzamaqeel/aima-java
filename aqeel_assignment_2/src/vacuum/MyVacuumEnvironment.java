package vacuum;

import aima.core.environment.vacuum.VacuumEnvironment;

import java.util.Arrays;

public class MyVacuumEnvironment extends VacuumEnvironment {

    public MyVacuumEnvironment() {
        super(
                Arrays.asList("A","B","C","D","E","F","G","H"),
                LocationState.Dirty, LocationState.Dirty, LocationState.Dirty, LocationState.Dirty,
                LocationState.Dirty, LocationState.Dirty, LocationState.Dirty, LocationState.Dirty
        );
    }
}