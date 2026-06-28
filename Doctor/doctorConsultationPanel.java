package Doctor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import System.HospitalSystem;


/**
 * ConsultationPanel — Doctor views current appointment patient's full info,
 * medical history, and can issue prescriptions.
 */
public class doctorConsultationPanel extends JPanel {

    // ── Colors ─────────────────────────────────────────────────────────────
    private static final Color BG         = new Color(245, 247, 252);
    private static final Color CARD_WHITE = Color.WHITE;
    private static final Color NAVY       = new Color(22,  43,  90);
    private static final Color NAVY2      = new Color(40,  75, 145);
    private static final Color ACCENT     = new Color(37,  99, 235);
    private static final Color GREEN      = new Color(16, 185, 129);
    private static final Color ORANGE     = new Color(245,158,  11);
    private static final Color RED        = new Color(220, 38,  38);
    private static final Color TEXT_DARK  = new Color(30,  41,  59);
    private static final Color TEXT_MID   = new Color(100,116, 139);
    private static final Color TEXT_LIGHT = new Color(148,163, 184);
    private static final Color DIVIDER    = new Color(226,232, 240);

    // ── Patient selector & live detail areas ───────────────────────────────
    private JComboBox<String> patientSelector;
    /** Maps display label (e.g. "Ali Khan  [APT]") → raw patient name */
    private final java.util.LinkedHashMap<String, String> selectorMap = new java.util.LinkedHashMap<>();

    private JEditorPane infoPane, vitalsPane, mhPane;
    private JPanel prescriptionHistoryPanel;
    private DefaultTableModel rxTableModel;

    // Action buttons — kept as fields so visibility can be toggled
    private JButton admitBtn;
    private JButton dischargeBtn;
    private JButton emergencyBtn;

    public doctorConsultationPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(BG);

        add(buildHeader(), BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildLeftPanel(), buildRightPanel());
        split.setDividerLocation(420);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setBackground(BG);
        add(split, BorderLayout.CENTER);

        // Register listener so selector refreshes when patients change
        doctorDataStore.get().addListener(this::refreshPatientList);

