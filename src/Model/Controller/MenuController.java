package Model.Controller;

import java.awt.event.*;

/**
 * Handles events from the JMenuBar.
 * It routes all menu item clicks to the GameController.
 * This implements the ActionListener interface.
 */
public class MenuController implements ActionListener{
    private final GameController gameController;

    public MenuController(GameController gameController){
        this.gameController = gameController;
    }

    /**
     * This ActionListener is added to the JMenuItems in MainFrame.
     * It decides what to do based on the ActionEvent's "action command".
     */
    @Override
    public void actionPerformed(ActionEvent e){
        String command = e.getActionCommand();

        if(command == null){
            return;
        }

        switch(command){
            case "NEW_GAME":
                gameController.showNewGameDialog();
                break;
            case "SAVE_GAME_JSON":
                gameController.handleSaveGame();
                break;
            case "LOAD_GAME_JSON":
                gameController.handleLoadGame();
                break;
            case "OFFER_DRAW":
                gameController.handleDrawOffer();
                break;
            case "EXPORT_PGN":
                gameController.handleExportPgn();
                break;
            case "IMPORT_PGN":
                gameController.handleImportPgn();
                break;
            case "EXIT":
                System.exit(0);
                break;
            default:
                System.out.println("Unknown menu command: " + command);
                break;
        }
    }
}