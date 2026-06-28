package Doctor;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.text.SimpleDateFormat;
import java.util.*;
import System.DoctorRosterStore;
import System.HospitalSystem;

public class doctorSchedulePanel extends JPanel {

    private JPanel slotContainer;
    private JButton btnDaily, btnWeekly;
    private JPanel  mainPanel;
    private CardLayout cardLayout;

    private static final String[] SLOT_TIMES_FALLBACK = {
        "08:00 AM","08:30 AM","09:00 AM","09:30 AM","10:00 AM","10:30 AM",
        "11:00 AM","11:30 AM","12:00 PM","12:30 PM",
        "01:00 PM","01:30 PM","02:00 PM","02:30 PM","03:00 PM","03:30 PM",
        "04:00 PM","04:30 PM","05:00 PM","05:30 PM","06:00 PM"
    };

    private JLabel bannerBookedLbl, bannerFreeLbl;
    private JLabel cardBookedVal, cardFreeVal;
    private JPanel weeklyCardsPanel;
    private DefaultTableModel weeklyModel;
    private JTable weeklyTable;

    private static final Color BG          = new Color(243, 246, 251);
    private static final Color NAVY        = new Color(22,  43,  90);
    private static final Color NAVY2       = new Color(40,  75, 145);
    private static final Color WHITE       = Color.WHITE;
    private static final Color DIVIDER     = new Color(226, 232, 240);
    private static final Color TEXT_DARK   = new Color(30,  41,  59);
    private static final Color TEXT_MID    = new Color(100, 116, 139);
    private static final Color TEXT_LIGHT  = new Color(148, 163, 184);
    private static final Color BOOKED_BG   = new Color(239, 246, 255);
    private static final Color BOOKED_LEFT = new Color(59,  130, 246);
    private static final Color FREE_BG     = new Color(240, 253, 244);
    private static final Color FREE_LEFT   = new Color(16,  185, 129);
    private static final Color EMERG_BG    = new Color(255, 241, 242);
    private static final Color EMERG_LEFT  = new Color(220,  38,  38);
    private static final Color C_BLUE      = new Color(59,  130, 246);
    private static final Color C_GREEN     = new Color(16,  185, 129);
    private static final Color C_PURPLE    = new Color(139,  92, 246);
    private static final Color C_RED       = new Color(220,  38,  38);

