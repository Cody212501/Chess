package Model.Pieces;

import Model.*;

import java.util.*;

public class Bishop extends Piece{
    public Bishop(boolean isWhite){
        super(isWhite, PieceType.BISHOP);
    }

    @Override
    public Set<Position> getPossiblyLegalMoves(Board board, Position currentPos){
        Set<Position> moves = new HashSet<>();

        // Átlós irányok
        int[] dRows = { -1, -1,  1, 1 };
        int[] dCols = { -1,  1, -1, 1 };

        for(int i = 0; i < 4; i++){
            addSlidingMoves(board, currentPos, moves, dRows[i], dCols[i]);
        }

        return moves;
    }
}
