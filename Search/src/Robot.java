import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

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
	private LinkedList<Action> path;
	private boolean pathFound;
	private long openCount;
	private int pathLength;
	private Iterator<Action> pathIterator;

	/**
	 * Initializes a Robot on a specific tile in the environment.
	 */
	// public Robot (Environment env) { this(env, 0, 0); }

	public Robot(Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.path = new LinkedList<>();
		this.pathFound = false;
		this.openCount = 0;
		this.pathLength = 0;
	}

	public boolean getPathFound() {
		return this.pathFound;
	}

	public long getOpenCount() {
		return this.openCount;
	}

	public int getPathLength() {
		return this.pathLength;
	}

	public void resetOpenCount() {
		this.openCount = 0;
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
		if (pathIterator == null || !pathIterator.hasNext()) {
			return Action.DO_NOTHING;
		}
		return (Action) pathIterator.next();

	}

	/**
	 * This method implements breadth-first search. It populates the path
	 * LinkedList and sets pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the openCount field every time your code
	 * adds a node to the open data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void bfs() {
		LinkedList<Position> targets = env.getTargets();
		LinkedList<Position> open = new LinkedList<>();
		open.add(new Position(posRow, posCol));
		openCount++;

		LinkedList<Position> closed = new LinkedList<Position>();
		
		HashMap<Position, Position> previous = new HashMap<>();
		
		Position pos = null;
		Position target = findNextTarget(posRow, posCol, targets);
		
		while(target != null) {
			
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
						openCount++;
					}
				}

			}
			if(open.isEmpty()) {
				return;
			}
			
			targets.remove(target);
			target = findNextTarget(pos.row, pos.col, targets);
		}
		
		path = buildPath(pos, previous);
		
		pathFound = true;
		pathLength = path.size();
		pathIterator = path.iterator();
	}
	
	private ArrayList<Position[]> generatePermutations(LinkedList<Position> targets) {
		ArrayList<Position[]> targetPermutations = new ArrayList<>();
		
		Position[] targetList = targets.toArray(new Position[targets.size()]);
		int n = targetList.length;
		
		int[] indexes = new int[n];
//		for (int i = 0; i < n; i++) {
//		    indexes[i] = 0;
//		}
		
		targetPermutations.add(targetList.clone());
		 
		int i = 0;
		while (i < n) {
		    if (indexes[i] < i) {
		        swap(targetList, i % 2 == 0 ?  0: indexes[i], i);
		        targetPermutations.add(targetList.clone());
		        indexes[i]++;
		        i = 0;
		    }
		    else {
		        indexes[i] = 0;
		        i++;
		    }
		}
		
		return targetPermutations;
	}
	
	private void swap(Position[] input, int a, int b) {
	    Position temp = input[a];
	    input[a] = input[b];
	    input[b] = temp;
	}

	private boolean containsPos(int row, int col, LinkedList<Position> list) {

		for (Position p : list) {
			if (p.col == col && p.row == row)
				return true;
		}
		return false;
	}

	
	/**
	 * This method implements A* search for maps 0-5. It populates the path
	 * LinkedList and sets pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the openCount field every time your code
	 * adds a node to the open data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void astar() {
		LinkedList<Position> targets = env.getTargets();
		Position target = targets.get(0); // TODO Change

		State current = new State(posRow, posCol, h1(posRow, posCol, target), 0);
		PriorityQueue<State> open = new PriorityQueue<>();
		HashMap<State, State> previous = new HashMap<>();

		open.add(current);
		openCount++;

		LinkedList<State> closed = new LinkedList<State>();

		while (!open.isEmpty()) {

			if (current.row == target.row && current.col == target.col) {
				// toRemove.add(target);
				closed.clear();
				open.clear();
				open.add(current);
				continue;
			}

			closed.add(current);

			// check the 4 possible successors
			int[] iVals = { -1, 0, 0, 1 };
			int[] jVals = { 0, -1, 1, 0 };
			for (int k = 0; k < 4; k++) {
				int i = current.row + iVals[k];
				int j = current.col + jVals[k];

				State successor;

				if (containsPos(i, j, closed)) {
					for (State p : closed) {
						if (p.row == i && p.row == j) {
							successor = p;
							if (successor.gScore < current.gScore) {
								current.gScore = successor.gScore;
								previous.put(successor, current);
							}
						}
					}
				} else if (containsPos(i, j, open)) {
					for (State p : open) {
						if (p.row == i && p.row == j) {
							successor = p;
							if (successor.gScore < current.gScore) {
								current.gScore = successor.gScore;
								previous.put(successor, current);
							}
						}
					}
				} else {
					int gScore = current.gScore + 1;
					int fScore = gScore + h1(i, j, target);
					successor = new State(i, j, fScore, gScore);
					open.add(successor);
					openCount++;
				}

			}

		}

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
			return this.fScore-other.fScore;
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
	
	private Position findNextTarget(int row, int col, LinkedList<Position> targets) {
		if(targets.isEmpty()) {
			return null;
		}
		
		Position target = targets.getFirst();
		
		int smallestDist = h1(row, col, target);
		
		for(Position p : targets) {
			if(h1(row, col, p) < smallestDist) {
				smallestDist = h1(row, col, p);
				target = p;
			}
		}
		
		return target;
	}

	/**
	 * This method implements A* search for maps 10, 11 and 12. It populates the
	 * path LinkedList and sets pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the openCount field every time your code
	 * adds a node to the open data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void astar101112() {

	}

	/**
	 * This method implements A* search for maps 14, 15 and 16. It populates the
	 * path LinkedList and sets pathFound to true, if a path has been found.
	 * IMPORTANT: This method increases the openCount field every time your code
	 * adds a node to the open data structure, i.e. the queue or priorityQueue
	 * 
	 */
	public void astar141516() {

	}

	private LinkedList<Action> buildPath(Position pos, HashMap<Position, Position> previous) {
		LinkedList<Action> route = new LinkedList<>();
		Position last;

		while (previous.containsKey(pos)) {
			last = previous.get(pos);

			if (pos.row - last.row > 0) {
				route.add(Action.MOVE_RIGHT);
			} else if (pos.row - last.row < 0) {
				route.add(Action.MOVE_LEFT);
			} else if (pos.col - last.col > 0) {
				route.add(Action.MOVE_DOWN);
			} else {
				route.add(Action.MOVE_UP);
			}

			pos = last;
		}
		Collections.reverse(route);
		return route;
	}

}