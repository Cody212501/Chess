package Model.Pieces;

import Model.*;

import java.util.*;

public class King extends Piece{
    public King(boolean isWhite){
        super(isWhite, PieceType.KING);
    }

    @Override
    public Set<Position> getPossiblyLegalMoves(Board board, Position currentPos){
        Set<Position> moves = new HashSet<>();

        // the 8 neighbouring squares
        for(int dr = -1; dr <= 1; dr++){
            for(int dc = -1; dc <= 1; dc++){
                if(dr == 0 && dc == 0) continue; // ignoring the current square

                Position targetPos = new Position(currentPos.row() + dr, currentPos.column() + dc);

                if(targetPos.isOnBoard()){
                    // only checking for not stepping on own piece
                    if(!board.isOccupied(targetPos) || board.isOccupiedByEnemy(targetPos, isWhite)){
                        moves.add(targetPos);
                    }
                }
            }
        }
        return moves;
    }
}
