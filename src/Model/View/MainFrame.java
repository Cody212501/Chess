package Model.View;

import javax.swing.*;
import java.awt.*;

import Model.Controller.*;

public class MainFrame extends JFrame{
    private BoardPanel boardPanel;
    private SidePanel sidePanel;

    // Controllers must be referenced to keep them alive and connected
    private final GameController gameController;
    private final MenuController menuController;
    private final MouseController mouseController;

    public MainFrame(){
        setTitle("Sakk Program");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. Create View components
        boardPanel = new BoardPanel();
        sidePanel = new SidePanel();

        add(boardPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        // 2. Initialize Controllers (Connecting MVC)
        // GameController coordinates the logic
        gameController = new GameController(this, boardPanel, sidePanel);

        // MenuController handles menu clicks
        menuController = new MenuController(gameController);

        // MouseController handles board interaction
        mouseController = new MouseController(gameController, boardPanel);

        // 3. Setup Menu (Now passing the controller!)
        createMenuBar(menuController);

        // 4. Setup Listeners
        boardPanel.addMouseListener(mouseController);
        boardPanel.addMouseMotionListener(mouseController);

        // 5. Finalize Window
        pack(); // Resize the window to fit components
        setLocationRelativeTo(null); // Center on screen
        setResizable(false);
        setVisible(true);

        // Start the application logic
        gameController.start();
    }

    private void createMenuBar(MenuController listener){
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("Fájl");
        addMenuItem(fileMenu, "Új Játék...", "NEW_GAME", listener);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Állapot Mentése (JSON)...", "SAVE_GAME_JSON", listener);
        addMenuItem(fileMenu, "Állapot Betöltése (JSON)...", "LOAD_GAME_JSON", listener);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Exportálás PGN...", "EXPORT_PGN", listener);
        // Now importing is implemented
        addMenuItem(fileMenu, "Importálás PGN...", "IMPORT_PGN", listener);
        fileMenu.addSeparator();
        addMenuItem(fileMenu, "Kilépés", "EXIT", listener);

        // Game Menu
        JMenu gameMenu = new JMenu("Játék");
        addMenuItem(gameMenu, "Döntetlen ajánlása", "OFFER_DRAW", listener);

        menuBar.add(fileMenu);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    /**
     * Helper method to create a JMenuItem and attach the listener.
     */
    private void addMenuItem(JMenu menu, String text, String command, MenuController listener){
        JMenuItem item = new JMenuItem(text);
        item.setActionCommand(command);
        item.addActionListener(listener);
        menu.add(item);
    }
}