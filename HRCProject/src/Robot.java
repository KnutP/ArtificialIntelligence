import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
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
 * Represents an intelligent agent moving through a particular room. The robot
 * only has one sensor - the ability to get the status of any tile in the
 * environment through the command env.getTileStatus(row, col).
 * 
 * @author Adam Gaweda, Michael Wollowski
 */

public class Robot {
	private Environment env;
	private int posRow;
	private int posCol;
	private Action lastAction;
	private String robotName;
	private String[] acknowlegement = { "Gotcha. ", "Uh huh. ", "Yeah bro. ", "Yep. ", "Cool. " };
	private String[] actionPrepends = { 
			"I think you want me to ", 
			"I am going to ", 
			"Orders are to ", 
			"Cool, I'll ", 
			"Sure dude. I'll " };
	private String[] clarification = { 
			"Sorry dude, what do you want me to do?",
			"I didn't quite get that. Please try again.", "What was that?", "Unable to parse input.",
			"I'm confused. What do you want me to do?", "I don't understand. Try somethin' else.", "Bruh, what?",
			"I dunno know what to do bro.", "Can you make it more clear?", "Waddya mean bro?" };
	private String[] praiseResponsePhrase = {
			"No problem bro",
			"It's chill",
			"Radical, dude",
			"I gotchu fam"
	};

	private Properties props;
	private StanfordCoreNLP pipeline;

