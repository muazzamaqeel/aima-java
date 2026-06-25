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
import java.util.Locale;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

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
    public final static String PARAM_RUN_COUNT = "runs";

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

        Parameter p7 = new Parameter(PARAM_RUN_COUNT,
                "1",
                "3",
                "5",
                "10",
                "20",
                "50");

        return Arrays.asList(p1, p2, p3, p4, p5, p6, p7);
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

    private void initializeForNextRunOnFxThread() {
        if (Platform.isFxApplicationThread()) {
            initialize();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<RuntimeException> exception = new AtomicReference<>();

        Platform.runLater(() -> {
            try {
                initialize();
            } catch (RuntimeException e) {
                exception.set(e);
            } finally {
                latch.countDown();
            }
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        if (exception.get() != null) {
            throw exception.get();
        }
    }

    /**
     * Starts the experiment.
     */
    public void startExperiment() {
        int totalRunCount = getSelectedRunCount();

        performanceMetrics = new VacuumPerformanceMetrics();

        List<Double> totalPerformances = new ArrayList<>();
        List<Long> totalTimesMs = new ArrayList<>();
        List<Integer> totalMoves = new ArrayList<>();

        int completedRuns = 0;

        for (int runNumber = 1; runNumber <= totalRunCount && !Tasks.currIsCancelled(); runNumber++) {
            initializeForNextRunOnFxThread();

            if (runNumber == 1) {
                initializeAverageLists(totalPerformances, totalTimesMs, totalMoves, agents.size());
                performanceMetrics.show(agents, selectedAlgorithms, totalRunCount);
            }

            RunResult runResult = runSingleExperiment(runNumber, totalRunCount);

            completedRuns++;
            addRunResultToTotals(runResult, totalPerformances, totalTimesMs, totalMoves);

            if (totalRunCount > 1) {
                performanceMetrics.addRunResult(
                        runNumber,
                        runResult.performances,
                        runResult.timesMs,
                        runResult.moves
                );
            }

            if (totalRunCount > 1) {
                List<Double> averagePerformances = calculateAveragePerformances(totalPerformances, completedRuns);
                List<Double> averageTimesMs = calculateAverageTimesMs(totalTimesMs, completedRuns);
                List<Double> averageMoves = calculateAverageMoves(totalMoves, completedRuns);

                performanceMetrics.updateAverages(
                        completedRuns,
                        totalRunCount,
                        averagePerformances,
                        averageTimesMs,
                        averageMoves
                );

                taskPaneCtrl.setStatus(createAveragePerformanceText(
                        completedRuns,
                        totalRunCount,
                        averagePerformances,
                        averageTimesMs,
                        averageMoves
                ));
            }
        }

        if (completedRuns > 0) {
            if (totalRunCount > 1) {
                List<Double> averagePerformances = calculateAveragePerformances(totalPerformances, completedRuns);
                List<Double> averageTimesMs = calculateAverageTimesMs(totalTimesMs, completedRuns);
                List<Double> averageMoves = calculateAverageMoves(totalMoves, completedRuns);

                envViewCtrl.notify(createAveragePerformanceText(
                        completedRuns,
                        totalRunCount,
                        averagePerformances,
                        averageTimesMs,
                        averageMoves
                ));
            } else {
                envViewCtrl.notify(createPerformanceText());
            }
        }
    }

    private RunResult runSingleExperiment(int runNumber, int totalRunCount) {
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

        performanceMetrics.update(
                agents,
                env,
                createDisplayedTimes(0),
                new ArrayList<>(agentResultStatuses)
        );

        taskPaneCtrl.setStatus(createRunStatusText(runNumber, totalRunCount));

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

            taskPaneCtrl.setStatus(createRunStatusText(runNumber, totalRunCount));
            taskPaneCtrl.waitAfterStep();
        }

        long totalTimeMs = (System.nanoTime() - startTimeNano) / 1_000_000;
        updateFinishedAgents(totalTimeMs);

        List<Long> displayedTimesMs = createDisplayedTimes(totalTimeMs);

        performanceMetrics.update(
                agents,
                env,
                displayedTimesMs,
                new ArrayList<>(agentResultStatuses)
        );

        taskPaneCtrl.setStatus(createRunStatusText(runNumber, totalRunCount));

        return createRunResult(displayedTimesMs);
    }

    private int getSelectedRunCount() {
        try {
            return Integer.parseInt(taskPaneCtrl.getParamValue(PARAM_RUN_COUNT).toString());
        } catch (Exception e) {
            return 1;
        }
    }

    private void initializeAverageLists(List<Double> totalPerformances,
                                        List<Long> totalTimesMs,
                                        List<Integer> totalMoves,
                                        int size) {
        totalPerformances.clear();
        totalTimesMs.clear();
        totalMoves.clear();

        for (int i = 0; i < size; i++) {
            totalPerformances.add(0.0);
            totalTimesMs.add(0L);
            totalMoves.add(0);
        }
    }

    private RunResult createRunResult(List<Long> displayedTimesMs) {
        List<Double> performances = new ArrayList<>();
        List<Long> timesMs = new ArrayList<>();
        List<Integer> moves = new ArrayList<>();

        for (int i = 0; i < agents.size(); i++) {
            performances.add(getAgentPerformance(i));

            if (i < displayedTimesMs.size()) {
                timesMs.add(displayedTimesMs.get(i));
            } else {
                timesMs.add(0L);
            }

            moves.add(getAgentMoves(i));
        }

        return new RunResult(performances, timesMs, moves);
    }

    private double getAgentPerformance(int index) {
        SimpleAgent<VacuumPercept, Action> currentAgent = agents.get(index);

        if (currentAgent instanceof SmartMazeVacuumAgent) {
            return ((SmartMazeVacuumAgent) currentAgent).getOriginalStylePerformance();
        }

        return env.getPerformanceMeasure(currentAgent);
    }

    private int getAgentMoves(int index) {
        SimpleAgent<VacuumPercept, Action> currentAgent = agents.get(index);

        if (currentAgent instanceof SmartMazeVacuumAgent) {
            return ((SmartMazeVacuumAgent) currentAgent).getMoveCount();
        }

        return 0;
    }

    private void addRunResultToTotals(RunResult runResult,
                                      List<Double> totalPerformances,
                                      List<Long> totalTimesMs,
                                      List<Integer> totalMoves) {
        for (int i = 0; i < runResult.performances.size(); i++) {
            totalPerformances.set(i, totalPerformances.get(i) + runResult.performances.get(i));
            totalTimesMs.set(i, totalTimesMs.get(i) + runResult.timesMs.get(i));
            totalMoves.set(i, totalMoves.get(i) + runResult.moves.get(i));
        }
    }

    private List<Double> calculateAveragePerformances(List<Double> totalPerformances, int completedRuns) {
        List<Double> result = new ArrayList<>();

        for (Double totalPerformance : totalPerformances) {
            result.add(totalPerformance / completedRuns);
        }

        return result;
    }

    private List<Double> calculateAverageTimesMs(List<Long> totalTimesMs, int completedRuns) {
        List<Double> result = new ArrayList<>();

        for (Long totalTimeMs : totalTimesMs) {
            result.add(totalTimeMs / (double) completedRuns);
        }

        return result;
    }

    private List<Double> calculateAverageMoves(List<Integer> totalMoves, int completedRuns) {
        List<Double> result = new ArrayList<>();

        for (Integer totalMove : totalMoves) {
            result.add(totalMove / (double) completedRuns);
        }

        return result;
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

    private String createRunStatusText(int runNumber, int totalRunCount) {
        if (totalRunCount > 1) {
            return "Run " + runNumber + "/" + totalRunCount + " | " + createPerformanceText();
        }

        return createPerformanceText();
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

    private String createAveragePerformanceText(int completedRuns,
                                                int totalRunCount,
                                                List<Double> averagePerformances,
                                                List<Double> averageTimesMs,
                                                List<Double> averageMoves) {
        StringBuilder result = new StringBuilder();

        result.append("Average after ")
                .append(completedRuns)
                .append("/")
                .append(totalRunCount)
                .append(" runs: ");

        for (int i = 0; i < averagePerformances.size(); i++) {
            if (i > 0) {
                result.append(" | ");
            }

            result.append("Agent ")
                    .append(i + 1)
                    .append(" Avg Performance=")
                    .append(String.format(Locale.US, "%.2f", averagePerformances.get(i)))
                    .append(", Avg Time=")
                    .append(String.format(Locale.US, "%.2f", averageTimesMs.get(i)))
                    .append(" ms")
                    .append(", Avg Moves=")
                    .append(String.format(Locale.US, "%.2f", averageMoves.get(i)));
        }

        return result.toString();
    }

    @Override
    public void cleanup() {
        taskPaneCtrl.cancelExecution();
    }

    private static class RunResult {
        private final List<Double> performances;
        private final List<Long> timesMs;
        private final List<Integer> moves;

        private RunResult(List<Double> performances,
                          List<Long> timesMs,
                          List<Integer> moves) {
            this.performances = performances;
            this.timesMs = timesMs;
            this.moves = moves;
        }
    }
}