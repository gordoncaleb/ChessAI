package chessIO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import chessBackend.Board;
import chessBackend.BoardMaker;
import chessBackend.Move;

public class MoveBook {

	Hashtable<Long, ArrayList<Long>> hashMoveBook;

	Hashtable<String, ArrayList<Long>> verboseMoveBook;

	boolean stillValid = true;

	public MoveBook() {

	}

	public static void main(String[] args) {
		Hashtable<Long, ArrayList<Long>> moveBook = MoveBook.moveBookFromPGNFile("book.pgn");

		Hashtable<Long, ArrayList<Long>> loadedMoveBook = loadCompiledMoveBook("book.pgn.compiled");

		System.out.println("Verifying saved and loaded are =");
		ArrayList<Long> hashCodes = Collections.list(moveBook.keys());
		ArrayList<ArrayList<Long>> moves = new ArrayList<ArrayList<Long>>(moveBook.values());

		ArrayList<Long> loadedMoves = new ArrayList<Long>();

		int moveCount = 0;
		int maxMove = 0;
		if (hashCodes.size() == loadedMoveBook.size()) {

			for (int i = 0; i < hashCodes.size(); i++) {
				loadedMoves = loadedMoveBook.get(hashCodes.get(i));

				if (loadedMoves != null) {

					if (moves.get(i).size() == loadedMoves.size()) {
						maxMove = Math.max(moves.get(i).size(), maxMove);

						for (int m = 0; m < moves.get(i).size(); m++) {
							if (Move.equals(moves.get(i).get(m), loadedMoves.get(m))) {
								moveCount++;
							} else {
								System.out.println("error! moves not =");
								break;
							}
						}

					} else {
						System.out.println("error! move size not =");
						break;
					}
				} else {
					System.out.println("hashcode missing in loaded move book " + Long.toHexString(hashCodes.get(i)));
					System.out.println("error! hashcodes not =");

					break;
				}

			}

		} else {
			System.out.println("error! hashcodes not same size");
		}

		System.out.println("Done!");

		System.out.println("Move Count = " + moveCount);
		System.out.println("max move Count = " + maxMove);
		System.out.println("Hash Count = " + hashCodes.size());

	}

	public long getRecommendation(Long hashCode) {
		long move = 0;

		if (stillValid) {
			ArrayList<Long> moves = hashMoveBook.get(hashCode);

			if (moves != null && moves.size() > 0) {
				double maxIndex = (double) (moves.size() - 1);
				double randDouble = Math.random();
				double randIndexDouble = randDouble * maxIndex;
				long randIndex = Math.round(randIndexDouble);
				move = moves.get((int) randIndex);
			} else {
				stillValid = false;
			}
		}

		return move;
	}

	public ArrayList<Long> getAllRecommendations(Long hashCode) {

		ArrayList<Long> moves = hashMoveBook.get(hashCode);

		return moves;
	}

	public void removeEntry(String xmlBoard, Long hashcode, Move move) {
		ArrayList<Long> verboseEntries = verboseMoveBook.get(xmlBoard);
		ArrayList<Long> entries = hashMoveBook.get(hashcode);

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
		// hashMoveBook =
		// XMLParser.XMLToMoveBook(FileIO.readFile("moveBook.xml"));
		hashMoveBook = loadCompiledMoveBook("book.pgn.compiled");
	}

	private void loadVerboseMoveBook() {
		verboseMoveBook = XMLParser.XMLToVerboseMoveBook(FileIO.readFile("verboseMoveBook.xml"));
		loadMoveBook();
	}
	
	public void setStillValid(boolean stillValid){
		this.stillValid = stillValid;
	}


	public void saveMoveBook() {

		String xmlMoveBook = this.toXML();
		String xmlVerboseMoveBook = this.toVerboseXML();

		FileIO.writeFile("moveBook.xml", xmlMoveBook, false);
		FileIO.writeFile("verboseMoveBook.xml", xmlVerboseMoveBook, false);

	}

