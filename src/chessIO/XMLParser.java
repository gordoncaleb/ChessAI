package chessIO;

import java.io.IOException;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import chessBackend.BitBoard;
import chessBackend.Board;
import chessBackend.Move;
import chessBackend.MoveNote;
import chessBackend.Side;
import chessBackend.RNGTable;
import chessPieces.Piece;
import chessPieces.PieceID;

public class XMLParser {

	public XMLParser() {

	}

	public static void main(String[] args) {
		String xmlBoard = FileIO.readFile("out.xml");

		Board board = XMLParser.XMLToBoard(xmlBoard);
	}

	public static Board XMLToBoard(String xmlBoard) {
		Document doc = XMLParser.getDocument(xmlBoard);

		Piece[][] board = new Piece[8][8];
		Vector<Piece> blackPieces = new Vector<Piece>();
		Vector<Piece> whitePieces = new Vector<Piece>();
		Piece whiteKing = null;
		Piece blackKing = null;
		long[] posBitBoard = { 0, 0 };

		Stack<Move> moveHistory = new Stack<Move>();
		Side player;

		String stringBoard = getCharacterDataFromElement((Element) doc.getElementsByTagName("setup").item(0));

		String[] stringPieces = stringBoard.split(",");
		Piece piece;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				piece = Piece.fromString(stringPieces[8 * row + col].trim(), row, col);

				board[row][col] = piece;

				if (piece != null) {

					posBitBoard[piece.getSide().ordinal()] |= BitBoard.getMask(row, col);

					if (piece.getSide() == Side.BLACK) {
						blackPieces.add(piece);

						if (piece.getPieceID() == PieceID.KING)
							blackKing = piece;
					} else {
						whitePieces.add(piece);

						if (piece.getPieceID() == PieceID.KING)
							whiteKing = piece;
					}
				}

			}
		}

		String turn = getCharacterDataFromElement((Element) doc.getElementsByTagName("turn").item(0));

		if (turn.compareTo(Side.WHITE.toString()) == 0) {
			player = Side.WHITE;
		} else {
			player = Side.BLACK;
		}

		NodeList nodes = doc.getElementsByTagName("move");
		Move m;
		for (int n = 0; n < nodes.getLength(); n++) {
			m = buildMove((Element) nodes.item(n));
			moveHistory.push(m);
		}

		if (blackPieces.size() == 0 || whitePieces.size() == 0 || blackKing == null || whiteKing == null) {
			System.out.println("Error loading xml board!");
			return null;
		}

		return new Board(board, blackPieces, whitePieces, posBitBoard, blackKing, whiteKing, player, moveHistory, null, new RNGTable());
	}

	public static Move XMLToMove(String xmlMove) {
		Document doc = XMLParser.getDocument(xmlMove);

		return buildMove((Element) doc.getElementsByTagName("move").item(0));
	}

	private static Move buildMove(Element moveElement) {

		NodeList nodes;

		String[] from = getCharacterDataFromElement((Element) moveElement.getElementsByTagName("from").item(0)).split(",");
		String[] to = getCharacterDataFromElement((Element) moveElement.getElementsByTagName("to").item(0)).split(",");

		int fromRow = Integer.parseInt(from[0].trim());
		int fromCol = Integer.parseInt(from[1].trim());
		int toRow = Integer.parseInt(to[0].trim());
		int toCol = Integer.parseInt(to[1].trim());

		nodes = moveElement.getElementsByTagName("had_moved");
		boolean hadMoved = false;

		if (nodes.getLength() != 0) {
			String stringHadMoved = getCharacterDataFromElement((Element) nodes.item(0));

			if (stringHadMoved.compareTo("true") == 0) {
				hadMoved = true;
			}

		}

		nodes = moveElement.getElementsByTagName("note");
		MoveNote moveNote;

		if (nodes.getLength() != 0) {
			moveNote = MoveNote.valueOf(getCharacterDataFromElement((Element) nodes.item(0)));
		} else {
			moveNote = MoveNote.NONE;
		}

		nodes = moveElement.getElementsByTagName("piece");

		Piece pieceTaken = null;

		if (nodes.getLength() != 0) {
			pieceTaken = buildPiece((Element) nodes.item(0));
		}

		return new Move(fromRow, fromCol, toRow, toCol, 0, moveNote, pieceTaken, hadMoved);
	}

	public static Piece XMLToPiece(String xmlPiece) {
		Document doc = XMLParser.getDocument(xmlPiece);

		return buildPiece((Element) doc.getElementsByTagName("piece").item(0));
	}

	public static Piece buildPiece(Element pieceElement) {

		String id = getCharacterDataFromElement((Element) pieceElement.getElementsByTagName("id").item(0));
		String stringHasMoved = getCharacterDataFromElement((Element) pieceElement.getElementsByTagName("has_moved").item(0));

		if (stringHasMoved.compareTo("true") == 0) {
			id += "1";
		} else {
			id += "0";
		}

		String[] position = getCharacterDataFromElement((Element) pieceElement.getElementsByTagName("position").item(0)).split(",");

		int row = Integer.parseInt(position[0].trim());
		int col = Integer.parseInt(position[1].trim());

		return Piece.fromString(id, row, col);
	}

	public static Hashtable<Long, Vector<Move>> XMLToMoveBook(String xmlMoveBook) {
		Document doc = XMLParser.getDocument(xmlMoveBook);

		Hashtable<Long, Vector<Move>> moveBook = new Hashtable<Long, Vector<Move>>();
		Long hashcode = null;
		Vector<Move> moves;

		NodeList nodes = doc.getElementsByTagName("entry");

		NodeList state;
		NodeList response;
		NodeList moveNodes;
		for (int i = 0; i < nodes.getLength(); i++) {

			state = ((Element) nodes.item(i)).getElementsByTagName("state");
			hashcode = XMLToHashcode(getCharacterDataFromElement((Element) state.item(0)));

			if (hashcode == null) {
				hashcode = Board.getHashCode(getCharacterDataFromElement((Element) state.item(0)));
				if (hashcode == null) {
					System.out.println("Error parsing move book");
					continue;
				}
			}

			response = ((Element) nodes.item(i)).getElementsByTagName("response");

			moveNodes = ((Element) response.item(i)).getElementsByTagName("move");
			moves = new Vector<Move>(moveNodes.getLength());
			for (int m = 0; m < moveNodes.getLength(); m++) {
				moves.add(buildMove((Element) moveNodes.item(m)));
			}

			if (moves.size() > 0) {
				Vector<Move> oldMoves = moveBook.get(hashcode);
				if (oldMoves != null) {
					moves.addAll(oldMoves);
				}

				moveBook.put(hashcode, moves);
			}
		}

		return moveBook;
	}

	public static Hashtable<String, Vector<Move>> XMLToVerboseMoveBook(String xmlVerboseMoveBook) {
		Document doc = XMLParser.getDocument(xmlVerboseMoveBook);

		Hashtable<String, Vector<Move>> moveBook = new Hashtable<String, Vector<Move>>();
		String stringState = null;
		Vector<Move> moves;

		NodeList main = doc.getElementsByTagName("moveBook");

		NodeList entries = ((Element)main.item(0)).getElementsByTagName("entry");

		NodeList state;
		NodeList response;
		NodeList moveNodes;
		for (int i = 0; i < entries.getLength(); i++) {

			state = ((Element) entries.item(i)).getElementsByTagName("state");
			stringState = getCharacterDataFromElement((Element) state.item(0));

			response = ((Element) entries.item(i)).getElementsByTagName("response");

			moveNodes = ((Element) response.item(i)).getElementsByTagName("move");
			moves = new Vector<Move>(moveNodes.getLength());
			for (int m = 0; m < moveNodes.getLength(); m++) {
				moves.add(buildMove((Element) moveNodes.item(m)));
			}

			if (moves.size() > 0) {
				moveBook.put(stringState, moves);
			}
		}

		return moveBook;
	}

	public static Long XMLToHashcode(String xmlHashcode) {
		Document doc = XMLParser.getDocument(xmlHashcode);

		Long hashcode = null;
		NodeList nodes = doc.getElementsByTagName("hashcode");

		if (nodes.getLength() != 0) {
			String stringHashcode = getCharacterDataFromElement((Element) nodes.item(0));
			hashcode = Long.parseLong(stringHashcode, 16);
		}

		return hashcode;

	}

	private static Document getDocument(String xml) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		Document doc = null;

		// Using factory get an instance of document builder
		try {
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xml));
			doc = db.parse(is);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return doc;
	}

	public static String getCharacterDataFromElement(Element e) {

		Node child = e.getFirstChild();
		if (child instanceof CharacterData) {
			CharacterData cd = (CharacterData) child;
			return cd.getData();
		}
		return "";
	}

}
