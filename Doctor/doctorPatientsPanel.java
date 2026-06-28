package Doctor;

import javax.swing.*;
import java.awt.*;

public class doctorPatientsPanel extends JPanel {

    public doctorPatientsPanel() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("Patients");
        title.setFont(new Font("Arial", Font.BOLD, 20));

        String[] cols = {"ID", "Name", "Age", "Condition"};
        String[][] data = {
                {"1", "Ali", "45", "Flu"},
                {"2", "Sara", "30", "Diabetes"},
                {"3", "John", "60", "Heart Issue"}
        };

        JTable table = new JTable(data, cols);

        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }
}

















