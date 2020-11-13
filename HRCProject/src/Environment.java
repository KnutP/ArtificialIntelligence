
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;

/**
 * The world in which this simulation exists. As a base
 * world, this produces a 10x10 room of tiles. In addition,
 * 20% of the room is covered with "walls" (tiles marked as IMPASSABLE).
 * 
 * This object will allow the agent to explore the world and is how
 * the agent will retrieve information about the environment.
 * DO NOT MODIFY.
 * @author Adam Gaweda, Michael Wollowski
 */
public class Environment {
	private Tile[][] tiles;
	private int rows, cols;
	private LinkedList<Position> targets = new LinkedList<>();
	private ArrayList<Robot> robots;
	private double[][] utilityMatrix;
	private Action[][] policyMatrix;
	
	public Environment(LinkedList<String> map, ArrayList<Robot> robots) { 
		this.cols = map.get(0).length();
		this.rows = map.size();
		this.tiles = new Tile[rows][cols];
		for (int row = 0; row < this.rows; row++) {
			for (int col = 0; col < this.cols; col++) {
				char tile = map.get(row).charAt(col);
				switch(tile) {
				case 'R': tiles[row][col] = new Tile(TileStatus.CLEAN); {
					robots.add(new Robot(this, row, col));
					break;
				}
				case 'D': tiles[row][col] = new Tile(TileStatus.DIRTY); break;
				case 'C': tiles[row][col] = new Tile(TileStatus.CLEAN); break;
				case 'W': tiles[row][col] = new Tile(TileStatus.IMPASSABLE); break;
				case 'T': tiles[row][col] = new Tile(TileStatus.TARGET); targets.add(new Position(row, col)); break;
				}
			}
		}
		this.utilityMatrix = new double[rows][cols];
		this.policyMatrix = new Action[rows][cols];
		
		this.calculateUtilityMatrix();
		
		this.generatePolicyMatrix();
		
		this.robots = robots;
	}
	
	/* Traditional Getters and Setters */
	public Tile[][] getTiles() { return tiles; }
	
	public int getRows() { return this.rows; }
	
	public int getCols() { return this.cols; }

	public LinkedList<Position> getTargets(){
		return (LinkedList<Position>) this.targets.clone();
	}
	
	public ArrayList<Robot> getRobots(){
		return (ArrayList<Robot>) this.robots.clone();
	}
	

	/*
	 * Returns a the status of a tile at a given [row][col] coordinate
	 */
	public TileStatus getTileStatus(int row, int col) {
		if (row < 0 || row >= rows || col < 0 || col >= cols) return TileStatus.IMPASSABLE; 
		else return tiles[row][col].getStatus();
	}