	private Scanner sc;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.lastAction = Action.DO_NOTHING;
		this.robotName = "Steve";

		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		pipeline = new StanfordCoreNLP(props);

	}

	public int getPosRow() {
		return posRow;
	}

	public int getPosCol() {
		return posCol;
	}

	public void incPosRow() {
		posRow++;
	}

	public void decPosRow() {
		posRow--;
	}

	public void incPosCol() {
		posCol++;
	}

	public void decPosCol() {
		posCol--;
	}

	/**
	 * Returns the next action to be taken by the robot. A support function that
	 * processes the path LinkedList that has been populates by the search
	 * functions.
	 */
	public Action getAction() {
		Annotation annotation;
		System.out.print("> ");
		sc = new Scanner(System.in);
		String name = sc.nextLine();
		// System.out.println("got: " + name);
		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
			// DONE: remove prettyPrint() and use the SemanticGraph to determine
			// the action to be executed by the robot.
			// graph.prettyPrint();

			// get root of parse graph
			IndexedWord root = graph.getFirstRoot();
			// type of root
			String type = root.tag();
			switch (type) {
			case "VB":
				return processVerbPhrase(graph, root);
			case "JJ":
				return processAdjectivePhrase(graph, root);
			case "NN":
				return processNounPhrase(graph, root);
			default:
				return processOtherPhrase(graph, root);
			}

		}

		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	// **** Phrase Processing ****

	public Action processVerbPhrase(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Action: " + root.toString());
		
		if(isThereNegation(dependencies, root)) {
			System.out.println("Doing nothing");
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}

		if (root.originalText().equalsIgnoreCase("clean")) {
			System.out.println("cleaning (Verb Phrase)");
			this.lastAction = Action.CLEAN;
			return Action.CLEAN;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		System.out.println(s.toString()); // TODO: Remove

		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;
			return getActionFromWord(w.originalText());
		}

		System.out.println("doing nothing (Verb Phrase)");
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action processAdjectivePhrase(SemanticGraph dependencies, IndexedWord root) {
		if (root.originalText().equalsIgnoreCase("clean")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.println("Cleaning.");
			this.lastAction = Action.CLEAN;
			return Action.CLEAN;
		}

		System.out.println(getRandom(this.clarification));
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action processNounPhrase(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Action: " + root.toString());
		
		if(isThereGreeting(dependencies, root)) {
			System.out.println(getGreetingForTime());
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		
		if(isPraising(dependencies, root)) {
			System.out.println(getRandom(this.praiseResponsePhrase));
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		
		if (root.originalText().equalsIgnoreCase("clean")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.println("Cleaning.");
			this.lastAction = Action.CLEAN;
			return Action.CLEAN;
		} else if (root.originalText().equalsIgnoreCase("repeat")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.println("Repeating the last action.");
			return this.lastAction;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		System.out.println(s.toString());

		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;

			return getActionFromWord(w.originalText());

		}

		System.out.println(getRandom(this.clarification));
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action processOtherPhrase(SemanticGraph dependencies, IndexedWord root) {
		System.out.println("Action: " + root.toString() +" " + root.tag());
		
		if(isThereNegation(dependencies, root)) {
			System.out.println("Doing nothing");
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		
		if(isThereGreeting(dependencies, root)) {
			System.out.println(getGreetingForTime());
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		
		if(isAskingForName(dependencies, root)) {
			System.out.println("My name is " + this.robotName);
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		
		if(isPraising(dependencies, root)) {
			System.out.println(getRandom(this.praiseResponsePhrase));
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		
		return keywordSearch(dependencies, root);
	}

	// **** Helper Functions ****
	
	private Action keywordSearch(SemanticGraph dependencies, IndexedWord root) {

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		
		String word = root.originalText();
		if (word.equalsIgnoreCase("up") || word.equalsIgnoreCase("down")
				|| word.equalsIgnoreCase("left") || word.equalsIgnoreCase("right")
				|| word.equalsIgnoreCase("clean") || word.equalsIgnoreCase("again") 
				|| word.equalsIgnoreCase("more") || word.equalsIgnoreCase("further")
				|| word.equalsIgnoreCase("repeat")) {
			return getActionFromWord(word);
		}
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;
			word = w.originalText();
			
			if (word.equalsIgnoreCase("up") || word.equalsIgnoreCase("down")
					|| word.equalsIgnoreCase("left") || word.equalsIgnoreCase("right")
					|| word.equalsIgnoreCase("clean") || word.equalsIgnoreCase("again") 
					|| word.equalsIgnoreCase("more") || word.equalsIgnoreCase("further")
					|| word.equalsIgnoreCase("repeat")) {
				return getActionFromWord(word);
			}
		}
		
		System.out.println(getRandom(this.clarification));
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	private boolean isThereNegation(SemanticGraph dependencies, IndexedWord root) {
		String word = root.originalText();
		
		if (word.equalsIgnoreCase("no") || word.equalsIgnoreCase("n't") || word.equalsIgnoreCase("not")) {
			return true;
		}
		
		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		System.out.println(s.toString()); // TODO: remove
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) { // first pass to check for negation
			GrammaticalRelation r = p.first;
			IndexedWord w = p.second;
			word = w.originalText();

			if (word.equalsIgnoreCase("no") || word.equalsIgnoreCase("n't") || word.equalsIgnoreCase("not")) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isThereGreeting(SemanticGraph dependencies, IndexedWord root) {
		String word = root.originalText();
		
		if (word.equalsIgnoreCase("hi") || word.equalsIgnoreCase("hello") || word.equalsIgnoreCase("greetings") 
				|| word.equalsIgnoreCase("sup") || word.equalsIgnoreCase("yo")) {
			return true;
		}
		
		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		System.out.println(s.toString()); // TODO: remove
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) { // first pass to check for negation
			GrammaticalRelation r = p.first;
			IndexedWord w = p.second;
			word = w.originalText();

			if (word.equalsIgnoreCase("hi") || word.equalsIgnoreCase("hello") || word.equalsIgnoreCase("greetings") 
					|| word.equalsIgnoreCase("sup") || word.equalsIgnoreCase("yo")) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isAskingForName(SemanticGraph dependencies, IndexedWord root) {
		String word = root.originalText();
		
		if (root.tag().equals("WP")) {
			List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
			System.out.println(s.toString()); // TODO: remove
			
			for (Pair<GrammaticalRelation, IndexedWord> p : s) {
				IndexedWord w = p.second;
				word = w.originalText();

				if (word.equalsIgnoreCase("name") || word.equalsIgnoreCase("you")) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean isPraising(SemanticGraph dependencies, IndexedWord root) {
		String word = root.originalText();
		
		if (word.equalsIgnoreCase("good") || word.equalsIgnoreCase("nice") || word.equalsIgnoreCase("well")) {
			return true;
		}
		
		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		System.out.println(s.toString()); // TODO: remove
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;
			word = w.originalText();

			if (word.equalsIgnoreCase("good") || word.equalsIgnoreCase("nice") || word.equalsIgnoreCase("well")) {
				return true;
			}
		}
		
		return false;
	}

	private String getGreetingForTime() {
        Date dt = new Date();
        int hours = dt.getHours();
        int min = dt.getMinutes();
        String greeting = "";

        if(hours>=1 && hours<=12){
            greeting = "Good Morning";
        }else if(hours>=12 && hours<=16){
        	greeting = "Good Afternoon";
        }else if(hours>=16 && hours<=21){
        	greeting = "Good Evening";
        }else if(hours>=21 && hours<=24){
        	greeting = "Good Night";
        }
        
        return greeting;
	}
	
	private static String getRandom(String[] array) {
		int rnd = new Random().nextInt(array.length);
		return array[rnd];
	}

	private Action getActionFromWord(String word) {

		if (word.equalsIgnoreCase("left")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move left.");
			this.lastAction = Action.MOVE_LEFT;
			return Action.MOVE_LEFT;
		} else if (word.equalsIgnoreCase("clean")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("clean the tile.");
			this.lastAction = Action.CLEAN;
			return Action.CLEAN;
		} else if (word.equalsIgnoreCase("right")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move right.");
			this.lastAction = Action.MOVE_RIGHT;
			return Action.MOVE_RIGHT;
		} else if (word.equalsIgnoreCase("up")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move up.");
			this.lastAction = Action.MOVE_UP;
			return Action.MOVE_UP;
		} else if (word.equalsIgnoreCase("down")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("move down.");
			this.lastAction = Action.MOVE_DOWN;
			return Action.MOVE_DOWN;
		} else if (word.equalsIgnoreCase("again") || word.equalsIgnoreCase("more") || word.equalsIgnoreCase("further")
				|| word.equalsIgnoreCase("repeat")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("repeat the last action.");
			return this.lastAction;
		}

		System.out.println(getRandom(this.clarification));
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}
	
}