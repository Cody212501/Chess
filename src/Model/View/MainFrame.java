package Model.View;

import javax.swing.*;
import java.awt.*;

import Model.Controller.*;

public class MainFrame extends JFrame{
    private BoardPanel boardPanel;
    private SidePanel sidePanel;
    // private GameController controller; // A vezérlő

    public MainFrame() {
        setTitle("Sakk Program");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 1. A Menü (Kötelező követelmény)
        createMenuBar();

        // 2. A sakktábla (Graphics)
        boardPanel = new BoardPanel(); // Itt történik a rajzolás
        add(boardPanel, BorderLayout.CENTER);

        // 3. Oldalsáv (JTable)
        sidePanel = new SidePanel(); // Ez tartalmazza a JTable-t
        add(sidePanel, BorderLayout.EAST);

        // Controller inicializálása (később)
        // controller = new GameController(this, boardPanel, sidePanel);

        pack(); // Méretezi az ablakot a komponensek alapján
        setLocationRelativeTo(null); // Középre igazítás
        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Fájl Menü
        JMenu fileMenu = new JMenu("Fájl");
        fileMenu.add(new JMenuItem("Új Játék..."));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Állapot Mentése (JSON)..."));
        fileMenu.add(new JMenuItem("Állapot Betöltése (JSON)..."));
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Exportálás PGN..."));
        fileMenu.add(new JMenuItem("Importálás PGN..."));
        // ... (CSV, TXT)
        fileMenu.addSeparator();
        fileMenu.add(new JMenuItem("Kilépés"));

        // Játék Menü
        JMenu gameMenu = new JMenu("Játék");
        gameMenu.add(new JMenuItem("Döntetlen ajánlása"));

        menuBar.add(fileMenu);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);

        // TODO: Eseménykezelők hozzáadása a menüpontokhoz
        // (ezt a MenuController végzi)
    }

    /**
     * Helper method to create a JMenuItem and attach the listener.
     */
    private void addMenuItem(JMenu menu, String text, String command, MenuController listener) {
        JMenuItem item = new JMenuItem(text);
        item.setActionCommand(command);
        item.addActionListener(listener);
        menu.add(item);
    }
}