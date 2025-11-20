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
        this.repaint(); // Redraw the board in its new orientation
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
        // Adjust for margin
        int x = (viewRow - MARGIN) / TILE_SIZE;
        int y = (viewCol - MARGIN) / TILE_SIZE;

        if (x < 0 || x > 7 || y < 0 || y > 7){
            return new Position(-1, -1);
        }

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
        return new Point(viewCol * TILE_SIZE + MARGIN, viewRow * TILE_SIZE + MARGIN);
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

        drawCoordinates(g2);

        // Translate grid to account for margin
        g2.translate(MARGIN, MARGIN);

        // Iterate over the VIEW (panel)
        for (int viewRow = 0; viewRow < 8; viewRow++) {
            for (int viewCol = 0; viewCol < 8; viewCol++) {
                // Get the corresponding MODEL position for this view square
                Position modelPos = getModelPosition(viewRow, viewCol);

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
                    g2.setColor(new Color(255, 0, 0, 150));
                    // Create a radial gradient for nicer look
                    RadialGradientPaint rgp = new RadialGradientPaint(
                            new Point(x + TILE_SIZE/2, y + TILE_SIZE/2),
                            TILE_SIZE/2,
                            new float[]{0f, 1f},
                            new Color[]{new Color(255, 60, 60, 200), new Color(200, 0, 0, 0)});
                    g2.setPaint(rgp);
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
                String resourcePath = IMAGE_PATH_PREFIX + fileName;
                try {
                    /**
                     *  Use getResource() for loading from JAR (deployed app) or file system (IDE development)
                     *  Image img = ImageIO.read(Objects.requireNonNull(
                     *      getClass().getResource(IMAGE_PATH_PREFIX + fileName),
                     *       "Image not found: " + IMAGE_PATH_PREFIX + fileName
                    *));
                     */

                    // FIX(hopefully): getClass().getResource expects path from root of classpath
                    java.net.URL imgURL = getClass().getResource(resourcePath);
                    if (imgURL == null) {
                        System.err.println("Image not found: " + resourcePath);
                        continue;
                    }

                    Image img = ImageIO.read(imgURL);
                    System.out.println("Image " + fileName +" found at: " + resourcePath);
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

    private void drawCoordinates(Graphics2D g2) {
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 14));
        FontMetrics fm = g2.getFontMetrics();

        for (int i = 0; i < 8; i++) {
            // Rows (1-8)
            String rowLabel = isViewFromWhiteSide ? String.valueOf(8 - i) : String.valueOf(i + 1);
            int yPos = MARGIN + i * TILE_SIZE + TILE_SIZE / 2 + fm.getAscent() / 2 - 2;
            g2.drawString(rowLabel, MARGIN / 2 - fm.stringWidth(rowLabel) / 2, yPos);

            // Columns (A-H)
            String colLabel = isViewFromWhiteSide ? String.valueOf((char)('a' + i)) : String.valueOf((char)('h' - i));
            int xPos = MARGIN + i * TILE_SIZE + TILE_SIZE / 2 - fm.stringWidth(colLabel) / 2;
            g2.drawString(colLabel, xPos, getHeight() - MARGIN / 2 + fm.getAscent() / 2 - 2);
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
            g2.setFont(new Font("SansSerif", Font.BOLD, 60));
            String symbol = getPieceSymbol(piece);
            FontMetrics fm = g2.getFontMetrics();
            int textX = x + (TILE_SIZE - fm.stringWidth(symbol)) / 2;
            int textY = y + (TILE_SIZE - fm.getHeight()) / 2 + fm.getAscent();

            g2.drawString(symbol, textX, textY);
        }
    }

    private void drawSelectedSquare(Graphics2D g2) {
        if (selectedPosition != null) {
            Point p = getViewCoordinates(selectedPosition);
            // Point already includes Margin because getViewCoordinates adds it.
            // BUT we are inside a g2.translate(MARGIN, MARGIN) block, so we must subtract margin here.
            g2.setColor(new Color(255, 255, 0, 100));
            g2.fillRect(p.x - MARGIN, p.y - MARGIN, TILE_SIZE, TILE_SIZE);
        }
    }

    private void drawValidMoves(Graphics2D g2) {
        g2.setColor(new Color(139, 134, 128, 80)); // Transparent grey

        for (Position modelPos : validMoves) {
            Point viewPoint = getViewCoordinates(modelPos);
            // Remove margin due to translation context
            int x = viewPoint.x - MARGIN;
            int y = viewPoint.y - MARGIN;

            Piece targetPiece = currentBoard.getPieceAt(modelPos);

            if (targetPiece != null) {
                // Capture: Yellow corners or full background highlight
                g2.setColor(new Color(255, 100, 0, 150)); // Orange/Reddish ring
                int stroke = 6;
                g2.setStroke(new BasicStroke(stroke));
                g2.drawRect(x + stroke/2, y + stroke/2, TILE_SIZE - stroke, TILE_SIZE - stroke);
            } else {
                // Move: Grey circle
                g2.setColor(new Color(100, 100, 100, 128));
                int radius = TILE_SIZE / 6;
                g2.fillOval(x + TILE_SIZE/2 - radius, y + TILE_SIZE/2 - radius, radius * 2, radius * 2);
            }
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