package sudoku;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 * @author petersk, 9/7/2020
 *
 */
public class Sudoku {

	private static int boardSize = 0;
	private static int partitionSize = 0;

	public static void main(String[] args) {
		String filename = "sudoku9Hard.txt";
		File inputFile = new File(filename);
		Scanner input = null;
		int[][] vals = null;

		ArrayList<Variable> vars = new ArrayList<Variable>();

		int temp = 0;
		int count = 0;

		try {
			input = new Scanner(inputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp);
			vals = new int[boardSize][boardSize];

			System.out.println("Input:");
			int i = 0;
			int j = 0;
			while (input.hasNext()) {
				temp = input.nextInt();
				count++;
				System.out.printf("%3d", temp);
				vals[i][j] = temp;
				if (temp == 0) {
					vars.add(new Variable(i, j));
				}
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
					System.out.println();
				}
				if (j == boardSize) {
					break;
				}
			}
			input.close();
		} catch (FileNotFoundException exception) {
			System.out.println("Input file not found: " + filename);
		}
		if (count != boardSize * boardSize)
			throw new RuntimeException("Incorrect number of inputs.");

		boolean solved = solve(vars, vals, 0);

		// Write result to file
		File file = new File(filename.substring(0, filename.length() - 4) + "Solution.txt");
		FileWriter fw;
		try {
			fw = new FileWriter(file);
			PrintWriter pw = new PrintWriter(fw);
			if (!solved) {
				pw.print("-1");
				pw.close();
			} else {
				for (int i = 0; i < boardSize; i++) {
					for (int j = 0; j < boardSize; j++) {
						pw.print(vals[i][j]);
						if (j != boardSize - 1) {
							pw.print(" ");
						}
					}
					pw.println();
				}
				pw.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Output
		if (!solved) {
			System.out.println("No solution found.");
			return;
		}
		System.out.println("\nOutput\n");
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.printf("%3d", vals[i][j]);
			}
			System.out.println();
		}

	}

	public static boolean solve(ArrayList<Variable> assignments, int[][] vals, int varIndex) {

		if (varIndex >= assignments.size()) {
			return false;
		}

		Variable currentVar = assignments.get(varIndex);
		while (currentVar.value <= 9) {

			if (consistent(currentVar, vals)) {

				vals[currentVar.row][currentVar.col] = currentVar.value;
				if (varIndex == assignments.size() - 1) {
					return true;
				}

				if (solve(assignments, vals, varIndex + 1)) {
					return true;
				}
			}
			currentVar.value++;
		}

		currentVar.value = 1;
		vals[currentVar.row][currentVar.col] = 0;

		return false;
	}

	public static boolean consistent(Variable temp, int[][] vals) {
		int val = temp.value;

		// check column
		for (int i = 0; i < vals.length; i++) {
			if (vals[i][temp.col] == val) {
				return false;
			}
		}

		// check row
		for (int j = 0; j < vals.length; j++) {
			if (vals[temp.row][j] == val) {
				return false;
			}
		}

		// check box
		int boxJ = temp.col - temp.col % 3; // 7 -2 = 6
		int boxI = temp.row - temp.row % 3; // 0
		for (int i = boxI; i < boxI + 3; i++) {
			for (int j = boxJ; j < boxJ + 3; j++) {
				if (vals[i][j] == val) {
					return false;
				}
			}
		}

		return true;
	}

}

class Variable {
	int value;
	int row;
	int col;

	Variable(int row, int col) {
		this.row = row;
		this.col = col;
		this.value = 1;
	}

}