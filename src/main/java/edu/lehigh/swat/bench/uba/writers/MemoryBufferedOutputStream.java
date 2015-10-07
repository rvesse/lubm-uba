package edu.lehigh.swat.bench.uba.writers;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.lehigh.swat.bench.uba.GlobalState;

/**
 * An output stream that buffers the contents in memory until the file is closed
 * at which time it writes it out to the final destination file
 * 
 * @author rvesse
 *
 */
public class MemoryBufferedOutputStream extends FilterOutputStream {

    private final GlobalState state;

    public MemoryBufferedOutputStream(GlobalState state) {
        super(new ByteArrayOutputStream(16384));
        this.state = state;
    }

    @Override
    public void close() throws IOException {
        super.close();

        OutputStream consolidatedOutput = this.state.getConsolidatedOutput();
        synchronized (consolidatedOutput) {
            try {
                consolidatedOutput.write(((ByteArrayOutputStream) this.out).toByteArray());
                consolidatedOutput.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error writing to consolidated output file", e);
            }
        }
    }
}
