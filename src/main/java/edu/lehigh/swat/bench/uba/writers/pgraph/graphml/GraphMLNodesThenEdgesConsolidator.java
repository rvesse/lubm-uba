package edu.lehigh.swat.bench.uba.writers.pgraph.graphml;

import java.util.LinkedList;
import java.util.Queue;

public class GraphMLNodesThenEdgesConsolidator extends GraphMLConsolidator {

    private final Queue<String> edgeFiles = new LinkedList<>();

    public GraphMLNodesThenEdgesConsolidator(String targetFilename) {
        super(targetFilename);
    }

    @Override
    protected String nextFile() {
        while (true) {
            String file = super.nextFile();
            if (file == null) {
                if (this.finished) {
                    // Can consolidate edge files so provide the next available
                    synchronized (this.edgeFiles) {
                        return this.edgeFiles.poll();
                    }
                } else {
                    // No work for now
                    return null;
                }
            }

            // Check if it is an edge file?
            if (file.endsWith("edges.graphml") || file.endsWith("edges.graphml.gz")) {
                // Queue it for later consolidation
                synchronized (this.edgeFiles) {
                    this.edgeFiles.add(file);
                }
            } else {
                // If it is a nodes file can return it for consolidation
                // immediately
                return file;
            }
        }
    }
}
