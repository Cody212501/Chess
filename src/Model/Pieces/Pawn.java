package Model.Pieces;

import Model.*;

import java.util.*;

public class Pawn extends Piece{
    public Pawn(boolean isWhite){
        super(isWhite, PieceType.PAWN);
    }

    @Override
    public Set<Position> getPossiblyLegalMoves(Board board, Position currentPos){
        Set<Position> moves = new HashSet<>();
        int r = currentPos.row();
        int c = currentPos.column();

        // the direction of the move is based on color (white: -1 (upwards), black: +1 (downwards))
        int direction = isWhite ? -1 : 1;

        // 1. simple move forward
        Position oneStep = new Position(r + direction, c);
        if(oneStep.isOnBoard() && !board.isOccupied(oneStep)){
            moves.add(oneStep);

            // 2. Double move forward (just on the first move, and only if the square is free)
            boolean atStartRow =(isWhite && r == 6) ||(!isWhite && r == 1);
            if(atStartRow){
                Position twoSteps = new Position(r + 2 * direction, c);
                if(twoSteps.isOnBoard() && !board.isOccupied(twoSteps)){
                    moves.add(twoSteps);
                }
            }
        }

        // 3. Captures (diagonally)
        int[] captureCols = { c - 1, c + 1 };
        for(int captureCol : captureCols){
            Position capturePos = new Position(r + direction, captureCol);
            if(capturePos.isOnBoard() && board.isOccupiedByEnemy(capturePos, isWhite)){
                moves.add(capturePos);
            }
        }
        return moves;
    }
}
