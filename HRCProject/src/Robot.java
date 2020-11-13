import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
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
	private String[] jokes =  {
		"Why didn't the surfer ride the glassy waves? Because he heard they were breaking!",
		"What did the wave say to the surfer? Have a swell time!",
		"Why do surfers eat cold food? Because they hate microwaves."		
	};
	private String[] weekendPlans =  {
			"Dude, I finna go surfing",
			"I'm gonna hit the beach",
			"Just vibin', yah know?"		
		};
	
	private boolean isRecording;
	private boolean isExecuting;
	private HashMap<String, LinkedList<Action>> paths;
	private LinkedList<Action> currentPath;
	private Iterator<Action> pathIterator;

	private Properties props;
	private StanfordCoreNLP pipeline;

	private Scanner sc;
	
	private LinkedList<Action> path;
	private Position target;
	private boolean toCleanOrNotToClean;


	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.lastAction = Action.DO_NOTHING;
		this.robotName = "Steve";
		
		this.isRecording = false;
		this.isExecuting = false;
		this.paths = new HashMap<>();

		props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		pipeline = new StanfordCoreNLP(props);
		
		this.toCleanOrNotToClean = false;
		this.target = new Position(this.posRow, this.posCol);

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
		
		if(this.isExecuting) {
			if(this.pathIterator.hasNext()) {
				return this.pathIterator.next();
			} else {
				this.isExecuting = false;
			}
		}
		
		Annotation annotation;
		System.out.print("> ");
		sc = new Scanner(System.in);
		String name = sc.nextLine();
		annotation = new Annotation(name);
		pipeline.annotate(annotation);
		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		
		if (sentences != null && !sentences.isEmpty()) {
			CoreMap sentence = sentences.get(0);
			SemanticGraph graph = sentence
					.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

			// get root of parse graph
			IndexedWord root = graph.getFirstRoot();
			
			this.checkForStartRecording(name);
			
			if(this.isRecording) {				
				this.checkForEndRecording(name);
				this.checkForCoordinates(graph, root);
				this.checkForSavePlan(name);
				
				return Action.DO_NOTHING;
			}
			
			// check for path names
			this.checkForExecutePlan(name);

			// execute paths
			if(this.isExecuting) {
				if(this.pathIterator.hasNext()) {
					return this.pathIterator.next();
				} else {
					this.isExecuting = false;
				}
			}
			
			// combine
			if(this.checkForCombinePlan(name))
				return Action.DO_NOTHING;
			
			if(this.checkForSavePlan(name))
				return Action.DO_NOTHING;
			
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
		//System.out.println("Action: " + root.toString());
		
		if(isThereNegation(dependencies, root)) {
			System.out.println("Doing nothing");
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}
		
		if(isAskingForJoke(dependencies, root)) {
			System.out.println(getRandom(this.jokes));
			this.lastAction = Action.DO_NOTHING;
			return Action.DO_NOTHING;
		}

		if (root.originalText().equalsIgnoreCase("clean")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("clean the tile.");
			this.lastAction = Action.CLEAN;
			return Action.CLEAN;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		//System.out.println(s.toString());

		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;
			return getActionFromWord(w.originalText());
		}

		System.out.println(getRandom(this.clarification));
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action processAdjectivePhrase(SemanticGraph dependencies, IndexedWord root) {
		if (root.originalText().equalsIgnoreCase("clean")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("clean the tile.");
			this.lastAction = Action.CLEAN;
			return Action.CLEAN;
		}

		System.out.println(getRandom(this.clarification));
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action processNounPhrase(SemanticGraph dependencies, IndexedWord root) {
		//System.out.println("Action: " + root.toString());
		
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
			System.out.print(getRandom(this.actionPrepends));
			System.out.println("clean the tile.");
			this.lastAction = Action.CLEAN;
			return Action.CLEAN;
		} else if (root.originalText().equalsIgnoreCase("repeat")) {
			System.out.print(getRandom(this.acknowlegement));
			System.out.println("Repeating the last action.");
			return this.lastAction;
		}

		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		//System.out.println(s.toString());

		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;

			return getActionFromWord(w.originalText());

		}

		System.out.println(getRandom(this.clarification));
		this.lastAction = Action.DO_NOTHING;
		return Action.DO_NOTHING;
	}

	public Action processOtherPhrase(SemanticGraph dependencies, IndexedWord root) {
		//System.out.println("Action: " + root.toString() +" " + root.tag());
		
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
		
		if(isAskingAboutWeekend(dependencies, root)) {
			System.out.println(getRandom(this.weekendPlans));
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
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) { // first pass to check for negation
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
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) { // first pass to check for negation
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
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;
			word = w.originalText();

			if (word.equalsIgnoreCase("good") || word.equalsIgnoreCase("nice") || word.equalsIgnoreCase("well")) {
				return true;
			}
		}
		
		return false;
	}

	private boolean isAskingForJoke(SemanticGraph dependencies, IndexedWord root) {
		String word = root.originalText();
		
		if (word.equalsIgnoreCase("joke") || word.equalsIgnoreCase("funny")) {
			return true;
		}
		
		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;
			word = w.originalText();

			if (word.equalsIgnoreCase("joke") || word.equalsIgnoreCase("funny")) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean isAskingAboutWeekend(SemanticGraph dependencies, IndexedWord root) {
		String word = root.originalText();
		
		if (word.equalsIgnoreCase("weekend") || word.equalsIgnoreCase("plans")) {
			return true;
		}
		
		List<Pair<GrammaticalRelation, IndexedWord>> s = dependencies.childPairs(root);
		
		for (Pair<GrammaticalRelation, IndexedWord> p : s) {
			IndexedWord w = p.second;
			word = w.originalText();

			if (word.equalsIgnoreCase("weekend") || word.equalsIgnoreCase("plans")) {
				return true;
			}
		}
		
		return false;
	}
	
	private String getGreetingForTime() {
        Date dt = new Date();
        int hours = dt.getHours();
        String greeting = "";

        if(hours>=1 && hours<=12){
            greeting = "Mornin' dude";
        }else if(hours>=12 && hours<=16){
        	greeting = "Afternoon bro";
        }else if(hours>=16 && hours<=21){
        	greeting = "Good evening";
        }else if(hours>=21 && hours<=24){
        	greeting = "Good night";
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
	
	private void checkForStartRecording(String input) {
		if(input.contains("begin record")) {
			this.isRecording = true;
			System.out.println("Recording started");
		}
	}
	
	private void checkForEndRecording(String input) {
		if(input.contains("end record")) {
			this.isRecording = false;
			System.out.println("Recording finished");
		}
	}
	
	private void checkForCoordinates(SemanticGraph dependencies, IndexedWord root) {
		// extract coordinates
		ArrayList<Integer> coords = new ArrayList<>();
		List<IndexedWord> words = dependencies.topologicalSort();
		
		//System.out.println(words.toString());
		for (IndexedWord w : words) {
			if(w.tag().equals("CD")) {
				coords.add(Integer.parseInt(w.originalText()));
				//System.out.println("found a coordinate");
			}
		}
		
		// if found, execute astar
		if(coords.size() >= 2) {
			//System.out.println("Found coordinates");
			astar(coords.get(0), coords.get(1));
		}
		
	}
	
	private boolean checkForCombinePlan(String input) {
		
		if(input.contains("combine")) {
			this.currentPath = new LinkedList<>();
			
			for(String s : this.paths.keySet()) {
				if(input.contains(s)) {
					this.currentPath.addAll(this.paths.get(s));
				}
			}
			return true;
		}
		
		return false;
		
	}
	
	private boolean checkForSavePlan(String input) {
		String name;
		if(input.contains("name the plan ")) {
			name = input.replace("name the plan ", "");
			System.out.println("Named the plan " + name);
			this.paths.put(name, this.currentPath);
			return true;
		} else if(input.contains("name the path ")) {
			name = input.replace("name the path ", "");
			System.out.println("Named the plan " + name);
			this.paths.put(name, this.currentPath);
			return true;
		}
		
		return false;
		
	}
	
	private void checkForExecutePlan(String input) {
		
		if(input.contains("execute plan ")) {
			executePlan(input.replace("execute plan ", ""));
		} else if(input.contains("execute path ")) {
			executePlan(input.replace("execute path ", ""));
		}  else if(input.contains("execute symmetric path ")) {
			executeSymmetricPlan(input.replace("execute symmetric path ", ""));
		} else if(input.contains("execute symmetric plan ")) {
			executeSymmetricPlan(input.replace("execute symmetric plan ", ""));
		}
		
	}
	
	private void executePlan(String name) {
		System.out.println("Executing plan " + name);
		
		this.currentPath = paths.get(name);
		
		if(this.currentPath == null) {
			System.out.println("No path found");
		}
		
		this.isExecuting = true;
		this.pathIterator = this.currentPath.iterator();
		
	}
	
	private void executeSymmetricPlan(String name) {
		System.out.println("Executing symmetric plan " + name);
		
		this.currentPath = paths.get(name);
		
		if(this.currentPath == null) {
			System.out.println("No path found");
			return;
		}
		
		this.currentPath = getSymmetricPlan(this.currentPath);
		
		this.isExecuting = true;
		this.pathIterator = this.currentPath.iterator();
		
	}
	
	private LinkedList<Action> getSymmetricPlan(LinkedList<Action> path) {
		LinkedList<Action> newPath = new LinkedList<>();
		
		for(Action a : path) {
			if(a.equals(Action.MOVE_DOWN)) {
				newPath.add(Action.MOVE_UP);
			} else if(a.equals(Action.MOVE_UP)) {
				newPath.add(Action.MOVE_DOWN);
			} else if(a.equals(Action.MOVE_RIGHT)) {
				newPath.add(Action.MOVE_LEFT);
			} else if(a.equals(Action.MOVE_LEFT)) {
				newPath.add(Action.MOVE_RIGHT);
			}
		}
		
		return newPath;
	}
	
	
	
	// ***** astar search methods ****
	
	public void astar(int x, int y) {
		Position target = new Position(x, y);

		State current = new State(posRow, posCol, h1(posRow, posCol, target), 0);
		PriorityQueue<State> open = new PriorityQueue<>();
		HashMap<State, State> previous = new HashMap<>();

		open.add(current);

		LinkedList<State> closed = new LinkedList<State>();

		while (!open.isEmpty()) {

			current = open.poll();
			closed.add(current);

			if (current.row == target.row && current.col == target.col) {
				// toRemove.add(target);
				closed.clear();
				open.clear();
				open.add(current);
				break;
			}

			// check the 4 possible successors
			int[] iVals = { -1, 0, 0, 1 };
			int[] jVals = { 0, -1, 1, 0 };
			for (int k = 0; k < 4; k++) {
				int i = current.row + iVals[k];
				int j = current.col + jVals[k];

				State successor;

				if (containsPos(i, j, open)) {
					for (State p : open) {
						if (p.row == i && p.row == j) {
							successor = p;
							if (successor.gScore < current.gScore) {
								current.gScore = successor.gScore;
								current.fScore = current.gScore + h1(i, j, target);
								previous.put(successor, current);
							}
						}
					}
				} else if ((env.getTileStatus(i, j) != TileStatus.IMPASSABLE) && !containsPos(i, j, closed)) {
					int gScore = current.gScore + 1;
					int fScore = gScore + h1(i, j, target);
					successor = new State(i, j, fScore, gScore);
					open.add(successor);
					previous.put(successor, current);
				}

			}

		}
		if (open.isEmpty()) {
			return;
		}

		currentPath = buildPath(current, previous);

		System.out.println("path found");
	}

	class State implements Comparable<State> {
		public int row;
		public int col;
		public int fScore;
		public int gScore;

		State(int row, int col, int fScore, int gScore) {
			this.row = row;
			this.col = col;
			this.fScore = fScore;
			this.gScore = gScore;
		}

		public boolean equals(State other) {
			return row == other.row && col == other.col;
		}

		public boolean equals(Position other) {
			return row == other.row && col == other.col;
		}

		@Override
		public int compareTo(State other) {
			return (this.fScore - other.fScore);
		}

	}

	private int h1(int row, int col, Position target) {
		return Math.abs(row - target.row) + Math.abs(col - target.col);
	}

	private boolean containsPos(int row, int col, Iterable<State> T) {

		for (State p : T) {
			if (p.col == col && p.row == row)
				return true;
		}
		return false;
	}
	
	private LinkedList<Action> buildPath(State pos, HashMap<State, State> previous) {
		LinkedList<Action> route = new LinkedList<>();
		State last;

		while (previous.containsKey(pos)) {
			last = previous.get(pos);

			if (pos.col - last.col > 0) {
				route.add(Action.MOVE_RIGHT);
			} else if (pos.col - last.col < 0) {
				route.add(Action.MOVE_LEFT);
			} else if (pos.row - last.row > 0) {
				route.add(Action.MOVE_DOWN);
			} else {
				route.add(Action.MOVE_UP);
			}

			pos = last;
		}
		Collections.reverse(route);
		return route;
	}
	
	
	// ***** multi-robot planning *****
	
	/**
	   Returns the next action to be taken by the robot. A support function 
	   that processes the path LinkedList that has been populates by the
	   search functions.
	*/
	public Action executeMultiRobotPlan() {
		
		if (toCleanOrNotToClean) {
			toCleanOrNotToClean = false;
			return Action.CLEAN;
		}
		toCleanOrNotToClean = true;
		
		// BFS
		if(this.pathIterator == null || !this.pathIterator.hasNext()) {
//			this.target = getClosestDirtyTile(); // No robot communication
			this.target = getClosestUnclaimedDirtyTile(); // Robot Communication
			bfs(target.row, target.col);
		}
		
		if(this.pathIterator != null && this.pathIterator.hasNext()) {
			Action next = this.pathIterator.next();
			
			if(isNextTileOccupied(next)) {
				this.pathIterator = null;
				return Action.CLEAN;
			} else {
				return next;
			}
		}
		
		return Action.DO_NOTHING;
	}
	
	@SuppressWarnings("unused")
	private Action getStepFromTarget(int row, int col) {
		int rowDist = row - this.posRow;
		int colDist = col - this.posCol;
		
		if(rowDist == 0 && colDist == 0)
			return Action.CLEAN;
		
		if(Math.abs(rowDist) > Math.abs(colDist)) {
			if(row > this.posRow)
				return Action.MOVE_DOWN;
			else
				return Action.MOVE_UP;
		} else {
			if(col > this.posCol)
				return Action.MOVE_RIGHT;
			else
				return Action.MOVE_LEFT;
		}
	}
	
	@SuppressWarnings("unused")
	private Position getClosestDirtyTile() {
		LinkedList<Position> open = new LinkedList<>();
		open.add(new Position(posRow, posCol));

		LinkedList<Position> closed = new LinkedList<Position>();
		Position pos = null;

		while (!open.isEmpty()) {
			

			pos = open.pop();
			closed.add(pos);

			if(this.env.getTileStatus(pos.row, pos.col) == TileStatus.DIRTY) {
				return new Position(pos.row, pos.col);
			}

			// check the 4 possible successors
			int[] iVals = { -1, 0, 0, 1 };
			int[] jVals = { 0, -1, 1, 0 };
			for (int k = 0; k < 4; k++) {
				int i = pos.row + iVals[k];
				int j = pos.col + jVals[k];

				if (!containsPos(i, j, open) && !containsPos(i, j, closed) && env.validPos(i, j)) {
					Position successor = new Position(i, j);
					open.add(successor);
				}
			}

		}
		
		return new Position(this.posRow, this.posCol);
	}

	private Position getClosestUnclaimedDirtyTile() {
		LinkedList<Position> open = new LinkedList<>();
		open.add(new Position(posRow, posCol));

		LinkedList<Position> closed = new LinkedList<Position>();
		Position pos = null;

		while (!open.isEmpty()) {

			pos = open.pop();
			closed.add(pos);

			if(this.env.getTileStatus(pos.row, pos.col) == TileStatus.DIRTY) {
				boolean isUnclaimed = true;
				
				for(Robot r : this.env.getRobots()){
					if(r.target.row == pos.row && r.target.col == pos.col) {
						isUnclaimed = false;
						
					}
				}
				
				if(isUnclaimed) {
					return new Position(pos.row, pos.col);
				}
			}

			// check the 4 possible successors
			int[] iVals = { -1, 0, 0, 1 };
			int[] jVals = { 0, -1, 1, 0 };
			for (int k = 0; k < 4; k++) {
				int i = pos.row + iVals[k];
				int j = pos.col + jVals[k];

				if (!containsPos(i, j, open) && !containsPos(i, j, closed) && env.validPos(i, j)) {
					Position successor = new Position(i, j);
					open.add(successor);
				}
			}

		}
		
		return new Position(this.posRow, this.posCol);
	}
	
	private void bfs(int row, int col) {
		Position target = new Position(row, col);
		LinkedList<Position> open = new LinkedList<>();
		open.add(new Position(posRow, posCol));

		LinkedList<Position> closed = new LinkedList<Position>();

		HashMap<Position, Position> previous = new HashMap<>();

		Position pos = null;

			while (!open.isEmpty()) {

				pos = open.pop();
				closed.add(pos);

				if (pos.row == target.row && pos.col == target.col) {
					closed.clear();
					open.clear();
					open.add(pos);
					break;
				}

				// check the 4 possible successors
				int[] iVals = { -1, 0, 0, 1 };
				int[] jVals = { 0, -1, 1, 0 };
				for (int k = 0; k < 4; k++) {
					int i = pos.row + iVals[k];
					int j = pos.col + jVals[k];

					if (!containsPos(i, j, open) && !containsPos(i, j, closed)
							&& (env.getTileStatus(i, j) != TileStatus.IMPASSABLE)) {
						Position successor = new Position(i, j);
						open.add(successor);
						previous.put(successor, pos);
					}
				}

			}

			if (open.isEmpty()) {
				return;
			}

		path = buildPath(pos, previous);
		pathIterator = path.iterator();
	}

	private boolean containsPos(int row, int col, LinkedList<Position> list) {

		for (Position p : list) {
			if (p.col == col && p.row == row)
				return true;
		}
		return false;
	}
	
	private LinkedList<Action> buildPath(Position pos, HashMap<Position, Position> previous) {
		LinkedList<Action> route = new LinkedList<>();
		Position last;

		while (previous.containsKey(pos)) {
			last = previous.get(pos);

			if (pos.col - last.col > 0) {
				route.add(Action.MOVE_RIGHT);
			} else if (pos.col - last.col < 0) {
				route.add(Action.MOVE_LEFT);
			} else if (pos.row - last.row > 0) {
				route.add(Action.MOVE_DOWN);
			} else {
				route.add(Action.MOVE_UP);
			}

			pos = last;
		}
		Collections.reverse(route);
		return route;
	}

	public Position getTarget(){
		return this.target;
	}

	private boolean isNextTileOccupied(Action a) {
		int row = this.posRow;
		int col = this.posCol;
		
		if(a == Action.MOVE_DOWN) {
			row++;
		} else if (a == Action.MOVE_UP) {
			row--;
		} else if (a == Action.MOVE_LEFT) {
			col--;
		} else if (a == Action.MOVE_RIGHT) {
			col++;
		}
		
		for(Robot r : this.env.getRobots()) {
			if(row == r.getPosRow() && col == r.getPosCol() && !r.equals(this)) {
				return true;
			}
			
		}
		
		return false;
	}

	
}