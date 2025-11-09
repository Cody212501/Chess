package Model.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import Model.*;

public class BoardPanel extends JPanel{
    public static final int TILE_SIZE = 80;

    //Control states
    private Board currentBoard;
    private Set<Position> validMoves;
    private Position selectedPosition;

    //Drag&Drop GUI state ---
    private Piece draggedPiece;
    private Point dragPosition;

    // TODO: Load actual piece images
    //private Map<String, BufferedImage> pieceImages;

    public BoardPanel() {
        setPreferredSize(new Dimension(TILE_SIZE * 8, TILE_SIZE * 8));
        this.validMoves = new HashSet<>();
        this.currentBoard = new Board(); //default board

        //loadPieceImages();
    }

    /**
     * The core drawing method. Fulfils the 'Graphics' requirement.
     * It draws the board, pieces, selections, and dragged piece
     * based on the current state variables.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        //draw the tiles
        drawTiles(g2);

        //draw the pieces from the 'currentBoard' state
        drawPieces(g2);

        //highlight the selected piece's square
        drawSelectedSquare(g2);

        //highlight the valid moves
        drawValidMoves(g2);

        //draw the dragged piece (on top of everything)
        drawDraggedPiece(g2);
    }

    /**
     * Called by GameController to set the new board state after a move.
     */
    public void updateBoard(Board board) {
        this.currentBoard = board;
        repaint();
    }

    /**
     * Called by MouseController to show valid move highlights.
     */
    public void showValidMoves(Set<Position> moves) {
        this.validMoves.clear();
        this.validMoves.addAll(moves);
        repaint();
    }

    /**
     * Called by MouseController to highlight the selected square.
     */
    public void setSelectedPosition(Position pos) {
        this.selectedPosition = pos;
        repaint();
    }

    /**
     * Called by MouseController to clear all highlights.
     */
    public void clearSelections() {
        this.selectedPosition = null;
        this.validMoves.clear();
        repaint();
    }

    /**
     * Called by MouseController when a drag starts.
     */
    public void startDrag(Position pos, Point point) {
        this.draggedPiece = currentBoard.getPieceAt(pos);
        this.dragPosition = point;
        this.selectedPosition = pos; //select the square
        repaint();
    }

    /**
     * Called by MouseController as the mouse is dragged.
     */
    public void updateDrag(Point point) {
        this.dragPosition = point;
        repaint();
    }

    /**
     * Called by MouseController when the drag is released.
     */
    public void stopDrag() {
        this.draggedPiece = null;
        this.dragPosition = null;
        //Don't clear selectedPosition or validMoves here, the move logic in GameController will handle that.
        repaint();
    }

    //helper methods
    private void drawTiles(Graphics2D g2) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Color color = (row + col) % 2 == 0 ? Color.WHITE : new Color(118, 150, 86); // White/Green
                g2.setColor(color);
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }
    }

    private void drawPieces(Graphics2D g2) {
        if (currentBoard == null) return;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = currentBoard.getPieceAt(new Position(row, col));
                if (piece != null && piece != draggedPiece) {
                    // TODO: Replace with image drawing, when merging
                    // g2.drawImage(getPieceImage(piece), col * TILE_SIZE, ...);

                    // Placeholder drawing
                    g2.setColor(piece.isWhite() ? Color.DARK_GRAY : Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 36));
                    g2.drawString(getPieceSymbol(piece), col * TILE_SIZE + 25, row * TILE_SIZE + 50);
                }
            }
        }
    }

    private void drawSelectedSquare(Graphics2D g2) {
        if (selectedPosition != null) {
            g2.setColor(new Color(255, 255, 0, 100));
            g2.fillRect(selectedPosition.col() * TILE_SIZE, selectedPosition.row() * TILE_SIZE, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawValidMoves(Graphics2D g2) {
        g2.setColor(new Color(139, 134, 128, 80)); //Transparent Dark Green
        for (Position pos : validMoves) {
            //Draw a circle in the middle of the tile
            int x = pos.col() * TILE_SIZE + TILE_SIZE / 2;
            int y = pos.row() * TILE_SIZE + TILE_SIZE / 2;
            int radius = TILE_SIZE / 6;
            g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    private void drawDraggedPiece(Graphics2D g2) {
        if (draggedPiece != null && dragPosition != null) {
            // TODO: Replace with image drawing
            g2.setColor(draggedPiece.isWhite() ? Color.DARK_GRAY : Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 42)); // Slightly larger
            g2.drawString(getPieceSymbol(draggedPiece), dragPosition.x - TILE_SIZE / 2, dragPosition.y - TILE_SIZE / 2 + 55);
        }
    }

    // --- Placeholder helper ---
    private String getPieceSymbol(Piece piece) {
        switch(piece.getType()) {
            case PAWN: return "P";
            case ROOK: return "R";
            case KNIGHT: return "N";
            case BISHOP: return "B";
            case QUEEN: return "Q";
            case KING: return "K";
            default: return "?";
        }
    }
}