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
        int c = currentPos.col();

        // A lépés iránya a színtől függ (fehér: -1 (felfelé), fekete: +1 (lefelé))
        int direction = isWhite ? -1 : 1;

        // 1. Egyszerű lépés előre
        Position oneStep = new Position(r + direction, c);
        if(oneStep.isOnBoard() && !board.isOccupied(oneStep)){
            moves.add(oneStep);

            // 2. Dupla lépés (csak ha az első lépés is szabad volt)
            boolean atStartRow =(isWhite && r == 6) ||(!isWhite && r == 1);
            if(atStartRow){
                Position twoSteps = new Position(r + 2 * direction, c);
                if(twoSteps.isOnBoard() && !board.isOccupied(twoSteps)){
                    moves.add(twoSteps);
                }
            }
        }

        // 3. Ütések (átlósan)
        int[] captureCols = { c - 1, c + 1 };
        for(int captureCol : captureCols){
            Position capturePos = new Position(r + direction, captureCol);
            if(capturePos.isOnBoard() && board.isOccupiedByEnemy(capturePos, isWhite)){
                moves.add(capturePos);
            }
        }

        // TODO: Az En Passant logikát a RuleEngine-nek kell kezelnie,
        // mivel az függ a GameState-től (az ellenfél utolsó lépése).

        return moves;
    }
}
