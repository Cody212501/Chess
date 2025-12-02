package Model.Pieces;

import Model.*;

import java.util.*;

public class Knight extends Piece{
    public Knight(boolean isWhite){
        super(isWhite, PieceType.KNIGHT);
    }

    @Override
    public Set<Position> getPossiblyLegalMoves(Board board, Position currentPos){
        Set<Position> moves = new HashSet<>();

        // the 8 possible L step
        int[] dRows = { -2, -2, -1, -1,  1,  1,  2,  2 };
        int[] dCols = { -1,  1, -2,  2, -2,  2, -1,  1 };

        for(int i = 0; i < 8; i++){
            Position targetPos = new Position(currentPos.row() + dRows[i], currentPos.column() + dCols[i]);

            if(targetPos.isOnBoard()){
                // the Knight jumps over obstacles,
                // so only needs to check not stepping on own piece.
                if(!board.isOccupied(targetPos) || board.isOccupiedByEnemy(targetPos, isWhite)){
                    moves.add(targetPos);
                }
            }
        }
        return moves;
    }
}
