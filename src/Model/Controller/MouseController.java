package Model.Controller;

import Model.*;
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
    private final GameController gameController;
    private final BoardPanel boardPanel;

    //State variables for tracking the GUI

    //For click-to-move logic
    private Position selectedPos = null;

    //For drag-and-drop logic
    private Position dragStartPos = null;

    public MouseController(GameController gameController, BoardPanel boardPanel){
        this.gameController = gameController;
        this.boardPanel = boardPanel;
    }

    /**
     * Mouse Pressed: This initiates the move (either selection or drag).
     */
    @Override
    public void mousePressed(MouseEvent e){
        if (gameController.getGameState() == null) return;

        int col = e.getX() / BoardPanel.TILE_SIZE;
        int row = e.getY() / BoardPanel.TILE_SIZE;
        Position clickPos = new Position(row, col);

        //If the click is off-board, do nothing
        if (!clickPos.isOnBoard()) return;

        //This is the start of a drag
        dragStartPos = clickPos;

        //Start the visual drag on the BoardPanel
        boardPanel.startDrag(clickPos, e.getPoint());
    }

    /**
     * Mouse Dragged: Update the visual position of the dragged piece.
     */
    @Override
    public void mouseDragged(MouseEvent e){
        if (dragStartPos != null){
            //Update the view to show where the piece is being dragged
            boardPanel.updateDrag(e.getPoint());
        }
    }

    /**
     * Mouse Released: This completes the move.
     */
    @Override
    public void mouseReleased(MouseEvent e){
        if (dragStartPos == null) return; //If we didn't initiate the drag, ignore

        int col = e.getX() / BoardPanel.TILE_SIZE;
        int row = e.getY() / BoardPanel.TILE_SIZE;
        Position releasePos = new Position(row, col);

        //Finalise the visual drag (hides the dragged piece)
        boardPanel.stopDrag();

        //--- Logic ---

        //Case 1: DRAG-AND-DROP
        //If the start and end positions are different.
        if (!dragStartPos.equals(releasePos) && releasePos.isOnBoard()){
            gameController.handleMoveAttempt(dragStartPos, releasePos);
        }
        //Case 2: CLICK
        //If the start and end positions are the same (it was a simple click).
        else if (dragStartPos.equals(releasePos)){
            handleBoardClick(releasePos);
        }

        //Whatever happened, the drag operation is over
        dragStartPos = null;

        //If it was a drag-and-drop, clear selection.
        //If it was a click, let handleBoardClick manage the selection.
        if (!dragStartPos.equals(releasePos)){
            selectedPos = null;
            boardPanel.clearSelections();
        }
    }

    /**
     * Separate function for "click-to-move" logic.
     * @param clickPos The square the user clicked on.
     */
    private void handleBoardClick(Position clickPos){
        //Case 1: First click (no selection yet)
        if (selectedPos == null){
            //Get the moves to see if this is a valid piece to select
            Set<Position> validMoves = gameController.getValidMovesForPiece(clickPos);

            //Only select if it's the player's own piece
            if (gameController.getGameState().getBoard().getPieceAt(clickPos) != null &&
                    gameController.getGameState().getBoard().getPieceAt(clickPos).isWhite() == gameController.getGameState().isWhiteTurn())
            {
                selectedPos = clickPos;
                boardPanel.setSelectedPosition(clickPos);
                boardPanel.showValidMoves(validMoves); //Show the dots
            }
        }
        //Case 2: Second click (selection already exists)
        else {
            //If clicking the same square, deselect
            if (selectedPos.equals(clickPos)){
                selectedPos = null;
                boardPanel.clearSelections();
            }
            //If clicking elsewhere, it's a move attempt
            else {
                gameController.handleMoveAttempt(selectedPos, clickPos);
                //After the move, clear all GUI selections
                selectedPos = null;
                boardPanel.clearSelections();
            }
        }
    }
}