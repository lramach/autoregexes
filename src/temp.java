import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

class temp{
	
	public static void main(String[] args){
		StringTokenizer st = new StringTokenizer("hello-word this is fun");
		while(st.hasMoreTokens()){
			System.out.println(st.nextToken());
		}
		HashMap<String, Integer> hash = new HashMap<String, Integer>();
		hash.put("hello", 3);
		hash.put("hi", 9);
		hash.put("bye", 5);
		temp t = new temp();
		hash = t.sortByValues(hash);
		
		for(String key : hash.keySet()){
			System.out.println(key);
		}
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