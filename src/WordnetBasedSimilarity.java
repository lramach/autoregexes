import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import processing.core.*;
import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.PointerUtils;


import rita.wordnet.*;
import rita.*;

public class WordnetBasedSimilarity{

	//different degree of matching for GRAPH CENTRALITY/PATTERN MATCHING
		public static double NOMATCH = 0; //distinct
		
		public static double OVERLAPEXAM = 1;//overlapping examples
		public static double OVERLAPDEFIN = 1;//overlapping definitions
		public static double COMMONPARENTS = 2;//common parents
		public static double MERONYM = 3; //paraphrasing
		public static double HOLONYM = 3; //paraphrasing
		public static double HYPONYM = 4; //paraphrasing
		public static double HYPERNYM = 4; //paraphrasing
		public static double SYNONYM = 5; //paraphrasing
		public static double EXACT = 6; //exact strings
		
		public static int NUMARRAYELEMENTS;
		
		public static RiWordnet rwordnet;
		
		public WordnetBasedSimilarity(){
	        rwordnet = new RiWordnet();//, "/Users/lakshmi/Documents/Computer - workspaces et al/Add-ons/WordNet-3.0/dict/"
		}
		
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
			
			//System.out.println("Checking the presence of ::"+check);
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
		public double compareStrings(String word1, String word2) throws ClassNotFoundException, StackOverflowError
		{	
			String review = word1;
			String submission = word2;
			
			//System.out.println("@@@@@@@@@ Comparing Vertices:: "+review +" & "+submission);
			double match = 0;//initialise match to 0 
			double count = 0;
			StringTokenizer stokRev, stokSub;
			stokRev = new StringTokenizer(review);

			List listRevArr = Arrays.asList(new String[10]);
			List listSubArr = Arrays.asList(new String[10]);
			
			String reviewPOS = "";
			String submPOS ="";
			
			//checking for exact match between the complete vertex name
			if(word1.toLowerCase().equals(word2.toLowerCase())){
				match = match + EXACT;
				return match;
			}
			
			//iterating through every review token in the vertex phrase
//			while(stokRev.hasMoreTokens()){//traversing review tokens
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
				String[] revHyponyms = null;//hyponyms (more specific -> submission
				String[] revHypernyms = null;//hypernyms (more specific -> submission
				String[] revMer = null;//meronyms of the review (more specific parts of something bigger ->submission)
				String[] revHol = null; //holonyms are the opposit of meronyms ("arm" is a meronym of "body" and "body" is a holonym of "arm")
				String[] revGloss = null;//glosses or definitions of the review token
				String[] revAnt = null;
				String[] revSynset = null;
				String[] revDerivedTerms = null;
				
				try{//some "get" functions return a null and this try-catch block catches those "NullPointerExceptions"
					revSynset = rwordnet.getSynset(revToken.toLowerCase(), reviewPOS);
					revStems = rwordnet.getStems(revToken.toLowerCase(), reviewPOS);				
					revSyn = rwordnet.getSynonyms(revToken.toLowerCase(), reviewPOS);
					revNom = rwordnet.getAllNominalizations(revToken.toLowerCase(), reviewPOS);
					revHyponyms = rwordnet.getAllHyponyms(revToken.toLowerCase(), reviewPOS);
					revHypernyms = rwordnet.getAllHypernyms(revToken.toLowerCase(), reviewPOS);
					revMer = rwordnet.getAllMeronyms(revToken.toLowerCase(), reviewPOS);
					revHol = rwordnet.getAllHolonyms(revToken.toLowerCase(), reviewPOS);
					revAnt = rwordnet.getAllAntonyms(revToken.toLowerCase(), reviewPOS);
					revGloss = rwordnet.getAllGlosses(revToken.toLowerCase(), reviewPOS);
					revDerivedTerms = rwordnet.getAllDerivedTerms(revToken.toLowerCase(), reviewPOS);
				}
				catch(NullPointerException e){
//					e.printStackTrace();
					//System.out.println("Null object returned.");
				}
				catch(StackOverflowError e){
//					e.printStackTrace();
				}
				
				if(revStems != null){
					listRevArr = Arrays.asList(revStems);
				}			

				stokSub = new StringTokenizer(submission);
				//traversing submission's tokens
//				while(stokSub.hasMoreTokens())
//				{
					String subToken = stokSub.nextToken().toLowerCase();
					if(submPOS == "")
						submPOS = determinePOS(word2);
					if(subToken.equals("n't")){
						subToken = "not";
						//System.out.println("replacing n't");
					}
					//"continue" to next token if subToken is a frequent word

					String[] subStems = null;//all stems of the submission token
					String[] subSyn = null;//synonyms of the submission token (one of the strings in the phrase)
					String[] subNom = null;//nominalized forms 
					String[] subHypernyms = null;//hypernyms (more abstract -> review)
					String[] subHyponyms = null;
					String[] subMer = null;//meronyms of the subm token
					String[] subHol = null;//holonyms is the opposite of a meronym
					String[] subAnt = null;
					String[] subGloss = null;//glosses or definitions of the subm token 
					String[] subDerivedTerms = null;
					String[] subSynset = null;
					
					try{					
						subStems = rwordnet.getStems(subToken.toLowerCase(), submPOS);
						subSyn = rwordnet.getSynonyms(subToken.toLowerCase(), submPOS);
						subNom = rwordnet.getNominalizations(subToken.toLowerCase(), submPOS);
						subHypernyms = rwordnet.getAllHypernyms(subToken.toLowerCase(), submPOS);
						subHyponyms = rwordnet.getAllHyponyms(subToken.toLowerCase(), submPOS);
						subMer = rwordnet.getAllMeronyms(subToken.toLowerCase(), submPOS);
						subHol = rwordnet.getAllHolonyms(subToken.toLowerCase(), submPOS);
						subAnt = rwordnet.getAllAntonyms(subToken.toLowerCase(), submPOS);
						subGloss = rwordnet.getAllGlosses(subToken.toLowerCase(), submPOS);
						subDerivedTerms = rwordnet.getAllDerivedTerms(subToken.toLowerCase(), submPOS);
						subSynset = rwordnet.getSynset(subToken.toLowerCase(), submPOS);
					}
					catch(NullPointerException e){
//						e.printStackTrace();
						//System.out.println("Null object returned.");
					}
					catch(StackOverflowError e){
//						e.printStackTrace();
					}
					
					if(subStems != null){
						//System.out.println("subStem:: "+subStems[0]);
						listSubArr = Arrays.asList(subStems);
					}
					
					//checks are ordered from BEST to LEAST degree of semantic relatedness
					//*****exact matches				
					if(subToken.toLowerCase().equals(revToken.toLowerCase()) || 
							(subStems != null && revStems != null && 
							subStems[0].toLowerCase().equals(revStems[0].toLowerCase()))){//EXACT MATCH (submission.toLowerCase().equals(review.toLowerCase()))
							match = match + EXACT;
						count++;
					}		
					
					String[] temp = new String[10];
					//------------
//					//Checking for Antonyms
//					if(revAnt != null){
//						if(revAnt.length > 10){
//							System.arraycopy(revAnt, 0, temp, 0, 10);
//							listRevArr = Arrays.asList(temp);
//						}
//						else
//							listRevArr = Arrays.asList(revAnt);
//						if(listRevArr!=null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) ||
//								contains(listRevArr.toString(), subToken.toLowerCase()))){
//									match = match + ANTONYM;
//							 count++;
//						}
//					}
//					//checking if review token appears in the submission's definition
//					if(subAnt != null){
//						if(subAnt.length > 10){
//							System.arraycopy(subAnt, 0, temp, 0, 10);
//							listSubArr = Arrays.asList(temp);
//						}
//						else
//							listSubArr = Arrays.asList(subAnt);
//						if(listSubArr!=null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) ||
//								contains(listSubArr.toString(), revToken.toLowerCase()))){
//									match = match + ANTONYM;
//							count++;
//						}
//					}
					
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
						if(listRevArr != null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) || 
								contains(listRevArr.toString(), subToken.toLowerCase()))){					
									match = match + SYNONYM;
							count++;
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
//							System.out.println("Synonym found between: "+revToken +" & "+subToken);
									match = match + SYNONYM;
							count++;
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
						//if(listRevArr != null)
							//System.out.println("listRevArr derived terms:: "+listRevArr);
						if(listRevArr != null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) || 
								contains(listRevArr.toString(), subToken.toLowerCase()))){					
									match = match + SYNONYM;
							count++;
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
									match = match + SYNONYM;
							count++;
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
									match = match + SYNONYM;
							count++;
						}
					}
					//checking if any of the subm. token's synonyms match with tOVERLAPEXAMhe review token or its stem form
					if(subSynset != null){
						if(subSynset.length > 10){
							System.arraycopy(subSynset, 0, temp, 0, 10);
							listSubArr = Arrays.asList(temp);
						}
						else
							listSubArr = Arrays.asList(subSynset);
						if(listSubArr != null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) || 
								contains(listSubArr.toString(), revToken.toLowerCase()))){
									match = match + SYNONYM;
							count++;
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
						//if(listRevArr != null)
							//System.out.println("listRevArr nominalizations:: "+listRevArr.toString());
						if(listRevArr != null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) || 
								contains(listRevArr.toString(), subToken.toLowerCase()))){
//							System.out.println("Nominalization found: "+revToken +" & "+subToken);
//							if(reviewState != SentenceState.DEFAULTSTATE)
									match = match + SYNONYM;
							count++;
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
									match = match + SYNONYM;
							count++;
						}
					}
					
					//------------
					//******For Hyponyms or Hypernyms
					//Reviews contain a more high-level description of the content in subm.
					//Do the opp. take the hyponym of review tokens
					//checking if any of the review token's hyponyms match with the subm. token or its stem form
					if(revHyponyms != null){
						if(revHyponyms.length > 10){
							System.arraycopy(revHyponyms, 0, temp, 0, 10);
							listRevArr = Arrays.asList(temp);
						}
						else
							listRevArr = Arrays.asList(revHyponyms);
						//if(listRevArr != null)
							//System.out.println("listRevArr hyponym:: "+listRevArr.toString());
						if(listRevArr!=null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) ||
								contains(listRevArr.toString(), subToken.toLowerCase()))){
//							System.out.println("Hyponym found: "+revToken +" and "+subToken);
//							if(reviewState != SentenceState.DEFAULTSTATE)
									match = match + HYPONYM;
							 count++;
						}
					}
					
					//remove this for text paraphrasing
					if(subHyponyms != null){
						if(subHyponyms.length > 10){
							System.arraycopy(subHyponyms, 0, temp, 0, 10);
							listSubArr = Arrays.asList(temp);
						}
						else
							listSubArr = Arrays.asList(subHyponyms);
						//if(listSubArr != null)
							//System.out.println("listSubArr hyponym:: "+listSubArr.toString());
						if(listSubArr!=null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) ||
								contains(listSubArr.toString(), revToken.toLowerCase()))){
//							System.out.println("Hyponym found: "+revToken +" and "+subToken);
//							if(reviewState != SentenceState.DEFAULTSTATE)
									match = match + HYPONYM;
							 count++;
						}
					}
					
					//------------
					//Reviews contain a more high-level description of the content in subm.
					//do the opp. take the hypernym of submission tokens
					//checking if any of the submission token's hypernyms match with the subm. token or its stem form
					if(subHypernyms != null){
						if(subHypernyms.length > 10){
							System.arraycopy(subHypernyms, 0, temp, 0, 10);
							listSubArr = Arrays.asList(temp);
						}
						else
							listSubArr = Arrays.asList(subHypernyms);
						//if(listSubArr != null)
							//System.out.println("listSubArr hypernyms:: "+listSubArr.toString());
						if(listSubArr!=null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) || 
								contains(listSubArr.toString(), revToken.toLowerCase()))){
//							System.out.println("hypernyms found between "+revToken +" and "+subToken);
//							if(reviewState != SentenceState.DEFAULTSTATE)
									match = match + HYPERNYM;
							 count++;
						}
					}
					
					//remove this for text paraphrasing
					if(revHypernyms != null){
						if(revHypernyms.length > 10){
							System.arraycopy(revHypernyms, 0, temp, 0, 10);
							listRevArr = Arrays.asList(temp);
						}
						else
							listRevArr = Arrays.asList(revHypernyms);
						//if(listRevArr != null)
							//System.out.println("listRevArr hypernyms:: "+listRevArr.toString());
						if(listRevArr!=null && ((subStems != null && contains(listRevArr.toString(), subStems[0].toLowerCase())) ||
								contains(listRevArr.toString(), subToken.toLowerCase()))){
//							System.out.println("hypernyms found: "+revToken +" and "+subToken);
//							if(reviewState != SentenceState.DEFAULTSTATE)
									match = match + HYPERNYM;
							 count++;
						}
					}
					
					//------------
					//********Meronyms
					//checks if submission contains meronyms of words in the review -> compare submission with meronym of review token
					if(revMer != null){
						if(revMer.length > 10){
							System.arraycopy(revMer, 0, temp, 0, 10);
							listRevArr = Arrays.asList(temp);
						}
						else
							listRevArr = Arrays.asList(revMer);
						if(listRevArr!=null && ((subStems != null &&  contains(listRevArr.toString(), subStems[0].toLowerCase())) ||
								contains(listRevArr.toString(), subToken.toLowerCase()))){
									match = match + MERONYM;
							 count++;
						}
					}
					//check the other way around - if review is a meronym of the submission token
					if(subMer != null){
						if(subMer.length > 10){
							System.arraycopy(subMer, 0, temp, 0, 10);
							listSubArr = Arrays.asList(temp);
						}
						else
							listSubArr = Arrays.asList(subMer);
						if(listSubArr!=null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) || 
								contains(listSubArr.toString(), revToken.toLowerCase()))){
									match = match + MERONYM;
							 count++;
						}
					}
					
					//------------
					//********Holonyms
					if(revHol != null){
						if(revHol.length > 10){
							System.arraycopy(revHol, 0, temp, 0, 10);
							listRevArr = Arrays.asList(temp);
						}
						else
							listRevArr = Arrays.asList(revHol);
						//if(listRevArr != null)
							//System.out.println("listRevArr holonyms:: "+listRevArr.toString());
						if(listRevArr!=null && ((subStems != null &&  contains(listRevArr.toString(), subStems[0].toLowerCase())) ||
								contains(listRevArr.toString(), subToken.toLowerCase()))){
//							System.out.println("Holonym found: "+revToken +" and "+subToken);
//							if(reviewState != SentenceState.DEFAULTSTATE)
									match = match + HOLONYM;
							 count++;
						}
					}
					//check the other way around - if review is a meronym of the submission token
					if(subHol != null){
						if(subHol.length > 10){
							System.arraycopy(subHol, 0, temp, 0, 10);
							listSubArr = Arrays.asList(temp);
						}
						else
							listSubArr = Arrays.asList(subHol);
						if(listSubArr!=null && ((revStems != null && contains(listSubArr.toString(), revStems[0].toLowerCase())) || 
								contains(listSubArr.toString(), revToken.toLowerCase()))){
									match = match + HOLONYM;
							 count++;
						}
					}
					
					//------------
					//looking for common parents across the word tokens if any
					String[] commonParents = null;
					float distanceBetweenTokens = 0;
					try{
						//if(rwordnet.getBestPos(subToken) != null){
							commonParents = rwordnet.getCommonParents(revToken, subToken, submPOS);
							distanceBetweenTokens = rwordnet.getDistance(revToken, subToken, submPOS);
						//}
						if(commonParents == null){
							commonParents = rwordnet.getCommonParents(revToken, subToken, reviewPOS);
							distanceBetweenTokens = rwordnet.getDistance(revToken, subToken, reviewPOS);
						}
					}
					catch (IndexOutOfBoundsException e){
//						System.out.println("**Common parents:: null");
						commonParents = null;
					}
					catch(StackOverflowError e){
						//e.printStackTrace();
					}
					if(commonParents != null){
						listSubArr = Arrays.asList(commonParents);
						//if(listSubArr != null)
							//System.out.println("common parents:: "+listSubArr.toString());
						if(listSubArr != null && !listSubArr.toString().contains("entity") && !listSubArr.toString().contains("object") 
								&& !listSubArr.toString().contains("organism") && !listSubArr.toString().contains("being")
								&& !listSubArr.toString().contains("abstraction") && !listSubArr.toString().contains("whole")
								&& !listSubArr.toString().contains("unit")){//entity is a generic parent
//							System.out.println("**Common parents:: "+listSubArr +" Shortest distance:: "+distanceBetweenTokens);
							float similarity = 1 - distanceBetweenTokens;
							//calculate similarity [0,1][min,max] in range of [0,6][a,b]
							//((b-a)(x-min)/(max-min)) + a
							double scaledSimilarity = ((double)6 * (double)similarity);  
//							System.out.println("**Scaled sim:: "+scaledSimilarity);
							if((int) scaledSimilarity > 0){
//								if(reviewState != SentenceState.DEFAULTSTATE)
										match = match + COMMONPARENTS;
								 count++;
							}
						}
					}
					
					//------------
					//*****OVERLAPPING DEFINITIONS
					//overlap (definition, definition)
					if(revGloss != null && subGloss != null){
						String[] tempRev = extractDefinitionss(revGloss);
						String[] tempSub = extractDefinitionss(subGloss);
						if(overlap(tempRev, tempSub) > 0){
//							if(reviewState != SentenceState.DEFAULTSTATE)
									match = match + OVERLAPDEFIN;
							count++;
						}
					}
					
					//if none of the above matches were found!
					//System.out.println("**No match:: ");
					match = match + NOMATCH;
					//do not increment for NOMATCH - that just decreases the cumulative average!
					count++;
									
