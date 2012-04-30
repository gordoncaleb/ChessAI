package chessIO;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import chessBackend.Move;

public class MoveBook {

	Hashtable<Long, Vector<Move>> hashMoveBook;

	Hashtable<String, Vector<Move>> verboseMoveBook;

	public MoveBook() {

	}

	public Move getRecommendation(Long hashCode) {
		Move move = null;

		Vector<Move> moves = hashMoveBook.get(hashCode);

		if (moves != null && moves.size() > 0) {
			double maxIndex = (double) (moves.size() - 1);
			int randInt = (int) Math.round((Math.random() * maxIndex));
			move = moves.elementAt(randInt);
		}

		return move;
	}
	
	public Vector<Move> getAllRecommendations(Long hashCode) {

		Vector<Move> moves = hashMoveBook.get(hashCode);

		return moves;
	}
	
	public void removeEntry(String xmlBoard, Move move){
		Vector<Move> entry = verboseMoveBook.get(xmlBoard);

		if (entry != null) {
			entry.remove(move);
			
			if(entry.size()==0){
				verboseMoveBook.remove(xmlBoard);
			}
		}
	}

	public void loadMoveBook(String fileName) {
		hashMoveBook = XMLParser.XMLToMoveBook(FileIO.readFile(fileName));
	}

	public void loadVerboseMoveBook(String fileName) {
		verboseMoveBook = XMLParser.XMLToVerboseMoveBook(FileIO.readFile(fileName));
		loadMoveBook(fileName);
	}

	public void saveMoveBook() {
		
		String xmlMoveBook = this.toXML();
		String xmlVerboseMoveBook = this.toVerboseXML();
		
		try {
			FileIO.writeFile(new File("moveBook.xml"), xmlMoveBook, false);
			FileIO.writeFile(new File("verboseMoveBook.xml"), xmlVerboseMoveBook, false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

	}

	public void addEntry(String xmlBoard, Move move) {
		Vector<Move> entry = verboseMoveBook.get(xmlBoard);

		if (entry != null) {
			entry.add(move);
		} else {
			entry = new Vector<Move>();
			entry.add(move);
			verboseMoveBook.put(xmlBoard, entry);
		}
	}

	public String toXML() {
		if (hashMoveBook == null) {
			return "";
		}

		String xmlMoveBook = "<moveBook>\n";

		int i = 0;
		Vector<Long> keys = new Vector<Long>(hashMoveBook.keySet());
		for (Vector<Move> moves : hashMoveBook.values()) {
			xmlMoveBook += "<entry>\n";

			xmlMoveBook += "<state>\n";

			xmlMoveBook += "<hashcode>\n";
			
			xmlMoveBook += Long.toHexString(keys.elementAt(i));
			
			xmlMoveBook += "</hashcode>\n";

			xmlMoveBook += "</state>\n";

			xmlMoveBook += "<response>\n";

			for (int m = 0; m < moves.size(); m++) {
				xmlMoveBook += moves.elementAt(m).toXML();
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
		for (Vector<Move> moves : verboseMoveBook.values()) {
			xmlMoveBook += "<entry>\n";

			xmlMoveBook += "<state>\n";

			xmlMoveBook += keys.elementAt(i);

			xmlMoveBook += "</state>\n";

			xmlMoveBook += "<response>\n";

			for (int m = 0; m < moves.size(); m++) {
				xmlMoveBook += moves.elementAt(m).toXML();
			}

			xmlMoveBook += "</response>\n";

			xmlMoveBook += "</entry>\n";
			i++;
		}

		xmlMoveBook += "</moveBook>";
		return xmlMoveBook;
	}

}
