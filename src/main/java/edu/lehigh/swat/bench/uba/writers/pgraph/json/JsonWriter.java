package edu.lehigh.swat.bench.uba.writers.pgraph.json;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.writers.graphml.SegregatedFormattingPropertyGraphWriter;

public class JsonWriter extends SegregatedFormattingPropertyGraphWriter {

    public JsonWriter(GeneratorCallbackTarget callbackTarget) {
        super(callbackTarget, new JsonFormatter());
    }

}
