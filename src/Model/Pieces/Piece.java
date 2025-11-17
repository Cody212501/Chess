package Model.Pieces;

import Model.*;

import java.util.*;

public abstract class Piece{
    protected final boolean isWhite;
    protected final PieceType type;

    public Piece(boolean isWhite, PieceType type) {
        this.isWhite = isWhite;
        this.type = type;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public PieceType getType() {
        return type;
    }

    /**
     *Returns all legal moves by the piece(any move permitted by the rules).
     *This does not check for any checks, that is the responsibility
     * of the RuleEngine!
     *
     * @param board Current state of the board.
     * @param currentPos The position of the piece at this moment.
     */
    public abstract Set<Position> getPossiblyLegalMoves(Board board, Position currentPos);

    /**
     *Helper function for the Queen, Rook and Bishop pieces.
     *Goes to the end of the board in one direction (d_rows, d_columns),
     * till it finds the edge, or another piece.
     */
    protected void addSlidingMoves(Board board, Position start, Set<Position> moves, int dRow, int dCol) {
        Position nextPos = new Position(start.row() + dRow, start.column() + dCol);

        while (nextPos.isOnBoard()) {
            if (!board.isOccupied(nextPos)) {
                //empty square, adding, and moving on
                moves.add(nextPos);
            } else {
                //occupied square
                if (board.isOccupiedByEnemy(nextPos, this.isWhite)) {
                    //opponent's piece, adding to move, than stopping in this line
                    moves.add(nextPos);
                }
                //own piece, just stopping, can't take it off the board
                break;
            }
            nextPos = new Position(nextPos.row() + dRow, nextPos.column() + dCol);
        }
    }

    /**
     * Returns the base filename for the piece's image.
     * e.g., "Pawn", "Rook", "King".
     * The BoardPanel will add colour prefix (White or Black) and ".png" suffix.
     */
    public String getImageBaseName() {
        return type.name(); // Uses the name of the enum constant (e.g., "PAWN")
    }
}
