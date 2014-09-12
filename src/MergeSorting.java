import java.util.ArrayList;


public class MergeSorting {
	
	/**
	 * Sorting algorithm from wikipedia for merge sort
	 * @param list
	 * @param flag - indicates if SentenceSimilarity or TopicSentenceIdentification classes was calling it
	 * @return
	 */
	public ArrayList sorting(ArrayList list, int flag){
		//System.out.println("Inside sorting: "+list.size());
		int size = list.size();
		if(list.size() <= 1)//if only one element is in the list, return it as it is
			return list;
		//implementing sorting as a merge sort algorithm
		int middle = size/2;//getting the mid value
		ArrayList left = new ArrayList();
		ArrayList right = new ArrayList();
		for(int i = 0; i < size; i++){
			if(i < middle)
				left.add(list.get(i));
			else if(i >= middle)
				right.add(list.get(i));
		}
		
		left = sorting(left, flag);
		right = sorting(right, flag);
		
		return merge(left, right, flag);
	}
	
	public ArrayList merge(ArrayList left, ArrayList right, int flag){
		//System.out.println("Inside merge");
		ArrayList result = new ArrayList();
		while(left.size() > 0 || right.size() > 0){
			//when both the left and right have elements, compare them
			if(left.size() > 0 && right.size() > 0){
				if(flag == 0){//for type arrayList
					//if the left element is greater than the right element
					if(((ArrayList)left.get(0)).size() >= ((ArrayList)right.get(0)).size()){
						//System.out.println("Adding:"+left.get(0));
						result.add(left.get(0));
						left.remove(0);//removing element in the first index
					}
					else{
						//System.out.println("Adding:"+right.get(0));
						result.add(right.get(0));
						right.remove(0);
					}
				}
			}
			//since only the left has elements
			else if(left.size() > 0){
				//System.out.println("Adding:"+left.get(0));
				result.add(left.get(0));
				left.remove(0);
			}
			//since only the right has elements
			else if(right.size() > 0){
				//System.out.println("Adding:"+right.get(0));
				result.add(right.get(0));
				right.remove(0);
			}
		}//end of the while loop
		return result;
	}
}
