package edu.lehigh.swat.bench.uba.writers.pgraph.graphml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.lehigh.swat.bench.uba.model.Ontology;
import edu.lehigh.swat.bench.uba.writers.utils.SingleFileConsolidator;

public class GraphMLConsolidator extends SingleFileConsolidator {

    public GraphMLConsolidator(String targetFilename) {
        super(targetFilename);
    }

    @Override
    protected void writeHeader(OutputStream output) throws IOException {
        PrintStream print = new PrintStream(output);
        print.println(GraphMLVocabulary.HEADER);

        print.println("<key id=\"type\" for=\"node\" attr.name=\"type\" attr.type=\"string\" />");
        print.println("<key id=\"uri\" for=\"node\" attr.name=\"uri\" attr.type=\"string\" />");
        print.println(
                "<key id=\"researchAssistant\" for=\"node\" attr.name=\"researchAssistant\" attr.type=\"boolean\" />");
        for (int index = 0; index < Ontology.PROP_TOKEN.length; index++) {
            print.print("<key id=\"");
            print.print(Ontology.PROP_TOKEN[index]);
            print.print("\" for=\"");
            if (GraphMLVocabulary.IS_NODE_ATTRIBUTE[index]) {
                print.print("node");
            } else {
                print.print("edge");
            }
            print.print("\" attr.name=\"");
            print.print(Ontology.PROP_TOKEN[index]);
            print.println("\" attr.type=\"string\" />");
        }

        print.println(GraphMLVocabulary.GRAPH_START);
        print.flush();

        if (print.checkError())
            throw new IOException("Error writing GraphML Header");
    }

    @Override
    protected void writeFooter(OutputStream output) throws IOException {
        PrintStream print = new PrintStream(output);
        print.println(GraphMLVocabulary.GRAPH_END);
        print.println(GraphMLVocabulary.FOOTER);
        print.flush();

        if (print.checkError())
            throw new IOException("Error writing GraphML Footer");
    }

}
