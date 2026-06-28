package Doctor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class doctorOverviewPanel extends JPanel {

    // ── Colors ──────────────────────────────────────────────────────────
    private final Color BACKGROUND = new Color(245, 247, 252);
    private final Color CARD_BG    = Color.WHITE;
    private final Color BLUE       = new Color(37, 99, 235);
    private final Color GREEN      = new Color(16, 185, 129);
    private final Color ORANGE     = new Color(245, 158, 11);
    private final Color RED        = new Color(239, 68, 68);

    // ── Live stat labels (updated on refresh()) ──────────────────────────
    private JLabel totalAppointmentsVal;
    private JLabel emergencyVal;

    // ── Appointments list inside the card ────────────────────────────────
    private JPanel appointmentListPanel;

    // ── Next Patient live detail labels ───────────────────────────────────
    private JLabel npNameVal, npAgeVal, npGenderVal, npContactVal, npBloodVal, npTimeVal;

    public doctorOverviewPanel(String doctorName) {
        setLayout(new BorderLayout(20, 20));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(25, 25, 25, 25));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel title = new JLabel("Doctor Dashboard");
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        title.setForeground(new Color(30, 41, 59));

        JLabel subtitle = new JLabel("Welcome back, " + doctorName);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 15));
        subtitle.setForeground(Color.GRAY);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(5));
        titlePanel.add(subtitle);
        topPanel.add(titlePanel, BorderLayout.WEST);

        // Stats cards
        JPanel statsPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        statsPanel.setOpaque(false);

        doctorDataStore ds = doctorDataStore.get();

        totalAppointmentsVal = new JLabel(String.valueOf(ds.getBookedSlotCount()));
        emergencyVal         = new JLabel(String.valueOf(ds.getEmergencyCount()));

        statsPanel.add(createCard("Appointments",    totalAppointmentsVal, ORANGE));
        statsPanel.add(createCard("Emergency Active",emergencyVal,         RED));

        // Center section
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 20));
        centerPanel.setOpaque(false);
        centerPanel.add(createAppointmentsCard());
        centerPanel.add(createPatientPanel());

        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setOpaque(false);
        content.add(statsPanel, BorderLayout.NORTH);
        content.add(centerPanel, BorderLayout.CENTER);

        add(topPanel, BorderLayout.NORTH);
        add(content,  BorderLayout.CENTER);

        // Register as a DataStore listener so counts update automatically
        ds.addListener(this::refresh);
    }

    /**
     * Called whenever the Overview tab is shown or data changes.
     * Re-reads all counts and appointment list from DataStore.
     */
    public void refresh() {
        doctorDataStore ds = doctorDataStore.get();
        totalAppointmentsVal.setText(String.valueOf(ds.getBookedSlotCount()));
        emergencyVal.setText(String.valueOf(ds.getEmergencyCount()));

        // Rebuild appointment list — only slot-valid appointments
        appointmentListPanel.removeAll();
        java.util.List<Object[]> slotAppts = ds.getSlotAppointments();
        if (slotAppts.isEmpty()) {
            JLabel none = new JLabel("No appointments scheduled.");
            none.setFont(new Font("SansSerif", Font.ITALIC, 14));
            none.setForeground(Color.GRAY);
            appointmentListPanel.add(none);
        } else {
            int shown = Math.min(slotAppts.size(), 5);
            for (int i = 0; i < shown; i++) {
                Object[] row = slotAppts.get(i);
                JLabel lbl = new JLabel("- " + row[0] + "   " + row[1] + "  (" + row[2] + ")");
                lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
                lbl.setBorder(new EmptyBorder(8, 0, 8, 0));
                appointmentListPanel.add(lbl);
            }
        }
        appointmentListPanel.revalidate();
        appointmentListPanel.repaint();

        // Refresh next patient details panel
        if (npNameVal != null) refreshNextPatient();
    }

    // ── Card with a mutable value label ─────────────────────────────────
    private JPanel createCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 20, 20, 20)));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        titleLbl.setForeground(Color.GRAY);

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        valueLabel.setForeground(color);

        card.add(titleLbl);
        card.add(Box.createVerticalStrut(10));
        card.add(valueLabel);
        return card;
    }

    // ── Live appointments card ────────────────────────────────────────────
    private JPanel createAppointmentsCard() {
        JPanel panel = createSectionPanel("Today's Appointments");

        appointmentListPanel = new JPanel();
        appointmentListPanel.setLayout(new BoxLayout(appointmentListPanel, BoxLayout.Y_AXIS));
        appointmentListPanel.setOpaque(false);

        // Populate initially — only slot-valid appointments
        java.util.List<Object[]> initAppts = doctorDataStore.get().getSlotAppointments();
        int shown = Math.min(initAppts.size(), 5);
        for (int i = 0; i < shown; i++) {
            Object[] row = initAppts.get(i);
            JLabel lbl = new JLabel("- " + row[0] + "   " + row[1] + "  (" + row[2] + ")");
            lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
            lbl.setBorder(new EmptyBorder(8, 0, 8, 0));
            appointmentListPanel.add(lbl);
        }

        panel.add(appointmentListPanel, BorderLayout.CENTER);
        return panel;
    }

    // ── Next patient details — live from DataStore ────────────────────────
    private JPanel createPatientPanel() {
        JPanel panel = createSectionPanel("Next Patient Details");

        npNameVal    = createValueLabel("--");
        npAgeVal     = createValueLabel("--");
        npGenderVal  = createValueLabel("--");
        npContactVal = createValueLabel("--");
        npBloodVal   = createValueLabel("--");
        npTimeVal    = createValueLabel("--");

        JPanel info = new JPanel(new GridLayout(6, 2, 10, 12));
        info.setOpaque(false);
        info.add(createInfoLabel("Patient Name")); info.add(npNameVal);
        info.add(createInfoLabel("Age"));          info.add(npAgeVal);
        info.add(createInfoLabel("Gender"));       info.add(npGenderVal);
        info.add(createInfoLabel("Contact"));      info.add(npContactVal);
        info.add(createInfoLabel("Blood Group"));  info.add(npBloodVal);
        info.add(createInfoLabel("Appt. Time"));   info.add(npTimeVal);

        panel.add(info, BorderLayout.CENTER);

        // Populate immediately
        refreshNextPatient();
        return panel;
    }

    /** Pulls the first appointment from DataStore and fills the detail labels. */
    private void refreshNextPatient() {
        doctorDataStore ds = doctorDataStore.get();
        String name = ds.getNextPatientName();
        String time = ds.getNextPatientTime();
        String[] details = ds.getPatientDetails(name);

        npNameVal.setText(details[0]);
        npAgeVal.setText(details[1]);
        npGenderVal.setText(details[2]);
        npContactVal.setText(details[3]);
        npBloodVal.setText(details[4]);
        npTimeVal.setText(time);
    }

    // ── Helpers ───────────────────────────────────────────────────────────
    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD_BG);
        panel.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 230, 230), 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 18));
        titleLbl.setForeground(new Color(30, 41, 59));
        panel.add(titleLbl, BorderLayout.NORTH);
        return panel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel lbl = new JLabel(text + ":");
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lbl.setForeground(Color.GRAY);
        return lbl;
    }

    private JLabel createValueLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 15));
        lbl.setForeground(new Color(30, 41, 59));
        return lbl;
    }
}