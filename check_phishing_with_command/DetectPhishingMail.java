import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.WordUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.File;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

public class DetectPhishingMail {
	private String fileLocate;
	private String[] specialWord;
	private PrintWriter writer;
	private PrintWriter wrong_writer;
	
	private LexicalizedParser lp;	
	private String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	
	private CoreNLP cn; 	
	private MakeBlacklist BL;

	private int rCount,wCount;

	DetectPhishingMail(){
		rCount = 0;
		wCount = 0;
		
		writer = null;
		wrong_writer = null;
		
		specialWord = new String[4];
		specialWord[0] = "hope";
		specialWord[1] = "reply";
		specialWord[2] = "apply";	
		specialWord[3] = "kindly";
		
		lp = LexicalizedParser.loadModel(parserModel);
		fileLocate = System.getProperty("user.dir") + "\\src\\";
		
		//Use Wordnet with jwi
		cn = new CoreNLP();
		 
		//Manage blacklist
		BL = new MakeBlacklist(fileLocate + "result.txt");  
	}
	/*
	 * Check if a single pair of verb and obj is included the pair in blacklist
	 */
	private boolean IsBlackListPair(String verb, String obj) {
		return BL.checkBlacklist(verb, obj);
	}

	/*
	 * Check if a sentence is included the words in blacklist
	 */
	private boolean IsBlackListSent(List<TypedDependency> tdl, String sentence, List<String> obj, List<String> extVerb) {
	    for(int i = 0; i < tdl.size(); i++) {
			TypedDependency tdl_i = tdl.get(i);
	    	String typeDepen = tdl_i.reln().toString();
	    	
	    	//index 0 is just root, for matching index with sentence
	    	if(tdl_i.gov().index() == 0) continue;	    	   		
	    	
	    	//Extract the keyword pair (verb,obj)
	    	if( obj.contains(typeDepen) ) {
	    		
	    		//Check only extracted verb when the type dependency is nmod or xcomp
	    		if(( typeDepen.equals("nmod") || typeDepen.equals("xcomp") ) && !extVerb.contains(tdl_i.gov().originalText())) {
	    			continue;
	    		}
	    		
	    		//Lemmatize sentence
		    	List<String> lem = cn.lemmatize(sentence);
		    	String verbWord = lem.get(tdl_i.gov().index()-1);
		    	String objWord = lem.get(tdl_i.dep().index()-1);
	    		
	    		//Judge if this sentence is malicious
	    		if(IsBlackListPair(verbWord, objWord)) return true;
	    	}
	    }
		return false;
	}

	/*
	 * Check if the parse is imperative
	 * Return : root verbs
	 */
	private List<String> isImperative(Tree parse) {
		TregexPattern noNP = TregexPattern.compile("((@VP=verb > (S !> SBAR)) !$,,@NP)");
		TregexMatcher n = noNP.matcher(parse);
		ArrayList<String> VerbSet = new ArrayList<String>();
				
		while (n.find()) {
			String match = n.getMatch().firstChild().label().toString();
			Tree temp = n.getMatch().firstChild().firstChild();
			
			// remove gerund, to + infinitiv
			if (match.equals("VP")) {
				match = temp.label().toString();
			}
			if (match.equals("TO") || match.equals("VBG")) {
				n.find();
				continue;
			}
			
			//Find the last node within overlapped "VP" nodes. 
			while(temp.firstChild() != null) {
				temp = temp.firstChild();
			}
			
			//Store root verbs
			VerbSet.add(temp.toString());
		}
		return VerbSet;
	}

