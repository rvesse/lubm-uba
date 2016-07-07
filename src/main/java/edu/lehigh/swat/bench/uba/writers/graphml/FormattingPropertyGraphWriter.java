package edu.lehigh.swat.bench.uba.writers.graphml;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;

public abstract class FormattingPropertyGraphWriter extends PropertyGraphWriter {

    protected final PropertyGraphFormatter formatter;

    public FormattingPropertyGraphWriter(GeneratorCallbackTarget callbackTarget, PropertyGraphFormatter formatter) {
        super(callbackTarget);
        if (formatter == null)
            throw new NullPointerException("formatter cannot be null");
        this.formatter = formatter;
    }

    @Override
    protected void writeNode(Node n) {
        this.formatter.formatNode(n, this.out);
    }

    @Override
    protected void writeEdge(Edge e) {
        this.formatter.formatEdge(e, this.out);
    }

}
