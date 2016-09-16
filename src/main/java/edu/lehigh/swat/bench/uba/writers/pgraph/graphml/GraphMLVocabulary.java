package edu.lehigh.swat.bench.uba.writers.pgraph.graphml;

public class GraphMLVocabulary {
    public static final String HEADER = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n"
            + "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";
    public static final String FOOTER = "</graphml>";
    public static final String GRAPH_START = "  <graph id=\"LUBM\" edgedefault=\"directed\">";
    public static final String GRAPH_END = "  </graph>";
    public static final boolean[] IS_NODE_ATTRIBUTE = { true, false, false, false, false, false, false, false, false,
            false, false, true, true, true, false, false };
}
