import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import edu.mit.jwi.item.POS;

public class MakeBlacklist {
	public static Map<String,String> verb = new HashMap<String,String>();
	public static Map<String,Set<String>> obj = new HashMap<String,Set<String>>();
	
	public static Map<String,Set<String>> objList;
	public static WordNet wn = new WordNet();
	
	public MakeBlacklist() {
		try {
			readBlacklist();
		} catch(Exception e) {
			System.out.println("MakeBlacklist open fail");
		}
	}
	public static boolean checkBlacklist(String verbWord, String objWord){
	
		if(!verb.containsKey(verbWord)) {
			//System.out.println("no key");
			return false;
		}
		
		Set<String> temp = obj.get(UnionFind(verb.get(verbWord)));
		if(temp == null) {
			//System.out.println(UnionFind(verb.get(verbWord)) + "null value");
			return false;
			}
		if(!temp.contains(objWord)) {			
			//System.out.println(UnionFind(verb.get(verbWord)) + "no value");
			return false;
		}
		return true;
	}
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
				
				System.out.println(data[0]);
				//null인 경우 사전에 없는 단어
				String val = data[1];
				if(data[1].equals(null)) val = data[0];
				verb.put(data[0],val);
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

				String key = data[0];		
				String value =data[1];
				
				verb.put(key, null);
				
				if(obj.containsKey(key)) {
					Set<String> temp = obj.get(key);
					temp.add(value);
					obj.put(key,temp);
				}
				else {
					Set<String> l = new HashSet<String>();
					l.add(value);
					obj.put(key,l);
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
		
		ArrayList<String> synList = wn.getSynonyms(a, POS.VERB);
		if(synList == null) return null;
		
		for(String s : set){
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
		Map<String,String> temp = new HashMap<String,String>();
		
		
	    for (Map.Entry<String, String> entry : verb.entrySet()) {
			//if root(value) exists, don't search
	    	String key = entry.getKey();
	    	
	    	if(verb.get(key) != null) continue;
	    	ArrayList<String> synList = isSynonym(key, verb.keySet());
	    	if(synList == null) {

    			continue;
	    	}
	    	
	    	for(String i : synList) {	    	
	    		//if root exists, change the root.
	    		if(verb.get(i) != null) { 
	    			verb.put( UnionFind(key) , UnionFind(verb.get(i)) );
	    			continue;
	    		}
	    		temp.put(i,key);
	    	}
	    }
	    verb.putAll(temp);   
	}
	
	/*
	 * combine same meaning obj
	 */
	public static void makeObjList() {
		for (Map.Entry<String, String> entry : verb.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			if(key.equals(val)) continue;
			if(obj.get(val) != null)obj.get(val).addAll(obj.get(key));
			obj.remove(key);
		}
		
		//add synonyms
		for(Map.Entry<String, Set<String>> entry : obj.entrySet()) {
			Set<String> val = entry.getValue();
			Set<String> temp = new HashSet<String>();
			for(String word : val) {
				List<String> synWords = wn.getSynonyms(word, POS.NOUN);
				if(synWords != null)
					temp.addAll(wn.getSynonyms(word, POS.NOUN));
			}
			val.addAll(temp);
			entry.setValue(val);
		}
	}
	public static void makeObjTrie() {
	}
	public static void makeVerbTrie() {
	}
/*
	public static void main(String[] args) {
		readBlacklist();
		checkBlacklist("make","stack");
	*/
	
}
