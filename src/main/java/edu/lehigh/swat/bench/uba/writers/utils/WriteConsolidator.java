package edu.lehigh.swat.bench.uba.writers.utils;

/**
 * Interface for write consolidators
 * @author rvesse
 *
 */
public interface WriteConsolidator extends Runnable {

    /**
     * Should be called to indicate that the given file is ready for
     * consolidation
     * 
     * @param file
     *            File
     */
    public void addFile(String file);

    /**
     * Should be called to indicate that consolidation should be cancelled and
     * aborted
     */
    public void cancel();

    /**
     * Should be called to indicate that all files to be consolidate have been
     * registered and consolidation can now be completed
     */
    public void finish();
    
    /**
     * Indicates whether the consolidation has been started
     * @return
     */
    public boolean wasStarted();
}
