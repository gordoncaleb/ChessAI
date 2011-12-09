package chessPieces;

import java.util.Vector;

public class PiecePosition {
	Piece piecePresent;
	Vector<Piece> possibleEnemyPieces;
	
	public PiecePosition(Piece piecePresent, Vector<Piece> possibleEnemyPieces){
		this.piecePresent = piecePresent;
		this.possibleEnemyPieces = possibleEnemyPieces;
	}
	
	public PiecePosition(){
		possibleEnemyPieces = new Vector<Piece>();
	}
	
	public boolean hasPossibleEnemyPieces(){
		return (possibleEnemyPieces.size() != 0);
	}
	
	public void addPossibleEnemyPieces(Piece piece){
		possibleEnemyPieces.add(piece);
	}
	
	public void removePossibleEnemyPieces(Piece piece){
		possibleEnemyPieces.remove(piece);
	}
	
	public void clearPossibleEnemyPieces(){
		possibleEnemyPieces = new Vector<Piece>();
	}
	
	public void setPiecePresent(Piece piecePresent){
		this.piecePresent = piecePresent;
	}
	
	public void clearPiecePresent(){
		piecePresent = null;
	}
	
	public Piece getPiecePresent(){
		return piecePresent;
	}
	
	public boolean hasPiecePresent(){
		return (piecePresent!=null);
	}
	
	public PiecePosition getCopy(){
		Vector<Piece> copyPossibleEnemyPieces = new Vector<Piece>();
		
		for(int i=0;i<possibleEnemyPieces.size();i++){
			copyPossibleEnemyPieces.add(possibleEnemyPieces.elementAt(i).getCopy());
		}
		
		if(piecePresent!=null)	
			return new PiecePosition(piecePresent.getCopy(),copyPossibleEnemyPieces);
		else
			return new PiecePosition(null,copyPossibleEnemyPieces);
	}
}
