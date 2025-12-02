package Model.View;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;

import Model.*;

public class SidePanel extends JPanel{
    private final MoveLogModel moveLogModel;
    private final JTable moveTable;

    private final JLabel whitePlayerLabel;
    private final JLabel blackPlayerLabel;
    private final JPanel whitePanel;
    private final JPanel blackPanel;

    public SidePanel(){
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(300, 8 * BoardPanel.TILE_SIZE));
        this.setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Create player info panels
        this.whitePanel = createPlayerPanel("White");
        this.whitePlayerLabel = (JLabel) ((JPanel) whitePanel.getComponent(0)).getComponent(0);

        this.blackPanel = createPlayerPanel("Black");
        this.blackPlayerLabel = (JLabel) ((JPanel) blackPanel.getComponent(0)).getComponent(0);

        // Highlight white's turn by default
        updatePlayerTurn(true);

        // 2. Create the move log table (JTable requirement)
        this.moveLogModel = new MoveLogModel();
        this.moveTable = new JTable(moveLogModel);
        this.moveTable.setFillsViewportHeight(true);

        // Put the table in a scroll pane
        JScrollPane scrollPane = new JScrollPane(moveTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Move History"));

        // 3. Add components to the SidePanel
        this.add(whitePanel, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);
        this.add(blackPanel, BorderLayout.SOUTH);
    }

    /**
     * Helper method to create a standardized panel for player info.
     */
    private JPanel createPlayerPanel(String title){
        JPanel outerPanel = new JPanel(new BorderLayout());
        outerPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), title,
                TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION,
                null, Color.BLACK
        ));

        JPanel innerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        innerPanel.add(new JLabel("Player: " + title)); // Placeholder

        outerPanel.add(innerPanel, BorderLayout.CENTER);
        return outerPanel;
    }

    /**
     * Called by GameController to update the player names and ELO.
     */
    public void updatePlayerInfo(Player white, Player black){
        whitePlayerLabel.setText(String.format("%s (%d)", white.getName(), white.getElo()));
        blackPlayerLabel.setText(String.format("%s (%d)", black.getName(), black.getElo()));
    }

    /**
     * Called by GameController to visually indicate whose turn it is.
     */
    public void updatePlayerTurn(boolean isWhiteTurn){
        if(isWhiteTurn){
            whitePanel.setBackground(Color.LIGHT_GRAY);
            blackPanel.setBackground(null); // Reset to default
        }else{
            whitePanel.setBackground(null); // Reset to default
            blackPanel.setBackground(Color.LIGHT_GRAY);
        }
        this.repaint();
    }

    /**
     * Called by GameController after a move is made.
     * It passes the new list to the table model.
     */
    public void updateMoveHistory(List<Move> moves){
        moveLogModel.setMoves(moves);
    }
}
