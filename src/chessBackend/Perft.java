package chessBackend;

import java.util.ArrayList;

import chessAI.AI;

public class Perft {

	public int[] level = new int[20];

	public int[] sizes = { 1, 20, 400, 8902, 197281, 4865609 };

	// 7978ms@5 home
	// 3189ms@5 work

	public static void main(String[] args) {
		Perft p = new Perft();

		long t1 = System.currentTimeMillis();

		Board board = BoardMaker.getStandardChessBoard();
		
		p.grow(5,board);

		System.out.println("Took " + (System.currentTimeMillis() - t1) + "ms");

		for (int i = 0; i < 20; i++) {
			System.out.println("Level " + i + ": " + p.level[i]);
			if (p.level[i] == 0) {
				break;
			}
		}
	}

	public void grow(int depth, Board board) {
		level[depth]++;
		if (depth > 0) {
			board.makeNullMove();
			
			ArrayList<Long> moves = new ArrayList<Long>(board.generateValidMoves(true, 0, AI.noKillerMoves));

			for (int i = 0; i < moves.size(); i++) {
				board.makeMove(moves.get(i));
				grow(depth - 1,board);
				board.undoMove();
			}
		}

	}
	
}
