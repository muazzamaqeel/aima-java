import aima.gui.fx.framework.IntegrableApplication;
import aimax.osm.data.DataResource;
import aimax.osm.data.OsmMap;
import aimax.osm.data.Position;
import aimax.osm.data.entities.MapNode;
import aimax.osm.data.entities.Track;
import aimax.osm.gui.fx.viewer.MapPaneCtrl;
import aqeel.osm.routing.ExtendedRouteCalculator;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;

import java.text.DecimalFormat;
import java.util.List;

// VM options (Java>8): --module-path ${PATH_TO_FX} --add-modules javafx.controls,javafx.fxml

/**
 * Extendable application for route planning based on a real map of the city of Ulm.
 *
 * @author Ruediger Lunde
 */
public class ExtendedRoutePlannerOsmApp extends IntegrableApplication {

    /**
     * Entry point of the application.
     * Launches the JavaFX application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    private ComboBox<String> taskCombo;
    private Button calcBtn;
    private Label statusLabel;
    private MapPaneCtrl mapPaneCtrl;

    private ExtendedRouteCalculator routeCalculator;

    /**
     * Returns the title of the application window.
     * Used by the framework for display.
     * @return application title
     */
    @Override
    public String getTitle() {
        return "OSM Route Planner App";
    }

    /**
     * Creates and initializes the main UI layout.
     * Sets up toolbar, map view, and status display.
     * @return root pane of the application
     */
    @Override
    public Pane createRootPane() {

        routeCalculator = createRouteCalculator();

        BorderPane root = new BorderPane();

        ToolBar toolBar = new ToolBar();
        Button clearBtn = new Button("Clear");
        clearBtn.setOnAction(ev -> initialize());

        taskCombo = new ComboBox<>();
        taskCombo.getItems().addAll(routeCalculator.getTaskSelectionOptions());
        taskCombo.getSelectionModel().select(0);

        calcBtn = new Button("Calculate Route");
        calcBtn.setOnAction(ev -> calculateRoute());
        toolBar.getItems().addAll(clearBtn, new Separator(), taskCombo, calcBtn);
        root.setTop(toolBar);

        StackPane mapPane = new StackPane();
        mapPaneCtrl = new MapPaneCtrl(mapPane);
        mapPaneCtrl.getMap().addMapDataEventListener(ev -> updateEnabledState());
        mapPaneCtrl.loadMap(DataResource.getUlmFileResource());

        root.setCenter(mapPane);

        statusLabel = new Label();
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setAlignment(Pos.CENTER);
        statusLabel.setFont(Font.font(16));
        root.setBottom(statusLabel);
        return root;
    }

    /**
     * Factory method for creating the route calculator.
     * Can be overridden to provide custom routing algorithms.
     * @return instance of ExtendedRouteCalculator
     */
    protected ExtendedRouteCalculator createRouteCalculator() {
        return new ExtendedRouteCalculator();
    }

    /**
     * Resets the application state.
     * Clears all markers and tracks from the map.
     */
    @Override
    public void initialize() {
        mapPaneCtrl.getMap().clearMarkersAndTracks();
        statusLabel.setText("");
    }

    /**
     * Performs cleanup when the application is closed.
     * Currently no cleanup actions are required.
     */
    @Override
    public void cleanup() {
        // nothing to do here...
    }

    /**
     * Calculates and displays the route between selected markers.
     * Updates the map and status information.
     */
    public void calculateRoute() {
        OsmMap map = mapPaneCtrl.getMap();
        List<Position> positions = routeCalculator.calculateRoute(
                map.getMarkers(), map, taskCombo.getSelectionModel().getSelectedIndex());
        mapPaneCtrl.getMap().createTrack("Route", positions);

        Track track = mapPaneCtrl.getMap().getTrack("Route");
        if (taskCombo.getSelectionModel().getSelectedIndex() == 3) {
            statusLabel.setText(getTimeTrackInfo(track));
        } else {
            statusLabel.setText(getTrackInfo(track));
        }
    }

    /**
     * Enables or disables the calculate button.
     * Button is enabled only if at least two markers are set.
     */
    protected void updateEnabledState() {
        calcBtn.setDisable(mapPaneCtrl.getMap().getMarkers().size() < 2);
    }

    /**
     * Computes and returns information about the track length.
     * Also includes direction if only two nodes are present.
     * @param track the route track
     * @return formatted track information string
     */
    protected String getTrackInfo(Track track) {
        List<MapNode> nodes = track.getNodes();
        DecimalFormat f1 = new DecimalFormat("#0.00");
        double km = Position.getTrackLengthKM(nodes);
        String info = track.getName() + ": Length " + f1.format(km)
                + " km";
        if (nodes.size() == 2) {
            DecimalFormat f2 = new DecimalFormat("#000");
            MapNode m1 = nodes.get(nodes.size() - 2);
            MapNode m2 = nodes.get(nodes.size() - 1);
            int course = new Position(m1).getCourseTo(m2);
            info += "; Direction " + f2.format(course);
        }
        return info;
    }

    /**
     * Computes and returns estimated travel time for the track.
     * Also includes direction if only two nodes are present.
     * @param track the route track
     * @return formatted time information string
     */
    protected String getTimeTrackInfo(Track track) {
        List<MapNode> nodes = track.getNodes();
        DecimalFormat f1 = new DecimalFormat("#0.00");
        double hours = estimateTime(nodes);
        String info = track.getName() + ": Estimated Time " + f1.format(hours)
                + " h";
        if (nodes.size() == 2) {
            DecimalFormat f2 = new DecimalFormat("#000");
            MapNode m1 = nodes.get(nodes.size() - 2);
            MapNode m2 = nodes.get(nodes.size() - 1);
            int course = new Position(m1).getCourseTo(m2);
            info += "; Direction " + f2.format(course);
        }
        return info;
    }

    /**
     * Estimates travel time based on distance between nodes.
     * Uses a constant speed assumption.
     * @param nodes list of map nodes forming the route
     * @return estimated time in hours
     */
    protected double estimateTime(List<MapNode> nodes) {
        double time = 0.0;
        for (int i = 0; i < nodes.size() - 1; i++) {
            MapNode a = nodes.get(i);
            MapNode b = nodes.get(i + 1);

            double distance = Position.getDistKM(
                    a.getLat(), a.getLon(),
                    b.getLat(), b.getLon()
            );

            double speed = 50.0;
            time += distance / speed;
        }
        return time;
    }
}