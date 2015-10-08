package edu.lehigh.swat.bench.uba.writers;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.zip.GZIPOutputStream;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.GlobalState;
import edu.lehigh.swat.bench.uba.writers.utils.BufferSizes;
import edu.lehigh.swat.bench.uba.writers.utils.MemoryBufferedOutputStream;

public class AbstractWriter {

    /** white space string */
    protected static final String T_SPACE = " ";
    /** output stream */
    protected PrintStream out = null;
    /** the generator */
    protected GeneratorCallbackTarget callbackTarget;

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
    protected void prepareOutputStream(String fileName, GlobalState state) {
        if (state.consolidationMode() != ConsolidationMode.Full) {
            try {
                // Prepare the output stream
                OutputStream stream = new FileOutputStream(fileName);
                if (fileName.endsWith(".gz")) {
                    stream = new GZIPOutputStream(stream, BufferSizes.GZIP_BUFFER_SIZE);
                } else {
                    stream = new BufferedOutputStream(stream, BufferSizes.OUTPUT_BUFFER_SIZE);
                }
                out = new PrintStream(stream);
            } catch (IOException e) {
                throw new RuntimeException("Create file failure!", e);
            }
        } else {
            out = new PrintStream(new MemoryBufferedOutputStream(state));
        }
    }

    /**
     * Cleans up the output stream
     */
    protected void cleanupOutputStream() {
        if (out.checkError()) {
            throw new RuntimeException("Error writing file");
        }

        out.flush();
        out.close();

        if (out.checkError()) {
            throw new RuntimeException("Error writing file");
        }
    }

}