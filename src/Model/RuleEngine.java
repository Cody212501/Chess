package Model;

import Model.Pieces.*;

import java.util.*;
// Note: This is a stateless Rule Engine.
// All states are passed in via the 'GameState' object.

/**
 * The "brain" of the chess game.
 * This class is responsible for validating moves, detecting check, checkmate,
 * stalemate, and handling all special move logic like castling and en passant.
 */
public class RuleEngine{
    /**
     * PRIMARY PUBLIC METHOD
     * Checks if a move from 'from' to 'to' is legal and, if so,
     * returns a fully detailed Move object.
     *
     * @param state The current GameState.
     * @param from The starting position.
     * @param to The target position.
     * @return A valid Move object if the move is legal, otherwise null.
     */
    public Move generateMove(GameState state, Position from, Position to) {
        Piece piece = state.getBoard().getPieceAt(from);

        // Basic check: is it our piece?
        if (piece == null || piece.isWhite() != state.isWhiteTurn()) {
            return null;
        }

        // Get all *possible*, not feasible legal moves for this piece
        Set<Move> legalMoves = getLegalMovesForPiece(state, from);

        // Check if any of the legal moves match the target 'to' position
        for (Move move : legalMoves) {
            if (move.getTo().equals(to)) {

                //PROMOTION
                // If it's a promotion, we must ask the user what piece they want.
                // We set the piece to Queen by default, GameController can override.
                if (piece.getType() == PieceType.PAWN) {
                    int endRow = piece.isWhite() ? 0 : 7; // White moves up (row 0), Black moves down
                    if (to.row() == endRow) {
                        move.setPromotion(true);
                        // Default promotion is Queen, controller will ask user, what they want
                        move.setPromotionPiece(new Queen(state.isWhiteTurn()));
                    }
                }

                //CHECK/MATE detection
                // Before returning, simulate this move and see if it puts the
                // *opponent* in check or checkmate.
                Board nextBoard = simulateMove(state.getBoard(), move);
                GameState nextState = new GameState(nextBoard, !state.isWhiteTurn());

                if (isKingInCheck(nextState, !state.isWhiteTurn())) { // Check opponent's king
                    move.setCheck(true);
                    // Check for mate
                    if (hasNoLegalMoves(nextState)) {
                        move.setCheckmate(true);
                    }
                }
                return move;
            }
        }

        return null; // No legal move found from 'from' to 'to'
    }

    /**
     * Gets all valid moves for a single piece, filtered for self-check.
     * This is used by the GUI to show move dots.
     */
    public Set<Position> getValidMovesForPiece(GameState state, Position piecePos) {
        Set<Move> legalMoves = getLegalMovesForPiece(state, piecePos);
        Set<Position> legalToPositions = new HashSet<>();
        for (Move move : legalMoves) {
            legalToPositions.add(move.getTo());
        }
        return legalToPositions;
    }

    /**
     * PRIVATE: Gets a Set of all *fully legal* Move objects for a piece.
     */
    private Set<Move> getLegalMovesForPiece(GameState state, Position piecePos) {
        Set<Move> legalMoves = new HashSet<>();
        Piece piece = state.getBoard().getPieceAt(piecePos);

        if (piece == null || piece.isWhite() != state.isWhiteTurn()) {
            return legalMoves;
        }

        // 1. Get all pseudo-legal moves (ignoring check)
        Set<Position> targets = piece.getPossiblyLegalMoves(state.getBoard(), piecePos);

        // 2. Filter for self-check
        for (Position targetPos : targets) {
            Move move = new Move(piecePos, targetPos, piece);

            // En Passant validation
            if (piece.getType() == PieceType.PAWN
                    && Math.abs(piecePos.column() - targetPos.column()) == 1
                    && state.getBoard().getPieceAt(targetPos) == null) {
                move.setEnPassant(true);
            }

            // Check if move doesn't put OWN king in check
            if (isMoveSafe(state, move)) {
                legalMoves.add(move);
            }
        }

        // 3. Add castling moves
        if (piece.getType() == PieceType.KING) {
            addCastlingMoves(state, piecePos, legalMoves);
        }

        return legalMoves;
    }

    /**
     * Checks if a given move puts the current player's own king in check.
     * @param state The current state (before the move).
     * @param move The move to test.
     * @return true if the move is safe, false if it results in self-check.
     */
    private boolean isMoveSafe(GameState state, Move move) {
        // Create a temporary board with the move applied
        Board simulatedBoard = simulateMove(state.getBoard(), move);
        // Checking if player's OWN king is/would be in check on the new board
        return !isKingInCheck(simulatedBoard, state.isWhiteTurn());
    }

