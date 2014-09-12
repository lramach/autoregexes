
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import net.didion.jwnl.JWNL;
import processing.core.PApplet;

import rita.wordnet.*;

public class compareNode extends PApplet{

	//different degree of matching for GRAPH CENTRALITY/PATTERN MATCHING
	public static double NOMATCH = 0; //distinct
	public static double SYNONYM = 1; //paraphrasing
	public static double SUBSTRING = 2; //exact strings
	public static double EXACT = 2; //exact strings
	public static double NEGSYNONYM = -1; //paraphrasing
	public static double ANTONYM = -1;//antonyms
	public static double NEGSUBSTRING = -2; //exact strings
	public static double NEGEXACT = -2; //exact strings
	public static int NUMARRAYELEMENTS;
	public static RiWordnet rwordnet;
	
	public String determinePOS(String str_pos){
		String pos = "";
		//System.out.println("Tagged word::"+str_pos);
		//str_pos = str_pos.substring(str_pos.indexOf("/")+1, str_pos.indexOf("/")+3).trim();
		if(str_pos.contains("CD") || str_pos.contains("NN") || str_pos.contains("PR") || str_pos.contains("IN") || str_pos.contains("EX") || str_pos.contains("WP")){
			pos = "n";
		}
		else if(str_pos.contains("JJ")){
			pos = "a";
		}
		else if(str_pos.contains("TO") || str_pos.contains("VB") || str_pos.contains("MD")){
			pos = "v";
		}
		else if(str_pos.contains("RB")){
			pos = "r";
		}
		else{
			pos = "n";
		}
		return pos;
	}
	
	/*
	 * Checking to see if "check" is present in "containingStr" as a complete word and not a substring of any word in the "containingStr"
	 * string array is of the form [advance, better, improve, meliorate] 
	 */
	public boolean contains(String containingStr, String check){
		
		//beginning of the array of values
		if(containingStr.contains("["+check.toLowerCase()+",") || containingStr.toString().contains(" "+check.toLowerCase()+",") 
				|| containingStr.toString().contains(" "+check.toLowerCase()+"]") || containingStr.toString().contains("["+check.toLowerCase()+"]")){
			return true;
		}
		else
			return false;	
	}
	
	/*
	 * Checking to see if "check" is present in "containingStr" as a complete word and not a substring of any word in the "containingStr"
	 * glosses are of the form [have or possess, either in a concrete or an abstract sense; "She has $1,000 in the bank";]
	 */
	public boolean containsGloss(String containingStr, String check){
		
		//System.out.println("Checking presence of ::"+check);
		//beginning of the array of values
		if(containingStr.contains("["+check.toLowerCase()+" ")|| containingStr.toString().contains(" "+check.toLowerCase()+" ")
				 || containingStr.contains(" "+check.toLowerCase()+",") || containingStr.contains("\""+check.toLowerCase()+" ") 
				 || containingStr.contains(" "+check.toLowerCase()+";") || containingStr.contains(" "+check.toLowerCase()+"\";") 
				|| containingStr.toString().contains(" "+check.toLowerCase()+"]") || containingStr.toString().contains("["+check.toLowerCase()+"]")){
			return true;
		}
		else
			return false;	
	}
	
