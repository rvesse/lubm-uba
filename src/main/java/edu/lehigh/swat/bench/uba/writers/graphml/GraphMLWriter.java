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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.lehigh.swat.bench.uba.Generator;
import edu.lehigh.swat.bench.uba.writers.Writer;

public class GraphMLWriter implements Writer {
	private static final String HEADER = "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n"
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
			+ "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns\n"
			+ "http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">";
	private static final String END = "</graphml>"; 	
	private static final String GRAPH_START = "<graph id=\"LUMB\" edgedefault=\"directed\">";
	private static final String GRAPH_END = "</graph>";
	private static final boolean[] IS_NODE_ATTRIBUTE = { true, false, false, false, false, false, false, false, false, false, false, false, true, true, false, false };
	
	private Generator generator;
	private PrintStream out;
	private ArrayList<String> edges;
	private String currentID;
	private String line;
	private boolean isAboutSection;
	private HashSet<String> universities;
	private HashSet<String> requiredUniversities;
	private HashMap<String, String> graduateStudents;
	private boolean isGraduateStudent;
	
	public GraphMLWriter(Generator generator, boolean isSorted) {
		this.generator = generator;
		this.out = null;
		this.edges = new ArrayList<String>();
		this.currentID = null;
		this.line = null;
		this.isAboutSection = false;
		this.universities = new HashSet<String>();
		this.requiredUniversities = new HashSet<String>();
		this.isGraduateStudent = false;
		this.graduateStudents = new HashMap<String, String>();
	}
	
	@Override
	public void start() {
		String file = System.getProperty("user.dir") + System.getProperty("file.separator") + Generator.PATH_DATA + System.getProperty("file.separator") + "university" + generator._getFileSuffix();
		
		try {
			out = new PrintStream(file);
			generateHeader();
			insertAttributes();
			openGraph();
		} catch (FileNotFoundException e) {
			System.err.println("Cannot create the output file.");
		}
	}

	@Override
	public void end() {
		storeRequiredUniversities();
		closeGraph();
		generateEnd();
		out.close();
	
		try {
			GraphFormatConverter.convertForNeo4j(System.getProperty("user.dir") + System.getProperty("file.separator") + Generator.PATH_DATA, "university" + generator._getFileSuffix());
		} catch (IOException e) {
			System.err.println("Cannot convert GraphML for Neo4j.");
		}
	}

	@Override
	public void startFile(String fileName) {
	}

	@Override
	public void endFile() {
		storeGraduateStudents();
	}

	@Override
	public void startSection(int classType, String id) {
		generator.startSectionCB(classType);
		currentID = id;
		line = "<node id=\"" + id + "\" labels=\"" + Generator.CLASS_TOKEN[classType] + "\">\n" 
				+ "\t<data key=\"uri\">" + id + "</data>\n"
				+ "\t<data key=\"type\">" + Generator.CLASS_TOKEN[classType] + "</data>";
		
		if(classType == 0) {
			universities.add(id);
			out.print(line);
		}
		else if(classType == 10) {
			graduateStudents.put(id, line);
			isGraduateStudent = true;
		}
		else {
			out.print(line);
		}
	}

	@Override
	public void startAboutSection(int classType, String id) {
		generator.startAboutSectionCB(classType);
		
		currentID = id;
		isAboutSection = true;
		if(classType == 12) {
			line = "\t<data key=\"researchAssistant\">" + true + "</data>\n";
			String temp = graduateStudents.remove(id);
			graduateStudents.put(id, temp + line);
		}
	}

	@Override
	public void endSection(int classType) {
		if(!isAboutSection) {
			if(!isGraduateStudent) {
				line = "</node>";
				out.println(line);
			}
		}
		
		for(String edge : edges) {
			out.println(edge);
		}
		edges.clear();
		isAboutSection = false;
		isGraduateStudent = false;
	}

	@Override
	public void addProperty(int property, String value, boolean isResource) {
		generator.addPropertyCB(property);

		if (isResource) {
			edges.add("<edge id=\"" + Generator.PROP_TOKEN[property] + "\" label=\"" + Generator.PROP_TOKEN[property] + "\" source=\"" + currentID + "\" target=\"" + value + "\">\n</edge>");
		}
		else {
			line = "\t<data key=\"" + Generator.PROP_TOKEN[property] + "\">" + value + "</data>";
			if(isGraduateStudent) {
				String temp = graduateStudents.remove(currentID);
				graduateStudents.put(currentID, temp + line + "\n");
			}
			else {
				out.println(line);
			}
		}
	}

	@Override
	public void addProperty(int property, int valueClass, String valueId) {
		generator.addPropertyCB(property);
		generator.addValueClassCB(valueClass);
		edges.add("<edge id=\"" + Generator.PROP_TOKEN[property] + "\" label=\"" + Generator.PROP_TOKEN[property] + "\" source=\"" + currentID + "\" target=\"" + valueId + "\">\n</edge>");
		
		if(property == 3 || property == 4 || property == 5) {
			requiredUniversities.add(valueId);
		}
	}

	private void generateHeader() {
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
		out.println(HEADER);
	}
	
	private void generateEnd() {
		out.println(END);
	}
	
	private void openGraph() {
		out.println(GRAPH_START);
	}
	
	private void closeGraph() {
		out.println(GRAPH_END);
	}
	
	private void insertAttributes() {
		out.println("<key id=\"type\" for=\"node\" attr.name=\"type\" attr.type=\"string\"/>");
		out.println("<key id=\"uri\" for=\"node\" attr.name=\"uri\" attr.type=\"string\"/>");
		out.println("<key id=\"researchAssistant\" for=\"node\" attr.name=\"researchAssistant\" attr.type=\"boolean\"/>");
		for(int index = 0;index < Generator.PROP_TOKEN.length;index++) {
			if(IS_NODE_ATTRIBUTE[index]) {
				line = "<key id=\"" + Generator.PROP_TOKEN[index] + "\" for=\"node\" attr.name=\"" + Generator.PROP_TOKEN[index] + "\" attr.type=\"string\"/>";
			}
			else {
				line = "<key id=\"" + Generator.PROP_TOKEN[index] + "\" for=\"edge\" attr.name=\"" + Generator.PROP_TOKEN[index] + "\" attr.type=\"string\"/>";
			}
			out.println(line);
		}
	}
	
	private void storeRequiredUniversities() {
		for(String university : requiredUniversities) {
			if(!universities.contains(university)) {
				line = "<node id=\"" + university + "\" labels=\"" + Generator.CLASS_TOKEN[0] + "\">\n" 
						+ "\t<data key=\"uri\">" + university + "</data>\n"
						+ "\t<data key=\"type\">" + Generator.CLASS_TOKEN[0] + "</data>\n"
						+ "</node>";
				out.println(line);
			}
		}
	}
	
	private void storeGraduateStudents() {
		for(String key : graduateStudents.keySet()) {
			out.println(graduateStudents.get(key) + "</node>");
		}
		graduateStudents.clear();
	}
}
