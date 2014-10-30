
import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.lemmatizer.Options;
import is2.tag3.*;
import is2.tools.Tool;
import is2.mtag.*;
import is2.parser.Parser;

import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.io.IOException;

public class GraphGenerator
{
	//other constants
	public static int WORDS = 10000;
	public static int NOUNS = 10000;
	static int ALPHA_FREQ = 1;//alpha - the frequency threshold (lower)
	static int BETA_FREQ = 10;//alpha - the frequency threshold (upper)
	//the combined set of vertices and edges from the review and submissions
	public static Vertex[] vertices;
	public static Edge[] edges;
	//****************************************************
	public static int numEdges = 0, numVertices;
	public static int numDoubleEdges = 0;
	static double reviewStrLen = 0;
	//static RiWordnet rwordnet = null;
	//defining more constants
	public static int NOUN = 1;
	public static int VERB = 2;
	public static int ADJ = 3;
	public static int ADV = 4;
	
	public static int POSITIVE = 0;
	public static int SUGGESTIVE = 1;
	public static int NEGATED = 2;

	public static long timeTaken = 0;
	
	//counts of the number of different types of phrases
	public static int nounPhrases = 0;
	public static int prepPhrases = 0;
	public static int verbPhrases = 0;
	public static int progressVerbTenses = 0;
	public static int modalAuxPhrases = 0;
	public static int adjectiveCount = 0;
	public static int adverbialCount = 0;
	 
