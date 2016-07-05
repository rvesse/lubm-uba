package edu.lehigh.swat.bench.uba.writers.graphml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class GraphFormatConverter { 
	private static final int TYPE_NULL = 0;
	private static final int TYPE_NODE = 1;
	private static final int TYPE_EDGE = 2;
	
	public static void convertForNeo4j(String workingDirectory, String inputFile) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(workingDirectory + System.getProperty("file.separator") + inputFile));
		PrintWriter nodeWriter = new PrintWriter(new FileWriter(workingDirectory + System.getProperty("file.separator") + "node.graphml"));
		PrintWriter edgeWriter = new PrintWriter(new FileWriter(workingDirectory + System.getProperty("file.separator") + "edge.graphml"));
		String line = null;
		
		while((line = reader.readLine()) != null) {
			nodeWriter.println(line);
			if(line.startsWith("<graph id"))
				break;
		}
		
		int type = TYPE_NULL;
		while((line = reader.readLine()) != null) {
			if(line.startsWith("<node"))
				type = TYPE_NODE;
			else if(line.startsWith("<edge") || line.startsWith("</graph>") || line.startsWith("</graphml>"))
				type = TYPE_EDGE;
			
			if(type == TYPE_NODE)
				nodeWriter.println(line);
			else if(type == TYPE_EDGE)
				edgeWriter.println(line);
		}
		
		reader.close();
		nodeWriter.close();
		edgeWriter.close();
		
		reader = new BufferedReader(new FileReader(workingDirectory + System.getProperty("file.separator") + "node.graphml"));
		PrintWriter writer = new PrintWriter(new FileWriter(workingDirectory + System.getProperty("file.separator") + "neo4j.graphml"));
		while((line = reader.readLine()) != null) {
			writer.println(line);
		}
		reader.close();
		reader = new BufferedReader(new FileReader(workingDirectory + System.getProperty("file.separator") + "edge.graphml"));
		while((line = reader.readLine()) != null) {
			writer.println(line);
		}
		reader.close();
		writer.close();
		
		File file = new File(workingDirectory + System.getProperty("file.separator") + "node.graphml");
		if(file.exists())
			file.delete();
		
		file = new File(workingDirectory + System.getProperty("file.separator") + "edge.graphml");
		if(file.exists())
			file.delete();
		
		file = new File(workingDirectory + System.getProperty("file.separator") + inputFile);
		if(file.exists())
			file.delete();
		
		file = new File(workingDirectory + System.getProperty("file.separator") + "neo4j.graphml");
		if(file.exists())
			file.renameTo(new File(workingDirectory + System.getProperty("file.separator") + inputFile));
	}
	
	public static void convertForGraphX(String nodeFile, String edgeFile, String outputFile) throws IOException {
		PrintWriter writer = new PrintWriter(new FileWriter(outputFile));
		BufferedReader reader = new BufferedReader(new FileReader(nodeFile));
		String previousLine = null, line = null;
		
		previousLine = reader.readLine();
		while(true) {
			line = reader.readLine().trim();
			if(line.equals(""))continue;
			
			if(line.startsWith("]")) {
				String tempLine = previousLine.trim();
				writer.print(tempLine.substring(0, tempLine.length() - 1) + "]");
				break;
			}
			else {
				writer.print(previousLine.trim());
			}
			previousLine = line;
		}
		reader.close();
		reader = new BufferedReader(new FileReader(edgeFile));
		reader.readLine();
		writer.print(",");
		
		previousLine = reader.readLine();
		while(true) {
			line = reader.readLine().trim();
			if(line.equals(""))continue;
			
			if(line.startsWith("]")) {
				String tempLine = previousLine.trim();
				writer.print(tempLine.substring(0, tempLine.length() - 1) + "]}");
				break;
			}
			else {
				writer.print(previousLine.trim());
			}
			previousLine = line;
		}
		reader.close();
		writer.close();
		
		File file = new File(nodeFile);
		if(file.exists()) 
			file.delete();
		
		file = new File(edgeFile);
		if(file.exists())
			file.delete();
	}
}
