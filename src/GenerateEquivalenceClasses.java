import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class GenerateEquivalenceClasses{
	double threshold = 2.0;
	public ArrayList identifyClassesOfWords(ArrayList<String> rubricTokens, ArrayList<String> topScoringTokens, 
		ArrayList finalListOfTokenClasses, MaxentTagger posTagger) throws ClassNotFoundException, IOException{
		GenerateEquivalenceClasses genClass = new GenerateEquivalenceClasses();
		ArrayList<ArrayList> tokenClasses = new ArrayList<ArrayList>();
		WordnetBasedSimilarity wn = new WordnetBasedSimilarity();
		int sizes = 0;
		for(int i = 0; i < rubricTokens.size(); i++){
			//'token' is the unigram from the sentence segment in the rubirc text
			String token = rubricTokens.get(i);
			String temptoken = token;
			
			//replacing common suffixes with (aaa)? and making it optional
			PorterStemmer s = new PorterStemmer();
			String[] stemText = s.getStemmedTextAndSuffix(temptoken, s);
			if(stemText[1] != "")
				temptoken = stemText[0]+"("+stemText[1]+")?";
			else
				temptoken = stemText[0];
			//get the class of words for this token
			ArrayList classOfWords = genClass.getClassOfWords(temptoken, topScoringTokens, s, posTagger);
			//sorting the arraylist before adding it to make it easy to compare unordered lists
			Collections.sort(classOfWords);
			if(!tokenClasses.contains(classOfWords)){
				System.out.println("Adding: "+classOfWords);
				tokenClasses.add(classOfWords);
			}
		}
		
		String concatListOfTokens = "";
		//select the top token-classes
		//restrict them to 5-grams
		int grams = 0;
		for(ArrayList tokClass : tokenClasses){
			if(!finalListOfTokenClasses.contains(tokClass)){
				if(tokClass.size() > 1){ //adding each token class individually
					finalListOfTokenClasses.add(tokClass);
				}
				if(grams < 5){ //building the longer array of token classes, more specific regex
					concatListOfTokens = concatListOfTokens + " @@ "+tokClass; //add the tokens to the final list
					grams++;
				} else if(grams == 5){
					if(!finalListOfTokenClasses.contains(concatListOfTokens.trim()) && grams > 1){
						System.out.println("concatListOfTokens: "+concatListOfTokens);
						finalListOfTokenClasses.add(concatListOfTokens.trim());
					}
					//reset the grams so that the rest of the token classes in this rubric segment can be concatenated together
					grams = 0;
					concatListOfTokens = "";
				}
			}
		}
		return finalListOfTokenClasses;
	}
	
	public String identifyClassesOfPhrases(String rubricPhrase, ArrayList<String> topScoringTokens, MaxentTagger posTagger) throws ClassNotFoundException, IOException{
			GenerateEquivalenceClasses genClass = new GenerateEquivalenceClasses();
			WordnetBasedSimilarity wn = new WordnetBasedSimilarity();
			String outputPhrase = "";
			StringTokenizer st = new StringTokenizer(rubricPhrase);
			while(st.hasMoreTokens()){
				//'token' is the unigram from the sentence segment in the rubirc text
				String token = st.nextToken();
				if(!token.contains("(\\\\w{0-4}\\\\s)")){ //if the token is not a frequent word
					String temptoken = token;
					//replacing common suffixes with (aaa)? and making it optional
					PorterStemmer s = new PorterStemmer();
					String[] stemText = s.getStemmedTextAndSuffix(temptoken, s);
					if(stemText[1] != "")
						temptoken = stemText[0]+"("+stemText[1]+")?";
					else
						temptoken = stemText[0];
					//get the class of words for this token
					ArrayList tokenClass = genClass.getClassOfWords(temptoken, topScoringTokens, s, posTagger);
					outputPhrase = outputPhrase +" "+tokenClass;
				}else{
					outputPhrase = outputPhrase +" "+token;
				}
			}//iterating over each of the tokens in the rubric phrase
			System.out.println("outphrase: "+outputPhrase);
			return outputPhrase.trim();
		}
	
	
	public ArrayList getClassOfWords(String token, ArrayList<String> topscorers, PorterStemmer s, MaxentTagger posTagger) throws ClassNotFoundException, IOException{
		//compare token with words in the top scoring responses
		StringTokenizer sttop;
		ArrayList tokenClass = new ArrayList<String>();//initializing the class
		//adding root token
		tokenClass.add(token);
		WordnetBasedSimilarity wn = new WordnetBasedSimilarity();
		String[] tokenSyns = null;
		//TO DO LATER!! call wordnet code for rubric token's values, so this does not have to be recomputed for every topscorer token
		for(int i = 0; i< topscorers.size(); i++){
			if(topscorers.get(i) != null){
				sttop = new StringTokenizer(topscorers.get(i));
				//compare the rubric token with each top score token
				while(sttop.hasMoreTokens()){
					String toptoken = sttop.nextToken();//'toptoken' is the token from the top-scoring response
					//compare rubric token and the top scoring tokens.
					WordNetMatch wordnetMatch = wn.compareStrings(token, toptoken, posTagger);
					double match = wordnetMatch.matchValue;
					tokenSyns = wordnetMatch.synonyms;
					//if the tokens' match is above the threshold
					if(match >= threshold && match != WordnetBasedSimilarity.EXACT){ //add the matched words to the class, don't have to add EXACT matches
						//stemming toptoken before it is added to the list
						String[] stemText = s.getStemmedTextAndSuffix(toptoken, s);
						if(stemText[1] != "")
							toptoken = stemText[0]+"("+stemText[1]+")?";
						else
							toptoken = stemText[0];
						if(!tokenClass.contains(toptoken))
							tokenClass.add(toptoken.trim());
					}
				}
			}
		}
		System.out.println("Synonyms of token: "+token);
		if(tokenSyns != null){
			for(int i = 0; i < tokenSyns.length; i++){
				System.out.println(tokenSyns[i]);
				//stemming toptoken before it is added to the list
				String[] stemText = s.getStemmedTextAndSuffix(tokenSyns[i], s);
				String toptoken;
				if(stemText[1] != "")
					toptoken = stemText[0]+"("+stemText[1]+")?";
				else
					toptoken = stemText[0];
				if(!tokenClass.contains(toptoken))
					tokenClass.add(toptoken);
			}
		}
		System.out.println("Eq. classes for token:"+token);
		System.out.println(tokenClass.toString());
		return tokenClass;
	}
		
}