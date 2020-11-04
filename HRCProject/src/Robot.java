import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

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
	private boolean toCleanOrNotToClean;
	
	private LinkedList<Action> path;
	private Iterator<Action> pathIterator;
	private Position target;
	
	/**
	    Initializes a Robot on a specific tile in the environment. 
	*/

	
	public Robot (Environment env, int posRow, int posCol) {
		this.env = env;
		this.posRow = posRow;
		this.posCol = posCol;
		this.toCleanOrNotToClean = false;
		
		this.target = new Position(this.posRow, this.posCol);
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
		
		if (toCleanOrNotToClean) {
			toCleanOrNotToClean = false;
			return Action.CLEAN;
		}
		toCleanOrNotToClean = true;
		
		// No planning
//		this.target = getClosestDirtyTile();
//		return this.getStepFromTarget(target.row, target.col);
		
		// BFS
		if(this.pathIterator == null || !this.pathIterator.hasNext()) {
//			this.target = getClosestDirtyTile(); // No robot communication
			this.target = getClosestUnclaimedDirtyTile(); // Robot Communication
			bfs(target.row, target.col);
		}
		
		if(this.pathIterator != null && this.pathIterator.hasNext()) {
			return this.pathIterator.next();
		}
		
		return Action.DO_NOTHING;
	}
	
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

}