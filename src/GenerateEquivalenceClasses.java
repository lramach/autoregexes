import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

class GenerateEquivalenceClasses{
	double threshold = 2.0;
	public ArrayList identifyClassesOfWords(ArrayList<String> rubricTokens, ArrayList<String> topScoringTokens, 
		ArrayList finalListOfTokenClasses) throws ClassNotFoundException, IOException{
		
		ArrayList<ArrayList> tokenClasses = new ArrayList<ArrayList>();
		WordnetBasedSimilarity wn = new WordnetBasedSimilarity();
		int sizes = 0;
		for(int i = 0; i < rubricTokens.size(); i++){
			//'token' is the unigram from the sentence segment in the rubirc text
			String token = rubricTokens.get(i);
			String temptoken = token;
			ArrayList tokenClass = new ArrayList<String>();//initializing the class
			//replacing common suffixes with (aaa)? and making it optional
			PorterStemmer s = new PorterStemmer();
			String[] stemText = s.getStemmedTextAndSuffix(temptoken, s);
			if(stemText[1] != "")
				temptoken = stemText[0]+"("+stemText[1]+")?";
			else
				temptoken = stemText[0];
			tokenClass.add(temptoken);//adding the stemmed token
					
			//compare token with words in the top scoring responses
			for(int j = 0; j < topScoringTokens.size(); j++){
				//compare the rubric token with each top score token
				String toptoken = topScoringTokens.get(j);//'toptoken' is the token from the top-scoring response
				//compare rubric token and the top scoring tokens.
				double match = wn.compareStrings(token, toptoken);
				//if the tokens' match is above the threshold
				if(match >= threshold && match != WordnetBasedSimilarity.EXACT){ //add the matched words to the class, don't have to add EXACT matches
					//stemming toptoken before it is added to the list
					stemText = s.getStemmedTextAndSuffix(toptoken, s);
					if(stemText[1] != "")
						toptoken = stemText[0]+"("+stemText[1]+")?";
					else
						toptoken = stemText[0];
						
					if(!tokenClass.contains(toptoken))
						tokenClass.add(toptoken.trim());
				}
					
				System.out.println("Eq. classes for token:"+token);
				System.out.println(tokenClass.toString());
				
				//sorting the arraylist before adding it to make it easy to compare unordered lists
				Collections.sort(tokenClass);
				if(!tokenClasses.contains(tokenClass)){
					System.out.println("Adding: "+tokenClass);
					tokenClasses.add(tokenClass);
					sizes += tokenClass.size();
				}
			}
		}
			
		//sort the token classes based on the number of elements in each array list, select the top few
		MergeSorting msort = new MergeSorting();
		tokenClasses = msort.sorting(tokenClasses, 0);
		System.out.println("tokenClasses: "+tokenClasses);
				
		String concatListOfTokens = "";
		//select the top token-classes
		//restrict them to 5-grams
		int grams = 0;
		for(ArrayList tokClass : tokenClasses){
			if(!finalListOfTokenClasses.contains(tokClass)){
				if(tokClass.size() > 1){
					if(!finalListOfTokenClasses.contains(tokClass))
						finalListOfTokenClasses.add(tokClass);
				}
				if(grams < 5){
					concatListOfTokens = concatListOfTokens + " @@ "+tokClass; //add the tokens to the final list
					grams++;
				} else if(grams == 5){
					if(!finalListOfTokenClasses.contains(concatListOfTokens.trim()) && grams > 1){
						System.out.println("concatListOfTokens: "+concatListOfTokens);
						finalListOfTokenClasses.add(concatListOfTokens.trim());
					}
					//reset
					grams = 0;
					concatListOfTokens = "";
				}
			}
		}
		return finalListOfTokenClasses;
	}
	
