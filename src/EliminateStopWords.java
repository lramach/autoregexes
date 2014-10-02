import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
class EliminateStopWords{
	public static HashMap hs;
	public String[] eliminateStopWords(String[] segments){
		StringTokenizer st;
		EliminateStopWords em = new EliminateStopWords();
		for(int i = 0; i < segments.length; i++){
			//System.out.println("Before: "+segments[i]);
			if(segments[i] != null){
				hs = new HashMap<String, String>(); //to get the unique tokens in a segment
				String seg = segments[i].toLowerCase();
				seg = seg.replaceAll(";", "");
				seg = seg.replaceAll(",", "");
				seg = seg.replaceAll(":", "");
				seg = seg.replaceAll("\"", "");
				seg = seg.replaceAll("\\.", " ");
				seg = seg.replaceAll("\\/", " ");
				seg = seg.replaceAll("\\:", " ");
				seg = seg.replaceAll("[?()]", "");
				seg = seg.replaceAll("!", "");
				seg = seg.replaceAll("[(]", "");
				seg = seg.replaceAll("[)]", "");
				st = new StringTokenizer(seg);
				//System.out.println("After: "+ seg);
				//String temp = "";
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					String temptoken = token;
					for(int j = 0; j < Stopwords.suffixes.length; j++){
						if(temptoken.endsWith(Stopwords.suffixes[j]) && temptoken.length() > 5){
							temptoken = temptoken.replace(Stopwords.suffixes[j], "");
							break;
						}
					}
					if(isStopWordorFrequentWord(temptoken)){
						continue;
					}else{
						em.selectUniqueWords(temptoken);
						//temp = temp+" "+token;
					}
				}
				//getting the complete segment with all its unique words
				String temp = "";
				Iterator iter = hs.entrySet().iterator();
				while (iter.hasNext()) {
					Map.Entry mEntry = (Map.Entry) iter.next();
					temp = temp+" "+mEntry.getValue();
				}
				
				System.out.println("temp:: " +temp);
				//replace segments
				segments[i] = temp;
			}
		}
		return segments;
	}
	
	public static boolean isStopWordorFrequentWord(String word){
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
	        return true;
	      }
		}
	    //checking for stopwords
	    for (int i = 0; i < Stopwords.STOP_WORDS.length; i++)
		      if ((word.replaceAll("\"", "")).equalsIgnoreCase(Stopwords.STOP_WORDS[i])){
		        return true;
		      }
	    for (int i = 0; i < Stopwords.FREQUENT_WORDS.length; i++)
		      if (word.equalsIgnoreCase(Stopwords.FREQUENT_WORDS[i])){
		        return true;
		      }
	    return false;    
	  }
	
	public void selectUniqueWords(String word){
		if(hs.get(word) == null){
			hs.put(word, word);
		}
	}
}