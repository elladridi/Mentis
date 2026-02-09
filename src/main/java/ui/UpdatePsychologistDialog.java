package ui;
import ui.PsychologistTablePanel;

import javax.swing.*;

public class UpdatePsychologistDialog extends UpdatePatientDialog {

    public UpdatePsychologistDialog(JFrame parent, PsychologistTablePanel panel,
                                    int id, String firstName, String lastName,
                                    String phone, String dob, String email) {

        super(parent, null, id, firstName, lastName, phone, dob, email);
        setTitle("Update Psychologist");
    }
}
