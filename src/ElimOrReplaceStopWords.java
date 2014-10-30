import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
class ElimOrReplaceStopWords{
	public static HashMap hs;
	public String[] eliminateStopWords(String[] segments){
		StringTokenizer st;
		ElimOrReplaceStopWords em = new ElimOrReplaceStopWords();
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
				PorterStemmer s = new PorterStemmer();
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					String temptoken = token;
					String[] stemText = s.getStemmedTextAndSuffix(temptoken, s);
					if(stemText[1] != "")
						temptoken = stemText[0]+"("+stemText[1]+")?";
					else
						temptoken = stemText[0];
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
	
	//replace stopwords in longer phrases
	public ArrayList<String> replaceStopWords(ArrayList<String> segments){
		ArrayList<String> outList = new ArrayList<String>();
		StringTokenizer st;
		ElimOrReplaceStopWords em = new ElimOrReplaceStopWords();
		for(int i = 0; i < segments.size(); i++){
			//System.out.println("Before: "+segments[i]);
			String temp = ""; //holds the segment created without the stopwords
			if(segments.get(i) != null){
				hs = new HashMap<String, String>(); //to get the unique tokens in a segment
				String seg = segments.get(i).toLowerCase();
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
				PorterStemmer s = new PorterStemmer();
				while(st.hasMoreTokens()){
					String token = st.nextToken();
					String temptoken = token;
					String[] stemText = s.getStemmedTextAndSuffix(temptoken, s);
					if(stemText[1] != "")
						temptoken = stemText[0]+"("+stemText[1]+")?";
					else
						temptoken = stemText[0];
					
					if(isStopWordorFrequentWord(temptoken)){
						temp = temp + " (\\\\w{0-4}\\\\s)";//regex for stopwords and space
					}else{
//						em.selectUniqueWords(temptoken);
						temp = temp+" "+token;
					}
				}
//				System.out.println("temp before:: " +temp);
				int j = 1;
				while(j < 11){
					if(temp.contains("(\\\\w{0-4}\\\\s) (\\\\w{0-4}\\\\s)")){
						temp = temp.replace("(\\\\w{0-4}\\\\s) (\\\\w{0-4}\\\\s)", "(\\\\w{0-4}\\\\s)");
					}else{
						break; //break out of the loop
					}
					j++;
				}
				//temp = temp.replaceAll("\\\\s) (\\\\w", "\\\\s)(\\\\w");//remove spaces 
				temp = temp.replace("(\\\\w{0-4}\\\\s)", "(\\\\w{0-4}\\\\s){0-"+j+"}");//"j" number of stopwords
				temp = temp.trim();
//				System.out.println("print temp:" +temp);
				//replace segments
				if(temp.trim() != "" && !temp.trim().equals("(\\\\w{0-4}\\\\s){0-"+j+"}")){
//					System.out.println("Adding:" +temp);
					outList.add(temp);
				}
			}
		}
		return outList;
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