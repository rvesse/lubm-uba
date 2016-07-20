package edu.lehigh.swat.bench.uba.writers.graphml;

import java.io.PrintStream;

public interface PropertyGraphFormatter {

    public void formatNode(Node n, PrintStream output);

    public void formatEdge(Edge e, PrintStream output);

    /**
     * Called when a new file is started so any state tracking can be reset
     */
    public void newFile();
}
