import aima.core.agent.Action;
import aima.core.agent.impl.SimpleAgent;
import aima.core.environment.vacuum.*;
import aima.core.environment.vacuum.algorithms.MazeSearchAlgorithm;
import aima.core.search.agent.NondeterministicSearchAgent;
import aima.core.search.nondeterministic.NondeterministicProblem;
import aima.core.util.Tasks;
import aima.gui.fx.framework.IntegrableApplication;
import aima.gui.fx.framework.Parameter;
import aima.gui.fx.framework.TaskExecutionPaneBuilder;
import aima.gui.fx.framework.TaskExecutionPaneCtrl;
import aima.gui.fx.views.SimpleEnvironmentViewCtrl;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// VM options (Java>8): --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml

/**
 * Integrable application which demonstrates how different kinds of vacuum
 * cleaner agents behave in a two square environment.
 *
 * @author Ruediger Lunde
 */
public class VacuumAgentApp extends IntegrableApplication {

    public static void main(String[] args) {
        launch(args);
    }

    public final static String PARAM_ENV = "environment";
    public final static String PARAM_AGENT = "agent";
    public final static String PARAM_AGENT_COUNT = "agents";
    public final static String PARAM_ALGORITHM_AGENT_1 = "algorithm agent 1";
    public final static String PARAM_ALGORITHM_AGENT_2 = "algorithm agent 2";
    public final static String PARAM_ALGORITHM_AGENT_3 = "algorithm agent 3";

    private static final String ALGORITHM_PACKAGE = "aima.core.environment.vacuum.algorithms";

    protected TaskExecutionPaneCtrl taskPaneCtrl;
    protected SimpleEnvironmentViewCtrl<VacuumPercept, Action> envViewCtrl;
    protected VacuumEnvironment env = null;
    protected SimpleAgent<VacuumPercept, Action> agent = null;

    private final List<SimpleAgent<VacuumPercept, Action>> agents = new ArrayList<>();
    private final List<String> selectedAlgorithms = new ArrayList<>();
    private final List<Long> agentFinishedTimesMs = new ArrayList<>();
    private final List<String> agentResultStatuses = new ArrayList<>();

