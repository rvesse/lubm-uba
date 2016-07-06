package edu.lehigh.swat.bench.uba.writers.graphml;

public class Edge {

    private final String label, source, target;
    
    public Edge(String label, String source, String target) {
        this.label = label;
        this.source = source;
        this.target = target;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public String getSource() {
        return this.source;
    }
    
    public String getTarget() {
        return this.target;
    }
}