	private String extractOneWord(int num, ArrayList<TaggedWord> listedTaggedString) {
		return listedTaggedString.get(num).toString().toLowerCase();
	}

/*
 * 주석 달아주세용
 *  you + moral
 */
	private boolean isSuggestion(LexicalizedParser lp, String sentence,
			ArrayList<TaggedWord> listedTaggedString) {
		//
		for (int i = 0; i < listedTaggedString.size() - 1; i++) {
			if (extractOneWord(i, listedTaggedString).contentEquals("should/md")
					|| extractOneWord(i, listedTaggedString).contentEquals("could/md")
					|| extractOneWord(i, listedTaggedString).contentEquals("might/md")
					|| extractOneWord(i, listedTaggedString).contentEquals("may/md")
					|| extractOneWord(i, listedTaggedString).contentEquals("must/md")
					|| (extractOneWord(i, listedTaggedString).contentEquals("have/vbp")
							&& extractOneWord(i + 1, listedTaggedString).contentEquals("to/to"))) {
				if (i != 0 && extractOneWord(i - 1, listedTaggedString).contentEquals("you/prp")) {
				//	System.out.println("It is suggestion.");
					return true;
				}
			} else if (extractOneWord(i, listedTaggedString).contentEquals("would/md")) {
				if (extractOneWord(i + 1, listedTaggedString).contentEquals("like/vb")) {
				//	System.out.println("It is desire.");
					return true;
				} else if (i != 0 && extractOneWord(i - 1, listedTaggedString).contentEquals("you/prp")) {
				//	System.out.println("It is suggestion.");
					return true;
				}
			} else if (extractOneWord(i, listedTaggedString).contentEquals("'d/md")) {
				if (extractOneWord(i + 1, listedTaggedString).contentEquals("like/md")) {
				//	System.out.println("It is desire.");
					return true;
				} else if (i != 0 && extractOneWord(i - 1, listedTaggedString).contentEquals("you/prp")) {
				//	System.out.println("It is suggestion.");
					return true;
				}
			}
		}
		return false;
	}

	/*
	 * Find the sentence formed "You + moral"
	 */
	private boolean isSuggestion(Tree parse) {
		TregexPattern sug = TregexPattern.compile("((@VP=md > S ) $,,@NP=you )");
		TregexMatcher s = sug.matcher(parse);

		while (s.find()) {
			String y = s.getNode("you").getChild(0).getChild(0).value();

			if (y.equals("you") || y.equals("You") || y.equals("YOU")) {
				return true;
			}
		}
		return false;
	}
	
/*
 * 주석달
 * including desire verb
 */
	private boolean isDesireExpression(List<TypedDependency> tdl) {
		for (int i = 0; i < tdl.size(); i++) {
			String extractElement = tdl.get(i).reln().toString();
			String oneWord = tdl.get(i).gov().value().toString().toLowerCase();
			if (extractElement.equals("nsubj")) {
				if (oneWord.contains("want") || oneWord.equals("hope") || oneWord.equals("wish")
						|| oneWord.equals("desire")) {
					//System.out.println("It is desire sentence.");
					return true;
					// printObjVerb(tdl);
				}
			}
		}
		return false;
	}

