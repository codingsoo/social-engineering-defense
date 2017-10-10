/*
 * wordnet java library JWI
 * */
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

class WordNet {
	String path = System.getProperty("user.dir") + "\\src\\dict";
    IDictionary dict = null;
	
	WordNet(){
		try {
			URL url = new URL("file", null, path);
			dict = new Dictionary(url);
		    dict.open();
		    
		    System.out.println("open dictionary");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
	}
	

	/*
	 * hypernyms about w
	 */
	List<String> getHypernyms(String w) {

		List<String> result = new ArrayList<String>();
		
		// get the synset
		IIndexWord idxWord = dict.getIndexWord (w, POS . NOUN ) ;
	    
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
