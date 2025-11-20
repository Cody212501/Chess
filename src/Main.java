/**
 * TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
 * click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
*/

import Model.View.*;

import javax.swing.*;

/**
 * The main entry point for the Chess Application.
 *
 * Its sole responsibility is to instantiate and run the main
 * application window (MainFrame) on the Event Dispatch Thread (EDT). */

public class Main{
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.

        /**
         *All Swing applications should be started on the
         *Event Dispatch Thread (EDT) to ensure thread safety.
         *We use SwingUtilities.invokeLater() to achieve this.*/
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // This creates the main window, which in turn
                // creates the controllers and starts the game logic.
                new MainFrame();
            }
        });
    }
}