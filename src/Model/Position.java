package Model;

/**
 * Stores a square's position, immutable class. (thus the record instead of class)
 * @param row (0-7): Rows 1->8
 * @param column (0-7): Columns A->H
 */
public record Position(int row, int column){
    /**
    *Checks, if given position is within the boundaries of the 8x8 board
    */
    public boolean isOnBoard() {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }
}
