package edu.lehigh.swat.bench.uba.writers.pgraph.graphml;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.writers.graphml.SegregatedFormattingPropertyGraphWriter;

public class SegregatedGraphMLWriter extends SegregatedFormattingPropertyGraphWriter {

    public SegregatedGraphMLWriter(GeneratorCallbackTarget callbackTarget, boolean neo4j) {
        super(callbackTarget, new GraphMLFormatter(neo4j));
    }

}
