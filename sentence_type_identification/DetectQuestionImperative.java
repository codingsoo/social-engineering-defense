import edu.stanford.nlp.simple.Sentence;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang.WordUtils;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

import edu.stanford.nlp.ling.SentenceUtils;
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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

public class DetectQuestionImperative {

	private static String[] specialWord;
	public static int numOfImperative = 0;
	public static int numOfNonImperative = 0;
	static JSONArray question_list = new JSONArray();
	static JSONArray imperative_list = new JSONArray();

	/*
	 * Extracting phishing keywords
	 */
	private static void searchKeyword(List<TypedDependency> tdl, List<String> verb, List<String> obj, PrintWriter pw4) {
		for (int i = 0; i < tdl.size(); i++) {
			String typeDepen = tdl.get(i).reln().toString();

			// verb
			if (verb.contains(typeDepen)) {
				pw4.println("verb :" + tdl.get(i).dep().originalText() + ">" + tdl.get(i).gov().originalText());
			}
			// obj
			if (obj.contains(typeDepen)) {
				pw4.println("obj :" + tdl.get(i).gov().originalText() + ">" + tdl.get(i).dep().originalText());
			}
		}
	}

	private static boolean isMalicious(List<TypedDependency> tdl) throws IOException {
		FileReader blfr = null;
		BufferedReader blbr = null;
		blfr = new FileReader(
				"c:/users/dyson/desktop/java_workspace/stanfordParser/sentence_tokenized_scam.json");
		blbr = new BufferedReader(blfr);
		String dobj = "";

		for (int i = 0; i < tdl.size(); i++) {
			String extractElement = tdl.get(i).reln().toString();

			if (extractElement.equals("dobj")) {
				dobj = tdl.get(i).gov().originalText().toLowerCase() + " "
						+ tdl.get(i).dep().originalText().toLowerCase();
				String value;

				while ((value = blbr.readLine()) != null) {
					if (dobj.contentEquals(value.toLowerCase())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private static boolean isQuestion(Tree parse, String sentence) {
		String POSTag = String.valueOf(parse);
		if (POSTag.contains("SBARQ") || POSTag.contains("SQ")) {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isImperative(Tree parse) {
		if (parse.firstChild().getNodeNumber(1).toString().contains("SQ")
				|| parse.firstChild().getNodeNumber(1).toString().contains("SBARQ")) {
			return false;
		}
		TregexPattern noNP = TregexPattern.compile("((@VP=verb > (S !> SBAR)) !$,,@NP)");
		TregexMatcher n = noNP.matcher(parse);
		while (n.find()) {
			String match = n.getMatch().firstChild().label().toString();

			// remove gerund, to + infinitive
			if (match.equals("VP")) {
				match = n.getMatch().firstChild().firstChild().label().toString();
			}
			if (match.equals("TO") || match.equals("VBG")) {
				n.find();
				continue;
			}
			return true;
		}
		return false;
	}

	/* Change one word to lowerase. */
	private static String extractOneWord(int num, ArrayList<TaggedWord> listedTaggedString) {
		return listedTaggedString.get(num).toString().toLowerCase();
	}

	/* Determine if it is a suggestion or not by using string match. */
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
					return true;
				}
			} else if (extractOneWord(i, listedTaggedString).contentEquals("would/md")) {
				if (extractOneWord(i + 1, listedTaggedString).contentEquals("like/vb")) {
					return true;
				} else if (i != 0 && extractOneWord(i - 1, listedTaggedString).contentEquals("you/prp")) {
					return true;
				}
			} else if (extractOneWord(i, listedTaggedString).contentEquals("'d/md")) {
				if (extractOneWord(i + 1, listedTaggedString).contentEquals("like/md")) {
					return true;
				} else if (i != 0 && extractOneWord(i - 1, listedTaggedString).contentEquals("you/prp")) {
					return true;
				}
			}
		}
		return false;
	}

	/* Determine if it is a suggestion or not by using regular expression. */
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

	/* Determine if it is a desire expression or not by using string match. */
	private static boolean isDesireExpression(List<TypedDependency> tdl) {
		for (int i = 0; i < tdl.size(); i++) {
			String extractElement = tdl.get(i).reln().toString();
			String oneWord = tdl.get(i).gov().value().toString().toLowerCase();
			if (extractElement.equals("nsubj")) {
				if (oneWord.contains("want") || oneWord.equals("hope") || oneWord.equals("wish")
						|| oneWord.equals("desire")) {
					return true;
				}
			}
		}
		return false;
	}

	/* Determine if it is a malicious sentence or not by using blacklist. */
	private static void judgeMaliciousSentence(LexicalizedParser lp, String sentence) throws IOException {
		String blacklistFileName = "c:/users/dyson/desktop/java_workspace/stanfordParser/maliciousSentencesOnlyOBJ/ouput_Imperative_004_verb_obj.txt";
		PrintWriter blacklist = new PrintWriter(new FileWriter(blacklistFileName, true));
	}

	private static boolean detectCommand(LexicalizedParser lp, String sentence, JSONArray imperative_listz, JSONArray question_listz) throws IOException {
		// if the sentence has only one word or too many words, go to the next sentence.
		if (sentence.split(" ").length < 2 || sentence.split(" ").length > 50) {
			return false;
		}

		// penn tree
		try {
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

			// 0. Hope, Kindly, Apply, Reply exception process
			for (int i = 0; i < 4; i++) {
				if (sentence.toLowerCase().startsWith(specialWord[i])) {
					imperative_listz.add(sentence);
					
					return true;
				}
			}

			// 1. extracting imperative sentence
			if (isQuestion(parse, sentence)) {
				question_listz.add(sentence);
				
				return true;
			} else if (isImperative(parse)) {
				imperative_listz.add(sentence);
				
				return true;
			}

			// 2. extracting suggestion sentence
			else if (isSuggestion(lp, sentence, listedTaggedString)) {
				imperative_listz.add(sentence);
				
				return true;
			}

			// 3. extracting sentence including desire expression
			else if (isDesireExpression(tdl)) {
				// ¿å¸Á
				imperative_listz.add(sentence);
				
				return true;
			} else {
				
				return false;
			}

		} catch (OutOfMemoryError e) {
			System.err.println(
					"WARNING: Parsing of sentence ran out of memory.  " + "Will ignore and continue: " + sentence);
		}
		return false;
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

	public static void main(String[] args) throws IOException, ParseException {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		// String fileName = System.getProperty("user.dir") + "\\src\\data";

		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

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

				switch (inputMethod) {

				// standard text input
				case 1:
					PrintWriter pw3 = new PrintWriter(new FileWriter(
							"c:/users/dyson/desktop/java_workspace/stanfordParser/output_Imperative.txt", true));
					while (scanner.hasNext()) {
						String value = scanner.nextLine();
						//detectCommand(lp, value);
					}
					pw3.close();
					break;

				// text input file
				case 2:
					FileReader fr = null;
					BufferedReader br = null;
					try {
						fr = new FileReader(
								"c:/users/dyson/desktop/java_workspace/stanfordParser/imperatives.txt");
						br = new BufferedReader(fr);

						String value;

						while ((value = br.readLine()) != null) {
							value = WordUtils.capitalizeFully(value, new char[] { '.' });
							
							// Judge the sentence is imperative sentence or not.
							/*if(detectCommand(lp, value)) {
								System.out.println(value + " ==> this is malicious");
							}
							else {
								System.out.println(value + " ==> this is not malicious");
							}*/
						}
					} catch (IOException e) {
						e.printStackTrace();
					} finally {
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
						int count = 0;

						JSONParser parser = new JSONParser();
						JSONArray scam_email = (JSONArray) parser.parse(new FileReader("sentence_tokenized_scam.json"));

						for (Object content : scam_email){
				            count = count + 1;
				            if(count % 500 == 0)
				            	System.out.println(count);
				            
				        	JSONArray question_listz = new JSONArray();
				        	JSONArray imperative_listz = new JSONArray();
				        	
				            for(Object scam_sentence : (JSONArray)content ){
				            	try {
					            	scam_sentence = ((String) scam_sentence).replaceAll("`", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("~", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("!", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("@", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("#", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("[$]", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("%", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("\\^", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("&", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("[*]", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("\\(", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("\\)", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("-", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("_", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("=", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("[+]", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("\\{", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("\\}", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("\\[", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("\\]", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll("<", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll(">", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll(":", " ");
				                    scam_sentence = ((String) scam_sentence).replaceAll(";", " ");
				                    Sentence sent = new Sentence((String) scam_sentence);
				                    String value = sent.toString();
				                    value = WordUtils.capitalizeFully(value, new char[] { '.' });
				                    detectCommand(lp, value, imperative_listz, question_listz);
				            	} catch (Exception e){
				                    System.out.println(e);
				                }
							}
				            imperative_list.add(imperative_listz);
				            question_list.add(question_listz);
						}
						try {

				            FileWriter file = new FileWriter("output_question.json");
				            file.write(question_list.toJSONString());
				            file.flush();
				            file.close();
				            FileWriter file2 = new FileWriter("output_Imperative.json");
				            file2.write(imperative_list.toJSONString());
				            file2.flush();
				            file2.close();

				        } catch (IOException e) {
				            e.printStackTrace();
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
	}

}