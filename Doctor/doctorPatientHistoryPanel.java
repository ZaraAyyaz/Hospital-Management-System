package Doctor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

/**
 * PatientHistoryPanel — Appointments, Emergency, Overall Patients, Prescriptions.
 * Each tab now shows the full medical-history columns from DataStore.
 * Rows are selectable and can be viewed / edited via a side panel.
 */
public class doctorPatientHistoryPanel extends JPanel {

    private static final Color HEADER_BLUE = new Color(30,  55, 105);
    private static final Color CONTENT_BG  = new Color(245, 247, 250);
    private static final Color CARD_WHITE  = Color.WHITE;
    private static final Color ACCENT      = new Color(37,  99, 235);
    private static final Color GREEN       = new Color(16, 185, 129);
    private static final Color ORANGE      = new Color(245,158,  11);
    private static final Color TEXT_DARK   = new Color(30,  41,  59);
    private static final Color TEXT_MID    = new Color(100, 116, 139);
    private static final Color DIVIDER     = new Color(226, 232, 240);

    // Detail side-panel labels (populated on row selection)
    private JPanel detailPanel;
    private JLabel dpName, dpAge, dpContact, dpType, dpDate, dpTime;
    private JLabel dpDiagnosis, dpMedications, dpAllergies, dpNotes;
    private JLabel dpTreatment, dpBP, dpOutcome;
    private String currentDetailMode = "appt"; // "appt" | "emg" | "overall"

    // Track the currently displayed patient/row so detail can refresh after edits
    private String currentDetailPatient = null;
    private int    currentDetailRow     = -1;