	/* Counts number of tiles that are not walls */
	public int getNumTiles() {
		int count = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (this.tiles[row][col].getStatus() != TileStatus.IMPASSABLE)
                    count++;
            }
        }
        return count;
    }
	
	/* Cleans the tile at coordinate [x][y] */
	public void cleanTile(int x, int y) {
		tiles[x][y].cleanTile();		
	}
	
	/* Counts number of clean tiles */
	public int getNumCleanedTiles() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.tiles[i][j].getStatus() == TileStatus.CLEAN)
                    count++;
            }
        }
        return count;
    }
	
	/* Counts number of dirty tiles */
	public int getNumDirtyTiles() {
        int count = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (this.tiles[i][j].getStatus() == TileStatus.DIRTY)
                    count++;
            }
        }
        return count;
    }

	/* Determines if a particular [row][col] coordinate is within
	 * the boundaries of the environment. This is a rudimentary
	 * "collision detection" to ensure the agent does not walk
	 * outside the world (or through walls).
	 */
	public boolean validPos(int row, int col) {
	    return row >= 0 && row < rows && col >= 0 && col < cols &&
	    		tiles[row][col].getStatus() != TileStatus.IMPASSABLE;
	}
	
	private void calculateUtilityMatrix() {
		double[][] rewards = new double[this.rows][this.cols];
		
		// initialize reward matrix
		for(int i = 0; i < this.rows; i++) {
			for(int j = 0; j < this.cols; j++) {
				
				if(this.getTileStatus(i, j).equals(TileStatus.CLEAN)) {
					rewards[i][j] = -0.04;
				} else if (this.getTileStatus(i, j).equals(TileStatus.DIRTY)) {
					rewards[i][j] = 1;
				} else {
					rewards[i][j] = 0;
				}
				
			}
		}
		
		double discount = 0.9;
		double maxError = 0.01;
		double currentError = 1;
		double[][] tempMatrix;
		
		while(currentError > maxError) {
			currentError = 0;
			tempMatrix = new double[this.rows][this.cols];
			System.out.println("Iteration " + currentError);
			
			for(int i = 0; i < this.rows; i++) {
				for(int j = 0; j < this.cols; j++) {
					
					if(this.getTileStatus(i, j).equals(TileStatus.IMPASSABLE))
						continue;
					
					Double[] directions = new Double[4];
					double[] utilityVals = new double[4];
					
					// up
					if(i == 0 || this.getTileStatus(i-1, j).equals(TileStatus.IMPASSABLE)) { // can't move up
						utilityVals[0] = this.utilityMatrix[i][j];
					} else {
						utilityVals[0] = this.utilityMatrix[i-1][j];
					}
					
					// down
					if(i == this.rows || this.getTileStatus(i+1, j).equals(TileStatus.IMPASSABLE)) { // can't move down
						utilityVals[1] = this.utilityMatrix[i][j];
					} else {
						utilityVals[1] = this.utilityMatrix[i+1][j];
					}
					
					// left
					if(j == 0 || this.getTileStatus(i, j-1).equals(TileStatus.IMPASSABLE)) { // can't move left
						utilityVals[2] = this.utilityMatrix[i][j];
					} else {
						utilityVals[2] = this.utilityMatrix[i][j-1];
					}
					
					// right
					if(j == this.cols || this.getTileStatus(i, j+1).equals(TileStatus.IMPASSABLE)) { // can't move right
						utilityVals[3] = this.utilityMatrix[i][j];
					} else {
						utilityVals[3] = this.utilityMatrix[i][j+1];
					}
					
					directions[0] = 0.8*utilityVals[0] + 0.1*utilityVals[2] + 0.1*utilityVals[3]; // up
					directions[1] = 0.8*utilityVals[1] + 0.1*utilityVals[3] + 0.1*utilityVals[2]; // down
					directions[2] = 0.8*utilityVals[2] + 0.1*utilityVals[1] + 0.1*utilityVals[0]; // left
					directions[3] = 0.8*utilityVals[3] + 0.1*utilityVals[0] + 0.1*utilityVals[1]; // right
					
					tempMatrix[i][j] = rewards[i][j] + discount*Collections.max(Arrays.asList(directions));
					
					if(Math.abs(tempMatrix[i][j] - this.utilityMatrix[i][j]) > currentError) {
						currentError = Math.abs(tempMatrix[i][j] - this.utilityMatrix[i][j]);
					}
					
				}
			}
			System.out.println("Current error: " + currentError);
			this.utilityMatrix = tempMatrix;


		}
		 
        for (double[] row : this.utilityMatrix) 
            System.out.println(Arrays.toString(row));
		
	}

	private void generatePolicyMatrix() {
		
		
		for(int i = 0; i < this.rows; i++) {
			for(int j = 0; j < this.cols; j++) {
				
				if(this.getTileStatus(i, j).equals(TileStatus.IMPASSABLE)) {
					this.policyMatrix[i][j] = Action.DO_NOTHING;
					continue;
				} else if(this.getTileStatus(i, j).equals(TileStatus.DIRTY)) {
					this.policyMatrix[i][j] = Action.DO_NOTHING;
					continue;
				}
				
				Double[] directions = new Double[4];
				double[] utilityVals = new double[4];
				
				// up
				if(i == 0 || this.getTileStatus(i-1, j).equals(TileStatus.IMPASSABLE)) { // can't move up
					utilityVals[0] = this.utilityMatrix[i][j];
				} else {
					utilityVals[0] = this.utilityMatrix[i-1][j];
				}
				
				// down
				if(i == this.rows || this.getTileStatus(i+1, j).equals(TileStatus.IMPASSABLE)) { // can't move down
					utilityVals[1] = this.utilityMatrix[i][j];
				} else {
					utilityVals[1] = this.utilityMatrix[i+1][j];
				}
				
				// left
				if(j == 0 || this.getTileStatus(i, j-1).equals(TileStatus.IMPASSABLE)) { // can't move left
					utilityVals[2] = this.utilityMatrix[i][j];
				} else {
					utilityVals[2] = this.utilityMatrix[i][j-1];
				}
				
				// right
				if(j == this.cols || this.getTileStatus(i, j+1).equals(TileStatus.IMPASSABLE)) { // can't move right
					utilityVals[3] = this.utilityMatrix[i][j];
				} else {
					utilityVals[3] = this.utilityMatrix[i][j+1];
				}
				
				directions[0] = 0.8*utilityVals[0] + 0.1*utilityVals[2] + 0.1*utilityVals[3]; // up
				directions[1] = 0.8*utilityVals[1] + 0.1*utilityVals[3] + 0.1*utilityVals[2]; // down
				directions[2] = 0.8*utilityVals[2] + 0.1*utilityVals[1] + 0.1*utilityVals[0]; // left
				directions[3] = 0.8*utilityVals[3] + 0.1*utilityVals[0] + 0.1*utilityVals[1]; // right
				
				this.policyMatrix[i][j] = Action.DO_NOTHING;
				double lastVal = 0;
				for(int k = 0; k < 4; k++) {
					if(directions[k] > lastVal) {
						lastVal = directions[k];
						
						if(k == 0)
							this.policyMatrix[i][j] = Action.MOVE_UP;
						else if(k == 1)
							this.policyMatrix[i][j] = Action.MOVE_DOWN;
						else if(k == 2)
							this.policyMatrix[i][j] = Action.MOVE_LEFT;
						else if(k == 3)
							this.policyMatrix[i][j] = Action.MOVE_RIGHT;
						
					}
				}
				
			}
		}
		
		for(Action[] row : this.policyMatrix) {
			for(Action a : row) {
				if(a.equals(Action.MOVE_UP))
					System.out.print("u, ");
				else if(a.equals(Action.MOVE_DOWN))
					System.out.print("d, ");
				else if(a.equals(Action.MOVE_LEFT))
					System.out.print("l, ");
				else if(a.equals(Action.MOVE_RIGHT))
					System.out.print("r, ");
				else if(a.equals(Action.DO_NOTHING))
					System.out.print("-, ");
				
			}
			System.out.println();
			
		}
		
	}
}
