package Model.Controller;

import Model.*;
import Model.Pieces.*;
import Model.IOs.*;
import Model.View.*;

import javax.swing.*;
import java.io.*;
import java.util.*;

/**
 * The central controller (the core of the "C" in MVC).
 * It holds the master GameState and coordinates all communication
 * between the Model and the View.
 */
public class GameController{
    //The Model
    private GameState gameState;
    private RuleEngine ruleEngine;

    //The View components
    private final MainFrame mainFrame;
    private final BoardPanel boardPanel;
    private final SidePanel sidePanel;

    //Helper classes (Persistence)
    private final JsonPersistence jsonPersistence;
    private final PGNFormatter pgnFormatter;
    private final PGNParser pgnParser;

    // State flag to stop interactions when game is over
    private boolean isGameInProgress = false;

    public GameController(MainFrame mainFrame, BoardPanel boardPanel, SidePanel sidePanel){
        this.mainFrame = mainFrame;
        this.boardPanel = boardPanel;
        this.sidePanel = sidePanel;

        //Initialise helper classes
        this.jsonPersistence = new JsonPersistence();
        this.pgnFormatter = new PGNFormatter();
        this.pgnParser = new PGNParser();

        //No game is loaded initially
        this.gameState = null;

        this.ruleEngine = new RuleEngine();
    }

    /**
     * Starts the application.
     * Asks the user if they want to start a new game or load an existing one.
     */
    public void start(){
        Object[] options = {"New Game", "Load Game (JSON)"};
        int choice = JOptionPane.showOptionDialog(mainFrame,
                "Would you like to start a new game or load an existing one?",
                "Start Chess",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]);