	/*
	 * generates the graph for the given review text and 
	 * INPUT: an array of sentences for a review or a submission. Every row in 'text' contains one sentence.
	 * type - tells you if it was a review or s submission
	 * type = 1 - submission/past review
	 * type = 2 - new review
	 */
	public int generateGraph(String[] text, MaxentTagger posTagger, int trainTestFlag, Parser parser) throws IOException
	{		
		long begin = System.currentTimeMillis();
		System.out.println("Inside generateGraph");
		Vertex[] vertices = new Vertex[WORDS];
		Edge[] edges = new Edge[WORDS/2];
		numVertices = 0;//when starting a new sentence			
		numEdges = 0;//when starting a new sentence
		numDoubleEdges = 0;
		nounPhrases = 0;
		prepPhrases = 0;
		verbPhrases = 0;
		progressVerbTenses = 0;
		modalAuxPhrases = 0;
		adjectiveCount = 0;
		adverbialCount = 0;
		
		int STATE;
		int cou = 0;
		//every row in the 2D matrix 'text' corresponds to a sentence in the training review
		for(int i = 0; i < text.length && text[i] != null; i++)
		{	
			System.out.println("text[i]:"+text[i]);
			if(text[i].isEmpty() || text[i].trim().equals("")) continue;//skip the text[i] if it is empty
			
			text[i] = replaceApostrophes(text[i]); //this method replaces occurences of 's with " is" and "n't" with "not", and 'd with could/would or should
			////////////////////
			//new code to identify labels and parents			
            // Create a data container for a sentence
            SentenceData09 sent = new SentenceData09();
            
            String tags = posTagger.tagString(text[i]);//taggedStr[i];
            StringTokenizer st = new StringTokenizer(tags); //new StringTokenizer(text[i]);//args[0]
            ArrayList<String> forms = new ArrayList<String>();            
            forms.add("<root>");
            while(st.hasMoreTokens()){
            	String temp = st.nextToken();
            	forms.add(temp.substring(0,temp.lastIndexOf("/")));            
            }
            sent.init(forms.toArray(new String[0]));
            //System.out.println("Sent::"+sent.toString());
            
            //reinitialize since st would have run out by then
            st = new StringTokenizer(tags);//args[0]
            //System.out.println("tags::"+tags);
            
            forms = new ArrayList<String>();            
            forms.add("<root-POS>");
            while(st.hasMoreTokens()){
            	String str = st.nextToken();
            	forms.add(str.substring(str.indexOf("/")+1));            
            }
            
            sent.setPPos(forms.toArray(new String[0]));
            if(sent == null)
            	System.out.println("Sent is null");
            
            // parse the sentence
            SentenceData09 out;
            try{//skip the problem text
            	out = parser.parse(sent);
            }
            catch(OutOfMemoryError e){
            	continue;
            }
            // output the sentence and dependency tree
//            System.out.println(out.toString());
            
            //the semantic role labels
            String[] labels;
            labels = out.getLabels();
            
            //tokens' parents
			String[] parents = new String[out.forms.length];
//            System.out.println("Forms length:"+out.forms.length);
            int[] parentIDs = out.getParents();
            for(int j = 0; j < parentIDs.length; j++){
            	//System.out.println(parentIDs[j]);
            	if(parentIDs[j] != 0)
            		parents[j] = out.forms[parentIDs[j]-1];
            	else
            		parents[j] = null;
            }

            //for(int j = 0; j < parents.length; j++)
            	//System.out.println("Parent:: "+parents[j]);
            //Getting the string that is to be tokenized! - only then would the POS-ed tokens, labels and parents be referring to the same strings!
            //tagging them individually would cause the tokens to get wrong POS tags
            String tempTagString = posTagger.tagString(text[i]);
            st = new StringTokenizer(tempTagString);
            System.out.println("tempTagString: "+ tempTagString);
            String[] taggedString = new String[st.countTokens()];
            int g = 0;
            while(st.hasMoreTokens()){
            	taggedString[g] = st.nextToken();
            	//System.out.println("taggedString[j]"+ taggedString[g]);
            	g++;
            }
            
            String toTokenize = "";
            String strWithPosTags = "";
            for(int j = 0; j < out.forms.length;){
            	toTokenize = toTokenize +" "+ out.forms[j];
            	//System.out.println("out.forms[j]"+out.forms[j]);
//            	System.out.println("taggedString[j]"+ taggedString[j]);
            	int h = j;
            	while(h < taggedString.length && !taggedString[h].toLowerCase().contains(out.forms[j].toLowerCase())){//finding the taggedString position where the token is found
            		h++;
            	}
            	//when a matching taggedString is found
            	if(h < taggedString.length && taggedString[h].toLowerCase().contains(out.forms[j].toLowerCase())){
            		//System.out.println("taggedString found");
            		strWithPosTags = strWithPosTags +" "+ taggedString[h];
            		j++;
            	}
            	//if no matching tagged string was found
            	else{
//            		System.out.println(new StringTokenizer(posTagger.tagString(out.forms[j])).nextToken());
            		strWithPosTags = strWithPosTags +" "+ new StringTokenizer(posTagger.tagString(out.forms[j])).nextToken();
            		j++;
            	}
            	//System.out.println(new StringTokenizer(posTagger.tagString(out.forms[j])).nextToken());//e.g.: "incidence--1" was broken up by tagger as "incidence/NN --/: 1/CD" - too many strings! 
            	//strWithPosTags = strWithPosTags +" "+new StringTokenizer(posTagger.tagString(out.forms[j])).nextToken();
            }
            ////////////////////
            
//            System.out.println("&&&&&&&&toTokenize:: "+toTokenize);
//            System.out.println("&&&&&&&&strWithPosTags:: "+strWithPosTags);
            
			//System.out.println(text[i]);
			//String strWithPosTags = posTagger.tagString()
			//String strWithPosTags = posTagger.tagSentence()
			//System.out.println(text[i]);
			//System.out.println(strWithPosTags);
			st = new StringTokenizer(strWithPosTags);
			
			String[] nouns = new String[st.countTokens()];//maximum number of nouns could be "st.countTokens()" 
			int nCount = 0;
			String[] verbs = new String[st.countTokens()];
			int vCount = 0;
			String[] adjectives = new String[st.countTokens()];
			int adjCount = 0;
			String[] adverbs = new String[st.countTokens()];			
			int advCount = 0;			
			int prevType = -1;//holds the POS of the previous word in the text
			
			//determining length of the review from across the different sentences that it contains
			reviewStrLen = reviewStrLen + st.countTokens();
			int fAppendedVertex = 0;//
            
			//SETTING UP STATE INFORMATION FOR EACH OF THE REVIEW SEGMENTS
			//Checking to see if the sentence contains any negations or double negations (positive) etc.
			SentenceState sstate = new SentenceState();
			int[] states_array = sstate.identifySentenceState(strWithPosTags);
			int states_counter = 0;
			STATE = states_array[states_counter];//SETTING THE DEFAULT STATE
			states_counter++;
//			System.out.println("^^^^FIRST STATE:: "+STATE+"length of states array:"+states_array.length);
			
			//to keep track of the parent and labels
			int labelCounter = 0;
			int ParentCounter = 0;
			while(st.hasMoreTokens())
			{
				String ps = st.nextToken();
				String s = ps.substring(0, ps.indexOf("/"));
				String POSTag = ps.substring(ps.indexOf("/")+1);
				
//				System.out.println("**Value::"+ps+"LabelCounter:: "+labelCounter+"ParentCounter:: "+ParentCounter+" POSTag::"+POSTag);
				//System.out.println("**ps.indexOf(/)::"+ps.indexOf("/")+"ps.length()-1:: "+(ps.length()-2));
				if(ps.contains("/POS") || (ps.indexOf("/") == ps.length()-1) || (ps.indexOf("/") == ps.length()-2))//this is for strings containinig "'s" or without POS
					continue;
				
				//SETTING STATE
				//since the CC or IN are part of the following sentence segment, we set the STATE for that segment when we see a CC or IN
				if(ps.contains("/CC")){//|| ps.contains("/IN")
					STATE = states_array[states_counter];
					states_counter++;
					//System.out.println("^^^^NEW STATE:: "+STATE);
				}
				
				
				//if the string is a noun, personal pronoun, determiner, (preposition or subordinating conjunction), existential, Wh-pronoun
				if(ps.contains("/NN") || ps.contains("/PRP") || (ps.contains("/IN") && s.length() > 2) || ps.contains("/EX") || ps.contains("/WP"))
				{//&& !ps.contains("/NNP")) (&& s.length() > 2 for PRP) 
//					System.out.println("Noun:: "+s);					
					Vertex nounVertex = null;
					if(prevType == NOUN){//if prevtype is noun, combine the nouns
						nounPhrases++;
						nCount = nCount - 1;
						Vertex prevVertex = searchVertices(vertices, nouns[nCount], i, 0);//fetching the previous vertex
						if(prevVertex.POSTag.contains("IN") || ps.contains("IN"))
							prepPhrases++;
						nouns[nCount] = nouns[nCount] + " " + s;
						//checking if the previous noun concatenated with "s" already exists among the vertices
						if((nounVertex = getVertex(vertices, nouns[nCount], i)) == null){//getVertex(nouns[nCount], vertices)) == null){
							prevVertex.name = prevVertex.name + " " + s;//concatenating the nouns
							nounVertex = prevVertex;//the current concatenated vertex will be considered
							//if(nounVertex.state != STATE)//reset the state
								//nounVertex.state = STATE;
							//if the existing vertex's label is empty
							if(nounVertex.label =="")
								nounVertex.label = labels[labelCounter];
							//if parent is null
							if(nounVertex.parent == "" || nounVertex.parent == null || nounVertex.parent.equalsIgnoreCase(s))
								nounVertex.parent = parents[ParentCounter];
								
							//if the label is not empty and if "labels" contains non-generic labels, then set it to the adverb
							if(labels[labelCounter] != "NMOD" || labels[labelCounter] != "PMOD")//resetting labels for the concatenated vertex
								nounVertex.label = labels[labelCounter];
							fAppendedVertex = 1;
							//no incrementing the number of vertices for appended vertices
						}//if the vertex already exists, just use nounVertex - the returned vertex for ops.					
					}
					else{
						//System.out.println("Noun here 2:: "+s);
						nouns[nCount] = s;//this is checked for later on
						nounVertex = searchVertices(vertices, s, i, 1);
						if(nounVertex == null){//the string doesn't already exist
							vertices[numVertices] = new Vertex(nouns[nCount], NOUN, i, STATE, labels[labelCounter], parents[ParentCounter], POSTag);
							nounVertex = vertices[numVertices];//the newly formed vertex will be considered
							numVertices++;
						}
					}
					
					//if an adjective was found earlier, we add a new edge
					if(prevType == ADJ){
						Vertex v1, v2;
						int e;
						//set previous noun's property to null, if it was set, if there is a noun following the adjective
						if(nCount >= 0){//delete the edge created and add a new edge
							if(nCount == 0)
								v1 = searchVertices(vertices, nouns[nCount], i, 0);//fetching the previous noun
							else
								v1 = searchVertices(vertices, nouns[nCount-1], i, 0);//fetching the previous noun
							
							v2 = searchVertices(vertices, adjectives[adjCount-1], i, 0);//fetching the previous adjective							
							//if such an edge exists - DELETE IT
							if(v1 != null && v2 != null && (e = searchEdgesToSetNull(edges, v1, v2, i)) != -1){//-1 is when no such edge exists
								edges[e] = null;//setting the edge to null
								//numEdges--;//deducting counter messes shit up!
							}							
						}
						//if this noun vertex was encountered for the first time, nCount < 1,
						// so do adding of edge outside the if condition						
						//add a new edge with v1 as the adjective and v2 as the new noun
						v1 = searchVertices(vertices, adjectives[adjCount-1], i, 0);
						v2 = nounVertex;
						//if such an edge did not already exist
						if(v1 != null && v2 != null && (e = searchEdges(edges, v1, v2, i)) == -1){
//							System.out.println("$$$ adding edge:: "+v1.name+" - "+v2.name);
							edges[numEdges] = new Edge("noun-property",NOUN);
							edges[numEdges].inVertex = v1;//for nCount = 0;
							edges[numEdges].outVertex = v2;// the verb
							edges[numEdges].index = i;
							numEdges++;	
						}
					}
						
					//a noun has been found and has established a verb as an invertex and such an edge doesnt already previously exist
					if(vCount >= 1 && fAppendedVertex == 0) 
					//add edge only when a fresh vertex is created not when existing vertex is appended to
					{
						//System.out.println("here2 verb name "+verbs[vCount-1] +" i"+i);
						Vertex v1 = searchVertices(vertices, verbs[vCount-1], i, 0);
						Vertex v2 = nounVertex;
						//System.out.println("check edge "+v1.name +" - "+v2.name);
						//if such an edge does not already exist add it
						int e;
						if(v1 != null && v2 != null && (e = searchEdges(edges,v1, v2, i)) == -1){
//							System.out.println("$$$ adding edge:: "+v1.name+" - "+v2.name);
							edges[numEdges] = new Edge("verb", VERB);							
							edges[numEdges].inVertex = v1;//for vCount = 0
							edges[numEdges].outVertex = v2;
							edges[numEdges].index = i;
							//vCount = vCount - 1;//so that this verb is not associated with any other noun!
							numEdges++;
						}
					}
					fAppendedVertex = 0;//resetting the appended vertex flag
					prevType = NOUN;
					nCount++;
					//numVertices++;
					
				}//end of if condition for noun

//------------------------------------------------------------------------------------------------ 
				
				//if the string is an adjective
				//adjectives are vertices but they are not connected by an edge to the nouns, instead they are the noun's properties
				else if(ps.contains("/JJ")){
					Vertex adjective = null;
					adjectiveCount++;
//					System.out.println("Adjective::"+ps+" count::"+adjectiveCount);
					if(prevType == ADJ){//combine the adjectives
						if(adjCount >= 1)
							adjCount = adjCount - 1;
						Vertex prevVertex = searchVertices(vertices, adjectives[adjCount], i, 0);//fetching the previous vertex
						adjectives[adjCount] = adjectives[adjCount] + " " + s;							
						//if the concatenated vertex didn't already exist
						if((adjective = getVertex(vertices, adjectives[adjCount], i)) == null){
							prevVertex.name = prevVertex.name +" " + s;
							adjective = prevVertex;//set it as "adjective" for further execution
							//if(adjective.state != STATE)//reset the state
								//adjective.state = STATE;
							//if the existing vertex's label is empty
							if(adjective.label =="")
								adjective.label = labels[labelCounter];
							//if parent is null
							if(adjective.parent == "" || adjective.parent == null || adjective.parent.equalsIgnoreCase(s))
								adjective.parent = parents[ParentCounter];
							//if the label is not empty and if "labels" contains non-generic labels, then set it to the adjective
							if(labels[labelCounter] != "NMOD" || labels[labelCounter] != "PMOD")//resetting labels for the concatenated vertex
								adjective.label = labels[labelCounter];
						}
					}
					else{//new adjective vertex
						adjectives[adjCount] = s;
						if((adjective = getVertex(vertices, s, i)) == null){//the string doesn't already exist
							vertices[numVertices] = new Vertex(adjectives[adjCount], ADJ, i, STATE, labels[labelCounter], parents[ParentCounter], POSTag);
							adjective = vertices[numVertices];
							numVertices++;
						}
					}
					
					//by default associate the adjective with the previous/latest noun and if there is a noun following it immediately, then remove the property from the older noun (done under noun condition)
					if(nCount >= 0){
						//gets the previous noun to form the edge
						Vertex v1;
						if(nCount == 0)
							v1 = searchVertices(vertices, nouns[nCount], i, 0);
						else
							v1 = searchVertices(vertices, nouns[nCount-1], i, 0);
						
						Vertex v2 = adjective;
						int e;
						//if such an edge does not already exist add it
						if(v1 != null && v2 != null && (e = searchEdges(edges, v1, v2, i)) == -1){
//							System.out.println("$$$ adding edge:: "+v1.name+" - "+v2.name);
							edges[numEdges] = new Edge("noun-property",NOUN);
							edges[numEdges].inVertex = v1;//for nCount = 0;
							edges[numEdges].outVertex = v2;// the verb
							edges[numEdges].index = i;
							numEdges++;							
						}		
					}
					prevType = ADJ;
					adjCount++;
					//numVertices++;
				}//end of if condition for adjective

//--------------------------------------------------------------------------------------------------------
				
				//if the string is a verb or a modal//length condition for verbs is, be, are...
				else if(ps.contains("/VB") || ps.contains("MD")){//&& s.length() >= 3)
//					System.out.println("***VB "+s +" index:: "+i);
					Vertex verbVertex = null;//searchVertices(vertices[i], s);
					if(ps.contains("VBG"))
						progressVerbTenses++;
					if(prevType == VERB){//combine the verbs	
						verbPhrases++;
						vCount = vCount - 1;
						Vertex prevVertex = searchVertices(vertices, verbs[vCount], i, 0);//fetching the previous vertex
						if(prevVertex.POSTag.contains("MD") || ps.contains("MD"))
							modalAuxPhrases++;
						verbs[vCount] = verbs[vCount] + " " + s;						
						//if the concatenated vertex didn't already exist
						if((verbVertex = getVertex(vertices, verbs[vCount], i)) == null){
							prevVertex.name = prevVertex.name + " " + s;
							verbVertex = prevVertex;//concatenated vertex becomes the new verb vertex
							//if(verbVertex.state != STATE)//reset the state
								//verbVertex.state = STATE;
							//if the existing vertex's label is empty
							if(verbVertex.label =="")
								verbVertex.label = labels[labelCounter];
							//if parent is null
							if(verbVertex.parent == "" || verbVertex.parent == null || verbVertex.parent.equalsIgnoreCase(s))
								verbVertex.parent = parents[ParentCounter];
							//if the label is not empty and if "labels" contains non-generic labels, then set it to the verbVertex
							if(labels[labelCounter] != "NMOD" || labels[labelCounter] != "PMOD")//resetting labels for the concatenated vertex
								verbVertex.label = labels[labelCounter];
							fAppendedVertex = 1;
						}
					}
					else{
						verbs[vCount] = s;
						if((verbVertex = getVertex(vertices, s, i)) == null){
							//System.out.println("setting vertex "+s + "numVertices "+numVertices);
							vertices[numVertices] = new Vertex(s, VERB, i, STATE, labels[labelCounter], parents[ParentCounter], POSTag);
							verbVertex = vertices[numVertices];//newly created verb vertex will be considered in the future
							numVertices++;
						}
					}
					
					//if an adverb was found earlier, we set that as the verb's property
					if(prevType == ADV){
						Vertex v1, v2;
						int e;
						//System.out.println("verb "+s +" advcount "+advCount);
						//set previous verb's property to null, if it was set, if there is a verb following the adverb
						if(vCount >= 0){
							if(vCount == 0)
								v1 = searchVertices(vertices, verbs[vCount], i, 0); //fetching the previous verb
							else
								v1 = searchVertices(vertices, verbs[vCount-1], i, 0); //fetching the previous verb
							v2 = searchVertices(vertices, adverbs[advCount-1], i, 0);//fetching the previous adverb							
							//if such an edge exists - DELETE IT
							if(v1 != null && v2 != null && (e = searchEdgesToSetNull(edges, v1, v2, i)) != -1){
								edges[e] = null;//setting the edge to null
								//numEdges--;//deducting an edge count -- messes the counter up!
							}							
						}
						//if this verb vertex was encountered for the first time, vCount < 1,
						// so do adding of edge outside the if condition
						//add a new edge with v1 as the adverb and v2 as the new verb
						v1 = searchVertices(vertices, adverbs[advCount-1], i, 0);
						v2 = verbVertex;
						//if such an edge did not already exist
						if(v1 != null && v2 != null && (e = searchEdgesToSetNull(edges, v1, v2, i)) == -1){
//							System.out.println("$$$ adding edge:: "+v1.name+" - "+v2.name);
							edges[numEdges] = new Edge("verb-property",VERB);
							edges[numEdges].inVertex = v1;//for nCount = 0;
							edges[numEdges].outVertex = v2;// the verb
							edges[numEdges].index = i;
							numEdges++;	
						}
						//advCount--;//having assigned the adverb, we can remove it from the list
					}
					
					//making the previous noun, one of the vertices of the verb edge
					if(nCount >= 1 && fAppendedVertex == 0){//&& vertices[i]!=null && vertices[i][numVertices - 1]!= null){//third condition is to avoid re-assignment
						//gets the previous noun to form the edge
						Vertex v1 = searchVertices(vertices, nouns[nCount-1], i, 0);
						Vertex v2 = verbVertex;
						//if such an edge does not already exist add it
						//System.out.println("check edge "+v1.name +" - "+v2.name);
						int e;
						if(v1 != null && v2 != null && (e = searchEdges(edges, v1, v2, i)) == -1){
//							System.out.println("$$$ adding edge:: "+v1.name+" - "+v2.name);
							edges[numEdges] = new Edge("verb",VERB);
							edges[numEdges].inVertex = v1;//for nCount = 0;
							edges[numEdges].outVertex = v2;// the verb
							edges[numEdges].index = i;
							//compareEdges(edges, edges[numEdges]);
							//nCount = nCount - 1;//decrementing so that this noun is not attached to any other verb!
							numEdges++;
						}
					}
					//System.out.println("edge "+edges[i][numEdges-1].inVertex.name +" - "+edges[i][numEdges-1].outVertex.name);
					fAppendedVertex = 0;//resetting the flag
					prevType = VERB;
					vCount++;	
					//numVertices++;
				}//end of if condition for the verb

//----------------------------------------------------------------------------------------------
				
				//if the string is an adverb
				else if(ps.contains("/RB")){
//					System.out.println("Adverb "+s);	
					adverbialCount++;
					Vertex adverb = null;
					if(prevType == ADV){//appending to existing adverb
						if(advCount >= 1)
							advCount = advCount - 1;
						Vertex prevVertex = searchVertices(vertices, adverbs[advCount], i, 0);//fetching the previous vertex
						adverbs[advCount] = adverbs[advCount] + " " + s;
						//if the concatenated vertex didn't already exist
						if((adverb = getVertex(vertices, adverbs[advCount], i)) == null){
							prevVertex.name = prevVertex.name + " " + s;
							adverb = prevVertex;//setting it as "adverb" for further computation
							//if(adverb.state != STATE)//reset the state
								//adverb.state = STATE;
							//if the existing vertex's label is empty
							if(adverb.label =="")
								adverb.label = labels[labelCounter];
							//if parent is null
							if(adverb.parent == "" || adverb.parent == null || adverb.parent.equalsIgnoreCase(s))
								adverb.parent = parents[ParentCounter];
							//if the label is not empty and if "labels" contains non-generic labels, then set it to the adverb
							else if(labels[labelCounter] != "NMOD" || labels[labelCounter] != "PMOD")//resetting labels for the concatenated vertex
								adverb.label = labels[labelCounter];
						}
					}
					else{//else creating a new vertex
						adverbs[advCount] = s;
						if((adverb = getVertex(vertices, s, i)) == null){
							vertices[numVertices] = new Vertex(adverbs[advCount], ADV, i, STATE, labels[labelCounter], parents[ParentCounter], POSTag);
							adverb = vertices[numVertices];
							numVertices++;
						}
					}
					
					//by default associate it with the previous/latest verb and if there is a verb following it immediately, then remove the property from the verb
					if(vCount >= 0){
						//gets the previous noun to form the edge
						//System.out.println("Previous verb:: "+verbs[vCount-1]);
						//System.out.println("Adverb :: "+adverb.name);
						Vertex v1;
						if(vCount == 0)
							v1 = searchVertices(vertices, verbs[vCount], i, 0);
						else
							v1 = searchVertices(vertices, verbs[vCount-1], i, 0);
						Vertex v2 = adverb;
						int e;
						//if such an edge does not already exist add it
						if(v1 != null && v2 != null && (e = searchEdges(edges, v1, v2, i)) == -1){
//							System.out.println("$$$ adding edge:: "+v1.name+" - "+v2.name);
							edges[numEdges] = new Edge("verb-property",VERB);
							edges[numEdges].inVertex = v1;//for nCount = 0;
							edges[numEdges].outVertex = v2;// the verb
							edges[numEdges].index = i;
							numEdges++;
						}
					}
					advCount++;
					prevType = ADV;
					//numVertices++;
				}//end of if condition for adverb
				
				//incrementing counters for labels and parents
				labelCounter++;
				ParentCounter++;
			}//end of the while loop for the tokens of a sentence
						
			//checking if there are any double edges
			System.out.println("cou:"+cou);
			System.out.println("numEdges:"+numEdges);
			for(int de = cou; (de+1) < numEdges; de++){
				if(edges[de] != null && edges[de+1] != null){
					System.out.println(edges[de].inVertex.name+" - "+ edges[de].outVertex.name);
					System.out.println(edges[de+1].inVertex.name+" - "+ edges[de+1].outVertex.name);
					if(edges[de].outVertex == edges[de+1].inVertex){
						numDoubleEdges++;
						break;
					}
				}
			}
			System.out.println("numDoubleEdges: "+ numDoubleEdges);
			cou = numEdges;
			nouns = null;
			verbs = null;
			adjectives = null;
			adverbs = null;
		}//end of the for loop
		setSemanticLabelsForEdges(vertices, edges);
		
		//edges that satisfy the frequency threshold are selected for semantic matching
		//Prune by frequency for submission
//		if(trainTestFlag == 1){
//			if(numEdges > 1){
//				int temp = numEdges;
//				edge[] tempEdges = edges;
//				System.out.println("*********Pruning edges by frequency!");
//				edges = frequencyThreshold(edges, numEdges, trainTestFlag);
//				if(numEdges == 0){//if no edges were selected
//					edges = tempEdges;
//					numEdges = temp;
//				}
//				else
//					vertices = selectVertices(edges, numEdges);
//			}
//		}

		GraphGenerator.edges = edges;
		GraphGenerator.vertices = vertices;

//		printGraph(edges, vertices);
//		System.out.println("Number of edges:: "+numEdges);
//		System.out.println("Number of vertices:: "+numVertices);
		
		edges = null;
		vertices = null;	
		
		timeTaken = System.currentTimeMillis() - begin;
		
		return numEdges;
	}//end of the method
	
