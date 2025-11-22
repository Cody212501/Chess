package Model.View;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.*;
import java.io.*;
import javax.imageio.*;

import Model.*;
import Model.Pieces.*;

public class BoardPanel extends JPanel{
    public static final int TILE_SIZE = 80;
    public static final int MARGIN = 30;

    //State variables managed by the Controller
    private Board currentBoard;
    private Set<Position> validMoves;
    private Position selectedPosition;

    //Viewpoint variable
    private boolean isViewFromWhiteSide = true; // Default to White's view

    //Drag-and-Drop GUI state
    private Piece draggedPiece;
    private Point dragPosition;

    //Image Caching
    private final Map<String, Image> pieceImages;
    private final String IMAGE_PATH_PREFIX = "/Pictures/";

    // Red highlighting for King in check
    private Position kingInCheckPos = null;

//methods start here
    public BoardPanel() {
        setPreferredSize(new Dimension(8 * TILE_SIZE + 2 * MARGIN, 8 * TILE_SIZE + 2 * MARGIN));
        setBackground(new Color(40, 40, 40));
        setOpaque(true); //Ensuring background is painted

        this.validMoves = new HashSet<>();
        this.pieceImages = new HashMap<>();
        //initializing board because of NPE
        this.currentBoard = new Board();

        loadAllPieceImages();
    }

    /**
     * Called by GameController to set the board orientation.
     */
    public void setViewpoint(boolean isWhiteTurn) {
        this.isViewFromWhiteSide = isWhiteTurn;
        repaint(); // Redraw the board in its new orientation
    }

    public void setKingInCheck(Position pos) {
        this.kingInCheckPos = pos;
        repaint();
    }

    /**
     * Translates a "View" coordinate (what the user sees)
     * to a "Model" coordinate (what the Board.java logic uses).
     * @param viewRow The row from the top of the panel (0-7)
     * @param viewCol The column from the left of the panel (0-7)
     * @return The corresponding Position in the logical model.
     */
    public Position getModelPosition(int viewRow, int viewCol) {
        // Convert raw pixel coordinates (relative to this panel) to model Position (row,column)

        // 1) Adjust for margin
        int x = (viewRow - MARGIN);
        int y = (viewCol - MARGIN);

        // 2) are we within the confines of the board?
        if (x < 0 || x >= 8 * TILE_SIZE || y < 0 || y >= 8 * TILE_SIZE){
            return new Position(-1, -1);
        }

        // 3) Convert to view indices (0-7)
        int viewingColumn = x / TILE_SIZE; // horizontal
        int viewingRow = y / TILE_SIZE; // vertical


        // 4) Map view indices to model indices depending on viewpoint orientation
        if (isViewFromWhiteSide) {
            // If White's view, coordinates are the same, as in the source code
            return new Position(viewingRow, viewingColumn);
        } else {
            // If Black's view, the board is flipped
            // View row 0 is Model row 7
            // View col 0 is Model col 7
            return new Position(7 - viewingRow, 7 - viewingColumn);
        }
    }

