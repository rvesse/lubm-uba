package edu.lehigh.swat.bench.uba;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.GZIPOutputStream;

import edu.lehigh.swat.bench.uba.model.Ontology;
import edu.lehigh.swat.bench.uba.writers.ConsolidationMode;
import edu.lehigh.swat.bench.uba.writers.WriterType;

public class GlobalState {

    private final int numUniversities;

    /** user specified seed for the data generation */
    private final long baseSeed;

    /** starting index of the universities */
    private final int startIndex;

    /** univ-bench ontology url */
    private final String ontology;

    private final AtomicLong[] totalInstancesGenerated;
    private final AtomicLong[] totalPropertiesGenerated;

    private final WriterType writerType;
    private final File outputDir;
    private final boolean compress, quiet;
    private final ConsolidationMode consolidate;
    private OutputStream consolidatedOutput;

    private final ExecutorService executorService;
    private final long executionTimeout;
    private final TimeUnit executionTimeoutUnit;

    public GlobalState(int univNum, long baseSeed, int startIndex, String ontologyUrl, WriterType type, File outputDir,
            ConsolidationMode consolidate, boolean compress, int threads, long executionTimeout,
            TimeUnit executionTimeoutUnit, boolean quiet) {
        this.numUniversities = univNum;
        this.baseSeed = baseSeed;
        this.startIndex = startIndex;
        this.ontology = ontologyUrl;
        this.writerType = type;
        this.outputDir = outputDir;
        this.consolidate = consolidate;
        this.compress = compress;
        this.quiet = quiet;
        this.executionTimeout = executionTimeout;
        this.executionTimeoutUnit = executionTimeoutUnit;

        if (this.consolidate == ConsolidationMode.Full) {
            StringBuilder fileName = new StringBuilder();
            fileName.append(this.outputDir.getAbsolutePath());
            if (fileName.charAt(fileName.length() - 1) != File.separatorChar)
                fileName.append(File.separatorChar);
            fileName.append("Universities");
            fileName.append(this.getFileExtension());
            if (this.compress) {
                fileName.append(".gz");
            }
            try {
                this.consolidatedOutput = new FileOutputStream(fileName.toString());
                if (this.compress) {
                    this.consolidatedOutput = new GZIPOutputStream(this.consolidatedOutput);
                }
            } catch (IOException e) {
                throw new RuntimeException(String.format("Failed to prepare consolidated output file %s", fileName), e);
            }
        } else {
            this.consolidatedOutput = null;
        }

        this.totalInstancesGenerated = new AtomicLong[Ontology.CLASS_NUM];
        for (int i = 0; i < Ontology.CLASS_NUM; i++) {
            this.totalInstancesGenerated[i] = new AtomicLong(0l);
        }
        this.totalPropertiesGenerated = new AtomicLong[Ontology.PROP_NUM];
        for (int i = 0; i < Ontology.PROP_NUM; i++) {
            this.totalPropertiesGenerated[i] = new AtomicLong(0l);
        }

        if (threads <= 1) {
            this.executorService = Executors.newSingleThreadExecutor();
        } else {
            this.executorService = Executors.newFixedThreadPool(threads);
        }
    }

    public long getBaseSeed() {
        return this.baseSeed;
    }

    public String getOntologyUrl() {
        return this.ontology;
    }

    public int getNumberUniversities() {
        return this.numUniversities;
    }

    public int getStartIndex() {
        return this.startIndex;
    }

    public WriterType getWriterType() {
        return this.writerType;
    }

    public File getOutputDirectory() {
        return this.outputDir;
    }

    public boolean compressFiles() {
        return this.compress;
    }

    public ConsolidationMode consolidationMode() {
        return this.consolidate;
    }

    public boolean isQuietMode() {
        return this.quiet;
    }

    public ExecutorService getExecutor() {
        return this.executorService;
    }

    public long getExecutionTimeout() {
        return this.executionTimeout;
    }

    public TimeUnit getExecutionTimeoutUnit() {
        return this.executionTimeoutUnit;
    }

    public void incrementTotalInstances(int classType) {
        this.totalInstancesGenerated[classType].incrementAndGet();
    }

    public void incrementTotalProperties(int propType) {
        this.totalPropertiesGenerated[propType].incrementAndGet();
    }

    public long getTotalInstances(int classType) {
        return this.totalInstancesGenerated[classType].get();
    }

    public long getTotalProperties(int propType) {
        return this.totalPropertiesGenerated[propType].get();
    }

    /**
     * Gets the file extension for the configured writer type
     * 
     * @return File extension
     */
    public String getFileExtension() {
        // Extension
        switch (this.writerType) {
        case OWL:
            return ".owl";
        case DAML:
            return ".daml";
        case NTRIPLES:
            return ".nt";
        case TURTLE:
            return ".ttl";
        default:
            throw new RuntimeException("Unknown writer type");
        }
    }
    
    public OutputStream getConsolidatedOutput() {
        return this.consolidatedOutput;
    }

    public void finish() {
        if (this.consolidationMode() == ConsolidationMode.Full) {
            try {
                synchronized (this.consolidatedOutput) {
                    this.consolidatedOutput.flush();
                    this.consolidatedOutput.close();
                }
            } catch (IOException e) {
                throw new RuntimeException("Error finishing consolidated output file", e);
            }
        }
    }
}