        if (choice == JOptionPane.YES_OPTION){
            showNewGameDialog();
        } else {
            handleLoadGame();
        }
    }

    /**
     * Displays the new game settings dialog.
     */
    public void showNewGameDialog(){
        NewGameDialog dialog = new NewGameDialog(mainFrame);
        dialog.setVisible(true);

        if (dialog.isSucceeded()){
            Player white = new Player(dialog.getWhiteName(), dialog.getWhiteElo());
            Player black = new Player(dialog.getBlackName(), dialog.getBlackElo());
            // Timer settings are collected in dialog but GameState doesn't support time yet.
            // We proceed without it for now.
            //TODO: Process timer settings (dialog.isTimerEnabled()...)

            startNewGame(white, black);
        } else {
            //If the user closes the dialog, send them back to the start() menu.
            start();
        }
    }

    /**
     * Starts a new game with the specified players.
     */
    private void startNewGame(Player white, Player black){
        this.gameState = new GameState();
        this.gameState.setPlayers(white, black);
        this.isGameInProgress = true;

        //We refresh the views with the new state.
        refreshAllViews();
    }

    /**
     * Handles the user's move attempt (from click or drag).
     * Asks the RuleEngine to generate a full Move object.
     *
     * @param from The starting position.
     * @param to The target position.
     */
    public void handleMoveAttempt(Position from, Position to){
        if (gameState == null || !isGameInProgress) return;

        // 1. Ask the RuleEngine to generate and validate the move.
        Move move = ruleEngine.generateMove(gameState, from, to);

        if (move != null) {
            // 2. Handle Promotion (if move is tagged)
            if (move.isPromotion()) {
                // We must ask the user what piece they want(by default, a Queen is selected)
                Piece promotionPiece = askForPromotionPiece(gameState.isWhiteTurn());
                move.setPromotionPiece(promotionPiece);
            }

            // 3. Execute the move on the Model
            gameState.makeMove(move);
            refreshAllViews();

            // 4. Check for game-ending conditions
            if (move.isCheckmate()) {
                boardPanel.setKingInCheck(findKingPos(!gameState.isWhiteTurn())); // Highlight loser king
                JOptionPane.showMessageDialog(mainFrame, "Sakk-matt!");
                isGameInProgress = false;
            } else if (move.isCheck()) {
                boardPanel.setKingInCheck(findKingPos(gameState.isWhiteTurn())); // Highlight current king
            } else {
                boardPanel.setKingInCheck(null); // Clear highlight
            }

            if (ruleEngine.isStalemate(gameState)) {
                JOptionPane.showMessageDialog(mainFrame, "Patt!");
                isGameInProgress = false;
            }

        } else {
            // Move was illegal, just reset the GUI
            boardPanel.clearSelections();
            boardPanel.updateBoard(gameState.getBoard()); // Resets the piece
        }
    }

    /**
     * Helper method to show a dialog for pawn promotion.
     */
    private Piece askForPromotionPiece(boolean isWhite) {
        Object[] options = {"Queen", "Rook", "Bishop", "Knight"};
        String pieceName = (String) JOptionPane.showInputDialog(mainFrame,
                "Choose a piece for promotion:",
                "Pawn Promotion",
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

        switch (pieceName) {
            case "Rook": return new Rook(isWhite);
            case "Bishop": return new Bishop(isWhite);
            case "Knight": return new Knight(isWhite);
            case "Queen":
            default: return new Queen(isWhite);
        }
    }

    private Position findKingPos(boolean isWhite) {
        Board b = gameState.getBoard();
        for(int r=0; r<8; r++) {
            for(int c=0; c<8; c++) {
                Position p = new Position(r,c);
                Piece pc = b.getPieceAt(p);
                if(pc != null && pc.getType() == PieceType.KING && pc.isWhite() == isWhite) return p;
            }
        }
        return null;
    }

    /**
     * Handles the "Save (JSON)" menu item.
     */
    public void handleSaveGame(){
        if (gameState == null){
            JOptionPane.showMessageDialog(mainFrame, "Nothing to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Game State (JSON)");
        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();

            // Ensure extension is correct
            try {
                jsonPersistence.saveGame(gameState, file.getPath());
                JOptionPane.showMessageDialog(mainFrame, "Game saved successfully!");
            } catch (Exception e){
                JOptionPane.showMessageDialog(mainFrame, "Error while saving:\n" + e.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the "Load (JSON)" menu item.
     */
    public void handleLoadGame(){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Load Game State (JSON)");
        if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try {
                this.gameState = jsonPersistence.loadGame(file.getPath());
                refreshAllViews();
                JOptionPane.showMessageDialog(mainFrame, "Game loaded successfully!");
            } catch (Exception e){
                JOptionPane.showMessageDialog(mainFrame, "Error while loading:\n" + e.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the "PGN Export" menu item.
     */
    public void handleExportPgn(){
        if (gameState == null){
            JOptionPane.showMessageDialog(mainFrame, "Nothing to export!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Game as PGN");
        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)){
                String pgnText = pgnFormatter.format(gameState);
                writer.write(pgnText);
                JOptionPane.showMessageDialog(mainFrame, "Game exported to PGN successfully!");
            } catch (Exception e){
                JOptionPane.showMessageDialog(mainFrame, "Error during export:\n" + e.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the "PGN Import" menu item.
     */
    public void handleImportPgn() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("PGN Importálása");
        if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                GameState newState = pgnParser.importGame(file.getPath());
                this.gameState = newState;
                this.isGameInProgress = true;

                // Assuming successful load means game is active
                refreshAllViews();
                JOptionPane.showMessageDialog(mainFrame, "PGN import successful!");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(mainFrame, "Hiba PGN importálás közben:\n" + e.getMessage(), "Import Hiba", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles the "Offer Draw" menu item.
     */
    public void handleDrawOffer(){
        if (gameState == null) return;

        if (!gameState.canCurrentPlayerOfferDraw()){
            JOptionPane.showMessageDialog(mainFrame, "You cannot offer a draw at this time.", "Draw", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String offeringPlayer = gameState.isWhiteTurn() ? "White" : "Black";
        String opponentPlayer = gameState.isWhiteTurn() ? "Black" : "White";

        int response = JOptionPane.showConfirmDialog(mainFrame,
                opponentPlayer + ", do you accept " + offeringPlayer + "'s draw offer?",
                "Draw Offer",
                JOptionPane.YES_NO_OPTION);

        if (response == JOptionPane.YES_OPTION){
            JOptionPane.showMessageDialog(mainFrame, "The game has ended in a draw.");
            //TODO: End the game, disable GUI
            gameState = null;
        } else {
            JOptionPane.showMessageDialog(mainFrame, "Draw offer declined.");
            gameState.recordDrawOfferRejection();
        }
    }

    /**
     * Refreshes all views based on the current GameState.
     */
    private void refreshAllViews(){
        if (gameState == null) return;

        // If it's a new turn and not check, clear red highlight
        if (!ruleEngine.isKingInCheck(gameState, gameState.isWhiteTurn())) {
            boardPanel.setKingInCheck(null);
        }

        // Tell the board panel who the current player is so it can rotate
        boardPanel.setViewpoint(gameState.isWhiteTurn());

        boardPanel.updateBoard(gameState.getBoard());
        sidePanel.updateMoveHistory(gameState.getMoveHistory());
        sidePanel.updatePlayerInfo(gameState.getWhitePlayer(), gameState.getBlackPlayer());
        sidePanel.updatePlayerTurn(gameState.isWhiteTurn());
    }

    public GameState getGameState(){ return gameState; }

    /**
     * Gets the valid moves for the GUI (MouseController)
     * so it can draw the green dots.
     */
    public Set<Position> getValidMovesForPiece(Position pos){
        if (gameState == null){
            return Set.of(); //Return an empty set
        }
        return ruleEngine.getValidMovesForPiece(gameState, pos);
    }
}