	//------------------------------------------#------------------------------------------#------------------------------------------

	public String replaceApostrophes(String text){
	  if(text.contains("'s"))
		  text = text.replaceAll("'s", " is");
	  if(text.contains("'d"))
		  text = text.replaceAll("'d", " could"); //We are replacing it with a could, although it could be reaplced with any modal
	  if(text.contains("s'"))
	    text = text.replaceAll("s'", "s");
	  if(text.contains("'re"))
		  text = text.replaceAll("'re", " are");
	  
	  if(text.contains("can't"))
	    text = text.replaceAll("'t", " not");
	  else if(text.contains("n't"))
		text = text.replaceAll("n't", " not");
	  
	  return text;
	}
	
	public Edge[] frequencyThreshold(Edge[] edges, int num, int trainTest){
		//freqEdges maintains the top frequency edges from ALPHA_FREQ to BETA_FREQ
		Edge[][] freqEdges = new Edge[BETA_FREQ][num];//from alpha = 3 to beta = 10
		//iterating through all the edges
		for(int j = 0; j < num; j++){
			if(edges[j]!=null){				
				if(edges[j].frequency <= BETA_FREQ && edges[j].frequency >= ALPHA_FREQ && freqEdges[edges[j].frequency-1] != null){
					int i;
					for(i = 0; freqEdges[edges[j].frequency-1][i] != null; i++);//iterating to find i for which freqEdges is null
					freqEdges[edges[j].frequency-1][i] = edges[j];
				}
			}
		}
		
		//Selecting only those edges that satisfy the frequency condition [between ALPHA and BETA]
		int maxSelected = 0;//counter
		Edge[] selectedEdges = new Edge[numEdges];//MAX is the number of edges in the graph
		for(int j = BETA_FREQ-1; j >= ALPHA_FREQ-1; j--){//&& maxSelected < MAX
			if(freqEdges[j] != null){
				for(int i = 0; freqEdges[j][i] != null && i < num; i++){//&& maxSelected < MAX
					selectedEdges[maxSelected] = freqEdges[j][i];
					maxSelected++;
				}
			}
		}
		
		if(maxSelected != 0)
			numEdges = maxSelected;//replacing numEdges with the number of selected edges
		return selectedEdges;
	}
	
