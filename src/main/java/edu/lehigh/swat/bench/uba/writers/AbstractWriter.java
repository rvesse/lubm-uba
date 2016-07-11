package edu.lehigh.swat.bench.uba.writers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.GZIPOutputStream;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.GlobalState;
import edu.lehigh.swat.bench.uba.writers.utils.BufferSizes;
import edu.lehigh.swat.bench.uba.writers.utils.MemoryBufferedOutputStream;
import edu.lehigh.swat.bench.uba.writers.utils.WriteConsolidator;

public class AbstractWriter {

    /** white space string */
    protected static final String T_SPACE = " ";
    /** output stream */
    protected PrintStream out = null;
    /** the generator */
    protected GeneratorCallbackTarget callbackTarget;

    protected Map<String, GlobalState> currentFiles = new HashMap<>();

    public AbstractWriter(GeneratorCallbackTarget callbackTarget) {
        this.callbackTarget = callbackTarget;
    }

    /**
     * Prepares the output stream
     * 
     * @param fileName
     *            File name
     * @param state
     *            State
     */
    protected final PrintStream prepareOutputStream(String fileName, GlobalState state) {
        if (state.consolidationMode() != ConsolidationMode.Full) {
            try {
                // Track file for consolidation submission later
                currentFiles.put(fileName, state);

                // Prepare the output stream
                OutputStream stream = new FileOutputStream(fileName);
                if (fileName.endsWith(".gz")) {
                    stream = new GZIPOutputStream(stream, BufferSizes.GZIP_BUFFER_SIZE);
                } else {
                    stream = new BufferedOutputStream(stream, BufferSizes.OUTPUT_BUFFER_SIZE);
                }
                return new PrintStream(stream);
            } catch (IOException e) {
                throw new RuntimeException("Create file failure!", e);
            }
        } else {
            return new PrintStream(new MemoryBufferedOutputStream(state));
        }
    }

    /**
     * Cleans up the output stream
     */
    protected final void cleanupOutputStream(PrintStream out) {
        if (out.checkError()) {
            // Make sure to null out the output stream when we're done because
            // the nature of how we do multi-threading means we have lots of
            // references to our writers each of which may be holding a
            // reference to a buffer
            out = null;
            throw new RuntimeException("Error writing file");
        }

        out.flush();
        out.close();

        if (out.checkError()) {
            // Make sure to null out the output stream when we're done because
            // the nature of how we do multi-threading means we have lots of
            // references to our writers each of which may be holding a
            // reference to a buffer
            out = null;
            throw new RuntimeException("Error writing file");
        }
    }

    protected void submitWrites() {
        for (Entry<String, GlobalState> kvp : this.currentFiles.entrySet()) {
            WriteConsolidator consolidator = kvp.getValue().getWriteConsolidator();
            if (consolidator == null)
                continue;
            consolidator.addFile(kvp.getKey());
        }
        this.currentFiles.clear();
    }

}