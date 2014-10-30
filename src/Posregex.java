import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

class Posregex{
	public String identifyPOStagsforPhrases(String rubricPhrase, MaxentTagger posTagger) throws ClassNotFoundException, IOException{
		GenerateEquivalenceClasses genClass = new GenerateEquivalenceClasses();
		WordnetBasedSimilarity wn = new WordnetBasedSimilarity();
		String outputPhrase = "";
		StringTokenizer st = new StringTokenizer(rubricPhrase);
		if(st.countTokens() > 1){
			while(st.hasMoreTokens()){
				//'token' is the unigram from the sentence segment in the rubirc text
				String token = st.nextToken();
				if(!token.contains("(\\\\w{0-4}\\\\s)")){ //if the token is not a frequent word
					String postag = posTagger.tagString(token.trim());
					outputPhrase = outputPhrase +" "+postag.substring(postag.indexOf("/")+1).trim();
					//outputPhrase = outputPhrase +" "+token;
				}else{
					outputPhrase = outputPhrase +" "+token;
				}
			}//iterating over each of the tokens in the rubric phrase
			System.out.println("outphrase: "+outputPhrase);
			return outputPhrase.trim();
		} else{
			return null;
		}
	}
}