    /**
     * Checks if the king of a specific colour is currently in check.
     */
    private boolean isKingInCheck(Board board, boolean isWhiteKing) {
        Position kingPos = findKing(board, isWhiteKing);
        if (kingPos == null) {
            return false; // Should not happen
        }
        //inspecting, if any enemy piece is attacking our king
        return isSquareAttacked(board, kingPos, !isWhiteKing);
    }

    //public for Checkmate/Stalemate checks
    public boolean isKingInCheck(GameState state, boolean isWhiteKing) {
        return isKingInCheck(state.getBoard(), isWhiteKing);
    }

    /**
     * Checks if a specific square is attacked by any piece of the 'attacker' colour.
     */
    private boolean isSquareAttacked(Board board, Position targetPos, boolean byWhiteAttacker) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position attackerPos = new Position(r, c);
                Piece attacker = board.getPieceAt(attackerPos);

                if (attacker != null && attacker.isWhite() == byWhiteAttacker) {
                    // Check if this attacker's possibly-legal moves include the target
                    if (attacker.getPossiblyLegalMoves(board, attackerPos).contains(targetPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Finds the position of the king.
     */
    private Position findKing(Board board, boolean isWhiteKing) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = board.getPieceAt(pos);
                if (p != null && p.getType() == PieceType.KING && p.isWhite() == isWhiteKing) {
                    return pos;
                }
            }
        }
        return null; // Should never happen in any real game
    }

    /**
     * Checks for and adds Castling moves to the list.
     */
    private void addCastlingMoves(GameState state, Position kingPos, Set<Move> legalMoves) {
        boolean isWhite = state.isWhiteTurn();
        CastlingRights rights = state.getCastlingRights();
        Board board = state.getBoard();

        // Cannot castle out of check
        if (isKingInCheck(state, isWhite)) {
            return;
        }

        // Kingside Castling (O-O)
        if ((isWhite && rights.canWhiteCastleKingSide()) || (!isWhite && rights.canBlackCastleKingSide())) {
            int r = isWhite ? 7 : 0;
            Position f_sq = new Position(r, 5); // f1 or f8
            Position g_sq = new Position(r, 6); // g1 or g8

            if (!board.isOccupied(f_sq) && !board.isOccupied(g_sq)) {
                // Cannot castle through check
                if (!isSquareAttacked(board, f_sq, !isWhite) &&
                        !isSquareAttacked(board, g_sq, !isWhite)) {

                    Move castleMove = new Move(kingPos, g_sq, board.getPieceAt(kingPos));
                    castleMove.setCastling(true);
                    legalMoves.add(castleMove);
                }
            }
        }

        // Queenside Castling (O-O-O)
        if ((isWhite && rights.canWhiteCastleQueenSide()) || (!isWhite && rights.canBlackCastleQueenSide())) {
            int r = isWhite ? 7 : 0;
            Position b_sq = new Position(r, 1); // b1 or b8
            Position c_sq = new Position(r, 2); // c1 or c8
            Position d_sq = new Position(r, 3); // d1 or d8

            if (!board.isOccupied(b_sq) && !board.isOccupied(c_sq) && !board.isOccupied(d_sq)) {
                // Cannot castle through check
                if (!isSquareAttacked(board, c_sq, !isWhite) &&
                        !isSquareAttacked(board, d_sq, !isWhite)) {

                    Move castleMove = new Move(kingPos, c_sq, board.getPieceAt(kingPos));
                    castleMove.setCastling(true);
                    legalMoves.add(castleMove);
                }
            }
        }
    }

    /**
     * Creates a new Board object representing the state after a move.
     * This is vital for simulation.
     */
    private Board simulateMove(Board original, Move move) {
        Board simBoard = original.deepCopy();
        //Uses the logic in Board.java to handle flags
        simBoard.applyMove(move);

        return simBoard;
    }

    /**
     * Checks if the current player has any legal moves.
     */
    private boolean hasNoLegalMoves(GameState state) {
        boolean isWhite = state.isWhiteTurn();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = state.getBoard().getPieceAt(pos);
                if (p != null && p.isWhite() == isWhite) {
                    Set<Move> moves = getLegalMovesForPiece(state, pos);
                    if (!moves.isEmpty()) {
                        return false; // Found a legal move
                    }
                }
            }
        }
        return true; // No legal moves found
    }

    /**
     * Public Stalemate detector
     * Stalemate occures, when a player should make a move, but is unable to do so,
     * while they are not being in check.
     */
    public boolean isStalemate(GameState state) {
        return !isKingInCheck(state, state.isWhiteTurn()) && hasNoLegalMoves(state);
    }
}