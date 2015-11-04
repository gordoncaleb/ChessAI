package com.gordoncaleb.chess.ui.gui.ethernet;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gordoncaleb.chess.board.Board;
import com.gordoncaleb.chess.board.Move;
import com.gordoncaleb.chess.board.serdes.JSONParser;
import com.gordoncaleb.chess.ui.gui.game.Game;
import com.gordoncaleb.chess.ui.gui.game.Player;
import com.gordoncaleb.chess.ui.gui.game.PlayerContainer;
import com.gordoncaleb.chess.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EthernetPlayerClient implements Player, EthernetMsgRxer {
    private static final Logger logger = LoggerFactory.getLogger(EthernetPlayerClient.class);

    private PlayerContainer game;

    private Socket clientSocket;

    private int port = 1234;
    private String dest = "localhost";

    private String payload;

    public EthernetPlayerClient() {
        EthernetMsgServer server = new EthernetMsgServer(this, 2345);
        server.start();

    }

    private void sendMessage(String message) {

        try {
            if (clientSocket == null) {
                clientSocket = new Socket(dest, port);
            } else {
                if (clientSocket.isClosed()) {
                    clientSocket = new Socket(dest, port);
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            logger.info("Connection to ethernet player could not be made.");
        }

        EthernetMsgServer.sendMessage(message, clientSocket);

    }

    public synchronized void newMessage(String message) {

        int tagStart = message.indexOf("<");
        int tagEnd = message.indexOf(">");

        if (tagStart < 0 || tagEnd < 0) {
            return;
        }

        String tag = message.substring(tagStart, tagEnd + 1);
        String payload = message.substring(tagEnd + 1, message.length());

        // logger.debug("Message tag = " + tag);

        try {
            switch (tag) {

                case "<move>":
                    game.makeMove(JSON.fromJSON(message, Move.class));
                    break;
                case "<newGame>":
                    game.newGame(null, false);
                    break;
                case "<board>":
                    game.newGame(JSONParser.fromJSON(message), false);
                    break;
                case "<undoMove>":
                    game.undoMove();
                    break;
                case "<pause>":
                    game.pause();
                    break;
                case "<version>":
                    this.payload = payload;
                    break;
                case "<progress>":
                    game.showProgress(Integer.parseInt(payload));
                    break;
                case "<recommendation>":
                    game.recommendationMade(Move.fromLong(Long.parseLong(payload)));
                    break;
                case "<recommend>":
                    game.requestRecommendation();
                    break;
                default:
                    logger.debug("Client unrecognized command received: \n" + message);
                    break;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.notifyAll();

        // logger.debug("Rx:\n" + message);
    }

    @Override
    public Move undoMove() {
        sendMessage("<undoMove>");
        return null;
    }

    @Override
    public void newGame(Board board) {
        try {
            sendMessage(board.toJson(true));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setGame(PlayerContainer game) {
        this.game = game;

    }

    @Override
    public void makeMove() {
        sendMessage("<makeMove>");

    }

    @Override
    public boolean moveMade(Move move) {
        try {
            sendMessage(move.toJson());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void pause() {
        sendMessage("<pause>");
    }

    @Override
    public Game.GameStatus getGameStatus() {

        return null;
    }

    @Override
    public Board getBoard() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getVersion() {
        sendMessage("<version>");

        return getResponse();
    }

    public String getResponse() {

        synchronized (this) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return payload;

    }

    @Override
    public void connectionReset() {
        game.endGame();
    }

    public void gameOver(int winlose) {
        if (winlose > 0) {
            sendMessage("<gameOver>win");
        } else {
            if (winlose < 0) {
                sendMessage("<gameOver>lose");
            } else {
                sendMessage("<gameOver>draw");
            }
        }
    }

    @Override
    public void endGame() {
        try {
            clientSocket.close();
            clientSocket = null;

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void showProgress(int progress) {
        //sendMessage("<progress>" + progress);
    }

    @Override
    public void requestRecommendation() {
        sendMessage("<recommend>");
    }

    @Override
    public void recommendationMade(Move move) {
        sendMessage("<recommendation>" + move.toLong());
    }

}