        // Initial load
        javax.swing.SwingUtilities.invokeLater(() -> {
            refreshPatientList();
            if (patientSelector.getItemCount() > 0) {
                patientSelector.setSelectedIndex(0);
                String sel = (String) patientSelector.getSelectedItem();
                if (sel != null) {
                    loadPatient(sel);
                    updateButtonVisibility(selectorMap.getOrDefault(sel, sel));
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, NAVY, getWidth(), 0, NAVY2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose(); super.paintComponent(g);
            }
        };
        banner.setOpaque(false);
        banner.setPreferredSize(new Dimension(0, 88));
        banner.setBorder(new EmptyBorder(16, 28, 16, 28));

        JLabel title = label("Patient Consultation", Font.BOLD, 26, Color.WHITE);
        JLabel sub   = label("View patient info - medical history - issue prescriptions",
                             Font.PLAIN, 13, new Color(180, 205, 240));
        JPanel col = new JPanel(new GridLayout(2,1,0,3));
        col.setOpaque(false);
        col.add(title); col.add(sub);
        banner.add(col, BorderLayout.WEST);

        // Patient selector in header
        JPanel selPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        selPanel.setOpaque(false);
        selPanel.add(label("Select Patient:", Font.BOLD, 13, new Color(180,205,240)));
        patientSelector = new JComboBox<>();
        patientSelector.setPreferredSize(new Dimension(220, 36));
        patientSelector.setFont(new Font("Segoe UI", Font.BOLD, 13));
        patientSelector.setBackground(new Color(30, 55, 105));
        patientSelector.setForeground(Color.WHITE);
        patientSelector.addActionListener(e -> {
            String sel = (String) patientSelector.getSelectedItem();
            if (sel != null && !sel.isEmpty()) {
                loadPatient(sel);
                String patient = selectorMap.getOrDefault(sel, sel);
                updateButtonVisibility(patient);
            }
        });
        selPanel.add(patientSelector);
        banner.add(selPanel, BorderLayout.EAST);
        return banner;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LEFT — Patient Info + Medical History
    // ═══════════════════════════════════════════════════════════════════════
    private JScrollPane buildLeftPanel() {
        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBackground(BG);
        left.setBorder(new EmptyBorder(16, 16, 16, 8));

        left.add(buildPatientInfoCard());
        left.add(Box.createVerticalStrut(12));
        left.add(buildVitalsCard());
        left.add(Box.createVerticalStrut(12));
        left.add(buildMedicalHistoryCard());

        JScrollPane scroll = new JScrollPane(left);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildPatientInfoCard() {
        JPanel card = card("Patient Information");

        infoPane = new JEditorPane("text/html",
            "<html><body style='font-family:Segoe UI;font-size:13px;color:#1E293B;padding:0;margin:0'>"
            + "<table cellpadding='3' cellspacing='0'>"
            + rowHtml("Full Name", "--") + rowHtml("Age", "--") + rowHtml("Gender", "--")
            + rowHtml("Contact", "--") + rowHtml("Email", "--") + rowHtml("Blood Group", "--")
            + rowHtml("Total Visits", "--") + rowHtml("Source", "--") + rowHtml("Appt. Time", "--")
            + "</table></body></html>");
        infoPane.setEditable(false);
        infoPane.setOpaque(false);
        infoPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        infoPane.setBorder(null);
        card.add(infoPane, BorderLayout.CENTER);

        // Admit / Discharge buttons
        admitBtn     = actionButton("Admit Patient",     GREEN,  Color.WHITE);
        dischargeBtn = actionButton("Discharge Patient", RED,    Color.WHITE);
        emergencyBtn = actionButton("Add to Emergency",  new Color(178, 34, 34), Color.WHITE);

        admitBtn.addActionListener(e -> {
            String displayLabel = (String) patientSelector.getSelectedItem();
            if (displayLabel == null || displayLabel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a patient first.");
                return;
            }
            String patient = selectorMap.getOrDefault(displayLabel, displayLabel);
            if (doctorDataStore.get().isAdmitted(patient)) {
                JOptionPane.showMessageDialog(this,
                        patient + " is already admitted.", "Already Admitted",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            doctorDataStore.get().admitPatient(patient);
            JOptionPane.showMessageDialog(this,
                    patient + " has been admitted successfully.", "Patient Admitted",
                    JOptionPane.INFORMATION_MESSAGE);
            updateButtonVisibility(patient);
        });

        dischargeBtn.addActionListener(e -> {
            String displayLabel = (String) patientSelector.getSelectedItem();
            if (displayLabel == null || displayLabel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a patient first.");
                return;
            }
            String patient = selectorMap.getOrDefault(displayLabel, displayLabel);
            if (!doctorDataStore.get().isAdmitted(patient) && !doctorDataStore.get().isInEmergency(patient)) {
                JOptionPane.showMessageDialog(this,
                        patient + " is not currently admitted or in emergency.", "Not Admitted",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Discharge " + patient + "?", "Confirm Discharge",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                doctorDataStore.get().dischargePatient(patient);
                JOptionPane.showMessageDialog(this,
                        patient + " has been discharged.", "Patient Discharged",
                        JOptionPane.INFORMATION_MESSAGE);
                updateButtonVisibility(patient);
            }
        });

        emergencyBtn.addActionListener(e -> {
            String displayLabel = (String) patientSelector.getSelectedItem();
            if (displayLabel == null || displayLabel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a patient first.");
                return;
            }
            String patient = selectorMap.getOrDefault(displayLabel, displayLabel);
            openAddToEmergencyDialog(patient);
            // refresh after dialog closes (patient may now be in emergency queue)
            updateButtonVisibility(patient);
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));
        btnRow.setOpaque(false);
        btnRow.add(admitBtn);
        btnRow.add(dischargeBtn);
        btnRow.add(emergencyBtn);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  ADD TO EMERGENCY DIALOG
    // ═══════════════════════════════════════════════════════════════════════
    private void openAddToEmergencyDialog(String patientName) {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add to Emergency — " + patientName, true);
        dialog.setSize(440, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(28, 36, 28, 36));

        JLabel head = label("Add to Emergency Queue", Font.BOLD, 18, TEXT_DARK);
        JLabel sub2 = label("Patient: " + patientName, Font.PLAIN, 13, TEXT_MID);
        head.setAlignmentX(LEFT_ALIGNMENT);
        sub2.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(4));
        content.add(sub2);
        content.add(Box.createVerticalStrut(20));

        doctorDataStore ds = doctorDataStore.get();
        String[] det = ds.getPatientDetails(patientName);
        Map<String, String> mh = ds.getMedicalHistory(patientName);

        JTextField ageF       = dialogField(); ageF.setText(det[1].equals("--") ? "" : det[1]);
        JTextField genderF    = dialogField(); genderF.setText(det[2].equals("--") ? "" : det[2]);
        JTextField contactF   = new JTextField(15);
        contactF.setText(det[3].equals("--") ? "" : det[3]);
        contactF.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        contactF.setAlignmentX(LEFT_ALIGNMENT);
        contactF.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JTextField) contactF).setDocument(new javax.swing.text.PlainDocument() {
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                if (str == null) return;
                String digits = str.replaceAll("\\D", "");
                if (getLength() + digits.length() > 11) digits = digits.substring(0, 11 - getLength());
                super.insertString(offs, digits, a);
            }
            public void replace(int offs, int len, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                if (str == null) return;
                String digits = str.replaceAll("\\D", "");
                int newLen = getLength() - len + digits.length();
                if (newLen > 11) digits = digits.substring(0, 11 - (getLength() - len));
                super.replace(offs, len, digits, a);
            }
        });
        contactF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) && c != java.awt.event.KeyEvent.VK_BACK_SPACE && c != java.awt.event.KeyEvent.VK_DELETE) {
                    e.consume();
                }
                if (contactF.getText().length() >= 11 && c != java.awt.event.KeyEvent.VK_BACK_SPACE && c != java.awt.event.KeyEvent.VK_DELETE) {
                    e.consume();
                }
            }
        });
        JTextField bloodF     = dialogField(); bloodF.setText(det[4].equals("--") ? "" : det[4]);
        JTextField conditionF = dialogField(); conditionF.setText(mh.getOrDefault("Diagnosis", ""));
        JTextField wardF      = dialogField();

        String[] priorities = {"P1", "P2"};
        JComboBox<String> priorityCB = new JComboBox<>(priorities);
        priorityCB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        priorityCB.setAlignmentX(LEFT_ALIGNMENT);
        priorityCB.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        addFieldDlg(content, "AGE",         ageF);
        addFieldDlg(content, "GENDER",      genderF);
        addFieldDlg(content, "CONTACT",     contactF);
        addFieldDlg(content, "BLOOD GROUP", bloodF);
        addFieldDlg(content, "CONDITION",   conditionF);
        addFieldDlg(content, "WARD / BED",  wardF);

        JLabel priLabel = label("PRIORITY", Font.BOLD, 10, TEXT_MID);
        priLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(priLabel);
        content.add(Box.createVerticalStrut(4));
        content.add(priorityCB);
        content.add(Box.createVerticalStrut(16));

        JButton saveBtn = actionButton("Add to Emergency Queue", new Color(178, 34, 34), Color.WHITE);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        saveBtn.addActionListener(e -> {
            String condition = conditionF.getText().trim();
            if (condition.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Condition is required.");
                return;
            }
            String contact  = contactF.getText().trim();
            if (!contact.isEmpty() && contact.length() != 11) {
                JOptionPane.showMessageDialog(dialog, "Contact must be exactly 11 digits.");
                return;
            }
            String priority = (String) priorityCB.getSelectedItem();
            String time     = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
        String status   = "P1".equals(priority) ? "CRITICAL" : "HIGH PRIORITY";
            String age      = ageF.getText().trim();
            String gender   = genderF.getText().trim();
            String blood    = bloodF.getText().trim();
            String ward     = wardF.getText().trim();

            doctorDataStore dss = doctorDataStore.get();
            dss.addEmergencyPatient(new Object[]{
                priority, patientName, age, condition, time, ward, status});
            dss.getHistoryEmergencyModel().addRow(
                    new Object[]{patientName, age, condition, priority, time, "Pending"});
            dss.ensureInOverall(
                    patientName,
                    age.isEmpty()     ? "--" : age,
                    gender.isEmpty()  ? "--" : gender,
                    contact.isEmpty() ? "--" : contact,
                    blood.isEmpty()   ? "--" : blood);
            dss.notifyListeners();

            JOptionPane.showMessageDialog(dialog,
                    patientName + " has been added to the Emergency Queue as " + priority + ".",
                    "Added to Emergency", JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content);
        sp.setBorder(null);
        dialog.add(sp);
        dialog.setVisible(true);
    }

    private JPanel buildVitalsCard() {
        JPanel card = card("Vitals & Physical");
        vitalsPane = new JEditorPane("text/html",
            "<html><body style='font-family:Segoe UI;font-size:13px;color:#1E293B;padding:0;margin:0'>"
            + "<table cellpadding='3' cellspacing='0'>"
            + rowHtml("Blood Pressure", "--") + rowHtml("Height", "--") + rowHtml("Weight", "--") + rowHtml("Allergies", "--")
            + "</table></body></html>");
        vitalsPane.setEditable(false);
        vitalsPane.setOpaque(false);
        vitalsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        vitalsPane.setBorder(null);
        card.add(vitalsPane, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildMedicalHistoryCard() {
        JPanel card = card("Medical History");
        mhPane = new JEditorPane("text/html",
            "<html><body style='font-family:Segoe UI;font-size:13px;color:#1E293B;padding:0;margin:0'>"
            + "<table cellpadding='3' cellspacing='0'>"
            + rowHtml("Last Diagnosis", "--") + rowHtml("Current Medications", "--")
            + rowHtml("Chronic Conditions", "--") + rowHtml("Family History", "--")
            + rowHtml("Past Surgeries", "--") + rowHtml("Vaccinations", "--")
            + "</table></body></html>");
        mhPane.setEditable(false);
        mhPane.setOpaque(false);
        mhPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);
        mhPane.setBorder(null);
        card.add(mhPane, BorderLayout.CENTER);

        // Edit history button
        JButton editBtn = actionButton("Edit Medical History", ORANGE, Color.WHITE);
        editBtn.addActionListener(e -> openEditHistoryDialog());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        btnRow.setOpaque(false);
        btnRow.add(editBtn);
        card.add(btnRow, BorderLayout.SOUTH);
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  RIGHT — Prescription Writer + History
    // ═══════════════════════════════════════════════════════════════════════
    private JScrollPane buildRightPanel() {
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBackground(BG);
        right.setBorder(new EmptyBorder(16, 8, 16, 16));

        right.add(buildDiagnosisWriter());
        right.add(Box.createVerticalStrut(12));
        right.add(buildPrescriptionWriter());
        right.add(Box.createVerticalStrut(12));
        right.add(buildPrescriptionHistory());
        JScrollPane scroll = new JScrollPane(right);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    private JPanel buildDiagnosisWriter() {
        JPanel card = card("Add Diagnosis");

        JTextArea diagArea = new JTextArea(4, 20);
        diagArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        diagArea.setLineWrap(true); diagArea.setWrapStyleWord(true);
        diagArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER,1),
                BorderFactory.createEmptyBorder(6,10,6,10)));
        diagArea.setToolTipText("Enter diagnosis for this patient");

        JButton saveDiagBtn = actionButton("Save Diagnosis", ACCENT, Color.WHITE);
        saveDiagBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveDiagBtn.addActionListener(e -> {
            String displayLabel = (String) patientSelector.getSelectedItem();
            if (displayLabel == null || displayLabel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a patient first.");
                return;
            }
            String patient = selectorMap.getOrDefault(displayLabel, displayLabel);
            String text = diagArea.getText().trim();
            if (text.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Diagnosis text is required.");
                return;
            }
            doctorDataStore ds = doctorDataStore.get();
            ds.updateMedicalHistory(patient, "Diagnosis", text);
            String displayLabel2 = (String) patientSelector.getSelectedItem();
            if (displayLabel2 != null) loadPatient(displayLabel2);
            diagArea.setText("");
            JOptionPane.showMessageDialog(this,
                    "Diagnosis saved for " + patient + ".", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JLabel lbl = label("DIAGNOSIS", Font.BOLD, 10, TEXT_MID);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        diagArea.setAlignmentX(LEFT_ALIGNMENT);
        diagArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        form.add(lbl);
        form.add(Box.createVerticalStrut(4));
        form.add(diagArea);
        form.add(Box.createVerticalStrut(10));
        form.add(saveDiagBtn);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPrescriptionWriter() {
        JPanel card = card("Issue Prescription");

        // Form fields
        JTextField medField   = dialogField(); medField.setToolTipText("e.g. Paracetamol 500mg");
        JTextField doseField  = dialogField(); doseField.setToolTipText("e.g. 1 tablet");
        JTextField freqField  = dialogField(); freqField.setToolTipText("e.g. 3x daily, after meals");
        JTextField durField   = dialogField(); durField.setToolTipText("e.g. 7 days");
        JTextArea  instrArea  = new JTextArea(3, 20);
        instrArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        instrArea.setLineWrap(true); instrArea.setWrapStyleWord(true);
        instrArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER,1),
                BorderFactory.createEmptyBorder(6,10,6,10)));
        instrArea.setToolTipText("Special instructions for the patient");

        JTextArea  notesArea  = new JTextArea(2, 20);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        notesArea.setLineWrap(true); notesArea.setWrapStyleWord(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER,1),
                BorderFactory.createEmptyBorder(6,10,6,10)));

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setOpaque(false);

        JPanel row1 = new JPanel(new GridLayout(1,2,10,0));
        row1.setOpaque(false);
        JPanel r1a = fieldGroup("MEDICATION / DRUG", medField);
        JPanel r1b = fieldGroup("DOSAGE", doseField);
        row1.add(r1a); row1.add(r1b);

        JPanel row2 = new JPanel(new GridLayout(1,2,10,0));
        row2.setOpaque(false);
        JPanel r2a = fieldGroup("FREQUENCY", freqField);
        JPanel r2b = fieldGroup("DURATION", durField);
        row2.add(r2a); row2.add(r2b);

        form.add(row1);
        form.add(Box.createVerticalStrut(8));
        form.add(row2);
        form.add(Box.createVerticalStrut(8));
        form.add(fieldGroupTA("SPECIAL INSTRUCTIONS", instrArea));
        form.add(Box.createVerticalStrut(8));
        form.add(fieldGroupTA("DOCTOR'S NOTES", notesArea));
        form.add(Box.createVerticalStrut(12));

        JButton rxBtn = actionButton("Issue Prescription", GREEN, Color.WHITE);
        rxBtn.setAlignmentX(LEFT_ALIGNMENT);
        rxBtn.addActionListener(e -> {
            String displayLabel = (String) patientSelector.getSelectedItem();
            if (displayLabel == null || displayLabel.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a patient first.");
                return;
            }
            String patient = selectorMap.getOrDefault(displayLabel, displayLabel);
            String med = medField.getText().trim();
            if (med.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Medication name is required.");
                return;
            }
            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            doctorDataStore.get().addPrescription(patient, date, med,
                    doseField.getText().trim(), freqField.getText().trim(),
                    durField.getText().trim(), instrArea.getText().trim(),
                    notesArea.getText().trim());
            // Mark appointment as Completed — match by patient+doctor (any date)
            doctorDataStore ds = doctorDataStore.get();
            String currentDoctor = ds.getDoctorDisplayName();
            System.out.println("[Prescription] Issuing for patient=" + patient + " doctor=" + currentDoctor);
            DefaultTableModel apptRx = ds.getAppointmentModel();
            boolean found = false;
            for (int j = 0; j < apptRx.getRowCount(); j++) {
                if (patient.equals(str(apptRx.getValueAt(j, 0)))
                        && currentDoctor.equals(str(apptRx.getValueAt(j, 3)))) {
                    String rxTime = str(apptRx.getValueAt(j, 1));
                    String rxDate = str(apptRx.getValueAt(j, 2));
                    // Check if this appointment is currently Accepted
                    String sk = patient + "|" + rxDate + "|" + rxTime + "|" + currentDoctor;
                    String curSt = ds.patientAppointmentStatus.get(sk);
                    if (!"Accepted".equals(curSt)) continue;
                    System.out.println("[Prescription] Accepted match: date=" + rxDate + " time=" + rxTime);
                    ds.updatePatientAppointmentStatus(patient, rxDate, rxTime, "Completed");
                    found = true;
                }
            }
            if (!found) {
                System.out.println("[Prescription] WARNING: No matching accepted appointment found for patient=" + patient + " doctor=" + currentDoctor);
            }
            medField.setText(""); doseField.setText("");
            freqField.setText(""); durField.setText("");
            instrArea.setText(""); notesArea.setText("");
            refreshPrescriptionHistory(patient);
            JOptionPane.showMessageDialog(this,
                    "Prescription issued for " + patient + ".", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        form.add(rxBtn);

        card.add(form, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildPrescriptionHistory() {
        JPanel card = card("Prescription History");

        rxTableModel = new DefaultTableModel(
                new String[]{"Patient","Date","Medication","Dosage","Frequency","Duration"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable rxTable = new JTable(rxTableModel);
        rxTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        rxTable.setRowHeight(34);
        rxTable.setShowGrid(false);
        rxTable.setIntercellSpacing(new Dimension(0,0));
        rxTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        rxTable.getTableHeader().setBackground(new Color(248,250,252));
        rxTable.getTableHeader().setForeground(TEXT_MID);
        rxTable.getTableHeader().setBorder(new MatteBorder(0,0,1,0,DIVIDER));

        JScrollPane scroll = new JScrollPane(rxTable);
        scroll.setBorder(BorderFactory.createLineBorder(DIVIDER, 1));
        scroll.getViewport().setBackground(CARD_WHITE);
        card.add(scroll, BorderLayout.CENTER);

        prescriptionHistoryPanel = card;
        return card;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  LOAD PATIENT
    // ═══════════════════════════════════════════════════════════════════════
    private void loadPatient(String displayLabel) {
        String name = selectorMap.getOrDefault(displayLabel, displayLabel);
        if (name == null || name.isEmpty()) return;
        doctorDataStore ds = doctorDataStore.get();

        java.util.LinkedHashMap<String, String> profile = HospitalSystem.fetchPatientProfile(name);
        String[] det = ds.getPatientDetails(name);
        Map<String, String> mh = ds.getMedicalHistory(name);

        // Info
        String infoName  = profile.containsKey("Name")    ? profile.get("Name")    : det[0];
        String infoAge   = profile.containsKey("Age")     ? profile.get("Age")     : det[1];
        String infoGen   = profile.containsKey("Gender")  ? profile.get("Gender")  : det[2];
        String infoCont  = profile.containsKey("Contact") ? profile.get("Contact") : det[3];
        String infoBlood = profile.containsKey("Blood")   ? profile.get("Blood")   : det[4];
        String infoEmail = profile.containsKey("Email")   ? profile.get("Email")   : "--";
        String infoVisits= det[5];

        // Source
        boolean isAppt = isAppointmentPatient(name);
        boolean isEmer = isEmergencyPatient(name);
        String source;
        if (isAppt && isEmer)       source = "Appointment + Emergency";
        else if (isAppt)            source = "Scheduled Appointment";
        else if (isEmer)            source = "Emergency Queue";
        else                        source = "--";

        // Appointment time
        String apptTime = "--";
        if (displayLabel.contains("[")) {
            String inner = displayLabel.substring(displayLabel.indexOf('[') + 1, displayLabel.indexOf(']'));
            if (inner.contains(",")) {
                String[] parts = inner.split(",", 2);
                apptTime = parts[0].trim() + "  (" + parts[1].trim() + ")";
            } else if (inner.equals("ER")) {
                DefaultTableModel emgModel = ds.getEmergencyModel();
                for (int i = 0; i < emgModel.getRowCount(); i++) {
                    if (name.equals(str(emgModel.getValueAt(i, 1)))) {
                        apptTime = "Arrived " + str(emgModel.getValueAt(i, 4))
                                 + "  [" + str(emgModel.getValueAt(i, 0)) + " – "
                                 + str(emgModel.getValueAt(i, 6)) + "]";
                        break;
                    }
                }
            }
        }

        // Build info HTML
        String infoHtml = "<html><body style='font-family:Segoe UI;font-size:13px;color:#1E293B;padding:0;margin:0'>"
            + "<table cellpadding='3' cellspacing='0'>"
            + rowHtml("Full Name", infoName)
            + rowHtml("Age", infoAge)
            + rowHtml("Gender", infoGen)
            + rowHtml("Contact", infoCont)
            + rowHtml("Email", infoEmail)
            + rowHtml("Blood Group", infoBlood)
            + rowHtml("Total Visits", infoVisits)
            + rowHtml("Source", source)
            + rowHtml("Appt. Time", apptTime)
            + "</table></body></html>";
        infoPane.setText(infoHtml);

        // Vitals
        String bp = mh.getOrDefault("BP", "--");
        String ht = mh.getOrDefault("Height", "--");
        String wt = mh.getOrDefault("Weight", "--");
        String al = profile.containsKey("Allergies") ? profile.get("Allergies") : mh.getOrDefault("Allergies", "--");
        String vitHtml = "<html><body style='font-family:Segoe UI;font-size:13px;color:#1E293B;padding:0;margin:0'>"
            + "<table cellpadding='3' cellspacing='0'>"
            + rowHtml("Blood Pressure", bp)
            + rowHtml("Height", ht)
            + rowHtml("Weight", wt)
            + rowHtml("Allergies", al)
            + "</table></body></html>";
        vitalsPane.setText(vitHtml);

        // Medical history
        String diag = profile.containsKey("Diagnosis")         ? profile.get("Diagnosis")
                    : mh.getOrDefault("Diagnosis", "--");
        String meds = profile.containsKey("Medications")       ? profile.get("Medications")
                    : mh.getOrDefault("Medications", "--");
        String chron= profile.containsKey("ChronicConditions") ? profile.get("ChronicConditions")
                    : mh.getOrDefault("ChronicConditions", "--");
        String fam  = profile.containsKey("FamilyHistory")     ? profile.get("FamilyHistory")
                    : mh.getOrDefault("FamilyHistory", "--");
        String surg = profile.containsKey("Surgeries")         ? profile.get("Surgeries")
                    : mh.getOrDefault("Surgeries", "--");
        String vacc = profile.containsKey("Vaccinations")      ? profile.get("Vaccinations")
                    : mh.getOrDefault("Vaccinations", "--");
        String mhHtml = "<html><body style='font-family:Segoe UI;font-size:13px;color:#1E293B;padding:0;margin:0'>"
            + "<table cellpadding='3' cellspacing='0'>"
            + rowHtml("Last Diagnosis", diag)
            + rowHtml("Current Medications", meds)
            + rowHtml("Chronic Conditions", chron)
            + rowHtml("Family History", fam)
            + rowHtml("Past Surgeries", surg)
            + rowHtml("Vaccinations", vacc)
            + "</table></body></html>";
        mhPane.setText(mhHtml);

        refreshPrescriptionHistory(name);
        revalidate(); repaint();
    }

    private void refreshPrescriptionHistory(String name) {
        if (rxTableModel == null) return;
        rxTableModel.setRowCount(0);
        DefaultTableModel all = doctorDataStore.get().getPrescriptionModel();
        for (int i = 0; i < all.getRowCount(); i++) {
            if (name.equals(str(all.getValueAt(i, 0)))) {
                rxTableModel.addRow(new Object[]{
                    all.getValueAt(i,0), all.getValueAt(i,1), all.getValueAt(i,2),
                    all.getValueAt(i,3), all.getValueAt(i,4), all.getValueAt(i,5)
                });
            }
        }
    }

    private void refreshPatientList() {
        String currentDisplay = (String) patientSelector.getSelectedItem();
        String currentName = selectorMap.getOrDefault(currentDisplay, currentDisplay);

        selectorMap.clear();
        patientSelector.removeAllItems();

        doctorDataStore ds = doctorDataStore.get();
        String doctorName = ds.getDoctorDisplayName();
        System.out.println("[Consult] Doctor=" + doctorName);
        DefaultTableModel apptModel = ds.getAppointmentModel();
        java.util.Map<String, String> statusMap = ds.patientAppointmentStatus;
        System.out.println("[Consult] apptModel rows=" + apptModel.getRowCount());

        // Show each accepted appointment as a separate entry: "PatientName  [Time, Date]"
        java.util.LinkedHashMap<String, String> apptEntries = new java.util.LinkedHashMap<>();
        for (int i = 0; i < apptModel.getRowCount(); i++) {
            String pat = str(apptModel.getValueAt(i, 0));
            String tim = str(apptModel.getValueAt(i, 1));
            String dat = str(apptModel.getValueAt(i, 2));
            String doc = str(apptModel.getValueAt(i, 3));
            if (!doc.equalsIgnoreCase(doctorName)) continue;
            // Check if this appointment is accepted by the doctor
            String sk = pat + "|" + dat + "|" + tim + "|" + doctorName;
            String st = statusMap.get(sk);
            // Only show accepted appointments
            if (!"Accepted".equals(st)) {
                System.out.println("[Consult] SKIP non-accepted: " + pat + " " + tim + " " + dat + " status=" + st);
                continue;
            }
            System.out.println("[Consult] SHOW: " + pat + " " + tim + " " + dat + " status=" + st);
            String label = pat + "  [" + tim + ", " + dat + "]";
            apptEntries.put(label, pat);
        }

        // Collect emergency patient names (one entry per unique name)
        java.util.LinkedHashMap<String, String> emerEntries = new java.util.LinkedHashMap<>();
        DefaultTableModel emgModel = ds.getEmergencyModel();
        for (int i = 0; i < emgModel.getRowCount(); i++) {
            String n = str(emgModel.getValueAt(i, 1));
            if (!n.isEmpty() && !"--".equals(n)) {
                String label = n + "  [ER]";
                emerEntries.put(label, n);
            }
        }

        // Merge: appointment entries first, then emergency entries not already in appointment
        java.util.LinkedHashMap<String, String> all = new java.util.LinkedHashMap<>();
        all.putAll(apptEntries);
        for (java.util.Map.Entry<String, String> e : emerEntries.entrySet()) {
            if (!all.containsValue(e.getValue())) {
                all.put(e.getKey(), e.getValue());
            }
        }

        for (java.util.Map.Entry<String, String> e : all.entrySet()) {
            selectorMap.put(e.getKey(), e.getValue());
            patientSelector.addItem(e.getKey());
        }

        // Restore previous selection by raw name
        if (currentName != null) {
            for (java.util.Map.Entry<String, String> e : selectorMap.entrySet()) {
                if (currentName.equals(e.getValue())) {
                    patientSelector.setSelectedItem(e.getKey());
                    break;
                }
            }
        }
    }

    // ── Button visibility ────────────────────────────────────────────────
    /**
     * Shows only Discharge when the patient is admitted or in the emergency queue;
     * shows Admit + Add-to-Emergency (and hides Discharge) otherwise.
     */
    private void updateButtonVisibility(String patientName) {
        if (admitBtn == null || dischargeBtn == null || emergencyBtn == null) return;
        boolean active = doctorDataStore.get().isAdmitted(patientName)
                      || doctorDataStore.get().isInEmergency(patientName);
        admitBtn.setVisible(!active);
        emergencyBtn.setVisible(!active);
        dischargeBtn.setVisible(active);
        // revalidate the parent so layout adjusts
        if (admitBtn.getParent() != null) {
            admitBtn.getParent().revalidate();
            admitBtn.getParent().repaint();
        }
    }

    // ── Source lookup helpers ────────────────────────────────────────────
    private boolean isAppointmentPatient(String name) {
        DefaultTableModel m = doctorDataStore.get().getAppointmentModel();
        for (int i = 0; i < m.getRowCount(); i++)
            if (name.equals(str(m.getValueAt(i, 0)))) return true;
        return false;
    }

    private boolean isEmergencyPatient(String name) {
        DefaultTableModel m = doctorDataStore.get().getEmergencyModel();
        for (int i = 0; i < m.getRowCount(); i++)
            if (name.equals(str(m.getValueAt(i, 1)))) return true;
        return false;
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  EDIT MEDICAL HISTORY DIALOG
    // ═══════════════════════════════════════════════════════════════════════
    private void openEditHistoryDialog() {
        String displayLabel = (String) patientSelector.getSelectedItem();
        if (displayLabel == null) { JOptionPane.showMessageDialog(this,"Select a patient first."); return; }
        String patient = selectorMap.getOrDefault(displayLabel, displayLabel);

        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Edit Medical History — " + patient, true);
        dlg.setSize(520, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(CARD_WHITE);
        content.setBorder(new EmptyBorder(28, 36, 28, 36));

        JLabel head = label("Edit Medical History", Font.BOLD, 18, TEXT_DARK);
        JLabel sub2 = label("Patient: " + patient, Font.PLAIN, 13, TEXT_MID);
        head.setAlignmentX(LEFT_ALIGNMENT);
        sub2.setAlignmentX(LEFT_ALIGNMENT);
        content.add(head);
        content.add(Box.createVerticalStrut(4));
        content.add(sub2);
        content.add(Box.createVerticalStrut(20));

        Map<String, String> mh = doctorDataStore.get().getMedicalHistory(patient);
        String[][] fields = {
            {"Diagnosis",         "Last Diagnosis"},
            {"Medications",       "Current Medications"},
            {"Allergies",         "Allergies"},
            {"BP",                "Blood Pressure"},
            {"Height",            "Height"},
            {"Weight",            "Weight"},
            {"ChronicConditions", "Chronic Conditions"},
            {"FamilyHistory",     "Family History"},
            {"Surgeries",         "Past Surgeries"},
            {"Vaccinations",      "Vaccinations"},
        };
        Map<String, JTextField> fieldMap = new LinkedHashMap<>();
        for (String[] fd : fields) {
            JTextField tf = dialogField();
            tf.setText(mh.getOrDefault(fd[0], ""));
            addFieldDlg(content, fd[1].toUpperCase(), tf);
            fieldMap.put(fd[0], tf);
        }

        JButton saveBtn = actionButton("Save Changes", ACCENT, Color.WHITE);
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.addActionListener(e -> {
            Map<String, String> updated = new LinkedHashMap<>();
            for (String[] fd : fields)
                updated.put(fd[0], fieldMap.get(fd[0]).getText().trim());
            doctorDataStore.get().setMedicalHistoryAll(patient, updated);
            loadPatient(patient);
            dlg.dispose();
        });
        content.add(Box.createVerticalStrut(10));
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dlg.add(sp);
        dlg.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════════════
    //  UI HELPERS
    // ═══════════════════════════════════════════════════════════════════════
    private JPanel card(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 12));
        p.setBackground(CARD_WHITE);
        p.setBorder(new CompoundBorder(
                new LineBorder(DIVIDER, 1, true),
                new EmptyBorder(18, 20, 18, 20)));
        JLabel t = label(title, Font.BOLD, 15, TEXT_DARK);
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.setOpaque(false);
        top.add(t, BorderLayout.NORTH);
        top.add(sep, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);
        return p;
    }

    private String rowHtml(String label, String value) {
        return "<tr><td style='color:#64748B;vertical-align:top;white-space:nowrap;padding:2px 12px 2px 0'>"
            + label + ":</td><td style='color:#1E293B;vertical-align:top'>" + escHtml(value) + "</td></tr>";
    }

    private String escHtml(String s) {
        if (s == null) return "--";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
    }

    private JLabel label(String text, int style, int size, Color fg) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", style, size));
        l.setForeground(fg);
        return l;
    }

    private JTextField dialogField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(DIVIDER, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JPanel fieldGroup(String labelText, JTextField f) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = label(labelText, Font.BOLD, 10, TEXT_MID);
        l.setAlignmentX(LEFT_ALIGNMENT);
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(f);
        return p;
    }

    private JPanel fieldGroupTA(String labelText, JTextArea f) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        JLabel l = label(labelText, Font.BOLD, 10, TEXT_MID);
        l.setAlignmentX(LEFT_ALIGNMENT);
        JScrollPane sp = new JScrollPane(f);
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setBorder(BorderFactory.createLineBorder(DIVIDER,1));
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(sp);
        return p;
    }

    private void addFieldDlg(JPanel p, String labelText, JTextField f) {
        JLabel l = label(labelText, Font.BOLD, 10, TEXT_MID);
        l.setAlignmentX(LEFT_ALIGNMENT);
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
    }

    private JButton actionButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(fg);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBorder(new EmptyBorder(10, 20, 10, 20));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private String str(Object o) { return o == null ? "" : o.toString(); }
}