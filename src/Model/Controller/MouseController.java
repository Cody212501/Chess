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

    public MouseController(GameController gameController, BoardPanel boardPanel){
        this.gameController = gameController;
        this.boardPanel = boardPanel;
    }

    /**
     * Mouse Pressed: This initiates the move (either selection or drag).
     */
    @Override
    public void mousePressed(MouseEvent e){
        if (gameController.getGameState() == null){
            return;
        }

        // 1. Get VIEW coordinates from mouse
        int viewColumn = e.getX() / BoardPanel.TILE_SIZE;
        int viewRow = e.getY() / BoardPanel.TILE_SIZE;

        // 2. Translate VIEW coordinates to MODEL coordinates
        Position modelClickPos = boardPanel.getModelPosition(viewRow, viewColumn);

        if (!modelClickPos.isOnBoard()){
            return;
        }

        // 3. Store the MODEL position for the drag
        dragStartModelPos = modelClickPos;

        // Start the visual drag (this uses the *raw* mouse point)
        boardPanel.startDrag(modelClickPos, e.getPoint());
    }

    /**
     * Mouse Dragged: Update the visual position of the dragged piece.
     */
    @Override
    public void mouseDragged(MouseEvent e){
        if (dragStartModelPos != null){
            //Update the view to show where the piece is being dragged
            boardPanel.updateDrag(e.getPoint());
        }
    }

    /**
     * Mouse Released: This completes the move.
     */
    @Override
    public void mouseReleased(MouseEvent e){
        if (dragStartModelPos == null){
            return; //In the unlikely event we didn't initiate the drag, ignore it
        }

        // 1. Get VIEW coordinates from mouse release
        int viewColumn = e.getX() / BoardPanel.TILE_SIZE;
        int viewRow = e.getY() / BoardPanel.TILE_SIZE;

        // 2. Translate VIEW coordinates to MODEL coordinates
        Position modelReleasePos = boardPanel.getModelPosition(viewRow, viewColumn);

        //Finalise the visual drag (hides the dragged piece)
        boardPanel.stopDrag();

        // 3. Use MODEL positions for all logic
        if (modelReleasePos.isOnBoard()) {
            if (!dragStartModelPos.equals(modelReleasePos)) {
                // Case 1: DRAG-AND-DROP
                gameController.handleMoveAttempt(dragStartModelPos, modelReleasePos);
            } else {
                // Case 2: CLICK
                handleBoardClick(modelReleasePos);
            }
        }

        //Whatever happened, the drag operation is over
        dragStartModelPos = null;

        //If it was a drag-and-drop, clear selection.
        //If it was a click, let handleBoardClick manage the selection.
        if (!modelReleasePos.equals(dragStartModelPos)) {
            selectedModelPos = null;
            boardPanel.clearSelections();
        }
    }

    /**
     * Separate function for "click-to-move" logic.
     * @param modelClickPos The square the user clicked on.
     */
    private void handleBoardClick(Position modelClickPos){
        //Case 1: First click (no selection yet)
        if (selectedModelPos == null){
            //Get the moves to see if this is a valid piece to select
            Set<Position> validMoves = gameController.getValidMovesForPiece(modelClickPos);

            // Check the piece at the MODEL position
            Piece clickedPiece = gameController.getGameState().getBoard().getPieceAt(modelClickPos);

            //Only select if it's the player's own piece
            if (clickedPiece != null &&
                    clickedPiece.isWhite() == gameController.getGameState().isWhiteTurn())
            {
                selectedModelPos = modelClickPos;
                boardPanel.setSelectedPosition(modelClickPos); //Tell view to highlight this model pos
                boardPanel.showValidMoves(validMoves); //Show the dots
            }
        }
        //Case 2: Second click (selection already exists)
        else {
            //If clicking the same square, deselect
            if (selectedModelPos.equals(modelClickPos)){
                selectedModelPos = null;
                boardPanel.clearSelections();
            }
            //If clicking elsewhere, it's a move attempt
            else {
                gameController.handleMoveAttempt(selectedModelPos, modelClickPos);
                //After the move, clear all GUI selections
                selectedModelPos = null;
                boardPanel.clearSelections();
            }
        }
    }
}