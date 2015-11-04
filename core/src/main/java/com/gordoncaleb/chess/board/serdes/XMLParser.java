package com.gordoncaleb.chess.board.serdes;

import java.io.StringReader;
import java.util.*;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.gordoncaleb.chess.util.FileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.board.pieces.Piece;

import static com.gordoncaleb.chess.board.pieces.Piece.PieceID.*;

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
        pieces[Side.WHITE] = new ArrayList<>();
        pieces[Side.BLACK] = new ArrayList<>();

        int player;
        String stringBoard = getCharacterDataFromElement((Element) boardElement.getElementsByTagName("setup").item(0));

        String[] stringPieces = stringBoard.split(",");
        Piece piece;
        String stringPiece;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {

                stringPiece = stringPieces[8 * row + col].trim();
                piece = pieceFromString(stringPiece, row, col);

                if (piece != null) {
                    pieces[piece.getSide()].add(piece);
                }

            }
        }

        String turn = getCharacterDataFromElement((Element) boardElement.getElementsByTagName("turn").item(0));

        if (turn.compareTo(Side.toString(Side.WHITE)) == 0) {
            player = Side.WHITE;
        } else {
            player = Side.BLACK;
        }

        NodeList nodes = boardElement.getElementsByTagName("move");
        Stack<Move> moveHistory = new Stack<>();
        for (int n = 0; n < nodes.getLength(); n++) {
            Move m = buildMove((Element) nodes.item(n));
            moveHistory.push(m);
        }

        if (pieces[Side.BLACK].size() == 0 || pieces[Side.WHITE].size() == 0) {
            logger.debug("Error loading xml board!");
            return null;
        }

        Board newBoard = new Board(pieces, player, new ArrayDeque<>());

        for (int i = 0; i < moveHistory.size(); i++) {
            newBoard.makeMove(moveHistory.elementAt(i));
        }

        return newBoard;
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

        return pieceFromString(id, row, col);
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

        return new Move(fromRow, fromCol, toRow, toCol, 0, moveNote, pieceTaken, hadMoved);
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

    private static String getCharacterDataFromElement(Element e) {

        Node child = e.getFirstChild();
        if (child instanceof CharacterData) {
            CharacterData cd = (CharacterData) child;
            return cd.getData();
        }
        return "";
    }

    public static String boardToXML(Board b, boolean includeHistory) {
        String xmlBoard = "<board>\n";

        if (includeHistory) {

            Stack<Move> movesToRedo = new Stack<>();
            Move m;
            while ((m = b.undoMove()) != null) {
                movesToRedo.push(m);
            }

            xmlBoard += "<setup>\n" + b.toString() + "</setup>\n";
            xmlBoard += "<turn>" + Side.toString(b.getTurn()) + "</turn>\n";

            while (!movesToRedo.isEmpty()) {
                b.makeMove(movesToRedo.pop());
                xmlBoard += "<setup>\n" + b.toString() + "</setup>\n";
            }

            for (Move move : b.getMoveHistory()) {
                xmlBoard += move.toXML();
            }

        } else {
            xmlBoard += "<setup>\n" + b.toString() + "</setup>\n";
            xmlBoard += "<turn>" + Side.toString(b.getTurn()) + "</turn>\n";
        }

        xmlBoard += "</board>";
        return xmlBoard;
    }

    public static String moveToXML(Move m) {
        String xmlMove = "";

        xmlMove += "<move>\n";

        xmlMove += "<from>" + m.getFromRow() + "," + m.getFromCol() + "</from>\n";
        xmlMove += "<to>" + m.getToRow() + "," + m.getToCol() + "</to>\n";

        if (m.hadMoved()) {
            xmlMove += "<had_moved>" + m.hadMoved() + "</had_moved>\n";
        }

        Move.MoveNote note = m.getNote();
        if (note != Move.MoveNote.NONE) {
            xmlMove += "<note>" + note.toString() + "</note>\n";
        }

        if (m.hasPieceTaken()) {
            xmlMove += m.getPieceTaken().toXML();
        }

        xmlMove += "</move>\n";

        return xmlMove;
    }

    public static String pieceToXML(Piece p) {
        String xmlPiece = "";

        xmlPiece += "<piece>\n";
        xmlPiece += "<id>" + p.toString() + "</id>\n";
        xmlPiece += "<has_moved>" + p.getHasMoved() + "</has_moved>\n";
        xmlPiece += "<position>" + p.getRow() + "," + p.getCol() + "</position>\n";
        xmlPiece += "</piece>\n";

        return xmlPiece;
    }

    private static Piece pieceFromString(String stringPiece, int row, int col) {
        int player;

        boolean hasMoved = false;

        try {
            if (Integer.parseInt(stringPiece.substring(1, 2)) % 2 != 0) {
                hasMoved = true;
            }
        } catch (Exception e) {
            //Ignoring with reason
        }

        if (stringPiece.charAt(0) < 'a') {
            player = Side.BLACK;
        } else {
            player = Side.WHITE;
        }

        int id;
        char type = stringPiece.toUpperCase().charAt(0);

        id = charIDtoPieceID(type);

        if (id != NONE) {
            return new Piece(id, player, row, col, hasMoved);
        } else {
            return null;
        }

    }

    private static int charIDtoPieceID(char type) {

        int id;

        switch (type) {
            case 'R':
                id = ROOK;
                break;
            case 'N':
                id = KNIGHT;
                break;
            case 'B':
                id = BISHOP;
                break;
            case 'Q':
                id = QUEEN;
                break;
            case 'K':
                id = KING;
                break;
            case 'P':
                id = PAWN;
                break;
            default:
                id = NONE;
        }

        return id;
    }

}