    public doctorSchedulePanel() {
        setLayout(new BorderLayout());
        setBackground(BG);
        add(buildHeader(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(BG);
        mainPanel.setBorder(new EmptyBorder(0, 24, 20, 24));
        mainPanel.add(buildDailyView(),  "Daily");
        mainPanel.add(buildWeeklyView(), "Weekly");
        add(mainPanel, BorderLayout.CENTER);

        doctorDataStore.get().addListener(this::refreshAll);
        DoctorRosterStore.addListener(this::refreshAll);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DEFAULT 9 AM APPOINTMENT (pre-seeded on first load)
    // ═══════════════════════════════════════════════════════════════
    private void addDefaultNineAmAppointment() {
        final String SLOT      = "09:00 AM";
        final String PATIENT   = "John Smith";
        final String TODAY     = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final String CONTACT   = "555-0100";
        final String TYPE      = "Consultation";
        final String NOTES     = "Annual check-up";

        doctorDataStore ds = doctorDataStore.get();

        // Check whether this patient+time already exists across all dates
        DefaultTableModel am = ds.getAppointmentModel();
        for (int i = 0; i < am.getRowCount(); i++) {
            if (PATIENT.equals(str(am.getValueAt(i, 0)))
                    && normalizeTime(str(am.getValueAt(i, 1))).equals(SLOT)) {
                return; // already exists — do nothing
            }
        }

        ds.addAppointment(PATIENT, SLOT, TODAY);
        String docName = ds.getDoctorDisplayName();
        ds.getHistoryAppointmentModel().addRow(new Object[]{
            PATIENT, "--", CONTACT,
            TYPE, TODAY, SLOT,
            NOTES, "", "", "", docName
        });
        ds.ensureInOverall(PATIENT, "--", "--", CONTACT, "--");
        ds.notifyListeners();      
    }
    private void addanotherdefaultAppointment() {
        final String SLOT      = "11:00 AM";
        final String PATIENT   = "sarah Smith";
        final String TODAY     = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        final String CONTACT   = "345-0100";
        final String TYPE      = "Consultation";
        final String NOTES     = "Annual check-up";

        doctorDataStore ds = doctorDataStore.get();

        // Check whether this patient+time already exists across all dates
        DefaultTableModel am = ds.getAppointmentModel();
        for (int i = 0; i < am.getRowCount(); i++) {
            if (PATIENT.equals(str(am.getValueAt(i, 0)))
                    && normalizeTime(str(am.getValueAt(i, 1))).equals(SLOT)) {
                return; // already exists — do nothing
            }
        }

        ds.addAppointment(PATIENT, SLOT, TODAY);
        String docName2 = ds.getDoctorDisplayName();
        ds.getHistoryAppointmentModel().addRow(new Object[]{
            PATIENT, "--", CONTACT,
            TYPE, TODAY, SLOT,
            NOTES, "", "", "", docName2
        });
        ds.ensureInOverall(PATIENT, "--", "--", CONTACT, "--");
        ds.notifyListeners();      
    }

    

    // ═══════════════════════════════════════════════════════════════
    //  COUNTS
    // ═══════════════════════════════════════════════════════════════
    private int countBooked() { return doctorDataStore.get().getBookedSlotCount(); }
    private int countFree()   { return doctorDataStore.get().getFreeSlotCount(); }
    /** Counts appointments in the current week from history model for this doctor */
    private int countWeeklyBooked() {
        String[] dates = weekDates();
        java.util.Set<String> weekSet = new java.util.LinkedHashSet<>(java.util.Arrays.asList(dates));
        int count = 0;
        DefaultTableModel hm = doctorDataStore.get().getHistoryAppointmentModel();
        String currentDoctor = doctorDataStore.get().getDoctorDisplayName();
        for (int i = 0; i < hm.getRowCount(); i++) {
            String date = normalizeDateYmd(str(hm.getValueAt(i, 4)));
            String doc  = str(hm.getValueAt(i, 10));
            if (weekSet.contains(date) && doc.equals(currentDoctor)) count++;
        }
        return count;
    }

    private Map<String, String[]> emergencyTimeMap() {
        Map<String, String[]> map = new LinkedHashMap<>();
        DefaultTableModel emg = doctorDataStore.get().getHistoryEmergencyModel();
        for (int i = 0; i < emg.getRowCount(); i++) {
            String norm = normalizeTime(str(emg.getValueAt(i, 4)));
            if (!map.containsKey(norm))
                map.put(norm, new String[]{
                    str(emg.getValueAt(i, 0)),
                    str(emg.getValueAt(i, 2)),
                    str(emg.getValueAt(i, 3))
                });
        }
        return map;
    }

    // ═══════════════════════════════════════════════════════════════
    //  REFRESH
    // ═══════════════════════════════════════════════════════════════
    private void refreshAll() {
        try {
            int booked = countBooked();
            int free   = countFree();

            if (bannerBookedLbl  != null) bannerBookedLbl.setText(String.valueOf(booked));
            if (bannerFreeLbl    != null) bannerFreeLbl.setText(String.valueOf(free));
            if (cardBookedVal    != null) cardBookedVal.setText(String.valueOf(booked));
            if (cardFreeVal      != null) cardFreeVal.setText(String.valueOf(free));

            buildSlots();
            if (weeklyCardsPanel != null) rebuildWeeklyStatCards();
            if (weeklyModel != null) rebuildWeeklyModel();

            revalidate();
            repaint();
        } catch (Exception e) {
            System.out.println("[SchedulePanel] refreshAll error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void rebuildWeeklyStatCards() {
        weeklyCardsPanel.removeAll();
        int wb = countWeeklyBooked();
        weeklyCardsPanel.add(statCard("This Week",      bigLbl(String.valueOf(wb)),
                                       C_BLUE,   "appointments"));
        weeklyCardsPanel.add(statCard("Free Slots",     bigLbl(String.valueOf(Math.max(0, 60-wb))),
                                       C_GREEN,  "available"));
        weeklyCardsPanel.add(statCard("Total Patients", bigLbl(String.valueOf(doctorDataStore.get().getTotalPatients())),
                                       C_PURPLE, "registered"));
        weeklyCardsPanel.revalidate();
        weeklyCardsPanel.repaint();
    }

    // ═══════════════════════════════════════════════════════════════
    //  HEADER  (Daily + Weekly tabs only)
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildHeader() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);

        JPanel banner = new GradientPanel(NAVY, NAVY2);
        banner.setLayout(new BorderLayout());
        banner.setPreferredSize(new Dimension(0, 88));
        banner.setBorder(new EmptyBorder(16, 28, 12, 28));

        JLabel title = styledLabel("My Schedule", Font.BOLD, 26, WHITE);
        JLabel sub   = styledLabel(
            new SimpleDateFormat("EEEE, MMMM dd, yyyy").format(new Date()),
            Font.PLAIN, 13, new Color(180, 205, 240));
        JPanel textCol = new JPanel(new GridLayout(2, 1, 0, 3));
        textCol.setOpaque(false);
        textCol.add(title); textCol.add(sub);
        banner.add(textCol, BorderLayout.WEST);

        bannerBookedLbl = styledLabel(String.valueOf(countBooked()), Font.BOLD, 20, WHITE);
        bannerFreeLbl   = styledLabel(String.valueOf(countFree()),   Font.BOLD, 20, WHITE);

        JPanel miniStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        miniStats.setOpaque(false);
        miniStats.add(miniStatPanel("Booked Today", bannerBookedLbl));
        miniStats.add(miniStatPanel("Free Slots",   bannerFreeLbl));
        banner.add(miniStats, BorderLayout.EAST);
        outer.add(banner, BorderLayout.NORTH);

        // ── Tab bar: Daily + Weekly only ──────────────────────────
        JPanel tabBar = new JPanel(new BorderLayout());
        tabBar.setBackground(WHITE);
        tabBar.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));

        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        tabs.setOpaque(false);
        tabs.setBorder(new EmptyBorder(0, 24, 0, 0));

        btnDaily  = tabBtn("Daily",  true);
        btnWeekly = tabBtn("Weekly", false);
        btnDaily.addActionListener(e  -> switchTab("Daily"));
        btnWeekly.addActionListener(e -> switchTab("Weekly"));

        tabs.add(btnDaily);
        tabs.add(btnWeekly);
        tabBar.add(tabs, BorderLayout.WEST);
        outer.add(tabBar, BorderLayout.CENTER);
        return outer;
    }

    private JPanel miniStatPanel(String label, JLabel valueLabel) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 2));
        p.setOpaque(false);
        p.add(valueLabel);
        p.add(styledLabel(label, Font.PLAIN, 10, new Color(180, 205, 240)));
        return p;
    }

    private JButton tabBtn(String label, boolean active) {
        JButton btn = new JButton(label);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setContentAreaFilled(false); btn.setOpaque(true);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(110, 44));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        styleTab(btn, active);
        return btn;
    }

    private void styleTab(JButton b, boolean active) {
        b.setBackground(WHITE);
        b.setForeground(active ? NAVY : TEXT_MID);
        b.setBorder(active
            ? new MatteBorder(0, 0, 3, 0, NAVY)
            : new EmptyBorder(0, 0, 3, 0));
    }

    private void switchTab(String view) {
        styleTab(btnDaily,  "Daily".equals(view));
        styleTab(btnWeekly, "Weekly".equals(view));
        cardLayout.show(mainPanel, view);
    }

    // ═══════════════════════════════════════════════════════════════
    //  DAILY VIEW
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildDailyView() {
        JPanel panel = new JPanel(new BorderLayout(0, 0));
        panel.setOpaque(false);
        panel.add(buildStatCards(), BorderLayout.NORTH);

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(4, 0, 10, 0));
        header.add(styledLabel("Today's Schedule", Font.BOLD, 15, TEXT_DARK), BorderLayout.WEST);

        JPanel legend = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        legend.setOpaque(false);
        legend.add(legendDot(BOOKED_LEFT, "Booked"));
        legend.add(legendDot(FREE_LEFT,   "Available"));
        legend.add(legendDot(EMERG_LEFT,  "Emergency"));
        header.add(legend, BorderLayout.EAST);

        slotContainer = new JPanel();
        slotContainer.setLayout(new BoxLayout(slotContainer, BoxLayout.Y_AXIS));
        slotContainer.setOpaque(false);
        buildSlots();

        JScrollPane scroll = new JScrollPane(slotContainer);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        scroll.setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(header, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatCards() {
        JPanel p = new JPanel(new GridLayout(1, 2, 14, 0));
        p.setOpaque(false);
        p.setBorder(new EmptyBorder(20, 0, 16, 0));

        cardBookedVal = bigLbl(String.valueOf(countBooked()));
        cardFreeVal   = bigLbl(String.valueOf(countFree()));

        p.add(statCard("Booked Today", cardBookedVal, C_BLUE,  "appointments"));
        p.add(statCard("Free Slots",   cardFreeVal,   C_GREEN, "available now"));
        return p;
    }

    // ═══════════════════════════════════════════════════════════════
    //  SLOT GRID
    // ═══════════════════════════════════════════════════════════════
    private void buildSlots() {
        slotContainer.removeAll();

        Map<String, String[]> booked = new LinkedHashMap<>();
        // Use getSlotAppointments() — already filtered to today + valid slots
        java.util.List<Object[]> todayAppts = doctorDataStore.get().getSlotAppointments();
        DefaultTableModel hist = doctorDataStore.get().getHistoryAppointmentModel();

        // Build time->historyRow lookup for rich type/notes details
        Map<String, Integer> histByTime = new LinkedHashMap<>();
        for (int i = 0; i < hist.getRowCount(); i++) {
            String t = normalizeTime(str(hist.getValueAt(i, 5)));
            if (!histByTime.containsKey(t)) histByTime.put(t, i);
        }

        for (Object[] row : todayAppts) {
            String t = normalizeTime(str(row[1]));
            if (booked.containsKey(t)) continue;
            String name = str(row[0]);
            String date = str(row[2]);
            Integer hi = histByTime.get(t);
            String type  = hi != null ? str(hist.getValueAt(hi, 3)) : "Appointment";
            String notes = hi != null ? str(hist.getValueAt(hi, 6)) : "";
            booked.put(t, new String[]{name, type, notes, date});
        }
        Map<String, String[]> emergency = emergencyTimeMap();

        for (String time : getSlotTimes()) {
            SlotRow row;
            if (booked.containsKey(time)) {
                row = new SlotRow(time, SlotRow.Kind.BOOKED, booked.get(time), null);
            } else if (emergency.containsKey(time)) {
                row = new SlotRow(time, SlotRow.Kind.EMERGENCY, emergency.get(time), null);
            } else {
                row = new SlotRow(time, SlotRow.Kind.FREE, null, this::onBookSlot);
            }
            slotContainer.add(row);
            slotContainer.add(Box.createVerticalStrut(6));
        }
        slotContainer.revalidate();
        slotContainer.repaint();
    }

    // ═══════════════════════════════════════════════════════════════
    //  EDIT APPOINTMENT DIALOG
    // ═══════════════════════════════════════════════════════════════
    private void openEditAppointmentDialog(String time, String[] data) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Edit Appointment — " + time, true);
        dialog.setSize(460, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(WHITE);
        content.setBorder(new EmptyBorder(28, 36, 28, 36));

        JLabel head    = styledLabel("Edit Appointment", Font.BOLD, 19, TEXT_DARK);
        JLabel timeLbl = styledLabel("Time Slot:  " + time, Font.BOLD, 13, BOOKED_LEFT);
        head.setAlignmentX(LEFT_ALIGNMENT);
        timeLbl.setAlignmentX(LEFT_ALIGNMENT);

        // ── Fields ──────────────────────────────────────────────
        JTextField nameF      = dialogField(); nameF.setText(data[0]);
        JTextField contactF   = dialogField();
        JTextField dateF      = dialogField();
        JTextField diagnosisF = dialogField();
        JTextField medsF      = dialogField();
        JTextField allergiesF = dialogField();
        JTextField bpF        = dialogField();
        JTextField notesF     = dialogField(); notesF.setText(data[2]);

        // Pre-fill medical history
        Map<String, String> mh = doctorDataStore.get().getMedicalHistory(data[0]);
        diagnosisF.setText(mh.getOrDefault("Diagnosis",   ""));
        medsF.setText(     mh.getOrDefault("Medications", ""));
        allergiesF.setText(mh.getOrDefault("Allergies",   ""));
        bpF.setText(       mh.getOrDefault("BP",          ""));

        // Pre-fill date from data[3] if available, otherwise from appointment model
        if (data.length > 3 && !data[3].isEmpty() && !data[3].equals("—")) {
            dateF.setText(data[3]);
        } else {
            DefaultTableModel am0 = doctorDataStore.get().getAppointmentModel();
            for (int i = 0; i < am0.getRowCount(); i++) {
                if (normalizeTime(str(am0.getValueAt(i, 1))).equals(time)
                        && str(am0.getValueAt(i, 0)).equals(data[0])) {
                    dateF.setText(str(am0.getValueAt(i, 2))); break;
                }
            }
        }

        String[] types = {"Consultation","Follow-up","Check-up","New Patient","Procedure"};
        JComboBox<String> typeCB = new JComboBox<>(types);
        typeCB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        typeCB.setAlignmentX(LEFT_ALIGNMENT);
        typeCB.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        for (int i = 0; i < types.length; i++)
            if (types[i].equals(data[1])) { typeCB.setSelectedIndex(i); break; }

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(NAVY); saveBtn.setForeground(WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setBorder(new EmptyBorder(10, 0, 10, 0));

        saveBtn.addActionListener(e -> {
            String name = nameF.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Patient name is required."); return;
            }
            doctorDataStore ds = doctorDataStore.get();

            // Update historyAppointmentModel
            DefaultTableModel hm = ds.getHistoryAppointmentModel();
            for (int i = 0; i < hm.getRowCount(); i++) {
                if (normalizeTime(str(hm.getValueAt(i, 5))).equals(time)
                        && str(hm.getValueAt(i, 0)).equals(data[0])) {
                    hm.setValueAt(name,                       i, 0);
                    hm.setValueAt(typeCB.getSelectedItem(),   i, 3);
                    hm.setValueAt(dateF.getText().trim(),     i, 4);
                    hm.setValueAt(notesF.getText().trim(),    i, 6);
                    hm.setValueAt(diagnosisF.getText().trim(),i, 7);
                    hm.setValueAt(medsF.getText().trim(),     i, 8);
                    hm.setValueAt(allergiesF.getText().trim(),i, 9);
                    break;
                }
            }

            // Update appointmentModel
            DefaultTableModel am = ds.getAppointmentModel();
            for (int i = 0; i < am.getRowCount(); i++) {
                if (normalizeTime(str(am.getValueAt(i, 1))).equals(time)
                        && str(am.getValueAt(i, 0)).equals(data[0])) {
                    am.setValueAt(name,                   i, 0);
                    am.setValueAt(dateF.getText().trim(), i, 2);
                    break;
                }
            }

            // Update medical history
            ds.updateMedicalHistory(name, "Diagnosis",   diagnosisF.getText().trim());
            ds.updateMedicalHistory(name, "Medications", medsF.getText().trim());
            ds.updateMedicalHistory(name, "Allergies",   allergiesF.getText().trim());
            ds.updateMedicalHistory(name, "BP",          bpF.getText().trim());
            // Mark appointment as Completed so patient no longer sees it in update/cancel/upcoming
            String saveDate = dateF.getText().trim();
            if (!saveDate.isEmpty()) {
                ds.updatePatientAppointmentStatus(name, saveDate, time, "Completed");
            }
            ds.notifyListeners();

            JOptionPane.showMessageDialog(dialog,
                "Appointment updated successfully.", "Saved",
                JOptionPane.INFORMATION_MESSAGE);
            dialog.dispose();
        });

        // Layout
        content.add(head); content.add(Box.createVerticalStrut(4));
        content.add(timeLbl); content.add(Box.createVerticalStrut(18));

        sectionLabel(content, "APPOINTMENT DETAILS");
        addField(content, "PATIENT NAME",       nameF);
        addField(content, "CONTACT",            contactF);
        addField(content, "DATE (yyyy-MM-dd)",  dateF);
        JLabel tl = styledLabel("APPOINTMENT TYPE", Font.BOLD, 10, TEXT_MID);
        tl.setAlignmentX(LEFT_ALIGNMENT);
        content.add(tl); content.add(Box.createVerticalStrut(4));
        content.add(typeCB); content.add(Box.createVerticalStrut(10));
        addField(content, "NOTES", notesF);

        content.add(Box.createVerticalStrut(6));
        sectionLabel(content, "MEDICAL HISTORY");
        addField(content, "DIAGNOSIS",      diagnosisF);
        addField(content, "MEDICATIONS",    medsF);
        addField(content, "ALLERGIES",      allergiesF);
        addField(content, "BLOOD PRESSURE", bpF);

        content.add(Box.createVerticalStrut(10));
        content.add(saveBtn);

        content.add(Box.createVerticalStrut(8));
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actionRow.setOpaque(false);
        actionRow.setAlignmentX(LEFT_ALIGNMENT);
        actionRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton acceptBtn = new JButton("Accept Appointment");
        acceptBtn.setBackground(new Color(16, 185, 129));
        acceptBtn.setForeground(WHITE);
        acceptBtn.setFocusPainted(false);
        acceptBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        acceptBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        acceptBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton rejectBtn = new JButton("Reject Appointment");
        rejectBtn.setBackground(C_RED);
        rejectBtn.setForeground(WHITE);
        rejectBtn.setFocusPainted(false);
        rejectBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rejectBtn.setBorder(new EmptyBorder(8, 16, 8, 16));
        rejectBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        final String fPatient = data[0];
        final String fTime = time;
        String fDate = dateF.getText().trim();
        if (fDate.isEmpty()) {
            DefaultTableModel amLookup = doctorDataStore.get().getAppointmentModel();
            for (int i = 0; i < amLookup.getRowCount(); i++) {
                if (normalizeTime(str(amLookup.getValueAt(i, 1))).equals(time)
                        && str(amLookup.getValueAt(i, 0)).equals(fPatient)) {
                    fDate = str(amLookup.getValueAt(i, 2)); break;
                }
            }
        }
        final String apptDate = fDate;

        boolean alreadyAccepted = doctorDataStore.get().isAccepted(fPatient, apptDate, fTime);
        if (alreadyAccepted) {
            JLabel acceptedLbl = new JLabel("  Accepted  ");
            acceptedLbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
            acceptedLbl.setForeground(new Color(16, 185, 129));
            acceptedLbl.setAlignmentX(LEFT_ALIGNMENT);
            JPanel acceptedRow = new JPanel(new FlowLayout(FlowLayout.CENTER));
            acceptedRow.setOpaque(false);
            acceptedRow.add(acceptedLbl);
            content.add(acceptedRow);
        } else {
            acceptBtn.addActionListener(e -> {
                doctorDataStore ds = doctorDataStore.get();
                ds.markAccepted(fPatient, apptDate, fTime);
                JOptionPane.showMessageDialog(dialog,
                    "Appointment ACCEPTED. Patient will be notified.",
                    "Accepted", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            });

            rejectBtn.addActionListener(e -> {
                int confirm = JOptionPane.showConfirmDialog(dialog,
                    "Reject appointment for " + fPatient + " at " + fTime + "?",
                    "Confirm Rejection", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    doctorDataStore ds = doctorDataStore.get();
                    ds.updatePatientAppointmentStatus(fPatient, apptDate, fTime, "Rejected");
                    removeAppointment(fTime, new String[]{fPatient, "", ""});
                    JOptionPane.showMessageDialog(dialog,
                        "Appointment REJECTED. Patient will be notified.",
                        "Rejected", JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                }
            });

            actionRow.add(acceptBtn);
            actionRow.add(rejectBtn);
            content.add(actionRow);
        }

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dialog.add(sp);
        dialog.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════
    //  REMOVE APPOINTMENT
    // ═══════════════════════════════════════════════════════════════
    private void removeAppointment(String time, String[] data) {
        doctorDataStore ds = doctorDataStore.get();
        String appDate = data.length > 3 ? data[3] : null;
        String normAppDate = appDate != null ? normalizeDateYmd(appDate) : null;
        DefaultTableModel am = ds.getAppointmentModel();
        for (int i = am.getRowCount()-1; i >= 0; i--)
            if (normalizeTime(str(am.getValueAt(i,1))).equals(time)
                    && str(am.getValueAt(i,0)).equals(data[0])
                    && (normAppDate == null || normalizeDateYmd(str(am.getValueAt(i,2))).equals(normAppDate)))
                { am.removeRow(i); break; }

        DefaultTableModel hm = ds.getHistoryAppointmentModel();
        for (int i = hm.getRowCount()-1; i >= 0; i--)
            if (normalizeTime(str(hm.getValueAt(i,5))).equals(time)
                    && str(hm.getValueAt(i,0)).equals(data[0])
                    && (normAppDate == null || normalizeDateYmd(str(hm.getValueAt(i,4))).equals(normAppDate)))
                { hm.removeRow(i); break; }

        // ── Cross-system sync: free up the slot in HospitalSystem shared tracker
        try {
            String dateForClear = (appDate != null && !appDate.isEmpty()) ? appDate
                : new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            HospitalSystem.clearSlot(ds.getDoctorDisplayName(), dateForClear, time);
        } catch (Exception ex) { }
        ds.notifyListeners();
    }

    // ═══════════════════════════════════════════════════════════════
    //  BOOK SLOT DIALOG
    // ═══════════════════════════════════════════════════════════════
    private void onBookSlot(String time) {
        JDialog dialog = new JDialog(
            (Frame) SwingUtilities.getWindowAncestor(this),
            "Book Slot — " + time, true);
        dialog.setSize(420, 620);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(WHITE);
        content.setBorder(new EmptyBorder(28, 36, 28, 36));

        JLabel head    = styledLabel("Book Appointment", Font.BOLD, 19, TEXT_DARK);
        JLabel timeLbl = styledLabel("Time: " + time,   Font.BOLD, 13, C_BLUE);
        head.setAlignmentX(LEFT_ALIGNMENT);
        timeLbl.setAlignmentX(LEFT_ALIGNMENT);

        JTextField nameF    = dialogField();
        JTextField dateF    = dialogField();
        dateF.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField ageF     = dialogField();
        JTextField genderF  = dialogField();
        JTextField contactF = dialogField();
        JTextField bloodF   = dialogField();
        JTextField notesF   = dialogField();

        String[] types = {"Consultation","Follow-up","Check-up","New Patient","Procedure"};
        JComboBox<String> typeCB = new JComboBox<>(types);
        typeCB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        typeCB.setAlignmentX(LEFT_ALIGNMENT);
        typeCB.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JButton saveBtn = new JButton("Confirm Booking");
        saveBtn.setBackground(FREE_LEFT); saveBtn.setForeground(WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        saveBtn.setAlignmentX(LEFT_ALIGNMENT);
        saveBtn.setBorder(new EmptyBorder(10, 0, 10, 0));

        saveBtn.addActionListener(e -> {
            String name    = nameF.getText().trim();
            String date    = dateF.getText().trim();
            String age     = ageF.getText().trim();
            String gender  = genderF.getText().trim();
            String contact = contactF.getText().trim();
            String blood   = bloodF.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Patient name is required."); return;
            }
            doctorDataStore ds = doctorDataStore.get();
            ds.addAppointment(name, time, date);
            ds.getHistoryAppointmentModel().addRow(new Object[]{
                name, age.isEmpty() ? "--" : age, contact,
                typeCB.getSelectedItem(), date, time,
                notesF.getText().trim(), "", "", "",
                ds.getDoctorDisplayName()
            });
            ds.ensureInOverall(
                name,
                age.isEmpty()    ? "--" : age,
                gender.isEmpty() ? "--" : gender,
                contact.isEmpty()? "--" : contact,
                blood.isEmpty()  ? "--" : blood
            );
            // ── Cross-system sync: mark slot as booked in HospitalSystem shared
            // tracker so receptionist's SlotPickerPanel shows it as unavailable.
            try {
                HospitalSystem.markSlotBooked(ds.getDoctorDisplayName(), date, time);
            } catch (Exception ex) {
                System.out.println("[SchedulePanel] markSlotBooked: " + ex.getMessage());
            }
            ds.notifyListeners();
            dialog.dispose();
        });

        content.add(head); content.add(Box.createVerticalStrut(4));
        content.add(timeLbl); content.add(Box.createVerticalStrut(20));
        addField(content, "PATIENT NAME",      nameF);
        addField(content, "DATE (yyyy-MM-dd)", dateF);
        addField(content, "AGE",               ageF);
        addField(content, "GENDER",            genderF);
        addField(content, "CONTACT",           contactF);
        addField(content, "BLOOD GROUP",       bloodF);
        JLabel tl = styledLabel("APPOINTMENT TYPE", Font.BOLD, 10, TEXT_MID);
        tl.setAlignmentX(LEFT_ALIGNMENT);
        content.add(tl); content.add(Box.createVerticalStrut(4));
        content.add(typeCB); content.add(Box.createVerticalStrut(10));
        addField(content, "NOTES", notesF);
        content.add(Box.createVerticalStrut(6));
        content.add(saveBtn);

        JScrollPane sp = new JScrollPane(content); sp.setBorder(null);
        dialog.add(sp);
        dialog.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════════
    //  SLOT ROW — Edit + Delete buttons on BOOKED slots
    // ═══════════════════════════════════════════════════════════════
    private class SlotRow extends JPanel {
        enum Kind { BOOKED, FREE, EMERGENCY }

        SlotRow(String time, Kind kind, String[] data,
                java.util.function.Consumer<String> onBook) {
            setLayout(new BorderLayout());
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
            setPreferredSize(new Dimension(0, 110));
            setOpaque(false);

            Color leftBar = switch (kind) {
                case BOOKED    -> BOOKED_LEFT;
                case EMERGENCY -> EMERG_LEFT;
                default        -> FREE_LEFT;
            };
            Color bgColor = switch (kind) {
                case BOOKED    -> BOOKED_BG;
                case EMERGENCY -> EMERG_BG;
                default        -> FREE_BG;
            };
            final Color BG_F  = bgColor;
            final Color BAR_F = leftBar;

            JPanel card = new JPanel(new BorderLayout()) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(BG_F);
                    g2.fill(new RoundRectangle2D.Float(
                        0, 0, getWidth(), getHeight(), 12, 12));
                    g2.setColor(BAR_F);
                    g2.fill(new RoundRectangle2D.Float(
                        0, 0, 5, getHeight(), 4, 4));
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            card.setOpaque(false);

            // ── Time column ──────────────────────────────────────
            JPanel timeCol = new JPanel(new GridBagLayout());
            timeCol.setOpaque(false);
            timeCol.setPreferredSize(new Dimension(115, 0));
            JPanel timeStack = new JPanel(new GridLayout(2, 1, 0, 2));
            timeStack.setOpaque(false);
            timeStack.setBorder(new EmptyBorder(0, 16, 0, 8));
            timeStack.add(styledLabel(time, Font.BOLD, 13, leftBar));
            timeStack.add(styledLabel("- " + nextHour(time), Font.PLAIN, 11, TEXT_LIGHT));
            timeCol.add(timeStack);

            JSeparator sep = new JSeparator(JSeparator.VERTICAL);
            sep.setForeground(new Color(200, 210, 225));
            sep.setPreferredSize(new Dimension(1, 0));

            // ── Info column ──────────────────────────────────────
            JPanel infoCol = new JPanel(new GridBagLayout());
            infoCol.setOpaque(false);
            infoCol.setBorder(new EmptyBorder(0, 16, 0, 16));
            GridBagConstraints gc = new GridBagConstraints();
            gc.anchor = GridBagConstraints.WEST; gc.weightx = 1;
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.gridx = 0; gc.insets = new Insets(1, 0, 1, 0);
            gc.gridy = 0;

            switch (kind) {
                case BOOKED -> {
                    infoCol.add(styledLabel(data[0], Font.BOLD, 14, TEXT_DARK), gc);
                    gc.gridy++;
                    JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                    meta.setOpaque(false);
                    meta.add(pill(data[1], BOOKED_LEFT));
                    if (data[2] != null && !data[2].equals("—"))
                        meta.add(styledLabel(data[2], Font.PLAIN, 11, TEXT_MID));
                    infoCol.add(meta, gc);
                    gc.gridy++;
                    String fp2 = data[0];
                    String dateForSlot = data.length > 3 ? data[3] : new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                    doctorDataStore ds = doctorDataStore.get();
                    boolean alreadyAccepted = ds.isAccepted(fp2, dateForSlot, time)
                        || "Scheduled".equals(ds.getAppointmentStatus(fp2, dateForSlot, time))
                        || "Completed".equals(ds.getAppointmentStatus(fp2, dateForSlot, time));
                    if (alreadyAccepted) {
                        JLabel acceptedLbl = new JLabel("Accepted");
                        acceptedLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                        acceptedLbl.setForeground(new Color(16, 185, 129));
                        infoCol.add(acceptedLbl, gc);
                    } else {
                        JPanel actionRow2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
                        actionRow2.setOpaque(false);
                        JButton acceptBtn2 = new JButton("Accept");
                        acceptBtn2.setBackground(new Color(16, 185, 129));
                        acceptBtn2.setForeground(WHITE);
                        acceptBtn2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        acceptBtn2.setFocusPainted(false);
                        acceptBtn2.setBorder(new EmptyBorder(4, 10, 4, 10));
                        acceptBtn2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        acceptBtn2.addActionListener(e -> {
                            doctorDataStore ds2 = doctorDataStore.get();
                            ds2.markAccepted(fp2, dateForSlot, time);
                            JOptionPane.showMessageDialog(doctorSchedulePanel.this,
                                "Appointment ACCEPTED.", "Accepted", JOptionPane.INFORMATION_MESSAGE);
                            refreshAll();
                        });
                        JButton rejectBtn2 = new JButton("Reject");
                        rejectBtn2.setBackground(C_RED);
                        rejectBtn2.setForeground(WHITE);
                        rejectBtn2.setFont(new Font("Segoe UI", Font.BOLD, 10));
                        rejectBtn2.setFocusPainted(false);
                        rejectBtn2.setBorder(new EmptyBorder(4, 10, 4, 10));
                        rejectBtn2.setCursor(new Cursor(Cursor.HAND_CURSOR));
                        rejectBtn2.addActionListener(e -> {
                            int confirm = JOptionPane.showConfirmDialog(doctorSchedulePanel.this,
                                "Reject " + fp2 + " at " + time + "?", "Confirm",
                                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                            if (confirm == JOptionPane.YES_OPTION) {
                                doctorDataStore ds2 = doctorDataStore.get();
                                ds2.updatePatientAppointmentStatus(fp2, dateForSlot, time, "Rejected");
                                removeAppointment(time, new String[]{fp2, "", ""});
                                JOptionPane.showMessageDialog(doctorSchedulePanel.this,
                                    "Appointment REJECTED.", "Rejected", JOptionPane.INFORMATION_MESSAGE);
                                refreshAll();
                            }
                        });
                        actionRow2.add(acceptBtn2);
                        actionRow2.add(rejectBtn2);
                        infoCol.add(actionRow2, gc);
                    }
                }
                case EMERGENCY -> {
                    gc.gridy++;
                    JPanel meta = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
                    meta.setOpaque(false);
                    meta.add(pill("! " + data[2], EMERG_LEFT));
                    meta.add(styledLabel(data[1], Font.PLAIN, 11, EMERG_LEFT));
                    infoCol.add(meta, gc);
                }
                default -> {
                    infoCol.add(styledLabel(
                        "Available", Font.BOLD, 14, FREE_LEFT), gc);
                    gc.gridy++;
                    infoCol.add(styledLabel(
                        "Slot is open for booking", Font.PLAIN, 12, TEXT_MID), gc);
                }
            }

            // ── Right action panel ───────────────────────────────
            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
            right.setOpaque(false);
            right.setPreferredSize(new Dimension(200, 0));

            switch (kind) {
                case BOOKED -> {
                    JButton editBtn = new JButton("Edit");
                    editBtn.setBackground(BOOKED_LEFT);
                    editBtn.setForeground(WHITE);
                    editBtn.setFont(new Font("Segoe UI", Font.BOLD, 11));
                    editBtn.setFocusPainted(false);
                    editBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
                    editBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    editBtn.addActionListener(e ->
                        openEditAppointmentDialog(time, data));
                    right.add(editBtn);
                }
                case EMERGENCY ->
                    right.add(pill("Emergency", EMERG_LEFT));
                default -> {
                    JButton b = new JButton("+ Book Slot");
                    b.setBackground(FREE_LEFT); b.setForeground(WHITE);
                    b.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    b.setFocusPainted(false);
                    b.setBorder(new EmptyBorder(7, 16, 7, 16));
                    b.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    b.addActionListener(e -> onBook.accept(time));
                    right.add(b);
                }
            }

            card.add(timeCol,  BorderLayout.WEST);
            card.add(sep,      BorderLayout.AFTER_LINE_ENDS);
            card.add(infoCol,  BorderLayout.CENTER);
            card.add(right,    BorderLayout.EAST);
            card.setBorder(BorderFactory.createCompoundBorder(
                new RoundedShadowBorder(leftBar),
                new EmptyBorder(10, 0, 10, 12)));

            add(card, BorderLayout.CENTER);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  WEEKLY VIEW  — shows each appointment as its own row,
    //                 click row to edit, button to delete selected
    // ═══════════════════════════════════════════════════════════════
    private JPanel buildWeeklyView() {
        JPanel panel = new JPanel(new BorderLayout(0, 14));
        panel.setOpaque(false);

        // Stat cards
        weeklyCardsPanel = new JPanel(new GridLayout(1, 3, 14, 0));
        weeklyCardsPanel.setOpaque(false);
        weeklyCardsPanel.setBorder(new EmptyBorder(20, 0, 6, 0));
        rebuildWeeklyStatCards();
        panel.add(weeklyCardsPanel, BorderLayout.NORTH);

        // Table + controls
        JPanel wrapper = new JPanel(new BorderLayout(0, 8));
        wrapper.setOpaque(false);

        JPanel hdrRow = new JPanel(new BorderLayout());
        hdrRow.setOpaque(false);
        hdrRow.add(styledLabel("Weekly Overview", Font.BOLD, 15, TEXT_DARK),
                   BorderLayout.WEST);
        hdrRow.add(styledLabel(
            "Click a row to edit - Select then press Remove to delete",
            Font.ITALIC, 11, TEXT_MID), BorderLayout.EAST);
        wrapper.add(hdrRow, BorderLayout.NORTH);

        // Columns: Day, Date, Patient, Type, Time, Status
        String[] cols = {"Day","Date","Patient","Type","Time","Status"};
        weeklyModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        rebuildWeeklyModel();

        weeklyTable = buildTable(weeklyModel, 5);
        setColWidths(weeklyTable, 110, 110, 160, 130, 100, 110);

        // Double-click row → open edit dialog for that appointment
        weeklyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() < 2) return;
                int row = weeklyTable.getSelectedRow();
                if (row < 0) return;
                String patient  = str(weeklyModel.getValueAt(row, 2));
                String slotTime = str(weeklyModel.getValueAt(row, 4));
                String rowDate  = str(weeklyModel.getValueAt(row, 1));
                if (patient.equals("—") || patient.equals("Free")) return;

                // Try to find matching data from history model
                DefaultTableModel hist = doctorDataStore.get().getHistoryAppointmentModel();
                for (int i = 0; i < hist.getRowCount(); i++) {
                    if (str(hist.getValueAt(i,0)).equals(patient)
                            && normalizeTime(str(hist.getValueAt(i,5))).equals(slotTime)
                            && str(hist.getValueAt(i,4)).equals(rowDate)) {
                        String[] d = {
                            str(hist.getValueAt(i,0)),
                            str(hist.getValueAt(i,3)),
                            str(hist.getValueAt(i,6)),
                            str(hist.getValueAt(i,4))
                        };
                        openEditAppointmentDialog(slotTime, d);
                        weeklyTable.clearSelection();
                        return;
                    }
                }
                // Fallback: open dialog with what we have from the weekly table
                openEditAppointmentDialog(slotTime, new String[]{patient, "", "", rowDate});
                weeklyTable.clearSelection();
            }
        });

        wrapper.add(styledScroll(weeklyTable), BorderLayout.CENTER);

        // Remove-selected button
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        btnBar.setOpaque(false);
        JButton delBtn = new JButton("Remove Selected Appointment");
        delBtn.setBackground(C_RED); delBtn.setForeground(WHITE);
        delBtn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        delBtn.setFocusPainted(false);
        delBtn.setBorder(new EmptyBorder(8, 18, 8, 18));
        delBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        delBtn.addActionListener(e -> {
            int row = weeklyTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a row first."); return;
            }
            String patient  = str(weeklyModel.getValueAt(row, 2));
            String slotTime = str(weeklyModel.getValueAt(row, 4));
            String rowDate  = str(weeklyModel.getValueAt(row, 1));
            if (patient.equals("—") || patient.equals("Free")) {
                JOptionPane.showMessageDialog(this,
                    "That row has no appointment to remove."); return;
            }
            int ok = JOptionPane.showConfirmDialog(doctorSchedulePanel.this,
                "Remove appointment for " + patient + " at " + slotTime + " on " + rowDate + "?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (ok != JOptionPane.YES_OPTION) return;
            doctorDataStore.get().updatePatientAppointmentStatus(patient, rowDate, slotTime, "Cancelled");
            // Remove from models — try both the weekly date and the raw history date
            removeAppointment(slotTime, new String[]{patient, "", "", rowDate});
            // Also try by looking up the date from the history model
            DefaultTableModel hm = doctorDataStore.get().getHistoryAppointmentModel();
            for (int i = 0; i < hm.getRowCount(); i++) {
                if (str(hm.getValueAt(i,0)).equals(patient)
                        && normalizeTime(str(hm.getValueAt(i,5))).equals(slotTime)) {
                    removeAppointment(slotTime, new String[]{patient, "", "", str(hm.getValueAt(i,4))});
                    break;
                }
            }
            weeklyTable.clearSelection();
            refreshAll();
        });
        btnBar.add(delBtn);
        wrapper.add(btnBar, BorderLayout.SOUTH);

        panel.add(wrapper, BorderLayout.CENTER);
        return panel;
    }

    /** One row per appointment; free days show a single "Free" row. */
    private void rebuildWeeklyModel() {
        if (weeklyTable != null) weeklyTable.clearSelection();
        weeklyModel.setRowCount(0);

        String[] days  = {"Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"};
        String[] dates = weekDates();

        // Bucket appointments by date
        Map<String, java.util.List<Object[]>> byDate = new LinkedHashMap<>();
        for (String d : dates) byDate.put(d, new ArrayList<>());

        DefaultTableModel hm = doctorDataStore.get().getHistoryAppointmentModel();
        String currentDoctor = doctorDataStore.get().getDoctorDisplayName();
        for (int i = 0; i < hm.getRowCount(); i++) {
            String rawDate = str(hm.getValueAt(i, 4));
            String date = normalizeDateYmd(rawDate);
            String doc  = str(hm.getValueAt(i, 10));
            if (byDate.containsKey(date) && doc.equals(currentDoctor)) {
                String patientName = str(hm.getValueAt(i, 0));
                String apptTime = normalizeTime(str(hm.getValueAt(i, 5)));
                String statusKey = patientName + "|" + date + "|" + apptTime + "|" + currentDoctor;
                String aptStatus = doctorDataStore.get().patientAppointmentStatus.get(statusKey);
                if ("Completed".equals(aptStatus)) continue;
                byDate.get(date).add(new Object[]{
                    patientName,
                    str(hm.getValueAt(i, 3)),
                    apptTime
                });
            }
        }

        for (int i = 0; i < 7; i++) {
            java.util.List<Object[]> appts = byDate.get(dates[i]);
            if (appts == null || appts.isEmpty()) {
                weeklyModel.addRow(new Object[]{
                    days[i], dates[i], "—", "—", "—",
                    "Free"
                });
            } else {
                for (Object[] a : appts) {
                    weeklyModel.addRow(new Object[]{
                        days[i], dates[i], a[0], a[1], a[2], "Scheduled"
                    });
                }
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════
    private String str(Object o) { return o == null ? "—" : o.toString(); }

    private String[] getSlotTimes() {
        java.util.List<String> rosterSlots = DoctorRosterStore.getExpandedTimeSlotsByName(
            doctorDataStore.get().getDoctorDisplayName());
        if (!rosterSlots.isEmpty()) return rosterSlots.toArray(new String[0]);
        return SLOT_TIMES_FALLBACK;
    }

    /** Parse any common date format to yyyy-MM-dd for weekly view key matching */
    private String normalizeDateYmd(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String ymd = raw.replaceAll("(?i)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)\\w*", "$1");
        String[] formats = {"yyyy-MM-dd", "dd MMM yyyy", "dd MMMM yyyy", "MMM dd yyyy", "MM/dd/yyyy"};
        for (String fmt : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(fmt, java.util.Locale.ENGLISH);
                sdf.setLenient(false);
                return new java.text.SimpleDateFormat("yyyy-MM-dd").format(sdf.parse(raw));
            } catch (Exception ignored) {}
        }
        return raw;
    }

    private String normalizeTime(String raw) {
        if (raw == null || raw.equals("—")) return "";
        String[] slots = getSlotTimes();
        for (String s : slots) if (s.equals(raw)) return s;
        for (String s : slots)
            if (raw.length() >= 5 && s.startsWith(raw.substring(0, 5))) return s;
        for (String s : slots)
            if (s.contains(raw.replace("AM","").replace("PM","").trim())) return s;
        return raw;
    }

    private String nextHour(String time) {
        String[] slots = getSlotTimes();
        for (int i = 0; i < slots.length - 1; i++)
            if (slots[i].equals(time)) return slots[i + 1];
        return time;
    }

    private String[] weekDates() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String[] d = new String[7];
        for (int i = 0; i < 7; i++) {
            d[i] = sdf.format(cal.getTime());
            cal.add(Calendar.DAY_OF_WEEK, 1);
        }
        return d;
    }

    private void sectionLabel(JPanel p, String text) {
        JLabel l = styledLabel(text, Font.BOLD, 10, C_BLUE);
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setBorder(new EmptyBorder(8, 0, 4, 0));
        p.add(l);
        JSeparator sep = new JSeparator();
        sep.setForeground(DIVIDER);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sep.setAlignmentX(LEFT_ALIGNMENT);
        p.add(sep);
        p.add(Box.createVerticalStrut(6));
    }

    private JLabel styledLabel(String t, int style, int sz, Color fg) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", style, sz));
        l.setForeground(fg);
        return l;
    }

    private JLabel bigLbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Segoe UI", Font.BOLD, 32));
        l.setForeground(TEXT_DARK);
        return l;
    }

    private JPanel legendDot(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel dot = new JLabel("*");
        dot.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dot.setForeground(color);
        p.add(dot);
        p.add(styledLabel(label, Font.PLAIN, 12, TEXT_MID));
        return p;
    }

    private JLabel pill(String text, Color color) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(
                    color.getRed(), color.getGreen(), color.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), getHeight(), getHeight()));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(color);
        lbl.setOpaque(false);
        lbl.setBorder(new EmptyBorder(3, 9, 3, 9));
        return lbl;
    }

    private JTextField dialogField() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(DIVIDER, 1),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private void addField(JPanel p, String lbl, JTextField f) {
        JLabel l = styledLabel(lbl, Font.BOLD, 10, TEXT_MID);
        l.setAlignmentX(LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
    }

    private JPanel statCard(String title, JLabel val, Color accent, String sub) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(WHITE);
                g2.fill(new RoundRectangle2D.Float(
                    0, 0, getWidth(), getHeight(), 14, 14));
                g2.setColor(accent);
                g2.fill(new RoundRectangle2D.Float(
                    0, 0, 5, getHeight(), 4, 4));
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(), new EmptyBorder(16, 20, 14, 16)));
        val.setFont(new Font("Segoe UI", Font.BOLD, 32));
        val.setForeground(TEXT_DARK);
        card.add(styledLabel(title.toUpperCase(), Font.BOLD, 10, TEXT_MID),
                 BorderLayout.NORTH);
        card.add(val,  BorderLayout.CENTER);
        card.add(styledLabel(sub, Font.PLAIN, 11, accent), BorderLayout.SOUTH);
        return card;
    }

    private JTable buildTable(DefaultTableModel model, int statusCol) {
        JTable t = new JTable(model) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.setRowHeight(44);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setSelectionBackground(new Color(239, 246, 255));
        t.setSelectionForeground(NAVY);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader h = t.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 10));
        h.setBackground(new Color(248, 250, 252));
        h.setForeground(TEXT_MID);
        h.setPreferredSize(new Dimension(0, 38));
        h.setBorder(new MatteBorder(0, 0, 1, 0, DIVIDER));
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            { setHorizontalAlignment(LEFT); setOpaque(true); }
            @Override public Component getTableCellRendererComponent(
                    JTable tb, Object v, boolean s, boolean f, int r, int c) {
                setText(v != null ? v.toString().toUpperCase() : "");
                setFont(new Font("Segoe UI", Font.BOLD, 10));
                setForeground(TEXT_MID);
                setBackground(new Color(248, 250, 252));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                return this;
            }
        });

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tb, Object v, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(tb, v, sel, foc, row, col);
                setFont(new Font("Segoe UI",
                    col == 0 ? Font.BOLD : Font.PLAIN, 13));
                setForeground(sel ? NAVY : col == 0 ? TEXT_DARK : TEXT_MID);
                setBackground(sel ? new Color(239,246,255)
                    : row%2==0 ? WHITE : new Color(250,251,253));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                setHorizontalAlignment(LEFT);
                return this;
            }
        });

        if (statusCol >= 0)
            t.getColumnModel().getColumn(statusCol)
             .setCellRenderer(new PillRenderer());
        return t;
    }

    private void setColWidths(JTable t, int... widths) {
        for (int i = 0; i < widths.length && i < t.getColumnCount(); i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    private JScrollPane styledScroll(JTable t) {
        JScrollPane s = new JScrollPane(t);
        s.setBorder(BorderFactory.createCompoundBorder(
            new ShadowBorder(),
            BorderFactory.createLineBorder(DIVIDER, 1)));
        s.getViewport().setBackground(WHITE);
        s.setBackground(WHITE);
        s.getVerticalScrollBar().setUnitIncrement(16);
        return s;
    }

    // ── Pill status renderer ──────────────────────────────────────
    private class PillRenderer extends JLabel implements TableCellRenderer {
        PillRenderer() { setHorizontalAlignment(CENTER); setOpaque(false); }
        @Override public Component getTableCellRendererComponent(
                JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v != null ? v.toString() : ""); return this;
        }
        @Override protected void paintComponent(Graphics g) {
            String txt = getText();
            Color bg, fg;
            switch (txt) {
                case "Scheduled" -> { bg=new Color(237,233,254); fg=new Color(76,29,149); }
                case "Free"      -> { bg=new Color(220,252,231); fg=new Color(22,101,52); }
                case "Off"       -> { bg=new Color(241,245,249); fg=TEXT_MID; }
                default          -> { bg=new Color(219,234,254); fg=new Color(30,64,175); }
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            FontMetrics fm = g2.getFontMetrics(new Font("Segoe UI", Font.BOLD, 11));
            int tw=fm.stringWidth(txt), pw=tw+20, ph=22;
            int px=(getWidth()-pw)/2, py=(getHeight()-ph)/2;
            g2.setColor(bg);
            g2.fill(new RoundRectangle2D.Float(px, py, pw, ph, ph, ph));
            g2.setColor(fg);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(txt, px+10, py+ph/2+fm.getAscent()/2-1);
            g2.dispose();
        }
    }

    // ── Visual helpers ────────────────────────────────────────────
    private static class GradientPanel extends JPanel {
        final Color c1, c2;
        GradientPanel(Color c1, Color c2) { this.c1=c1; this.c2=c2; setOpaque(false); }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2));
            g2.fillRect(0,0,getWidth(),getHeight());
            g2.dispose(); super.paintComponent(g);
        }
    }

    private static class RoundedShadowBorder extends AbstractBorder {
        private final Color accent;
        RoundedShadowBorder(Color a) { this.accent = a; }
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(2,2,4,4);
        }
        @Override public void paintBorder(Component c, Graphics g,
                int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0,18));
            g2.fill(new RoundRectangle2D.Float(x+2,y+2,w-3,h-3,12,12));
            g2.setColor(new Color(
                accent.getRed(),accent.getGreen(),accent.getBlue(),40));
            g2.draw(new RoundRectangle2D.Float(x,y,w-3,h-4,12,12));
            g2.dispose();
        }
    }

    private static class ShadowBorder extends AbstractBorder {
        @Override public Insets getBorderInsets(Component c) {
            return new Insets(2,2,4,4);
        }
        @Override public void paintBorder(Component c, Graphics g,
                int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0,0,0,15));
            g2.fill(new RoundRectangle2D.Float(x+2,y+2,w-3,h-3,8,8));
            g2.dispose();
        }
    }
}