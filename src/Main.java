import Model.View.*;

import javax.swing.*;

/**
 * The main entry point for the Chess Application.
 *
 * Its sole responsibility is to instantiate and run the main
 * application window (MainFrame) on the Event Dispatch Thread (EDT). */

public class Main{
    public static void main(String[] args){
        /**
         * All Swing applications should be started on the
         * Event Dispatch Thread (EDT) to ensure thread safety.
         * We use SwingUtilities.invokeLater() to achieve this.
         */
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                // This creates the main window, which in turn
                // creates the controllers and starts the game logic.
                new MainFrame();
            }
        });
    }
}