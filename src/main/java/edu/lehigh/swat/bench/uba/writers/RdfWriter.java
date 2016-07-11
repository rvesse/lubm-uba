/**
 * by Yuanbo Guo
 * Semantic Web and Agent Technology Lab, CSE Department, Lehigh University, USA
 * Copyright (C) 2004
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */

package edu.lehigh.swat.bench.uba.writers;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.GlobalState;
import edu.lehigh.swat.bench.uba.model.Ontology;

public abstract class RdfWriter extends AbstractWriter implements Writer {
    /**
     * Creates a new RDF writer
     * 
     * @param callbackTarget
     *            The callback target
     */
    public RdfWriter(GeneratorCallbackTarget callbackTarget) {
        super(callbackTarget);
    }

    /**
     * Implementation of Writer:startFile.
     */
    public void startFile(String fileName, GlobalState state) {
        String s;
        this.out = prepareOutputStream(fileName, state);

        // XML header
        s = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";
        out.println(s);

        // Root rdf:RDF element
        s = "<" + WriterVocabulary.T_RDF_PREFIX + "RDF";
        out.println(s);
        writeHeader();

    }

    /**
     * Implementation of Writer:endFile.
     */
    public void endFile(GlobalState state) {
        String s;
        s = "</" + WriterVocabulary.T_RDF_PREFIX + "RDF>";
        out.println(s);

        try {
            cleanupOutputStream(this.out);
        } finally {
            this.out = null;
        }
        
        this.submitWrites();
    }

    /**
     * Implementation of Writer:startSection.
     */
    public void startSection(int classType, String id) {
        callbackTarget.startSectionCB(classType);
        out.println();
        String s = "<" + WriterVocabulary.T_ONTO_PREFIX + Ontology.CLASS_TOKEN[classType] + T_SPACE
                + WriterVocabulary.T_RDF_ABOUT + "=\"" + id + "\">";
        out.println(s);
    }

    /**
     * Implementation of Writer:startAboutSection.
     */
    public void startAboutSection(int classType, String id) {
        callbackTarget.startAboutSectionCB(classType);
        out.println();
        String s = "<" + WriterVocabulary.T_ONTO_PREFIX + Ontology.CLASS_TOKEN[classType] + T_SPACE
                + WriterVocabulary.T_RDF_ABOUT + "=\"" + id + "\">";
        out.println(s);
    }

    /**
     * Implementation of Writer:endSection.
     */
    public void endSection(int classType) {
        String s = "</" + WriterVocabulary.T_ONTO_PREFIX + Ontology.CLASS_TOKEN[classType] + ">";
        out.println(s);
    }

    /**
     * Implementation of Writer:addProperty.
     */
    public void addProperty(int property, String value, boolean isResource) {
        callbackTarget.addPropertyCB(property);

        String s;
        if (isResource) {
            s = "   <" + WriterVocabulary.T_ONTO_PREFIX + Ontology.PROP_TOKEN[property] + T_SPACE
                    + WriterVocabulary.T_RDF_RES + "=\"" + value + "\" />";
        } else { // literal
            s = "   <" + WriterVocabulary.T_ONTO_PREFIX + Ontology.PROP_TOKEN[property] + ">" + value + "</"
                    + WriterVocabulary.T_ONTO_PREFIX + Ontology.PROP_TOKEN[property] + ">";
        }

        out.println(s);
    }

    /**
     * Implementation of Writer:addProperty.
     */
    public void addProperty(int property, int valueClass, String valueId) {
        callbackTarget.addPropertyCB(property);
        callbackTarget.addValueClassCB(valueClass);

        String s;
        s = "   <" + WriterVocabulary.T_ONTO_PREFIX + Ontology.PROP_TOKEN[property] + ">\n" + "      <"
                + WriterVocabulary.T_ONTO_PREFIX + Ontology.CLASS_TOKEN[valueClass] + T_SPACE
                + WriterVocabulary.T_RDF_ABOUT + "=\"" + valueId + "\" />" + "   </" + WriterVocabulary.T_ONTO_PREFIX
                + Ontology.PROP_TOKEN[property] + ">";

        out.println(s);
    }

    /**
     * Writes the header part.
     */
    abstract void writeHeader();
}