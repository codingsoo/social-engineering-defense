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
	private static DBConnection db;
	private static String[] specialWord;
	private static int save_mode;
	private static PrintWriter pw2;
	private static CoreNLP cn = new CoreNLP();
	private static MakeBlacklist BL = new MakeBlacklist();
	
	
	private static boolean checkDBBlacklist(String verb, String obj) {
		return db.DBcheck("blacklist", verb, obj);
	}
	private static boolean checkBlacklist(String verb, String obj) {
		if(BL.checkBlacklist(verb, obj)) {
			System.out.println("Spam mail!");
			return true;
		}
		else {
			System.out.println("nope");
			return false;
		}
	}


	/*
	 * Extracting phishing keywords
	 */
	private static void searchKeyword(List<TypedDependency> tdl, String sentence, List<String> verb ,List<String> obj, String extVerb) {
	    ArrayList<String> verbList = new ArrayList<String>(), objList = new ArrayList<String>();
		System.out.println("command!");
	    if(save_mode == 2) {
	    	pw2.println(sentence);
	    	pw2.println();
	    }
	
	    
		for(int i = 1; i < tdl.size(); i++) {
			TypedDependency tdl_i = tdl.get(i);
	    	String typeDepen = tdl_i.reln().toString();
	    	String subjWord = null, verbWord = null, objWord = null;
	    	   		
	    	//subj + verb
	    	if( verb.contains(typeDepen) ){
	    		//lemmatize words
		    	List<String> lem = cn.lemmatize(sentence);
		    	String govRoot = lem.get(tdl_i.gov().index()-1), depRoot = lem.get(tdl_i.dep().index()-1);
	    		
	    		subjWord = depRoot;
	    		verbWord = govRoot;
	    	}
	    	//verb + obj
	    	if( obj.contains(typeDepen) ) {
	    		
	    		if(( typeDepen.equals("nmod") || typeDepen.equals("xcomp") ) && !extVerb.equals(tdl_i.gov().originalText())) {
	    			continue;
	    		}
	    		//lemmatize words
		    	List<String> lem = cn.lemmatize(sentence);
		    	String govRoot = lem.get(tdl_i.gov().index()-1), depRoot = lem.get(tdl_i.dep().index()-1);
	    		
	    		verbWord = govRoot;
	    		objWord = depRoot;
	    		if(save_mode == 2)	pw2.println("verb > " + verbWord + " obj > " + objWord);
	    		
	    		System.out.println(verbWord + " " + objWord);
	    		verbList.add(verbWord);
	    		objList.add(objWord);
	    		
	    		checkBlacklist(verbWord, objWord);
	    	}
	    }
		
		//insert verb and object to table "inputword"
		if(save_mode == 1) db.DBadd("inputword",verbList, objList);
	}

	/*
	 * Extracting command sentence
	 */
	// "(@VP=verb (< S !> SBAR) !$,,@NP)"
	// (@VP=verb (< S !> SBAR) | (< S & > S & < NN) !$,,@NP)
	private static String isImperative(Tree parse) {
		TregexPattern noNP = TregexPattern.compile("((@VP=verb > (S !> SBAR)) !$,,@NP)");
		TregexMatcher n = noNP.matcher(parse);
		
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
			
			while(temp.firstChild() != null) {
				temp = temp.firstChild();
			}
			
			// imperative sentence
			//System.out.println("It is imperative sentence.");
			return temp.toString();
		}
		return null;
	}

	private static String extractOneWord(int num, ArrayList<TaggedWord> listedTaggedString) {
		return listedTaggedString.get(num).toString().toLowerCase();
	}

	/*
	 *  you + moral
	 */
	private static boolean isSuggestion(LexicalizedParser lp, String sentence,
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

	private static boolean isSuggestion(Tree parse) {
		TregexPattern sug = TregexPattern.compile("((@VP=md > S ) $,,@NP=you )");
		TregexMatcher s = sug.matcher(parse);

		while (s.find()) {
			String y = s.getNode("you").getChild(0).getChild(0).value();

			if (y.equals("you") || y.equals("You") || y.equals("YOU")) {
				//System.out.println("It is suggestion sentence.");
				return true;
			}
		}
		return false;
	}
/*
 * including desire verb
 */
	private static boolean isDesireExpression(List<TypedDependency> tdl) {
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
	 * detecting command line.
	 */
	private static void detectCommand(LexicalizedParser lp, String sentence) throws IOException {
		int sentLen = sentence.split(" ").length;
		// if the sentence has only one word, go to the next sentence.
		if (sentLen > 50 || sentLen < 2) {
			return;
		}
		
		// penn tree
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sentence));
		List<CoreLabel> rawWords = tok.tokenize();
		Tree parse = lp.apply(rawWords);
		ArrayList<TaggedWord> listedTaggedString = parse.taggedYield();

		// dependency
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

		//System.out.println("<<< " + sentence + " >>>");

		// 0. Hope, Kindly, Apply, Reply exception process
		for (int i = 0; i < 4; i++) {
			if (sentence.toLowerCase().startsWith(specialWord[i])) {
				searchKeyword(tdl, sentence,Arrays.asList("nsubj", "subjpass"), Arrays.asList("dobj"), "");
				return;
			}
		}

		// 1. extracting imperative sentence
		String imperVerb = isImperative(parse);
		if (imperVerb != null) {
			searchKeyword(tdl, sentence,Arrays.asList( "nsubj", "subjpass"), Arrays.asList("dobj","nmod","xcomp"),imperVerb);
			
		}

		// 2. extracting suggestion sentence
		else if (isSuggestion(lp, sentence, listedTaggedString)) {
			searchKeyword(tdl, sentence,Arrays.asList("nsubj", "subjpass"), Arrays.asList("dobj"),"");
		}

		// 3. extracting sentence including desire expression
		else if (isDesireExpression(tdl)) {
			// ¿å¸Á
			searchKeyword(tdl, sentence,Arrays.asList("nsubj", "subjpass"), Arrays.asList("dobj"),"");
		}
		//System.out.println();
	}

	public static List<String> readArray(JsonReader reader) throws IOException {
		List<String> contents = new ArrayList<String>();

		reader.beginArray();
		while (reader.hasNext()) {
			contents.add(reader.nextString());
		}
		reader.endArray();
		return contents;
	}

	public static void main(String[] args){
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		String fileName = System.getProperty("user.dir") + "\\src\\data";
		String number = "1000";
		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);


		int count = 0;
		specialWord = new String[4];
		specialWord[0] = "hope";
		specialWord[1] = "reply";
		specialWord[2] = "apply";	
		specialWord[3] = "kindly";

		if (args.length == 0) {
			Scanner scanner = null;
			try {
				scanner = new Scanner(System.in);
				System.out.println("Select the input method\n 1: text input 2: text File 3:JSON File  >> ");
				int inputMethod = scanner.nextInt();
				
				while(true) {
					
					try {
						System.out.println("0: non-save mode  1: data save mode  2: text file save mode >> ");
						save_mode = scanner.nextInt();
					} catch(Exception e) {
						scanner.nextLine();
						continue;
					}
					break;
				}
				if(save_mode == 1) db = new DBConnection();
		
				if(save_mode == 2) {
					try {
						pw2 = new PrintWriter(new FileWriter(
								fileName + "result_" + number + ".txt", true));
//						pw2 = new PrintWriter(new FileWriter(
//						"c:/users/dyson/desktop/java_workspace/stanfordParser/extract_imperative_command.txt", true));

					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				
				switch (inputMethod) {

				// standard text input
				case 1:
					while (scanner.hasNext()) {
						String value = scanner.nextLine();
						try {
							detectCommand(lp, value);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					break;

				// text input file
				case 2:
					FileReader fr = null;
					BufferedReader br = null;
					try {
				    	fr = new FileReader(fileName + number + ".txt"); 
						
				    	//fr = new FileReader("C:/Users/kimhyeji/Downloads/stanford-parser-full-2017-06-09/stanford-parser-full-2017-06-09/src/result1_extract_none_line.txt");
				    	br = new BufferedReader(fr);
						String value;
						while ((value = br.readLine()) != null) {
							value = WordUtils.capitalizeFully(value, new char[] { '.' });
							// reply, hope,
							if(count % 100 == 0) System.out.println(count);
							count++;
							try {
								detectCommand(lp, value);
						
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
					break;

				// json input file
				case 3:

					try {
						JsonReader reader = new JsonReader(new FileReader(fileName + ".json"));
			    		
						//JsonReader reader = new JsonReader(new FileReader(
						//		"c:/Users/dyson/Desktop/java_workspace/stanfordParser/sentence_tokenized_scam2.json"));
						Gson gson = new GsonBuilder().create();
						reader.beginObject();
						while (reader.hasNext()) {
							String name = reader.nextName();
							List<String> sentences = readArray(reader);
							for (String value : sentences) {
								value = WordUtils.capitalizeFully(value, new char[] { '.' });
								//System.out.println(++count);
								detectCommand(lp, value);
							}
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					break;

				default:
					System.out.println("wrong input");
				}
			} finally {
				if (scanner != null)
					scanner.close();
			}
		}
		pw2.close();
	}

}
