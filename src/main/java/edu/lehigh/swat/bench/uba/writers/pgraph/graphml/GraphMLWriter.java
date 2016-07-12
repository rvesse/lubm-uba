package edu.lehigh.swat.bench.uba.writers.pgraph.graphml;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.writers.graphml.FormattingPropertyGraphWriter;

public class GraphMLWriter extends FormattingPropertyGraphWriter {

    public GraphMLWriter(GeneratorCallbackTarget callbackTarget, boolean neo4j) {
        super(callbackTarget, new GraphMLFormatter(neo4j));
    }

}
