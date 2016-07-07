package edu.lehigh.swat.bench.uba.writers.graphml;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;

public class GraphMLWriter extends FormattingPropertyGraphWriter {

    public GraphMLWriter(GeneratorCallbackTarget callbackTarget, boolean includeLabels) {
        super(callbackTarget, new GraphMLFormatter(includeLabels));
    }

}
