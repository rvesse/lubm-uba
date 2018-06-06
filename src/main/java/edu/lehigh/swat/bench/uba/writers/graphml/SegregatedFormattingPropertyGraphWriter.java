package edu.lehigh.swat.bench.uba.writers.graphml;

import java.io.PrintStream;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.GlobalState;

public class SegregatedFormattingPropertyGraphWriter extends FormattingPropertyGraphWriter {

    protected PrintStream edgeOut;

    public SegregatedFormattingPropertyGraphWriter(GeneratorCallbackTarget callbackTarget,
            PropertyGraphFormatter formatter) {
        super(callbackTarget, formatter);
    }

    @Override
    public void startFile(String fileName, GlobalState state) {
        String extension = state.getFileExtension();
        String nodeFilename = fileName.replace(extension, "nodes" + extension);
        String edgeFilename = fileName.replace(extension, "edges" + extension);
        
        this.out = prepareOutputStream(nodeFilename, state);
        this.edgeOut = prepareOutputStream(edgeFilename, state);
        this.state = state;
        
        this.formatter.newFile();
    }
        
    @Override
    protected PrintStream getEdgeOutput() {
        return this.edgeOut;
    }

    @Override
    public void endFile(GlobalState state) {
        try {
            super.endFile(state);
        } finally {
            try {
                this.cleanupOutputStream(this.edgeOut);
                this.submitWrites();
            } finally {
                this.edgeOut = null;
            }
        }
    }

}
