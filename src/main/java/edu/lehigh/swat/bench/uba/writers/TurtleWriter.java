package edu.lehigh.swat.bench.uba.writers;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.model.Ontology;

public class TurtleWriter extends FlatWriter {

    public TurtleWriter(GeneratorCallbackTarget target, String ontologyUrl) {
        super(target, ontologyUrl);
    }

    @Override
    public void startFile(String fileName) {
        super.startFile(fileName);

        // Add prefix declarations
        prefix(WriterVocabulary.T_RDF_NS, WriterVocabulary.T_RDF_NS_URI);
        prefix(WriterVocabulary.T_RDFS_NS, WriterVocabulary.T_RDFS_NS_URI);
        prefix(WriterVocabulary.T_OWL_NS, WriterVocabulary.T_OWL_NS_URI);
        prefix(WriterVocabulary.T_ONTO_NS, this.ontologyUrl);
    }

    protected void prefix(String prefix, String uri) {
        out.format("@prefix %s: <%s> .", prefix, uri);
        out.println();
    }

    @Override
    protected void addTriple(String property, String object, boolean isResource) {
        out.print(subjectOrObjectUri(this.getCurrentSubject()));
        out.print(' ');
        out.print(predicate(property));
        out.print(' ');
        if (isResource) {
            out.print(subjectOrObjectUri(object));
        } else {
            out.format("\"%s\"", object);
        }
        out.println('.');
    }

    @Override
    protected void addTypeTriple(String subject, int classType) {
        String classUrl = String.format("%s#%s", this.ontologyUrl, Ontology.CLASS_TOKEN[classType]);
        addTriple(RDF_TYPE, classUrl, true);
    }

    private String shorten(String uri, boolean predicate) {
        if (uri.startsWith(this.ontologyUrl) && uri.length() > this.ontologyUrl.length()) {
            return String.format("%s:%s", WriterVocabulary.T_ONTO_NS, uri.substring(this.ontologyUrl.length() + 1));
        } else if (uri.startsWith(WriterVocabulary.T_RDF_NS_URI)) {
            if (predicate && uri.equals(RDF_TYPE)) {
                return "a";
            } else {
                return String.format("%s:%s", WriterVocabulary.T_RDF_NS,
                        uri.substring(WriterVocabulary.T_RDF_NS_URI.length() + 1));
            }
        } else if (uri.startsWith(WriterVocabulary.T_RDFS_NS_URI)) {
            return String.format("%s:%s", WriterVocabulary.T_RDFS_NS,
                    uri.substring(WriterVocabulary.T_RDFS_NS_URI.length() + 1));
        } else if (uri.startsWith(WriterVocabulary.T_OWL_NS_URI)) {
            return String.format("%s:%s", WriterVocabulary.T_OWL_NS,
                    uri.substring(WriterVocabulary.T_OWL_NS_URI.length() + 1));
        }

        return null;
    }

    private String subjectOrObjectUri(String uri) {
        String pname = shorten(uri, false);
        if (pname != null) {
            return pname;
        } else {
            return String.format("<%s>", uri);
        }
    }

    private String predicate(String uri) {
        String pname = shorten(uri, true);
        if (pname != null) {
            return pname;
        } else {
            return String.format("<%s>", uri);
        }
    }
}
