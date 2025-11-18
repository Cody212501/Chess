package Model;

import Model.Pieces.*;

import java.util.*;

public class GameState{
    private Board board;
    private Player whitePlayer;
    private Player blackPlayer;
    private boolean isWhiteTurn;
    private List<Move> moveHistory;
    private boolean canWhiteOfferDraw;
    private boolean canBlackOfferDraw;

    //State for RuleEngine
    private CastlingRights castlingRights;
    private Position enPassantTargetSquare; // The square a pawn can *move to*

    /**
     * Default constructor for a new game.
     */
    public GameState() {
        this.board = new Board(); // Fills with pieces
        this.moveHistory = new ArrayList<>();
        this.isWhiteTurn = true;
        this.canWhiteOfferDraw = true;
        this.canBlackOfferDraw = true;

        this.castlingRights = new CastlingRights();
        this.enPassantTargetSquare = null;
    }

    /**
     * Constructor for simulation
     */
    public GameState(Board board, boolean isWhiteTurn) {
        this(); // Call default constructor
        this.board = board;
        this.isWhiteTurn = isWhiteTurn;
        // Note: This sim-constructor doesn't copy history, castling, etc.
        // It's mainly for check detection.
    }

    // --- GETTERS ---
    public Board getBoard() { return board; }
    public boolean isWhiteTurn() { return isWhiteTurn; }
    public List<Move> getMoveHistory() { return moveHistory; }
    public CastlingRights getCastlingRights() { return castlingRights; }
    public Position getEnPassantTargetSquare() { return enPassantTargetSquare; }
    public Player getWhitePlayer() { return whitePlayer; }
    public Player getBlackPlayer() { return blackPlayer; }

    public void setPlayers(Player white, Player black) {
        this.whitePlayer = white;
        this.blackPlayer = black;
    }

    /**
     * This method executes the move and updates the game state.
     */
    public void makeMove(Move move) {
        Piece pieceMoved = move.getPieceMoved();
        Position from = move.getFrom();

        // 1. Clear previous en passant square *before* the move
        this.enPassantTargetSquare = null;

        // 2. Update castling rights *before* the move
        if (pieceMoved.getType() == PieceType.KING) {
            if (pieceMoved.isWhite()) {
                castlingRights.whiteCastleKingSide = false;
                castlingRights.whiteCastleQueenSide = false;
            } else {
                castlingRights.blackCastleKingSide = false;
                castlingRights.blackCastleQueenSide = false;
            }
        }
        // Check for Rook moves from corners
        if (pieceMoved.getType() == PieceType.ROOK) {
            if (from.equals(new Position(7, 0))) castlingRights.whiteCastleQueenSide = false;
            if (from.equals(new Position(7, 7))) castlingRights.whiteCastleKingSide = false;
            if (from.equals(new Position(0, 0))) castlingRights.blackCastleQueenSide = false;
            if (from.equals(new Position(0, 7))) castlingRights.blackCastleKingSide = false;
        }

        // 3. Apply the move to the board
        board.applyMove(move); // This now handles castling/en passant logic

        // 4. Set new en passant square *after* the move
        if (pieceMoved.getType() == PieceType.PAWN) {
            if (Math.abs(from.row() - move.getTo().row()) == 2) {
                // This was a double step, set en passant target
                int targetRow = (from.row() + move.getTo().row()) / 2;
                this.enPassantTargetSquare = new Position(targetRow, from.column());
            }
        }

        // 5. Update history and turn
        moveHistory.add(move);
        isWhiteTurn = !isWhiteTurn;

        // Draw offer logic
        if (isWhiteTurn) {
            canWhiteOfferDraw = true;
        } else {
            canBlackOfferDraw = true;
        }
    }

    // Draw-offer logic (unchanged)
    public boolean canCurrentPlayerOfferDraw() {
        return isWhiteTurn ? canWhiteOfferDraw : canBlackOfferDraw;
    }

    public void recordDrawOfferRejection() {
        if (isWhiteTurn) {
            canWhiteOfferDraw = false;
        } else {
            canBlackOfferDraw = false;
        }
    }
}