    private VacuumPerformanceMetrics performanceMetrics;

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
        envViewCtrl = new VacuumEnvironmentViewCtrl_Extended(envView, action -> {
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

    protected List<Parameter> createParameters() {
        Parameter p1 = new Parameter(PARAM_ENV,
                "A/B Deterministic Environment",
                "A/B Non-Deterministic Environment",
                "Small Maze Environment",
                "Maze Environment");

        Parameter p2 = new Parameter(PARAM_AGENT,
                "TableDrivenVacuumAgent",
                "ReflexVacuumAgent",
                "SimpleReflexVacuumAgent",
                "ModelBasedReflexVacuumAgent",
                "NondeterministicVacuumAgent",
                "RandomWalkVacuumAgent",
                "SmartMazeVacuumAgent");

        Parameter p3 = new Parameter(PARAM_AGENT_COUNT,
                "1",
                "2",
                "3");

        List<String> algorithms = loadAlgorithmNames();

        Parameter p4 = new Parameter(PARAM_ALGORITHM_AGENT_1,
                algorithms.toArray(new String[0]));

        Parameter p5 = new Parameter(PARAM_ALGORITHM_AGENT_2,
                algorithms.toArray(new String[0]));

        Parameter p6 = new Parameter(PARAM_ALGORITHM_AGENT_3,
                algorithms.toArray(new String[0]));

        return Arrays.asList(p1, p2, p3, p4, p5, p6);
    }

    private List<String> loadAlgorithmNames() {
        List<String> algorithmNames = new ArrayList<>();

        try {
            String packagePath = ALGORITHM_PACKAGE.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL packageUrl = classLoader.getResource(packagePath);

            if (packageUrl != null && "file".equals(packageUrl.getProtocol())) {
                File packageDirectory = new File(packageUrl.toURI());
                File[] files = packageDirectory.listFiles();

                if (files != null) {
                    for (File file : files) {
                        String fileName = file.getName();

                        if (fileName.endsWith(".class")) {
                            String className = fileName.substring(0, fileName.length() - 6);

                            if (!className.contains("$")
                                    && !className.equals("SmartMazeVacuumAgent")
                                    && !className.equals("MazeSearchAlgorithm")) {

                                Class<?> clazz = Class.forName(ALGORITHM_PACKAGE + "." + className);

                                if (MazeSearchAlgorithm.class.isAssignableFrom(clazz)) {
                                    algorithmNames.add(className);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Collections.sort(algorithmNames);

        if (algorithmNames.isEmpty()) {
            algorithmNames.add("Breadth_First_Search");
            algorithmNames.add("Depth_First_Search");
            algorithmNames.add("Depth_Limited_Search");
            algorithmNames.add("Iterative_Deepening_Search");
            algorithmNames.add("Uniform_Cost_Search");
        }

        return algorithmNames;
    }

    /**
     * Is called after each parameter selection change.
     */
    @Override
    public void initialize() {
        switch (taskPaneCtrl.getParamValueIndex(PARAM_ENV)) {
            case 0:
                env = new VacuumEnvironment();
                break;
            case 1:
                env = new NondeterministicVacuumEnvironment();
                break;
            case 2:
                env = new MazeVacuumEnvironment(5, 5, 0.5, 0.2);
                break;
            case 3:
                env = new MazeVacuumEnvironment(10, 10, 0.8, 0.3);
                break;
        }

        agents.clear();
        selectedAlgorithms.clear();
        agentFinishedTimesMs.clear();
        agentResultStatuses.clear();

        int numberOfAgents = Integer.parseInt(taskPaneCtrl.getParamValue(PARAM_AGENT_COUNT).toString());

        String algorithmAgent1 = taskPaneCtrl.getParamValue(PARAM_ALGORITHM_AGENT_1).toString();
        String algorithmAgent2 = taskPaneCtrl.getParamValue(PARAM_ALGORITHM_AGENT_2).toString();
        String algorithmAgent3 = taskPaneCtrl.getParamValue(PARAM_ALGORITHM_AGENT_3).toString();

        String startLocation = getSharedStartLocation();
        int startX = env.getX(startLocation);
        int startY = env.getY(startLocation);

        VacuumLayeredDirtManager.initialize(env, numberOfAgents);

        if (numberOfAgents >= 1) {
            SimpleAgent<VacuumPercept, Action> agent1 = createSelectedAgent(algorithmAgent1, 0, startX, startY);
            agents.add(agent1);
            selectedAlgorithms.add(algorithmAgent1);
            VacuumLayeredDirtManager.registerAgent(agent1, 0);
        }

        if (numberOfAgents >= 2) {
            SimpleAgent<VacuumPercept, Action> agent2 = createSelectedAgent(algorithmAgent2, 1, startX, startY);
            agents.add(agent2);
            selectedAlgorithms.add(algorithmAgent2);
            VacuumLayeredDirtManager.registerAgent(agent2, 1);
        }

        if (numberOfAgents >= 3) {
            SimpleAgent<VacuumPercept, Action> agent3 = createSelectedAgent(algorithmAgent3, 2, startX, startY);
            agents.add(agent3);
            selectedAlgorithms.add(algorithmAgent3);
            VacuumLayeredDirtManager.registerAgent(agent3, 2);
        }

        for (int i = 0; i < agents.size(); i++) {
            agentFinishedTimesMs.add(-1L);
            agentResultStatuses.add("Running");
        }

        if (!agents.isEmpty()) {
            agent = agents.get(0);
        }

        if (env != null && !agents.isEmpty()) {
            envViewCtrl.initialize(env);
            env.addEnvironmentListener(envViewCtrl);

            for (SimpleAgent<VacuumPercept, Action> currentAgent : agents) {
                env.addAgent(currentAgent, startLocation);
            }
        }
    }

    private SimpleAgent<VacuumPercept, Action> createSelectedAgent(String selectedAlgorithm,
                                                                   int agentIndex,
                                                                   int startX,
                                                                   int startY) {
        switch (taskPaneCtrl.getParamValueIndex(PARAM_AGENT)) {
            case 0:
                return new TableDrivenVacuumAgent();
            case 1:
                return new ReflexVacuumAgent();
            case 2:
                return new SimpleReflexVacuumAgent();
            case 3:
                return new ModelBasedReflexVacuumAgent();
            case 4:
                return new NondeterministicSearchAgent<>(VacuumWorldFunctions::getState, env);
            case 5:
                return new RandomWalkVacuumAgent();
            case 6:
                return new SmartMazeVacuumAgent(selectedAlgorithm, agentIndex, startX, startY);
        }

        return new SmartMazeVacuumAgent(selectedAlgorithm, agentIndex, startX, startY);
    }

    private String getSharedStartLocation() {
        for (String location : env.getLocations()) {
            if (env.getLocationState(location) != null) {
                return location;
            }
        }

        return env.getLocations().get(0);
    }

    /**
     * Starts the experiment.
     */
    public void startExperiment() {
        performanceMetrics = new VacuumPerformanceMetrics();
        performanceMetrics.show(agents, selectedAlgorithms);

        long startTimeNano = System.nanoTime();

        for (SimpleAgent<VacuumPercept, Action> currentAgent : agents) {
            if (currentAgent instanceof NondeterministicSearchAgent) {
                NondeterministicProblem<VacuumEnvironmentState, Action> problem =
                        new NondeterministicProblem<>(env.getCurrentState(),
                                VacuumWorldFunctions::getActions,
                                VacuumWorldFunctions.createResultsFunctionFor(currentAgent),
                                VacuumWorldFunctions::testGoal,
                                (s, a, sPrimed) -> 1.0);
                // Set the problem now for this kind of agent
                ((NondeterministicSearchAgent<VacuumPercept, VacuumEnvironmentState, Action>) currentAgent).makePlan(problem);
            }
        }

        updateFinishedAgents(0);

        while (!isExperimentDone() && !Tasks.currIsCancelled()) {
            env.step();

            long elapsedTimeMs = (System.nanoTime() - startTimeNano) / 1_000_000;
            updateFinishedAgents(elapsedTimeMs);

            performanceMetrics.update(
                    agents,
                    env,
                    createDisplayedTimes(elapsedTimeMs),
                    new ArrayList<>(agentResultStatuses)
            );

            taskPaneCtrl.setStatus(createPerformanceText());
            taskPaneCtrl.waitAfterStep();
        }

        long totalTimeMs = (System.nanoTime() - startTimeNano) / 1_000_000;
        updateFinishedAgents(totalTimeMs);

        performanceMetrics.update(
                agents,
                env,
                createDisplayedTimes(totalTimeMs),
                new ArrayList<>(agentResultStatuses)
        );

        envViewCtrl.notify(createPerformanceText());
    }

    private void updateFinishedAgents(long elapsedTimeMs) {
        for (int i = 0; i < agents.size(); i++) {
            SimpleAgent<VacuumPercept, Action> currentAgent = agents.get(i);

            if (currentAgent instanceof SmartMazeVacuumAgent) {
                SmartMazeVacuumAgent smartAgent = (SmartMazeVacuumAgent) currentAgent;

                smartAgent.finishIfOwnDirtCleaned();

                if (smartAgent.hasCompletedOwnDirt()) {
                    setAgentFinished(i, elapsedTimeMs, "Finished");
                } else if (smartAgent.hasNoMorePossibleMoves()) {
                    setAgentFinished(i, elapsedTimeMs, "No more possible moves");
                } else {
                    if (i < agentResultStatuses.size()) {
                        agentResultStatuses.set(i, "Running");
                    }
                }
            }
        }
    }

    private void setAgentFinished(int index, long elapsedTimeMs, String resultStatus) {
        if (index < agentFinishedTimesMs.size() && agentFinishedTimesMs.get(index) < 0) {
            agentFinishedTimesMs.set(index, elapsedTimeMs);
        }

        if (index < agentResultStatuses.size()) {
            agentResultStatuses.set(index, resultStatus);
        }
    }

    private List<Long> createDisplayedTimes(long elapsedTimeMs) {
        List<Long> result = new ArrayList<>();

        for (int i = 0; i < agents.size(); i++) {
            long finishedTime = -1L;

            if (i < agentFinishedTimesMs.size()) {
                finishedTime = agentFinishedTimesMs.get(i);
            }

            if (finishedTime >= 0) {
                result.add(finishedTime);
            } else {
                result.add(elapsedTimeMs);
            }
        }

        return result;
    }

    private boolean isExperimentDone() {
        if (VacuumLayeredDirtManager.isInitialized() && VacuumLayeredDirtManager.isAllClean()) {
            return true;
        }

        boolean allSmartAgentsFinished = true;

        for (SimpleAgent<VacuumPercept, Action> currentAgent : agents) {
            if (currentAgent instanceof SmartMazeVacuumAgent) {
                if (!((SmartMazeVacuumAgent) currentAgent).isFinished()) {
                    allSmartAgentsFinished = false;
                    break;
                }
            } else {
                allSmartAgentsFinished = false;
                break;
            }
        }

        if (allSmartAgentsFinished) {
            return true;
        }

        if (VacuumLayeredDirtManager.isInitialized()) {
            return false;
        }

        return env.isDone();
    }

    private String createPerformanceText() {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < agents.size(); i++) {
            if (i > 0) {
                result.append(" | ");
            }

            result.append("Agent ")
                    .append(i + 1)
                    .append(" Performance=");

            SimpleAgent<VacuumPercept, Action> currentAgent = agents.get(i);

            if (currentAgent instanceof SmartMazeVacuumAgent) {
                result.append(((SmartMazeVacuumAgent) currentAgent).getOriginalStylePerformance());
            } else {
                result.append(env.getPerformanceMeasure(currentAgent));
            }
        }

        return result.toString();
    }

    @Override
    public void cleanup() {
        taskPaneCtrl.cancelExecution();
    }
}