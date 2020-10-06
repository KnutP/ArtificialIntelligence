import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

/**
	Represents an intelligent agent moving through a particular room.	
	The robot only has one sensor - the ability to get the status of any  
	tile in the environment through the command env.getTileStatus(row, col).
	@author Adam Gaweda, Michael Wollowski
*/

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;

	private Properties props;
	private StanfordCoreNLP pipeline;
	
	private Scanner sc;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		
	    props = new Properties();
	    props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
	    pipeline = new StanfordCoreNLP(props);


	}
	
	public int getPosRow() { return posRow; }
	public int getPosCol() { return posCol; }
	public void incPosRow() { posRow++; }
	public void decPosRow() { posRow--; }
	public void incPosCol() { posCol++; }
    public void decPosCol() { posCol--; }
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action getAction () {
	    Annotation annotation;
	    System.out.print("> ");
	    sc = new Scanner(System.in); 
        String name = sc.nextLine(); 
//	    System.out.println("got: " + name);
        annotation = new Annotation(name);
	    pipeline.annotate(annotation);
	    List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
	    if (sentences != null && ! sentences.isEmpty()) {
	      CoreMap sentence = sentences.get(0);
	      SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
	      //TODO: remove prettyPrint() and use the SemanticGraph to determine the action to be executed by the robot.
	      graph.prettyPrint();
	      
	      
	   // get root of parse graph
          IndexedWord root = graph.getFirstRoot();
          // type of root
          String type = root.tag();
          switch (type) {
	          case "VB": return processVerbPhrase(graph, root);
	          case "JJ": return processAdjectivePhrase(graph, root);
	          case "NN": return processNounPhrase(graph, root);
	          default: return processOtherPhrase(graph, root);
          	}
	      
	      // Find verbs
	      
	      // if close to "left", go left
	      // same for right, up, down
	      
	      // if close to "clean", then clean tile
	      
	      
	      
	      
	    }
	    
	    return Action.DO_NOTHING;
	}
    
	
    public static Action processVerbPhrase(SemanticGraph dependencies, IndexedWord root){
        System.out.println("Action: " + root.toString());
        
    	if(root.originalText().equalsIgnoreCase("clean")) {
    		System.out.println("cleaning (Verb Phrase)");
    		return Action.CLEAN;
    	}
    	
    	List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);
    	System.out.println(s.toString());
    	
    	for(Pair p : s) {
    		GrammaticalRelation r = (GrammaticalRelation) p.first;
    		IndexedWord w = (IndexedWord) p.second;

	    	if(w.originalText().equalsIgnoreCase("left")) {
	    		System.out.println("moving left");
	    		return Action.MOVE_LEFT;
	    	} else if(w.originalText().equalsIgnoreCase("right")) {
	    		System.out.println("moving right");
	    		return Action.MOVE_RIGHT;
	    	} else if(w.originalText().equalsIgnoreCase("up")) {
	    		System.out.println("moving up");
	    		return Action.MOVE_UP;
	    	} else if(w.originalText().equalsIgnoreCase("down")) {
	    		System.out.println("moving down");
	    		return Action.MOVE_DOWN;
	    	}

    	}
    	
    	System.out.println("doing nothing (Verb Phrase)");
        return Action.DO_NOTHING;
    }
    
    public static Action processAdjectivePhrase(SemanticGraph dependencies, IndexedWord root) {
    	if(root.originalText().equalsIgnoreCase("clean")) {
    		System.out.println("cleaning (Adjective phrase)");
    		return Action.CLEAN;
    	}
    	
    	// TODO: Check similar words
    	
    	return Action.DO_NOTHING;
    }
    
    public static Action processNounPhrase(SemanticGraph dependencies, IndexedWord root){
        System.out.println("Action: " + root.toString());
        
    	if(root.originalText().equalsIgnoreCase("clean")) {
    		System.out.println("cleaning (Noun Phrase)");
    		return Action.CLEAN;
    	}
    	
    	List<Pair<GrammaticalRelation,IndexedWord>> s = dependencies.childPairs(root);
    	System.out.println(s.toString());
    	
    	for(Pair p : s) {
    		GrammaticalRelation r = (GrammaticalRelation) p.first;
    		IndexedWord w = (IndexedWord) p.second;

	    	if(w.originalText().equalsIgnoreCase("left")) {
	    		System.out.println("moving left");
	    		return Action.MOVE_LEFT;
	    	} else if(w.originalText().equalsIgnoreCase("right")) {
	    		System.out.println("moving right");
	    		return Action.MOVE_RIGHT;
	    	} else if(w.originalText().equalsIgnoreCase("up")) {
	    		System.out.println("moving up");
	    		return Action.MOVE_UP;
	    	} else if(w.originalText().equalsIgnoreCase("down")) {
	    		System.out.println("moving down");
	    		return Action.MOVE_DOWN;
	    	}

    	}
    	
    	System.out.println("doing nothing (Noun Phrase)");
        return Action.DO_NOTHING;
    }
    
    public static Action processOtherPhrase(SemanticGraph dependencies, IndexedWord root){
        System.out.println("Action: " + root.toString());
        
    	if(root.originalText().equalsIgnoreCase("clean")) {
    		System.out.println("cleaning (Noun Phrase)");
    		return Action.CLEAN;
    	} else if(root.originalText().equalsIgnoreCase("left")) {
    		System.out.println("moving left");
    		return Action.MOVE_LEFT;
    	} else if(root.originalText().equalsIgnoreCase("right")) {
    		System.out.println("moving right");
    		return Action.MOVE_RIGHT;
    	} else if(root.originalText().equalsIgnoreCase("up")) {
    		System.out.println("moving up");
    		return Action.MOVE_UP;
    	} else if(root.originalText().equalsIgnoreCase("down")) {
    		System.out.println("moving down");
    		return Action.MOVE_DOWN;
    	}
    	
    	
    	System.out.println("doing nothing " + root.tag().toString());
        return Action.DO_NOTHING;
    }


}