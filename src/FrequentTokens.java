import java.util.ArrayList;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class FrequentTokens{
	public ArrayList<String> getFrequentTokens(ArrayList<String> tokens){
		ArrayList<String> freqTokens = new ArrayList<String>();
		//counting the tokens
		HashMap<String, Integer> h = new HashMap();
		for(String tok : tokens){
			Object obj;
			if((obj = h.get(tok)) == null){
				h.put(tok, 1);
			} else{
				h.put(tok, ((Integer)obj + 1));
			}
		}
		//identify averge of the counts
		int sum = 0;
		for(Integer v : h.values()){
			sum += v;
		}
		int avgcount = sum/h.size();
		
		//all hashmap values >= avgcount are selected as frequent tokens
		for(String key : h.keySet()){
			if(((Integer)h.get(key)) >= avgcount){
				freqTokens.add(key);
			}
		}
		//sort the hashmap, select the most frequent tokens
		//SortedSet<Integer> values = new TreeSet<Integer>(h.values());
		return freqTokens;
	}
}