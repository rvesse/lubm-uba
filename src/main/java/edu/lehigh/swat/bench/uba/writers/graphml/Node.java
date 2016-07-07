package edu.lehigh.swat.bench.uba.writers.graphml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {

    private final String id;
    private final List<String> labels = new ArrayList<>();
    private final Map<String, String> properties = new HashMap<String, String>();

    public Node(String id) {
        this(id, (String[])null);
    }

    public Node(String id, String... labels) {
        this.id = id;
        if (labels != null) {
            for (String l : labels) {
                this.labels.add(l);
            }
        }
    }

    public String getId() {
        return this.id;
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public Map<String, String> getProperties() {
        return this.properties;
    }
}
