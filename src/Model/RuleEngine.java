package Model;

import Model.Pieces.*;

import java.util.*;

public class RuleEngine{
    // Note: This is a stateless RuleEngine.
    // All state is passed in via the 'GameState' object.

    /**
     * Public method for the Controller to check if a move is fully legal.
     * This is the main entry point for move validation.
     *
     * @param state The current GameState.
     * @param from The starting position.
     * @param to The target position.
     * @return true if the move is 100% legal, false otherwise.
     */
    public boolean isLegalMove(GameState state, Position from, Position to) {
        // TODO: This is the core logic.
        // 1. Get the piece at 'from'.
        // 2. Check if it's the correct player's turn (state.isWhiteTurn()).
        // 3. Get all valid moves for that piece using getValidMovesForPiece().
        // 4. Check if 'to' is in that set of valid moves.

        Set<Position> validMoves = getValidMovesForPiece(state, from);
        return validMoves.contains(to);
    }

    /**
     * Gets all fully legal moves for a piece at a given position.
     * This method is required by the GUI to show possible moves.
     *
     * @param state The current GameState.
     * @param piecePos The position of the piece to check.
     * @return A Set of Positions the piece can legally move to.
     */
    public Set<Position> getValidMovesForPiece(GameState state, Position piecePos) {
        Set<Position> legalMoves = new HashSet<>();
        Piece piece = state.getBoard().getPieceAt(piecePos);

        if (piece == null || piece.isWhite() != state.isWhiteTurn()) {
            return legalMoves; // Not your piece, or no piece
        }

        // 1. Get "possibly-legal" moves from the piece itself
        // These are moves the piece *could* make, ignoring check.
        Set<Position> possiblyLegalMoves = piece.getPossiblyLegalMoves(state.getBoard(), piecePos);

        // 2. Filter out moves that would put the king in check
        // This is the most complex part of a chess engine.
        for (Position targetPos : possiblyLegalMoves) {
            // TODO: Simulate the move:
            // 1. Create a copy of the board.
            // 2. Apply the move (from, targetPos) on the copy.
            // 3. Call isKingInCheck(copiedBoard, state.isWhiteTurn()).
            // 4. If the king is NOT in check, add targetPos to legalMoves.

            // Placeholder: For now, we assume all possibly-legal moves are legal.
            // This is INCORRECT but allows the program to run.
            // This must be replaced with the simulation logic described above.
            legalMoves.add(targetPos);
        }

        // 3. Add special moves (Castling, En Passant)
        // TODO: Check for legal castling
        // if (piece.getType() == PieceType.KING) {
        //    addCastlingMoves(legalMoves, state);
        // }
        // TODO: Check for legal en passant
        // if (piece.getType() == PieceType.PAWN) {
        //    addEnPassantMoves(legalMoves, state, piecePos);
        // }

        return legalMoves;
    }

    /**
     * Checks if the current player's king is in check.
     * @param state The current GameState.
     * @return true if the current player is in check.
     */
    public boolean isCurrentPlayerInCheck(GameState state) {
        // TODO: Implement isKingInCheck
        // 1. Find the king (state.getKingPosition(state.isWhiteTurn())).
        // 2. Call isPositionAttacked(state.getBoard(), kingPos, !state.isWhiteTurn()).
        return false; // Placeholder
    }

    /**
     * Checks if a specific position on the board is attacked by the enemy.
     * @param board The board state.
     * @param targetPos The position to check.
     * @param isAttackerWhite The colour of the attacking side.
     * @return true if an enemy piece attacks this square.
     */
    private boolean isPositionAttacked(Board board, Position targetPos, boolean isAttackerWhite) {
        // TODO: This is a complex "reverse" lookup.
        // Iterate over all 8x8 squares.
        // For each square, if it contains an enemy piece (isAttackerWhite):
        //   Get its possiblyLegalMoves.
        //   If those moves contain targetPos, return true.
        // If no piece attacks it, return false.
        return false; // Placeholder
    }

    /**
     * Checks if the current player is in checkmate.
     * @param state The current GameState.
     * @return true if it is checkmate.
     */
    public boolean isCheckmate(GameState state) {
        // Checkmate = In Check AND No Legal Moves
        if (!isCurrentPlayerInCheck(state)) {
            return false;
        }
        return hasNoLegalMoves(state);
    }

    /**
     * Checks if the current player is in stalemate.
     * @param state The current GameState.
     * @return true if it is stalemate.
     */
    public boolean isStalemate(GameState state) {
        // Stalemate = NOT In Check AND No Legal Moves
        if (isCurrentPlayerInCheck(state)) {
            return false;
        }
        return hasNoLegalMoves(state);
    }

    /**
     * Helper method to check if the current player has any legal moves.
     */
    private boolean hasNoLegalMoves(GameState state) {
        // TODO: Iterate over all 8x8 squares.
        // If a square contains a piece belonging to the current player:
        //   Call getValidMovesForPiece(state, squarePos).
        //   If that set is NOT empty, return false (player has a move).
        // If the loop finishes without finding any moves, return true.
        return false; // Placeholder
    }
}