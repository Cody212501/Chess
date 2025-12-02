package Model.Pieces;

import Model.*;

import java.util.*;

public class Rook extends Piece{
    public Rook(boolean isWhite){
        super(isWhite, PieceType.ROOK);
    }

    @Override
    public Set<Position> getPossiblyLegalMoves(Board board, Position currentPos){
        Set<Position> moves = new HashSet<>();

        // vertical and horizontal checking
        int[] dRows = { -1, 1, 0, 0 };
        int[] dCols = { 0, 0, -1, 1 };

        for(int i = 0; i < 4; i++){
            addSlidingMoves(board, currentPos, moves, dRows[i], dCols[i]);
        }

        return moves;
    }
}
