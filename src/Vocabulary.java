import java.util.ArrayList;
import java.util.StringTokenizer;

public class Vocabulary{
	public ArrayList<String> tokenize(String[] text){
		ArrayList<String> tokensList = new ArrayList<String>();
		//compare token with words in the top scoring responses
		StringTokenizer sttop;
		for(int i = 0; i< text.length; i++){
			if(text[i] != null){
				sttop = new StringTokenizer(text[i]);
				//compare the rubric token with each top score token
				while(sttop.hasMoreTokens()){
					String toptoken = sttop.nextToken();//'toptoken' is the token from the top-scoring response
					tokensList.add(toptoken); 
				}
			}
		}
		System.out.println("Returning from tokenization");
		return tokensList;
	}
}