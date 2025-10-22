package org.example.View;

import org.example.Taxonomy.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRank {
	public static Map<String, Double> applyPageRank(Map<String, Node> nodes, double dampingFactor, int maxIterations) {
		int N = nodes.size();
		Map<String, Double> rank = new HashMap<>();
		Map<String, List<String>> outgoing = new HashMap<>();

		// 1️⃣ Initialize ranks and outgoing edges
		for (Map.Entry<String, Node> entry : nodes.entrySet()) {
			String nodeId = entry.getKey();
			rank.put(nodeId, 1.0 / N);

			List<String> children = new ArrayList<>();
			for (Node child : entry.getValue().getchildren().keySet()) {
				children.add(child.getValue());
			}
			outgoing.put(nodeId, children);
		}

		// 2️⃣ Iterative PageRank updates
		for (int iter = 0; iter < maxIterations; iter++) {
			Map<String, Double> newRank = new HashMap<>();

			// Initialize new ranks with damping
			for (String nodeId : rank.keySet()) {
				newRank.put(nodeId, (1 - dampingFactor) / N);
			}

			// Distribute ranks
			for (String nodeId : rank.keySet()) {
				List<String> children = outgoing.get(nodeId);
				if (children == null || children.isEmpty()) continue;

				/* This version doesnt not count on other weights like confidence or support is simple PR*/
				double distributedRank = rank.get(nodeId) * dampingFactor / children.size();

				/*double totalWeight = entry.getValue().getchildren().values().stream()
						.mapToDouble(w -> w.get(1)) // confidence value
						.sum();
				for (Map.Entry<Node, List<Double>> child : entry.getValue().getchildren().entrySet()) {
					double weight = child.getValue().get(1); // confidence
					double distributedRank = rank.get(nodeId) * dampingFactor * (weight / totalWeight);
					newRank.merge(child.getKey().getValue(), distributedRank, Double::sum);
				}*/

				for (String childId : children) {
					newRank.merge(childId, distributedRank, Double::sum);
				}
			}

			rank = newRank;
		}

		// Normalize ranks
		double sum = rank.values().stream().mapToDouble(Double::doubleValue).sum();

		/*for (String key : rank.keySet()) {
			rank.put(key, rank.get(key) / sum);
		}*/

		//This might not work above is the normal one
		Map<String, Double> finalRank = rank;
		rank.replaceAll((k, v) -> finalRank.get(k) / sum);

		return rank;
	}

}
