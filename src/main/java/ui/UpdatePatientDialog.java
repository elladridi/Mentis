package ui;
import ui.AddPsychologistDialog;
import ui.PatientTablePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import services.userservice;
import models.user;

public class UpdatePatientDialog extends AddPsychologistDialog {

    private int userId;
    private PatientTablePanel parentPanel;

    public UpdatePatientDialog(JFrame parent, PatientTablePanel parentPanel,
                               int id, String firstName, String lastName,
                               String phone, String dob, String email) {

        super(parent, null);
        setTitle("Update Patient");
        this.userId = id;
        this.parentPanel = parentPanel;

        firstNameField.setText(firstName);
        lastNameField.setText(lastName);
        dobField.setText(dob);
        emailField.setText(email);
        phoneField.setText(phone);

        addButton.setText("Update");
        addButton.addActionListener(e -> handleUpdate());
    }

    private void handleUpdate() {
        user u = userservice.getuserById(userId);
        if (u == null) return;

        u.setFirstName(firstNameField.getText());
        u.setLastName(lastNameField.getText());
        u.setPhone(phoneField.getText());
        u.setDateofbirth(dobField.getText());
        u.setEmail(emailField.getText());

        if (userservice.updateuser(u)) {
            JOptionPane.showMessageDialog(this, "Patient updated successfully!");
            parentPanel.refreshTable();
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed.");
        }
    }
}
