package vacuum;

import aima.core.agent.Action;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.vacuum.*;
import aima.core.search.agent.NondeterministicSearchAgent;
import aima.core.search.nondeterministic.NondeterministicProblem;
import aima.core.util.Tasks;
import aima.gui.fx.framework.IntegrableApplication;
import aima.gui.fx.framework.Parameter;
import aima.gui.fx.framework.TaskExecutionPaneBuilder;
import aima.gui.fx.framework.TaskExecutionPaneCtrl;
import aima.gui.fx.views.SimpleEnvironmentViewCtrl;
import aima.gui.fx.views.VacuumEnvironmentViewCtrl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.List;

// VM options (Java>8): --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml

/**
 * Integrable application which demonstrates how different kinds of vacuum
 * cleaner agents behave in a two square environment.
 *
 * @author Ruediger Lunde
 */
public class MyVacuumApp extends IntegrableApplication {

    public static void main(String[] args) {
        launch(args);
    }

    public final static String PARAM_ENV = "environment";
    public final static String PARAM_AGENT = "agent";

    protected TaskExecutionPaneCtrl taskPaneCtrl;
    protected SimpleEnvironmentViewCtrl<VacuumPercept, Action> envViewCtrl;
    protected VacuumEnvironment env = null;
    protected SimpleAgent<VacuumPercept, Action> agent = null;

    @Override
    public String getTitle() {
        return "Vacuum Agent App";
    }
    /**
     * Defines state view, parameters, and call-back functions and calls the
     * simulation pane builder to create layout and controller objects.
     */
    @Override
    public Pane createRootPane() {
        BorderPane root = new BorderPane();

        StackPane envView = new StackPane();
        envViewCtrl = new VacuumEnvironmentViewCtrl(envView, action -> {
            if (action == VacuumEnvironment.ACTION_MOVE_LEFT) return 270.0;
            else if (action == VacuumEnvironment.ACTION_MOVE_RIGHT) return 90.0;
            else if (action == MazeVacuumEnvironment.ACTION_MOVE_UP) return 0.0;
            else if (action == MazeVacuumEnvironment.ACTION_MOVE_DOWN) return 180.0;
            else return null;
        });

        List<Parameter> params = createParameters();

        TaskExecutionPaneBuilder builder = new TaskExecutionPaneBuilder();
        builder.defineParameters(params);
        builder.defineStateView(envView);
        builder.defineInitMethod(this::initialize);
        builder.defineTaskMethod(this::startExperiment);
        taskPaneCtrl = builder.getResultFor(root);

        return root;
    }

    /**
     * Is called after each parameter selection change.
     */
    protected List<Parameter> createParameters() {

        // Muazzam: Row Environment option
        Parameter p1 = new Parameter(PARAM_ENV,
                "Row Environment (8 squares)",   // NEW
                "A/B Deterministic Environment",
                "A/B Non-Deterministic Environment",
                "Small Maze Environment",
                "Maze Environment");

        // Muazzam: MyVacuumAgent option
        Parameter p2 = new Parameter(PARAM_AGENT,
                "MyVacuumAgent",                 // NEW
                "TableDrivenVacuumAgent",
                "ReflexVacuumAgent",
                "SimpleReflexVacuumAgent",
                "ModelBasedReflexVacuumAgent",
                "NondeterministicVacuumAgent",
                "RandomWalkVacuumAgent");

        return Arrays.asList(p1, p2);
    }

    @Override
    public void initialize() {

        // Muazzam : shifted indices because we added new option
        switch (taskPaneCtrl.getParamValueIndex(PARAM_ENV)) {
            case 0:
                env = new RowVacuumEnvironment(8);  // YOUR ENVIRONMENT
                break;
            case 1:
                env = new VacuumEnvironment();
                break;
            case 2:
                env = new NondeterministicVacuumEnvironment();
                break;
            case 3:
                env = new MazeVacuumEnvironment(5, 5, 0.5, 0.2);
                break;
            case 4:
                env = new MazeVacuumEnvironment(10, 10, 0.8, 0.3);
                break;
        }

        switch (taskPaneCtrl.getParamValueIndex(PARAM_AGENT)) {

            case 0:
                agent = new MyVacuumAgent();   // Muazzam
                break;
            case 1:
                agent = new TableDrivenVacuumAgent();
                break;
            case 2:
                agent = new ReflexVacuumAgent();
                break;
            case 3:
                agent = new SimpleReflexVacuumAgent();
                break;
            case 4:
                agent = new ModelBasedReflexVacuumAgent();
                break;
            case 5:
                agent = new NondeterministicSearchAgent<>(VacuumWorldFunctions::getState, env);
                break;
            case 6:
                agent = new RandomWalkVacuumAgent();
                break;
        }

        if (env != null && agent != null) {
            envViewCtrl.initialize(env);
            env.addEnvironmentListener(envViewCtrl);
            env.addAgent(agent);
        }
    }

    /**
     * Starts the experiment.
     */
    public void startExperiment() {

        if (agent instanceof NondeterministicSearchAgent) {
            NondeterministicProblem<VacuumEnvironmentState, Action> problem =
                    new NondeterministicProblem<>(env.getCurrentState(),
                            VacuumWorldFunctions::getActions,
                            VacuumWorldFunctions.createResultsFunctionFor(agent),
                            VacuumWorldFunctions::testGoal,
                            (s, a, sPrimed) -> 1.0);

            ((NondeterministicSearchAgent<VacuumPercept, VacuumEnvironmentState, Action>) agent)
                    .makePlan(problem);
        }

        while (!env.isDone() && !Tasks.currIsCancelled()) {
            env.step();
            taskPaneCtrl.setStatus("Performance=" + env.getPerformanceMeasure(agent));
            taskPaneCtrl.waitAfterStep();
        }

        envViewCtrl.notify("Performance=" + env.getPerformanceMeasure(agent));
    }

    @Override
    public void cleanup() {
        taskPaneCtrl.cancelExecution();
    }
}