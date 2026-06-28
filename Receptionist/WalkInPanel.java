package Receptionist;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import System.HospitalSystem;
import Doctor.doctorDataStore;
import java.awt.event.*;
import java.util.Date;
import java.util.List;

/**
 * WalkInPanel
 * Full-width walk-in admission form.
 * Doctor combo is filtered by the selected department.
 */
public class WalkInPanel {

    public interface NavigationListener {
        void navigateTo(String section);
    }

    private final JFrame             owner;
    private final NavigationListener nav;

    public WalkInPanel(JFrame owner, NavigationListener nav) {
        this.owner = owner;
        this.nav   = nav;
    }

    public JPanel build() {
        JPanel p    = ReceptionistUIHelper.page();
        JPanel form = ReceptionistUIHelper.formCard("🚶  Walk-in Admission");

        JTextField tfName  = ReceptionistUIHelper.styledField("Full patient name");
        JTextField tfPhone = ReceptionistUIHelper.styledField("Phone number or CNIC");
        javax.swing.text.PlainDocument pdoc = new javax.swing.text.PlainDocument() {
            public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                if (str == null || (getLength() + str.length()) > 13) return;
                for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
                super.insertString(offs, str, a);
            }
            public void replace(int offs, int len, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
                if (str == null || (getLength() - len + str.length()) > 13) return;
                for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
                super.replace(offs, len, str, a);
            }
        };
        tfPhone.setDocument(pdoc);
        tfPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && !Character.isISOControl(e.getKeyChar())) e.consume();
            }
        });
        pdoc.addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                javax.swing.text.Document d = e.getDocument();
                if (d.getLength() > 13) { try { d.remove(13, d.getLength() - 13); } catch (javax.swing.text.BadLocationException ex) {} }
            }
            public void removeUpdate(javax.swing.event.DocumentEvent e) {}
            public void changedUpdate(javax.swing.event.DocumentEvent e) {}
        });
        JTextField tfComp  = ReceptionistUIHelper.styledField("Chief complaint / reason for visit");
        JComboBox<String> cbPrio = ReceptionistUIHelper.combo(
            new String[]{"Low","Medium","High","Critical"});

        // ── Department combo ──────────────────────────────────────────────────
        DefaultComboBoxModel<String> deptModel =
            new DefaultComboBoxModel<>(ReceptionistDataStore.getDynamicDepartments());
        JComboBox<String> cbDept = new JComboBox<>(deptModel);
        cbDept.setFont(ReceptionistUIHelper.F_SMALL);
        cbDept.setBackground(ReceptionistUIHelper.C_WHITE);

        // ── Doctor combo — filtered by dept ───────────────────────────────────
        DefaultComboBoxModel<String> docModel = new DefaultComboBoxModel<>();
        JComboBox<String> cbDoc = new JComboBox<>(docModel);
        cbDoc.setFont(ReceptionistUIHelper.F_SMALL);
        cbDoc.setBackground(ReceptionistUIHelper.C_WHITE);

        boolean[] updating = {false};

        Runnable fillDoctors = () -> {
            updating[0] = true;
            try {
                String dept = (String) cbDept.getSelectedItem();
                String[] depts = ReceptionistDataStore.getDynamicDepartments();
                List<String> docs = ReceptionistDataStore.getDynamicDoctorsByDept(
                    dept != null ? dept : depts[0]);
                docModel.removeAllElements();
                for (String d : docs) docModel.addElement(d);
                if (!docs.isEmpty()) cbDoc.setSelectedIndex(0);
            } finally {
                updating[0] = false;
            }
        };

        // ── Slot picker ───────────────────────────────────────────────────────
        SlotPickerPanel slotPicker = new SlotPickerPanel();

        JTextField[] tfDateRef = {null};
        Runnable refreshSlots = () -> {
            if (updating[0]) return;
            slotPicker.clearSelection();
            String doc   = (String) cbDoc.getSelectedItem();
            String dateN = ReceptionistDataStore.normaliseDate(
                tfDateRef[0] != null ? tfDateRef[0].getText() : "");
            if (doc == null || doc.isEmpty() || dateN.isEmpty()) return;
            slotPicker.refresh(doc, dateN, "");
        };

        JTextField tfDate = ReceptionistUIHelper.dateField(
            "DD/MM/YYYY  or  DD MMM YYYY", refreshSlots);
        tfDate.setText("10 Jun 2026");
        tfDateRef[0] = tfDate;

        // ── Wire listeners ────────────────────────────────────────────────────
        cbDept.addActionListener(e -> {
            if (updating[0]) return;
            fillDoctors.run();
            SwingUtilities.invokeLater(refreshSlots);
        });
        cbDoc.addActionListener(e -> {
            if (updating[0]) return;
            SwingUtilities.invokeLater(refreshSlots);
        });

        // Initial population
        fillDoctors.run();

        // Listen for roster changes and refresh combos
        ReceptionistDataStore.addRosterListener(() -> {
            SwingUtilities.invokeLater(() -> {
                deptModel.removeAllElements();
                for (String d : ReceptionistDataStore.getDynamicDepartments()) {
                    deptModel.addElement(d);
                }
                fillDoctors.run();
            });
        });

        // ── 3-column form grid ────────────────────────────────────────────────
        JPanel formGrid = new JPanel(new GridLayout(0, 3, 18, 14)); formGrid.setOpaque(false);
        formGrid.add(ReceptionistUIHelper.fg("Patient Name *",    tfName));
        formGrid.add(ReceptionistUIHelper.fg("Phone / CNIC",      tfPhone));
        formGrid.add(ReceptionistUIHelper.fg("Triage Priority",   cbPrio));
        formGrid.add(ReceptionistUIHelper.fg("Chief Complaint *", tfComp));
        formGrid.add(ReceptionistUIHelper.fg("Date *",            tfDate));
        formGrid.add(ReceptionistUIHelper.fg("Department *",      cbDept));
        formGrid.add(ReceptionistUIHelper.fg("Doctor",            cbDoc));

        // ── Buttons ───────────────────────────────────────────────────────────
        JButton btnAdmit = ReceptionistUIHelper.primary("✅  Admit Walk-in");
        JButton btnReset = ReceptionistUIHelper.ghost("Reset Form");
        JButton btnQueue = ReceptionistUIHelper.ghost("📋  View Queue →");
        btnQueue.addActionListener(e -> nav.navigateTo("Walk-in Queue"));

        Runnable resetAll = () -> {
            tfName.setText(""); tfPhone.setText(""); tfComp.setText("");
            tfDate.setText("10 Jun 2026");
            cbPrio.setSelectedIndex(0);
            try { updating[0] = true; cbDept.setSelectedIndex(0); }
            finally { updating[0] = false; }
            fillDoctors.run();
            slotPicker.clearSelection();
        };
        btnReset.addActionListener(e -> resetAll.run());

        btnAdmit.addActionListener(e -> {
            String name = tfName.getText().trim();
            String comp = tfComp.getText().trim();
            String phone = tfPhone.getText().trim();
            if (name.isEmpty()) {
                ReceptionistUIHelper.showError(owner, "Patient Name is required."); return; }
            if (comp.isEmpty()) {
                ReceptionistUIHelper.showError(owner, "Chief Complaint is required."); return; }
            if (!ReceptionistUIHelper.validateDateField(tfDate, owner)) return;
            if (slotPicker.getSelectedSlot().isEmpty()) {
                ReceptionistUIHelper.showError(owner, "Please select a time slot from the grid."); return; }
            if (ReceptionistDataStore.isDuplicatePatient(name, phone)) {
                ReceptionistUIHelper.showError(owner, "Patient \"" + name + "\" already exists in the system.");
                return;
            }

            String date   = ReceptionistDataStore.normaliseDate(tfDate.getText().trim());
            String time   = slotPicker.getSelectedSlot();
            String prio   = cbPrio.getSelectedItem().toString();
            String dept   = cbDept.getSelectedItem().toString();
            String doctor = cbDoc.getSelectedItem() != null
                ? cbDoc.getSelectedItem().toString() : "Unassigned";
            int wait = 5 + ReceptionistDataStore.walkModel.getRowCount() * 12;

            ReceptionistDataStore.walkCounter++;
            String qNum = "W-0" + ReceptionistDataStore.walkCounter;
            ReceptionistDataStore.walkModel.addRow(
                new Object[]{qNum, name, date, time, comp, prio, doctor, wait + " min"});

            ReceptionistDataStore.apptCounter++;
            String apptId = "#A-00" + ReceptionistDataStore.apptCounter;
            ReceptionistDataStore.apptModel.addRow(
                new Object[]{apptId, name, date, time, doctor, dept, "Walk-in", ""});
            ReceptionistDataStore.patientCounter++;
            ReceptionistDataStore.registerPatientInAdmin(name, phone, "PAT-" + name.replace(" ", ""), dept);
            // Always sync to shared appointment tracking for admin live log
            ReceptionistDataStore.syncAppointmentToSharedStores(name, doctor, date, time, apptId, "Walk-in");
            if (!doctor.equals("Unassigned")) {
                HospitalSystem.markSlotBooked(
                    ReceptionistDataStore.stripDr(doctor),
                    ReceptionistDataStore.toYmd(date),
                    time);
            }

            // Push Emergency Medicine walk-ins (non-Low priority) to doctor's emergency queue
            if (dept.equalsIgnoreCase("Emergency Medicine") && !"Low".equals(prio)) {
                String emPrio  = "Critical".equals(prio) ? "P1" : "P2";
                String emStatus = "Critical".equals(prio) ? "CRITICAL" : "HIGH PRIORITY";
                String now     = new java.text.SimpleDateFormat("HH:mm").format(new Date());
                doctorDataStore.get().addEmergencyPatient(
                    new Object[]{emPrio, name, "--", comp, now, "--", emStatus});
            }
            // Also push Medium/High/Critical walk-ins from any department to doctor's queue
            else if (!"Low".equals(prio)) {
                String emPrio  = "Critical".equals(prio) ? "P1" : "P2";
                String emStatus = "Critical".equals(prio) ? "CRITICAL" : "HIGH PRIORITY";
                String now     = new java.text.SimpleDateFormat("HH:mm").format(new Date());
                doctorDataStore.get().addEmergencyPatient(
                    new Object[]{emPrio, name, "--", comp, now, "--", emStatus});
            }

            JOptionPane.showMessageDialog(owner,
                "✅  Walk-in Admitted!\n\nQueue #: " + qNum +
                "\nPatient: " + name + "\nDoctor: " + doctor +
                "\nPriority: " + prio + "\nDate: " + date +
                "  at  " + time + "\nEst. Wait: " + wait +
                " min\nAppointment: " + apptId,
                "Walk-in Admitted", JOptionPane.INFORMATION_MESSAGE);
            resetAll.run();
        });

        JPanel br = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); br.setOpaque(false);
        br.add(btnQueue);
        br.add(Box.createHorizontalStrut(6));
        br.add(btnReset); br.add(btnAdmit);

        // Live queue badge
        JPanel qBadge = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        qBadge.setBackground(new Color(240, 249, 255));
        qBadge.setBorder(new CompoundBorder(
            new LineBorder(new Color(186, 230, 253), 1, true),
            new EmptyBorder(8, 14, 8, 14)));
        JLabel qIcon  = new JLabel("🚶");
        qIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
        JLabel qCount = new JLabel(
            ReceptionistDataStore.walkModel.getRowCount() + " patients currently in walk-in queue");
        qCount.setFont(ReceptionistUIHelper.F_BOLD_S);
        qCount.setForeground(new Color(3, 105, 161));
        JLabel qLink  = new JLabel("  →  View full queue");
        qLink.setFont(ReceptionistUIHelper.F_BOLD_S);
        qLink.setForeground(ReceptionistUIHelper.C_BLUE);
        qLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        qLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { nav.navigateTo("Walk-in Queue"); }
        });
        qBadge.add(qIcon); qBadge.add(qCount); qBadge.add(qLink);

        JPanel fb = new JPanel(new BorderLayout(0, 16)); fb.setOpaque(false);
        fb.add(qBadge,   BorderLayout.NORTH);
        fb.add(formGrid, BorderLayout.CENTER);

        JPanel lower = new JPanel(new BorderLayout(0, 14)); lower.setOpaque(false);
        lower.add(slotPicker, BorderLayout.CENTER);
        lower.add(br,         BorderLayout.SOUTH);

        JPanel bodyWrap = new JPanel(new BorderLayout(0, 16)); bodyWrap.setOpaque(false);
        bodyWrap.add(fb,    BorderLayout.NORTH);
        bodyWrap.add(lower, BorderLayout.CENTER);

        form.add(bodyWrap, BorderLayout.CENTER);
        ReceptionistUIHelper.stack(p, form);
        return p;
    }
}