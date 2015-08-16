package com.gordoncaleb.chess.io;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.gordoncaleb.chess.backend.Board;
import com.gordoncaleb.chess.backend.Move;
import com.gordoncaleb.chess.backend.Side;
import com.gordoncaleb.chess.pieces.Piece;

public class XMLParser {
	private static final Logger logger = LoggerFactory.getLogger(XMLParser.class);

	public XMLParser() {

	}

	public static void main(String[] args) {
		String xmlBoard = FileIO.readResource("out.xml");

		XMLParser.XMLToBoard(xmlBoard);
	}

	public static Board XMLToBoard(String xmlBoard) {
		Document doc = XMLParser.getDocument(xmlBoard.replace("\n", ""));

		return buildBoard((Element) doc.getElementsByTagName("board").item(0));
	}

	private static Board buildBoard(Element boardElement) {
		ArrayList<Piece>[] pieces = new ArrayList[2];
		pieces[Side.WHITE.ordinal()] = new ArrayList<Piece>();
		pieces[Side.BLACK.ordinal()] = new ArrayList<Piece>();

		Stack<Move> moveHistory = new Stack<Move>();
		Side player;

		String stringBoard = getCharacterDataFromElement((Element) boardElement.getElementsByTagName("setup").item(0));

		String[] stringPieces = stringBoard.split(",");
		Piece piece;
		String stringPiece;
		for (int row = 0; row < 8; row++) {
			for (int col = 0; col < 8; col++) {

				stringPiece = stringPieces[8 * row + col].trim();
				piece = Piece.fromString(stringPiece, row, col);

				if (piece != null) {
					pieces[piece.getSide().ordinal()].add(piece);
				}

			}
		}

		String turn = getCharacterDataFromElement((Element) boardElement.getElementsByTagName("turn").item(0));

		if (turn.compareTo(Side.WHITE.toString()) == 0) {
			player = Side.WHITE;
		} else {
			player = Side.BLACK;
		}

		NodeList nodes = boardElement.getElementsByTagName("move");
		Long m;
		for (int n = 0; n < nodes.getLength(); n++) {
			m = buildMove((Element) nodes.item(n));
			moveHistory.push(new Move(m));
		}

		if (pieces[Side.BLACK.ordinal()].size() == 0 || pieces[Side.WHITE.ordinal()].size() == 0) {
			logger.debug("Error loading xml board!");
			return null;
		}

		Board newBoard = new Board(pieces, player, new Stack<Move>(), null, null);

		for (int i = 0; i < moveHistory.size(); i++) {
			newBoard.makeMove(moveHistory.elementAt(i).getMoveLong());
		}

		return newBoard;
	}

	public static Piece XMLToPiece(String xmlPiece) {
		Document doc = XMLParser.getDocument(xmlPiece);

		return buildPiece((Element) doc.getElementsByTagName("piece").item(0));
	}

	private static Piece buildPiece(Element pieceElement) {

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

	public static long XMLToMove(String xmlMove) {
		Document doc = XMLParser.getDocument(xmlMove);

		return buildMove((Element) doc.getElementsByTagName("move").item(0));
	}

	private static long buildMove(Element moveElement) {

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
		Move.MoveNote moveNote;

		if (nodes.getLength() != 0) {
			moveNote = Move.MoveNote.valueOf(getCharacterDataFromElement((Element) nodes.item(0)));
		} else {
			moveNote = Move.MoveNote.NONE;
		}

		nodes = moveElement.getElementsByTagName("piece");

		Piece pieceTaken = null;

		if (nodes.getLength() != 0) {
			pieceTaken = buildPiece((Element) nodes.item(0));
		}

		return Move.moveLong(fromRow, fromCol, toRow, toCol, 0, moveNote, pieceTaken, hadMoved);
	}

	public static Hashtable<Long, ArrayList<Long>> XMLToMoveBook(String xmlMoveBook) {
		Document doc = XMLParser.getDocument(xmlMoveBook);

		Hashtable<Long, ArrayList<Long>> moveBook = new Hashtable<Long, ArrayList<Long>>();
		Long hashcode = null;
		ArrayList<Long> moves;

		NodeList entires = doc.getElementsByTagName("entry");

		NodeList stateTag;
		NodeList hashTag;
		NodeList boardTag;
		NodeList responseTag;
		NodeList moveTags;
		for (int i = 0; i < entires.getLength(); i++) {

			stateTag = ((Element) entires.item(i)).getElementsByTagName("state");

			hashTag = ((Element) stateTag.item(0)).getElementsByTagName("hashcode");

			if (hashTag.getLength() == 0) {
				boardTag = ((Element) stateTag.item(0)).getElementsByTagName("board");
				hashcode = buildBoard((Element) boardTag.item(0)).getHashCode();
			} else {
				hashcode = (new BigInteger(getCharacterDataFromElement((Element) hashTag.item(0)).trim(), 16)).longValue();
			}

			responseTag = ((Element) entires.item(i)).getElementsByTagName("response");

			moveTags = ((Element) responseTag.item(0)).getElementsByTagName("move");
			moves = new ArrayList<Long>(moveTags.getLength());
			for (int m = 0; m < moveTags.getLength(); m++) {
				moves.add(buildMove((Element) moveTags.item(m)));
			}

			if (moves.size() > 0) {
				ArrayList<Long> oldMoves = moveBook.get(hashcode);
				if (oldMoves != null) {
					moves.addAll(oldMoves);
				}

				moveBook.put(hashcode, moves);
			}
		}

		return moveBook;
	}

	public static Map<String, List<Long>> XMLToVerboseMoveBook(String xmlVerboseMoveBook) {
		Document doc = XMLParser.getDocument(xmlVerboseMoveBook);

		Map<String, List<Long>> moveBook = new HashMap<>();
		ArrayList<Long> moves;

		NodeList main = doc.getElementsByTagName("moveBook");

		NodeList entries = ((Element) main.item(0)).getElementsByTagName("entry");

		NodeList stateTag;
		NodeList boardTag;
		String xmlBoard;
		NodeList responseTag;
		NodeList moveTags;
		for (int i = 0; i < entries.getLength(); i++) {

			stateTag = ((Element) entries.item(i)).getElementsByTagName("state");

			boardTag = ((Element) stateTag.item(0)).getElementsByTagName("board");
			xmlBoard = buildBoard((Element) boardTag.item(0)).toXML(false);

			responseTag = ((Element) entries.item(i)).getElementsByTagName("response");

			moveTags = ((Element) responseTag.item(0)).getElementsByTagName("move");
			moves = new ArrayList<Long>(moveTags.getLength());
			for (int m = 0; m < moveTags.getLength(); m++) {
				moves.add(buildMove((Element) moveTags.item(m)));
			}

			if (moves.size() > 0) {
				moveBook.put(xmlBoard, moves);
			}
		}

		return moveBook;
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
