package chessIO;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import chessBackend.Board;
import chessBackend.BoardMaker;
import chessBackend.Move;

public class MoveBook {

	Hashtable<Long, Vector<Long>> hashMoveBook;

	Hashtable<String, Vector<Long>> verboseMoveBook;

	public MoveBook() {

	}

	public static void main(String[] args) {
		MoveBook.moveBookFromPGNFile("book.pgn");
	}

	public long getRecommendation(Long hashCode) {
		long move = 0;

		Vector<Long> moves = hashMoveBook.get(hashCode);

		if (moves != null && moves.size() > 0) {
			double maxIndex = (double) (moves.size() - 1);
			double randDouble = Math.random();
			double randIndexDouble = randDouble * maxIndex;
			long randIndex = Math.round(randIndexDouble);
			move = moves.elementAt((int) randIndex);
		}

		return move;
	}

	public Vector<Long> getAllRecommendations(Long hashCode) {

		Vector<Long> moves = hashMoveBook.get(hashCode);

		return moves;
	}

	public void removeEntry(String xmlBoard, Long hashcode, Move move) {
		Vector<Long> verboseEntries = verboseMoveBook.get(xmlBoard);
		Vector<Long> entries = hashMoveBook.get(hashcode);

		if (verboseEntries != null) {
			verboseEntries.remove(move.getMoveLong());

			if (verboseEntries.size() == 0) {
				verboseMoveBook.remove(xmlBoard);
			}
		}

		if (entries != null) {
			entries.remove(move.getMoveLong());

			if (entries.size() == 0) {
				hashMoveBook.remove(hashcode);
			}
		}
	}

	public void loadMoveBook() {
		//hashMoveBook = XMLParser.XMLToMoveBook(FileIO.readFile("moveBook.xml"));
		hashMoveBook = moveBookFromPGNFile("book.pgn");
	}

	public void loadVerboseMoveBook() {
		verboseMoveBook = XMLParser.XMLToVerboseMoveBook(FileIO.readFile("verboseMoveBook.xml"));
		loadMoveBook();
	}

	public void loadPGNMoveBook() {

	}

	public void saveMoveBook() {

		String xmlMoveBook = this.toXML();
		String xmlVerboseMoveBook = this.toVerboseXML();

		FileIO.writeFile("moveBook.xml", xmlMoveBook, false);
		FileIO.writeFile("verboseMoveBook.xml", xmlVerboseMoveBook, false);

	}

	public void addEntry(String xmlBoard, Long hashcode, long move) {
		Vector<Long> verboseEntries = verboseMoveBook.get(xmlBoard);
		Vector<Long> entries = hashMoveBook.get(hashcode);

		if (entries != null) {
			if (!entries.contains(move)) {
				entries.add(move);
			} else {
				return;
			}
		} else {
			entries = new Vector<Long>();
			entries.add(move);
			hashMoveBook.put(hashcode, entries);
		}

		if (verboseEntries != null) {
			verboseEntries.add(move);
		} else {
			verboseEntries = new Vector<Long>();
			verboseEntries.add(move);
			verboseMoveBook.put(xmlBoard, verboseEntries);
		}

	}

	public String toXML() {
		if (hashMoveBook == null) {
			return "";
		}

		String xmlMoveBook = "<moveBook>\n";

		int i = 0;
		Vector<Long> keys = new Vector<Long>(hashMoveBook.keySet());
		for (Vector<Long> moves : hashMoveBook.values()) {
			xmlMoveBook += "<entry>\n";

			xmlMoveBook += "<state>\n";

			xmlMoveBook += "<hashcode>";

			xmlMoveBook += Long.toHexString(keys.elementAt(i));

			xmlMoveBook += "</hashcode>\n";

			xmlMoveBook += "</state>\n";

			xmlMoveBook += "<response>\n";

			for (int m = 0; m < moves.size(); m++) {
				xmlMoveBook += Move.toXML(moves.elementAt(m));
			}

			xmlMoveBook += "</response>\n";

			xmlMoveBook += "</entry>\n";
			i++;
		}

		xmlMoveBook += "</moveBook>";
		return xmlMoveBook;
	}

