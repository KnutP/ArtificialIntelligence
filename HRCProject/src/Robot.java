import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

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
	    }
	    
	    return Action.DO_NOTHING;
	}


}