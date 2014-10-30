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

class Driver{
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
		
		String[] prompts = {"A", "B", "C"};
		for(int k=0; k < prompts.length; k++){
			Driver dr = new Driver();
			//Step 1:
			//get the rubric text as a set of sentence segments, and top-scoring responses
			//rubricSegments have the top scoring essays for PARCC since there are no sample respones
			String[] rubricSegments = dr.readFromFile(args[0], 1);
			//topScoringResponses has top two scored essays
			String[] topScoringResponses = dr.readFromFile(args[1], 1);
			//if prompt-stimulus text is available
			
			//Step 2:
			//extract long phrases from the text (edges from the word-order graph generated)
			ExtractPhrases extractphr = new ExtractPhrases();
			ArrayList<String> rubricPhrases = extractphr.extractPhrasesFromText(rubricSegments, posTagger, parser);
			//System.out.println(rubricPhrases);
			
			//eliminate or replace stop-words
			//Step 3A:
			//clean the rubric responses and top-scoring answers -- eliminate stopwords
			ElimOrReplaceStopWords elim = new ElimOrReplaceStopWords();
			rubricSegments = elim.eliminateStopWords(rubricSegments);
			topScoringResponses = elim.eliminateStopWords(topScoringResponses);
			//promptStimulusText = elim.eliminateStopWords(promptStimulusText);
			
			//Step 3B:
			//replace stop-words with \\w{0,4} in the extracted rubric phrases
			rubricPhrases = elim.replaceStopWords(rubricPhrases);
			
			//Step 4: Tokenize rubric and top-scoreres' text
			Tokenize tok = new Tokenize();
			ArrayList<ArrayList<String>> rubricTokens = tok.tokenizeRubric(rubricSegments);
			ArrayList<String> topScoringTokens = tok.tokenizeTopScorers(topScoringResponses);
			//ArrayList<String> promptStimulusTokens = tok.tokenizeTopScorers(promptStimulusText);
			//adding prompt-stimulus text tokens to set of top scoring tokens
			//topScoringTokens.addAll(promptStimulusTokens);
			
			//Step 5: Select most frequent words among the topscorers' and prompt/stimulus texts' tokens
			//so comparison of the rubric text is with a subset of tokens only
			FrequentTokens ft = new FrequentTokens();
			topScoringTokens = ft.getFrequentTokens(topScoringTokens);
			
			//step 6A:
			//Identify equivalence classes for the tokens in the rubric text (using semantic relatedness metrics include spelling mistakes)
			String writeToFile = args[2];
			PrintWriter csvWriter = new PrintWriter(new FileWriter(writeToFile));
			GenerateEquivalenceClasses genEqClass = new GenerateEquivalenceClasses();
			ArrayList finalListOfTokenClasses = new ArrayList();
			System.out.println("# of rubric segments: "+rubricTokens.size());
			for(int i = 0; i < rubricTokens.size(); i++){//every element contains tokens from each rubric segment
			  System.out.println("rubricSegments["+i+"]: "+rubricTokens.get(i));
			  finalListOfTokenClasses = genEqClass.identifyClassesOfWords(rubricTokens.get(i), topScoringTokens, finalListOfTokenClasses, posTagger);
			  System.out.println("finalListOfTokenClasses: "+finalListOfTokenClasses);
			}
			
			//step 6B: 
			//iterate over the extracted phrases in the rubric texts
			ArrayList<String> outputRubricPhrases = new ArrayList<String>();
			System.out.println("rubric phrases: "+rubricPhrases.size());
			for(int i = 0; i < rubricPhrases.size(); i++){
			  System.out.println("rubricPhrases.get("+i+"): "+rubricPhrases.get(i));
			  finalListOfTokenClasses.add(genEqClass.identifyClassesOfPhrases(rubricPhrases.get(i), topScoringTokens, posTagger));
			}
			
			//step 7:
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
			if(flag == 0){
				st = new StringTokenizer(temp, ",");
				st.nextToken(); st.nextToken(); st.nextToken();
				segments[i] = st.nextToken();
			} else{
				segments[i] = temp;
			}
			i=i+1;
		}
		segments = Arrays.copyOf(segments, i);
		return segments;
	}
}