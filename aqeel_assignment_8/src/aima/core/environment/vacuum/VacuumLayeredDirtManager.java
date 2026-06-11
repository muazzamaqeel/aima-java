package aima.core.environment.vacuum;

import aima.core.agent.Agent;
import aima.core.environment.vacuum.VacuumEnvironment.LocationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class VacuumLayeredDirtManager {

    private static final Map<String, Set<Integer>> remainingDirtLayers = new HashMap<>();
    private static final Map<Agent, Integer> agentIndexes = new HashMap<>();
    private static final Map<Integer, Integer> cleanedLayersByAgent = new HashMap<>();

    private static int agentCount = 1;
    private static boolean initialized = false;

    public static void initialize(VacuumEnvironment env, int numberOfAgents) {
        remainingDirtLayers.clear();
        agentIndexes.clear();
        cleanedLayersByAgent.clear();

        agentCount = numberOfAgents;
        initialized = true;

        for (int i = 0; i < agentCount; i++) {
            cleanedLayersByAgent.put(i, 0);
        }

        for (String location : env.getLocations()) {
            if (env.getLocationState(location) == LocationState.Dirty) {
                Set<Integer> layers = new HashSet<>();

                for (int i = 0; i < agentCount; i++) {
                    layers.add(i);
                }

                remainingDirtLayers.put(location, layers);
            }
        }
    }

    public static boolean isInitialized() {
        return initialized;
    }

    public static void registerAgent(Agent agent, int agentIndex) {
        agentIndexes.put(agent, agentIndex);
    }

    public static int getAgentIndex(Agent agent) {
        if (!agentIndexes.containsKey(agent)) {
            agentIndexes.put(agent, agentIndexes.size());
        }

        return agentIndexes.get(agent);
    }

    public static boolean hasDirtForAgent(int agentIndex, String location) {
        Set<Integer> layers = remainingDirtLayers.get(location);

        return layers != null && layers.contains(agentIndex);
    }

    public static boolean hasRemainingDirtForAgent(int agentIndex) {
        for (Set<Integer> layers : remainingDirtLayers.values()) {
            if (layers.contains(agentIndex)) {
                return true;
            }
        }

        return false;
    }

    public static boolean cleanDirtForAgent(int agentIndex, String location) {
        Set<Integer> layers = remainingDirtLayers.get(location);

        if (layers != null && layers.remove(agentIndex)) {
            cleanedLayersByAgent.put(agentIndex, getCleanedLayers(agentIndex) + 1);

            if (layers.isEmpty()) {
                remainingDirtLayers.remove(location);
            }

            return true;
        }

        return false;
    }

    public static ArrayList<Integer> getRemainingDirtLayers(String location) {
        Set<Integer> layers = remainingDirtLayers.get(location);

        if (layers == null) {
            return new ArrayList<>();
        }

        return new ArrayList<>(layers);
    }

    public static int getCleanedLayers(int agentIndex) {
        return cleanedLayersByAgent.getOrDefault(agentIndex, 0);
    }

    public static boolean isAllClean() {
        return remainingDirtLayers.isEmpty();
    }
}