	public String toVerboseXML() {

		if (verboseMoveBook == null) {
			return "";
		}

		String xmlMoveBook = "<moveBook>\n";

		int i = 0;
		Vector<String> keys = new Vector<String>(verboseMoveBook.keySet());
		for (Vector<Long> moves : verboseMoveBook.values()) {
			xmlMoveBook += "<entry>\n";

			xmlMoveBook += "<state>\n";

			xmlMoveBook += keys.elementAt(i);

			xmlMoveBook += "</state>\n";

			xmlMoveBook += "<response>\n";

			for (int m = 0; m < moves.size(); m++) {
				xmlMoveBook += Move.toXML(moves.elementAt(m));
			}

			xmlMoveBook += "</response>\n";

			xmlMoveBook += "</entry>\n";
			i++;
		}

		xmlMoveBook += "</moveBook>";
		return xmlMoveBook;
	}

	public static Hashtable<Long, Vector<Long>> moveBookFromPGNFile(String fileName) {

		
		long time1 = System.currentTimeMillis();
		Hashtable<Long, Vector<Long>> moveBook = new Hashtable<Long, Vector<Long>>();

		String contents = FileIO.readFile(fileName);

		String[] lines = contents.split("\n");

		ArrayList<String> gameLines = new ArrayList<String>();
		String gameLine = "";
		boolean gameLineStarted = false;

		for (int i = 0; i < lines.length; i++) {
			if (!lines[i].trim().startsWith("[") && !lines[i].trim().equals("")) {
				gameLine += lines[i].trim();
				gameLineStarted = true;
			} else {
				if (gameLineStarted) {
					gameLines.add(gameLine);
					gameLine = "";
				}
				gameLineStarted = false;
			}
		}

		if (gameLineStarted) {
			gameLines.add(gameLine);
		}

		Board board = BoardMaker.getStandardChessBoard();

		ArrayList<String> notations = new ArrayList<String>();
		String[] tokens;
		String token;
		for (int i = 0; i < gameLines.size(); i++) {

			//System.out.println("NEW GAME " + gameLines.get(i));
			tokens = gameLines.get(i).split(" ");

			for (int n = 0; n < tokens.length; n++) {

				token = tokens[n].trim();

				if (token.contains(".")) {
					token = token.substring(token.indexOf(".") + 1);
				}

				if (token.contains("O-O-O")) {
					token = "O-O-O";
				} else {
					if (token.contains("O-O")) {
						token = "O-O";
					}
				}

				if (!token.equals("O-O") && !token.equals("O-O-O")) {
					while (token.length() > 1) {
						if (!Character.isLowerCase(token.charAt(token.length() - 2)) || !Character.isDigit(token.charAt(token.length() - 1))) {
							token = token.substring(0, token.length() - 1);
						} else {
							break;
						}
					}
				}

				if (!token.equals("") && token.length() > 1) {
					notations.add(token);
				}
			}

			long move;
			Vector<Long> moves;
			for (int n = 0; n < notations.size(); n++) {

				move = board.resolveAlgebraicNotation(notations.get(n));

				//System.out.println(notations.get(n) + " => " + (new Move(move)));

				moves = moveBook.get(board.getHashCode());
				if (moves != null) {
					moves.add(move);
				} else {
					moves = new Vector<Long>();
					moves.add(move);
					moveBook.put(board.getHashCode(), moves);
				}

				board.makeMove(move);
			}

			while (board.canUndo()) {
				board.undoMove();
			}

			notations.clear();

		}
		
		System.out.println("Move book loaded!!!!");
		System.out.println("Took " + (System.currentTimeMillis()-time1) + "ms to load");

		return moveBook;
	}
	
	public void saveCompiledMoveBook(){
		
	}

}
