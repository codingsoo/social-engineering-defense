import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;

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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



public class DetectSpam{
	public static void detectCommand(LexicalizedParser lp, String sentence) {		   
		
		//penn tree
		TokenizerFactory<CoreLabel> tokenizerFactory =
	        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	    Tokenizer<CoreLabel> tok =
	        tokenizerFactory.getTokenizer(new StringReader(sentence));
	    List<CoreLabel> rawWords = tok.tokenize();
	    Tree parse = lp.apply(rawWords);

	    //dependency
	    TreebankLanguagePack tlp = lp.treebankLanguagePack(); // PennTreebankLanguagePack for English
	    GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	    GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	    List<TypedDependency> tdl = gs.typedDependenciesCCprocessed();
	   
	    //1. extracting imperative sentence
	    TregexPattern noNP = TregexPattern.compile("((@VP=verb > (S !> SBAR)) !$,,@NP)");
	    TregexMatcher n = noNP.matcher(parse);
	    System.out.print( "<<<" + sentence );
	    while(n.find()) {
	    	String match = n.getMatch().firstChild().label().toString();
	    	
	    	//remove gerund, to + infinitiv
	    	if(match.equals("VP")) {
	    		match = n.getMatch().firstChild().firstChild().label().toString();
	    	}
	    	if(match.equals("TO") || match.equals("VBG")) {
	    		continue;
	    	}
	    	
	    	//imperative sentence
	    	System.out.println(" --------- yes");
	    	n.getMatch().pennPrint();	
	    }
	    System.out.println(">>>");	   
	}
	
	public static void main(String[] args) {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	    String fileName = System.getProperty("user.dir")+"\\src\\data";
		
	    LexicalizedParser lp = LexicalizedParser.loadModel(parserModel);
	    
		if(args.length == 0){
			Scanner scanner = null;
			try {
				scanner = new Scanner(System.in);
			    System.out.println("Select the input method\n 1: text input 2: text File 3:JSON File  >> ");
				int inputMethod = scanner.nextInt();
			    
				switch(inputMethod) {
				
			    //standard text input
				case 1: 
			    	while(scanner.hasNext()) {
				    	String value = scanner.nextLine();
				    	detectCommand(lp, value);
			    	}
			    break;
			    
			    //text input file
			    case 2:
			    	FileReader fr = null;
			    	BufferedReader br = null;
			    	try {
				    	fr = new FileReader(fileName + ".txt"); 
				    	br = new BufferedReader(fr);
				    	
				    	String value;
				    	while((value = br.readLine()) != null) {
				    		detectCommand(lp, value);
				    	}
			    	} catch(IOException e) {
			    		e.printStackTrace();
			    	} finally {
			    		try {
			    			if(br != null) br.close();
			    			if(fr != null) fr.close();
			    		}catch(IOException ex) {
			    			ex.printStackTrace();
			    		}
			    	}
			    	break;
			    	
			    //json input file
			    case 3:
			    	JSONParser parser = new JSONParser();
			    	
			    	try {
			    		Object file = parser.parse(new FileReader(fileName+".json"));
			    		JSONObject jsonSpamData = (JSONObject)file;
				    	
				    	for(Object key: jsonSpamData.keySet()) {
				    		ArrayList<String> SpamSentences = (( ArrayList<String>)jsonSpamData.get((String)key));
				    		for(String value : SpamSentences) {
				    			detectCommand(lp, value);
				    		}
				    	}
			    	}catch (FileNotFoundException e) {
			    		e.printStackTrace();
			    	}catch (IOException e) {
			    		e.printStackTrace();
			    	}catch (ParseException e) {
			    		e.printStackTrace();
			    	}
			    	break;
			    
			    default:
			    	System.out.println("wrong input");			    
				}
			}
			finally{
			    if(scanner != null) scanner.close();
			}
	    }
	}

}
