package chessIO;

import java.util.Hashtable;
import java.util.Vector;

import chessBackend.Move;

public class MoveBook {

	Hashtable<Long, Vector<Long>> hashMoveBook;

	Hashtable<String, Vector<Long>> verboseMoveBook;

	public MoveBook() {

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
		hashMoveBook = XMLParser.XMLToMoveBook(FileIO.readFile("moveBook.xml"));
	}

	public void loadVerboseMoveBook() {
		verboseMoveBook = XMLParser.XMLToVerboseMoveBook(FileIO.readFile("verboseMoveBook.xml"));
		loadMoveBook();
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

}
