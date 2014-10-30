import is2.lemmatizer.Options;
import is2.parser.Parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import net.didion.jwnl.JWNL;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class ExtractPhrases{
	public ArrayList<String> extractPhrasesFromText(String[] txtSegments, MaxentTagger posTagger, Parser parser) throws IOException, ClassNotFoundException{
		ArrayList<String> txtPhrases = new ArrayList<String>(); 
				
		GraphGenerator g = new GraphGenerator();
		System.out.println(txtSegments.length);
		//generating graphs
		g.generateGraph(txtSegments, posTagger, 0, parser);
		String concatEdges = "";
		System.out.println("Number of vertices:"+GraphGenerator.numVertices);
		for(int j = 0; j < GraphGenerator.numVertices; j++){
			if(GraphGenerator.vertices[j] != null){
				txtPhrases.add(GraphGenerator.vertices[j].name);
			}
		}
		System.out.println("Number of edges:"+GraphGenerator.numEdges);
		for(int j = 0; j < GraphGenerator.numEdges; j++){
			if(GraphGenerator.edges[j] != null){
				txtPhrases.add(GraphGenerator.edges[j].inVertex.name+ " "+GraphGenerator.edges[j].outVertex.name);
			}
		}
		return txtPhrases;
	}
}