	//finalListOfTokenClasses contains the final list of tokens
	public ArrayList identifyClassesOfWords(String segment, String[] topscorers, PrintWriter csvWriter, String filename, ArrayList finalListOfTokenClasses) throws ClassNotFoundException, IOException{
		if(segment == null){
			return finalListOfTokenClasses; // return old list without adding any new token lists
		}
		if(!segment.isEmpty() || segment != null){
			StringTokenizer st = new StringTokenizer(segment);
			ArrayList<ArrayList> tokenClasses = new ArrayList<ArrayList>();
			WordnetBasedSimilarity wn = new WordnetBasedSimilarity();
			int sizes = 0;
			int numTokens = st.countTokens();
			//Iterating over the rubric segment's tokens
			while(st.hasMoreTokens()){//'token' is the unigram from the sentence segment in the rubirc text
				String token = st.nextToken();
				String temptoken = token;
				ArrayList tokenClass = new ArrayList<String>();//initializing the class
				//replacing common suffixes with (aaa)? and making it optional
				PorterStemmer s = new PorterStemmer();
				String[] stemText = s.getStemmedTextAndSuffix(temptoken, s);
				if(stemText[1] != "")
					temptoken = stemText[0]+"("+stemText[1]+")?";
				else
					temptoken = stemText[0];

				tokenClass.add(temptoken);//adding the stemmed token
				//compare token with words in the top scoring responses
				StringTokenizer sttop;
				for(int i = 0; i< topscorers.length; i++){
					if(topscorers[i] != null){
						sttop = new StringTokenizer(topscorers[i]);
						//compare the rubric token with each top score token
						while(sttop.hasMoreTokens()){
							String toptoken = sttop.nextToken();//'toptoken' is the token from the top-scoring response
							//compare rubric token and the top scoring tokens.
							double match = wn.compareStrings(token, toptoken);
							//if the tokens' match is above the threshold
							if(match >= threshold && match != WordnetBasedSimilarity.EXACT){ //add the matched words to the class, don't have to add EXACT matches
								//stemming toptoken before it is added to the list
								stemText = s.getStemmedTextAndSuffix(toptoken, s);
								if(stemText[1] != "")
									toptoken = stemText[0]+"("+stemText[1]+")?";
								else
									toptoken = stemText[0];
//								for(int j = 0; j < Stopwords.suffixes.length; j++){
//									//replacing common suffixes with (aaa)? and making it optional
//									if(toptoken.endsWith(Stopwords.suffixes[j])){
//										toptoken = toptoken.replace(Stopwords.suffixes[j], "("+Stopwords.suffixes[j]+")?");
//										break;
//									}
//								}
								if(!tokenClass.contains(toptoken))
									tokenClass.add(toptoken.trim());
							}
						}
					}
				}
				System.out.println("Eq. classes for token:"+token);
				System.out.println(tokenClass.toString());
				//sorting the arraylist before adding it to make it easy to compare unordered lists
				Collections.sort(tokenClass);
				if(!tokenClasses.contains(tokenClass)){
					System.out.println("Adding: "+tokenClass);
					tokenClasses.add(tokenClass);
					sizes += tokenClass.size();
				}
			} //end of iterating over the rubric tokens
			
			
			//sort the token classes based on the *frequency of stemmed tokens* and not the number of elements in each array list, 
			//(hence code is commented)
			//MergeSorting msort = new MergeSorting();
			//tokenClasses = msort.sorting(tokenClasses, 0);
			//System.out.println("tokenClasses: "+tokenClasses);
			
			String concatListOfTokens = "";
			//select the top token-classes
			//restrict them to 5-grams
			int grams = 0;
			for(ArrayList tokClass : tokenClasses){
				if(!finalListOfTokenClasses.contains(tokClass)){
					//if(tokClass.size() > 1){
						finalListOfTokenClasses.add(tokClass);
					//}
					if(grams < 5){
						//if(tokClass.size() > averageSizeOfTokenClasses){ //if the size of the token classes is greater than the average size 
							concatListOfTokens = concatListOfTokens + " @@ "+tokClass; //add the tokens to the final list
						//}
						grams++;
					}
				}
			}
			System.out.println("concatListOfTokens: "+concatListOfTokens);
			if(!finalListOfTokenClasses.contains(concatListOfTokens.trim()) && grams > 1){
				finalListOfTokenClasses.add(concatListOfTokens.trim());
			}
			return finalListOfTokenClasses;
		}
		else{
			return finalListOfTokenClasses;
		}
	}
}