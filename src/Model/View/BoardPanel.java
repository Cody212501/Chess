package Model.View;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.io.*;
import javax.imageio.*;

import Model.*;
import Model.Pieces.*;

public class BoardPanel extends JPanel{
    public static final int TILE_SIZE = 80;

    // --- State variables managed by the Controller ---
    private Board currentBoard;
    private Set<Position> validMoves;
    private Position selectedPosition;

    // --- Viewpoint variable ---
    private boolean isViewFromWhiteSide = true; // Default to White's view

    // --- Drag-and-Drop GUI state ---
    private Piece draggedPiece;
    private Point dragPosition;

    //Image Caching
    private final Map<String, Image> pieceImages;
    private final String IMAGE_PATH_PREFIX = "/resources/ChessPiecesPictures/";

//methods start here
    public BoardPanel() {
        setPreferredSize(new Dimension(8 * TILE_SIZE, 8 * TILE_SIZE));
        setBackground(Color.LIGHT_GRAY);
        setOpaque(true); //Ensuring background is painted

        this.validMoves = new HashSet<>();
        this.pieceImages = new HashMap<>();

        loadAllPieceImages();
    }

    /**
     * Called by GameController to set the board orientation.
     */
    public void setViewpoint(boolean isWhiteTurn) {
        this.isViewFromWhiteSide = isWhiteTurn;
        this.repaint(); // Redraw the board in its new orientation
    }

    /**
     * Translates a "View" coordinate (what the user sees)
     * to a "Model" coordinate (what the Board.java logic uses).
     * @param viewRow The row from the top of the panel (0-7)
     * @param viewCol The column from the left of the panel (0-7)
     * @return The corresponding Position in the logical model.
     */
    public Position getModelPosition(int viewRow, int viewCol) {
        if (isViewFromWhiteSide) {
            // If White's view, coordinates are the same, as in the source code
            return new Position(viewRow, viewCol);
        } else {
            // If Black's view, the board is flipped
            // View row 0 is Model row 7
            // View col 0 is Model col 7
            return new Position(7 - viewRow, 7 - viewCol);
        }
    }

    /**
     * Translates a "Model" coordinate (from Board.java)
     * to a "View" coordinate (for drawing on the panel).
     * @param modelPos The logical position from the model.
     * @return A Point(x, y) for drawing.
     */
    private Point getViewCoordinates(Position modelPos) {
        int viewRow;
        int viewCol;
        if (isViewFromWhiteSide) {
            viewRow = modelPos.row();
            viewCol = modelPos.column();
        } else {
            viewRow = 7 - modelPos.row();
            viewCol = 7 - modelPos.column();
        }
        return new Point(viewCol * TILE_SIZE, viewRow * TILE_SIZE);
    }

    /**
     * The core drawing method.
     * This now iterates over VIEW coordinates and translates them
     * to MODEL coordinates to fetch the correct piece.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Iterate over the VIEW (panel)
        for (int viewRow = 0; viewRow < 8; viewRow++) {
            for (int viewCol = 0; viewCol < 8; viewCol++) {
                // Get the corresponding MODEL position for this view square
                Position modelPos = getModelPosition(viewRow, viewCol);

                int x = viewCol * TILE_SIZE;
                int y = viewRow * TILE_SIZE;

                // 1. Draw the tile
                // Tile colour depends on the MODEL position
                Color color = (modelPos.row() + modelPos.column()) % 2 == 0 ? Color.WHITE : new Color(118, 150, 86);
                g2.setColor(color);
                g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                // 2. Draw the piece
                if (currentBoard != null) {
                    Piece piece = currentBoard.getPieceAt(modelPos);
                    if (piece != null && piece != draggedPiece) {
                        drawPieceAt(g2, piece, x, y);
                    }
                }
            }
        }

        // 3. Highlight selected square (must be translated)
        drawSelectedSquare(g2);

        // 4. Highlight valid moves (must be translated)
        drawValidMoves(g2);

        // 5. Draw dragged piece (this follows the mouse, no translation)
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

    /**
     * Loads all chess piece images into a cache.
     * This prevents reloading the same image multiple times, improving performance.
     */
    private void loadAllPieceImages() {
        String[] colors = {"White", "Black"};
        String[] types = {"Pawn", "Rook", "Knight", "Bishop", "Queen", "King"};

        for (String color : colors) {
            for (String type : types) {
                String fileName = color + type + ".png";
                try {
                    // Use getResource() for loading from JAR (deployed app) or file system (IDE development)
                    Image img = ImageIO.read(Objects.requireNonNull(
                            getClass().getResource(IMAGE_PATH_PREFIX + fileName),
                            "Image not found: " + IMAGE_PATH_PREFIX + fileName
                    ));
                    pieceImages.put(fileName, img.getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH));
                } catch (IOException e) {
                    System.err.println("Error loading image: " + fileName + " - " + e.getMessage());
                } catch (NullPointerException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * Returns the appropriate image for a given piece.
     * @param piece The piece for which to get the image.
     * @return The scaled Image object, or null if not found.
     */
    private Image getPieceImage(Piece piece) {
        if (piece == null) return null;
        String fileName = (piece.isWhite() ? "White" : "Black") + piece.getType().name() + ".png";
        return pieceImages.get(fileName);
    }

    private void drawPieceAt(Graphics2D g2, Piece piece, int x, int y) {
        if (currentBoard == null) return;

        Image pieceImage = getPieceImage(piece);
        if (pieceImage != null) {
            g2.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
        } else {
            // Fallback: draw text if image not found (unless something goes awry[e.g: failed download of repo], this is never used)
            g2.setColor(piece.isWhite() ? Color.DARK_GRAY : Color.BLACK);
            g2.setFont(new Font("Arial", Font.BOLD, 36));
            g2.drawString(getPieceSymbol(piece), x + 25, y + 50);
        }
    }

    private void drawSelectedSquare(Graphics2D g2) {
        if (selectedPosition != null) {
            Point viewPoint = getViewCoordinates(selectedPosition);
            g2.setColor(new Color(255, 255, 0, 100));
            g2.fillRect(viewPoint.x, viewPoint.y, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawValidMoves(Graphics2D g2) {
        g2.setColor(new Color(139, 134, 128, 80)); // Transparent grey

        for (Position modelPos : validMoves) {
            Point viewPoint = getViewCoordinates(modelPos);

            int x = viewPoint.x + TILE_SIZE / 2;
            int y = viewPoint.y + TILE_SIZE / 2;
            int radius = TILE_SIZE / 6;
            g2.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }

    private void drawDraggedPiece(Graphics2D g2) {
        if (draggedPiece != null && dragPosition != null) {
            // This is drawn at the raw mouse coordinate, no translation needed
            int x = dragPosition.x - TILE_SIZE / 2;
            int y = dragPosition.y - TILE_SIZE / 2;

            //Adjusting for drawString offset
            drawPieceAt(g2, draggedPiece, x - 25, y - 50);
        }
    }

    private String getPieceSymbol(Piece piece) {
        if (piece == null){
            return "?";
        }
        switch(piece.getType()) {
            case PAWN: return "P";
            case KNIGHT: return "N";
            case BISHOP: return "B";
            case ROOK: return "R";
            case QUEEN: return "Q";
            case KING: return "K";
            default: return "?";
        }
    }
}