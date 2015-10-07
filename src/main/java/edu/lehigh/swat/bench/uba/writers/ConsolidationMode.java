package edu.lehigh.swat.bench.uba.writers;

/**
 * Possible consolidation modes
 * 
 * @author rvesse
 *
 */
public enum ConsolidationMode {
    /**
     * No consolidation is done, each department of each university generates a
     * file
     */
    None, 
    /**
     * Partial consolidation, all data for each university is placed into
     * a single file
     */
    Partial, 
    /**
     * Full consolidation, all data is placed into a single file
     */
    Full
}
