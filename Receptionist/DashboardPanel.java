package Receptionist;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Date;

/**
 * DashboardPanel
 * Shows summary stat cards, quick-action tiles, and the appointments table.
 */
public class DashboardPanel {

    /** Callback so tiles can navigate to other sections. */
    public interface NavigationListener {
        void navigateTo(String section);
    }

    private final NavigationListener nav;
    private final JFrame owner;

    public DashboardPanel(NavigationListener nav, JFrame owner) {
        this.nav   = nav;
        this.owner = owner;
    }

    public JPanel build() {
        JPanel p = ReceptionistUIHelper.page();
        String today = ReceptionistDataStore.FMT_DISPLAY.format(new Date());

        // Count non-cancelled appointments for today
        int bookedToday = 0;
        for (int r = 0; r < ReceptionistDataStore.apptModel.getRowCount(); r++) {
            if (ReceptionistDataStore.normaliseDate(
                    ReceptionistDataStore.apptModel.getValueAt(r, 2).toString()).equals(today)
                && !ReceptionistDataStore.apptModel.getValueAt(r, 6).toString()
                    .equalsIgnoreCase("Cancelled"))
                bookedToday++;
        }

        // Stat cards row
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0)); stats.setOpaque(false);
        stats.add(ReceptionistUIHelper.statCard("👤", "Total Patients",
            String.valueOf(ReceptionistDataStore.apptModel.getRowCount()),
            "registered in system", ReceptionistUIHelper.C_BLUE));
        stats.add(ReceptionistUIHelper.statCard("📅", "Appointments",
            String.valueOf(ReceptionistDataStore.apptModel.getRowCount()),
            "all time records", ReceptionistUIHelper.C_NAVY));
        stats.add(ReceptionistUIHelper.statCard("✅", "Walk-in Queue",
            String.valueOf(ReceptionistDataStore.walkModel.getRowCount()),
            "currently waiting", ReceptionistUIHelper.C_GREEN));
        stats.add(ReceptionistUIHelper.statCard("🕐", "Booked Today",
            String.valueOf(bookedToday),
            "appointments today", ReceptionistUIHelper.C_AMBER));

        // Quick action tiles
        JPanel qa = new JPanel(new GridLayout(1, 3, 14, 0)); qa.setOpaque(false);
        qa.add(quickTile("📋", "Register\nPatient",   "Register Patient",  "Add a new patient to the system"));
        qa.add(quickTile("👨‍⚕️", "Assign\nDoctor",    "Assign Doctor",     "Schedule a doctor appointment"));
        qa.add(quickTile("🚶", "Walk-in\nAdmission",  "Walk-in Admission", "Admit a walk-in patient"));

        // Appointments table section
        JPanel sec = ReceptionistUIHelper.sectionCard(
            "📅  All Appointments  (" + ReceptionistDataStore.apptModel.getRowCount() + " total)");

        // Use AppointmentsPanel.makeTable so the Manage button works exactly as in Appointments panel
        JTable t = AppointmentsPanel.makeTable(owner);
        sec.add(ReceptionistUIHelper.tableScroll(t), BorderLayout.CENTER);

        ReceptionistUIHelper.stack(p,
            stats,
            ReceptionistUIHelper.vgap(22),
            ReceptionistUIHelper.lbl("Quick Actions", ReceptionistUIHelper.F_HEAD, ReceptionistUIHelper.C_MID),
            ReceptionistUIHelper.vgap(8),
            qa,
            ReceptionistUIHelper.vgap(24),
            sec);
        return p;
    }

    private JPanel quickTile(String icon, String text, String target, String subtext) {
        String[] lines = text.split("\n", 2);
        String line1 = lines.length > 0 ? lines[0] : text;
        String line2 = lines.length > 1 ? lines[1] : "";
        JPanel tile = new JPanel(new BorderLayout(0, 8)) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { nav.navigateTo(target); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? ReceptionistUIHelper.C_SKY : ReceptionistUIHelper.C_WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                if (hover) {
                    g2.setColor(ReceptionistUIHelper.C_BORDER);
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,12,12);
                }
                g2.dispose();
            }
        };
        tile.setBorder(new javax.swing.border.CompoundBorder(
            new ReceptionistUIHelper.PanelShadowBorder(),
            new javax.swing.border.EmptyBorder(18,14,18,14)));
        tile.setOpaque(false); tile.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ico = new JLabel(icon, SwingConstants.CENTER);
        ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        JLabel lb = new JLabel(
            "<html><center><b>" + line1 + " " + line2 + "</b><br>" +
            "<font color='#94a3b8'>" + subtext + "</font></center></html>",
            SwingConstants.CENTER);
        lb.setFont(ReceptionistUIHelper.F_SMALL);

        tile.add(ico, BorderLayout.CENTER);
        tile.add(lb,  BorderLayout.SOUTH);
        return tile;
    }
}