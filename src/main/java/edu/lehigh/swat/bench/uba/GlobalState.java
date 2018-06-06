package edu.lehigh.swat.bench.uba;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import edu.lehigh.swat.bench.uba.model.Ontology;
import edu.lehigh.swat.bench.uba.writers.ConsolidationMode;
import edu.lehigh.swat.bench.uba.writers.DamlWriter;
import edu.lehigh.swat.bench.uba.writers.NTriplesWriter;
import edu.lehigh.swat.bench.uba.writers.OwlWriter;
import edu.lehigh.swat.bench.uba.writers.TurtleWriter;
import edu.lehigh.swat.bench.uba.writers.Writer;
import edu.lehigh.swat.bench.uba.writers.WriterType;
import edu.lehigh.swat.bench.uba.writers.pgraph.graphml.GraphMLConsolidator;
import edu.lehigh.swat.bench.uba.writers.pgraph.graphml.GraphMLNodesThenEdgesConsolidator;
import edu.lehigh.swat.bench.uba.writers.pgraph.graphml.GraphMLWriter;
import edu.lehigh.swat.bench.uba.writers.pgraph.graphml.SegregatedGraphMLWriter;
import edu.lehigh.swat.bench.uba.writers.pgraph.json.JsonConsolidator;
import edu.lehigh.swat.bench.uba.writers.pgraph.json.JsonWriter;
import edu.lehigh.swat.bench.uba.writers.utils.SingleFileConsolidator;
import edu.lehigh.swat.bench.uba.writers.utils.WriteConsolidator;
import edu.lehigh.swat.bench.uba.writers.utils.WriterPool;

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

    private final int threads;
    private final ExecutorService executorService;
    private final long executionTimeout;
    private final TimeUnit executionTimeoutUnit;
    private final AtomicLong errorCount = new AtomicLong(0);

    private final WriterPool writerPool;
    private final WriteConsolidator writeConsolidator;
    private final ExecutorService consolidatorService = Executors.newSingleThreadExecutor();
    private Future<Long> consolidatorFuture;

    public GlobalState(int univNum, long baseSeed, int startIndex, String ontologyUrl, WriterType type, File outputDir,
            ConsolidationMode consolidate, boolean compress, int threads, long executionTimeout,
            TimeUnit executionTimeoutUnit, boolean quiet) {
        this.numUniversities = univNum;
        this.baseSeed = baseSeed;
        this.startIndex = startIndex;
        this.ontology = ontologyUrl;
        this.writerType = type;
        this.outputDir = outputDir;
        this.compress = compress;
        this.quiet = quiet;
        this.executionTimeout = executionTimeout;
        this.executionTimeoutUnit = executionTimeoutUnit;

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
            this.threads = 1;
        } else {
            this.threads = threads;
            this.executorService = Executors.newFixedThreadPool(threads);
        }

        // Adjust consolidation mode if Maximal/Full is specified
        if (consolidate == ConsolidationMode.Maximal) {
            switch (this.writerType) {
            case GRAPHML:
            case GRAPHML_NODESFIRST:
            case JSON:
            case NEO4J_GRAPHML:
                // All these formats will maximally consolidate regardless,
                // using Partial should give the best IO balance
                this.consolidate = ConsolidationMode.Partial;
                break;
            case NTRIPLES:
            case TURTLE:
                // All these formats can be trivially concatenated together so
                // again using Partial should give the best IO balance and we'll
                // need to add in a write consolidator
                this.consolidate = ConsolidationMode.Partial;
                break;
            default:
                // Otherwise default to full
                this.consolidate = ConsolidationMode.Full;
                break;
            }
        } else if (consolidate == ConsolidationMode.Full) {
            switch (this.writerType) {
            case GRAPHML:
            case GRAPHML_NODESFIRST:
            case JSON:
            case NEO4J_GRAPHML:
                // All these formats need us to use Partial consolidation as the
                // primary consolidation
                this.consolidate = ConsolidationMode.Partial;
                break;
            default:
                this.consolidate = consolidate;
                break;
            }
        } else {
            this.consolidate = consolidate;
        }

        if (this.consolidationMode() == ConsolidationMode.Full) {
            // Set up background writer service appropriately
            this.writerPool = new WriterPool(this);
        } else {
            this.writerPool = null;
        }

        // Prepare write consolidation if needed
        StringBuilder consolidatedFileName = new StringBuilder();
        consolidatedFileName.append(this.outputDir.getAbsolutePath());
        if (consolidatedFileName.charAt(consolidatedFileName.length() - 1) != File.separatorChar)
            consolidatedFileName.append(File.separatorChar);
        consolidatedFileName.append("Universities");
        String ext = this.getFileExtension();
        consolidatedFileName.append(ext);
        if (this.compress) {
            consolidatedFileName.append(".gz");
        }

        switch (this.writerType) {
        case GRAPHML:
            this.writeConsolidator = new GraphMLConsolidator(consolidatedFileName.toString());
            break;
        case GRAPHML_NODESFIRST:
        case NEO4J_GRAPHML:
            this.writeConsolidator = new GraphMLNodesThenEdgesConsolidator(consolidatedFileName.toString());
            break;
        case JSON:
            String file = consolidatedFileName.toString();
            this.writeConsolidator = new JsonConsolidator(file.replace(ext, "-nodes" + ext),
                    file.replace(ext, "-edges" + ext));
            break;
        case NTRIPLES:
        case TURTLE:
            if (consolidate == ConsolidationMode.Maximal) {
                this.writeConsolidator = new SingleFileConsolidator(consolidatedFileName.toString());
                break;
            }
        default:
            this.writeConsolidator = null;
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

    public int getThreads() {
        return this.threads;
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

    public void incrementErrorCount() {
        this.errorCount.incrementAndGet();
    }

    public boolean shouldContinue() {
        return this.errorCount.get() == 0;
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
        case GRAPHML:
        case GRAPHML_NODESFIRST:
        case NEO4J_GRAPHML:
            return ".graphml";
        case JSON:
            return ".json";
        default:
            throw new RuntimeException("Unknown writer type");
        }
    }

    /**
     * Creates a new writer
     * 
     * @param callbackTarget
     *            Callback target
     * @return Writer
     */
    public Writer createWriter(GeneratorCallbackTarget callbackTarget) {
        switch (this.getWriterType()) {
        case OWL:
            return new OwlWriter(callbackTarget, this.getOntologyUrl());

        case DAML:
            return new DamlWriter(callbackTarget, this.getOntologyUrl());

        case NTRIPLES:
            return new NTriplesWriter(callbackTarget, this.getOntologyUrl());

        case TURTLE:
            return new TurtleWriter(callbackTarget, this.getOntologyUrl());

        case GRAPHML:
            return new GraphMLWriter(callbackTarget, false);

        case GRAPHML_NODESFIRST:
            return new SegregatedGraphMLWriter(callbackTarget, false);

        case NEO4J_GRAPHML:
            return new SegregatedGraphMLWriter(callbackTarget, true);

        case JSON:
            return new JsonWriter(callbackTarget);

        default:
            throw new RuntimeException("Invalid writer type specified");
        }
    }

    public WriterPool getWriterPool() {
        return this.writerPool;
    }

    public WriteConsolidator getWriteConsolidator() {
        return this.writeConsolidator;
    }

    public void start() {
        if (this.writeConsolidator != null) {
            this.consolidatorFuture = this.consolidatorService.submit(this.writeConsolidator);
            while (!this.writeConsolidator.wasStarted()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    // Ignore and continue waiting
                }
            }
        }
    }

    public void finish() {
        if (this.consolidationMode() == ConsolidationMode.Full) {
            // Close the writer pool
            this.writerPool.close();
        }

        // Wait for write consolidation
        if (this.writeConsolidator != null) {
            this.writeConsolidator.finish();

            this.consolidatorService.shutdown();
            try {
                this.consolidatorFuture.get(this.executionTimeout, this.executionTimeoutUnit);
            } catch (InterruptedException e) {
                throw new RuntimeException("Write consolidation failed to terminate", e);
            } catch (ExecutionException e) {
                throw new RuntimeException("Write consolidation failed", e);
            } catch (TimeoutException e) {
                throw new RuntimeException("Write consolidation exceeded timeout");
            }
        }
    }
}
