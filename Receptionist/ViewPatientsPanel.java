package Receptionist;

import Doctor.doctorDataStore;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ViewPatientsPanel {

    private final JFrame owner;

    public ViewPatientsPanel(JFrame owner) {
        this.owner = owner;
    }

    public JPanel build() {
        JPanel p = ReceptionistUIHelper.page();

        DefaultTableModel model = doctorDataStore.get().getOverallPatientModel();

        int total = model.getRowCount();

        JPanel stats = new JPanel(new GridLayout(1, 3, 16, 0)); stats.setOpaque(false);
        stats.add(ReceptionistUIHelper.statCard("👤", "Total Patients",
            String.valueOf(total), "registered in system", ReceptionistUIHelper.C_BLUE));
        stats.add(ReceptionistUIHelper.statCard("🩺", "With Doctor",
            String.valueOf(doctorDataStore.get().getAppointmentModel().getRowCount()),
            "currently assigned", ReceptionistUIHelper.C_GREEN));
        stats.add(ReceptionistUIHelper.statCard("🚑", "Emergency",
            String.valueOf(doctorDataStore.get().getEmergencyCount()),
            "in emergency queue", ReceptionistUIHelper.C_RED));

        JPanel sec = ReceptionistUIHelper.sectionCard(
            "📋  All Patients  (" + total + " registered)");

        JTable t = buildPatientTable(model);
        sec.add(ReceptionistUIHelper.tableScroll(t), BorderLayout.CENTER);

        ReceptionistUIHelper.stack(p,
            stats, ReceptionistUIHelper.vgap(20),
            sec);
        return p;
    }

    private JTable buildPatientTable(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setFont(ReceptionistUIHelper.F_BODY);
        t.setForeground(ReceptionistUIHelper.C_DARK);
        t.setRowHeight(40);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(ReceptionistUIHelper.C_WHITE);
        t.setSelectionBackground(ReceptionistUIHelper.C_SKY);
        t.setSelectionForeground(ReceptionistUIHelper.C_NAVY);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = t.getTableHeader();
        h.setFont(ReceptionistUIHelper.F_BOLD_S);
        h.setBackground(ReceptionistUIHelper.C_BG);
        h.setForeground(ReceptionistUIHelper.C_MID);
        h.setBorder(new MatteBorder(0, 0, 1, 0, ReceptionistUIHelper.C_DIVIDER));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));

        int[] colWidths = {160, 60, 80, 130, 80, 60};
        for (int i = 0; i < colWidths.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            t.getColumnModel().getColumn(i).setMinWidth(colWidths[i] - 20);
        }

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, sel, foc, r, c);
                if (!sel) comp.setBackground(r % 2 == 0
                    ? ReceptionistUIHelper.C_WHITE : new Color(249, 252, 255));
                comp.setForeground(ReceptionistUIHelper.C_DARK);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 14, 0, 14));
                return comp;
            }
        });
        return t;
    }
}