//				}//end of while loop for submission tokens
//			}//end of while loop for review tokens
			
			if(count > 0){
//				System.out.println("Match: "+match +" Count:: "+count);
				//System.out.println("@@@@@@@@@ Returning Value: "+((double)match/(double)count));
				return (double)Math.round((double)match/(double)count);//an average of the matches found
			}
			//System.out.println("@@@@@@@@@ Returning NOMATCH");
			return (double)NOMATCH;	
		}//end of method

		/* calculates the number of words in common between the two strings
		 * comparing every pair of definitions or examples (and only the non-stopwords)
		 */
		public int overlap(String[] def1, String[] def2){
			int numOverlap = 0;
			//only overlaps across the ALL definitions
			for(int i = 0; i< def1.length; i++){
				if(def1[i] != null){//def1[i]
					def1[i] = def1[i].replaceAll("\"", " ");
					//System.out.println("***def1/ex1: "+def1[i]);
					if(def1[i].contains(";")){
						//System.out.println("***def1:: "+def1[i].substring(0, def1[i].indexOf(";")));
						def1[i] = def1[i].substring(0, def1[i].indexOf(";"));
					}			
					for(int j = 0; j < def2.length; j++){				
						if(def2[j] != null){//def2[j]
							//System.out.println("***def2/ex1: "+def2[j]);
							//def2[j] = def2[j].replaceAll("\"", " ");
							if(def2[j].contains(";")){
								//System.out.println("***def2:: "+def2[j].substring(0, def2[j].indexOf(";")));
								def2[j] = def2[j].substring(0, def2[j].indexOf(";"));
							}
						
							StringTokenizer s1 = new StringTokenizer(def1[i]);	
							while(s1.hasMoreTokens()){
								String str1 = s1.nextToken().toLowerCase();
							
								StringTokenizer s2 = new StringTokenizer(def2[j]);
								while(s2.hasMoreElements()){
									String str2 = s2.nextToken().toLowerCase();
									if(str1.equals(str2) && !isStopWordorFrequentWord(str1)){
										if(!isStopWordorFrequentWord(str1)){
//											System.out.println("**Overlap def/ex:: "+str1);
											numOverlap++;
										}
									}
								}
							}
						}//end of def2[j] being null
					}//end of for loop for def2 - j
				}//end of def1[i] being null
			}//end of for loop for def1 - i
			
			return numOverlap;
		}
		
		public String[] extractExamples(String[] glosses){
			//System.out.println("Inside extract examples::"+Arrays.asList(glosses));
			String[] examples = new String[glosses.length * 6];
			int ex_count = 0;
			
			int begin = 0, end = 0;
			//extracting examples from definitions
			for(int i = 0; i< glosses.length; i++){
				//System.out.println("inside for loop:: ");
				if(glosses[i] != null){
					String temp = glosses[i];
					if(temp.contains(";")){
						while(temp.contains(";"))//the text contains more than 1 sentence
					    {
					    	if(temp.charAt(begin) != ';'){
					    		begin = temp.indexOf(";");
					    	}
					    	else{//begin holds index of the first semicolon
					    		end = temp.indexOf(";");
					    		if(end > begin){//if begin and end are different
					    			examples[ex_count] = temp.substring(begin + 1, end - 1);//excluding semicolons
						    		//System.out.println("Example def1:: "+def1_examples[def1_count]);
						    		ex_count++;
						    		temp = temp.substring(end+1);
						    		begin = 0;
					    		}
					    		else if(temp.substring(begin+1).contains(";")){//if end == begin, look for another semicolon
					    			end = (temp.substring(begin+1)).indexOf(";");
					    			examples[ex_count] = (temp.substring(begin+1)).substring(0, end - 1);//excluding semicolons
						    		//System.out.println("Example def1:: "+def1_examples[def1_count]);
						    		ex_count++;
						    		temp = (temp.substring(begin+1)).substring(end+1);
						    		begin = 0;
					    		}
					    		else{//if begin was the only semicolon
					    			examples[ex_count] = temp.substring(begin+1);
									//System.out.println("Example def1:: "+def1_examples[def1_count]);
						    		ex_count++;
						    		temp = temp.substring(begin+1);
						    		begin = 0;
					    		}
					    	}
						}
					}
				}
			}	
			return examples;
		}
		
		public String[] extractDefinitionss(String[] glosses){
			//System.out.println("Inside extract definitions::"+Arrays.asList(glosses));
			String[] definitions = new String[glosses.length * 4];
			int def_count = 0;
			
			//extracting examples from definitions
			for(int i = 0; i< glosses.length; i++){
				//System.out.println("inside for loop:: ");
				if(glosses[i] != null){
					String temp = glosses[i];
					if(temp.contains(";")){//since only the first string in the array position is a definition, remaining are examples
						definitions[def_count] = temp.substring(0, temp.indexOf(";"));
					}
					else{
						definitions[def_count] = temp;
					}
					def_count++;
				}
			}	
			return definitions;
		}
		
		
		public static boolean isStopWordorFrequentWord(String word){
//			System.out.println("Inside isStopWordorFrequentWord: "+word);
			//constants con = new constants();
			if(word.contains("("))
				word = word.replace("(", "");
			if(word.contains(")"))
				word = word.replace(")", "");
			if(word.contains("["))
				word = word.replace("[", "");
			if(word.contains("]"))
				word = word.replace("]", "");
		    //checking for closed class words
			for (int i = 0; i < Stopwords.CLOSED_CLASS_WORDS.length; i++){
		      if ((word.replaceAll("\"", "")).equalsIgnoreCase(Stopwords.CLOSED_CLASS_WORDS[i])){
//		    	  System.out.println("return true");
		        return true;
		      }
			}
		    //checking for stopwords
		    for (int i = 0; i < Stopwords.STOP_WORDS.length; i++)
			      if ((word.replaceAll("\"", "")).equalsIgnoreCase(Stopwords.STOP_WORDS[i])){
//			    	  System.out.println("return true");
			        return true;
			      }
		    for (int i = 0; i < Stopwords.FREQUENT_WORDS.length; i++)
			      if (word.equalsIgnoreCase(Stopwords.FREQUENT_WORDS[i])){
//			    	System.out.println("return true");
			        return true;
			      }
		    return false;    
		  }
}