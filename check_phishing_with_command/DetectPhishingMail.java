import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class DetectPhishingMail {
	public static boolean detectCommand(LexicalizedParser lp, String sentence) {
		// penn tree
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sentence));
		List<CoreLabel> rawWords = tok.tokenize();
		Tree parse = lp.apply(rawWords);

		// dependency
		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

		// 1. extracting imperative sentence
		TregexPattern noNP = TregexPattern.compile("((@VP=verb > (S !> SBAR)) !$,,@NP)");
		TregexMatcher n = noNP.matcher(parse);
		System.out.print("<<<" + sentence);
		while (n.find()) {
			String match = n.getMatch().firstChild().label().toString();

			// remove gerund, to + infinitiv
			if (match.equals("VP")) {
				match = n.getMatch().firstChild().firstChild().label().toString();
			}
			if (match.equals("TO") || match.equals("VBG")) {
				continue;
			}

			//n.getMatch().pennPrint();
			System.out.println(">>>");
			
			// imperative sentence
			System.out.println("It is imperative sentence.");
			System.out.println();
			return true;
		}
		System.out.println(">>>");
		return false;
	}
	
	public static void printObjVerb(List<TypedDependency> tdl) throws IOException  {
		for (int i = 0; i < tdl.size(); i++) { // System.out.println(tdl.get(i));
			String extractElement = tdl.get(i).reln().toString();
			if (extractElement.equals("dobj")) {
				System.out.println("<Object : " + tdl.get(i).dep().value() + "> "
						+ "<Verb : " + tdl.get(i).gov().value() + ">");
			}
		}
	}

	public static void detectSuggestDesire(LexicalizedParser lp, String sentence) throws IOException {

		TreebankLanguagePack tlp = lp.treebankLanguagePack(); // a PennTreebankLanguagePack for English
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();

		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(sentence));
		List<CoreLabel> rawWords = tok.tokenize();
		Tree parse = lp.apply(rawWords);

		ArrayList<TaggedWord> listedTaggedString = parse.taggedYield();
		GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
		List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();

		if (parse.firstChild().getNodeNumber(1).toString().contains("SQ") ||
				parse.firstChild().getNodeNumber(1).toString().contains("SBARQ")) {
			System.out.println();
			return;
		} else {
			// Judge the suggestion sentence
			for (int i = 0; i < listedTaggedString.size() - 1; i++) {
				if (listedTaggedString.get(i).toString().toLowerCase().contentEquals("should/md")
						|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("could/md")
						|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("might/md")
						|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("may/md")
						|| listedTaggedString.get(i).toString().toLowerCase().contentEquals("must/md")
						|| (listedTaggedString.get(i).toString().toLowerCase().contentEquals("have/vbp")
								&& listedTaggedString.get(i + 1).toString().toLowerCase().contentEquals("to/to"))) {
					if (i != 0 && listedTaggedString.get(i - 1).toString().toLowerCase().contentEquals("you/prp")) {
						System.out.println("It is suggestion.");
						printObjVerb(tdl);
						System.out.println();
						return;
					}
				}
				else if(listedTaggedString.get(i).toString().toLowerCase().contentEquals("would/md")) {
					if(listedTaggedString.get(i+1).toString().toLowerCase().contentEquals("like/md")) {
						System.out.println("It is desire.");
						System.out.println();
						return;
					}
					else {
						System.out.println("It is suggestion.");
						System.out.println();
						return;
					}
				}
				else if(listedTaggedString.get(i).toString().toLowerCase().contentEquals("'d/md")) {
					if(listedTaggedString.get(i+1).toString().toLowerCase().contentEquals("like/md")) {
						System.out.println("It is desire.");
						System.out.println();
						return;
					}
					else {
						System.out.println("It is suggestion.");
						System.out.println();
						return;
					}
				}
			}

			// Judge the desire sentence
			for (int i = 0; i < tdl.size(); i++) {
				String extractElement = tdl.get(i).reln().toString();
				if (extractElement.equals("nsubj")) {
					if (tdl.get(i).gov().value().toString().toLowerCase().equals("want")
							|| tdl.get(i).gov().value().toString().toLowerCase().equals("hope")
							|| tdl.get(i).gov().value().toString().toLowerCase().equals("wish")
							|| tdl.get(i).gov().value().toString().toLowerCase().equals("desire")) {
						System.out.println("It is desire sentence.");
						System.out.println();
						return;
						//printObjVerb(tdl);
					}
				}
			}
		}
		System.out.println();
	}

	public static void main(String[] args) {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
		String fileName = System.getProperty("user.dir") + "\\src\\data";

		LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);

		if (args.length == 0) {
			Scanner scanner = null;
			try {
				scanner = new Scanner(System.in);
				System.out.println("Select the input method\n 1: text input 2: text File 3:JSON File  >> ");
				int inputMethod = scanner.nextInt();

				switch (inputMethod) {

				// standard text input
				case 1:
					while (scanner.hasNext()) {
						String value = scanner.nextLine();
						detectCommand(lp, value);
					}
					break;

				// text input file
				case 2:
					FileReader fr = null;
					BufferedReader br = null;
					try {
						fr = new FileReader("c:/users/dyson/desktop/java_workspace/stanfordParser/imperatives.txt");
						br = new BufferedReader(fr);

						String value;
						while ((value = br.readLine()) != null) {
							if(!detectCommand(lp, value)) {
								detectSuggestDesire(lp, value);
							}
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
					JSONParser parser = new JSONParser();

					try {
						Object file = parser.parse(new FileReader("c:/Users/dyson/Desktop/java_workspace/stanfordParser/sentence_tokenized_scam2.json"));
						JSONObject jsonSpamData = (JSONObject) file;

						for (Object key : jsonSpamData.keySet()) {
							ArrayList<String> SpamSentences = ((ArrayList<String>) jsonSpamData.get((String) key));
							for (String value : SpamSentences) {
								if(!detectCommand(lp, value)) {
									detectSuggestDesire(lp, value);
								}
							}
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (ParseException e) {
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