    public doctorPatientHistoryPanel() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);

        add(createHeader(), BorderLayout.NORTH);

        // Main split: tabs on left, detail panel on right
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildTabPane(), buildDetailPanel());
        split.setDividerLocation(680);
        split.setDividerSize(5);
        split.setBorder(null);
        split.setBackground(CONTENT_BG);
        add(split, BorderLayout.CENTER);
    }

    // ── Header ────────────────────────────────────────────────────────────
    private JPanel createHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(HEADER_BLUE);
        p.setPreferredSize(new Dimension(0, 70));
        p.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        JLabel l = new JLabel("PATIENT HISTORY & RECORDS");
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 22));
        p.add(l, BorderLayout.WEST);
        JLabel hint = new JLabel("Select a row to view details >");
        hint.setForeground(new Color(180, 205, 240));
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        p.add(hint, BorderLayout.EAST);
        return p;
    }

    // ── Tabbed pane ───────────────────────────────────────────────────────
    private JTabbedPane buildTabPane() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        tabs.addTab("Appointments",       buildApptTab());
        tabs.addTab("Emergency",          buildEmgTab());
        tabs.addTab("Overall Patients",   buildOverallTab());
        tabs.addTab("Prescriptions",      buildRxTab());
        tabs.addTab("Admitted Patients",  buildAdmittedTab());

        tabs.addChangeListener(e -> {
            currentDetailMode = switch (tabs.getSelectedIndex()) {
                case 1  -> "emg";
                case 2  -> "overall";
                case 3  -> "rx";
                case 4  -> "admitted";
                default -> "appt";
            };
            clearDetail();
        });
        return tabs;
    }

    // ── Appointment tab ───────────────────────────────────────────────────
    private JPanel buildApptTab() {
        JTable t = styledTable(doctorDataStore.get().getHistoryAppointmentModel());
        // Selection → detail panel
        t.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = t.getSelectedRow();
            if (row < 0) return;
            DefaultTableModel m = doctorDataStore.get().getHistoryAppointmentModel();
            showApptDetail(m, row);
        });
        return wrap(t);
    }

    // ── Emergency tab ─────────────────────────────────────────────────────
    private JPanel buildEmgTab() {
        JTable t = styledTable(doctorDataStore.get().getHistoryEmergencyModel());
        // colour-code by priority (col 3)
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                String pri = str(tb.getValueAt(row, 3));
                Color bg = sel ? new Color(219, 234, 254) :
                        "P1".equals(pri) ? new Color(255, 220, 220) :
                        "P2".equals(pri) ? new Color(255, 250, 205) :
                                           new Color(220, 240, 220);
                setBackground(bg);
                setForeground("P1".equals(pri) ? new Color(102,0,0) :
                              "P2".equals(pri) ? new Color(153,102,0) :
                                                 TEXT_DARK);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
        t.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = t.getSelectedRow();
            if (row < 0) return;
            showEmgDetail(doctorDataStore.get().getHistoryEmergencyModel(), row);
        });
        return wrap(t);
    }

    // ── Overall Patients tab ──────────────────────────────────────────────
    private JPanel buildOverallTab() {
        JTable t = styledTable(doctorDataStore.get().getOverallPatientModel());
        t.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = t.getSelectedRow();
            if (row < 0) return;
            showOverallDetail(doctorDataStore.get().getOverallPatientModel(), row);
        });
        return wrap(t);
    }

    // ── Prescriptions tab ─────────────────────────────────────────────────
    private JPanel buildRxTab() {
        JTable t = styledTable(doctorDataStore.get().getPrescriptionModel());
        doctorDataStore.get().addListener(() -> t.repaint());
        t.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = t.getSelectedRow();
            if (row < 0) return;
            showRxDetail(doctorDataStore.get().getPrescriptionModel(), row);
        });
        return wrap(t);
    }

    // ── Admitted Patients tab ─────────────────────────────────────────────
    private JPanel buildAdmittedTab() {
        JTable t = styledTable(doctorDataStore.get().getAdmittedPatientModel());

        // Highlight admitted rows in a soft green tint
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tb, Object v,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                setBackground(sel ? new Color(187, 247, 208) : new Color(240, 253, 244));
                setForeground(TEXT_DARK);
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });

        // Row selection → detail panel
        t.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = t.getSelectedRow();
            if (row < 0) return;
            showAdmittedDetail(doctorDataStore.get().getAdmittedPatientModel(), row);
        });

        // Auto-refresh when DataStore changes (admit/discharge events)
        doctorDataStore.get().addListener(() -> {
            t.repaint();
            t.revalidate();
        });

        // Wrap table with a status bar showing count
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CONTENT_BG);
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(Color.WHITE);
        wrapper.add(sp, BorderLayout.CENTER);

        JLabel statusBar = new JLabel("  0 patients currently admitted");
        statusBar.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusBar.setForeground(TEXT_MID);
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        wrapper.add(statusBar, BorderLayout.SOUTH);

        // Keep status bar count live
        doctorDataStore.get().addListener(() -> {
            int count = doctorDataStore.get().getAdmittedPatientModel().getRowCount();
            statusBar.setText("  " + count + " patient" + (count == 1 ? "" : "s") + " currently admitted");
        });

        return wrapper;
    }

    // ── Detail panel (right side) ─────────────────────────────────────────
    private JPanel buildDetailPanel() {
        detailPanel = new JPanel();
        detailPanel.setLayout(new BoxLayout(detailPanel, BoxLayout.Y_AXIS));
        detailPanel.setBackground(CARD_WHITE);
        detailPanel.setBorder(new EmptyBorder(20, 16, 20, 16));
        detailPanel.setPreferredSize(new Dimension(280, 0));

        // placeholder
        JLabel ph = new JLabel("<html><center>Select a row<br>to view patient details</center></html>");
        ph.setForeground(TEXT_MID);
        ph.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ph.setAlignmentX(CENTER_ALIGNMENT);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.add(ph);
        detailPanel.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(detailPanel);
        sp.setBorder(new MatteBorder(0,1,0,0,DIVIDER));
        sp.getViewport().setBackground(CARD_WHITE);
        // wrap in a plain JPanel so the split pane holds the scroll pane
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(sp, BorderLayout.CENTER);
        return wrapper;
    }

    private void clearDetail() {
        currentDetailPatient = null;
        currentDetailRow     = -1;
        detailPanel.removeAll();
        JLabel ph = new JLabel("<html><center>Select a row<br>to view patient details</center></html>");
        ph.setForeground(TEXT_MID);
        ph.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ph.setAlignmentX(CENTER_ALIGNMENT);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.add(ph);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void showApptDetail(DefaultTableModel m, int row) {
        // cols: Name,Age,Contact,Type,Date,Time,Notes,Diagnosis,Medications,Allergies
        currentDetailPatient = str(m.getValueAt(row, 0));
        currentDetailRow     = row;
        String name = currentDetailPatient;
        detailPanel.removeAll();
        detailPanel.add(sectionTitle("Appointment Record"));
        detailPanel.add(Box.createVerticalStrut(12));
        addDetailRow(detailPanel, "Patient",     name);
        addDetailRow(detailPanel, "Age",         str(m.getValueAt(row,1)));
        addDetailRow(detailPanel, "Contact",     str(m.getValueAt(row,2)));
        addDetailRow(detailPanel, "Type",        str(m.getValueAt(row,3)));
        addDetailRow(detailPanel, "Date",        str(m.getValueAt(row,4)));
        addDetailRow(detailPanel, "Time",        str(m.getValueAt(row,5)));
        addDetailRow(detailPanel, "Notes",       str(m.getValueAt(row,6)));
        detailPanel.add(Box.createVerticalStrut(16));
        detailPanel.add(sectionTitle("Medical History"));
        detailPanel.add(Box.createVerticalStrut(8));
        addDetailRow(detailPanel, "Diagnosis",   str(m.getValueAt(row,7)));
        addDetailRow(detailPanel, "Medications", str(m.getValueAt(row,8)));
        addDetailRow(detailPanel, "Allergies",   str(m.getValueAt(row,9)));
        addMedHistoryFromStore(detailPanel, name);
        detailPanel.add(Box.createVerticalStrut(16));

        // Edit Appointment Record button
        JButton editApptBtn = styledBtn("Edit Appointment Record", ACCENT);
        editApptBtn.addActionListener(e -> openEditAppointmentDialog(name, row));
        detailPanel.add(editApptBtn);
        detailPanel.add(Box.createVerticalStrut(8));

        // Edit Medical History button
        addEditMedHistoryBtn(detailPanel, name);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void showEmgDetail(DefaultTableModel m, int row) {
        // cols: Name,Age,Condition,Priority,Time,Outcome,Treatment,Medications,Allergies,Blood Pressure
        currentDetailPatient = str(m.getValueAt(row, 0));
        currentDetailRow     = row;
        String name = currentDetailPatient;
        detailPanel.removeAll();
        detailPanel.add(sectionTitle("Emergency Record"));
        detailPanel.add(Box.createVerticalStrut(12));
        addDetailRow(detailPanel, "Patient",       name);
        addDetailRow(detailPanel, "Age",           str(m.getValueAt(row,1)));
        addDetailRow(detailPanel, "Condition",     str(m.getValueAt(row,2)));
        addDetailRow(detailPanel, "Priority",      str(m.getValueAt(row,3)));
        addDetailRow(detailPanel, "Time",          str(m.getValueAt(row,4)));
        addDetailRow(detailPanel, "Outcome",       str(m.getValueAt(row,5)));
        addDetailRow(detailPanel, "Treatment",     str(m.getValueAt(row,6)));
        addDetailRow(detailPanel, "Medications",   str(m.getValueAt(row,7)));
        addDetailRow(detailPanel, "Allergies",     str(m.getValueAt(row,8)));
        addDetailRow(detailPanel, "Blood Pressure",str(m.getValueAt(row,9)));
        detailPanel.add(Box.createVerticalStrut(16));
        detailPanel.add(sectionTitle("Full Medical History"));
        detailPanel.add(Box.createVerticalStrut(8));
        addMedHistoryFromStore(detailPanel, name);
        detailPanel.add(Box.createVerticalStrut(16));

        // Edit Emergency Record button
        JButton editEmgBtn = styledBtn("Edit Emergency Record", new Color(178, 34, 34));
        editEmgBtn.addActionListener(e -> openEditEmergencyDialog(name, row));
        detailPanel.add(editEmgBtn);
        detailPanel.add(Box.createVerticalStrut(8));

        // Edit Medical History button
        addEditMedHistoryBtn(detailPanel, name);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void showAdmittedDetail(DefaultTableModel m, int row) {
        // cols: Name, Age, Gender, Contact, Blood Group, Admit Date, Ward, Status
        currentDetailPatient = str(m.getValueAt(row, 0));
        currentDetailRow     = row;
        String name = currentDetailPatient;
        detailPanel.removeAll();
        detailPanel.add(sectionTitle("Admitted Patient"));
        detailPanel.add(Box.createVerticalStrut(12));
        addDetailRow(detailPanel, "Name",       name);
        addDetailRow(detailPanel, "Age",        str(m.getValueAt(row, 1)));
        addDetailRow(detailPanel, "Gender",     str(m.getValueAt(row, 2)));
        addDetailRow(detailPanel, "Contact",    str(m.getValueAt(row, 3)));
        addDetailRow(detailPanel, "Blood Group",str(m.getValueAt(row, 4)));
        addDetailRow(detailPanel, "Admit Date", str(m.getValueAt(row, 5)));
        addDetailRow(detailPanel, "Ward",       str(m.getValueAt(row, 6)));
        addDetailRow(detailPanel, "Status",     str(m.getValueAt(row, 7)));
        detailPanel.add(Box.createVerticalStrut(16));
        detailPanel.add(sectionTitle("Medical History"));
        detailPanel.add(Box.createVerticalStrut(8));
        addMedHistoryFromStore(detailPanel, name);
        detailPanel.add(Box.createVerticalStrut(16));

        JButton editAdmittedBtn = styledBtn("Edit Admitted Record", new Color(14, 116, 144));
        editAdmittedBtn.addActionListener(e -> openEditAdmittedDialog(name, row));
        detailPanel.add(editAdmittedBtn);
        detailPanel.add(Box.createVerticalStrut(8));

        addEditMedHistoryBtn(detailPanel, name);
        detailPanel.add(Box.createVerticalStrut(8));

        // Discharge button inside detail panel
        JButton dischargeBtn = styledBtn("Discharge Patient", new Color(220, 38, 38));
        dischargeBtn.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Discharge " + name + "?", "Confirm Discharge",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                doctorDataStore.get().dischargePatient(name);
                clearDetail();
                JOptionPane.showMessageDialog(this,
                        name + " has been discharged.", "Patient Discharged",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        detailPanel.add(dischargeBtn);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void showOverallDetail(DefaultTableModel m, int row) {
        // cols: Name,Age,Gender,Contact,Blood,Visits
        currentDetailPatient = str(m.getValueAt(row, 0));
        currentDetailRow     = row;
        String name = currentDetailPatient;
        detailPanel.removeAll();
        detailPanel.add(sectionTitle("Patient Overview"));
        detailPanel.add(Box.createVerticalStrut(12));
        addDetailRow(detailPanel, "Name",    name);
        addDetailRow(detailPanel, "Age",     str(m.getValueAt(row,1)));
        addDetailRow(detailPanel, "Gender",  str(m.getValueAt(row,2)));
        addDetailRow(detailPanel, "Contact", str(m.getValueAt(row,3)));
        addDetailRow(detailPanel, "Blood",   str(m.getValueAt(row,4)));
        addDetailRow(detailPanel, "Visits",  str(m.getValueAt(row,5)));
        detailPanel.add(Box.createVerticalStrut(16));
        detailPanel.add(sectionTitle("Medical History"));
        detailPanel.add(Box.createVerticalStrut(8));
        addMedHistoryFromStore(detailPanel, name);
        detailPanel.add(Box.createVerticalStrut(16));

        JButton editOverallBtn = styledBtn("Edit Patient Record", GREEN);
        editOverallBtn.addActionListener(e -> openEditOverallDialog(name, row));
        detailPanel.add(editOverallBtn);
        detailPanel.add(Box.createVerticalStrut(8));

        addEditMedHistoryBtn(detailPanel, name);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    private void showRxDetail(DefaultTableModel m, int row) {
        // cols: Patient(0),Date(1),Medication(2),Dosage(3),Frequency(4),Duration(5),Instructions(6),Doctor Notes(7)
        currentDetailPatient = str(m.getValueAt(row, 0));
        currentDetailRow     = row;
        String name = currentDetailPatient;
        detailPanel.removeAll();
        detailPanel.add(sectionTitle("Prescription Record"));
        detailPanel.add(Box.createVerticalStrut(12));
        addDetailRow(detailPanel, "Patient",      name);
        addDetailRow(detailPanel, "Date",         str(m.getValueAt(row, 1)));
        addDetailRow(detailPanel, "Medication",   str(m.getValueAt(row, 2)));
        addDetailRow(detailPanel, "Dosage",       str(m.getValueAt(row, 3)));
        addDetailRow(detailPanel, "Frequency",    str(m.getValueAt(row, 4)));
        addDetailRow(detailPanel, "Duration",     str(m.getValueAt(row, 5)));
        addDetailRow(detailPanel, "Instructions", str(m.getValueAt(row, 6)));
        addDetailRow(detailPanel, "Doctor Notes", str(m.getValueAt(row, 7)));
        detailPanel.add(Box.createVerticalStrut(16));

        JButton editRxBtn = styledBtn("Edit Prescription", new Color(109, 40, 217));
        editRxBtn.addActionListener(e -> openEditRxDialog(name, row));
        detailPanel.add(editRxBtn);
        detailPanel.add(Box.createVerticalGlue());
        detailPanel.revalidate(); detailPanel.repaint();
    }

    /** Adds rich medical history fields from DataStore for any patient name */
    private void addMedHistoryFromStore(JPanel panel, String name) {
        Map<String, String> mh = doctorDataStore.get().getMedicalHistory(name);
        if (mh.isEmpty()) {
            JLabel none = new JLabel("No additional medical history.");
            none.setFont(new Font("Segoe UI", Font.ITALIC, 12));
            none.setForeground(TEXT_MID);
            panel.add(none);
            return;
        }
        String[][] fields = {
            {"Diagnosis","Diagnosis"}, {"ChronicConditions","Chronic Conditions"},
            {"BP","Blood Pressure"}, {"Height","Height"}, {"Weight","Weight"},
            {"FamilyHistory","Family History"}, {"Surgeries","Surgeries"},
            {"Vaccinations","Vaccinations"}
        };
        for (String[] fd : fields) {
            String val = mh.get(fd[0]);
            if (val != null && !val.isEmpty()) addDetailRow(panel, fd[1], val);
        }
    }

    private void addEditMedHistoryBtn(JPanel panel, String name) {
        JButton btn = new JButton("Edit Medical History");
        btn.setBackground(ORANGE);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(LEFT_ALIGNMENT);
        btn.addActionListener(e -> openEditHistoryDialog(name));
        panel.add(btn);
    }

    private void openEditHistoryDialog(String patient) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Medical History — " + patient, true);
        dlg.setSize(480, 560);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel head = new JLabel("Medical History — " + patient);
        head.setFont(new Font("Segoe UI", Font.BOLD, 17));
        head.setForeground(TEXT_DARK);
        head.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(16));

        Map<String, String> mh = doctorDataStore.get().getMedicalHistory(patient);
        String[][] fields = {
            {"Diagnosis","LAST DIAGNOSIS"}, {"Medications","CURRENT MEDICATIONS"},
            {"Allergies","ALLERGIES"}, {"BP","BLOOD PRESSURE"},
            {"Height","HEIGHT"}, {"Weight","WEIGHT"},
            {"ChronicConditions","CHRONIC CONDITIONS"}, {"FamilyHistory","FAMILY HISTORY"},
            {"Surgeries","PAST SURGERIES"}, {"Vaccinations","VACCINATIONS"},
        };
        Map<String, JTextField> fmap = new LinkedHashMap<>();
        for (String[] fd : fields) {
            JTextField tf = styledField();
            tf.setText(mh.getOrDefault(fd[0], ""));
            addDlgField(content, fd[1], tf);
            fmap.put(fd[0], tf);
        }
        content.add(Box.createVerticalStrut(10));
        JButton save = new JButton("Save Medical History");
        save.setBackground(ACCENT); save.setForeground(Color.WHITE);
        save.setFocusPainted(false);
        save.setFont(new Font("Segoe UI", Font.BOLD, 13));
        save.setBorder(new EmptyBorder(10,20,10,20));
        save.setAlignmentX(LEFT_ALIGNMENT);
        save.addActionListener(e -> {
            Map<String, String> updated = new LinkedHashMap<>();
            for (String[] fd : fields) updated.put(fd[0], fmap.get(fd[0]).getText().trim());
            doctorDataStore.get().setMedicalHistoryAll(patient, updated);
            dlg.dispose();
            // Immediately re-render whichever detail view is showing
            refreshCurrentDetail();
        });
        content.add(save);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ── Refresh detail after in-panel edits ───────────────────────────────
    /**
     * Re-renders whichever detail view is currently showing, so edits appear
     * immediately without the user having to re-select the row.
     */
    private void refreshCurrentDetail() {
        if (currentDetailPatient == null || currentDetailRow < 0) return;
        switch (currentDetailMode) {
            case "emg"      -> showEmgDetail(doctorDataStore.get().getHistoryEmergencyModel(), currentDetailRow);
            case "overall"  -> showOverallDetail(doctorDataStore.get().getOverallPatientModel(), currentDetailRow);
            case "admitted" -> showAdmittedDetail(doctorDataStore.get().getAdmittedPatientModel(), currentDetailRow);
            case "rx"       -> showRxDetail(doctorDataStore.get().getPrescriptionModel(), currentDetailRow);
            default         -> showApptDetail(doctorDataStore.get().getHistoryAppointmentModel(), currentDetailRow);
        }
    }

    // ── Edit Appointment Record dialog ────────────────────────────────────
    /**
     * Lets the doctor edit appointment-specific fields for a row in
     * historyAppointmentModel. Key clinical fields are also synced to
     * the medicalHistory store so they reflect everywhere.
     *
     * historyAppointmentModel cols:
     *   Name(0), Age(1), Contact(2), Type(3), Date(4), Time(5),
     *   Notes(6), Diagnosis(7), Medications(8), Allergies(9), Doctor(10)
     */
    private void openEditAppointmentDialog(String patientName, int historyRow) {
        DefaultTableModel am = doctorDataStore.get().getHistoryAppointmentModel();

        if (historyRow >= am.getRowCount()) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Appointment Record — " + patientName, true);
        dlg.setSize(500, 680);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel head = new JLabel("Appointment Record — " + patientName);
        head.setFont(new Font("Segoe UI", Font.BOLD, 17));
        head.setForeground(TEXT_DARK);
        head.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Changes update the Appointments tab and medical history.");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub.setForeground(TEXT_MID);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(16));

        // Pre-fill all fields from the model row
        JTextField ageF       = styledField(); ageF.setText(str(am.getValueAt(historyRow, 1)));
        JTextField contactF   = styledField(); contactF.setText(str(am.getValueAt(historyRow, 2)));
        JTextField typeF      = styledField(); typeF.setText(str(am.getValueAt(historyRow, 3)));
        JTextField dateF      = styledField(); dateF.setText(str(am.getValueAt(historyRow, 4)));
        JTextField timeF      = styledField(); timeF.setText(str(am.getValueAt(historyRow, 5)));
        JTextField notesF     = styledField(); notesF.setText(str(am.getValueAt(historyRow, 6)));
        JTextField diagnosisF = styledField(); diagnosisF.setText(str(am.getValueAt(historyRow, 7)));
        JTextField medsF      = styledField(); medsF.setText(str(am.getValueAt(historyRow, 8)));
        JTextField allergiesF = styledField(); allergiesF.setText(str(am.getValueAt(historyRow, 9)));

        addDlgField(content, "AGE",              ageF);
        addDlgField(content, "CONTACT",          contactF);
        addDlgField(content, "APPOINTMENT TYPE", typeF);
        addDlgField(content, "DATE",             dateF);
        addDlgField(content, "TIME",             timeF);
        addDlgField(content, "NOTES",            notesF);
        addDlgField(content, "DIAGNOSIS",        diagnosisF);
        addDlgField(content, "MEDICATIONS",      medsF);
        addDlgField(content, "ALLERGIES",        allergiesF);

        JButton saveBtn = styledBtn("Save Appointment Record", ACCENT);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.addActionListener(e -> {
            String newAge       = ageF.getText().trim();
            String newContact   = contactF.getText().trim();
            String newType      = typeF.getText().trim();
            String newDate      = dateF.getText().trim();
            String newTime      = timeF.getText().trim();
            String newNotes     = notesF.getText().trim();
            String newDiagnosis = diagnosisF.getText().trim();
            String newMeds      = medsF.getText().trim();
            String newAllergies = allergiesF.getText().trim();

            // ── 1. Update historyAppointmentModel ───────────────────────
            am.setValueAt(newAge,       historyRow, 1);
            am.setValueAt(newContact,   historyRow, 2);
            am.setValueAt(newType,      historyRow, 3);
            am.setValueAt(newDate,      historyRow, 4);
            am.setValueAt(newTime,      historyRow, 5);
            am.setValueAt(newNotes,     historyRow, 6);
            am.setValueAt(newDiagnosis, historyRow, 7);
            am.setValueAt(newMeds,      historyRow, 8);
            am.setValueAt(newAllergies, historyRow, 9);

            // ── 2. Sync into medicalHistory store and overallPatientModel
            doctorDataStore ds = doctorDataStore.get();
            ds.updateMedicalHistory(patientName, "Diagnosis",   newDiagnosis);
            ds.updateMedicalHistory(patientName, "Medications", newMeds);
            ds.updateMedicalHistory(patientName, "Allergies",   newAllergies);

            // Also update age/contact in the overall patient model
            DefaultTableModel om = ds.getOverallPatientModel();
            for (int i = 0; i < om.getRowCount(); i++) {
                if (patientName.equals(str(om.getValueAt(i, 0)))) {
                    if (!newAge.isEmpty())     om.setValueAt(newAge,     i, 1);
                    if (!newContact.isEmpty()) om.setValueAt(newContact, i, 3);
                    break;
                }
            }

            ds.notifyListeners();
            dlg.dispose();

            // Refresh the detail panel in-place
            showApptDetail(am, historyRow);
        });
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ── Edit Emergency Record dialog ──────────────────────────────────────
    /**
     * Lets the doctor edit all emergency-specific fields for a row in
     * historyEmergencyModel. Changes are also propagated to the live
     * emergencyModel (the queue panel) so both stay in sync.
     *
     * historyEmergencyModel cols: Name(0),Age(1),Condition(2),Priority(3),Time(4),
     *                             Outcome(5),Treatment(6),Medications(7),Allergies(8),BP(9)
     * emergencyModel cols:        Priority(0),Name(1),Age(2),Condition(3),Time(4),Ward(5),Status(6)
     */
    private void openEditEmergencyDialog(String patientName, int historyRow) {
        DefaultTableModel hm = doctorDataStore.get().getHistoryEmergencyModel();

        // Guard: row may have been removed since the panel was built
        if (historyRow >= hm.getRowCount()) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Emergency Record — " + patientName, true);
        dlg.setSize(500, 620);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel head = new JLabel("Emergency Record — " + patientName);
        head.setFont(new Font("Segoe UI", Font.BOLD, 17));
        head.setForeground(TEXT_DARK);
        head.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(4));

        JLabel sub = new JLabel("Changes update both History and the live Emergency Queue.");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub.setForeground(TEXT_MID);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(16));

        // Fields bound to historyEmergencyModel columns
        JTextField conditionF  = styledField(); conditionF.setText(str(hm.getValueAt(historyRow, 2)));
        JTextField outcomeF    = styledField(); outcomeF.setText(str(hm.getValueAt(historyRow, 5)));
        JTextField treatmentF  = styledField(); treatmentF.setText(str(hm.getValueAt(historyRow, 6)));
        JTextField medsF       = styledField(); medsF.setText(str(hm.getValueAt(historyRow, 7)));
        JTextField allergiesF  = styledField(); allergiesF.setText(str(hm.getValueAt(historyRow, 8)));
        JTextField bpF         = styledField(); bpF.setText(str(hm.getValueAt(historyRow, 9)));

        String[] priorities = {"P1", "P2", "P3"};
        JComboBox<String> priorityCB = new JComboBox<>(priorities);
        priorityCB.setSelectedItem(str(hm.getValueAt(historyRow, 3)));
        priorityCB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        priorityCB.setAlignmentX(LEFT_ALIGNMENT);
        priorityCB.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        addDlgField(content, "CONDITION",   conditionF);
        addDlgField(content, "OUTCOME",     outcomeF);
        addDlgField(content, "TREATMENT",   treatmentF);
        addDlgField(content, "MEDICATIONS", medsF);
        addDlgField(content, "ALLERGIES",   allergiesF);
        addDlgField(content, "BLOOD PRESSURE", bpF);

        JLabel priLabel = new JLabel("PRIORITY");
        priLabel.setFont(new Font("Segoe UI", Font.BOLD, 10));
        priLabel.setForeground(TEXT_MID);
        priLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(priLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(priorityCB);
        content.add(Box.createVerticalStrut(16));

        JButton saveBtn = styledBtn("Save Emergency Record", new Color(178, 34, 34));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.addActionListener(e -> {
            String newCondition  = conditionF.getText().trim();
            String newOutcome    = outcomeF.getText().trim();
            String newTreatment  = treatmentF.getText().trim();
            String newMeds       = medsF.getText().trim();
            String newAllergies  = allergiesF.getText().trim();
            String newBP         = bpF.getText().trim();
            String newPriority   = (String) priorityCB.getSelectedItem();
            String newStatus     = "P1".equals(newPriority) ? "CRITICAL"
                                 : "P2".equals(newPriority) ? "HIGH PRIORITY" : "MODERATE";

            // ── 1. Update historyEmergencyModel ─────────────────────────
            hm.setValueAt(newCondition,  historyRow, 2);
            hm.setValueAt(newPriority,   historyRow, 3);
            hm.setValueAt(newOutcome,    historyRow, 5);
            hm.setValueAt(newTreatment,  historyRow, 6);
            hm.setValueAt(newMeds,       historyRow, 7);
            hm.setValueAt(newAllergies,  historyRow, 8);
            hm.setValueAt(newBP,         historyRow, 9);

            // ── 2. Update live emergencyModel (col 1 = Patient Name) ────
            DefaultTableModel em = doctorDataStore.get().getEmergencyModel();
            for (int i = 0; i < em.getRowCount(); i++) {
                if (patientName.equals(str(em.getValueAt(i, 1)))) {
                    em.setValueAt(newPriority,  i, 0);
                    em.setValueAt(newCondition, i, 3);
                    em.setValueAt(newStatus,    i, 6);
                    break;
                }
            }

            // ── 3. Also persist key fields into medicalHistory store ────
            doctorDataStore ds = doctorDataStore.get();
            ds.updateMedicalHistory(patientName, "Diagnosis",   newCondition);
            ds.updateMedicalHistory(patientName, "Medications", newMeds);
            ds.updateMedicalHistory(patientName, "Allergies",   newAllergies);
            ds.updateMedicalHistory(patientName, "BP",          newBP);
            ds.updateMedicalHistory(patientName, "Treatment",   newTreatment);

            ds.notifyListeners();
            dlg.dispose();

            // Refresh the detail panel in-place
            showEmgDetail(hm, historyRow);
        });
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ── Edit Overall Patient dialog ───────────────────────────────────────
    /**
     * overallPatientModel cols: Name(0), Age(1), Gender(2), Contact(3), Blood(4), Visits(5)
     */
    private void openEditOverallDialog(String patientName, int row) {
        DefaultTableModel om = doctorDataStore.get().getOverallPatientModel();
        if (row >= om.getRowCount()) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Patient Record — " + patientName, true);
        dlg.setSize(500, 480);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel head = new JLabel("Patient Record — " + patientName);
        head.setFont(new Font("Segoe UI", Font.BOLD, 17));
        head.setForeground(TEXT_DARK);
        head.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(4));
        JLabel sub = new JLabel("Changes update Overall Patients and sync to other tabs.");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub.setForeground(TEXT_MID);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(16));

        JTextField ageF     = styledField(); ageF.setText(str(om.getValueAt(row, 1)));
        JTextField genderF  = styledField(); genderF.setText(str(om.getValueAt(row, 2)));
        JTextField contactF = styledField(); contactF.setText(str(om.getValueAt(row, 3)));
        JTextField bloodF   = styledField(); bloodF.setText(str(om.getValueAt(row, 4)));
        JTextField visitsF  = styledField(); visitsF.setText(str(om.getValueAt(row, 5)));

        addDlgField(content, "AGE",         ageF);
        addDlgField(content, "GENDER",      genderF);
        addDlgField(content, "CONTACT",     contactF);
        addDlgField(content, "BLOOD GROUP", bloodF);
        addDlgField(content, "VISITS",      visitsF);

        JButton saveBtn = styledBtn("Save Patient Record", GREEN);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.addActionListener(e -> {
            om.setValueAt(ageF.getText().trim(),     row, 1);
            om.setValueAt(genderF.getText().trim(),  row, 2);
            om.setValueAt(contactF.getText().trim(), row, 3);
            om.setValueAt(bloodF.getText().trim(),   row, 4);
            om.setValueAt(visitsF.getText().trim(),  row, 5);
            doctorDataStore.get().notifyListeners();
            dlg.dispose();
            showOverallDetail(om, row);
        });
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ── Edit Admitted Patient dialog ──────────────────────────────────────
    /**
     * admittedPatientModel cols: Name(0),Age(1),Gender(2),Contact(3),
     *                            Blood Group(4),Admit Date(5),Ward(6),Status(7)
     */
    private void openEditAdmittedDialog(String patientName, int row) {
        DefaultTableModel am = doctorDataStore.get().getAdmittedPatientModel();
        if (row >= am.getRowCount()) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Admitted Record — " + patientName, true);
        dlg.setSize(500, 560);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel head = new JLabel("Admitted Record — " + patientName);
        head.setFont(new Font("Segoe UI", Font.BOLD, 17));
        head.setForeground(TEXT_DARK);
        head.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(4));
        JLabel sub = new JLabel("Changes update the Admitted Patients tab.");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub.setForeground(TEXT_MID);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(16));

        JTextField ageF       = styledField(); ageF.setText(str(am.getValueAt(row, 1)));
        JTextField genderF    = styledField(); genderF.setText(str(am.getValueAt(row, 2)));
        JTextField contactF   = styledField(); contactF.setText(str(am.getValueAt(row, 3)));
        JTextField bloodF     = styledField(); bloodF.setText(str(am.getValueAt(row, 4)));
        JTextField admitDateF = styledField(); admitDateF.setText(str(am.getValueAt(row, 5)));
        JTextField wardF      = styledField(); wardF.setText(str(am.getValueAt(row, 6)));
        JTextField statusF    = styledField(); statusF.setText(str(am.getValueAt(row, 7)));

        addDlgField(content, "AGE",        ageF);
        addDlgField(content, "GENDER",     genderF);
        addDlgField(content, "CONTACT",    contactF);
        addDlgField(content, "BLOOD GROUP",bloodF);
        addDlgField(content, "ADMIT DATE", admitDateF);
        addDlgField(content, "WARD",       wardF);
        addDlgField(content, "STATUS",     statusF);

        JButton saveBtn = styledBtn("Save Admitted Record", new Color(14, 116, 144));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.addActionListener(e -> {
            am.setValueAt(ageF.getText().trim(),       row, 1);
            am.setValueAt(genderF.getText().trim(),    row, 2);
            am.setValueAt(contactF.getText().trim(),   row, 3);
            am.setValueAt(bloodF.getText().trim(),     row, 4);
            am.setValueAt(admitDateF.getText().trim(), row, 5);
            am.setValueAt(wardF.getText().trim(),      row, 6);
            am.setValueAt(statusF.getText().trim(),    row, 7);

            // Also sync age/contact back to overallPatientModel
            DefaultTableModel om = doctorDataStore.get().getOverallPatientModel();
            for (int i = 0; i < om.getRowCount(); i++) {
                if (patientName.equals(str(om.getValueAt(i, 0)))) {
                    if (!ageF.getText().trim().isEmpty())     om.setValueAt(ageF.getText().trim(),     i, 1);
                    if (!genderF.getText().trim().isEmpty())  om.setValueAt(genderF.getText().trim(),  i, 2);
                    if (!contactF.getText().trim().isEmpty()) om.setValueAt(contactF.getText().trim(), i, 3);
                    if (!bloodF.getText().trim().isEmpty())   om.setValueAt(bloodF.getText().trim(),   i, 4);
                    break;
                }
            }

            doctorDataStore.get().notifyListeners();
            dlg.dispose();
            showAdmittedDetail(am, row);
        });
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ── Edit Prescription dialog ──────────────────────────────────────────
    /**
     * prescriptionModel cols: Patient(0),Date(1),Medication(2),Dosage(3),
     *                         Frequency(4),Duration(5),Instructions(6),Doctor Notes(7)
     */
    private void openEditRxDialog(String patientName, int row) {
        DefaultTableModel pm = doctorDataStore.get().getPrescriptionModel();
        if (row >= pm.getRowCount()) return;

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Prescription — " + patientName, true);
        dlg.setSize(500, 580);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel head = new JLabel("Prescription — " + patientName);
        head.setFont(new Font("Segoe UI", Font.BOLD, 17));
        head.setForeground(TEXT_DARK);
        head.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(4));
        JLabel sub = new JLabel("Changes update the Prescriptions tab.");
        sub.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sub.setForeground(TEXT_MID);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        content.add(sub);
        content.add(Box.createVerticalStrut(16));

        JTextField dateF         = styledField(); dateF.setText(str(pm.getValueAt(row, 1)));
        JTextField medicationF   = styledField(); medicationF.setText(str(pm.getValueAt(row, 2)));
        JTextField dosageF       = styledField(); dosageF.setText(str(pm.getValueAt(row, 3)));
        JTextField frequencyF    = styledField(); frequencyF.setText(str(pm.getValueAt(row, 4)));
        JTextField durationF     = styledField(); durationF.setText(str(pm.getValueAt(row, 5)));
        JTextField instructionsF = styledField(); instructionsF.setText(str(pm.getValueAt(row, 6)));
        JTextField notesF        = styledField(); notesF.setText(str(pm.getValueAt(row, 7)));

        addDlgField(content, "DATE",          dateF);
        addDlgField(content, "MEDICATION",    medicationF);
        addDlgField(content, "DOSAGE",        dosageF);
        addDlgField(content, "FREQUENCY",     frequencyF);
        addDlgField(content, "DURATION",      durationF);
        addDlgField(content, "INSTRUCTIONS",  instructionsF);
        addDlgField(content, "DOCTOR NOTES",  notesF);

        JButton saveBtn = styledBtn("Save Prescription", new Color(109, 40, 217));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.addActionListener(e -> {
            pm.setValueAt(dateF.getText().trim(),         row, 1);
            pm.setValueAt(medicationF.getText().trim(),   row, 2);
            pm.setValueAt(dosageF.getText().trim(),       row, 3);
            pm.setValueAt(frequencyF.getText().trim(),    row, 4);
            pm.setValueAt(durationF.getText().trim(),     row, 5);
            pm.setValueAt(instructionsF.getText().trim(), row, 6);
            pm.setValueAt(notesF.getText().trim(),        row, 7);
            doctorDataStore.get().notifyListeners();
            dlg.dispose();
            showRxDetail(pm, row);
        });
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ── Shared styled button helper ────────────────────────────────────────
    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 14, 8, 14));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setAlignmentX(LEFT_ALIGNMENT);
        return b;
    }

    // ── UI helpers ─────────────────────────────────────────────────────────
    private JTable styledTable(DefaultTableModel model) {
        JTable t = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        t.setRowHeight(30);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        t.getTableHeader().setBackground(new Color(30, 55, 105));
        t.getTableHeader().setForeground(Color.WHITE);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        return t;
    }

    private JPanel wrap(JTable t) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(CONTENT_BG);
        JScrollPane sp = new JScrollPane(t);
        sp.getViewport().setBackground(CARD_WHITE);
        p.add(sp, BorderLayout.CENTER);
        return p;
    }

    private JLabel sectionTitle(String text) {
        JPanel wrapper = new JPanel(new BorderLayout(0,4));
        wrapper.setOpaque(false);
        wrapper.setAlignmentX(LEFT_ALIGNMENT);
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 13));
        l.setForeground(ACCENT);
        wrapper.add(l, BorderLayout.NORTH);
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(200, 220, 255));
        wrapper.add(sep, BorderLayout.SOUTH);
        return l;
    }

    private void addDetailRow(JPanel p, String labelText, String value) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        JLabel lbl = new JLabel(labelText + ":");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MID);
        lbl.setPreferredSize(new Dimension(110, 0));
        JLabel val = new JLabel(value == null || value.isEmpty() ? "—" : value);
        val.setFont(new Font("Segoe UI", Font.BOLD, 12));
        val.setForeground(TEXT_DARK);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        p.add(row);
        p.add(Box.createVerticalStrut(6));
    }

    private JTextField styledField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        f.setAlignmentX(LEFT_ALIGNMENT);
        return f;
    }

    private void addDlgField(JPanel p, String labelText, JTextField f) {
        JLabel l = new JLabel(labelText);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setForeground(TEXT_MID);
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}