    /**
     * Model Position -> Screen Coordinate (relative to grid top-left)
     * Does NOT include MARGIN! (Because we use g2.translate)
     */
    private Point getRelativeScreenCoordinates(Position modelPos) {
        int viewRow, viewCol;

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

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. Draw Coordinates (outside the translated grid)
        drawCoordinates(g2);

        // 2. Translate grid to accounting for margin
        g2.translate(MARGIN, MARGIN);

        // 3. Draw Board & Pieces by iterating over the VIEW (panel)
        for (int viewRow = 0; viewRow < 8; viewRow++) {
            for (int viewCol = 0; viewCol < 8; viewCol++) {
                // Get the corresponding MODEL position for this view square
                Position modelPos = isViewFromWhiteSide ?
                        new Position(viewRow, viewCol) : new Position(7 - viewRow, 7 - viewCol);

                int x = viewCol * TILE_SIZE;
                int y = viewRow * TILE_SIZE;

                // 1. Draw the tile
                // Tile colour depends on the MODEL position
                Color color = (modelPos.row() + modelPos.column()) % 2 == 0 ?
                        new Color(205, 170, 125) : // Light wood
                        new Color(119, 148, 85);   // Green
                g2.setColor(color);
                g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);

                // 2. Highlight King in Check (Red Background)
                if (kingInCheckPos != null && kingInCheckPos.equals(modelPos)) {
                    g2.setColor(new Color(255, 0, 0, 180));
                    g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);
                }

                // 3. Draw the piece
                if (currentBoard != null) {
                    Piece piece = currentBoard.getPieceAt(modelPos);
                    if (piece != null && piece != draggedPiece) {
                        drawPieceAt(g2, piece, x, y);
                    }
                }
            }
        }

        // 4. Highlights (Selected & Valid Moves)
        drawSelectedSquare(g2);
        drawValidMoves(g2);

        // 5. Draw Dragged Piece (Translated back to frame coords)
        g2.translate(-MARGIN, -MARGIN);
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

    private void drawCoordinates(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < 8; i++) {
            // Rows (1-8)
            String rowStr = isViewFromWhiteSide ? String.valueOf(8 - i) : String.valueOf(i + 1);
            int y = MARGIN + i * TILE_SIZE + TILE_SIZE/2 + fm.getAscent()/2;
            g2.drawString(rowStr, MARGIN/2 - fm.stringWidth(rowStr)/2, y);

            // Columns (A-H)
            String colStr = isViewFromWhiteSide ? String.valueOf((char)('a' + i)) : String.valueOf((char)('h' - i));
            int x = MARGIN + i * TILE_SIZE + TILE_SIZE/2 - fm.stringWidth(colStr)/2;
            g2.drawString(colStr, x, 8 * TILE_SIZE + MARGIN + MARGIN/2 + fm.getAscent()/2);
        }
    }

    private void drawSelectedSquare(Graphics2D g2) {
        if (selectedPosition != null) {
            Point p = getRelativeScreenCoordinates(selectedPosition);
            // Point already includes Margin because getViewCoordinates adds it.
            // BUT we are inside a g2.translate(MARGIN, MARGIN) block, so we must subtract margin here.
            g2.setColor(new Color(255, 255, 0, 100)); // Transparent Yellow
            g2.fillRect(p.x, p.y, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawValidMoves(Graphics2D g2) {
        g2.setColor(new Color(139, 134, 128, 80)); // Transparent grey

        for (Position modelPos : validMoves) {
            Point p = getRelativeScreenCoordinates(modelPos);

            Piece targetPiece = currentBoard.getPieceAt(modelPos);

            if (targetPiece != null) {
                // Capture: Yellow corners or full background highlight
                g2.setColor(new Color(255, 100, 0, 150)); // Orange/Reddish ring
                g2.fillRect(p.x, p.y, TILE_SIZE, TILE_SIZE);
            } else {
                // Move: Grey circle
                g2.setColor(new Color(100, 100, 100, 128));
                int radius = TILE_SIZE / 6;
                g2.fillOval(p.x + TILE_SIZE/2 - radius, p.y + TILE_SIZE/2 - radius, radius * 2, radius * 2);
            }
        }
    }

    private void drawDraggedPiece(Graphics2D g2) {
        if (draggedPiece != null && dragPosition != null) {
            // This is drawn at the raw mouse coordinate, no translation needed
            int x = dragPosition.x - TILE_SIZE / 2;
            int y = dragPosition.y - TILE_SIZE / 2;

            //Adjusting for drawString offset
            drawPieceAt(g2, draggedPiece, x, y);
        }
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
                String fileName = color + type + ".jpg";
                String path = IMAGE_PATH_PREFIX + fileName;
                try {
                    /**
                     *  Use getResource() for loading from JAR (deployed app) or file system (IDE development)
                     *  Image img = ImageIO.read(Objects.requireNonNull(
                     *      getClass().getResource(IMAGE_PATH_PREFIX + fileName),
                     *       "Image not found: " + IMAGE_PATH_PREFIX + fileName
                     *));
                     */

                    // FIX(hopefully): getClass().getResource expects path from root of classpath
                    URL url = getClass().getResource(path);
                    if (url == null) {
                        System.err.println("Image not found: " + path);
                        continue;
                    }

                    Image img = ImageIO.read(url);
                    System.out.println("Image " + fileName +" found at: " + path);
                    pieceImages.put(fileName, img.getScaledInstance(TILE_SIZE, TILE_SIZE, Image.SCALE_SMOOTH));
                    System.out.println("Image loaded!");

                } catch (IOException e) {
                    System.err.println("Error loading image: " + fileName + " - " + e.getMessage());
                } catch (NullPointerException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void drawPieceAt(Graphics2D g2, Piece piece, int x, int y) {
        if (currentBoard == null) return;

        Image pieceImage = getPieceImage(piece);
        if (pieceImage != null) {
            g2.drawImage(pieceImage, x, y, TILE_SIZE, TILE_SIZE, this);
        } else {
            /**
             * great resource: https://en.wikipedia.org/wiki/Chess_symbols_in_Unicode
             * the white pieces are hollow, while the black pieces are filled in
             */
            // Fallback: Draw Unicode Chess Symbols if image fails (unless something goes awry[e.g: failed download of repo], this is never used)
            g2.setColor(piece.isWhite() ? Color.WHITE : Color.BLACK);
            // Draw text outline for visibility
            g2.setFont(new Font("SansSerif", Font.BOLD, 40));
            String s = getPieceSymbol(piece);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (TILE_SIZE - fm.stringWidth(s)) / 2;
            int ty = y + (TILE_SIZE - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(s, tx, ty);
        }
    }

    /**
     * Returns the appropriate image for a given piece.
     * @param piece The piece for which to get the image.
     * @return The scaled Image object, or null if not found.
     */
    private Image getPieceImage(Piece piece) {
        if (piece == null) return null;
        String fileName = (piece.isWhite() ? "White" : "Black") + piece.getType().name() + ".jpg";
        return pieceImages.get(fileName);
    }

    //fallback option just in case
    private String getPieceSymbol(Piece piece) {
        if (piece == null){
            return "?";
        }
        if (piece.isWhite()) {
            switch(piece.getType()) {
                case PAWN: return "♙";
                case ROOK: return "♖";
                case KNIGHT: return "♘";
                case BISHOP: return "♗";
                case QUEEN: return "♕";
                case KING: return "♔";
            }
        } else {
            switch(piece.getType()) {
                case PAWN: return "♟";
                case ROOK: return "♜";
                case KNIGHT: return "♞";
                case BISHOP: return "♝";
                case QUEEN: return "♛";
                case KING: return "♚";
            }
        }
        return "?";
    }
}