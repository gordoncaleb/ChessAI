package chessIO;

import chessBackend.Move;

public class MoveBook {
	

	public MoveBook() {
		loadDB();

	}

	public Move getRecommendation(Long hashCode) {
		Move move = null;


		return move;
	}

	public void loadDB() {

	}

	public void saveDB() {

	}

	private Move invertMove(Move move) {
		return new Move(7 - move.getFromRow(), 7 - move.getFromCol(), 7 - move.getToRow(), 7 - move.getToCol());
	}

}
