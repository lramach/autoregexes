import is2.lemmatizer.Options;
import is2.parser.Parser;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.didion.jwnl.JWNL;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class DriverGenPOSRegexes{
	int MAX = 35000;
	public static void main(String[] args) throws IOException, ClassNotFoundException{
		
		//getting the text
		MaxentTagger posTagger = new MaxentTagger("/Users/lakshmiramachandran/Documents/Add-ons/stanford-postagger-2011-09-14/models/bidirectional-distsim-wsj-0-18.tagger");
		//for the graph-genator's SRL code
		String[] opts ={"-model","/Users/lakshmiramachandran/Documents/Add-ons/srl-20101031/featuresets/prs-eng.model"};
		Options options = new Options(opts);
		// create a parser
		Parser parser = new Parser(options);

		//initializing the dictionary for JWNL
		//initializing the Java WordNet Library
		String propsFile = "/Users/lakshmiramachandran/Documents/workspace/ShortAnswerScoringGraphRelevance/file_properties.xml";
		try {
			JWNL.initialize(new FileInputStream(propsFile));
		} catch (Exception ex) {
		    ex.printStackTrace();
		    System.exit(-1);
		}
		
		//"50479", "50670-07", "50674-07", "50942", "51034", "80090", "80138-07", "80530-06", 
//		String[] prompts = {"85056-08", "85076-08"};
//		String[] prompts = {"Set1", "Set2", "Set3", "Set4", "Set5", "Set6", "Set7", "Set8", "Set9", "Set10" };
		String[] prompts = {"COSC120160", "COSC120170", "COSC120274", "COSC120275", "COSC130037", "COSC130066",  //"COSC130086",
				 "COSC130088", "COSC130108", "COSC130196", "COSC130202", "COSC130242", "COSC130247", 
				"COSC130267", "COSS120325", "COSS120329", "COSS120363", "COSS120382","COSS130095", "COSS130110"};
//		String[] prompts = {"COSC120274"};
		for(int k=0; k < prompts.length; k++){
			DriverGenPOSRegexes dr = new DriverGenPOSRegexes();
			//Step 1:
			//get the rubric text as a set of sentence segments, and top-scoring responses
			//rubricSegments have the top scoring essays for PARCC since there are no sample respones
//			String[] rubricSegments = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/Colorado/Operational Data/autoregexes/"+prompts[k]+"-rubric-and-topscorers.csv", 1);
			String[] rubricSegments = dr.readFromFile("/Users/lakshmiramachandran/Documents/Kaggle/ASAP-SAS/regexes/"+prompts[k]+"-rubric.csv", 1);
			//topScoringResponses has top two scored essays
			String[] topScoringResponses = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/Colorado/Operational Data/Attempt01/regexes/"+prompts[k]+"-topscorers.csv", 1);
//			String[] topScoringResponses = dr.readFromFile("/Users/lakshmiramachandran/Documents/Kaggle/ASAP-SAS/regexes/"+prompts[k]+"-topscorers.csv", 1);
			//if prompt-stimulus text is available
//			String[] promptStimulusText = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/Colorado/Operational Data/autoregexes"+prompts[k]+"-prompt-stimulus.csv", 1);
//			String[] promptStimulusText = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/Maryland/"+prompts[k]+"/Set"+prompts[k]+"-prompt-stimulus.csv", 1);
			
			//Step 2:
			//extract long phrases from the text (edges from the word-order graph generated)
			ExtractPhrases extractphr = new ExtractPhrases();
			ArrayList<String> rubricPhrases = extractphr.extractPhrasesFromText(rubricSegments, posTagger, parser);
			//System.out.println(rubricPhrases);
			
			//eliminate or replace stop-words
			//Step 3:
			//clean the rubric responses and top-scoring answers -- eliminate stopwords
			ElimOrReplaceStopWords elim = new ElimOrReplaceStopWords();
			//replace stop-words with \\w{0,4} in the extracted rubric phrases
			rubricPhrases = elim.replaceStopWords(rubricPhrases);
			
			//step 4:
			//Identify equivalence classes for the tokens in the rubric text (using semantic relatedness metrics include spelling mistakes)
//			String writeToFile = "/Users/lakshmiramachandran/Documents/pearson-datasets/Maryland/"+prompts[k]+"/Set"+prompts[k]+"-regex-phrases.csv";
			String writeToFile = "/Users/lakshmiramachandran/Documents/Kaggle/ASAP-SAS/regexes/"+prompts[k]+"-pos-phrases.csv";
			PrintWriter csvWriter = new PrintWriter(new FileWriter(writeToFile));
			ArrayList finalListOfTokenClasses = new ArrayList();
			
			//step 5: 
			//iterate over the extracted phrases in the rubric texts and get POS tagged regexes
			Posregex posregexes = new Posregex();
			ArrayList<String> outputRubricPhrases = new ArrayList<String>();
			System.out.println("rubric phrases: "+rubricPhrases.size());
			for(int i = 0; i < rubricPhrases.size(); i++){
			  System.out.println("rubricPhrases.get("+i+"): "+rubricPhrases.get(i));
			  String output = posregexes.identifyPOStagsforPhrases(rubricPhrases.get(i), posTagger);
			  if(output != null && !finalListOfTokenClasses.contains(output)){
				  finalListOfTokenClasses.add(output);
			  }
			}
			//step 6:
			//writing out the results, converting to Perl regex format
			for(int i = 0; i < finalListOfTokenClasses.size(); i++){
				if(finalListOfTokenClasses.get(i) == null)
						continue;
				String temp = finalListOfTokenClasses.get(i).toString();
				System.out.println("temp before: "+temp);
				if(temp.contains("] @@ ["))
					temp = temp.replace("] @@ [", ").*)(?=.*(");
				if(temp.contains("]["))
					temp = temp.replace("][", ").*)(?=.*(");
				if(temp.contains("@@ ["))
					temp = temp.replace("@@ [", "(?=.*(");
				if(temp.contains("} "))
					temp = temp.replace("} ", "}");
				if(temp.contains(") "))
					temp = temp.replace(") ", ")");
				if(temp.contains("] "))
					temp = temp.replace("] ", "]");
				temp = temp.replace("]", ").*)");
				temp = temp.replace("[", "(?=.*(");
				temp = temp.replace(", ", "|");
				System.out.println("temp after: "+temp);
				csvWriter.append(temp); 
				csvWriter.append("\n");
			}
			csvWriter.close();
		}
	}
	
	public String[] readFromFile(String filename, int flag) throws IOException{
		String[] segments = new String[MAX];
		BufferedReader reader = new BufferedReader(new FileReader(filename));
		String temp = "";
		int i = 0;
		StringTokenizer st;
		while((temp = reader.readLine()) != null){
			if(i > 0){ //skipping the header in the .csv file
				if(flag == 0){
					st = new StringTokenizer(temp, ",");
					st.nextToken(); st.nextToken(); st.nextToken();
					segments[i-1] = st.nextToken();
				} else{
					segments[i-1] = temp;
				}
				//System.out.println("i-1: "+(i-1)+" -- "+segments[i-1]);
			}
			i=i+1;
		}
		segments = Arrays.copyOf(segments, i);
		return segments;
	}
}