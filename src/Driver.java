import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;

class Driver{
	int MAX = 500;
	public static void main(String[] args) throws IOException, ClassNotFoundException{
//		ArrayList c= new ArrayList<ArrayList>();
//		ArrayList csub = new ArrayList<String>();
//		csub.add("world"); csub.add("hello"); csub.add("yum");
//		Collections.sort(csub);
//		c.add(csub);
//		ArrayList csub2 = new ArrayList<String>();
//		csub2.add("world"); csub2.add("hello");
//		Collections.sort(csub2);
//		System.out.println(csub);
//		System.out.println(c.containsAll(csub2));
//		if(c.containsAll(csub2)){
//			System.out.println("Contains");
//		}else{
//			System.out.println("Does not contain");
//		}
		
		//String[] parccprompts = { "0281", "2208", "2421", "4305", "6643", "8554", "VF643242",  "VF802885", "0282",
				//"2349", "4126", "4326", "8014", "VF643244",  "VH014988", "0613","2350", "4127", "6148", "8154", "VF651229"};
		//"0029",     "0450",     "0518",     "0592",     "0691",     "2027",       "2187",     
		//"2406", "2420",     "2727",     "4072",     "4174",      "4302",     "4336",     "6138",     "6490",     "6635",     "6909",      "6910",     
				//"6919",     "6964",     "8015",     "8040",     "8153",      "8270",     "8699",     "VF581947", 
//				String[] parccprompts = { 	"VF608787", "VF640654", 
//				"VF641289",  "VF646462", "VF647317", "VF651979", "VF653522", "VF737543", "VF820533",  "VF821667", "VF821677", "VF882724", 
//				"VF884220", "VF906000", "VF907744",  "VF908672", "VF909277", "VF909282", "VH000408", "VH012735", "VH014982",  "VH017547", 
//				"VH017614", "VH024097", "VH036436", "VH036969"};
				String[] prompts = {"Set1", "Set2", "Set3", "Set4", "Set5", "Set6", "Set7", "Set8", "Set9", "Set10" };
		//String[] parccprompts = {"VF651229"};
		for(int k=0; k < prompts.length; k++){
			Driver dr = new Driver();
			//Step 1:
			//get the rubric text as a set of sentence segments, and top-scoring responses
			//rubricSegments have the top scoring essays for PARCC since there are no sample respones
			//String[] rubricSegments = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/PARCC/Essays/bestresponses.train."+ parccprompts[k] +".csv", 0);
			String[] rubricSegments = dr.readFromFile("/Users/lakshmiramachandran/Documents/Kaggle/ASAP-SAS/regexes/"+prompts[k]+"-rubric.csv", 1);
			//topScoringResponses has top two scored essays
			//String[] topScoringResponses = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/PARCC/Essays/topscorers.train."+ parccprompts[k] +".csv", 0);
			String[] topScoringResponses = dr.readFromFile("/Users/lakshmiramachandran/Documents/Kaggle/ASAP-SAS/regexes/"+prompts[k]+"-topscorers.csv", 1);
			//String[] promptStimulusText = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/PARCC/Essays/prompttext."+ parccprompts[k] +".csv", 1);
			//String[] promptStimulusText = dr.readFromFile("/Users/lakshmiramachandran/Documents/pearson-datasets/PARCC/Essays/prompttext."+ parccprompts[k] +".csv", 1);
			
			//Step 2:
			//clean the rubric responses and top-scoring answers -- eliminate stopwords
			EliminateStopWords elim = new EliminateStopWords();
			rubricSegments = elim.eliminateStopWords(rubricSegments);
			topScoringResponses = elim.eliminateStopWords(topScoringResponses);
			//promptStimulusText = elim.eliminateStopWords(promptStimulusText);
			
			//Step 3: Tokenize rubric text
			Vocabulary v = new Vocabulary();
			ArrayList<String> rubricTokens = v.tokenize(rubricSegments);
//			csvWriter.append(rubricTokens.toString()); csvWriter.append("\n");
			ArrayList<String> topScoringTokens = v.tokenize(topScoringResponses);
//			csvWriter.append(topScoringTokens.toString()); csvWriter.append("\n");
			//ArrayList<String> promptStimulusTokens = v.tokenize(promptStimulusText);
//			csvWriter.append(rubricTokens.toString()); csvWriter.append("\n");
			
			//Step 4: Select most frequent words in the rubric tokens
			FrequentTokens ft = new FrequentTokens();
			rubricTokens = ft.getFrequentTokens(rubricTokens);
			topScoringTokens = ft.getFrequentTokens(topScoringTokens);
			//promptStimulusTokens = ft.getFrequentTokens(promptStimulusTokens);
			//topScoringTokens.addAll(promptStimulusTokens);
			
			//Step 3:
			//String writeToFile = "/Users/lakshmiramachandran/Documents/pearson-datasets/PARCC/Essays/autoregex."+ parccprompts[k] +".csv";
//			csvWriter.append(rubricTokens.toString()); csvWriter.append("\n");
//			csvWriter.append(topScoringTokens.toString()); csvWriter.append("\n");
//			csvWriter.append(promptStimulusTokens.toString()); csvWriter.append("\n");
//			csvWriter.append(topScoringTokens.toString()); csvWriter.append("\n");
//			csvWriter.close();
			
			//step 4:
			String writeToFile = "/Users/lakshmiramachandran/Documents/Kaggle/ASAP-SAS/regexes/"+prompts[k]+"-auto-regex.csv";
			PrintWriter csvWriter = new PrintWriter(new FileWriter(writeToFile));
			GenerateEquivalenceClasses genEqClass = new GenerateEquivalenceClasses();
			//for each sentence segment in the rubric{
			ArrayList finalListOfTokenClasses = new ArrayList();
			System.out.println("rubricSegments.length: "+rubricSegments.length);
			for(int i = 0; i < rubricSegments.length; i++){
			  System.out.println("rubricSegments["+i+"]: "+rubricSegments[i]);
			  //Identify equivalence classes for the tokens in the rubric text (using semantic relatedness metrics include spellmistakes)
			  //finalListOfTokenClasses = genEqClass.identifyClassesOfWords(rubricSegments[i], topScoringResponses, csvWriter, writeToFile, finalListOfTokenClasses);
			  finalListOfTokenClasses = genEqClass.identifyClassesOfWords(rubricTokens, topScoringTokens, finalListOfTokenClasses);
			  System.out.println("finalListOfTokenClasses: "+finalListOfTokenClasses);
			}
			
			System.out.println("finalListOfTokenClasses.size(): "+finalListOfTokenClasses.size());
			//writing out the results
			for(int i = 0; i < finalListOfTokenClasses.size(); i++){
				if(finalListOfTokenClasses.get(i) == null)
						continue;
				System.out.println((finalListOfTokenClasses.get(i)).toString());
				String temp = (finalListOfTokenClasses.get(i)).toString().replace("] @@ [", ").*)(?=.*(");
				temp = temp.replace("@@ [", "(?=.*(");
				temp = temp.replace("]", ").*)");
				temp = temp.replace("[", "(?=.*(");
				temp = temp.replace(", ", "|");
				csvWriter.append(temp.trim()); csvWriter.append("\n");
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
				System.out.println(segments[i-1]);
			}
			i=i+1;
		}
		segments = Arrays.copyOf(segments, i);
		return segments;
	}
}