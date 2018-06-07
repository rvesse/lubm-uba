package edu.lehigh.swat.bench.uba.writers;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.GlobalState;
import edu.lehigh.swat.bench.uba.model.Ontology;

public class TurtleWriter extends FlatWriter {

    private String lastSubject;
    private Map<String, String> prefixes = new HashMap<>();

    public TurtleWriter(GeneratorCallbackTarget target, String ontologyUrl) {
        super(target, ontologyUrl);
    }

    @Override
    public void startFile(String fileName, GlobalState state) {
        this.out = prepareOutputStream(fileName, state);

        // Add prefix declarations
        prefix(WriterVocabulary.T_RDF_NS, WriterVocabulary.T_RDF_NS_URI);
        prefix(WriterVocabulary.T_RDFS_NS, WriterVocabulary.T_RDFS_NS_URI);
        prefix(WriterVocabulary.T_OWL_NS, WriterVocabulary.T_OWL_NS_URI);
        if (this.ontologyUrl.endsWith("#") || this.ontologyUrl.endsWith("/")) {
            prefix(WriterVocabulary.T_ONTO_NS, this.ontologyUrl);
        } else {
            prefix(WriterVocabulary.T_ONTO_NS, this.ontologyUrl + "#");
        }

        addOntologyDeclaration();
    }
    
    @Override
    public void endFile(GlobalState state, OutputStream output) {
        if (lastSubject != null) {
            PrintStream print = new PrintStream(output);
            print.println('.');
            lastSubject = null;
            print.flush();
        }
        
        super.endFile(state, output);
    }

    @Override
    public void endFile(GlobalState state) {
        if (lastSubject != null) {
            endPredicateObjectList();
        }

        super.endFile(state);
    }

    protected void prefix(String prefix, String uri) {
        out.format("@prefix %s: <%s> .", prefix, uri);
        prefixes.put(uri, prefix);
        out.println();
    }

    @Override
    protected void addTriple(String property, String object, boolean isResource) {
        String currSubject = this.getCurrentSubject();

        if (lastSubject != null) {
            if (!lastSubject.equals(currSubject)) {
                // Start new predicate object list
                endPredicateObjectList();
                startPredicateObjectList(currSubject);
            }
        } else {
            startPredicateObjectList(currSubject);
        }
        out.print(' ');
        out.print(predicate(property));
        out.print(' ');
        if (isResource) {
            out.print(subjectOrObjectUri(object));
        } else {
            out.format("\"%s\"", object);
        }
        out.println(" ;");
    }

    protected void endPredicateObjectList() {
        out.println('.');
        lastSubject = null;
    }

    protected void startPredicateObjectList(String currSubject) {
        out.print(subjectOrObjectUri(currSubject));
        lastSubject = currSubject;
    }

    @Override
    protected void addTypeTriple(String subject, int classType) {
        String classUrl = String.format("%s#%s", this.ontologyUrl, Ontology.CLASS_TOKEN[classType]);
        if (lastSubject != null) {
            if (!lastSubject.equals(subject)) {
                endPredicateObjectList();
                startPredicateObjectList(subject);
            }
        } else {
            startPredicateObjectList(subject);
        }
        out.print(" a ");
        out.print(subjectOrObjectUri(classUrl));
        out.println(" ;");
    }

    private String shorten(String uri, boolean predicate) {
        if (predicate && uri.equals(RDF_TYPE)) return "a";
        
        for (String nsUri : prefixes.keySet()) {
            if (uri.startsWith(nsUri)) {
                return String.format("%s:%s", prefixes.get(nsUri), uri.substring(nsUri.length()));
            }
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
