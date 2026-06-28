package Receptionist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import System.HospitalSystem;

public class HospitalAdmin extends JFrame {

    private static class Employee {
        private String id;
        private String name;
        public Employee(String id, String name) { this.id = id; this.name = name; }
        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    private static class Doctor extends Employee {
        private String department;
        private String roomNumber;
        private boolean isActive;
        private ArrayList<String> shiftSlots;
        public Doctor(String id, String name, String department, String roomNumber) {
            super(id, name); this.department = department; this.roomNumber = roomNumber;
            this.isActive = true; this.shiftSlots = new ArrayList<>();
        }
        public String getDepartment() { return department; }
        public String getRoomNumber() { return roomNumber; }
        public boolean isActive() { return isActive; }
        public ArrayList<String> getShiftSlots() { return shiftSlots; }
        public void setDepartment(String d) { this.department = d; }
        public void setRoomNumber(String r) { this.roomNumber = r; }
        public void toggleStatus() { this.isActive = !this.isActive; }
        public void addShiftSlot(String slot) { this.shiftSlots.add(slot); }
        public void removeShiftSlot(int i) { if(i>=0&&i<shiftSlots.size()) this.shiftSlots.remove(i); }
        public void updateShiftSlot(int i, String ns) { if(i>=0&&i<shiftSlots.size()) this.shiftSlots.set(i, ns); }
        public String getShiftsFormatted() {
            if (shiftSlots.isEmpty()) return "No Assigned Shifts";
            return String.join(" | ", shiftSlots);
        }
    }

    private static class Receptionist extends Employee {
        private String department;
        private String shift;
        public Receptionist(String id, String name, String department, String shift) {
            super(id, name); this.department = department; this.shift = shift;
        }
        public String getDepartment() { return department; }
        public String getShift() { return shift; }
        public void setDepartment(String d) { this.department = d; }
        public void setShift(String s) { this.shift = s; }
    }

    private static class Patient {
        private String id, name, disease, roomNumber;
        public Patient(String id, String name, String disease, String roomNumber) {
            this.id = id; this.name = name; this.disease = disease; this.roomNumber = roomNumber;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDisease() { return disease; }
        public String getRoomNumber() { return roomNumber; }
    }

    private static class Appointment {
        private String appointmentId, patientName, doctorName, timeSlot;
        public Appointment(String appointmentId, String patientName, String doctorName, String timeSlot) {
            this.appointmentId = appointmentId; this.patientName = patientName;
            this.doctorName = doctorName; this.timeSlot = timeSlot;
        }
        public String getDetails() {
            return "Appt ID: " + appointmentId + "  |  Patient: " + patientName + "  -->  Doctor: " + doctorName + "  |  Time: " + timeSlot;
        }
    }

    private static class EmergencyCase {
        private String caseId, patientName, severityLevel;
        private Doctor assignedDoctor;
        public EmergencyCase(String caseId, String patientName, String severityLevel, Doctor assignedDoctor) {
            this.caseId = caseId; this.patientName = patientName;
            this.severityLevel = severityLevel; this.assignedDoctor = assignedDoctor;
        }
        public String getDetails() {
            String deptName = (assignedDoctor != null) ? assignedDoctor.getDepartment() : "N/A";
            String docDisplay = (assignedDoctor != null) ? "Dr. " + assignedDoctor.getName() : "Unassigned";
            return "Emergency ID: " + caseId + "  |  Patient: " + patientName + "  |  Dept: [" + deptName + "] Specialist: " + docDisplay + "  |  Severity: [" + severityLevel + "]";
        }
        public String getSeverityLevel() { return severityLevel; }
        public void setSeverityLevel(String s) { this.severityLevel = s; }
        public Doctor getAssignedDoctor() { return assignedDoctor; }
        public void setAssignedDoctor(Doctor d) { this.assignedDoctor = d; }
    }

    // =========================================================================
    // 2. THE ADMIN WORKSPACE USER INTERFACE (RESTORED TO ORIGINAL COMPACT BOUNDS)
    // =========================================================================
    
    
    private ArrayList<Doctor> doctorList;
    private ArrayList<Receptionist> receptionistList;
    private ArrayList<Patient> patientList;
    private ArrayList<Appointment> appointmentList;
    private ArrayList<EmergencyCase> emergencyList;
    
    private JPanel displayPanel;
    private javax.swing.Timer refreshTimer;
    
    private final String[] DEPARTMENTS = {
        "Cardiology", "Electrophysiology", "Cardiac Imaging", "Interventional Cardiology", "Emergency Medicine"
    };

    // --- COLOR THEME CONFIGURATION ---
    private final Color COLOR_BG_DARK     = new Color(10, 22, 47);    
    private final Color COLOR_CARD_NAVY   = new Color(19, 31, 62);    
    private final Color COLOR_INPUT_BOX   = new Color(27, 43, 83);    
    private final Color COLOR_ACCENT_BLUE = new Color(0, 145, 255);   
    private final Color COLOR_HOVER_BLUE  = new Color(0, 115, 215);
    private final Color COLOR_BUTTON_GRAY = new Color(83, 97, 127);   
    private final Color COLOR_HOVER_GRAY  = new Color(120, 135, 165); 
    private final Color COLOR_DELETE_RED  = new Color(220, 53, 69);   
    private final Color COLOR_TEXT_WHITE  = new Color(255, 255, 255);    
    private final Color COLOR_TEXT_MUTED  = new Color(160, 175, 200); 
    