	/* Compares node's strings and depending on if they were synonyms, hyponyms or hypernyms the 
	 * corresponding integer value is returned
	 * s1 is the submission and s2 is the review
	 * Functions are of the form "function(review, submission/past review)" */
	public double compareStrings(String word1, int state1, String word2, int state2) throws ClassNotFoundException
	{	
		RiWordnet rwordnet = new RiWordnet();
		//System.out.println("After POS:: "+reviewPOS +" AND subm POS:: "+submPOS);
		String w1 = word1;//.substring(0, word1.indexOf("/"));
		String w2 = word2;//.substring(0, word2.indexOf("/"));
		int reviewState = state1;
		int submState = state2;
		
		System.out.println("@@@@@@@@@ Comparing Vertices:: "+w1 +" & "+w2 +":: w1state::" + reviewState+" and w2State:: "+submState);
		double match = 0;//initialise match to 0 
		double count = 0;
		//RiWordnet rwordnet = new RiWordnet();
		//RiLexicon rlex = new RiLexicon();
		//System.out.println("Checking stopword: "+ rlex.isStopWord("this"));
		StringTokenizer stokRev, stokSub;
		stokRev = new StringTokenizer(w1);

		List listRevArr = Arrays.asList(new String[10]);
		List listSubArr = Arrays.asList(new String[10]);
		
		String reviewPOS = "";
		String submPOS ="";
		
		//checking for exact match between the complete vertex name
		if(w1.toLowerCase().equals(w2.toLowerCase())){
			//System.out.println("Review vertex types!"+reviewVertex.type+" && "+submVertex.type);
			if(reviewState == submState)// && reviewVertex.type == submVertex.type)
				match = match + EXACT;
			else if(reviewState != submState)// && reviewVertex.type == submVertex.type)
				match = match + NEGEXACT;			
			return match;
		}
		
		//checking if substrings exist, while ensuring that there are more than one tokens in that vertex
		if(stokRev.countTokens() > 1 && new StringTokenizer(w2).countTokens() > 1 && 
			(w1.toLowerCase().contains(w2.toLowerCase()) || 
			w1.toLowerCase().contains(w2.toLowerCase()))){
			//System.out.println("Review vertex types!"+reviewVertex.type+" && "+submVertex.type);
			if(reviewState == submState)// && reviewVertex.type == submVertex.type)
				match = match + SUBSTRING;
			else if(reviewState != submState)// && reviewVertex.type == submVertex.type)
				match = match + NEGSUBSTRING;			
			System.out.println("Found a substring match between vertices!");
			return match;
		}
				
		//iterating through every review token in the vertex phrase
		while(stokRev.hasMoreTokens()){//traversing review tokens
			//fetching the set of review and submission tokens
			String revToken = stokRev.nextToken().toLowerCase();
			if(reviewPOS == "")//do not reset POS for every new token, it changes the POS of the vertex e.g. like has diff POS for vertices "like"(n) and "would like"(v)
				reviewPOS = determinePOS(word1);
			if(revToken.equals("n't")){
				revToken = "not";
			}
			
			//Initializing the String variables.
			String[] revStems = null;//the stem form of the review token
			String[] revSyn = null;//synonyms of the review token (one of the strings in the phrase)
			String[] revNom = null;//nominalized forms
			String[] revAnt = null;
			String[] revSynset = null;
			String[] revDerivedTerms = null;
			
			try{//some "get" functions return a null and this try-catch block catches those "NullPointerExceptions"
				revSynset = rwordnet.getSynset(revToken.toLowerCase(), reviewPOS);
				revStems = rwordnet.getStems(revToken.toLowerCase(), reviewPOS);				
				revSyn = rwordnet.getSynonyms(revToken.toLowerCase(), reviewPOS);
				revNom = rwordnet.getAllNominalizations(revToken.toLowerCase(), reviewPOS);
				revAnt = rwordnet.getAllAntonyms(revToken.toLowerCase(), reviewPOS);
				revDerivedTerms = rwordnet.getAllDerivedTerms(revToken.toLowerCase(), reviewPOS);
			}
			catch(NullPointerException e){
				e.printStackTrace();
				//System.out.println("Null object returned.");
			}
			
			if(revStems != null){
				listRevArr = Arrays.asList(revStems);
			}			
			stokSub = new StringTokenizer(w2);
			//traversing submission's tokens
			while(stokSub.hasMoreTokens()){
				String subToken = stokSub.nextToken().toLowerCase();
				if(submPOS == "")
					submPOS = determinePOS(word2);
				if(subToken.equals("n't")){
					subToken = "not";
					//System.out.println("replacing n't");
				}

				String[] subStems = null;//all stems of the submission token
				String[] subSyn = null;//synonyms of the submission token (one of the strings in the phrase)
				String[] subNom = null;//nominalized forms 
				String[] subAnt = null;
				String[] subDerivedTerms = null;
				String[] subSynset = null;
				
				try{					
					subStems = rwordnet.getStems(subToken.toLowerCase(), submPOS);
					subSyn = rwordnet.getSynonyms(subToken.toLowerCase(), submPOS);
					subNom = rwordnet.getNominalizations(subToken.toLowerCase(), submPOS);
					subAnt = rwordnet.getAllAntonyms(subToken.toLowerCase(), submPOS);
					subDerivedTerms = rwordnet.getAllDerivedTerms(subToken.toLowerCase(), submPOS);
					subSynset = rwordnet.getSynset(subToken.toLowerCase(), submPOS);
				}
				catch(NullPointerException e){
					e.printStackTrace();
					//System.out.println("Null object returned.");
				}
				
				if(subStems != null){
					//System.out.println("subStem:: "+subStems[0]);
					listSubArr = Arrays.asList(subStems);
				}		
				//checks are ordered from BEST to LEAST degree of semantic relatedness
				//*****exact matches				
				if(subToken.toLowerCase().equals(revToken.toLowerCase()) || (subStems != null && revStems != null && subStems[0].toLowerCase().equals(revStems[0].toLowerCase()))){//EXACT MATCH (submission.toLowerCase().equals(review.toLowerCase()))
					if(reviewState == submState)// && reviewPOS == submPOS)
						match = match + EXACT;
					else if(reviewState != submState)// && reviewPOS == submPOS)
						match = match + NEGEXACT;
					count++;
					continue;//skip all remaining checks
				}		
				
				String[] temp = new String[10];
				//------------
				//Checking for Antonyms
				if(revAnt != null){
					if(revAnt.length > 10){
						System.arraycopy(revAnt, 0, temp, 0, 10);
						listRevArr = Arrays.asList(temp);
					}
					else
						listRevArr = Arrays.asList(revAnt);
					//if(listRevArr != null)
						//System.out.println("listRevArr antonyms:: "+listRevArr.toString());
					if(listRevArr!=null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) ||
							contains(listRevArr.toString(), subToken.toLowerCase()))){
						if(reviewState == submState){// && reviewPOS == submPOS){
							match = match + ANTONYM;
						}
						else if(reviewState != submState){// && reviewPOS == submPOS){
							match = match + SYNONYM;
						}
						 count++;
						 continue;//skip all remaining checks
					}
				}
				//checking if review token appears in the submission's definition
				if(subAnt != null){
					if(subAnt.length > 10){
						System.arraycopy(subAnt, 0, temp, 0, 10);
						listSubArr = Arrays.asList(temp);
					}
					else
						listSubArr = Arrays.asList(subAnt);
					if(listSubArr != null)
						System.out.println("listSubArr antonyms:: "+listSubArr.toString());
					if(listSubArr!=null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) ||
							contains(listSubArr.toString(), revToken.toLowerCase()))){
						if(reviewState == submState){// && reviewPOS == submPOS){
							match = match + ANTONYM;
						}
						else if(reviewState != submState){// && reviewPOS == submPOS){
							match = match + SYNONYM;
						}
						count++;
						continue;//skip all remaining checks
					}
				}
				
				//------------
				//*****For Synonyms
				//checking if any of the review token's synonyms match with the subm. token or its stem form
				if(revSyn!= null){
					if(revSyn.length > 10){
						System.arraycopy(revSyn, 0, temp, 0, 10);
						listRevArr = Arrays.asList(temp);
					}
					else
						listRevArr = Arrays.asList(revSyn);
					//if(listRevArr != null)
						//System.out.println("listRevArr synonyms:: "+listRevArr.toString()+" - Lenght::"+revSyn.length);
					if(listRevArr != null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) || 
							contains(listRevArr.toString(), subToken.toLowerCase()))){					
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				//checking if any of the subm. token's synonyms match with tOVERLAPEXAMhe review token or its stem form
				if(subSyn != null){
					if(subSyn.length > 10){
						System.arraycopy(subSyn, 0, temp, 0, 10);
						listSubArr = Arrays.asList(temp);
					}
					else
						listSubArr = Arrays.asList(subSyn);
					if(listSubArr != null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) || 
							contains(listSubArr.toString(), revToken.toLowerCase()))){
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				
				//------------
				if(revDerivedTerms!= null){
					if(revDerivedTerms.length > 10){
						System.arraycopy(revDerivedTerms, 0, temp, 0, 10);
						listRevArr = Arrays.asList(temp);
					}
					else
						listRevArr = Arrays.asList(revDerivedTerms);
					if(listRevArr != null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) || 
							contains(listRevArr.toString(), subToken.toLowerCase()))){					
						System.out.println("Derived Terms found between: "+revToken +" & "+subToken);
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				//checking if any of the subm. token's synonyms match with tOVERLAPEXAMhe review token or its stem form
				if(subDerivedTerms != null){
					if(subDerivedTerms.length > 10){
						System.arraycopy(subDerivedTerms, 0, temp, 0, 10);
						listSubArr = Arrays.asList(temp);
					}
					else
						listSubArr = Arrays.asList(subDerivedTerms);	
					if(listSubArr != null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) || 
							contains(listSubArr.toString(), revToken.toLowerCase()))){
						System.out.println("Derived terms found between: "+revToken +" & "+subToken);
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				
				//------------
				//looking for synset match
				if(revSynset != null){
					if(revSynset.length > 10){
						System.arraycopy(revSynset, 0, temp, 0, 10);
						listRevArr = Arrays.asList(temp);
					}
					else
						listRevArr = Arrays.asList(revSynset);
					if(listRevArr != null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) || 
							contains(listRevArr.toString(), subToken.toLowerCase()))){					
						System.out.println("Sysnset match found between: "+revToken +" & "+subToken);
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				//checking if any of the subm. token's synonyms match with the review token or its stem form
				if(subSynset != null){
					if(subSynset.length > 10){
						System.arraycopy(subSynset, 0, temp, 0, 10);
						listSubArr = Arrays.asList(temp);
					}
					else
						listSubArr = Arrays.asList(subSynset);
					if(listSubArr != null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) || 
							contains(listSubArr.toString(), revToken.toLowerCase()))){
						System.out.println("Synset match found between: "+revToken +" & "+subToken);
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				
				//------------
				//*****For Nominalizations
				//checking if any of the review token's nominalizations match with the subm. token or its stem form
				if(revNom !=null){
					if(revNom.length > 10){
						System.arraycopy(revNom, 0, temp, 0, 10);
						listRevArr = Arrays.asList(temp);
					}
					else
						listRevArr = Arrays.asList(revNom);
					if(listRevArr != null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) || 
							contains(listRevArr.toString(), subToken.toLowerCase()))){
						System.out.println("Nominalization found: "+revToken +" & "+subToken);
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				//checking if any of the submission token's nominalizations match with the review token or its stem form
				if(subNom !=null){
					if(subNom.length > 10){
						System.arraycopy(subNom, 0, temp, 0, 10);
						listSubArr = Arrays.asList(temp);
					}
					else
						listSubArr = Arrays.asList(subNom);
					if(listSubArr != null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) ||
							contains(listSubArr.toString(), revToken.toLowerCase()))){
						System.out.println("Nominalization found: "+revToken +" & "+subToken);
						if(reviewState == submState)// && reviewPOS == submPOS)
							match = match + SYNONYM;
						else if(reviewState != submState)// && reviewPOS == submPOS)
							match = match + NEGSYNONYM;
						count++;
						continue;//skip all remaining checks
					}
				}
				
				//if none of the above matches were found!
				match = match + NOMATCH;
				//do not increment for NOMATCH - that just decreases the cumulative average!
				count++;
								
			}//end of while loop for submission tokens
		}//end of while loop for review tokens
		
		if(count > 0){
			System.out.println("@@@@@@@@@ Returning Value: "+((double)match/(double)count));
			return (double)match/(double)count;//an average of the matches found
		}
		System.out.println("@@@@@@@@@ Returning Value: "+NOMATCH);
		return (double)NOMATCH;	
	}//end of method


}