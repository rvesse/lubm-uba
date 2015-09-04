package edu.lehigh.swat.bench.uba;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import edu.lehigh.swat.bench.uba.model.Ontology;
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
    
    private final ExecutorService executorService;
    
    public GlobalState(int univNum, long baseSeed, int startIndex, String ontologyUrl, WriterType type, File outputDir, int threads) {
        this.numUniversities = univNum;
        this.baseSeed = baseSeed;
        this.startIndex = startIndex;
        this.ontology = ontologyUrl;
        this.writerType = type;
        this.outputDir = outputDir;

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
    
    public ExecutorService getExecutor() {
        return this.executorService;
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
}
