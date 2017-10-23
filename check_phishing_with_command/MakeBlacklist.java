import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class MakeBlacklist {
	public static Map<String,String> verb = new HashMap<String,String>();
	public static Map<String,Set<String>> obj = new HashMap<String,Set<String>>();
	
	public static Map<String,Set<String>> objList;
	public static WordNet wn = new WordNet();

	/*
	 * read saved blacklist
	 */
	public static void readBlacklist() {
		String fileName = System.getProperty("user.dir") + "\\src";
		FileReader fr = null;
    	BufferedReader br = null;
		try {
			fr = new FileReader(fileName + "\\result.txt");
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null && !s.equals("obj")) {
				String[] data = s.split(" ");
				if(data.length < 2) continue;
				
				verb.put(data[0],data[1]);
				
			}
			while((s = br.readLine()) != null ) {
				String[] data = s.split(" ");
				Set<String> temp = new HashSet<String>();
				for(int i = 1; i < data.length; i++)
					temp.add(data[i]);
				obj.put(data[0], temp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/*
	 * Reading text data (verb obj)
	 */
	public static void readData() {
		String fileName = System.getProperty("user.dir") + "\\src";
		FileReader fr = null;
    	BufferedReader br = null;
		try {
			fr = new FileReader(fileName + "\\data.txt");
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] data = s.split(" ");
				if(data.length < 2) continue;
				
				verb.put(data[0], null);
				
				if(obj.containsKey(data[0])) {
					obj.get(data[0]).add(data[1]);
				}
				else {
					Set<String> l = new HashSet<String>();
					l.add(data[1]);
					obj.put(data[0],l);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/*
	 * save blacklist
	 */
	public static void saveData() {
		String fileName = System.getProperty("user.dir") + "\\src";
		FileWriter wr = null;
    	BufferedWriter br = null;
		try {
			wr = new FileWriter(fileName + "\\result.txt");
			br = new BufferedWriter(wr);
			for (Map.Entry<String, String> entry : verb.entrySet()) {
				br.write(entry.getKey() + " " + entry.getValue() + "\r\n");
			}
			br.write("obj\r\n");
			for (Map.Entry<String, Set<String>> entry : obj.entrySet()) {
				br.write(entry.getKey() + " ");
				for(String word : entry.getValue()) {
					br.write(word + " ");
				}
				br.write("\r\n");
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	/*
	 * if if set has synonyms with string a, return them.
	 */
	public static ArrayList<String> isSynonym(String a, Set<String> set) {
		ArrayList<String> result = new ArrayList<String>();
		
		ArrayList<String> synList = wn.getSynonyms(a);
		for(String s : set) {
			if(synList.contains(s)) {
				result.add(s);
			}
		}
		
		return result;
	}
	
	/*
	 * finding root.
	 */
	public static String UnionFind(String s) {
		String root = verb.get(s);
		if(s == root) return s;
		verb.put(s, UnionFind(root));
		return root;
	}
	
	/*
	 * combine same meaning verb
	 */
	public static void makeVerbList(){
	    for (Map.Entry<String, String> entry : verb.entrySet()) {
	    	//if root(value) exists, don't search
	    	String key = entry.getKey();
	    	if(verb.get(key) != null) continue;
	    	ArrayList<String> synList = isSynonym(key, verb.keySet());
	    	for(String i : synList) {
	    		//if root exists, change the root.
	    		if(verb.get(i) != null) {
	    			verb.put( UnionFind(key) , UnionFind(verb.get(i)) );
	    			continue;
	    		}
	    		verb.put(i,key);
	    	}
	    }
	    
	}
	
	/*
	 * combine same meaning obj
	 */
	public static void makeObjList() {
		for (Map.Entry<String, String> entry : verb.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			if(key.equals(val)) continue;
			
			obj.get(val).addAll(obj.get(key));
			obj.remove(key);		
		}   
	}
	public static void makeObjTrie() {
		
	}

	public static void makeVerbTrie() {
		
	}
	public static void main(String[] args) {
		readData();
		System.out.println("read");
		makeVerbList();
		System.out.println("makeVerbList");
		makeObjList();
		System.out.println("makeObjList");
		saveData();
	
	}
}
