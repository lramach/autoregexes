import java.util.ArrayList;
import java.util.StringTokenizer;

public class Tokenize{
	public ArrayList<String> tokenizeTopScorers(String[] text){
		ArrayList<String> tokensList = new ArrayList<String>();
		StringTokenizer sttop;
		for(int i = 0; i< text.length; i++){
			if(text[i] != null){
				sttop = new StringTokenizer(text[i]);
				while(sttop.hasMoreTokens()){
					String toptoken = sttop.nextToken();//'toptoken' is the token from the top-scoring response
					tokensList.add(toptoken); 
				}
			}
		}
		return tokensList;
	}
	
	public ArrayList<ArrayList<String>> tokenizeRubric(String[] text){
		ArrayList<ArrayList<String>> tokensList = new ArrayList<ArrayList<String>>();
		StringTokenizer sttop;
		for(int i = 0; i< text.length; i++){
			System.out.println("rubric text["+i+"]:"+text[i]);
			if(text[i] != null){
				sttop = new StringTokenizer(text[i]);
				ArrayList<String> temp = new ArrayList<String>();
				while(sttop.hasMoreTokens()){
					String toptoken = sttop.nextToken();
					temp.add(toptoken); 
				}
				tokensList.add(temp);
			}
		}
		return tokensList;
	}
}