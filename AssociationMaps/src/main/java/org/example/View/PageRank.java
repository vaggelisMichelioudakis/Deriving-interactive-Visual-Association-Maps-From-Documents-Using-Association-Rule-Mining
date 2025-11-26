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

			for (Map.Entry<String, Node> entry : nodes.entrySet()) {
				String nodeId = entry.getKey();
				Node currentNode = entry.getValue();

				// Get rank from *previous* iteration
				double currentRank = rank.get(nodeId);

				Map<Node, List<Double>> children = currentNode.getchildren();

				// FIX: Handle dangling nodes (no outgoing links)
				if (children == null || children.isEmpty()) {
					// Distribute this node's rank equally to ALL nodes
					double distributedRank = currentRank * dampingFactor / N;
					for (String allNodesId : rank.keySet()) {
						newRank.merge(allNodesId, distributedRank, Double::sum);
					}
				} else {
					// This node has weighted outgoing links
					// FIX: Calculate totalWeight based on this node's children
					double totalWeight = children.values().stream()
							.mapToDouble(w -> w.get(1)) // Assuming get(1) is confidence
							.sum();

					// FIX: Handle case where links exist but total weight is 0
					if (totalWeight == 0) {
						// Fallback: Treat as unweighted distribution among children
						double distributedRank = currentRank * dampingFactor / children.size();
						for (Node childNode : children.keySet()) {
							newRank.merge(childNode.getValue(), distributedRank, Double::sum);
						}
					} else {
						// Normal weighted distribution
						// FIX: This loop was in the wrong place and had scope errors
						for (Map.Entry<Node, List<Double>> childEntry : children.entrySet()) {
							String childId = childEntry.getKey().getValue();
							double weight = childEntry.getValue().get(1); // confidence

							// Distribute rank proportional to weight
							double distributedRank = currentRank * dampingFactor * (weight / totalWeight);
							newRank.merge(childId, distributedRank, Double::sum);
						}
					}
				}
			}
			rank = newRank; // Update rank map for next iteration
		}
		// Normalize ranks
		double sum = rank.values().stream().mapToDouble(Double::doubleValue).sum();
		if (sum == 0) return rank;

		/*for (String key : rank.keySet()) {
			rank.put(key, rank.get(key) / sum);
		}*/

		//This might not work above is the normal one
		Map<String, Double> finalRank = rank;
		rank.replaceAll((k, v) -> finalRank.get(k) / sum);

		return rank;
	}

}
