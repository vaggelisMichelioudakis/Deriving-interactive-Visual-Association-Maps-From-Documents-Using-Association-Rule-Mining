package org.example.View;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Edge {

    private double lift;
    @JsonProperty("from")
    private String from;

    @JsonProperty("to")
    private String to;

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("support")
    private double support;
    public Edge(String from, String to, double conf, double support, double lift){
        this.to = to;
        this.from = from;
        this.confidence = conf;
        this.support = support;
        this.lift = lift;
    }

    public double getConfidence() {
        return confidence;
    }
    public String getFrom() {
        return from;
    }
    public String getTo() {
        return to;
    }
    public double getSupport() {
        return support;
    }
}