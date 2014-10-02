import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

public class FrequentTokens{
	public ArrayList<String> getFrequentTokens(ArrayList<String> tokens){
		FrequentTokens ft = new FrequentTokens();
		
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
		
		//identify average of the counts
		int sum = 0;
		for(Integer v : h.values()){
			sum += v;
		}
		int avgcount = sum/h.size();
		
		//sort the hashmap by values
		h = ft.sortByValues(h);
		
		//all hashmap values >= avgcount are selected as frequent tokens
		System.out.println("Printing the top frequent tokens");
		for(String key : h.keySet()){
			if(((Integer)h.get(key)) >= avgcount){
				System.out.println(key);
				freqTokens.add(key);
			}
		}
		//sort the hashmap, select the most frequent tokens
		//SortedSet<Integer> values = new TreeSet<Integer>(h.values());
		return freqTokens;
	}
	
	private static HashMap sortByValues(HashMap map) { 
	       List list = new LinkedList(map.entrySet());
	       Collections.sort(list, new Comparator() {
	            public int compare(Object o1, Object o2) {
	               return ((Comparable) ((Map.Entry) (o2)).getValue())
	                  .compareTo(((Map.Entry) (o1)).getValue());
	            }
	       });

	       // Here I am copying the sorted list in HashMap
	       // using LinkedHashMap to preserve the insertion order
	       HashMap sortedHashMap = new LinkedHashMap();
	       for (Iterator it = list.iterator(); it.hasNext();) {
	              Map.Entry entry = (Map.Entry) it.next();
	              sortedHashMap.put(entry.getKey(), entry.getValue());
	       } 
	       return sortedHashMap;
	  }
}