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

    public MoveLogModel() {
        this.moves = new ArrayList<>();
    }

    /**
     * Public method to update the internal move list.
     * It notifies the JTable that its data has completely changed.
     */
    public void setMoves(List<Move> newMoves) {
        this.moves.clear();
        this.moves.addAll(newMoves);
        // This is crucial! It tells the JTable to refresh itself.
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        // The number of rows is (total moves / 2), rounded up.
        return (int) Math.ceil(moves.size() / 2.0);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                // Column 0: Move Number (e.g., "1.")
                return (rowIndex + 1) + ".";
            case 1:
                // Column 1: White's move
                // (rowIndex * 2) gives the index of the white move
                int whiteMoveIndex = rowIndex * 2;
                return (whiteMoveIndex < moves.size()) ? formatMove(moves.get(whiteMoveIndex)) : "";
            case 2:
                // Column 2: Black's move
                // (rowIndex * 2 + 1) gives the index of the black move
                int blackMoveIndex = rowIndex * 2 + 1;
                return (blackMoveIndex < moves.size()) ? formatMove(moves.get(blackMoveIndex)) : "";
            default:
                return "";
        }
    }

    /**
     * Converts a Move object into a simple notation (e.g., "e2e4").
     * A full implementation would convert this to Standard Algebraic Notation
     * (e.g., "e4" or "Nf3"), which is much more complex.
     */
    private String formatMove(Move move) {
        // TODO: This should be replaced by a proper PGN/SAN formatter
        // for full compliance (e.g., "Nf3" instead of "g1f3").
        // For now, simple coordinate notation is used.
        return positionToNotation(move.getFrom()) + positionToNotation(move.getTo());
    }

    private String positionToNotation(Position pos) {
        char file = (char) ('a' + pos.column());
        char rank = (char) ('8' - pos.row());
        return "" + file + rank;
    }
}
