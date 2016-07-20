package edu.lehigh.swat.bench.uba.writers.pgraph.graphml;

import java.io.PrintStream;
import java.util.Map.Entry;

import edu.lehigh.swat.bench.uba.writers.graphml.Edge;
import edu.lehigh.swat.bench.uba.writers.graphml.Node;
import edu.lehigh.swat.bench.uba.writers.graphml.PropertyGraphFormatter;

public class GraphMLFormatter implements PropertyGraphFormatter {

    private final boolean neo4j;

    public GraphMLFormatter(boolean neo4j) {
        this.neo4j = neo4j;
    }

    @Override
    public void formatNode(Node n, PrintStream output) {
        output.print("    <node id=\"");
        output.print(n.getId());
        output.print('"');
        if (neo4j) {
            output.print(" labels=\"");
            for (int i = 0; i < n.getLabels().size(); i++) {
                if (i > 0)
                    output.print(',');
                output.print(n.getLabels().get(i));
            }
            output.print('"');
        }
        output.println('>');

        for (Entry<String, String> kvp : n.getProperties().entrySet()) {
            output.print("      <data key=\"");
            output.print(kvp.getKey());
            output.print("\">");
            output.print(kvp.getValue());
            output.println("</data>");
        }

        output.println("    </node>\n");
    }

    @Override
    public void formatEdge(Edge e, PrintStream output) {
        output.print("    <edge source=\"");
        output.print(e.getSource());
        output.print("\" target=\"");
        output.print(e.getTarget());
        output.print('"');
        if (neo4j) {
            output.print(" label=\"");
            output.print(e.getLabel());
            output.println("\"></edge>");
        } else {
            output.println('>');
            output.print("      <data key=\"type\">");
            output.print(e.getLabel());
            output.println("</data>");
            output.println("    </edge>");
        }
    }

    @Override
    public void newFile() {
        // No state to reset
    }

}
