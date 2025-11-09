package Model.View;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import Model.*;

public class BoardPanel extends JPanel{
    public static final int TILE_SIZE = 80;

    // Állapotok a GUI kezeléséhez
    // private GameState gameState; // Ezt a Controllertől kapná meg
    private Piece selectedPiece = null; // A kattintással kijelölt bábu
    private Piece draggedPiece = null; // A vonszolt bábu
    private Point dragPosition = null; // A vonszolt bábu rajzolt pozíciója

    // private Set<Position> validMoves = null; // A kijelölt bábu lépései

    public BoardPanel() {
        setPreferredSize(new Dimension(TILE_SIZE * 8, TILE_SIZE * 8));

        MouseController mouseController = new MouseController();
        addMouseListener(mouseController);
        addMouseMotionListener(mouseController);
    }

    /**
     * A Grafika (Graphics) követelmény teljesítése.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. Tábla kirajzolása
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Color color = (row + col) % 2 == 0 ? Color.WHITE : Color.GRAY;
                g2.setColor(color);
                g2.fillRect(col * TILE_SIZE, row * TILE_SIZE, TILE_SIZE, TILE_SIZE);
            }
        }

        // TODO: 2. Bábuk kirajzolása (a 'gameState' alapján)
        // g2.drawImage(piece.getImage(), x, y, TILE_SIZE, TILE_SIZE, null);

        // TODO: 3. Lehetséges lépések kirajzolása (ha 'validMoves' nem null)
        // g2.setColor(new Color(0, 255, 0, 100)); // Áttetsző zöld
        // g2.fillOval(move.getCol() * TILE_SIZE, ...);

        // 4. Vonszolt bábu kirajzolása (a kurzornál)
        if (draggedPiece != null && dragPosition != null) {
            // TODO: A 'draggedPiece' képének kirajzolása
            // g2.drawImage(draggedPiece.getImage(),
            //    dragPosition.x - TILE_SIZE / 2,
            //    dragPosition.y - TILE_SIZE / 2,
            //    TILE_SIZE, TILE_SIZE, null);
            g2.setColor(Color.BLUE); // Placeholder
            g2.drawString("VONSZOLT BÁBU", dragPosition.x - 30, dragPosition.y);
        }
    }

    /**
     * Belső osztály a kattintás és a drag-and-drop kezelésére.
     */
    private class MouseController extends MouseAdapter {

        @Override
        public void mousePressed(MouseEvent e) {
            int col = e.getX() / TILE_SIZE;
            int row = e.getY() / TILE_SIZE;

            // TODO: Lekérni a bábut a (row, col) pozícióról a gameState-ből
            // Piece pieceAtClick = gameState.getBoard().getPieceAt(row, col);

            // Ez a DRAG-AND-DROP kezdete
            // if (pieceAtClick != null && pieceAtClick.isWhite() == gameState.isWhiteTurn()) {
            //     draggedPiece = pieceAtClick;
            //     dragPosition = e.getPoint();
            //     repaint();
            // }

            System.out.println("Pressed: " + row + ", " + col);
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            // Ez a DRAG-AND-DROP közbeni mozgás
            if (draggedPiece != null) {
                dragPosition = e.getPoint();
                repaint(); // Újrarajzoljuk a panelt, hogy a bábu kövesse a kurzort
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            int col = e.getX() / TILE_SIZE;
            int row = e.getY() / TILE_SIZE;

            if (draggedPiece != null) {
                // DRAG-AND-DROP vége (leejtés)
                // TODO: Ellenőrizni, hogy (row, col) valid lépés-e a draggedPiece-nek
                // Ha igen: controller.makeMove(draggedPiece, row, col);

                System.out.println("Dropped " + draggedPiece + " at " + row + ", " + col);
                draggedPiece = null; // Vonszolás vége

            } else {
                // KATTINTÁSOS LÉPÉS (Kijelölés -> Cél)

                if (selectedPiece == null) {
                    // Első kattintás: BÁBU KIJELÖLÉSE
                    // TODO: Ellenőrizni, van-e itt saját bábu
                    // selectedPiece = pieceAtClick;
                    // validMoves = ruleEngine.getValidMoves(selectedPiece);
                    System.out.println("Selected piece at " + row + ", " + col);

                } else {
                    // Második kattintás: CÉLMEZŐ KIJELÖLÉSE
                    // TODO: Ellenőrizni, hogy (row, col) benne van-e a validMoves-ban
                    // Ha igen: controller.makeMove(selectedPiece, row, col);

                    System.out.println("Moved " + selectedPiece + " to " + row + ", " + col);
                    selectedPiece = null; // Kijelölés törlése
                    // validMoves = null;
                }
            }

            repaint(); // Mindig rajzoljunk újra a lépés után
        }
    }
}