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

/**
 * This class is an extension for GraphML serialization format support.
 * Author: Seokyong Hong, STAC Lab, Computer Science, 
 * 			North Carolina State University, USA.
 * Date: 2015-01-22
 */
package edu.lehigh.swat.bench.uba.writers.graphml;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;

import edu.lehigh.swat.bench.uba.GeneratorCallbackTarget;
import edu.lehigh.swat.bench.uba.GlobalState;
import edu.lehigh.swat.bench.uba.model.Ontology;
import edu.lehigh.swat.bench.uba.writers.AbstractWriter;
import edu.lehigh.swat.bench.uba.writers.Writer;

public abstract class PropertyGraphWriter extends AbstractWriter implements Writer {

    private static Set<String> requiredUniversities = new HashSet<>();

    protected GlobalState state;
    private Stack<Node> subjects = new Stack<>();
    private boolean isAboutSection;
    private Map<String, Node> graduateStudents = new HashMap<>();
    private boolean isGraduateStudent;

    public PropertyGraphWriter(GeneratorCallbackTarget callbackTarget) {
        super(callbackTarget);
    }

    @Override
    public void startFile(String fileName, GlobalState state) {
        this.out = prepareOutputStream(fileName, state);
        this.state = state;
    }
    
    @Override
    public void startFile(GlobalState state, OutputStream output) {
        throw new UnsupportedOperationException(
                "Full consolidation is not directly supported by property graph writers");
    }

    @Override
    public void endFile(GlobalState state) {
        storeGraduateStudents();

        try {
            cleanupOutputStream(this.out);
            this.submitWrites();
        } finally {
            this.out = null;
        }
    }

    @Override
    public void endFile(GlobalState state, OutputStream output) {
        throw new UnsupportedOperationException(
                "Full consolidation is not directly supported by property graph writers");
    }

    protected abstract void writeNode(Node n);

    protected abstract void writeEdge(Edge e);

    @Override
    public void startSection(int classType, String id) {
        callbackTarget.startSectionCB(classType);

        Node n = new Node(id, Ontology.CLASS_TOKEN[classType]);
        n.getProperties().put("uri", id);
        n.getProperties().put("type", Ontology.CLASS_TOKEN[classType]);
        subjects.push(n);

        if (classType == Ontology.CS_C_GRADSTUD) {
            graduateStudents.put(id, n);
            isGraduateStudent = true;
        }
    }

    @Override
    public void startAboutSection(int classType, String id) {
        callbackTarget.startAboutSectionCB(classType);

        Node n = new Node(id);
        n.getProperties().put("type", Ontology.CLASS_TOKEN[classType]);
        isAboutSection = true;
        if (classType == Ontology.CS_C_RA) {
            Node temp = graduateStudents.get(id);
            if (temp != null) {
                n = temp;
            } else {
                graduateStudents.put(id, n);
            }
            n.getProperties().put("researchAssistant", "true");
        }
        subjects.push(n);
    }

    @Override
    public void endSection(int classType) {
        Node n = subjects.pop();

        if (!isAboutSection) {
            if (!isGraduateStudent) {
                writeNode(n);
            }
        }

        isAboutSection = false;
        isGraduateStudent = false;
    }

    @Override
    public void addProperty(int property, String value, boolean isResource) {
        callbackTarget.addPropertyCB(property);

        Node n = subjects.peek();
        if (isResource) {
            Edge e = new Edge(Ontology.PROP_TOKEN[property], n.getId(), value);
            writeEdge(e);
        } else {
            n.getProperties().put(Ontology.PROP_TOKEN[property], value);
        }
    }

    @Override
    public void addProperty(int property, int valueClass, String valueId) {
        callbackTarget.addPropertyCB(property);
        callbackTarget.addValueClassCB(valueClass);
        Node n = subjects.peek();
        Edge e = new Edge(Ontology.PROP_TOKEN[property], n.getId(), valueId);
        writeEdge(e);
        if (property == Ontology.CS_P_UNDERGRADFROM || property == Ontology.CS_P_GRADFROM
                || property == Ontology.CS_P_DOCFROM) {

            // Is this a university that won't be generated?
            // i.e. is it's numeric ID outside the range startIndex to
            // (startIndex + numUniversites - 1)
            int idStartPos = valueId.lastIndexOf('y') + 1;
            int idEndPos = valueId.lastIndexOf('.');
            int univId = Integer.parseInt(valueId.substring(idStartPos, idEndPos));
            if (univId < this.state.getStartIndex()
                    || univId >= this.state.getStartIndex() + this.state.getNumberUniversities()) {
                synchronized (requiredUniversities) {
                    if (!requiredUniversities.contains(valueId)) {
                        // Generate university now
                        Node u = new Node(valueId, Ontology.CLASS_TOKEN[Ontology.CS_C_UNIV]);
                        u.getProperties().put("uri", valueId);
                        u.getProperties().put("type", Ontology.CLASS_TOKEN[Ontology.CS_C_UNIV]);
                        writeNode(u);

                        // Remember we've generated it
                        requiredUniversities.add(valueId);
                    }
                }
            }

        }
    }

    private void storeGraduateStudents() {
        for (Entry<String, Node> gradStudent : graduateStudents.entrySet()) {
            writeNode(gradStudent.getValue());
        }
        graduateStudents.clear();
    }
}
