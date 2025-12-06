package org.example.View;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphDt {
	@JsonProperty("nodes")
	private Map<String, Map<String, Object>> nodes;
	//private Map<String, Map<String, Integer>> nodes;
	@JsonProperty("edges")
	private List<Edge> edges;
	private long time;

	public GraphDt() {
		this.nodes = new HashMap<>();
		this.edges = new ArrayList<>();
	}

	public void setTime(long time) {this.time = time;}

	public long getTime() {return time;}

	public List<Edge> getEdges() {
		return edges;
	}

	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}

	public Map<String, Map<String, Object>> getNodes() {
		return nodes;
	}

	public void addNode(String id, String label, Integer level) {
		Map<String, Object> tmp = new HashMap<>();
		tmp.put("label", label);
		tmp.put("level", level);
		tmp.put("rank", 0);
		nodes.put(id, tmp);
	}


	public void setNodeRank(String id, double rank) {
		Map<String, Object> nodeData = nodes.get(id);
		if (nodeData != null) {
			nodeData.put("rank", rank);
		}
	}
}