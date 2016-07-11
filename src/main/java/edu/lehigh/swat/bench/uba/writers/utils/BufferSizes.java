package edu.lehigh.swat.bench.uba.writers.utils;

import edu.lehigh.swat.bench.uba.writers.ConsolidationMode;

public class BufferSizes {

    /**
     * Initial buffer size per {@link UniversityGenerator} used when doing
     * {@link ConsolidationMode#Full} consolidation.
     * <p>
     * Since we know we're going to write a few hundred kilobytes if not tens of
     * megabytes it makes sense to start with a larger buffer
     * </p>
     */
    public static int MEMORY_BUFFER_SIZE = 64 * 1024;
    
    /**
     * Buffer size used for compressed output
     */
    public static int GZIP_BUFFER_SIZE = 32 * 1024;
    
    /**
     * Buffer size used for uncompressed output
     */
    public static int OUTPUT_BUFFER_SIZE = 64 * 1024;

}
