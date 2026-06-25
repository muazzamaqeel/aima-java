package aima.core.environment.vacuum;

import aima.core.agent.impl.SimpleAgent;
import aima.core.agent.Action;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class VacuumPerformanceMetrics {

    private Stage stage;
    private VBox root;

    private final List<Label> stepLabels = new ArrayList<>();
    private final List<Label> moveLabels = new ArrayList<>();
    private final List<Label> suckLabels = new ArrayList<>();
    private final List<Label> cleanedLabels = new ArrayList<>();
    private final List<Label> timeLabels = new ArrayList<>();
    private final List<Label> performanceLabels = new ArrayList<>();
    private final List<Label> resultLabels = new ArrayList<>();

    private final List<Label> averageRunLabels = new ArrayList<>();
    private final List<Label> averagePerformanceLabels = new ArrayList<>();
    private final List<Label> averageTimeLabels = new ArrayList<>();
    private final List<Label> averageMoveLabels = new ArrayList<>();
    private final List<TextArea> runHistoryAreas = new ArrayList<>();

    private boolean showAverageSection = false;

    public void show(List<SimpleAgent<VacuumPercept, Action>> agents,
                     List<String> selectedAlgorithms,
                     int totalRunCount) {
        Platform.runLater(() -> {
            stepLabels.clear();
            moveLabels.clear();
            suckLabels.clear();
            cleanedLabels.clear();
            timeLabels.clear();
            performanceLabels.clear();
            resultLabels.clear();

            averageRunLabels.clear();
            averagePerformanceLabels.clear();
            averageTimeLabels.clear();
            averageMoveLabels.clear();
            runHistoryAreas.clear();

            showAverageSection = totalRunCount > 1;

            stage = new Stage();
            stage.setTitle("Performance Measure");

            root = new VBox(12);
            root.setPadding(new Insets(15));

            for (int i = 0; i < agents.size(); i++) {
                Label headingLabel = new Label("Agent " + (i + 1));
                headingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label algorithmLabel = new Label("Algorithm / Mode: " + selectedAlgorithms.get(i));
                Label stepLabel = new Label("Steps: 0");
                Label moveLabel = new Label("Moves: 0");
                Label suckLabel = new Label("Suck Actions: 0");
                Label cleanedLabel = new Label("Cleaned Dirt: 0");
                Label timeLabel = new Label("Time: 0 ms");
                Label performanceLabel = new Label("Performance: 0.0");
                Label resultLabel = new Label("Result: Running");

                VBox agentBox = new VBox(4);
                agentBox.setPadding(new Insets(8));
                agentBox.setStyle("-fx-border-color: lightgray; -fx-border-radius: 5; -fx-padding: 8;");

                agentBox.getChildren().addAll(
                        headingLabel,
                        algorithmLabel,
                        stepLabel,
                        moveLabel,
                        suckLabel,
                        cleanedLabel,
                        timeLabel,
                        performanceLabel,
                        resultLabel
                );

                stepLabels.add(stepLabel);
                moveLabels.add(moveLabel);
                suckLabels.add(suckLabel);
                cleanedLabels.add(cleanedLabel);
                timeLabels.add(timeLabel);
                performanceLabels.add(performanceLabel);
                resultLabels.add(resultLabel);

                if (showAverageSection) {
                    Separator separator = new Separator();

                    Label averageHeadingLabel = new Label("Average Results");
                    averageHeadingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                    Label averageRunLabel = new Label("Completed Runs: 0/" + totalRunCount);
                    Label averagePerformanceLabel = new Label("Average Performance: 0.00");
                    Label averageTimeLabel = new Label("Average Time: 0.00 ms");
                    Label averageMoveLabel = new Label("Average Moves: 0.00");

                    Label historyHeadingLabel = new Label("Run History / Cases");
                    historyHeadingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

                    TextArea runHistoryArea = new TextArea();
                    runHistoryArea.setEditable(false);
                    runHistoryArea.setWrapText(false);
                    runHistoryArea.setPrefRowCount(5);
                    runHistoryArea.setText("");

                    agentBox.getChildren().addAll(
                            separator,
                            averageHeadingLabel,
                            averageRunLabel,
                            averagePerformanceLabel,
                            averageTimeLabel,
                            averageMoveLabel,
                            historyHeadingLabel,
                            runHistoryArea
                    );

                    averageRunLabels.add(averageRunLabel);
                    averagePerformanceLabels.add(averagePerformanceLabel);
                    averageTimeLabels.add(averageTimeLabel);
                    averageMoveLabels.add(averageMoveLabel);
                    runHistoryAreas.add(runHistoryArea);
                }

                root.getChildren().add(agentBox);
            }

            int agentBoxHeight = showAverageSection ? 360 : 155;
            Scene scene = new Scene(root, 540, 220 + agents.size() * agentBoxHeight);
            stage.setScene(scene);
            stage.show();
        });
    }

    public void update(List<SimpleAgent<VacuumPercept, Action>> agents,
                       VacuumEnvironment env,
                       List<Long> displayedTimesMs,
                       List<String> resultStatuses) {
        Platform.runLater(() -> {
            for (int i = 0; i < agents.size(); i++) {
                SimpleAgent<VacuumPercept, Action> agent = agents.get(i);

                int steps = 0;
                int moves = 0;
                int sucks = 0;
                int cleaned = 0;
                double performance = env.getPerformanceMeasure(agent);

                if (agent instanceof SmartMazeVacuumAgent) {
                    SmartMazeVacuumAgent smartAgent = (SmartMazeVacuumAgent) agent;

                    steps = smartAgent.getStepCount();
                    moves = smartAgent.getMoveCount();
                    sucks = smartAgent.getSuckCount();
                    cleaned = smartAgent.getCleanedCount();
                    performance = smartAgent.getOriginalStylePerformance();
                } else if (VacuumLayeredDirtManager.isInitialized()) {
                    cleaned = VacuumLayeredDirtManager.getCleanedLayers(i);
                }

                long displayedTimeMs = 0;

                if (i < displayedTimesMs.size()) {
                    displayedTimeMs = displayedTimesMs.get(i);
                }

                String resultStatus = "Running";

                if (i < resultStatuses.size()) {
                    resultStatus = resultStatuses.get(i);
                }

                if (i < stepLabels.size()) {
                    stepLabels.get(i).setText("Steps: " + steps);
                    moveLabels.get(i).setText("Moves: " + moves);
                    suckLabels.get(i).setText("Suck Actions: " + sucks);
                    cleanedLabels.get(i).setText("Cleaned Dirt: " + cleaned);
                    timeLabels.get(i).setText("Time: " + displayedTimeMs + " ms");
                    performanceLabels.get(i).setText("Performance: " + performance);
                    resultLabels.get(i).setText("Result: " + resultStatus);
                }
            }
        });
    }

    public void updateAverages(int completedRuns,
                               int totalRunCount,
                               List<Double> averagePerformances,
                               List<Double> averageTimesMs,
                               List<Double> averageMoves) {
        if (!showAverageSection) {
            return;
        }

        Platform.runLater(() -> {
            for (int i = 0; i < averagePerformances.size(); i++) {
                if (i < averageRunLabels.size()) {
                    averageRunLabels.get(i).setText("Completed Runs: " + completedRuns + "/" + totalRunCount);
                    averagePerformanceLabels.get(i).setText("Average Performance: "
                            + String.format(Locale.US, "%.2f", averagePerformances.get(i)));
                    averageTimeLabels.get(i).setText("Average Time: "
                            + String.format(Locale.US, "%.2f", averageTimesMs.get(i)) + " ms");
                    averageMoveLabels.get(i).setText("Average Moves: "
                            + String.format(Locale.US, "%.2f", averageMoves.get(i)));
                }
            }
        });
    }

    public void addRunResult(int runNumber,
                             List<Double> performances,
                             List<Long> timesMs,
                             List<Integer> moves) {
        if (!showAverageSection) {
            return;
        }

        Platform.runLater(() -> {
            for (int i = 0; i < performances.size(); i++) {
                if (i < runHistoryAreas.size()) {
                    String line = "Run " + runNumber
                            + ": Performance="
                            + String.format(Locale.US, "%.2f", performances.get(i))
                            + ", Time="
                            + timesMs.get(i)
                            + " ms"
                            + ", Moves="
                            + moves.get(i)
                            + "\n";

                    runHistoryAreas.get(i).appendText(line);
                }
            }
        });
    }
}