package edu.lehigh.swat.bench.uba.writers.graphml;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;

public class SegregatedGraphMLWriter extends SegregatedFormattingPropertyGraphWriter {

    public SegregatedGraphMLWriter(GeneratorCallbackTarget callbackTarget, boolean neo4j) {
        super(callbackTarget, new GraphMLFormatter(neo4j));
    }

}