	/*
	 * Detect the command line.
	 */
	private boolean detectCommand(LexicalizedParser lp, String sentence) throws IOException {
		int sentLen = sentence.split(" ").length;
		// if the sentence has only one word, go to the next sentence.
		if (sentLen > 50 || sentLen < 2) {
			return false;
		}
		
		// Penn tree
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sentence));
		List<CoreLabel> rawWords = tok.tokenize();
		Tree parse = lp.apply(rawWords);
		ArrayList<TaggedWord> listedTaggedString = parse.taggedYield();

		// Dependency
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

		/*
		* Extract all kind of imperative sentence.
		*/
		// Hope, Kindly, Apply, Reply exception process
		for (int i = 0; i < 4; i++) {
			if (sentence.toLowerCase().startsWith(specialWord[i])) {
				return IsBlackListSent(tdl, sentence, Arrays.asList("dobj"), Arrays.asList(specialWord));
			}
		}

		// extracting imperative sentence
		List<String> imperVerb = isImperative(parse);
		if (!imperVerb.isEmpty()) {
			return IsBlackListSent(tdl, sentence, Arrays.asList("dobj","nmod","xcomp"),imperVerb);
		}

		// extracting suggestion sentence and desire expression sentence
		if (isSuggestion(lp, sentence, listedTaggedString) || isDesireExpression(tdl)) {
			return IsBlackListSent(tdl, sentence, Arrays.asList("dobj"),imperVerb);
		}
		return false;
	}
	
	
	/*
	 * You can check or write something with result.
	 */
	private boolean checkMalicious(boolean result, String sent) {
		if(result) {
			rCount++;
			if(writer != null) {
				/* ************************ */
				writer.println(sent + '\n');
			}
			return true;
		}
		else {
			wCount++;
			if(wrong_writer != null) {
				/* ************************ */
				wrong_writer.println(sent + '\n');
			}
			return false;
		}
	}
	
	
	/*
	 * Read sentence through JsonReader
	 */
	public List<String> readJsonArray(JsonReader reader) throws IOException {
		List<String> contents = new ArrayList<String>();

		reader.beginArray();
		while (reader.hasNext()) {
			contents.add(reader.nextString());
		}
		reader.endArray();
		return contents;
	}
	
	/*
	 * Read sentences from user input. 
	 */
	public void readTextLine() {
		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			String value = scanner.nextLine();
			try {
				if(value.equals("exit")) return;
				checkMalicious(detectCommand(lp, value), value);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		scanner.close();
	}
	
	/*
	 * Read sentences from json form 
	 */
	public void readJsonFile(String fileName) {
		try {
			JsonReader reader = new JsonReader(new FileReader(fileLocate + fileName));
    		
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				List<String> sentences = readJsonArray(reader);
				for (String value : sentences) {
					value = WordUtils.capitalizeFully(value, new char[] { '.' });
					//System.out.println(++count);
					checkMalicious(detectCommand(lp, value), value);
					}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Read sentences from text file 
	 */
	public void readTextFile(String fileName) {
		FileReader fr = null;
		BufferedReader br = null;
		int  count = 0;
		try {
	    	fr = new FileReader(fileLocate + fileName); 
			
	    	br = new BufferedReader(fr);
			String value;
			while ((value = br.readLine()) != null) {
				value = WordUtils.capitalizeFully(value, new char[] { '.' });
				
				if(count++ % 100 == 0) System.out.println(count);
				
				try {
					checkMalicious(detectCommand(lp, value), value);
			
				}
				catch(OutOfMemoryError e) {
					continue;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (br != null)
					br.close();
				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	/*
	 *word data file : verb  obj
	 *BL file :  Black List file
	 *sent Data File : sentences which want to check if it is phishing data.
	 *result File : writer File
	 */
	public void check(String wordDataFile, String BLfile, String sentDataFile, String resultFile ){

		//Make BlackList Mode
		if(wordDataFile != null) {
			BL.saveBlacklist(fileLocate + wordDataFile, fileLocate + BLfile);
		}
				
		// Save Mode
		if(resultFile != null) {
			try {
				System.out.println("-- save mode");
				File f = new File(fileLocate + resultFile);
				f.createNewFile();
				writer = new PrintWriter(new FileWriter(f));

				File wf = new File(fileLocate + "wrong" + resultFile);
				wf.createNewFile();
				wrong_writer = new PrintWriter(new FileWriter(wf));
			} catch (IOException e1) {
				System.out.println("io error");
				e1.printStackTrace();
			}
		}
		//line input mode
		if(sentDataFile == null) {
			System.out.println(" test sentence >> ");
			readTextLine();
			return;
		}
				
		//json input file
		if(sentDataFile.endsWith("json")){
			System.out.println("-- json File");
			readJsonFile(sentDataFile);				
			return;
		}
		
		//text input file
		if(sentDataFile.endsWith("txt")) {
			System.out.println("-- text File");
			readTextFile(sentDataFile);	
			return;
		}
		
		if(writer != null) {
			/* ************************ */
			writer.println(rCount + ' ' + wCount);
			writer.close();
			wrong_writer.close();
		}
	}

	public static void main(String[] args){
		DetectPhishingMail d = new DetectPhishingMail();
		
		//verb+obj File or null , Blacklist File , json or txt or null (input) , result or null 
		d.check(null,"result.txt","malicious.txt","new_result.txt");
	}
}
