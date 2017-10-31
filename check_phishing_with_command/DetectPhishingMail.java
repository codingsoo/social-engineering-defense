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
	private static String[] specialWord;
	private static int save_mode;
	private static PrintWriter pw2;
	
	private static String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private static String fileLocate = System.getProperty("user.dir") + "\\src\\";
	
	private static CoreNLP cn = new CoreNLP(); 	//Use Wordnet with jwi
	private static MakeBlacklist BL = new MakeBlacklist(fileLocate + "result.txt");  //Manage blacklist

	
	/*
	 * Check if a single pair of verb and obj is included the pair in blacklist
	 */
	private static boolean IsBlackListPair(String verb, String obj) {
		return BL.checkBlacklist(verb, obj);
	}

	/*
	 * Check if a sentence is included the words in blacklist
	 */
	private static boolean IsBlackListSent(List<TypedDependency> tdl, String sentence, List<String> obj, List<String> extVerb) {
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
	 * Check if the parse if imperative
	 * Return : root verbs
	 */
	private static List<String> isImperative(Tree parse) {
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

	/*
	 * Find the sentence formed "You + moral"
	 */
	private static boolean isSuggestion(Tree parse) {
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
	 * Detect the command line.
	 */
	private static boolean detectCommand(LexicalizedParser lp, String sentence) throws IOException {
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

		
		//Extract all kind of imperative sentence.
		
		// Hope, Kindly, Apply, Reply exception process
		for (int i = 0; i < 4; i++) {
			if (sentence.toLowerCase().startsWith(specialWord[i])) {
				IsBlackListSent(tdl, sentence, Arrays.asList("dobj"), Arrays.asList(specialWord));
				return true;
			}
		}

		// extracting imperative sentence
		List<String> imperVerb = isImperative(parse);
		if (!imperVerb.isEmpty()) {
			IsBlackListSent(tdl, sentence, Arrays.asList("dobj","nmod","xcomp"),imperVerb);
			return true;
		}

		// extracting suggestion sentence and desire expression sentence
		if (isSuggestion(lp, sentence, listedTaggedString) || isDesireExpression(tdl)) {
			IsBlackListSent(tdl, sentence, Arrays.asList("dobj"),imperVerb);
			return true;
		}
		return false;
	}

	/*
	 * Read sentence through JsonReader
	 */
	public static List<String> readJsonArray(JsonReader reader) throws IOException {
		List<String> contents = new ArrayList<String>();

		reader.beginArray();
		while (reader.hasNext()) {
			contents.add(reader.nextString());
		}
		reader.endArray();
		return contents;
	}

	
	public static void main(String[] args){
		String fileName = "non_malicious";
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
				System.out.println("Select the input method\n 1: text input 2: text File 3:JSON File 4:Make Black List >> ");
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
		
				if(save_mode > 1) {
					try {
						pw2 = new PrintWriter(new FileWriter(
								fileLocate + "result_"+fileName + "_wrong.txt", true));

					} catch (IOException e1) {
						System.out.println("io error");
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
							System.out.println("io error");
							e.printStackTrace();
						}
					}
					break;

				// text input file
				case 2:
					FileReader fr = null;
					BufferedReader br = null;
					try {
				    	fr = new FileReader(fileLocate + fileName + ".txt"); 
						
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
			    		
						reader.beginObject();
						while (reader.hasNext()) {
							String name = reader.nextName();
							List<String> sentences = readJsonArray(reader);
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
				case 4:
					BL.saveBlacklist(fileLocate + "data.txt", fileLocate + "result.txt");
					break;
				default:
					System.out.println("wrong input");
				}
			} finally {
				if (scanner != null)
					scanner.close();
			}
		}
		if(save_mode == 2) {
			pw2.close();
		}
	}

}
