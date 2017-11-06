import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import edu.mit.jwi.item.POS;

public class MakeBlacklist {
	private Map<String,String> verb = new HashMap<String,String>();
	private Map<String,Set<String>> obj = new HashMap<String,Set<String>>();
	
	private WordNet wn = new WordNet();
	private CoreNLP cn = new CoreNLP();
	
	private String NUMBER = "_NUMBER_";
	private File blackList;
	
	
	public MakeBlacklist(String BLlocate) {
		try {
			blackList = new File(BLlocate);
			if(!blackList.exists()) {
				System.out.println("-- no blacklist file");
			}
			blackList.createNewFile();
			readBlacklist();
		} catch(Exception e) {
			System.out.println("MakeBlacklist open fail");
		}
	}
	
	/*
	 * Check if the pair is in blacklist.
	 */
	public boolean checkBlacklist(String verbWord, String objWord){
	
		if(!verb.containsKey(verbWord)) {
			//System.out.println("no key");
			return false;
		}
		
		Set<String> temp = obj.get(UnionFind(verb.get(verbWord)));
		if(temp == null) {
			//System.out.println(UnionFind(verb.get(verbWord)) + "null value");
			return false;
			}
		
		//if obj is numeric 
		if(isNumber(objWord) && temp.contains(NUMBER)) return true; 
		
		if(!temp.contains(objWord)) {			
			//System.out.println(UnionFind(verb.get(verbWord)) + "no value");
			return false;
		}
		return true;
	}
	
	/*
	 * read saved blacklist
	 */
	public void readBlacklist() {
		FileReader fr = null;
    	BufferedReader br = null;
		try {
			fr = new FileReader(blackList);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null && !s.equals("obj")) {
				String[] data = s.split(" ");

				if(data.length < 2) continue;
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
		}catch(FileNotFoundException e){
			System.out.println("no file");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Check if it is the number.
	 */
	public boolean isNumber(String s) {
		return s.matches(".*\\d.*");
	}
	
	/*
	 * Reading text data (verb obj)
	 */
	public void readData(String fileLocate) {
		FileReader fr = null;
    	BufferedReader br = null;
		try {
			fr = new FileReader(fileLocate);
			br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				String[] data = s.split(" ");
				if(data.length < 2) continue;

				//Lemmatize words
				List<String> lem = cn.lemmatize(data[0] + " " + data[1]);
				String key = lem.get(0);		
				String value = lem.get(1);
				
				verb.put(key, null);
				
				if(obj.containsKey(key)) {
					Set<String> temp = obj.get(key);

					//if obj is numeric
					if(isNumber(value)) temp.add(NUMBER);
					else temp.add(value);
					obj.put(key,temp);
				}
				else {
					Set<String> l = new HashSet<String>();
					
					//if obj is numeric
					if(isNumber(value)) l.add(NUMBER);
					else l.add(value);
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
	public void saveData() {
		FileWriter wr = null;
    	BufferedWriter br = null;
		try {
			wr = new FileWriter(blackList);
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
	public ArrayList<String> isSynonym(String a, Set<String> set) {
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
	 * Find the root.
	 */
	public String UnionFind(String s) {
		String root = verb.get(s);
		
		if(s == root) return s;
		verb.put(s, UnionFind(root));
		
		return root;
	}
	
	/*
	 * Combine same meaning verb
	 */
	public void makeVerbList(){		
	    for (Map.Entry<String, String> entry : verb.entrySet()) {
			//if root(value) exists, don't search
	    	String key = entry.getKey();
	    	if(verb.get(key) != null) continue;
	    	
	    	ArrayList<String> synList = isSynonym(key, verb.keySet());
    		verb.put(key, key);
    		
	    	if(synList == null) {
    			continue;
	    	}
	    	for(String i : synList) {	    	
	    		//if root exists, change the root.
	    		if(verb.get(i) != null) { 
	    			verb.put(UnionFind(i), UnionFind(key));
	    			continue;
	    		}
	    	}
	    }  
	}
	
	/*
	 * Combine same meaning obj
	 */
	public void makeObjList() {
		for (Map.Entry<String, String> entry : verb.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			if(key.equals(val)) continue;
			if(obj.get(val) != null && obj.get(key) != null)obj.get(val).addAll(obj.get(key));
			obj.remove(key);
		}
	}
	
	/*
	 * Save Blacklist
	 */
	public void saveBlacklist(String dataFileLocate) {
		readData(dataFileLocate);
		makeVerbList();
		makeObjList();
		saveData();
		System.out.println("-- Finished saving blacklist!");
	}
	
}
