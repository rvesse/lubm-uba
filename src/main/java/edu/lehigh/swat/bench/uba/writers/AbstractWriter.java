package edu.lehigh.swat.bench.uba.writers;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.zip.GZIPOutputStream;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;

public class AbstractWriter {

    /** white space string */
    protected static final String T_SPACE = " ";
    /** output stream */
    protected PrintStream out = null;
    /** the generator */
    protected GeneratorCallbackTarget callbackTarget;

    public AbstractWriter(GeneratorCallbackTarget callbackTarget) {
        this.callbackTarget = callbackTarget;
    }

    protected void prepareOutputStream(String fileName) {
        try {
            // Prepare the output stream
            OutputStream stream = new FileOutputStream(fileName);
            if (fileName.endsWith(".gz")) {
                stream = new GZIPOutputStream(stream);
            }
            out = new PrintStream(stream);
        } catch (IOException e) {
            System.out.println("Create file failure!");
        }
    }

    protected void cleanupOutputStream() {
        if (out.checkError()) {
            throw new RuntimeException("Error writing file");
        }
    
        out.flush();
        out.close();
        
        if (out.checkError()) {
            throw new RuntimeException("Error writing file");
        }
    }

}