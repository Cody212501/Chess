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
    private JSpinner minutesSpinner;
    private JSpinner incrementSpinner;

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

        //Timer Settings
        gbc.gridx = 0; gbc.gridy = 4;
        timerCheckBox = new JCheckBox("Időzítő használata");
        formPanel.add(timerCheckBox, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 180, 1));
        incrementSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 60, 1));
        timerPanel.add(new JLabel("Perc:"));
        timerPanel.add(minutesSpinner);
        timerPanel.add(new JLabel("Incr (mp):"));
        timerPanel.add(incrementSpinner);
        formPanel.add(timerPanel, gbc);

        // Disable timer fields by default
        enableTimerFields(false);

        // Add listener to checkbox
        timerCheckBox.addActionListener(e -> enableTimerFields(timerCheckBox.isSelected()));

        //OK / Cancel Buttons
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

    private void enableTimerFields(boolean enable) {
        minutesSpinner.setEnabled(enable);
        incrementSpinner.setEnabled(enable);
    }

    private void onOK() {
        if (whiteNameField.getText().trim().isEmpty() || blackNameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "You must give a name, this field can't be empty!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
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