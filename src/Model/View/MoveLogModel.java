package Model.View;

import javax.swing.table.*;
import java.util.*;

import Model.*;

/**
 * This is the custom TableModel for the JTable in SidePanel.
 * It adapts our List<Move> into a three-column format:
 * | No. | White | Black |
 * | 1.  | e4    | e5    |
 * | 2.  | Nf3   | Nc6   | */
public class MoveLogModel extends AbstractTableModel{
    private final List<Move> moves;
    private final String[] columnNames = {"No.", "White", "Black"};

    public MoveLogModel(){
        this.moves = new ArrayList<>();
    }

    /**
     * Public method to update the internal move list.
     * It notifies the JTable that its data has completely changed.
     */
    public void setMoves(List<Move> newMoves){
        this.moves.clear();
        this.moves.addAll(newMoves);
        // This is crucial! It tells the JTable to refresh itself.
        fireTableDataChanged();
    }

    @Override
    public int getRowCount(){
        // The number of rows is (total moves / 2), rounded up.
        return (int) Math.ceil(moves.size() / 2.0);
    }

    @Override
    public int getColumnCount(){
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column){
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex){
        switch(columnIndex){
            case 0:
                // Column 0: Move Number (e.g., "1.")
                return (rowIndex + 1) + ".";
            case 1:
                // Column 1: White's move
                // (rowIndex * 2) gives the index of the white move
                int whiteMoveIndex = rowIndex * 2;
                return (whiteMoveIndex < moves.size()) ? formatSan(moves.get(whiteMoveIndex)) : "";
            case 2:
                // Column 2: Black's move
                // (rowIndex * 2 + 1) gives the index of the black move
                int blackMoveIndex = rowIndex * 2 + 1;
                return (blackMoveIndex < moves.size()) ? formatSan(moves.get(blackMoveIndex)) : "";
            default:
                return "";
        }
    }

    /**
     * Converts a Move object into a simple notation (e.g., "e2e4").
     * A full implementation would convert this to Standard Algebraic Notation
     * (e.g., "e4" or "Nf3"), which is much more complex.
     */
    private String formatSan(Move move){
        // castling move
        if(move.isCastling()){
            boolean kingside = move.getTo().column() == 6;
            return (kingside ? "O-O" : "O-O-O") + getCheckSymbol(move);
        }

        // Simple PGN/SAN formatter for display
        StringBuilder sb = new StringBuilder();

        // 1. Piece
        if(move.getPieceMoved().getType() != PieceType.PAWN){
            sb.append(getPieceChar(move.getPieceMoved().getType()));
        }

        // 2. Disambiguation (Simplified: always show file if capture by pawn)
        if(move.getPieceCaptured() != null && move.getPieceMoved().getType() == PieceType.PAWN){
            sb.append(getFileChar(move.getFrom().column()));
        }

        // 3. Capture
        if(move.getPieceCaptured() != null){
            sb.append("x");
        }

        // 4. Target
        sb.append(positionToNotation(move.getTo()));

        // 5. Promotion
        if(move.isPromotion() && move.getPromotionPiece() != null){
            sb.append("=").append(getPieceChar(move.getPromotionPiece().getType()));
        }

        // 6. Check or Mate
        if(move.isCheckmate()){
            sb.append("#"); // mate
        }else if(move.isCheck()){
            sb.append("+"); // check
        }

        return sb.toString();
    }

    private String getCheckSymbol(Move move){
        if(move.isCheckmate()) return "#";
        if(move.isCheck()) return "+";
        return "";
    }

    private String getPieceChar(PieceType type){
        switch(type){
            case KNIGHT: return "N";
            case BISHOP: return "B";
            case ROOK: return "R";
            case QUEEN: return "Q";
            case KING: return "K";
            default: return "";
        }
    }


    private String positionToNotation(Position pos){
        return getFileChar(pos.column()) + (8 - pos.row());
    }

    private String getFileChar(int col){
        return String.valueOf((char)('a' + col));
    }
}
