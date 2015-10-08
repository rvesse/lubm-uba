package edu.lehigh.swat.bench.uba.writers.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.lehigh.swat.bench.uba.GlobalState;
import edu.lehigh.swat.bench.uba.writers.ConsolidationMode;

/**
 * A background writer service
 * <p>
 * This is used to avoid the generator threads blocking unduly on a single point
 * of IO when using {@link ConsolidationMode#Full} consolidation.
 * </p>
 * 
 * @author rvesse
 *
 */
public class BackgroundWriterService implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundWriterService.class);

    private static final long KB_PER_MB = 1024;
    /**
     * The approximate sizes of each universities output data in the different
     * formats both compressed and uncompressed. We'll use these to help
     * estimate how large to make the capacity of our blocking queue.
     */
    //@formatter:off
    private static final long APPROX_SIZE_OWL = 12 * KB_PER_MB,
                              APPROX_SIZE_DAML = APPROX_SIZE_OWL,
                              APPROX_SIZE_NTRIPLES = 24 * KB_PER_MB,
                              APPROX_SIZE_TURTLE = 8 * KB_PER_MB,
                              APPROX_SIZE_OWL_COMPRESSED = 350,
                              APPROX_SIZE_DAML_COMPRESSED = APPROX_SIZE_OWL_COMPRESSED,
                              APPROX_SIZE_NTRIPLES_COMPRESSED = 750,
                              APPROX_SIZE_TURTLE_COMPRESSED = 350;
    //@formatter:on

    private boolean stop, terminate;
    private final ArrayBlockingQueue<byte[]> writeQueue;
    private final GlobalState state;

    public BackgroundWriterService(GlobalState state) {
        this.state = state;

        // We'll use up to half the available heap
        // Our calculations our in KB so remember to convert appropriately
        long approxSizePerWrite = getApproxSize(state);
        long availableMemory = Runtime.getRuntime().maxMemory() / 2;
        availableMemory /= KB_PER_MB;
        LOGGER.info("Will use at most {}MB of heap memory for write buffers",
                ((double) availableMemory / (double) KB_PER_MB));

        // Calculate capacity by dividing the fraction of available memory by
        // the approximate size per write
        int capacity = (int) (availableMemory / approxSizePerWrite);
        if (capacity % state.getThreads() != 0 && state.getThreads() > 1) {
            // We'll round down so we have an equal number of slots per thread
            // unless we're doing single threaded generation in which case we
            // can still improve performance by maximising use of write buffers

            // When we have multiple threads we'll be using fair scheduling so
            // having a capacity be a multiple of the number of threads means
            // each thread can have the same number of writes in the queue and
            // no thread will be unduly blocked submitted to the writer service
            capacity = capacity - (capacity % state.getThreads());
        }
        if (state.getThreads() > 1 && capacity > state.getThreads() * 16) {
            // Cap the capacity at 16 times the number of threads
            // When we calculate a high capacity it means we are using
            // compression however since all the compression happens on this
            // thread the writer thread is stuck doing masses of compression
            // work at the end after all the generators have filled the queue
            capacity = 16 * state.getThreads();
        }
        LOGGER.info("Background write buffer has total capacity of {}", capacity);

        this.writeQueue = new ArrayBlockingQueue<>(capacity, true);
    }

    private static long getApproxSize(GlobalState state) {
        if (state.compressFiles()) {
            switch (state.getWriterType()) {
            case OWL:
                return APPROX_SIZE_OWL_COMPRESSED;
            case DAML:
                return APPROX_SIZE_DAML_COMPRESSED;
            case NTRIPLES:
                return APPROX_SIZE_NTRIPLES_COMPRESSED;
            case TURTLE:
                return APPROX_SIZE_TURTLE_COMPRESSED;
            }
        } else {
            switch (state.getWriterType()) {
            case OWL:
                return APPROX_SIZE_OWL;
            case DAML:
                return APPROX_SIZE_DAML;
            case NTRIPLES:
                return APPROX_SIZE_NTRIPLES;
            case TURTLE:
                return APPROX_SIZE_TURTLE;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown Writer Type %s", state.getWriterType()));
    }

    /**
     * Tells the service that there is no further work to do and it should
     * terminate once outstanding writes are completed.
     */
    public void stop() {
        if (this.stop)
            return;

        this.stop = true;
        try {
            this.writeQueue.put(new byte[0]);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to stop background writer service", e);
        }
    }

    /**
     * Tells the service that it should terminate ASAP regardless of whether
     * there are outstanding writes
     */
    public void terminate() {
        this.terminate = true;
    }

    /**
     * Submits a write to the service
     * 
     * @param data
     *            Data to be written
     */
    public void submit(byte[] data) {
        // Illegal to submit writes after asked to stop
        if (this.stop)
            throw new IllegalStateException("Cannot submit a write after the service has been told to stop");

        // Ignore write submission if asked to terminate
        if (this.terminate)
            return;

        // Submit write which may block
        try {
            this.writeQueue.put(data);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to submit write to background writer service", e);
        }
    }

    @Override
    public void run() {
        // Set our priority higher than normal
        Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 1);

        try {
            while (!this.terminate) {
                try {
                    // Wait for the next available write
                    byte[] next = this.writeQueue.take();

                    // We'll try and do multiple writes in one go wherever
                    // possible
                    // The number of writes we'll do will depend on how many
                    // writes are in the queue right now and will be capped at
                    // the number of threads in use
                    // UNLESS we've been told we're stopping at which point we
                    // can exhaust the entire queue in one go
                    Queue<byte[]> writes = new LinkedList<>();
                    writes.add(next);
                    int numWrites = this.stop ? this.writeQueue.size()
                            : Math.min(this.writeQueue.size(), this.state.getThreads() - 1);

                    while (numWrites > 0) {
                        // It it safe to use poll() here because we know that
                        // there must be at least numWrites writes in the queue
                        // so no need to worry about writes
                        writes.add(this.writeQueue.poll());
                        numWrites--;
                    }

                    // Perform all the writes in one go
                    OutputStream output = this.state.getConsolidatedOutput();
                    try {
                        LOGGER.debug("Batching {} writes", writes.size());
                        while (!writes.isEmpty()) {
                            if (this.terminate)
                                break;

                            byte[] data = writes.poll();
                            if (data.length == 0) {
                                // A zero length write signifies no further
                                // writes
                                LOGGER.debug("No further writes");
                                this.terminate = true;
                                if (!writes.isEmpty() || this.writeQueue.size() > 0) {
                                    throw new IllegalStateException(
                                            "Unexpectedly received signal for end of writes but there are still writes in the write queue");
                                }
                                break;
                            }
                            output.write(data);
                        }

                        if (this.terminate)
                            continue;

                        // Only flush after we've done all the writes in this
                        // batch
                        output.flush();
                    } catch (IOException e) {
                        throw new RuntimeException("Error writing to consolidated output file", e);
                    }

                } catch (InterruptedException e) {
                    LOGGER.warn("Interrupted while waiting for writes");
                }
            }
        } finally {
            // Ensure we close the output stream when we terminate
            try {
                LOGGER.debug("Closing consolidated output file");
                OutputStream output = this.state.getConsolidatedOutput();
                output.flush();
                output.close();
                LOGGER.debug("Consolidated output file is closed");
            } catch (IOException e) {
                throw new RuntimeException("Error while closing consolidated output file", e);
            }
        }
    }

}
