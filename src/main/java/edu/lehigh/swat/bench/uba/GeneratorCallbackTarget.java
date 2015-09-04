package edu.lehigh.swat.bench.uba;

public interface GeneratorCallbackTarget {

    /**
     * Callback invoked by a writer when it starts an instance section.
     * 
     * @param classType
     *            Type of the instance.
     */
    public abstract void startSectionCB(int classType);

    /**
     * Callback invoked by a writer when it starts an instance section
     * identified by an rdf:about attribute.
     * 
     * @param classType
     *            Type of the instance.
     */
    public abstract void startAboutSectionCB(int classType);

    /**
     * Callback invoked by a writer when it adds a property statement.
     * 
     * @param property
     *            Type of the property.
     */
    public abstract void addPropertyCB(int property);

    /**
     * Callback by the writer when it adds a property statement whose value is
     * an individual.
     * 
     * @param classType
     *            Type of the individual.
     */
    public abstract void addValueClassCB(int classType);
}
