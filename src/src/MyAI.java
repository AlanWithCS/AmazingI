

/*

<<<<<<< HEAD
AUTHOR:      Zhiying QIAN, Yifan ZHANG
=======
AUTHOR:      ZHIYING QIAN, YIFAN ZHANG
>>>>>>> 3e681f6d097a2620f8eeebefcbcf43a77f2cdd86

*/

package src;
import src.Action.ACTION;

import java.util.Queue;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MyAI extends AI {
	// ########################## INSTRUCTIONS ##########################
	// 1) The Minesweeper Shell will pass in the board size, number of mines
	// 	  and first move coordinates to your agent. Create any instance variables
	//    necessary to store these variables.
	//
	// 2) You MUST implement the getAction() method which has a single parameter,
	// 	  number. If your most recent move is an Action.UNCOVER action, this value will
	//	  be the number of the tile just uncovered. If your most recent move is
	//    not Action.UNCOVER, then the value will be -1.
	// 
	// 3) Feel free to implement any helper functions.
	//
	// ###################### END OF INSTURCTIONS #######################
	
	// This line is to remove compiler warnings related to using Java generics
	// if you decide to do so in your implementation.
	@SuppressWarnings("unchecked")
	
	// private variables
	private final int ROW_DIMENSION;
	private final int COL_DIMENSION;
	private final int TOTAL_MINES;
	// NEED TO MODIFY IF UNFLAG
	private boolean[][] visited; // check if the <x,y> has been visited
	// record the last Action, which contains ACTION, lastX and lastY
	private Queue<Action> toUncover;
	private Queue<Action> toFlag;
	private int lastX;
	private int lastY;
	private ACTION lastA;
	private int[][] board;

	/*
	 * x,y coordinates are 1-indexed. That is,
	 * 1 <= x <= colDimension & 1 <= y <= rowDimension
	 */
	public MyAI(int rowDimension, int colDimension, int totalMines, int startX, int startY) {
		// ################### Implement Constructor (required) ####################	
		this.ROW_DIMENSION = rowDimension;
		this.COL_DIMENSION = colDimension;
		this.board = new int[this.ROW_DIMENSION][this.COL_DIMENSION];
		this.visited = new boolean[this.ROW_DIMENSION][this.COL_DIMENSION];
		this.TOTAL_MINES = totalMines;
		this.toUncover = new LinkedList<Action>();
		this.toFlag = new LinkedList<Action>();
		// coordinates need to be translated 
		int[] translatedXY = this.translateToBoard(startX, startY);
		this.lastX = translatedXY[0]; 
		this.lastY = translatedXY[1];
		this.lastA = ACTION.UNCOVER;
			
		// initialize the board as covered region with value -2
		// Not Mine: >= 0
		// Flag: -1
		// Covered: -2
		for (int i = 0; i < this.ROW_DIMENSION; i++) {
			for (int j = 0; j < this.COL_DIMENSION; j++) {
				board[i][j] = -2;
			}
		}	
	}
	
	// ################## Implement getAction(), (required) #####################
	public Action getAction(int number) {
		// System.out.println("myai action");
		
		// if use UNFLAG, change the operation
		board[lastX][lastY] = number;
		
		// explore the neighbors of (lastX, lastY) including itself
		for (int row = lastX - 1; row <= lastX + 1; row++) {
			for (int col = lastY - 1; col <= lastY + 1; col++) {
				if (isValid(row, col) && board[row][col] >= 0) {
					// push the neighbors with certain state (safe or mine)
					pushCandidates(row, col);	
				}
			}
		}
		
		/* check if toUncover & toFlag is empty
		 * as long as any of them is not empty, return a new action according to the queue type
		 * if both are empty, choose a random node and go to next action	
		 */
		Action nextAction;
		if (toFlag.isEmpty() && toUncover.isEmpty()) {
			CSPSolver solver = new CSPSolver();
			List<List<int[]>> result = solver.getResult();
			
			for (int j = 0; j < result.get(0).size(); j++) {
				toFlag.add(new Action(ACTION.FLAG, result.get(0).get(j)[0], result.get(0).get(j)[1]));
			}
			for (int j = 0; j < result.get(1).size(); j++) {
				toUncover.add(new Action(ACTION.UNCOVER, result.get(1).get(j)[0], result.get(1).get(j)[1]));
			}
			if (result.get(0).size() == 0 && result.get(0).size() == 0) {
				if (result.get(3).size() > 0) {
					toUncover.add(new Action(ACTION.UNCOVER, result.get(3).get(0)[0], result.get(3).get(0)[1]));
				}
			}
		}
		if (!toFlag.isEmpty()) { 
			nextAction = toFlag.poll(); 
			}
		else if (!toUncover.isEmpty()) { nextAction = toUncover.poll(); }
		else {
			// if toUncover and toFlag are both empty
			// rather than get a random action, evaluate each cell with a probability that it might be a mine
			nextAction = this.getRandomAction();	
		}	
		// before we send back the result to the world, we need to translate the coordinate into a world format
		lastX = nextAction.x;
		lastY = nextAction.y;
		lastA = nextAction.action;
		int[] worldXY = this.translateToWorld(lastX, lastY);
		return new Action(lastA, worldXY[0], worldXY[1]);
	}

	// ################### Helper Functions Go Here (optional) ##################
	// ...
	
	// push the potential candidates (all free neighbors or all mine neighbors of cell [row][col]) to the corresponding queue
	private void pushCandidates(int row, int col) {
		if (isAFN(row, col, board[row][col])) {
		    // put all the neighbors to the toUncover queue
		    List<int[]> toUncoverList = getCoveredNeighbors(row,col);
		    for(int[] coordinate: toUncoverList){
		    	// add Uncover action to the queue
		    	// System.out.println("added new action uncover: ("+coordinate[0]+", "+coordinate[1]+")");  	
		    	this.toUncover.add(new Action(ACTION.UNCOVER,coordinate[0],coordinate[1]));	    	
		    }
		}
		else if (isAMN(row, col, board[row][col])) {
		    // put all the neighbors to the toFlag queue
			List<int[]> toFlagList = getCoveredNeighbors(row,col);
		    for(int[] coordinate:toFlagList){
		    	this.toFlag.add(new Action(ACTION.FLAG,coordinate[0],coordinate[1]));
		    }
	    }
	}
	
	// return if the [x,y] has all free neighbors
	private boolean isAFN(int x, int y, int number) {
		int[] countNeighborAndMine = countNeighborsAndMines(this.board, x,y);
		if (number - countNeighborAndMine[1] == 0) return true;	   
		return false;
	}
	
	// return if the [x,y] has all mine neighbors
	private boolean isAMN(int x, int y, int number) {
		int[] countNeighborAndMine = countNeighborsAndMines(this.board, x,y);
		if(countNeighborAndMine[0] == number - countNeighborAndMine[1]) return true;	 
		return false;
	}
	
	// return all the covered neighbors, possibly free neighbors or mines
	private List<int[]> getCoveredNeighbors(int x, int y) {
		List<int[]> neighbors = new ArrayList<int[]>();
		for (int row = x - 1; row <= x + 1; row++) {
			for (int col = y - 1; col <= y + 1; col++) {
				if (row == x && col == y) continue;
				if (this.isValid(row, col) && !visited[row][col] && board[row][col] == -2) {
					neighbors.add(new int[] {row, col});
					visited[row][col] = true;
				}
			}
		}
		return neighbors;
	}

	
	// return the number of [covered neighbors, uncovered mines]
	private int[] countNeighborsAndMines(int[][] board, int x, int y) {
		int coveredNeighbors = 0;
		int uncoveredMines = 0;
		for (int row = x - 1; row <= x + 1; row++) {
			for (int col = y - 1; col <= y + 1; col++) {
				if (row == x && col == y) continue; 
				if (this.isValid(row, col) ) {
					if (board[row][col] == -1) { uncoveredMines++; } // -1: mines
					else if (board[row][col] == -2) { coveredNeighbors++; }
				}
			}
		}
		return new int[] {coveredNeighbors, uncoveredMines};
	}
	
	/*
	 * a backtracking algorithm to solve the problem 
	 * when we have no AMN & AFN nodes
	 * problem: too many flags
	 */
	class CSPSolver {
		private int ROW_DIMENSION;
		private int COL_DIMENSION;
		private int TOTAL_MINES;
		private int[][] solverBoard;
		private List<List<int[]>> minesAndSafe; // [List<int[]> Mines, List<int[]> Safes, List<int[]> possibleMines, List<int[]> possibleSafe]
		private List<boolean[]> solutions;
		public CSPSolver() {
			this.ROW_DIMENSION = MyAI.this.ROW_DIMENSION;
			this.COL_DIMENSION = MyAI.this.COL_DIMENSION;
			this.TOTAL_MINES = MyAI.this.TOTAL_MINES;
			this.solverBoard = this.copyBoard(MyAI.this.board);
			// solutions: a list of solution, solution[i] = ture => borderTiles.get(i) = mine
			this.solutions = new ArrayList<>();
			this.minesAndSafe = new ArrayList<>();
			for (int i = 0; i < 4; i++) { minesAndSafe.add(new ArrayList<int[]>()); }
			solver();
		}
		
		public List<List<int[]>> getResult() {
			return this.minesAndSafe;
		}
		
		private void solver() {
			long startTime = System.currentTimeMillis(); // record the current time
			/*
			 * Optimization Part
			 * need to segregate the border tiles
			 * now we just set them in a list
			 * but we can segregate them into different parts
			 */
			boolean[][] isBorder = new boolean[this.ROW_DIMENSION][this.COL_DIMENSION]; // whether a tile is on the border
			// List<int[]> coveredTiles = new ArrayList<>();
			
			int uncoveredMines = 0;
			int nonBorderCoveredTiles = 0;
			// save all the border tiles' information to the array isBorder
			for (int i = 0; i < this.ROW_DIMENSION; i++) {
				for (int j = 0; j < this.COL_DIMENSION; j++) {
					if (this.isBorder(this.solverBoard, i, j)) {
						isBorder[i][j] = true;
					} else if (this.solverBoard[i][j] == -1) {
						uncoveredMines++;
					} else if (this.solverBoard[i][j] == -2) {
						nonBorderCoveredTiles++;
					}
				}
			}
			
			/* we need to segregate the tiles
			 * segregate进行划分 -> dfs -> find the connected componenets
			 */
			List<List<int[]>> segregatedTiles = this.segregate(isBorder);
			if (segregatedTiles.size() == 0) return;
			
			/*
			 * Each element in the segregatedTiles is a set of independent tiles
			 * Each time, we create a new solutions set to save the solutions for a set of tiles
			 * To prevent that the solverBoard will be revised, every time we copy the board from myAI
			 * We need to get the safetyIndex globally
			 * @safetyThreshold: the possibility of randomly choosing in non-border regions
			 * = left mines / (covered region - border size)
			 */
			double safetyThreshold = (double) (this.TOTAL_MINES - uncoveredMines)  / nonBorderCoveredTiles; // the threshold for a node to be safe
			int[] safetyTile = new int[] {-1, -1};
			
			System.out.println("segregatedTiles size = " + segregatedTiles.size());
			for (List<int[]> borderTiles : segregatedTiles) {
				System.out.println("borderTiles size = " + borderTiles.size());
				this.solutions = new ArrayList<boolean[]>();
				this.solverBoard = this.copyBoard(MyAI.this.board);
				backtracking(borderTiles, isBorder, 0, 0, 0); // set the prevX and prevY as 0
				// if no solutions find, return
				if (this.solutions.size() == 0) { continue; }
				
				/*
				 * From all the solutions, how many cases is this tile segregated.get(i).get(j) to be a mine?
				 * if solution[j] = true => a mine
				 * if solution[j] = false => not a mine
				 */
				List<Double> possibilities = new ArrayList<Double>();
				// System.out.println("check segregated.get(i).size() == solutions.size()? "+ (segregated.get(i).size() == solutions.get(0).length));
				for (int j = 0; j < borderTiles.size(); j++) {
					int mines = 0; // the possibility to be a mine in all solutions
					for (boolean[] solution : this.solutions) {
						mines += solution[j] ? 1 : 0;
					}
					double pMine = (double)mines / this.solutions.size();
					possibilities.add(pMine);
				}
				
				/*
				 * Traverse the possibility array, return a list of as safeToUncover of mines
				 * if the possibilities[j] == 1, then it is definitely a mine
				 * else if possibilities[j] == 0, then it is definitely safe
				 * then we should set some threshold to sieve the remaining answers
				 */
				for (int j = 0; j < possibilities.size(); j++) {
					if (possibilities.get(j) == 1.0) { 
						this.minesAndSafe.get(0).add(borderTiles.get(j));
					} else if (possibilities.get(j) == 0.0) {
						this.minesAndSafe.get(1).add(borderTiles.get(j));
					} else if (possibilities.get(j) < safetyThreshold) {
						safetyThreshold = possibilities.get(j);
						safetyTile = borderTiles.get(j);
					}
				}	
			}
			// don't add unnecessary tiles
			// add only when we don't have 100 percent confidence
			if (this.minesAndSafe.get(0).size() == 0 && this.minesAndSafe.get(1).size() == 0) {
				if (safetyTile[0] != -1) {
					this.minesAndSafe.get(3).add(safetyTile);
					System.out.println("add the safest tile with possibility: "+safetyThreshold);
				} else {
					this.minesAndSafe.get(3).add(this.getRandomTile(isBorder));
				}
			}
			long endTime = System.currentTimeMillis();
			System.out.println("running time: "+(endTime - startTime));	
		}
		
		/*
		 * Given a list of borderTiles, return the segregated list
		 * each element in the list is an independent set of border tiles
		 * using dfs to generate the segragated list
		 */
		private List<List<int[]>> segregate(boolean[][] isBorder) {
			/*
			 * start from any node, run dfs if it is on border 
			 * implement a "visited" array to record the visiting information of each tile
			 * DO NOT MODIFY THE PARAMETERS
			 */
			boolean[][] visited = new boolean[this.ROW_DIMENSION][this.COL_DIMENSION];
			List<List<int[]>> segregatedTiles = new ArrayList<>();
			int level = 0;
			for (int i = 0; i < this.ROW_DIMENSION; i++) {
				for (int j = 0; j < this.COL_DIMENSION; j++) {
					if (isBorder[i][j] && !visited[i][j]) {
						dfs(i, j, level, isBorder, visited, segregatedTiles);
						level++;
					}
				}
			}
			return segregatedTiles;
		}
		
		/*
		 * i, j: the coordinate of the current tile
		 * level: the current level (or connected id) of all the connected tiles
		 * isBorder: if a tile is on the border
		 * visited: if a tile is already visited
		 */
		private void dfs(int i, int j, int level, boolean[][] isBorder, boolean[][] visited, List<List<int[]>> segregatedTiles) {
			if (level == segregatedTiles.size()) {
				segregatedTiles.add(new ArrayList<int[]>());
			}
			visited[i][j] = true;
			segregatedTiles.get(level).add(new int[] {i, j});
			// check the surrounding 4 tiles, if they are borders && they are not visited
			// run dfs on it
			
			/* ensures that 
			 * 1. the [row, col] is valid 
			 * 2. the [row, col] is on the border and has not been visited
			 */
			if (MyAI.this.isValid(i-1, j) && isBorder[i-1][j] && !visited[i-1][j]) {
				dfs(i-1, j, level, isBorder, visited, segregatedTiles);
			}
			if (MyAI.this.isValid(i+1, j) && isBorder[i+1][j] && !visited[i+1][j]) {
				dfs(i+1, j, level, isBorder, visited, segregatedTiles);
			}
			if (MyAI.this.isValid(i, j-1) && isBorder[i][j-1] && !visited[i][j-1]) {
				dfs(i, j-1, level, isBorder, visited, segregatedTiles);
			}
			if (MyAI.this.isValid(i, j+1) && isBorder[i][j+1] && !visited[i][j+1]) {
				dfs(i, j+1, level, isBorder, visited, segregatedTiles);
			}
		}
		
		
		/*
		 * This method enumerate every possible assignments for the borderTiles
		 * If there is an answer, we add it to the solution
		 * In the loop, we check every possible node that will be influenced
		 * 1. whether the uncoveredMines > num: if we find more mines than theoretical value, it's not possible
		 * 2. if the maximum number of mines [uncoveredMines + coveredNeighbors] < num, it's impossible as well
		 * 3. if the total mines > TOTAL_MINES, it is impossible
		 */
		private void backtracking(List<int[]> borderTiles, boolean[][] isBorder, int prevX, int prevY, int index) {
			// here we just make the judge locally to save memory
			// we only check the influence of the previous assigned tile
			if (index > 0) {
				for (int i = prevX - 1; i <= prevX + 1; i++) {
					for (int j = prevY - 1; j <= prevY + 1; j++) {
						// if (this.solverBoard[i][j] == -1) { totalMines++; }
						// if the index is not valid OR the index is on the border OR it's already a mine, we don't check it
						if (!MyAI.this.isValid(i, j) || isBorder[i][j] || solverBoard[i][j] < 0) continue;
						
						int[] neighborsAndMines = countNeighborsAndMines(this.solverBoard, i, j);
						int uncoveredMines = neighborsAndMines[1];
						int coveredNeighbors = neighborsAndMines[0];
						if (uncoveredMines > solverBoard[i][j]) return;
						if (solverBoard[i][j] - coveredNeighbors > uncoveredMines) return;
					}
				}
			}
			// if (totalMines > this.TOTAL_MINES) return; // not necessarily needed
			if (index == borderTiles.size()) {
				boolean[] solution =  new boolean[borderTiles.size()];
				for (int i = 0; i < borderTiles.size(); i++) {
					int[] coordinates = borderTiles.get(i);
					int x = coordinates[0], y = coordinates[1];
					// solution can only be -1 or 0
					solution[i] = this.solverBoard[x][y] == -1 ? true : false;
				}
				solutions.add(solution);
				return;
			}
			
			// when we assgn values, we simply assign mines -> -1
			// and not mines -> 0
			// since we will not check the border, we will only check the uncovered not-mine tiles
			int[] coordinates = borderTiles.get(index);
			int x = coordinates[0], y = coordinates[1];
			this.solverBoard[x][y] = -1;
			backtracking(borderTiles, isBorder, x, y, index + 1);
			this.solverBoard[x][y] = 0;
			backtracking(borderTiles, isBorder, x, y, index + 1);
			this.solverBoard[x][y] = -2;			
		}
		
		/*
		 * deep copy of the current board
		 */
		private int[][] copyBoard(int[][] board) {
			int[][] newBoard = new int[this.ROW_DIMENSION][this.COL_DIMENSION];
			for (int i = 0; i < this.ROW_DIMENSION; i++) {
				for (int j = 0; j < this.COL_DIMENSION; j++) {
					newBoard[i][j] = board[i][j];
				}
			}	
			return newBoard;
		}
		
		/*
		 * returns whether a tile is a border
		 * 1. the tile must be equals to -2
		 * 2. one of the neighbor of the tile must be >= 0
		 */
		private boolean isBorder(int[][] board, int x, int y) {
			if (board[x][y] != -2) return false;
			for (int row = x - 1; row <= x + 1; row++) {
				for (int col = y - 1; col <= y + 1; col++) {
					if (MyAI.this.isValid(row, col) && board[row][col] >= 0) {
						return true;
					}
				}
			}
			return false;
		}	
		
		/*
		 * return a random action which is not a border
		 */
		private int[] getRandomTile(boolean[][] isBorder) {
			int randX, randY;
			while (true) {
				randX = (int) (Math.random() * this.ROW_DIMENSION);
				randY = (int) (Math.random() * this.COL_DIMENSION);
				if (MyAI.this.board[randX][randY] == -2 && !isBorder[randX][randY]) break;
			}	
			return new int[]{randX, randY};
		}
	}
	
	// return if the coordinate (x, y) is valid
	private boolean isValid(int x, int y) {
		if (x < 0 || y < 0 || x >= this.ROW_DIMENSION || y >= this.COL_DIMENSION) return false;
		return true;
	}
	
	private Action getRandomAction() {
		int randX, randY;
		while (true) {
			randX = (int) (Math.random() * this.ROW_DIMENSION);
			randY = (int) (Math.random() * this.COL_DIMENSION);
			if (board[randX][randY] == -2) break;
		}	
		return new Action(ACTION.UNCOVER, randX, randY);
	}
	
	//
	private int[] translateToBoard(int x, int y) {
		/*	Inputs:
		 * 		x - x coordinate of board
		 * 		y - y coordinate of board 
		 * 
		 * 	Outputs:
		 * 		TwoTuple, t, where:
		 * 			t.x is the corresponding row index in the board
		 * 			t.y is the corresponding col index in the board
		 * 
		 * 	Description:
		 * 	Translates the given (x,y) coordinate to a (row, col) tuple used
		 * 	for indexing into the board instance variable. (See Note below).
		 * 
		 * 	Notes:
		 * 	The internal representation of a board is a 2-d array, 0-indexed 
		 * 	array. However, users, specify locations on the board using 1-indexed
		 * 	(x,y) Cartesian coordinates. 
		 * 	Hence, to access the proper indicies into the board array, a translation 
		 * 	must be performed first.
		 */
		return new int[] {this.ROW_DIMENSION - y , x - 1};
	}
	
	private int[] translateToWorld(int x, int y) {
		return new int[] {y + 1, this.ROW_DIMENSION - x};
	}
}





