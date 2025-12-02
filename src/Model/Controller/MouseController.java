package Model.Controller;

import Model.*;
import Model.Pieces.*;
import Model.View.*;

import java.awt.event.*;
import java.util.*;

/**
 * Handles all mouse events on the BoardPanel.
 * It extends MouseAdapter (which handles both mouseListener
 * and mouseMotionListener).
 * Implements both "click-to-move" and "drag-and-drop" logic.
 */
public class MouseController extends MouseAdapter{
    //State variables for tracking the GUI
    private final GameController gameController;
    private final BoardPanel boardPanel;

    //For click-to-move logic
    private Position selectedModelPos = null;

    //For drag-and-drop logic
    private Position dragStartModelPos = null;

    //flag for deselecting on second click
    private boolean clickingSelectedPiece = false;

    public MouseController(GameController gameController, BoardPanel boardPanel){
        this.gameController = gameController;
        this.boardPanel = boardPanel;
    }

    /**
     * Mouse Pressed: This initiates the move (either selection or drag).
     */
    @Override
    public void mousePressed(MouseEvent e){
        if(gameController.getGameState() == null){
            return;
        }

        // Reset flag
        clickingSelectedPiece = false;

        // 1. Convert click to Model Position
        Position modelPos = boardPanel.getModelPosition(e.getX(), e.getY());

        if(!modelPos.isOnBoard()){
            // Clicked outside board (e.g. margin) -> deselect
            selectedModelPos = null;
            boardPanel.clearSelections();
            return;
        }

        // 2. Check if we clicked a piece
        Piece clickedPiece = gameController.getGameState().getBoard().getPieceAt(modelPos);
        boolean isMyTurn = (clickedPiece != null && clickedPiece.isWhite() == gameController.getGameState().isWhiteTurn());

        // 3. Handle Logic
        if(selectedModelPos == null){
            // No previous selection -> Select this piece if it's ours
            if(isMyTurn){
                selectPiece(modelPos);
                // Also start dragging
                dragStartModelPos = modelPos;
                boardPanel.startDrag(modelPos, e.getPoint());
            }
        }else{
            // We already have a selection
            if(modelPos.equals(selectedModelPos)){
                // Clicked same piece -> deselect
                clickingSelectedPiece = true;

                // Let's also re-drag.
                dragStartModelPos = modelPos;
                boardPanel.startDrag(modelPos, e.getPoint());
            }else{
                // Clicked different square -> Move attempt(foe) OR Select new piece(friendly)
                if(isMyTurn){
                    // Clicked another friendly piece -> Change selection
                    selectPiece(modelPos);
                    dragStartModelPos = modelPos;
                    boardPanel.startDrag(modelPos, e.getPoint());
                }else{
                    // Clicked empty or enemy -> Move attempt
                    gameController.handleMoveAttempt(selectedModelPos, modelPos);
                    selectedModelPos = null;
                    dragStartModelPos = null;
                    boardPanel.clearSelections();
                }
            }
        }
    }

    private void selectPiece(Position pos){
        selectedModelPos = pos;
        boardPanel.setSelectedPosition(pos);
        Set<Position> moves = gameController.getValidMovesForPiece(pos);
        boardPanel.showValidMoves(moves);
    }

    /**
     * Mouse Dragged: Update the visual position of the dragged piece.
     */
    @Override
    public void mouseDragged(MouseEvent e){
        if(dragStartModelPos != null){
            //Update the view to show where the piece is being dragged
            boardPanel.updateDrag(e.getPoint());
        }
    }

    /**
     * Mouse Released: This completes the move.
     */
    @Override
    public void mouseReleased(MouseEvent e){
        if(dragStartModelPos == null){
            return; //In the unlikely event we didn't initiate the drag, we simply ignore it
        }

        Position releasePos = boardPanel.getModelPosition(e.getX(), e.getY());
        boardPanel.stopDrag();

        // 1. DRAG-AND-DROP: Released on a different square
        if(releasePos.isOnBoard() && !releasePos.equals(dragStartModelPos)){
            // Drag ended on a different valid square -> Move attempt
            gameController.handleMoveAttempt(dragStartModelPos, releasePos);

            // After a drag-move attempt, we usually clear selection
            selectedModelPos = null;
            boardPanel.clearSelections();
        }
        // 2. CLICK: Released on the same square
        else{
            // Checking if we should deselect
            if(clickingSelectedPiece){
                // We clicked (pressed and released) on the piece that was ALREADY selected.
                // This means "Deselect".
                selectedModelPos = null;
                boardPanel.clearSelections();
            }
            // If clickingSelectedPiece is false, it means we just selected it in this click (first click),
            // so we keep the selection.
        }

        // If released on the same square, we keep the selection (from mousePressed)
        dragStartModelPos = null;
        clickingSelectedPiece = false;
    }
}