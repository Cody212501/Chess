package Model;

import Model.Pieces.*;

public class Board{
    //The 8x8 grid. pieceGrid[row][column]
    private final Piece[][] pieceGrid;

    public Board() {
        this.pieceGrid = new Piece[8][8];
        initialSetup();
    }

    /**
     * Copy constructor for simulation
     */
    public Board(Board other) {
        this.pieceGrid = new Piece[8][8];
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                this.pieceGrid[r][c] = other.pieceGrid[r][c]; // Pieces are immutable, so shallow copy is fine
            }
        }
    }

    public Board deepCopy() {
        return new Board(this);
    }

    /**
     * Sets up the board to the standard starting position.
     */
    public void initialSetup() {
        // Black pieces (Row 0 and 1)
        pieceGrid[0][0] = new Rook(false);
        pieceGrid[0][1] = new Knight(false);
        pieceGrid[0][2] = new Bishop(false);
        pieceGrid[0][3] = new Queen(false);
        pieceGrid[0][4] = new King(false);
        pieceGrid[0][5] = new Bishop(false);
        pieceGrid[0][6] = new Knight(false);
        pieceGrid[0][7] = new Rook(false);
        for (int col = 0; col < 8; col++) {
            pieceGrid[1][col] = new Pawn(false);
        }

        // White pieces (Row 6 and 7)
        for (int col = 0; col < 8; col++) {
            pieceGrid[6][col] = new Pawn(true);
        }
        pieceGrid[7][0] = new Rook(true);
        pieceGrid[7][1] = new Knight(true);
        pieceGrid[7][2] = new Bishop(true);
        pieceGrid[7][3] = new Queen(true);
        pieceGrid[7][4] = new King(true);
        pieceGrid[7][5] = new Bishop(true);
        pieceGrid[7][6] = new Knight(true);
        pieceGrid[7][7] = new Rook(true);
    }

    /**
     * Clears the board of all pieces.
     * This is essential for the JUnit tests.
     */
    public void setupEmpty() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                pieceGrid[r][c] = null;
            }
        }
    }

    /**
     * Physically moves a piece on the board.
     * This method is "dumb" - it performs the move without validation.
     * Validation is the job of the RuleEngine.
     * It also handles setting the pieceCaptured field on the move.
     *
     * @param move The move to apply.
     */
    public void applyMove(Move move) {
        Position from = move.getFrom();
        Position to = move.getTo();
        Piece piece = move.getPieceMoved();

        // 1. Standard Move
        Piece capturedPiece = getPieceAt(to);
        if (capturedPiece != null) {
            move.setPieceCaptured(capturedPiece);
        }
        setPieceAt(to, piece);
        setPieceAt(from, null);

        // --- 2. Handle Special Moves ---
        if (move.isCastling()) {
            // King already moved from (e,g) or (e,c)
            // We just need to move the rook.
            if (to.column() == 6) { // Kingside (g-file)
                Position rookFrom = new Position(from.row(), 7); // h-file
                Position rookTo = new Position(from.row(), 5); // f-file
                setPieceAt(rookTo, getPieceAt(rookFrom));
                setPieceAt(rookFrom, null);
            } else if (to.column() == 2) { // Queenside (c-file)
                Position rookFrom = new Position(from.row(), 0); // a-file
                Position rookTo = new Position(from.row(), 3); // d-file
                setPieceAt(rookTo, getPieceAt(rookFrom));
                setPieceAt(rookFrom, null);
            }
        }
        else if (move.isEnPassant()) {
            // The pawn moved to the 'to' square.
            // We must remove the *opponent's* pawn, which is
            // on the same file as 'to', but on the 'from' row.
            int capturedPawnRow = from.row();
            int capturedPawnCol = to.column();
            Position capturedPawnPos = new Position(capturedPawnRow, capturedPawnCol);

            move.setPieceCaptured(getPieceAt(capturedPawnPos));
            setPieceAt(capturedPawnPos, null);
        }
        else if (move.isPromotion()) {
            // The pawn has already moved to the 'to' square.
            // We replace it with the promotion piece.
            setPieceAt(to, move.getPromotionPiece());
        }
    }

    public Piece getPieceAt(Position pos) {
        if (!pos.isOnBoard()) {
            return null;
        }
        return pieceGrid[pos.row()][pos.column()];
    }

    public void setPieceAt(Position pos, Piece piece) {
        if (pos.isOnBoard()) {
            pieceGrid[pos.row()][pos.column()] = piece;
        }
    }

    public boolean isOccupied(Position pos) {
        return getPieceAt(pos) != null;
    }

    public boolean isOccupiedByEnemy(Position pos, boolean isWhitePlayer) {
        Piece piece = getPieceAt(pos);
        return piece != null && piece.isWhite() != isWhitePlayer;
    }
}