	/*
	 * Locating an older vertex and returning it to the main code.
	 */
	public Vertex searchVertices(Vertex[] list, String s, int index, int flag){
//		System.out.println("***** searchVertices:: "+s);
		for(int i = 0;i < numVertices; i++){
			if(list[i]!=null && s != null)			
				if((list[i].name.toLowerCase()).equals(s.toLowerCase()) && list[i].index == index){//if the vertex exists and in the same sentence (index)
//					System.out.println("***** Returning:: "+s);
					return list[i];
				}
		}
				
		return null;
	}
	
	/*
	 * While checking if the complete vertex already exists and if it does incrementing its frequency.
	 * Also, deleting substrings that would have formed full vertices early on, if any exist.
	 */
	public Vertex getVertex(Vertex[] verts, String s, int index)
	{
		int position = 0;
		int flag = 0;
//		System.out.println("***getVertex:: "+s);
		if(s == null){
			return null;
		}
		for(int i = 0;  i < numVertices; i++){
				//System.out.println("Comparing "+ verts[textNo][i].name.toLowerCase() +" - "+s.toLowerCase());
				if(verts[i] != null && verts[i].name.equalsIgnoreCase(s) && index == verts[i].index){
//					System.out.println("**** FOUND vertex:: "+s);
					flag = 1;
					position = i;
					verts[i].frequency++;

					//NULLIFY ALL VERTICES CONTAINING SUBSTRINGS OF THIS VERTEX IN THE SAME SENTENCE (verts[j].index == index)
					for(int j = numVertices - 1;  j >= 0; j--){
						if(verts[j] != null && verts[j].index == index && !s.equalsIgnoreCase(verts[j].name) && s.toLowerCase().contains(verts[j].name.toLowerCase())){
//							System.out.println("FOUND substring:: "+verts[j].name);
							verts[j] = null;
							//numVertices--;
						}
					}
					break;
				}
		}
		
		if(flag == 1)
			return verts[position];
		else{
//			System.out.println("***getVertex returning null");
			return null;
		}
	}
	
