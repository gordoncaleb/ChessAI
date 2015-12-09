package com.gordoncaleb.chess.ui.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.JFrame;
import javax.swing.JTextArea;

import com.gordoncaleb.chess.engine.legacy.AI;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.BoardFactory;
import com.gordoncaleb.chess.ui.gui.game.Game;
import com.gordoncaleb.chess.ui.gui.game.GameResults;
import com.gordoncaleb.chess.ui.gui.game.Player;
import com.gordoncaleb.chess.board.Side;
import com.gordoncaleb.chess.ui.gui.ethernet.EthernetPlayerClient;
import com.gordoncaleb.chess.util.FileIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TournamentGUI {
	public static Logger logger = LoggerFactory.getLogger(TournamentGUI.class);

	JFrame frame;
	JTextArea statusTxt;

	public static void main(String[] args) throws Exception {

		Game game;
		GameResults results;

		int draws = 0;

		TournamentGUI tournamentGui = new TournamentGUI();

		ObserverGUI observer = new ObserverGUI(null, false);

		// Game game = new Game(GameType.AI_VS_AI);

		Player playerOne = new AI(null);
		// Player playerTwo = new AI(null, debug);

		Player playerTwo = new EthernetPlayerClient();

		String playerOneVersion = playerOne.getVersion();
		String playerTwoVersion = playerTwo.getVersion();

		logger.info("Player One: " + playerOneVersion);
		logger.info("Player Two: " + playerTwoVersion);

		((AI) playerOne).setUseBook(true);

		Hashtable<Integer, Player> players = new Hashtable<>();

		players.put(Side.WHITE, playerOne);
		players.put(Side.BLACK, playerTwo);

		Hashtable<Player, String> playerNames = new Hashtable<Player, String>();

		playerNames.put(playerOne, playerOneVersion);
		playerNames.put(playerTwo, playerTwoVersion);

		Hashtable<String, Long[]> playerScore = new Hashtable<>();

		ArrayList<Long> boardsPlayed = new ArrayList<>();

		// blackwins, whitewins ,winby,numMoves,time,maxTime,good draws,
		// bad draws, draw by pts, caused stalemate, caused invalid
		Long[] temp1 = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };
		Long[] temp2 = { 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L };
		playerScore.put(playerNames.get(playerOne), temp1);
		playerScore.put(playerNames.get(playerTwo), temp2);

		game = new Game(players);

		playerOne.setGame(game);
		playerTwo.setGame(game);
		game.addObserver(observer);

		FileIO.clearDirectory(".\\tournament");

		boolean play960 = true;

		Board board;
		Long[] winnerScore;
		Long[] loserScore;
		int numOfGames = 960;
		for (int i = 0; i < numOfGames; i++) {

			if (play960) {
				board = BoardFactory.getRandomChess960Board();
				while (boardsPlayed.contains(board.getHashCode())) {
					board = BoardFactory.getRandomChess960Board();
				}
			} else {
				board = BoardFactory.getStandardChessBoard();
			}

			for (int s = 0; s < 2; s++) {

				logger.info("Game#" + (i * 2 + s));

				logger.info(board.toString());

				results = game.newGame(board, true);

				winnerScore = playerScore.get(playerNames.get(players.get(results.getWinner())));
				loserScore = playerScore.get(playerNames.get(players.get(Side.otherSide(results.getWinner()))));

				FileIO.writeFile((".\\tournament\\game" + (i * 2 + s) + "_" + results.getEndGameStatus() + ".json"), ((AI) playerOne).getBoard().toJson(true), false);

				if (results.getEndGameStatus() == Game.GameStatus.CHECKMATE) {

					winnerScore[results.getWinner()]++;
					winnerScore[2] += results.getWinBy();
					winnerScore[3] += results.getNumOfMoves() / 2;
					winnerScore[4] += results.getTime(results.getWinner());
					if (winnerScore[5] < results.getMaxTime(results.getWinner())) {
						winnerScore[5] = results.getMaxTime(results.getWinner());
					}

					loserScore[3] += results.getNumOfMoves() / 2;
					loserScore[4] += results.getTime(Side.otherSide(results.getWinner()));
					if (loserScore[5] < results.getMaxTime(Side.otherSide(results.getWinner()))) {
						loserScore[5] = results.getMaxTime(Side.otherSide(results.getWinner()));
					}
				} else {
					if (results.getEndGameStatus() == Game.GameStatus.DRAW || results.getEndGameStatus() == Game.GameStatus.STALEMATE) {
						if (results.getWinBy() < 0) {
							winnerScore[6]++;
							loserScore[7]++;
						} else {
							winnerScore[7]++;
							loserScore[6]++;
						}

						winnerScore[8] += results.getWinBy();
						loserScore[8] -= results.getWinBy();

						draws++;

						if (results.getEndGameStatus() == Game.GameStatus.STALEMATE) {
							winnerScore[9]++;
						}

					} else {
						if (results.getEndGameStatus() == Game.GameStatus.INVALID) {
							loserScore[10]++;
						}
					}
				}

				String playerOneName = playerNames.get(playerOne);
				Long[] playerOneScore = playerScore.get(playerOneName);

				String out1 = getScoreResults(playerOneScore, playerOneName);

				String playerTwoName = playerNames.get(playerTwo);
				Long[] playerTwoScore = playerScore.get(playerTwoName);

				String out2 = getScoreResults(playerTwoScore, playerTwoName);

				String outResults = out1 + "\n" + out2 + "Draws: " + draws + "\nGames played: " + (i * 2 + s + 1) + "/" + numOfGames * 2 + " - "
						+ (numOfGames * 2 - i * 2 - s - 1) + " left";

				FileIO.writeFile(".\\tournament\\results.txt", outResults, false);

				tournamentGui.setStatusTxt(outResults);

				// switch player sides
				Player whitePlayer = players.get(Side.WHITE);
				players.put(Side.WHITE, players.get(Side.BLACK));
				players.put(Side.BLACK, whitePlayer);

			}

		}

		logger.info("Tournament done");
	}

	public TournamentGUI() {

		frame = new JFrame("Tournament");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());

		frame.setVisible(true);

		frame.setPreferredSize(new Dimension(500, 500));
		statusTxt = new JTextArea();

		// statusTxt.setFont(new Font("Courier New", Font.ITALIC, 45));
		// statusTxt.setHorizontalAlignment(SwingConstants.CENTER);

		frame.add(statusTxt, BorderLayout.CENTER);
		frame.pack();
	}

	public void setStatusTxt(String stats) {
		statusTxt.setText(stats);
	}

	public static String getScoreResults(Long[] score, String playerName) {

		// score[11] = {
		// [0]blackwins, [1]whitewins , [2]winby, [3]numMoves, [4]time,
		// [5]maxTime, [6]good draws,
		// [7]bad draws, [8]draw by pts, [9]caused stalemate,
		// [10]caused invalid}

		int totalWins = (int) (score[0] + score[1]);
		double avgWinby = (double) score[2] / ((double) totalWins);
		double avgTimePerMove = (double) score[4] / (double) (score[3]);
		int totalDraws = (int) (score[6] + score[7]);
		double avgDrawPts = (double) score[8] / ((double) totalDraws);

		String out = "Player: " + playerName + "\n";
		out += "Total wins: " + totalWins + " (" + score[Side.BLACK] + " black, " + score[Side.WHITE] + " white)\n";
		out += "Average pts won by: " + avgWinby + "\n";
		out += "Draws: " + totalDraws + " (" + score[6] + " good, " + score[7] + " bad)\n";
		out += "Caused Stalemate: " + score[9] + "\n";
		out += "Caused Invalid: " + score[10] + "\n";
		out += "Average draw by pts: " + avgDrawPts + "\n";
		out += "Average time per move: " + avgTimePerMove + "\n";
		out += "Max time on move: " + score[5] + "\n";

		return out;
	}

	public enum GameType {
        AI_VS_AI,TWO_GUI,ONE_GUI,GUI_VS_AI
    }
}