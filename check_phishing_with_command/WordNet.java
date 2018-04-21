/*
 * wordnet java library JWI
 * */
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.morph.WordnetStemmer;

class WordNet {
	String path = System.getProperty("user.dir") + "/dict";
	IDictionary dict = null;
	
	WordNet(){
		try {
			System.out.println(path);
			URL url = new URL("file", null, path);
			dict = new Dictionary(url);
		    dict.open();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}   
	    System.out.println("-- open dictionary");
	}
	
	ArrayList<String> getSynonyms(String strWord, POS pos){
		IIndexWord idxWord = dict.getIndexWord(strWord, pos);
		ArrayList<String> result = new ArrayList<String>();
		
		if(idxWord == null) return null;
    	
		for(IWordID i : idxWord.getWordIDs()) {
    		ISynset synset = dict.getWord(i).getSynset();
    		for(IWord w : synset.getWords()) {
    			String lemma = w.getLemma();
				if(lemma.split("_").length > 1) continue;
    			if(!result.contains(lemma)) {
    				result.add(lemma);
    			}
    		}
		}
		
		return result;
	}
	
	String getStemWord(String word) {
		WordnetStemmer Stemmer = new WordnetStemmer(dict);
	    List<String> StemmedWords;
	    String str = null;
	        
	    // null for all words, POS.NOUN for nouns
	    StemmedWords = Stemmer.findStems(word, null);
	    if (StemmedWords.isEmpty())
	    	return word;
	    
	    str = StemmedWords.iterator().next();
	    return str;
	}	
	
	/*
	 * hypernyms about w
	 */
	ArrayList<String> getHypernyms(String w, POS pos) {

		ArrayList<String> result = new ArrayList<String>();
		
		// get the synset
		IIndexWord idxWord = dict.getIndexWord (w, pos ) ;
	    
		if(idxWord == null) return result;
		
		IWordID wordID = idxWord.getWordIDs().get(0) ; // 1st meaning
		IWord word = dict.getWord ( wordID );
		ISynset synset = word.getSynset ();
		
		// get the hypernyms
		List<ISynsetID> hypernyms = synset.getRelatedSynsets(Pointer.HYPERNYM);
		
		List<IWord> words;

		for(ISynsetID sid : hypernyms) {
			words = dict.getSynset(sid).getWords();
			for(Iterator<IWord> i = words.iterator(); i.hasNext();) {
				String hyperWord = i.next().getLemma();
				System.out.println(hyperWord);
				result.add(hyperWord);
			}
		}
		
		return result;
	}
}
