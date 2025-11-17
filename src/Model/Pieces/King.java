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

        // A 8 szomszédos mező
        for (int dr = -1; dr <= 1; dr++){
            for (int dc = -1; dc <= 1; dc++){
                if (dr == 0 && dc == 0) continue; // A jelenlegi pozíciót kihagyjuk

                Position targetPos = new Position(currentPos.row() + dr, currentPos.column() + dc);

                if (targetPos.isOnBoard()){
                    // Csak azt ellenőrizzük, hogy nem lépünk-e saját bábura
                    if (!board.isOccupied(targetPos) || board.isOccupiedByEnemy(targetPos, isWhite)){
                        moves.add(targetPos);
                    }
                }
            }
        }

        // TODO: A sáncolás (Castling) logikát a RuleEngine-nek kell kezelnie,
        // mivel az függ a GameState-től (nem volt-e sakkban, nem mozdult-e a király/bástya).
        // A RuleEngine adhatja hozzá a (g1, c1) stb. lépéseket, ha legálisak.

        return moves;
    }
}
