import java.util.AbstractCollection;
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
	private Iterator pathIterator;

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
		if(pathIterator == null || !pathIterator.hasNext()) {
			return Action.DO_NOTHING;
		}
		return (Action)pathIterator.next();
		
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
		HashMap<Position, Position> previous = new HashMap<>();
		open.add(new Position(posRow, posCol));
		openCount++;

		LinkedList<Position> closed = new LinkedList<Position>();

		Position pos = null;
		

		while (!targets.isEmpty()) {
			if(open.isEmpty()) {
				return; // no solution
			}
			
			pos = open.pop();
			closed.add(pos);

			if (containsPos(pos.row, pos.col, targets)) {
				ArrayList<Position> toRemove = new ArrayList<>();
				for(Position p : targets) {
					if(p.col == pos.col && p.row == pos.row)
						toRemove.add(p);
				}
				targets.removeAll(toRemove);
//				closed.clear();
//				open.clear();
				continue;
			}

			// check the 4 possible successors
			int[] iVals = {-1, 0, 0, 1};
			int[] jVals = {0, -1, 1, 0};
			for (int k = 0; k < 4; k++) {
				int i = pos.row + iVals[k];
				int j = pos.col + jVals[k];
				
					if(!containsPos(i, j, open) && !containsPos(i, j, closed) && (env.getTileStatus(i, j) != TileStatus.IMPASSABLE)) {
						Position successor = new Position(i, j);
						open.add(successor);
						previous.put(successor, pos);
						openCount++;
					}
			}

		}
		
		Position last;
		while (previous.containsKey(pos)) {
			last = previous.get(pos);
			
			if(pos.row - last.row > 0) {
				path.add(Action.MOVE_RIGHT);
			} else if (pos.row - last.row < 0) {
				path.add(Action.MOVE_LEFT);
			} else if (pos.col - last.col > 0) {
				path.add(Action.MOVE_DOWN);
			} else {
				path.add(Action.MOVE_UP);
			}
			
			pos = last;
		}
		Collections.reverse(path);
		
		pathLength = path.size();
		pathFound = true;
		pathIterator = path.iterator();
	}

	private boolean containsPos(int row, int col, LinkedList<Position> list) {
		
		for(Position p : list) {
			if(p.col == col && p.row == row)
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

}