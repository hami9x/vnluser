package rfx.server.util;

import java.util.Arrays;
import java.util.List;

public class RoundRobin<T> {  
	
    private List<T> values;
    private int N = 0;
    private int counter = -1;
    private T singleValue;

    public T next() {
    	if(singleValue != null){
    		return singleValue;
    	} else {
    		counter = (counter + 1) % N; // % is the remainder operator
            return values.get(counter);	
    	}        
    }    
    
    public RoundRobin(T value){
    	singleValue = value;
    	N = 1;
    }
    
    public RoundRobin(List<T> list){
    	values = list;
    	N = values.size();
    }

    public static void main(String[] args) {    	
    	String str = "A, B, C, D";
    	List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
    	RoundRobin<String> roundRobin = new RoundRobin<>(items);
    	
        for (int i = 0; i < 10; i++) {
            System.out.println(roundRobin.next());
        }
    }
}