    // --- STYLISH TYPOGRAPHY ---
    private final Font FONT_CLASSY_TITLE = new Font("Georgia", Font.BOLD, 16);
    private final Font FONT_CLASSY_LABEL = new Font("Georgia", Font.BOLD, 13);
    private final Font FONT_MAIN         = new Font("Georgia", Font.PLAIN, 14);

    public HospitalAdmin(ArrayList<Doctor> sharedDoctors, 
                         ArrayList<Receptionist> sharedReceptionists,
                         ArrayList<Patient> sharedPatients, 
                         ArrayList<Appointment> sharedAppointments,
                         ArrayList<EmergencyCase> sharedEmergencies) {
        
        this.doctorList = sharedDoctors;
        this.receptionistList = sharedReceptionists;
        this.patientList = sharedPatients;
        this.appointmentList = sharedAppointments;
        this.emergencyList = sharedEmergencies;

        setTitle("Smart Healthcare System - Master Admin Hub");
        setSize(1100, 700); // CRITICAL FIX: Restored exact original window sizing bounds
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setLocationRelativeTo(null); 

        getContentPane().setBackground(COLOR_BG_DARK);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setDividerLocation(270); 
        splitPane.setEnabled(false); 
        splitPane.setBorder(null); 

        splitPane.setDividerSize(1); 
        splitPane.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                javax.swing.plaf.basic.BasicSplitPaneDivider divider = super.createDefaultDivider();
                divider.setBorder(null); 
                divider.setBackground(COLOR_INPUT_BOX); 
                return divider;
            }
        }); 

        // --- SIDEBAR DESIGN ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setBackground(COLOR_CARD_NAVY);
        sidebarPanel.setLayout(new GridLayout(8, 1, 0, 10)); 
        sidebarPanel.setBorder(new EmptyBorder(20, 15, 20, 15));

        JLabel lblSidebarTitle = new JLabel("ADMIN PORTAL", SwingConstants.CENTER);
        lblSidebarTitle.setFont(FONT_CLASSY_TITLE);
        lblSidebarTitle.setForeground(COLOR_TEXT_WHITE);
        sidebarPanel.add(lblSidebarTitle);

