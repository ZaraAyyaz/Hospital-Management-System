package Receptionist;

import Doctor.doctorDataStore;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;

public class ReceptionistPrescriptionPanel {

    private final JFrame owner;

    public ReceptionistPrescriptionPanel(JFrame owner) {
        this.owner = owner;
    }

    public JPanel build() {
        JPanel page = ReceptionistUIHelper.page();

        // Section card
        JPanel sec = new JPanel(new BorderLayout(0, 14));
        sec.setBackground(ReceptionistUIHelper.C_WHITE);
        sec.setBorder(new CompoundBorder(
            new ReceptionistUIHelper.PanelShadowBorder(),
            new EmptyBorder(20, 20, 20, 20)));

        JLabel secTitle = new JLabel("💊  All Prescriptions  (" +
            doctorDataStore.get().getPrescriptionModel().getRowCount() + " total)");
        secTitle.setFont(ReceptionistUIHelper.F_HEAD);
        secTitle.setForeground(ReceptionistUIHelper.C_NAVY);
        sec.add(secTitle, BorderLayout.NORTH);

        JTable table = makeTable();
        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(ReceptionistUIHelper.C_DIVIDER, 1));
        sp.setBackground(ReceptionistUIHelper.C_WHITE);
        sp.getViewport().setBackground(ReceptionistUIHelper.C_WHITE);
        int rowCount = doctorDataStore.get().getPrescriptionModel().getRowCount();
        sp.setPreferredSize(new Dimension(900,
            Math.max(200, rowCount * 38 + 40)));

        sec.add(sp, BorderLayout.CENTER);
        ReceptionistUIHelper.stack(page, sec);
        return page;
    }

    private JTable makeTable() {
        JTable t = new JTable(doctorDataStore.get().getPrescriptionModel()) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        t.setFont(ReceptionistUIHelper.F_BODY);
        t.setForeground(ReceptionistUIHelper.C_DARK);
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(ReceptionistUIHelper.C_WHITE);
        t.setSelectionBackground(ReceptionistUIHelper.C_SKY);
        t.setSelectionForeground(ReceptionistUIHelper.C_NAVY);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(ReceptionistUIHelper.F_BOLD_S);
        h.setBackground(ReceptionistUIHelper.C_BG);
        h.setForeground(ReceptionistUIHelper.C_MID);
        h.setBorder(new MatteBorder(0, 0, 1, 0, ReceptionistUIHelper.C_DIVIDER));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer textR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, sel, foc, r, c);
                if (!sel) comp.setBackground(
                    r % 2 == 0 ? ReceptionistUIHelper.C_WHITE : new Color(249, 252, 255));
                comp.setForeground(ReceptionistUIHelper.C_DARK);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 12, 0, 12));
                return comp;
            }
        };
        for (int c = 0; c < t.getColumnCount(); c++)
            t.getColumnModel().getColumn(c).setCellRenderer(textR);

        int[] widths = {130, 100, 140, 70, 90, 70, 120, 150};
        for (int i = 0; i < Math.min(widths.length, t.getColumnCount()); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        return t;
    }
}
