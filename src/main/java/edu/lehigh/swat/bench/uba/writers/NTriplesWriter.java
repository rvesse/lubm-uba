package edu.lehigh.swat.bench.uba.writers;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.model.Ontology;

public class NTriplesWriter extends FlatWriter {
    
    public NTriplesWriter(GeneratorCallbackTarget target, String ontologyUrl) {
        super(target, ontologyUrl);
    }

    @Override
    protected void addTriple(String property, String object, boolean isResource) {
        if (isResource) {
            out.format("<%s> <%s> <%s> .", this.getCurrentSubject(), property, object);
        } else {
            out.format("<%s> <%s> \"%s\" .", this.getCurrentSubject(), property, object);
        }
        out.println();
    }

    @Override
    protected void addTypeTriple(String subject, int classType) {
        String classUrl = String.format("%s#%s", this.ontologyUrl, Ontology.CLASS_TOKEN[classType]);
        out.format("<%s> <%s> <%s> .", subject, RDF_TYPE, classUrl);
        out.println();
    }

}
