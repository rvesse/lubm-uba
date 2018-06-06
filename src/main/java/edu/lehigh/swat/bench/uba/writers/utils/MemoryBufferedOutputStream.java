package edu.lehigh.swat.bench.uba.writers.utils;

import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

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
    private final Object lock = new Object();

    public MemoryBufferedOutputStream(GlobalState state) {
        super(new ByteArrayOutputStream(BufferSizes.MEMORY_BUFFER_SIZE));
        this.state = state;
    }

    @Override
    public void flush() throws IOException {
        super.flush();

        synchronized (this.lock) {
            // Nothing to do if the buffer is empty
            if (this.out == null)
                return;

            // Flush the current state of the output stream
            OutputStream output = this.state.getWriterPool().getOutputStream();
            output.write(((ByteArrayOutputStream) this.out).toByteArray());
            output.flush();

            // Reset the buffer after a flush
            this.out = null;
            this.out = new ByteArrayOutputStream(BufferSizes.MEMORY_BUFFER_SIZE);
        }
    }

    @Override
    public void close() throws IOException {
        super.close();

        synchronized (this.lock) {
            // Submit the write to the background writer service
            OutputStream output = this.state.getWriterPool().getOutputStream();
            output.write(((ByteArrayOutputStream) this.out).toByteArray());
            output.flush();
            this.out = null;
        }
    }
}