	public void addEntry(String xmlBoard, Long hashcode, long move) {
		ArrayList<Long> verboseEntries = verboseMoveBook.get(xmlBoard);
		ArrayList<Long> entries = hashMoveBook.get(hashcode);

		if (entries != null) {
			if (!entries.contains(move)) {
				entries.add(move);
			} else {
				return;
			}
		} else {
			entries = new ArrayList<Long>();
			entries.add(move);
			hashMoveBook.put(hashcode, entries);
		}

		if (verboseEntries != null) {
			verboseEntries.add(move);
		} else {
			verboseEntries = new ArrayList<Long>();
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
		for (ArrayList<Long> moves : hashMoveBook.values()) {
			xmlMoveBook += "<entry>\n";

			xmlMoveBook += "<state>\n";

			xmlMoveBook += "<hashcode>";

			xmlMoveBook += Long.toHexString(keys.elementAt(i));

			xmlMoveBook += "</hashcode>\n";

			xmlMoveBook += "</state>\n";

			xmlMoveBook += "<response>\n";

			for (int m = 0; m < moves.size(); m++) {
				xmlMoveBook += Move.toXML(moves.get(m));
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
		for (ArrayList<Long> moves : verboseMoveBook.values()) {
			xmlMoveBook += "<entry>\n";

			xmlMoveBook += "<state>\n";

			xmlMoveBook += keys.elementAt(i);

			xmlMoveBook += "</state>\n";

			xmlMoveBook += "<response>\n";

			for (int m = 0; m < moves.size(); m++) {
				xmlMoveBook += Move.toXML(moves.get(m));
			}

			xmlMoveBook += "</response>\n";

			xmlMoveBook += "</entry>\n";
			i++;
		}

		xmlMoveBook += "</moveBook>";
		return xmlMoveBook;
	}

	public static Hashtable<Long, ArrayList<Long>> moveBookFromPGNFile(String fileName) {

		long time1 = System.currentTimeMillis();
		Hashtable<Long, ArrayList<Long>> moveBook = new Hashtable<Long, ArrayList<Long>>();

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

			// System.out.println("NEW GAME " + gameLines.get(i));
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
			ArrayList<Long> moves;
			for (int n = 0; n < notations.size(); n++) {

				move = board.resolveAlgebraicNotation(notations.get(n));

				// System.out.println(notations.get(n) + " => " + (new
				// Move(move)));

				moves = moveBook.get(board.getHashCode());
				if (moves != null) {
					if (!moves.contains(move)) {
						moves.add(move);
					}
				} else {
					moves = new ArrayList<Long>();
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
		System.out.println("Took " + (System.currentTimeMillis() - time1) + "ms to load");
		System.out.println("Hash Count = " + moveBook.size());

		saveCompiledMoveBook(moveBook);

		return moveBook;
	}

	public static void saveCompiledMoveBook(Hashtable<Long, ArrayList<Long>> moveBook) {

		DataOutputStream dout = FileIO.getDataOutputStream("book.pgn.compiled");

		ArrayList<Long> hashCodes = Collections.list(moveBook.keys());
		ArrayList<ArrayList<Long>> moves = new ArrayList<ArrayList<Long>>(moveBook.values());

		System.out.println("Creating compiled movebook file");
		try {
			for (int i = 0; i < hashCodes.size(); i++) {

				dout.writeLong(hashCodes.get(i));

				for (int m = 0; m < moves.get(i).size(); m++) {
					dout.writeShort((int) (Move.fromToMask & moves.get(i).get(m)));
				}

				dout.writeShort(-1);
			}
			System.out.println("Done!");

			dout.close();

		} catch (IOException e) {
			System.out.println("File io exception");
		}
	}

	public static Hashtable<Long, ArrayList<Long>> loadCompiledMoveBook(String fileName) {

		long time1 = System.currentTimeMillis();

		System.out.println("Loading compiled book");

		DataInputStream din = FileIO.getDataInputStream(fileName);

		if (din == null) {
			System.out.println("Couldnt open compiled file");
		}

		Hashtable<Long, ArrayList<Long>> moveBook = new Hashtable<Long, ArrayList<Long>>();

		Long hashCode;
		short move;
		ArrayList<Long> moves;

		boolean eof = false;
		while (!eof) {
			try {
				hashCode = din.readLong();
				moves = new ArrayList<Long>();

				while ((move = din.readShort()) != -1) {
					moves.add((long) move);
				}

				moveBook.put(hashCode, moves);

			} catch (EOFException e) {
				eof = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			din.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done");

		System.out.println("Compiled Book took " + (System.currentTimeMillis() - time1) + "ms  to load");

		return moveBook;
	}

}
