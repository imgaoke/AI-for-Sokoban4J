import static java.lang.System.out;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;
import java.util.*;

import agents.ArtificialAgent;
import game.actions.EDirection;
import game.actions.compact.*;
import game.board.compact.BoardCompact;
import game.board.compact.CTile;
import search.*;


class DeadSquareDetector {
		
	public static class Pair<T> {
		private T first;
		private T second;
		public Pair(T first, T second){
			this.first = first;
			this.second = second;
		}
		public T getFirst(){ return first; }
		public T getSecond(){ return second; }
		public void setFirst(T first){ this.first = first; }
		public void setSecond(T second){ this.second = second; }
	}
	
	
	public static class Graph {
		BoardCompact state;
		boolean[][] graph;
		ArrayList<Pair<Integer>> targets = new ArrayList<Pair<Integer>>();
		boolean[][] explored;
		ArrayList<EDirection> directions = new ArrayList<EDirection>();


		public Graph(BoardCompact state) {
			this.state = state;
			graph = new boolean[state.width()][state.height()]; // true means wall, false means free
			explored = new boolean[state.width()][state.height()]; 

			for(int i = 0; i < state.width(); i++){
				for(int j = 0; j < state.height(); j++){
					if (CTile.isWall(state.tile(i, j))){
						graph[i][j] = true;
					}
					else if (CTile.forSomeBox(state.tile(i, j))){
						targets.add(new Pair<Integer>(i, j));
					}
				}
			}
		}
		
	}
	static void DFS (Graph g, Pair<Integer> target, boolean[][] deadSqaures){
		g.explored[target.first][target.second] = true;
		for (var dir : EDirection.arrows()) {
			var interestedTile = new Pair<Integer>(target.first + dir.dX, target.second + dir.dY);
			if (CAction.isOnBoard(g.state, target.first, target.second, dir) &&
				CAction.isOnBoard(g.state, target.first + dir.dX, target.second + dir.dY, dir) &&
				CAction.isOnBoard(g.state, target.first + 2 * dir.dX, target.second + 2 * dir.dY, dir) &&
				g.graph[target.first + dir.dX][target.second + dir.dY] == false &&			//the position one step further is free
				g.graph[target.first + 2 * dir.dX][target.second + 2 * dir.dY] == false	&& 	// the position one step further is pushable
				!g.explored[target.first + dir.dX][target.second + dir.dY]					// the interestedTile is not explored
			)
			{
				deadSqaures[target.first + dir.dX][target.second + dir.dY] = false;
				DFS(g, interestedTile, deadSqaures);
			}	
		}		
	}
	
	public static boolean[][] detect(BoardCompact state){
		Graph g = new Graph(state);
		var finalDeadSqaures = new boolean[state.width()][state.height()];
		
		for (int i = 0; i < state.width(); i++){
			for (int j = 0; j < state.height(); j++){
				finalDeadSqaures[i][j] = true;
			}
		}
		for (var start : g.targets) {
			var tempDeadSquare = new boolean[state.width()][state.height()];
			for (int i = 0; i < state.width(); i++){
				for (int j = 0; j < state.height(); j++){
					tempDeadSquare[i][j] = true;
				}
			}
			tempDeadSquare[start.first][start.second] = false;
			DFS(g, start, tempDeadSquare);

			for (int i = 0; i < state.width(); i++){
				for (int j = 0; j < state.height(); j++){
					finalDeadSqaures[i][j] = finalDeadSqaures[i][j] && tempDeadSquare[i][j];
				}
			}
		}
		return finalDeadSqaures;
	};
};
public class MyAgent extends ArtificialAgent {
	protected BoardCompact board;
	protected int searchedNodes;

	

	public class SokobanMaze implements HeuristicProblem<BoardCompact, CAction> {
		public BoardCompact initialState() { 
			return board.clone(); 
		}
		public List<CAction> actions(BoardCompact state) { 

			List<CAction> list = new ArrayList<CAction>();

			for (CMove move : CMove.getActions()) {
				if (move.isPossible(state)) {
					list.add(move);
				}
			}
			for (CPush push : CPush.getActions()) {
				if (push.isPossible(state) && !DeadSquareDetector.detect(state)[state.playerX + 2 * push.getDirection().dX][state.playerY + 2 * push.getDirection().dY]){
					list.add(push);
				}
			}
			return list; 
		}
		public BoardCompact result(BoardCompact state, CAction action) {
			var newState = state.clone();
			action.perform(newState);
			return newState;
		}

		public boolean isGoal(BoardCompact state) { 
			var result = false;
			if (state.isVictory()) {
				result = true;
			}
			return result;
		}

		public double cost(BoardCompact state, CAction action) {
			return 1.0;
		}
		public double estimate(BoardCompact state){

			return state.boxCount - state.boxInPlaceCount;
		}

	}


	@Override
	protected List<EDirection> think(BoardCompact board) {
		this.board = board;
		searchedNodes = 0;		
		List<EDirection> result = new ArrayList<EDirection>();
		var maze = new SokobanMaze();
		var solution = AStar.search(maze);
		if (solution != null){
			for (CAction action : solution.actions) {
				result.add(action.getDirection());
			}
		}

		return result.isEmpty() ? null : result;
	}
}