	//use this method when looking for noun-adj or verb-adv matches, which you'd like to delete and replace by adj-noun etc.
	public int searchEdgesToSetNull(Edge[] list, Vertex in, Vertex out, int index){
		  int edgePos = -1;
			for(int i = 0;i < numEdges; i++){
				if(list[i]!=null && list[i].inVertex != null && list[i].outVertex != null){
					//checking for exact match with an edge
					//System.out.println("***** List[i]:: "+list[i].inVertex.name +" - "+list[i].outVertex.name);
					if(((list[i].inVertex.name.equalsIgnoreCase(in.name))//|| list[i].inVertex.name.contains(in.name) 
							&& (list[i].outVertex.name.equalsIgnoreCase(out.name))) || //|| list[i].outVertex.name.contains(out.name)
							((list[i].inVertex.name.equalsIgnoreCase(out.name))//|| list[i].inVertex.name.contains(out.name) 
							&& (list[i].outVertex.name.equalsIgnoreCase(in.name)))){//|| list[i].outVertex.name.contains(in.name)
						//System.out.println("***** Found edge! : index::"+index+"list[i].index::"+list[i].index);
						//if an edge was found
						edgePos = i;//returning its position in the array
						//INCREMENT FREQUENCY IF THE EDGE WAS FOUND IN A DIFFERENT SENT. (CHECK BY MAINTAINING A TEXT NUMBER AND CHECKING IF THE NEW # IS DIFF FROM PREV #)
						if(index != list[i].index){
							list[i].frequency++;
						}
						//System.out.println(list[i].inVertex.name+" - "+list[i].outVertex.name+" freq:: "+list[i].frequency);
						//System.out.println(in.name+" - "+out.name+" freq:: "+list[i].frequency);
					}
				}
			}
//		System.out.println("***** searchEdgesToSetNull:: "+in.name +" - "+out.name+" returning:: "+edgePos);
		return edgePos;
	}
	/* Checks to see if an edge between vertices "in" and "out" exists.
	 * true - if an edge exists and false - if an edge doesn't exist
	 */
	public int searchEdges(Edge[] list, Vertex in, Vertex out, int index){
		int edgePos = -1;
//		  System.out.println("***** Searching for edge:: "+in.name +" - "+out.name);
			for(int i = 0;i < numEdges; i++){
				if(list[i]!=null && list[i].inVertex != null && list[i].outVertex != null){
					//checking for exact match or superset match with an edge in the set (in any order)
//					System.out.println("***** List[i]:: "+list[i].inVertex.name +" - "+list[i].outVertex.name);
					if(((list[i].inVertex.name.equalsIgnoreCase(in.name)|| list[i].inVertex.name.contains(in.name)) 
							&& (list[i].outVertex.name.equalsIgnoreCase(out.name)|| list[i].outVertex.name.contains(out.name))) ||
							((list[i].inVertex.name.equalsIgnoreCase(out.name) || list[i].inVertex.name.contains(out.name))
							&& (list[i].outVertex.name.equalsIgnoreCase(in.name) || list[i].outVertex.name.contains(in.name)))){
						//System.out.println("***** Found edge! : index::"+index+"list[i].index::"+list[i].index);
						//if an edge was found
						edgePos = i;//returning its position in the array
						//INCREMENT FREQUENCY IF THE EDGE WAS FOUND IN A DIFFERENT SENT. (CHECK BY MAINTAINING A TEXT NUMBER AND CHECKING IF THE NEW # IS DIFF FROM PREV #)
						if(index != list[i].index){
							list[i].frequency++;
						}
						//break; //break out after the edge match is found - let it print all edges!
						//System.out.println(list[i].inVertex.name+" - "+list[i].outVertex.name+" freq:: "+list[i].frequency);
						//System.out.println(in.name+" - "+out.name+" freq:: "+list[i].frequency);
					}
				}
//				else{System.out.println("***** List[i]:: "+i+" is null! ");}
			}
			
			//NULLIFY ALL VERTICES CONTAINING SUBSTRINGS OF THIS EDGE's In-VERTEX or OUt-VETEX IN THE SAME SENTENCE (list[j].index == index)
			for(int j = numEdges - 1;  j >= 0; j--){
				if(list[j] != null && list[j].index == index){
					//when in-vertices are eq and out-verts are substrings or vice versa
					if(in.name.equalsIgnoreCase(list[j].inVertex.name) && !out.name.equalsIgnoreCase(list[j].outVertex.name.toLowerCase()) &&
						out.name.toLowerCase().contains(list[j].outVertex.name.toLowerCase())){
//						System.out.println("FOUND outvertex match for edge:: ");
						list[j] = null;
						//numEdges--;
					}
					//when in-vertices are eq and out-verts are substrings or vice versa
					else if(!in.name.equalsIgnoreCase(list[j].inVertex.name) && in.name.toLowerCase().contains(list[j].inVertex.name.toLowerCase()) &&
								out.name.equalsIgnoreCase(list[j].outVertex.name)){
//						System.out.println("FOUND intvertex match for edge: ");
						list[j] = null;
						//numEdges--;
					}
				}
			}
//			System.out.println("***** Searching for edge returning:: "+edgePos);
			return edgePos;
	}
	/*
	 * Setting semantic labels for edges based on the labels vertices have with their parents
	 */
	public void setSemanticLabelsForEdges(Vertex[] vertices, Edge[] edges){
		Vertex parent = null;
		for(int i = 0; i < numVertices; i++){
			if(vertices[i] != null && vertices[i].parent != null){//parent = null for ROOT
//				System.out.println("**Parent for::"+vertices[i].name);
				//search for the parent vertex
				for(int j = 0; j < numVertices; j++){
					if(vertices[j] != null && (vertices[j].name.equalsIgnoreCase(vertices[i].parent) || 
								vertices[j].name.toLowerCase().contains(vertices[i].parent.toLowerCase()))){
//						System.out.println("**Parent::"+vertices[j].name);
						parent = vertices[j];
						break;//break out of search for the parent
					}
				}
				if(parent != null){
					//check if an edge exists between vertices[i] and the parent
					for(int k = 0; k < numEdges; k++){
						if(edges[k]!=null && edges[k].inVertex != null && edges[k].outVertex != null){
							if((edges[k].inVertex.name == vertices[i].name && edges[k].outVertex.name == parent.name) || 					
								(edges[k].inVertex.name == parent.name && edges[k].outVertex.name == vertices[i].name)){
//								System.out.println("Setting label for edge::"+edges[k].inVertex.name+" && "+edges[k].outVertex.name);
								//set the role label
								if(edges[k].label == null || edges[k].label == "")//if the edge is null or empty
									edges[k].label = vertices[i].label; 
								//if eges[k]'s label is nmod or pmod and the vertex's label is not empty or nmod or pmod but adds more information
								else if(edges[k].label != null && (edges[k].label == "NMOD" || edges[k].label == "PMOD") && 
										(vertices[i].label != "" && vertices[i].label != "NMOD" && vertices[i].label != "PMOD"))
									edges[k].label = vertices[i].label;
							}
						}
					}
				}
				//resetting parent
				parent = null;
			}
		}//end of outer for loop
		
		//if any edges didn't get labeled, set them to the in-vertex's label
		for(int k = 0; k < edges.length; k++){
			if(edges[k]!=null && (edges[k].label == null  || edges[k].label == "")){
//				System.out.println(edges[k].inVertex.label +"-"+ edges[k].outVertex.label);
				//the in-vertex has priority over the out-vertex.
				if(edges[k].inVertex.label != "" && edges[k].inVertex.label != null)
					edges[k].label = edges[k].inVertex.label;
				else if((edges[k].inVertex.label == "" || edges[k].inVertex.label == null) && (edges[k].outVertex.label != "" && edges[k].outVertex.label != null))
					edges[k].label = edges[k].outVertex.label;
			}
		}
					
	}
	
	/*
	 * Selecting only those vertices that correspond to the the selected edges
	 */
	
	public Vertex[] selectVertices(Edge[] selectedEdges, int num){
		Vertex[] vertices = new Vertex[num * 2];
		int vertCount = 0;
		
		for(int i = 0;i < selectedEdges.length; i++){
			if(selectedEdges[i] != null){
				if(selectedEdges[i].inVertex.nodeID == -1){//to make sure the same vertex doesnt get selected twice
					//System.out.println("Invertex of i: "+i);
					selectedEdges[i].inVertex.nodeID = i;
					vertices[selectedEdges[i].inVertex.nodeID] = selectedEdges[i].inVertex;
					vertCount++;
				}
				if(selectedEdges[i].outVertex.nodeID == -1){
					//System.out.println("Outvertex of i: "+i);
					selectedEdges[i].outVertex.nodeID = i + num;//to generate a really large node id, which doesn't conflict with other vertices' ids
					vertices[selectedEdges[i].outVertex.nodeID] = selectedEdges[i].outVertex;
					vertCount++;
				}
			}
		}
		numVertices = vertCount;
		return vertices;		
	}
}


