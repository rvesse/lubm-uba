package edu.lehigh.swat.bench.uba.writers.utils;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import edu.lehigh.swat.bench.uba.GlobalState;

/**
 * An output stream that buffers the contents in memory until the file is closed
 * at which time it submits the write to the background writer service
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

        // Submit the write to the background writer service
        this.state.getBackgroundWriterService().submit(((ByteArrayOutputStream) this.out).toByteArray());
    }
}
