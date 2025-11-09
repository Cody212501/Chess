package Model.View;

import javax.swing.*;
import java.awt.*;

/**
 * A modal JDialog that collects information for a new game,
 * as specified in the requirements (names, ELO, timer settings).*/
public class NewGameDialog extends JDialog{
    private JTextField whiteNameField;
    private JSpinner whiteEloSpinner;
    private JTextField blackNameField;
    private JSpinner blackEloSpinner;
    private JCheckBox timerCheckBox;
    // TODO: Add spinners for timer settings (e.g., minutes, increment)

    private boolean succeeded = false;

    public NewGameDialog(Frame owner) {
        super(owner, "New Game Settings", true); //'true' for modal just because

        setLayout(new BorderLayout());

        //Main panel with GridBagLayout for flexible form layout
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Padding
        gbc.anchor = GridBagConstraints.WEST;

        //White
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("White Player:"), gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.fill = GridBagConstraints.HORIZONTAL;
        whiteNameField = new JTextField("Player 1", 15);
        formPanel.add(whiteNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("White ELO:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        whiteEloSpinner = new JSpinner(new SpinnerNumberModel(1200, 400, 3000, 100));
        formPanel.add(whiteEloSpinner, gbc);

        //Black
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Black Player:"), gbc);

        gbc.gridx = 1; gbc.gridy = 2; gbc.fill = GridBagConstraints.HORIZONTAL;
        blackNameField = new JTextField("Player 2", 15);
        formPanel.add(blackNameField, gbc);

        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Black ELO:"), gbc);

        gbc.gridx = 1; gbc.gridy = 3; gbc.fill = GridBagConstraints.HORIZONTAL;
        blackEloSpinner = new JSpinner(new SpinnerNumberModel(1200, 400, 3000, 100));
        formPanel.add(blackEloSpinner, gbc);

        // --- Timer Settings ---
        gbc.gridx = 0; gbc.gridy = 4;
        timerCheckBox = new JCheckBox("Use Timer");
        formPanel.add(timerCheckBox, gbc);
        // TODO: Add listener to enable/disable timer fields

        // --- OK / Cancel Buttons ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> onOK());
        cancelButton.addActionListener(e -> onCancel());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void onOK() {
        // TODO: Add validation (e.g., names cannot be empty)
        this.succeeded = true;
        setVisible(false);
    }

    private void onCancel() {
        this.succeeded = false;
        setVisible(false);
    }

    public boolean isSucceeded(){
        return succeeded;

    }
    public String getWhiteName(){
        return whiteNameField.getText();
    }
    public String getBlackName() {
        return blackNameField.getText();
    }

    public int getWhiteElo() {
        return (Integer) whiteEloSpinner.getValue();
    }

    public int getBlackElo() {
        return (Integer) blackEloSpinner.getValue();
    }

    public boolean isTimerEnabled(){
        return timerCheckBox.isSelected();
    }
}