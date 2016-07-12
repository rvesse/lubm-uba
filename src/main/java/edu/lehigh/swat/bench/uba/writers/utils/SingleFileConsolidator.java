package edu.lehigh.swat.bench.uba.writers.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.Queue;
import java.util.zip.GZIPOutputStream;

public class SingleFileConsolidator extends AbstractWriteConsolidator {

    private Queue<String> files = new LinkedList<>();
    private final String finalFilename;
    private OutputStream output;

    public SingleFileConsolidator(String targetFilename) {
        this.finalFilename = targetFilename;
    }

    @Override
    public void addFile(String file) {
        synchronized (this.files) {
            this.files.add(file);
            this.queued.incrementAndGet();
        }
    }

    @Override
    protected String nextFile() {
        synchronized (this.files) {
            return this.files.poll();
        }
    }

    @Override
    protected OutputStream getOutput(String filename) throws IOException {
        if (this.output == null) {
            if (filename.endsWith(".gz")) {
                this.output = new GZIPOutputStream(new FileOutputStream(this.finalFilename),
                        BufferSizes.GZIP_BUFFER_SIZE);
            } else {
                this.output = new BufferedOutputStream(new FileOutputStream(this.finalFilename),
                        BufferSizes.OUTPUT_BUFFER_SIZE);
            }
            this.writeHeader(output);
        }
        return this.output;
    }

    protected void writeHeader(OutputStream output) throws IOException {

    }

    protected void writeFooter(OutputStream output) throws IOException {

    }

    @Override
    protected void cleanupOutputs() throws IOException {
        try {
            if (this.output != null) {
                this.writeFooter(output);
                this.output.close();
            }
        } finally {
            if (this.cancelled) {
                new File(this.finalFilename).delete();
            }
        }
    }

}
