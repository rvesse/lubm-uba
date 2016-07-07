package edu.lehigh.swat.bench.uba.writers.graphml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;

import edu.lehigh.swat.bench.uba.Generator;
import edu.lehigh.swat.bench.uba.writers.Writer;

public class JSONWriter implements Writer {
	private static final String NODE_LIST_START = "{\n\t\"nodes\":[";
	private static final String EDGE_LIST_START = "{\n\t\"links\":[";
	private static final String NODE_LIST_END = "\n\t]";
	private static final String EDGE_LIST_END = "\n\t]\n}";
	
	private Generator generator;
	private PrintStream outNode;
	private PrintStream outEdge;
	private String line;
	private String currentID;
	private HashMap<String, String> graduateStudents;
	private boolean isAboutSection;
	private boolean isGraduateStudent;
	private String nodeFile;
	private String edgeFile;
	private HashSet<String> universities;
	private HashSet<String> requiredUniversities;
	
	public JSONWriter(Generator generator) {
		this.generator = generator;
		this.outNode = null;
		this.outEdge = null;
		this.line = null;
		this.currentID = null;
		this.isAboutSection = false;
		this.isGraduateStudent = false;
		this.nodeFile = null;
		this.edgeFile = null;
		this.graduateStudents = new HashMap<String, String>();
		this.universities = new HashSet<String>();
		this.requiredUniversities = new HashSet<String>();
	}

	@Override
	public void start() {
		nodeFile = System.getProperty("user.dir") + System.getProperty("file.separator") + Generator.PATH_DATA + System.getProperty("file.separator") + "nodes" + generator._getFileSuffix();
		edgeFile = System.getProperty("user.dir") + System.getProperty("file.separator") + Generator.PATH_DATA + System.getProperty("file.separator") + "edges" + generator._getFileSuffix();
		
		try {
			outNode = new PrintStream(nodeFile);
			outEdge = new PrintStream(edgeFile);
			//openGraph();
		} catch (FileNotFoundException e) {
			System.err.println("Cannot create the output file.");
		}
	}

	@Override
	public void end() {
		storeRequiredUniversities();
		//closeGraph();
		outNode.close();
		outEdge.close();
		//try {
		//	GraphFormatConverter.convertForGraphX(nodeFile, edgeFile, System.getProperty("user.dir") + System.getProperty("file.separator") + Generator.PATH_DATA + System.getProperty("file.separator") + "university" + generator._getFileSuffix());
		//} catch (IOException e) {
		//	System.err.println("Cannot convert to JSON file for GraphX.");
		//};
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
		line = "{\"id\": \"" + id + "\", \"type\": \"" + Generator.CLASS_TOKEN[classType] + "\"";
			
		if(classType == 10) {
			graduateStudents.put(id, line);
			isGraduateStudent = true;
		}
		else if(classType == 0) {
			//graduateStudents.put(id, line + "\n");
			universities.add(id);
			outNode.print(line);
		}
		else {
			outNode.print(line);
		}
	}

	@Override
	public void startAboutSection(int classType, String id) {
		generator.startAboutSectionCB(classType);
		
		isAboutSection = true;
		if(classType == 12) {
			line = ", \"researchAssistant\": " + true;
			String previous = graduateStudents.remove(id);
			graduateStudents.put(id, previous + line);
		}
		else {
			currentID = id;
		}
	}

	@Override
	public void endSection(int classType) {
		if(!isGraduateStudent && !isAboutSection)
			outNode.println("}");
		isGraduateStudent = false;
		isAboutSection = false;
		currentID = null;
	}

	@Override
	public void addProperty(int property, String value, boolean isResource) {
		generator.addPropertyCB(property);
	
		if (isResource) {
			line = "{\"source\": \"" + currentID + "\", \"target\": \"" + value + "\", \"type\": \"" + Generator.PROP_TOKEN[property] + "\"}\n";
			outEdge.print(line);
		}
		else {
			line = ", \"" + Generator.PROP_TOKEN[property] + "\": \"" + value + "\"";
			if(isGraduateStudent) {
				String previous = graduateStudents.remove(currentID);
				graduateStudents.put(currentID, previous + line);
			}
			else {
				outNode.print(line);
			}
		}
	}

	@Override
	public void addProperty(int property, int valueClass, String valueId) {
		generator.addPropertyCB(property);
		generator.addValueClassCB(valueClass);
		
		line = "{\"source\": \"" + currentID + "\", \"target\": \"" + valueId + "\", \"type\": \"" + Generator.PROP_TOKEN[property] + "\"}\n";
		outEdge.print(line);
		
		if(property == 3 || property == 4 || property == 5) {
			requiredUniversities.add(valueId);
		}
	}
	
	private void openGraph() {
		outNode.println(NODE_LIST_START);
		outEdge.println(EDGE_LIST_START);
	}
	
	private void storeGraduateStudents() {
		for(String key : graduateStudents.keySet()) {
			outNode.println(graduateStudents.get(key) + "}");
		}
		graduateStudents.clear();
	}
	
	private void closeGraph() {
		outNode.println(NODE_LIST_END);
		outEdge.println(EDGE_LIST_END);
	}
	
	private void storeRequiredUniversities() {
		for(String university : requiredUniversities) {
			if(!universities.contains(university)) {
				line = "{\"id\": \"" + university + "\", \"type\": \"" + Generator.CLASS_TOKEN[0] + "\"}";
				outNode.println(line);
			}
		}
	}
}
