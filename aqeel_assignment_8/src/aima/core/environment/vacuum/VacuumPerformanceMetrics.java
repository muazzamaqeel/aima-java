package aima.core.environment.vacuum;

import aima.core.agent.impl.SimpleAgent;
import aima.core.agent.Action;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

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

    public void show(List<SimpleAgent<VacuumPercept, Action>> agents,
                     List<String> selectedAlgorithms) {
        Platform.runLater(() -> {
            stepLabels.clear();
            moveLabels.clear();
            suckLabels.clear();
            cleanedLabels.clear();
            timeLabels.clear();
            performanceLabels.clear();
            resultLabels.clear();

            stage = new Stage();
            stage.setTitle("Performance Measure");

            root = new VBox(12);
            root.setPadding(new Insets(15));

            for (int i = 0; i < agents.size(); i++) {
                Label headingLabel = new Label("Agent " + (i + 1));
                headingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                Label algorithmLabel = new Label("Algorithm: " + selectedAlgorithms.get(i));
                Label stepLabel = new Label("Steps: 0");
                Label moveLabel = new Label("Moves: 0");
                Label suckLabel = new Label("Suck Actions: 0");
                Label cleanedLabel = new Label("Own Dirt Cleaned: 0");
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

                root.getChildren().add(agentBox);
            }

            Scene scene = new Scene(root, 390, 220 + agents.size() * 155);
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
                int cleaned = VacuumLayeredDirtManager.getCleanedLayers(i);
                double performance = env.getPerformanceMeasure(agent);

                if (agent instanceof SmartMazeVacuumAgent) {
                    SmartMazeVacuumAgent smartAgent = (SmartMazeVacuumAgent) agent;

                    steps = smartAgent.getStepCount();
                    moves = smartAgent.getMoveCount();
                    sucks = smartAgent.getSuckCount();
                    performance = smartAgent.getOriginalStylePerformance();
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
                    cleanedLabels.get(i).setText("Own Dirt Cleaned: " + cleaned);
                    timeLabels.get(i).setText("Time: " + displayedTimeMs + " ms");
                    performanceLabels.get(i).setText("Performance: " + performance);
                    resultLabels.get(i).setText("Result: " + resultStatus);
                }
            }
        });
    }
}