        JButton btnManageDocs = createAnimatedButton("Manage Doctors Roster", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnManageReceps = createAnimatedButton("Manage Receptionists", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnViewPatients = createAnimatedButton("Patient Profiles", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnViewAppts = createAnimatedButton("Live Appointment Logs", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnViewEmerg = createAnimatedButton("Emergency Cases", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnReport = createAnimatedButton("SYSTEM INSIGHT REPORT", COLOR_ACCENT_BLUE, COLOR_HOVER_BLUE);
        JButton btnLogout = createAnimatedButton("Close Workspace", COLOR_BG_DARK, COLOR_INPUT_BOX);

        sidebarPanel.add(btnManageDocs);
        sidebarPanel.add(btnManageReceps);
        sidebarPanel.add(btnViewPatients);
        sidebarPanel.add(btnViewAppts);
        sidebarPanel.add(btnViewEmerg);
        sidebarPanel.add(btnReport);
        sidebarPanel.add(btnLogout);

        // --- MAIN APPLICATION VIEWPORT ---
        displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout());
        displayPanel.setBackground(COLOR_BG_DARK);
        displayPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        splitPane.setLeftComponent(sidebarPanel);
        splitPane.setRightComponent(displayPanel);
        add(splitPane);

        // --- ACTION BINDINGS ---
        btnManageDocs.addActionListener(e -> showDoctorsManagementDashboard());
        btnManageReceps.addActionListener(e -> showReceptionistManagementDashboard());
        btnViewPatients.addActionListener(e -> showPatientsListLookup());
        btnViewAppts.addActionListener(e -> showMasterAppointmentSchedule());
        btnViewEmerg.addActionListener(e -> showActiveEmergencies());
        btnReport.addActionListener(e -> showComprehensiveInsightReport());
        btnLogout.addActionListener(e -> dispose()); 

        showComprehensiveInsightReport();
    }

    private JButton createAnimatedButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text);
        button.setFont(FONT_CLASSY_LABEL); 
        button.setForeground(COLOR_TEXT_WHITE);
        button.setBackground(baseColor);
        button.setFocusPainted(false);
        button.setBorder(null); 
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(hoverColor); }
            @Override public void mouseExited(MouseEvent e) { button.setBackground(baseColor); }
        });
        return button;
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(FONT_MAIN); 
        field.setForeground(COLOR_TEXT_WHITE); 
        field.setBackground(COLOR_INPUT_BOX);
        field.setCaretColor(COLOR_TEXT_WHITE); 
        field.setBorder(BorderFactory.createCompoundBorder(new LineBorder(COLOR_INPUT_BOX, 1), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return field;
    }

    private JScrollPane createCleanScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(COLOR_INPUT_BOX);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = COLOR_BUTTON_GRAY;
                this.trackColor = COLOR_INPUT_BOX;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroSizeButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroSizeButton(); }
            private JButton createZeroSizeButton() {
                JButton jb = new JButton(); jb.setPreferredSize(new Dimension(0,0)); return jb;
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        return scrollPane;
    }

    private JPanel createDepartmentBox(String deptName, JComponent listComponent) {
        JPanel box = new JPanel(new BorderLayout());
        box.setBackground(COLOR_CARD_NAVY);
        box.setBorder(BorderFactory.createTitledBorder(
            new LineBorder(COLOR_INPUT_BOX, 1, true),
            deptName.toUpperCase(),
            TitledBorder.LEFT,
            TitledBorder.TOP,
            FONT_CLASSY_LABEL.deriveFont(11.0f),
            COLOR_ACCENT_BLUE
        ));
        box.add(listComponent, BorderLayout.CENTER);
        return box;
    }

    // =========================================================================
    //                        DOCTOR VIEWS (MODULAR DEPT BOXES)
    // =========================================================================
    private void showDoctorsManagementDashboard() {
        displayPanel.removeAll();

        JPanel gridScrollContainer = new JPanel(new GridLayout(5, 1, 0, 10));
        gridScrollContainer.setBackground(COLOR_BG_DARK);

        ArrayList<JList<String>> allLists = new ArrayList<>();
        ArrayList<ArrayList<Doctor>> doctorsPerDept = new ArrayList<>();

        for (String dept : DEPARTMENTS) {
            DefaultListModel<String> model = new DefaultListModel<>();
            ArrayList<Doctor> subList = new ArrayList<>();
            
            for (Doctor doc : doctorList) {
                if (doc.getDepartment().equalsIgnoreCase(dept)) {
                    String status = doc.isActive() ? "ACTIVE" : "ON LEAVE";
                    model.addElement(" [ID: " + doc.getId() + "] Dr. " + doc.getName() + "  |  Shifts: [" + doc.getShiftsFormatted() + "]  |  " + status + "  |  " + doc.getRoomNumber());
                    subList.add(doc);
                }
            }
            
            JList<String> jList = new JList<>(model);
            jList.setFont(FONT_MAIN.deriveFont(13.0f)); jList.setForeground(COLOR_TEXT_WHITE); jList.setBackground(COLOR_INPUT_BOX);
            jList.setSelectionBackground(COLOR_ACCENT_BLUE);
            allLists.add(jList);
            doctorsPerDept.add(subList);

            jList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && jList.getSelectedIndex() != -1) {
                    for (JList<String> otherList : allLists) {
                        if (otherList != jList) otherList.clearSelection();
                    }
                }
            });

            JScrollPane scroll = createCleanScrollPane(jList);
            scroll.setPreferredSize(new Dimension(0, 90));
            JPanel deptBox = createDepartmentBox(dept, scroll);
            gridScrollContainer.add(deptBox);
        }

        JScrollPane mainScroll = createCleanScrollPane(gridScrollContainer);

        JPanel controlDeck = new JPanel(new GridLayout(1, 5, 8, 0));
        controlDeck.setBackground(COLOR_BG_DARK);
        controlDeck.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnHire = createAnimatedButton("HIRE NEW DOCTOR", COLOR_ACCENT_BLUE, COLOR_HOVER_BLUE);
        JButton btnShifts = createAnimatedButton("MANAGE SHIFTS", COLOR_ACCENT_BLUE, COLOR_HOVER_BLUE);
        JButton btnToggle = createAnimatedButton("TOGGLE LEAVE STATUS", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnUpdate = createAnimatedButton("EDIT DATA", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnFire = createAnimatedButton("FIRE DOCTOR", COLOR_DELETE_RED, COLOR_DELETE_RED.darker());

        Font smallerBtnFont = FONT_CLASSY_LABEL.deriveFont(11.0f);
        btnHire.setFont(smallerBtnFont); btnShifts.setFont(smallerBtnFont); btnToggle.setFont(smallerBtnFont); btnUpdate.setFont(smallerBtnFont); btnFire.setFont(smallerBtnFont);

        controlDeck.add(btnHire); controlDeck.add(btnShifts); controlDeck.add(btnToggle); controlDeck.add(btnUpdate); controlDeck.add(btnFire);

        btnShifts.addActionListener(e -> {
            Doctor targetDoc = null;
            for (int i = 0; i < allLists.size(); i++) {
                int sIdx = allLists.get(i).getSelectedIndex();
                if (sIdx != -1) targetDoc = doctorsPerDept.get(i).get(sIdx);
            }
            if (targetDoc == null) {
                JOptionPane.showMessageDialog(this, "Please select a doctor from one of the department boxes.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String[] shiftOptions = {"Allot New Time Slot", "Change Existing Slot", "Delete Time Slot"};
            String selectedAction = (String) JOptionPane.showInputDialog(this, "Shift Controls for Dr. " + targetDoc.getName() + ":", "Shift Hub", JOptionPane.QUESTION_MESSAGE, null, shiftOptions, shiftOptions[0]);
            if (selectedAction != null) {
                if (selectedAction.equals("Allot New Time Slot")) {
                    String newSlot = JOptionPane.showInputDialog(this, "Enter New Time Slot Window:");
                    if (newSlot != null && !newSlot.trim().isEmpty()) targetDoc.addShiftSlot(newSlot.trim());
                } else if (selectedAction.equals("Change Existing Slot")) {
                    if (targetDoc.getShiftSlots().isEmpty()) return;
                    String[] slots = targetDoc.getShiftSlots().toArray(new String[0]);
                    String oldSlot = (String) JOptionPane.showInputDialog(this, "Select Target Shift:", "Modify", JOptionPane.QUESTION_MESSAGE, null, slots, slots[0]);
                    if (oldSlot != null) {
                        String nSlot = JOptionPane.showInputDialog(this, "Update Window Configuration:", oldSlot);
                        if (nSlot != null && !nSlot.trim().isEmpty()) targetDoc.updateShiftSlot(targetDoc.getShiftSlots().indexOf(oldSlot), nSlot.trim());
                    }
                } else if (selectedAction.equals("Delete Time Slot")) {
                    if (targetDoc.getShiftSlots().isEmpty()) return;
                    String[] slots = targetDoc.getShiftSlots().toArray(new String[0]);
                    String slotDel = (String) JOptionPane.showInputDialog(this, "Select Shift to Remove:", "Delete", JOptionPane.QUESTION_MESSAGE, null, slots, slots[0]);
                    if (slotDel != null) targetDoc.removeShiftSlot(targetDoc.getShiftSlots().indexOf(slotDel));
                }
                showDoctorsManagementDashboard();
            }
        });

        btnHire.addActionListener(e -> {
            JTextField idF = createStyledTextField(); JTextField nameF = createStyledTextField();
            JComboBox<String> deptBox = new JComboBox<>(DEPARTMENTS); JTextField roomF = createStyledTextField();
            Object[] fields = {"Doctor ID:", idF, "Full Name:", nameF, "Select Department:", deptBox, "Room Assignment:", roomF};
            
            int opt = JOptionPane.showConfirmDialog(this, fields, "Register New Doctor", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION && !idF.getText().trim().isEmpty() && !nameF.getText().trim().isEmpty()) {
                doctorList.add(new Doctor(idF.getText().trim(), nameF.getText().trim(), (String)deptBox.getSelectedItem(), roomF.getText().trim()));
                showDoctorsManagementDashboard();
            }
        });

        btnToggle.addActionListener(e -> {
            for (int i = 0; i < allLists.size(); i++) {
                int sIdx = allLists.get(i).getSelectedIndex();
                if (sIdx != -1) {
                    doctorsPerDept.get(i).get(sIdx).toggleStatus();
                    showDoctorsManagementDashboard();
                    return;
                }
            }
        });

        btnUpdate.addActionListener(e -> {
            Doctor target = null;
            for (int i = 0; i < allLists.size(); i++) {
                int sIdx = allLists.get(i).getSelectedIndex();
                if (sIdx != -1) target = doctorsPerDept.get(i).get(sIdx);
            }
            if (target != null) {
                String newName = JOptionPane.showInputDialog(this, "Modify Doctor Name:", target.getName());
                String newRoom = JOptionPane.showInputDialog(this, "Modify Room Assignment:", target.getRoomNumber());
                if (newName != null && !newName.trim().isEmpty()) target.setName(newName.trim());
                if (newRoom != null && !newRoom.trim().isEmpty()) target.setRoomNumber(newRoom.trim());
                showDoctorsManagementDashboard();
            }
        });

        btnFire.addActionListener(e -> {
            for (int i = 0; i < allLists.size(); i++) {
                int sIdx = allLists.get(i).getSelectedIndex();
                if (sIdx != -1 && JOptionPane.showConfirmDialog(this, "Remove doctor permanently?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    doctorList.remove(doctorsPerDept.get(i).get(sIdx));
                    showDoctorsManagementDashboard();
                    return;
                }
            }
        });

        displayPanel.add(mainScroll, BorderLayout.CENTER);
        displayPanel.add(controlDeck, BorderLayout.SOUTH);
        displayPanel.revalidate(); displayPanel.repaint();
    }

    // =========================================================================
    //                    RECEPTIONIST VIEWS (MODULAR DEPT BOXES)
    // =========================================================================
    private void showReceptionistManagementDashboard() {
        displayPanel.removeAll();

        JPanel gridScrollContainer = new JPanel(new GridLayout(5, 1, 0, 10));
        gridScrollContainer.setBackground(COLOR_BG_DARK);

        ArrayList<JList<String>> allLists = new ArrayList<>();
        ArrayList<ArrayList<Receptionist>> recepPerDept = new ArrayList<>();

        for (String dept : DEPARTMENTS) {
            DefaultListModel<String> model = new DefaultListModel<>();
            ArrayList<Receptionist> subList = new ArrayList<>();
            
            for (Receptionist recep : receptionistList) {
                if (recep.getDepartment().equalsIgnoreCase(dept)) {
                    model.addElement(" [ID: " + recep.getId() + "]  Name: " + recep.getName() + "   |   Shift: " + recep.getShift());
                    subList.add(recep);
                }
            }
            
            JList<String> jList = new JList<>(model);
            jList.setFont(FONT_MAIN.deriveFont(13.0f)); jList.setForeground(COLOR_TEXT_WHITE); jList.setBackground(COLOR_INPUT_BOX);
            jList.setSelectionBackground(COLOR_ACCENT_BLUE);
            allLists.add(jList);
            recepPerDept.add(subList);

            jList.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && jList.getSelectedIndex() != -1) {
                    for (JList<String> otherList : allLists) {
                        if (otherList != jList) otherList.clearSelection();
                    }
                }
            });

            JScrollPane scroll = createCleanScrollPane(jList);
            scroll.setPreferredSize(new Dimension(0, 60));
            JPanel deptBox = createDepartmentBox(dept + " Desk", scroll);
            gridScrollContainer.add(deptBox);
        }

        JScrollPane mainScroll = createCleanScrollPane(gridScrollContainer);

        JPanel controlDeck = new JPanel(new GridLayout(1, 4, 10, 0));
        controlDeck.setBackground(COLOR_BG_DARK);
        controlDeck.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnHire = createAnimatedButton("HIRE RECEPTIONIST", COLOR_ACCENT_BLUE, COLOR_HOVER_BLUE);
        JButton btnShifts = createAnimatedButton("MANAGE SHIFTS", COLOR_ACCENT_BLUE, COLOR_HOVER_BLUE);
        JButton btnUpdate = createAnimatedButton("MODIFY INFO", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        JButton btnFire = createAnimatedButton("FIRE RECEPTIONIST", COLOR_DELETE_RED, COLOR_DELETE_RED.darker());
        
        Font smallerBtnFont = FONT_CLASSY_LABEL.deriveFont(11.0f);
        btnHire.setFont(smallerBtnFont); btnShifts.setFont(smallerBtnFont); btnUpdate.setFont(smallerBtnFont); btnFire.setFont(smallerBtnFont);
        controlDeck.add(btnHire); controlDeck.add(btnShifts); controlDeck.add(btnUpdate); controlDeck.add(btnFire);

        btnHire.addActionListener(e -> {
            JTextField idField = createStyledTextField(); JTextField nameField = createStyledTextField(); 
            JComboBox<String> deptBox = new JComboBox<>(DEPARTMENTS); 
            String[] shifts = {"Morning Shift", "Evening Shift"};
            JComboBox<String> shiftBox = new JComboBox<>(shifts);
            Object[] fields = {"Receptionist ID:", idField, "Full Staff Name:", nameField, "Department Allocation:", deptBox, "Duty Shift:", shiftBox};
            
            int opt = JOptionPane.showConfirmDialog(this, fields, "Hire Receptionist Staff", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION && !idField.getText().trim().isEmpty() && !nameField.getText().trim().isEmpty()) {
                receptionistList.add(new Receptionist(idField.getText().trim(), nameField.getText().trim(), (String)deptBox.getSelectedItem(), (String)shiftBox.getSelectedItem()));
                showReceptionistManagementDashboard();
            }
        });

        btnUpdate.addActionListener(e -> {
            Receptionist target = null;
            for (int i = 0; i < allLists.size(); i++) {
                int sIdx = allLists.get(i).getSelectedIndex();
                if (sIdx != -1) target = recepPerDept.get(i).get(sIdx);
            }
            if (target != null) {
                String newName = JOptionPane.showInputDialog(this, "Modify Staff Name:", target.getName());
                String[] shifts = {"Morning Shift", "Evening Shift"};
                String newShift = (String) JOptionPane.showInputDialog(this, "Update Shift Timing:", "Shift Configuration", JOptionPane.QUESTION_MESSAGE, null, shifts, target.getShift());
                if (newName != null && !newName.trim().isEmpty()) target.setName(newName.trim());
                if (newShift != null) target.setShift(newShift);
                showReceptionistManagementDashboard();
            }
        });

        btnFire.addActionListener(e -> {
            for (int i = 0; i < allLists.size(); i++) {
                int sIdx = allLists.get(i).getSelectedIndex();
                if (sIdx != -1 && JOptionPane.showConfirmDialog(this, "Terminate employee desk account?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    receptionistList.remove(recepPerDept.get(i).get(sIdx));
                    showReceptionistManagementDashboard();
                    return;
                }
            }
        });

        displayPanel.add(mainScroll, BorderLayout.CENTER);
        displayPanel.add(controlDeck, BorderLayout.SOUTH);
        displayPanel.revalidate(); displayPanel.repaint();
    }

    // =========================================================================
    //                         STANDARD DATA VIEWPORTS
    // =========================================================================
    private void showPatientsListLookup() {
        if (refreshTimer != null) refreshTimer.stop();
        displayPanel.removeAll();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        for (Patient pat : patientList) {
            String key = pat.getId();
            if (!seen.contains(key)) { seen.add(key); listModel.addElement(" [Patient ID: " + pat.getId() + "]    Name: " + pat.getName() + "  |  Condition: " + pat.getDisease()); }
        }
        for (Admin.HospitalAdmin.Patient p : Admin.HospitalAdmin.sharedPatientList) {
            String key = p.getId();
            if (!seen.contains(key)) { seen.add(key); listModel.addElement(" [Patient ID: " + p.getId() + "]    Name: " + p.getName() + "  |  Condition: " + p.getDisease()); }
        }
        if (listModel.isEmpty()) listModel.addElement(" No patients registered yet.");

        JList<String> jList = new JList<>(listModel);
        jList.setFont(FONT_MAIN); jList.setForeground(COLOR_TEXT_WHITE); jList.setBackground(COLOR_INPUT_BOX);
        jList.setSelectionBackground(COLOR_ACCENT_BLUE);

        JPanel inspectionCard = new JPanel(new GridLayout(4, 1, 5, 5));
        inspectionCard.setBackground(COLOR_CARD_NAVY);
        inspectionCard.setPreferredSize(new Dimension(300, 0));
        inspectionCard.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 15, 0, 0, COLOR_BG_DARK), BorderFactory.createCompoundBorder(new LineBorder(COLOR_INPUT_BOX, 1), new EmptyBorder(15, 15, 15, 15))));
        
        JLabel lblHeading = new JLabel("PATIENT MEDICAL PROFILE", SwingConstants.CENTER);
        lblHeading.setFont(FONT_CLASSY_LABEL.deriveFont(12.0f)); lblHeading.setForeground(COLOR_ACCENT_BLUE);
        JLabel lblNameData = new JLabel("Patient Selected: None", SwingConstants.LEFT);
        lblNameData.setForeground(COLOR_TEXT_WHITE); lblNameData.setFont(FONT_MAIN.deriveFont(13.0f));
        JLabel lblDiseaseData = new JLabel("Diagnosed Disease: --", SwingConstants.LEFT);
        lblDiseaseData.setForeground(COLOR_TEXT_MUTED); lblDiseaseData.setFont(FONT_MAIN.deriveFont(13.0f));
        JLabel lblRoomData = new JLabel("Assigned Room Space: --", SwingConstants.LEFT);
        lblRoomData.setForeground(COLOR_TEXT_MUTED); lblRoomData.setFont(FONT_MAIN.deriveFont(13.0f));

        inspectionCard.add(lblHeading); inspectionCard.add(lblNameData); inspectionCard.add(lblDiseaseData); inspectionCard.add(lblRoomData);

        jList.addListSelectionListener(e -> {
            int idx = jList.getSelectedIndex();
            if (idx != -1) {
                if (idx < patientList.size()) {
                    Patient target = patientList.get(idx);
                    lblNameData.setText("Patient: " + target.getName());
                    lblDiseaseData.setText("Condition: " + target.getDisease());
                    lblRoomData.setText("Location: " + target.getRoomNumber());
                } else {
                    int sharedIdx = idx - patientList.size();
                    if (sharedIdx < Admin.HospitalAdmin.sharedPatientList.size()) {
                        Admin.HospitalAdmin.Patient p = Admin.HospitalAdmin.sharedPatientList.get(sharedIdx);
                        lblNameData.setText("Patient: " + p.getName());
                        lblDiseaseData.setText("Condition: " + p.getDisease());
                        lblRoomData.setText("Location: " + p.getRoomNumber());
                    }
                }
            }
        });

        displayPanel.add(createDepartmentBox("Global Patient Directory", createCleanScrollPane(jList)), BorderLayout.CENTER);
        displayPanel.add(inspectionCard, BorderLayout.EAST);
        displayPanel.revalidate(); displayPanel.repaint();
    }

    private void showMasterAppointmentSchedule() {
        if (refreshTimer != null) refreshTimer.stop();
        displayPanel.removeAll();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        for (Appointment app : appointmentList) {
            String key = app.getDetails();
            if (!seen.contains(key)) { seen.add(key); listModel.addElement(" " + app.getDetails()); }
        }
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
            String detail = "Appt ID: " + sa.apptId + "  |  Patient: " + sa.patientName
                + "  -->  Doctor: " + sa.doctorName + "  |  Dept: " + sa.department
                + "  |  " + sa.date + "  " + sa.time
                + "  |  Status: [" + sa.status + "]";
            String key = detail;
            if (!seen.contains(key)) { seen.add(key); listModel.addElement(" " + detail); }
        }
        if (listModel.isEmpty()) listModel.addElement(" No appointments scheduled yet.");
        JList<String> jList = new JList<>(listModel);
        jList.setFont(FONT_MAIN.deriveFont(13.0f)); jList.setForeground(COLOR_TEXT_WHITE); jList.setBackground(COLOR_INPUT_BOX);
        displayPanel.add(createDepartmentBox("Central Master Appointment Logs", createCleanScrollPane(jList)), BorderLayout.CENTER);
        displayPanel.revalidate(); displayPanel.repaint();
    }

    private void showActiveEmergencies() {
        displayPanel.removeAll();
        DefaultListModel<String> listModel = new DefaultListModel<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        for (EmergencyCase em : emergencyList) {
            String key = em.getDetails();
            if (!seen.contains(key)) { seen.add(key); listModel.addElement(" " + em.getDetails()); }
        }
        for (HospitalSystem.SharedEmergencyCase sec : HospitalSystem.getSharedEmergencies()) {
            String detail = "Emergency ID: " + sec.caseId + "  |  Patient: " + sec.patientName
                + "  |  Dept: " + sec.department
                + "  |  Complaint: " + sec.complaint
                + "  |  Severity: [" + sec.severity + "]"
                + "  |  Doctor: " + sec.doctorName
                + "  |  " + sec.date + " " + sec.time;
            String key = detail;
            if (!seen.contains(key)) { seen.add(key); listModel.addElement(" " + detail); }
        }
        // 3. Shared appointments with Emergency Medicine department (from appointment table)
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
            if (sa.department != null && sa.department.equalsIgnoreCase("Emergency Medicine")) {
                String detail = "Emergency ID: " + sa.apptId + "  |  Patient: " + sa.patientName
                    + "  |  Dept: " + sa.department
                    + "  |  Complaint: Appointment registration"
                    + "  |  Severity: [Medium]"
                    + "  |  Doctor: " + sa.doctorName
                    + "  |  " + sa.date + " " + sa.time
                    + "  |  Status: [" + sa.status + "]";
                String key = detail;
                if (!seen.contains(key)) { seen.add(key); listModel.addElement(" " + detail); }
            }
        }
        System.out.println("[DEBUG Receptionist Admin] showActiveEmergencies: local=" + emergencyList.size() + " shared=" + HospitalSystem.getSharedEmergencies().size() + " sharedAppts=" + HospitalSystem.getSharedAppointments().size());
        if (listModel.isEmpty()) listModel.addElement(" No active emergency cases.");
        JList<String> jList = new JList<>(listModel);
        jList.setFont(FONT_MAIN.deriveFont(13.0f)); jList.setForeground(COLOR_TEXT_WHITE); jList.setBackground(COLOR_INPUT_BOX);
        jList.setSelectionBackground(COLOR_DELETE_RED);
        
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setBackground(COLOR_BG_DARK); controls.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnAssignDoc = createAnimatedButton("RE-ASSIGN DOCTOR", COLOR_BUTTON_GRAY, COLOR_HOVER_GRAY);
        btnAssignDoc.setPreferredSize(new Dimension(160, 32));
        JButton btnPrioritize = createAnimatedButton("CHANGE PRIORITY RANK", COLOR_DELETE_RED, COLOR_DELETE_RED.darker());
        btnPrioritize.setPreferredSize(new Dimension(190, 32));
        controls.add(btnAssignDoc); controls.add(btnPrioritize);

        btnAssignDoc.addActionListener(e -> {
            int selectedIdx = jList.getSelectedIndex();
            if (selectedIdx == -1) return;
            String[] doctorNames = new String[doctorList.size()];
            for (int i = 0; i < doctorList.size(); i++) doctorNames[i] = "Dr. " + doctorList.get(i).getName() + " (" + doctorList.get(i).getDepartment() + ")";
            String sel = (String) JOptionPane.showInputDialog(this, "Select Medical Specialist:", "Assign Duty", JOptionPane.QUESTION_MESSAGE, null, doctorNames, doctorNames[0]);
            if (sel == null) return;

            Doctor newDoc = null;
            for (Doctor d : doctorList) {
                if (sel.startsWith("Dr. " + d.getName())) { newDoc = d; break; }
            }
            if (newDoc == null) return;
            String newDept = newDoc.getDepartment();

            int localEmergCount = emergencyList.size();
            int sharedEmergCount = HospitalSystem.getSharedEmergencies().size();

            // Source 1: local emergencyList (no shared sync needed)
            if (selectedIdx < localEmergCount) {
                emergencyList.get(selectedIdx).setAssignedDoctor(newDoc);
            }
            // Source 2: shared emergencies
            else if (selectedIdx < localEmergCount + sharedEmergCount) {
                int emergIdx = selectedIdx - localEmergCount;
                HospitalSystem.SharedEmergencyCase sec = HospitalSystem.getSharedEmergencies().get(emergIdx);
                // Delegate via HospitalSystem for full sync (apptModel + shared stores)
                HospitalSystem.triggerAdminReassign(
                    sec.caseId, sec.patientName, sec.date, sec.time,
                    "Dr. " + newDoc.getName(), newDept);
            }
            // Source 3: shared appointments with Emergency Medicine dept
            else {
                int apptIdx = selectedIdx - localEmergCount - sharedEmergCount;
                java.util.List<HospitalSystem.SharedAppointment> emAppts = new java.util.ArrayList<>();
                for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
                    if (sa.department != null && sa.department.equalsIgnoreCase("Emergency Medicine")) {
                        emAppts.add(sa);
                    }
                }
                if (apptIdx < emAppts.size()) {
                    HospitalSystem.SharedAppointment sa = emAppts.get(apptIdx);
                    HospitalSystem.triggerAdminReassign(
                        sa.apptId, sa.patientName, sa.date, sa.time,
                        "Dr. " + newDoc.getName(), newDept);
                }
            }
            showActiveEmergencies();
        });

        displayPanel.add(createDepartmentBox("Active Live Emergency Trauma Stream", createCleanScrollPane(jList)), BorderLayout.CENTER);
        displayPanel.add(controls, BorderLayout.SOUTH);
        displayPanel.revalidate(); displayPanel.repaint();

        setupRefreshTimer(() -> {
            if (displayPanel.getComponentCount() > 0 && isVisible()) {
                showActiveEmergencies();
            }
        });
    }

    private void setupRefreshTimer(Runnable onRefresh) {
        if (refreshTimer != null) { refreshTimer.stop(); }
        refreshTimer = new javax.swing.Timer(3000, e -> {
            if (isVisible()) onRefresh.run();
        });
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    // =========================================================================
    //             CRITICAL FIX: FULLY INTERACTIVE INSIGHT REPORT CARDS
    // =========================================================================
    private void showComprehensiveInsightReport() {
        displayPanel.removeAll();
        JPanel mainDashboard = new JPanel(new GridLayout(2, 3, 15, 15));
        mainDashboard.setBackground(COLOR_BG_DARK);

        // Created interactive metrics cards that tie directly to workspace redirection listeners
        JPanel cardDocs = createReportCard("ACTIVE MEDICAL DOCTORS", String.valueOf(doctorList.size()), COLOR_ACCENT_BLUE);
        cardDocs.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showDoctorsManagementDashboard(); }
        });

        JPanel cardReceps = createReportCard("RECEPTIONISTS REGISTERED", String.valueOf(receptionistList.size()), COLOR_BUTTON_GRAY);
        cardReceps.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showReceptionistManagementDashboard(); }
        });

        int totalPatients = patientList.size() + Admin.HospitalAdmin.sharedPatientList.size();
        JPanel cardPatients = createReportCard("TOTAL REGISTERED PATIENTS", String.valueOf(totalPatients), COLOR_ACCENT_BLUE);
        cardPatients.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showPatientsListLookup(); }
        });

        int totalAppts = appointmentList.size() + HospitalSystem.getSharedAppointments().size();
        JPanel cardAppts = createReportCard("SCHEDULED APPOINTMENTS", String.valueOf(totalAppts), COLOR_BUTTON_GRAY);
        cardAppts.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showMasterAppointmentSchedule(); }
        });

        int totalEmerg = emergencyList.size() + HospitalSystem.getSharedEmergencies().size();
        JPanel cardEmerg = createReportCard("ACTIVE EMERGENCY traumas", String.valueOf(totalEmerg), COLOR_DELETE_RED);
        cardEmerg.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showActiveEmergencies(); }
        });

        JPanel cardUnits = createReportCard("OPERATIONAL CLINICAL UNITS", "5 Departments", COLOR_BUTTON_GRAY);
        cardUnits.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showDoctorsManagementDashboard(); }
        });

        mainDashboard.add(cardDocs); mainDashboard.add(cardReceps); mainDashboard.add(cardPatients);
        mainDashboard.add(cardAppts); mainDashboard.add(cardEmerg); mainDashboard.add(cardUnits);

        displayPanel.add(mainDashboard, BorderLayout.CENTER);
        displayPanel.revalidate(); displayPanel.repaint();

        setupRefreshTimer(() -> {
            if (displayPanel.getComponentCount() > 0 && isVisible()) {
                showComprehensiveInsightReport();
            }
        });
    }

    private JPanel createReportCard(String title, String metrics, Color indicatorColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD_NAVY); card.setBorder(new LineBorder(COLOR_INPUT_BOX, 1));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Adds clear hover affordance

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(FONT_CLASSY_LABEL.deriveFont(11.0f)); lblTitle.setForeground(COLOR_TEXT_MUTED);
        lblTitle.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        JLabel lblMetrics = new JLabel(metrics, SwingConstants.CENTER);
        lblMetrics.setFont(new Font("Segoe UI", Font.BOLD, 24)); lblMetrics.setForeground(COLOR_TEXT_WHITE);
        
        JPanel line = new JPanel(); line.setBackground(indicatorColor); line.setPreferredSize(new Dimension(0, 4));
        
        card.add(lblTitle, BorderLayout.NORTH); card.add(lblMetrics, BorderLayout.CENTER); card.add(line, BorderLayout.SOUTH);
        return card;
    }

    // =========================================================================
    //                       3. MASTER SEED DATA METHOD
    // =========================================================================
    public static void main(String[] args) {
        ArrayList<Doctor> mockDoctors = new ArrayList<>();
        ArrayList<Receptionist> mockReceptionists = new ArrayList<>();
        ArrayList<Patient> mockPatients = new ArrayList<>();
        ArrayList<Appointment> mockAppointments = new ArrayList<>();
        ArrayList<EmergencyCase> mockEmergencies = new ArrayList<>();

        // 20 Doctors (4 spread across each of the 5 distinct specialties)
        String[] depts = {"Cardiology", "Electrophysiology", "Cardiac Imaging", "Interventional Cardiology", "Emergency Medicine"};
        String[] docNames = {
            "Kamran Khan", "Faisal Qureshi", "Anwar Latif", "Bilal Javed",
            "Asif Malik", "Omer Shehzad", "Nabeel Shiraz", "Saad Ghafoor",
            "Haris Bilal", "Zubair Niazi", "Waqas Raza", "Yasir Arafat",
            "Zainab Raza", "Maryam Nawaz", "Amina Butt", "Sana Yousaf",
            "Sarmad Ali", "Hamza Tariq", "Taimoor Hassan", "Zeeshan Khan"
        };
        
        int nameIdx = 0;
        for (String dept : depts) {
            for (int i = 1; i <= 4; i++) {
                Doctor d = new Doctor("DOC-" + dept.substring(0,3).toUpperCase() + "-0" + i, docNames[nameIdx++], dept, "Room " + (100 + nameIdx));
                d.addShiftSlot("9 AM - 1 PM");
                mockDoctors.add(d);
            }
        }

        // EXACTLY 2 Receptionists per department category (1 Morning, 1 Evening)
        String[] recepNames = {
            "Fatima Ahmed", "Zainab Malik", "Sadia Khan", "Hina Yousaf", "Ayesha Bibi",
            "Amna Khan", "Iqra Ali", "Sana Ahmed", "Tayyaba Rashid", "Khadija Noor"
        };
        int rNameIdx = 0;
        for (String dept : depts) {
            mockReceptionists.add(new Receptionist("R-" + dept.substring(0,3).toUpperCase() + "-M", recepNames[rNameIdx++], dept, "Morning Shift"));
            mockReceptionists.add(new Receptionist("R-" + dept.substring(0,3).toUpperCase() + "-E", recepNames[rNameIdx++], dept, "Evening Shift"));
        }

        SwingUtilities.invokeLater(() -> {
            new HospitalAdmin(mockDoctors, mockReceptionists, mockPatients, mockAppointments, mockEmergencies).setVisible(true);
        });
    }
}