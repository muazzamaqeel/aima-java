package aqeel.osm.routing;

import aima.core.search.framework.Node;
import aima.core.search.framework.SearchForActions;
import aima.core.search.framework.problem.Problem;
import aima.core.search.framework.qsearch.GraphSearch;
import aima.core.search.informed.AStarSearch;
import aima.core.util.Tasks;
import aimax.osm.data.MapWayAttFilter;
import aimax.osm.data.MapWayFilter;
import aimax.osm.data.OsmMap;
import aimax.osm.data.Position;
import aimax.osm.data.entities.MapNode;
import aimax.osm.routing.OsmMoveAction;
import aimax.osm.routing.RouteCalculator;
import aimax.osm.routing.RouteFindingProblem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToDoubleFunction;

/**
 * Implements a search engine for shortest path calculations. Modified versions
 * can be implemented quite easily by overriding the various factory methods.
 *
 * @author Ruediger Lunde
 */
public class ExtendedRouteCalculator extends RouteCalculator {
	/** Returns the names of all supported way selection options. */
	public String[] getTaskSelectionOptions() {
		return new String[] {
				"Distance",
				"Distance (Car)",
				"Distance (Bike)",
				"Time (Car)",        // NEW
				"Fun (Cyclist)"      // NEW
		};
	}

	public List<Position> calculateRoute(List<MapNode> markers, OsmMap map,
	                                     int taskSelection) {
		List<Position> result = new ArrayList<>();
		try {
			MapWayFilter wayFilter = createMapWayFilter(map, taskSelection);
			boolean ignoreOneways = (taskSelection == 0);
			List<MapNode[]> pNodeList = subdivideProblem(markers, map, wayFilter);
			MapNode prevNode = null;
			for (int i = 0; i < pNodeList.size()
					&& !Tasks.currIsCancelled(); i++) {
				Problem<MapNode, OsmMoveAction> problem = createProblem(pNodeList.get(i), map, wayFilter,
						ignoreOneways, taskSelection);
				ToDoubleFunction<Node<MapNode, OsmMoveAction>> h = createHeuristicFunction(pNodeList.get(i),
						taskSelection);
				SearchForActions<MapNode, OsmMoveAction> search = createSearch(h, taskSelection);
				Optional<List<OsmMoveAction>> actions = search.findActions(problem);
				if (!actions.isPresent())
					break;
				for (OsmMoveAction action : actions.get()) {
					for (MapNode node : action.getNodes()) {
						if (prevNode != node) {
							result.add(new Position(node.getLat(), node.getLon()));
							prevNode = node;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	protected MapWayFilter createMapWayFilter(OsmMap map, int taskSelection) {
		if (taskSelection == 1)
			return MapWayAttFilter.createCarWayFilter();
		else if (taskSelection == 2)
			return MapWayAttFilter.createBicycleWayFilter();
		else
			return MapWayAttFilter.createAnyWayFilter();
	}

	protected List<MapNode[]> subdivideProblem(List<MapNode> markers,
	                                           OsmMap map, MapWayFilter wayFilter) {
		List<MapNode[]> result = new ArrayList<>();
		MapNode fromNode = map.getNearestWayNode(new Position(markers.get(0)),
				wayFilter);
		for (int i = 1; i < markers.size(); i++) {
			MapNode toNode = map.getNearestWayNode(
					new Position(markers.get(i)), wayFilter);
			result.add(new MapNode[] { fromNode, toNode });
			fromNode = toNode;
		}
		return result;
	}

	protected Problem<MapNode, OsmMoveAction> createProblem(MapNode[] pNodes, OsmMap map,
	                                                        MapWayFilter wayFilter, boolean ignoreOneways, int taskSelection) {
		return new RouteFindingProblem(
				pNodes[0],
				pNodes[1],
				wayFilter,
				ignoreOneways,
				(from, action, to) -> getCost(from, action, to, taskSelection)
		);
	}

	protected ToDoubleFunction<Node<MapNode, OsmMoveAction>> createHeuristicFunction(
			MapNode[] pNodes, int taskSelection) {

		MapNode goal = pNodes[1];

		return node -> {
			double distance = getDistance(node.getState(), goal);

			if (taskSelection == 3) {
				return distance / 120.0;
			}

			if (taskSelection == 4) {
				return distance;
			}

			return distance;
		};
	}

	protected SearchForActions<MapNode, OsmMoveAction> createSearch
			(ToDoubleFunction<Node<MapNode, OsmMoveAction>> h, int taskSelection) {
		return new AStarSearch<>(new GraphSearch<>(), h);
	}

	private double getCost(MapNode from, OsmMoveAction action, MapNode to, int taskSelection) {

		double distance = getDistance(from, to);

		if (taskSelection <= 2) {
			return distance;
		}

		String type = action.getWay().getAttributeValue("highway");

		if (taskSelection == 3) {

			double speed;

			if ("motorway".equals(type)) speed = 120;
			else if ("primary".equals(type)) speed = 80;
			else if ("secondary".equals(type)) speed = 60;
			else if ("residential".equals(type)) speed = 50;
			else speed = 30;

			return distance / speed;
		}

		if (taskSelection == 4) {

			if ("cycleway".equals(type)) return distance * 0.5;
			if ("residential".equals(type)) return distance;
			if ("primary".equals(type)) return distance * 3.0;

			return distance * 2.0;
		}

		return distance;
	}

	private double getDistance(MapNode a, MapNode b) {
		return Position.getDistKM(
				a.getLat(), a.getLon(),
				b.getLat(), b.getLon()
		);
	}
}