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

import java.io.OutputStream;
import java.io.PrintStream;

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

    @Override
    public void startFile(String fileName, GlobalState state) {
        this.out = prepareOutputStream(fileName, state);

        // WriterPool takes care of this for full consolidation
        // by calling the other overload of startFile()
        if (state.consolidationMode() == ConsolidationMode.Full)
            return;

        outputXmlHeader(this.out);
    }
    
    @Override
    public void startFile(GlobalState state, OutputStream output) {
        PrintStream print = new PrintStream(output);
        outputXmlHeader(print);
        print.flush();
    }

    protected void outputXmlHeader(PrintStream out) {
        // XML header
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");

        // Root rdf:RDF element
        out.format("<%sRDF", WriterVocabulary.T_RDF_PREFIX);
        out.println();
        writeHeader(out);
    }

    @Override
    public void flushFile(GlobalState state) {
        if (this.out != null)
            this.out.flush();
    }

    @Override
    public void endFile(GlobalState state) {
        endFile(state, this.out);

        try {
            cleanupOutputStream(this.out);
        } finally {
            this.out = null;
        }

        this.submitWrites();
    }

    @Override
    public void endFile(GlobalState state, OutputStream output) {
        PrintStream print = new PrintStream(output);
        print.format("</%sRDF>", WriterVocabulary.T_RDF_PREFIX);
        print.println();
        print.flush();
    }

    @Override
    public void startSection(int classType, String id) {
        callbackTarget.startSectionCB(classType);
        out.println();
        String s = "<" + WriterVocabulary.T_ONTO_PREFIX + Ontology.CLASS_TOKEN[classType] + T_SPACE
                + WriterVocabulary.T_RDF_ABOUT + "=\"" + id + "\">";
        out.println(s);
    }

    @Override
    public void startAboutSection(int classType, String id) {
        callbackTarget.startAboutSectionCB(classType);
        out.println();
        String s = "<" + WriterVocabulary.T_ONTO_PREFIX + Ontology.CLASS_TOKEN[classType] + T_SPACE
                + WriterVocabulary.T_RDF_ABOUT + "=\"" + id + "\">";
        out.println(s);
    }

    @Override
    public void endSection(int classType) {
        String s = "</" + WriterVocabulary.T_ONTO_PREFIX + Ontology.CLASS_TOKEN[classType] + ">";
        out.println(s);
    }

    @Override
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

    @Override
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
    protected abstract void writeHeader(PrintStream output);
}