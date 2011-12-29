package chessAI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import chessBackend.Move;

public class MoveBook {
	DataOutputStream dout;
	private MoveBookNode moveBookRootNode;
	private MoveBookNode currentPosition;
	private boolean stillInGameSpace = true;
	private boolean invertedTable;

	public MoveBook() {

		moveBookRootNode = new MoveBookNode(null, null);

		loadDB();

		currentPosition = moveBookRootNode;

	}

	public Move getRecommendation() {
		Move move = null;
		if (stillInGameSpace) {
			if (invertedTable) {
				move = invertMove(currentPosition.getChosenChild().getMove());
			} else {
				move = currentPosition.getChosenChild().getMove();
			}
		}

		return move;
	}

	public boolean hasRecommendation() {
		return stillInGameSpace;
	}

	public void moved(Move move) {
		boolean foundMove = false;
		int childrenSize = currentPosition.getChildrenSize();

		MoveBookNode currentChild = currentPosition.getHeadChild();
		for (int i = 0; i < childrenSize; i++) {
			if (currentChild.getMove().equals(move)) {
				foundMove = true;
				currentPosition = currentChild;
				System.out.println("Found move in DB!");

				if (!currentPosition.hasChildren()) {
					System.out.println("Reached end of Good Move DB game space");
					stillInGameSpace = false;
				}

				break;
			}

			currentChild = currentChild.getNextSibling();
		}

		if (!foundMove) {
			System.out.println("No move + " + move.toString() + " in DB");
			stillInGameSpace = false;
		}
	}

	public void loadDB() {
		MoveBookNode currentNode = moveBookRootNode;
		MoveBookNode newNode;
		try {

			DataInputStream din = new DataInputStream(new FileInputStream("opening_book.bin"));

			boolean fileStarted = false;
			boolean nextIsSibling = false;
			Move move;
			int pathValue;
			short val;

			readFile: while (true) {
				val = din.readShort();

				switch (val) {
				case 0x7EEE:
					if (!fileStarted) {
						fileStarted = true;

					} else {
						break readFile;
					}
					break;
				case 0x7FFF:
					nextIsSibling = true;
					break;
				case 0x7DDD:
					nextIsSibling = true;
					currentNode = currentNode.getParent();
					break;
				default:

					move = getMove(val);
					pathValue = din.readShort();

					if (nextIsSibling) {
						newNode = new MoveBookNode(currentNode.getParent(), move);
						currentNode.getParent().addChild(newNode);
					} else {
						newNode = new MoveBookNode(currentNode, move);
						currentNode.addChild(newNode);
					}

					currentNode = newNode;
					currentNode.setMoveBookValue(pathValue);

					System.out.println(newNode.toString());

					nextIsSibling = false;

					break;

				}
			}

			din.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void saveDB() {
		try {
			dout = new DataOutputStream(new FileOutputStream("opening_book.bin"));

			dout.writeShort(0x7EEE);

			saveNode(moveBookRootNode);

			dout.writeShort(0x7EEE);

			dout.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveNode(MoveBookNode node) throws IOException {

		if (node.getMove() != null) {
			dout.writeShort(getMove(node.getMove()));
			dout.writeShort((short) node.getMoveBookValue());
		}

		if (node.hasChildren()) {
			int childrenSize = node.getChildrenSize();
			MoveBookNode currentChild = node.getHeadChild();
			for (int i = 0; i < childrenSize; i++) {
				saveNode(currentChild);

				if (i == (childrenSize - 1)) {
					dout.writeShort(0x7DDD);
				} else {
					dout.writeShort(0x7FFF);
				}
				currentChild = currentChild.getNextSibling();
			}
		}
	}

	private Move getMove(short move) {

		int fromRow = (move & 0xF000) >> 12;
		int fromCol = (move & 0x0F00) >> 8;
		int toRow = (move & 0x00F0) >> 4;
		int toCol = (move & 0x000F);

		return new Move(fromRow, fromCol, toRow, toCol);
	}

	private short getMove(Move move) {
		int fromRow = move.getFromRow();
		int fromCol = move.getFromCol();
		int toRow = move.getToRow();
		int toCol = move.getToCol();

		short val = (short) ((fromRow << 12) | (fromCol << 8) | (toRow << 4) | toCol);

		return val;
	}

	private Move invertMove(Move move) {
		return new Move(7 - move.getFromRow(), 7 - move.getFromCol(), 7 - move.getToRow(), 7 - move.getToCol());
	}

	public void newGame() {
		stillInGameSpace = true;
		currentPosition = moveBookRootNode;
	}

	public MoveBookNode getMoveBookRootNode() {
		return moveBookRootNode;
	}

	public void setInvertedTable(boolean invertedTable) {
		this.invertedTable = invertedTable;
	}
}
