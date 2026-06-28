package Admin;

import System.DoctorRosterStore;
import System.HospitalSystem;
import System.ReceptionistRosterStore;
import Receptionist.ReceptionistUIHelper;
import Receptionist.ReceptionistDataStore;
import Doctor.doctorDataStore;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class HospitalAdmin extends JFrame {

    public static class Employee {
        private String id;
        private String name;

        public Employee(String id, String name) {
            this.id = id;
            this.name = name;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class Doctor extends Employee {
        private String department;
        private String roomNumber;
        private boolean isActive;
        private ArrayList<String> shiftSlots;

        public Doctor(String id, String name, String department, String roomNumber) {
            super(id, name);
            this.department = department;
            this.roomNumber = roomNumber;
            this.isActive = true;
            this.shiftSlots = new ArrayList<>();
        }
        public String getDepartment() { return department; }
        public String getRoomNumber() { return roomNumber; }
        public boolean isActive() { return isActive; }
        public ArrayList<String> getShiftSlots() { return shiftSlots; }
        public void setDepartment(String department) { this.department = department; }
        public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
        public void toggleStatus() { this.isActive = !this.isActive; }
        public void setActive(boolean active) { this.isActive = active; }
        public void addShiftSlot(String slot) { this.shiftSlots.add(slot); }
        public void removeShiftSlot(int index) { if(index >= 0 && index < shiftSlots.size()) this.shiftSlots.remove(index); }
        public void updateShiftSlot(int index, String newSlot) { if(index >= 0 && index < shiftSlots.size()) this.shiftSlots.set(index, newSlot); }
        public String getShiftsFormatted() {
            if (shiftSlots.isEmpty()) return "No Assigned Shifts";
            return String.join(" | ", shiftSlots);
        }
    }

    public static class Receptionist extends Employee {
        private ArrayList<String> shiftSlots;

        public Receptionist(String id, String name) {
            super(id, name);
            this.shiftSlots = new ArrayList<>();
        }
        public ArrayList<String> getShiftSlots() { return shiftSlots; }
        public void addShiftSlot(String slot) { this.shiftSlots.add(slot); }
        public void removeShiftSlot(int index) { if(index >= 0 && index < shiftSlots.size()) this.shiftSlots.remove(index); }
        public void updateShiftSlot(int index, String newSlot) { if(index >= 0 && index < shiftSlots.size()) this.shiftSlots.set(index, newSlot); }
        public String getShiftsFormatted() {
            if (shiftSlots.isEmpty()) return "No Assigned Shifts";
            return String.join(" | ", shiftSlots);
        }
    }

    public static class Patient {
        private String id, name, disease, roomNumber;

        public Patient(String id, String name, String disease, String roomNumber) {
            this.id = id;
            this.name = name;
            this.disease = disease;
            this.roomNumber = roomNumber;
        }
        public String getId() { return id; }
        public String getName() { return name; }
        public String getDisease() { return disease; }
        public String getRoomNumber() { return roomNumber; }
    }

    public static class Appointment {
        private String appointmentId, patientName, doctorName, timeSlot;

        public Appointment(String appointmentId, String patientName, String doctorName, String timeSlot) {
            this.appointmentId = appointmentId;
            this.patientName = patientName;
            this.doctorName = doctorName;
            this.timeSlot = timeSlot;
        }
        public String getDetails() {
            return "Appt ID: " + appointmentId + "  |  Patient: " + patientName + "  -->  Doctor: " + doctorName + "  |  Time: " + timeSlot;
        }
    }

    private static class EmergencyCase {
        private String caseId, patientName, severityLevel;
        private Doctor assignedDoctor;

        public EmergencyCase(String caseId, String patientName, String severityLevel, Doctor assignedDoctor) {
            this.caseId = caseId;
            this.patientName = patientName;
            this.severityLevel = severityLevel;
            this.assignedDoctor = assignedDoctor;
        }
    public String getDetails() {
        String docDisplay = (assignedDoctor != null) ? "Dr. " + assignedDoctor.getName() : "Unassigned";
        String deptName = (assignedDoctor != null) ? assignedDoctor.getDepartment() : "N/A";
        return "Emergency ID: " + caseId + "  |  Patient: " + patientName
             + "  |  Dept: [" + deptName
             + "] Specialist: " + docDisplay
             + "  |  Severity: [" + severityLevel + "]";
    }
        public String getSeverityLevel() { return severityLevel; }
        public void setSeverityLevel(String severityLevel) { this.severityLevel = severityLevel; }
        public Doctor getAssignedDoctor() { return assignedDoctor; }
        public void setAssignedDoctor(Doctor assignedDoctor) { this.assignedDoctor = assignedDoctor; }
    }

    // =========================================================================
    // 2. THE ADMIN WORKSPACE USER INTERFACE (RESTORED TO ORIGINAL COMPACT BOUNDS)
    // =========================================================================
    
    private ArrayList<Doctor> doctorList;
    private ArrayList<Receptionist> receptionistList;
    private ArrayList<Patient> patientList;
    private ArrayList<Appointment> appointmentList;
    private ArrayList<EmergencyCase> emergencyList;

    public static ArrayList<Patient> sharedPatientList = new ArrayList<>();
    public static ArrayList<Appointment> sharedAppointmentList = new ArrayList<>();

    public static void registerPatient(String id, String name, String disease, String room) {
        sharedPatientList.add(new Patient(id, name, disease, room));
    }

    public static void registerAppointment(String id, String patientName, String doctorName, String timeSlot) {
        sharedAppointmentList.add(new Appointment(id, patientName, doctorName, timeSlot));
    }

    private JPanel displayPanel;
    
    private final String[] DEPARTMENTS = {
        "Cardiology", "Electrophysiology", "Cardiac Imaging", "Interventional Cardiology", "Emergency Medicine"
    };

    // --- COLOR THEME (IDENTICAL TO RECEPTIONIST) ---
    private final Color COLOR_BG_DARK     = ReceptionistUIHelper.C_BG;
    private final Color COLOR_CARD_NAVY   = ReceptionistUIHelper.C_WHITE;
    private final Color COLOR_INPUT_BOX   = ReceptionistUIHelper.C_WHITE;
    private final Color COLOR_ACCENT_BLUE = ReceptionistUIHelper.C_BLUE;
    private final Color COLOR_HOVER_BLUE  = ReceptionistUIHelper.C_BLUE_MID;
    private final Color COLOR_BUTTON_GRAY = ReceptionistUIHelper.C_SIDEBAR_A;
    private final Color COLOR_HOVER_GRAY  = ReceptionistUIHelper.C_BLUE;
    private final Color COLOR_DELETE_RED  = ReceptionistUIHelper.C_RED;
    private final Color COLOR_TEXT_WHITE  = ReceptionistUIHelper.C_DARK;
    private final Color COLOR_TEXT_MUTED  = ReceptionistUIHelper.C_MUTED;
    
    // --- TYPOGRAPHY (IDENTICAL TO RECEPTIONIST) ---
    private final Font FONT_CLASSY_TITLE = ReceptionistUIHelper.F_TITLE;
    private final Font FONT_CLASSY_LABEL = ReceptionistUIHelper.F_HEAD;
    private final Font FONT_MAIN         = ReceptionistUIHelper.F_BODY;

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

        setTitle("Smart Portal — Admin Dashboard");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); 
        setSize(1340, 840);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(null); 
        setLayout(new BorderLayout());
        getContentPane().setBackground(ReceptionistUIHelper.C_BG);

        // --- TOP BAR (MATCHING RECEPTIONIST) ---
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0, ReceptionistUIHelper.C_NAVY,
                    getWidth(),0, new Color(20,65,170)));
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(new Color(255,255,255,18));
                g2.fillRect(0,getHeight()-1,getWidth(),1);
                g2.dispose();
            }
        };
        topBar.setPreferredSize(new Dimension(0, 60));
        topBar.setBorder(new EmptyBorder(0,24,0,24));

        JPanel leftTop = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0)); leftTop.setOpaque(false);
        JLabel cross = new JLabel("✚"); cross.setFont(new Font("Segoe UI",Font.BOLD,22)); cross.setForeground(new Color(100,180,255));
        JLabel title = new JLabel("Smart Portal"); title.setFont(new Font("Segoe UI",Font.BOLD,17)); title.setForeground(Color.WHITE);
        JLabel sep   = new JLabel(" | "); sep.setFont(new Font("Segoe UI",Font.PLAIN,16)); sep.setForeground(new Color(255,255,255,50));
        JLabel sub   = new JLabel("Admin Dashboard"); sub.setFont(new Font("Segoe UI",Font.PLAIN,13)); sub.setForeground(new Color(200,220,255));
        leftTop.add(cross); leftTop.add(title); leftTop.add(sep); leftTop.add(sub);
        topBar.add(leftTop, BorderLayout.WEST);

        // --- SIDEBAR DESIGN (MATCHING RECEPTIONIST) ---
        JPanel sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(ReceptionistUIHelper.C_SIDEBAR);
        sidebarPanel.setPreferredSize(new Dimension(230, 0));

        sidebarPanel.add(Box.createVerticalStrut(20));

        JLabel sectionLbl = new JLabel("  NAVIGATION");
        sectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sectionLbl.setForeground(new Color(100,130,200));
        sectionLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(sectionLbl);
        sidebarPanel.add(Box.createVerticalStrut(8));

        String[][] navItems = {
            {"📊", "System Insight Report"},
            {"👨‍⚕️", "Manage Doctors Roster"},
            {"👩‍💼", "Manage Receptionists"},
            {"🧑‍⚕️", "Patient Profiles"},
            {"📅", "Live Appointment Logs"},
            {"🚑", "Emergency Cases"},
        };
        JButton btnManageDocs = createNavButton("👨‍⚕️", "Manage Doctors Roster");
        JButton btnManageReceps = createNavButton("👩‍💼", "Manage Receptionists");
        JButton btnViewPatients = createNavButton("🧑‍⚕️", "Patient Profiles");
        JButton btnViewAppts = createNavButton("📅", "Live Appointment Logs");
        JButton btnViewEmerg = createNavButton("🚑", "Emergency Cases");
        JButton btnReport = createNavButton("📊", "System Insight Report");

        sidebarPanel.add(btnReport);
        sidebarPanel.add(btnManageDocs);
        sidebarPanel.add(btnManageReceps);
        sidebarPanel.add(btnViewPatients);
        sidebarPanel.add(btnViewAppts);
        sidebarPanel.add(btnViewEmerg);

        sidebarPanel.add(Box.createVerticalGlue());

        JButton btnLogout = new JButton("🚪   Close Workspace");
        btnLogout.setFont(new Font("Segoe UI",Font.PLAIN,13));
        btnLogout.setForeground(new Color(170,195,235));
        btnLogout.setHorizontalAlignment(SwingConstants.LEFT);
        btnLogout.setBorderPainted(false); btnLogout.setContentAreaFilled(false); btnLogout.setFocusPainted(false);
        btnLogout.setMaximumSize(new Dimension(230,44)); btnLogout.setPreferredSize(new Dimension(230,44));
        btnLogout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogout.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebarPanel.add(btnLogout);
        sidebarPanel.add(Box.createVerticalStrut(14));

        // --- MAIN APPLICATION VIEWPORT ---
        displayPanel = new JPanel();
        displayPanel.setLayout(new BorderLayout());
        displayPanel.setBackground(ReceptionistUIHelper.C_BG);
        displayPanel.setBorder(new EmptyBorder(26, 30, 34, 30));

        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(ReceptionistUIHelper.C_BG);
        JScrollPane contentScroll = new JScrollPane(displayPanel);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentScroll.getHorizontalScrollBar().setUnitIncrement(16);
        contentWrapper.add(contentScroll, BorderLayout.CENTER);

        add(topBar, BorderLayout.NORTH);
        add(sidebarPanel, BorderLayout.WEST);
        add(contentWrapper, BorderLayout.CENTER);

        // --- ACTION BINDINGS ---
        btnManageDocs.addActionListener(e -> showDoctorsManagementDashboard());
        btnManageReceps.addActionListener(e -> showReceptionistManagementDashboard());
        btnViewPatients.addActionListener(e -> showPatientsListLookup());
        btnViewAppts.addActionListener(e -> showMasterAppointmentSchedule());
        btnViewEmerg.addActionListener(e -> showActiveEmergencies());
        btnReport.addActionListener(e -> showComprehensiveInsightReport());
        btnLogout.addActionListener(e -> {
            dispose();
            if (HospitalSystem.showLauncher != null) HospitalSystem.showLauncher.run();
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (HospitalSystem.showLauncher != null) HospitalSystem.showLauncher.run();
            }
        });

        showComprehensiveInsightReport();
    }

    private JButton createNavButton(String icon, String label) {
        JButton btn = new JButton(icon + "   " + label) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover=true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hover=false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = hover ? ReceptionistUIHelper.C_SIDEBAR_H : null;
                if (bg != null) { g2.setColor(bg); g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,8,8); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI",Font.PLAIN,13));
        btn.setForeground(new Color(170,195,235));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(230,44)); btn.setPreferredSize(new Dimension(230,44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private JButton createAnimatedButton(String text, Color baseColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if      (getModel().isPressed())  g2.setColor(ReceptionistUIHelper.C_NAVY);
                else if (getModel().isRollover()) g2.setColor(hoverColor);
                else                              g2.setColor(baseColor);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        button.setFont(ReceptionistUIHelper.F_BOLD_S);
        button.setForeground(ReceptionistUIHelper.C_WHITE);
        button.setContentAreaFilled(false); button.setBorderPainted(false); button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 18, 9, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private DefaultListCellRenderer createPaddedRenderer() {
        return new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(8, 10, 8, 10));
                label.setFont(ReceptionistUIHelper.F_BODY);
                if (isSelected) {
                    label.setBackground(ReceptionistUIHelper.C_SKY);
                    label.setForeground(ReceptionistUIHelper.C_NAVY);
                } else {
                    label.setBackground(ReceptionistUIHelper.C_WHITE);
                    label.setForeground(ReceptionistUIHelper.C_DARK);
                }
                return label;
            }
        };
    }

    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(ReceptionistUIHelper.F_BODY);
        field.setForeground(ReceptionistUIHelper.C_DARK);
        field.setBackground(ReceptionistUIHelper.C_WHITE);
        field.setCaretColor(ReceptionistUIHelper.C_DARK);
        field.setBorder(ReceptionistUIHelper.fieldBorderDef());
        field.setPreferredSize(new Dimension(0, 38));
        return field;
    }

    private JScrollPane createCleanScrollPane(Component view) {
        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.setBorder(new LineBorder(ReceptionistUIHelper.C_DIVIDER, 1));
        scrollPane.getViewport().setBackground(ReceptionistUIHelper.C_WHITE);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = ReceptionistUIHelper.C_BORDER;
                this.trackColor = ReceptionistUIHelper.C_WHITE;
            }
            @Override protected JButton createDecreaseButton(int orientation) { return createZeroSizeButton(); }
            @Override protected JButton createIncreaseButton(int orientation) { return createZeroSizeButton(); }
            private JButton createZeroSizeButton() {
                JButton jb = new JButton(); jb.setPreferredSize(new Dimension(0,0)); return jb;
            }
        });
        scrollPane.getVerticalScrollBar().setPreferredSize(new Dimension(6, 0));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        return scrollPane;
    }

    private JPanel createDepartmentBox(String deptName, JComponent listComponent) {
        JPanel box = new JPanel(new BorderLayout(0, 14));
        box.setBackground(ReceptionistUIHelper.C_WHITE);
        box.setBorder(new CompoundBorder(new ReceptionistUIHelper.PanelShadowBorder(), new EmptyBorder(20, 20, 20, 20)));
        JLabel ttl = new JLabel(deptName);
        ttl.setFont(ReceptionistUIHelper.F_HEAD);
        ttl.setForeground(ReceptionistUIHelper.C_NAVY);
        box.add(ttl, BorderLayout.NORTH);
        box.add(listComponent, BorderLayout.CENTER);
        return box;
    }

    // ── Table builder with colored headers and visible grid ───────────────
    private JTable createAdminTable(DefaultTableModel model) {
        return createAdminTable(model, -1);
    }

    private JTable createAdminTable(DefaultTableModel model, int statusCol) {
        JTable t = new JTable(model);
        t.setFont(ReceptionistUIHelper.F_BODY);
        t.setForeground(ReceptionistUIHelper.C_DARK);
        t.setRowHeight(32);
        t.setShowGrid(true);
        t.setGridColor(ReceptionistUIHelper.C_DIVIDER);
        t.setIntercellSpacing(new Dimension(1, 1));
        t.setBackground(ReceptionistUIHelper.C_WHITE);
        t.setSelectionBackground(ReceptionistUIHelper.C_SKY);
        t.setSelectionForeground(ReceptionistUIHelper.C_NAVY);

        JTableHeader h = t.getTableHeader();
        h.setFont(ReceptionistUIHelper.F_BOLD_S);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 32));

        // Color each column header differently
        Color[] headerColors = {
            ReceptionistUIHelper.C_NAVY,
            ReceptionistUIHelper.C_BLUE,
            ReceptionistUIHelper.C_GREEN,
            ReceptionistUIHelper.C_AMBER,
            ReceptionistUIHelper.C_RED,
            ReceptionistUIHelper.C_INDIGO,
            new Color(100, 130, 200),
            new Color(150, 100, 180)
        };
        h.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel l = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                l.setFont(ReceptionistUIHelper.F_BOLD_S);
                l.setForeground(ReceptionistUIHelper.C_WHITE);
                l.setBackground(headerColors[col % headerColors.length]);
                l.setOpaque(true);
                l.setBorder(new CompoundBorder(
                    new LineBorder(new Color(255,255,255,40), 1),
                    new EmptyBorder(6, 10, 6, 10)));
                return l;
            }
        });

        // Alternate row colors with visible grid
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val,
                    boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, sel, foc, r, c);
                if (!sel) {
                    comp.setBackground(r % 2 == 0 ? ReceptionistUIHelper.C_WHITE : new Color(245, 248, 252));
                }
                comp.setForeground(ReceptionistUIHelper.C_DARK);
                ((JLabel)comp).setHorizontalAlignment(SwingConstants.CENTER);
                ((JLabel)comp).setBorder(new CompoundBorder(
                    new LineBorder(ReceptionistUIHelper.C_DIVIDER, 1),
                    new EmptyBorder(4, 8, 4, 8)));
                return comp;
            }
        });

        if (statusCol >= 0) {
            t.getColumnModel().getColumn(statusCol).setCellRenderer(new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable tbl, Object val,
                        boolean sel, boolean foc, int r, int c) {
                    JLabel l = new JLabel("  " + (val == null ? "" : val.toString()) + "  ", SwingConstants.CENTER);
                    l.setFont(ReceptionistUIHelper.F_BOLD_S);
                    l.setOpaque(true);
                    String v = val == null ? "" : val.toString().toLowerCase();
                    if      (v.contains("confirm") || v.contains("scheduled")) {
                        l.setBackground(ReceptionistUIHelper.C_GREEN_BG);
                        l.setForeground(new Color(21,128,61));
                    } else if (v.contains("pending") || v.contains("registered") || v.contains("medium")) {
                        l.setBackground(ReceptionistUIHelper.C_AMBER_BG);
                        l.setForeground(new Color(180,83,9));
                    } else if (v.contains("cancel") || v.contains("critical")) {
                        l.setBackground(ReceptionistUIHelper.C_RED_BG);
                        l.setForeground(ReceptionistUIHelper.C_RED);
                    } else if (v.contains("walk") || v.contains("high")) {
                        l.setBackground(ReceptionistUIHelper.C_INDIGO_BG);
                        l.setForeground(ReceptionistUIHelper.C_INDIGO);
                    } else if (v.contains("low")) {
                        l.setBackground(ReceptionistUIHelper.C_GREEN_BG);
                        l.setForeground(new Color(21,128,61));
                    } else if (v.contains("active") || v.contains("on leave")) {
                        if (v.contains("active")) {
                            l.setBackground(ReceptionistUIHelper.C_GREEN_BG);
                            l.setForeground(new Color(21,128,61));
                        } else {
                            l.setBackground(ReceptionistUIHelper.C_RED_BG);
                            l.setForeground(ReceptionistUIHelper.C_RED);
                        }
                    } else {
                        l.setBackground(ReceptionistUIHelper.C_WHITE);
                        l.setForeground(ReceptionistUIHelper.C_DARK);
                    }
                    l.setBorder(new CompoundBorder(
                        new LineBorder(ReceptionistUIHelper.C_DIVIDER, 1),
                        new EmptyBorder(4, 8, 4, 8)));
                    return l;
                }
            });
        }

        return t;
    }

    // =========================================================================
    //                        DOCTOR VIEWS (MODULAR DEPT BOXES)
    // =========================================================================
    private void showDoctorsManagementDashboard() {
        if (refreshTimer != null) refreshTimer.stop();
        displayPanel.removeAll();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        JPanel gridScrollContainer = new JPanel(new GridLayout(5, 1, 0, 10));
        gridScrollContainer.setOpaque(false);

        ArrayList<JTable> allTables = new ArrayList<>();
        ArrayList<ArrayList<Doctor>> doctorsPerDept = new ArrayList<>();

        String[] docCols = {"ID", "Name", "Shifts", "Status", "Room"};

        for (String dept : DEPARTMENTS) {
            DefaultTableModel dtm = new DefaultTableModel(docCols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            ArrayList<Doctor> subList = new ArrayList<>();
            
            for (Doctor doc : doctorList) {
                if (doc.getDepartment().equalsIgnoreCase(dept)) {
                    String status = doc.isActive() ? "ACTIVE" : "ON LEAVE";
                    dtm.addRow(new Object[]{doc.getId(), "Dr. " + doc.getName(), doc.getShiftsFormatted(), status, doc.getRoomNumber()});
                    subList.add(doc);
                }
            }
            
            JTable deptTable = createAdminTable(dtm, 3);
            allTables.add(deptTable);
            doctorsPerDept.add(subList);

            deptTable.getSelectionModel().addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting() && deptTable.getSelectedRow() != -1) {
                    for (JTable other : allTables) {
                        if (other != deptTable) other.clearSelection();
                    }
                }
            });

            JScrollPane scroll = createCleanScrollPane(deptTable);
            scroll.setPreferredSize(new Dimension(0, 100));
            JPanel deptBox = createDepartmentBox(dept, scroll);
            gridScrollContainer.add(deptBox);
        }

        JScrollPane mainScroll = createCleanScrollPane(gridScrollContainer);

        JPanel controlDeck = new JPanel(new GridLayout(1, 5, 8, 0));
        controlDeck.setOpaque(false);
        controlDeck.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnHire = createAnimatedButton("HIRE NEW DOCTOR", ReceptionistUIHelper.C_BLUE, ReceptionistUIHelper.C_BLUE_MID);
        JButton btnShifts = createAnimatedButton("MANAGE SHIFTS", ReceptionistUIHelper.C_BLUE, ReceptionistUIHelper.C_BLUE_MID);
        JButton btnToggle = createAnimatedButton("TOGGLE LEAVE STATUS", ReceptionistUIHelper.C_MID, ReceptionistUIHelper.C_MUTED);
        JButton btnUpdate = createAnimatedButton("EDIT DATA", ReceptionistUIHelper.C_MID, ReceptionistUIHelper.C_MUTED);
        JButton btnFire = createAnimatedButton("FIRE DOCTOR", ReceptionistUIHelper.C_RED, ReceptionistUIHelper.C_RED.darker());

        Font smallerBtnFont = ReceptionistUIHelper.F_BOLD_S;
        btnHire.setFont(smallerBtnFont); btnShifts.setFont(smallerBtnFont); btnToggle.setFont(smallerBtnFont); btnUpdate.setFont(smallerBtnFont); btnFire.setFont(smallerBtnFont);

        controlDeck.add(btnHire); controlDeck.add(btnShifts); controlDeck.add(btnToggle); controlDeck.add(btnUpdate); controlDeck.add(btnFire);

        btnShifts.addActionListener(e -> {
            Doctor targetDoc = null;
            for (int i = 0; i < allTables.size(); i++) {
                int sIdx = allTables.get(i).getSelectedRow();
                if (sIdx != -1) targetDoc = doctorsPerDept.get(i).get(sIdx);
            }
            if (targetDoc == null) {
                JOptionPane.showMessageDialog(this, "Please select a doctor from one of the department boxes.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String[] groupLabels = {
                "1st Doctor: 8 am to 11 am | 12 pm to 2 pm",
                "2nd Doctor: 2pm to 5pm | 6pm to 8pm",
                "3rd Doctor: 8pm to 9pm | 9pm to 2am",
                "4th Doctor: 2am to 4am | 5am to 8am"
            };
            String[][] shiftGroups = {
                {"8 am to 11 am", "12 pm to 2 pm"},
                {"2pm to 5pm", "6pm to 8pm"},
                {"8pm to 9pm", "9pm to 2am"},
                {"2am to 4am", "5am to 8am"}
            };

            String selected = (String) JOptionPane.showInputDialog(this,
                "Select time slot for Dr. " + targetDoc.getName() + ":",
                "Assign Shifts", JOptionPane.PLAIN_MESSAGE, null, groupLabels, groupLabels[0]);
            if (selected != null) {
                int idx = java.util.Arrays.asList(groupLabels).indexOf(selected);
                targetDoc.getShiftSlots().clear();
                for (String s : shiftGroups[idx]) targetDoc.addShiftSlot(s);
                syncDoctorShifts(targetDoc);
                adjustAppointmentTimes(targetDoc, shiftGroups[idx]);
                showDoctorsManagementDashboard();
            }
        });

        btnHire.addActionListener(e -> {
            JTextField idF = createStyledTextField(); JTextField nameF = createStyledTextField();
            JComboBox<String> deptBox = new JComboBox<>(DEPARTMENTS); JTextField roomF = createStyledTextField();
            Object[] fields = {"Doctor ID:", idF, "Full Name:", nameF, "Select Department:", deptBox, "Room Assignment:", roomF};
            
            int opt = JOptionPane.showConfirmDialog(this, fields, "Register New Doctor", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION && !idF.getText().trim().isEmpty() && !nameF.getText().trim().isEmpty()) {
                Doctor d = new Doctor(idF.getText().trim(), nameF.getText().trim(), (String)deptBox.getSelectedItem(), roomF.getText().trim());
                doctorList.add(d);
                DoctorRosterStore.addDoctor(d.getId(), d.getName(), d.getDepartment());
                showDoctorsManagementDashboard();
            }
        });

        btnToggle.addActionListener(e -> {
            for (int i = 0; i < allTables.size(); i++) {
                int sIdx = allTables.get(i).getSelectedRow();
                if (sIdx != -1) {
                    Doctor doc = doctorsPerDept.get(i).get(sIdx);
                    doc.toggleStatus();
                    DoctorRosterStore.setDoctorActive(doc.getId(), doc.isActive());
                    if (!doc.isActive()) {
                        String msg = "Dr. " + doc.getName() + " is on leave.";
                        HospitalSystem.addNotification(msg);
                        autoReassignOnLeave(doc);
                    }
                    showDoctorsManagementDashboard();
                    return;
                }
            }
        });

        btnUpdate.addActionListener(e -> {
            Doctor target = null;
            for (int i = 0; i < allTables.size(); i++) {
                int sIdx = allTables.get(i).getSelectedRow();
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
            for (int i = 0; i < allTables.size(); i++) {
                int sIdx = allTables.get(i).getSelectedRow();
                if (sIdx != -1 && JOptionPane.showConfirmDialog(this, "Remove doctor permanently?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    Doctor target = doctorsPerDept.get(i).get(sIdx);
                    doctorList.remove(target);
                    DoctorRosterStore.removeDoctor(target.getId());
                    showDoctorsManagementDashboard();
                    return;
                }
            }
        });

        displayPanel.add(mainScroll);
        displayPanel.add(Box.createVerticalStrut(12));
        displayPanel.add(controlDeck);
        displayPanel.revalidate(); displayPanel.repaint();
    }

    // =========================================================================
    //          AUTO-REASSIGNMENT WHEN DOCTOR IS SET ON LEAVE
    // =========================================================================
    private void autoReassignOnLeave(Doctor docOnLeave) {
        String docName = docOnLeave.getName();
        String dept = docOnLeave.getDepartment();

        java.util.List<Doctor> candidates = new ArrayList<>();
        for (Doctor d : doctorList) {
            if (d != docOnLeave && d.isActive() && d.getDepartment().equals(dept)) {
                candidates.add(d);
            }
        }

        if (candidates.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Doctor " + docName + " is now ON LEAVE.\nNo other active doctors in " + dept + " — existing appointments still reference " + docName + ".",
                "No Replacement Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String drName = "Dr. " + docName;
        int reassigned = 0, failed = 0;
        StringBuilder done = new StringBuilder();

        for (Appointment appt : sharedAppointmentList) {
            if (!appt.doctorName.equals(drName)) continue;

            String timeSlot = appt.timeSlot;
            int lastIdx = timeSlot.lastIndexOf(' ');
            String time = timeSlot.substring(0, lastIdx);
            time = time.substring(time.lastIndexOf(' ') + 1) + " " + timeSlot.substring(lastIdx + 1);
            // Now time is "08:00 AM" — extract date as the prefix before the time part
            String date = appt.timeSlot.substring(0, appt.timeSlot.indexOf(time) - 1);

            Doctor replacement = findReplacementForTime(candidates, time);
            if (replacement != null) {
                String newDr = "Dr. " + replacement.getName();
                appt.doctorName = newDr;
                HospitalSystem.triggerAdminReassign(appt.appointmentId, appt.patientName,
                    date, time, newDr, dept);
                reassigned++;
                done.append("  ").append(appt.appointmentId).append(" — ").append(appt.patientName)
                    .append(" → Dr. ").append(replacement.getName()).append("\n");
            } else {
                failed++;
            }
        }

        if (reassigned > 0 || failed > 0) {
            StringBuilder msg = new StringBuilder();
            msg.append(docName).append(" is now ON LEAVE.\n");
            if (reassigned > 0) msg.append("Reassigned ").append(reassigned).append(" appointment(s):\n").append(done);
            if (failed > 0) msg.append("Could not find a replacement for ").append(failed).append(" appointment(s) (no matching shift).");
            JOptionPane.showMessageDialog(this, msg.toString(), "Auto-Reassignment Complete",
                reassigned > 0 ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
        }
    }

    private Doctor findReplacementForTime(java.util.List<Doctor> candidates, String time12h) {
        for (Doctor d : candidates) {
            for (String shift : d.getShiftSlots()) {
                if (timeInShift(time12h, shift)) return d;
            }
        }
        return null;
    }

    private boolean timeInShift(String time12h, String shiftRange) {
        int apptMin = parseTimeMins(time12h);
        if (apptMin < 0) return true;
        String[] ends = shiftRange.split("\\s*to\\s*", 2);
        if (ends.length != 2) return false;
        int startMin = parseTimeMins(ends[0]);
        int endMin = parseTimeMins(ends[1]);
        if (startMin < 0 || endMin < 0) return true;
        if (endMin < startMin) endMin += 1440;
        int adjustedAppt = apptMin < startMin ? apptMin + 1440 : apptMin;
        return adjustedAppt >= startMin && adjustedAppt <= endMin;
    }

    private int parseTimeMins(String t) {
        t = t.trim().toLowerCase();
        try {
            String num = t.replaceAll("[^0-9]", "");
            if (num.isEmpty()) return -1;
            int h = Integer.parseInt(num);
            int m = t.contains(":") ? Integer.parseInt(t.split(":")[1].replaceAll("[^0-9]", "")) : 0;
            boolean pm = t.contains("pm");
            if (pm && h != 12) h += 12;
            if (!pm && h == 12) h = 0;
            return h * 60 + m;
        } catch (Exception e) {
            return -1;
        }
    }

    // =========================================================================
    //       ADJUST APPOINTMENT TIMES WHEN DOCTOR'S SHIFT CHANGES
    // =========================================================================
    private void adjustAppointmentTimes(Doctor doc, String[] newShiftGroup) {
        String rawName = doc.getName();
        String drName = "Dr. " + rawName;

        // Get all expanded slots from the new shift group
        java.util.Set<String> newSlots = new java.util.LinkedHashSet<>();
        for (String range : newShiftGroup) {
            newSlots.addAll(DoctorRosterStore.getExpandedTimeSlotsByName(rawName));
        }
        if (newSlots.isEmpty()) return;

        java.util.List<String> slotList = new ArrayList<>(newSlots);
        int moved = 0;

        for (Appointment appt : sharedAppointmentList) {
            if (!appt.doctorName.equals(drName)) continue;

            String timeSlot = appt.timeSlot;
            int lastSpace = timeSlot.lastIndexOf(' ');
            String time = timeSlot.substring(0, lastSpace);
            time = time.substring(time.lastIndexOf(' ') + 1) + " " + timeSlot.substring(lastSpace + 1);
            String date = appt.timeSlot.substring(0, appt.timeSlot.indexOf(time) - 1);

            if (newSlots.contains(time)) continue;

            // Convert display date to YYYY-MM-DD for slot booking
            String dateYmd = "";
            try {
                java.text.SimpleDateFormat sdfIn = new java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale.ENGLISH);
                java.text.SimpleDateFormat sdfOut = new java.text.SimpleDateFormat("yyyy-MM-dd");
                dateYmd = sdfOut.format(sdfIn.parse(date));
            } catch (Exception e) { continue; }

            String newTime = null;
            for (String s : slotList) {
                if (!HospitalSystem.isSlotBooked(rawName, dateYmd, s)) {
                    newTime = s;
                    break;
                }
            }
            if (newTime == null) continue;

            appt.timeSlot = date + " " + newTime;

            HospitalSystem.clearSlot(rawName, dateYmd, time);
            HospitalSystem.markSlotBooked(rawName, dateYmd, newTime);
            HospitalSystem.addSharedAppointment(appt.appointmentId, appt.patientName, drName,
                doc.getDepartment(), date, newTime, "Rescheduled");
            HospitalSystem.triggerTimeAdjust(rawName, time, newTime, date, appt.appointmentId, appt.patientName);
            HospitalSystem.cancelFromDoctorStore(appt.patientName, date, time);

            moved++;
        }

        if (moved > 0) {
            JOptionPane.showMessageDialog(this,
                "Shift changed for " + rawName + ".\n" + moved + " appointment(s) moved to new time slots.",
                "Appointments Adjusted", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // =========================================================================
    //             RECEPTIONIST VIEWS (CONSOLIDATED HOSPITAL DESK)
    // =========================================================================
    private void showReceptionistManagementDashboard() {
        if (refreshTimer != null) refreshTimer.stop();
        displayPanel.removeAll();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        String[] recepCols = {"ID", "Name", "Shifts"};
        DefaultTableModel tableModel = new DefaultTableModel(recepCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (Receptionist recep : receptionistList) {
            tableModel.addRow(new Object[]{recep.getId(), recep.getName(), recep.getShiftsFormatted()});
        }

        JTable recepTable = createAdminTable(tableModel, -1);
        JScrollPane scroll = createCleanScrollPane(recepTable);
        JPanel consolidatedBox = createDepartmentBox("Centralized Hospital Reception Registry", scroll);

        JPanel controlDeck = new JPanel(new GridLayout(1, 4, 10, 0));
        controlDeck.setOpaque(false);
        controlDeck.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnHire = createAnimatedButton("HIRE RECEPTIONIST", ReceptionistUIHelper.C_BLUE, ReceptionistUIHelper.C_BLUE_MID);
        JButton btnShifts = createAnimatedButton("MANAGE SHIFTS", ReceptionistUIHelper.C_BLUE, ReceptionistUIHelper.C_BLUE_MID);
        JButton btnUpdate = createAnimatedButton("MODIFY INFO", ReceptionistUIHelper.C_MID, ReceptionistUIHelper.C_MUTED);
        JButton btnFire = createAnimatedButton("FIRE RECEPTIONIST", ReceptionistUIHelper.C_RED, ReceptionistUIHelper.C_RED.darker());
        
        Font smallerBtnFont = ReceptionistUIHelper.F_BOLD_S;
        btnHire.setFont(smallerBtnFont); btnShifts.setFont(smallerBtnFont); btnUpdate.setFont(smallerBtnFont); btnFire.setFont(smallerBtnFont);
        
        controlDeck.add(btnHire); 
        controlDeck.add(btnShifts); 
        controlDeck.add(btnUpdate); 
        controlDeck.add(btnFire);

        btnHire.addActionListener(e -> {
            JTextField idField = createStyledTextField(); 
            JTextField nameField = createStyledTextField(); 
            Object[] fields = {"Receptionist ID:", idField, "Full Staff Name:", nameField};
            
            int opt = JOptionPane.showConfirmDialog(this, fields, "Hire Receptionist Staff", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION && !idField.getText().trim().isEmpty() && !nameField.getText().trim().isEmpty()) {
                String id = idField.getText().trim();
                String name = nameField.getText().trim();
                receptionistList.add(new Receptionist(id, name));
                ReceptionistRosterStore.addReceptionist(id, name);
                showReceptionistManagementDashboard();
            }
        });

        btnShifts.addActionListener(e -> {
            int selectedIdx = recepTable.getSelectedRow();
            if (selectedIdx == -1) {
                JOptionPane.showMessageDialog(this, "Please select a receptionist from the list.", "Selection Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Receptionist targetRecep = receptionistList.get(selectedIdx);

            String[] groupLabels = {
                "1st Receptionist: 8 am to 11 am | 12 pm to 2 pm",
                "2nd Receptionist: 2pm to 5pm | 6pm to 8pm",
                "3rd Receptionist: 8pm to 9pm | 9pm to 2am",
                "4th Receptionist: 2am to 4am | 5am to 8am"
            };
            String[][] shiftGroups = {
                {"8 am to 11 am", "12 pm to 2 pm"},
                {"2pm to 5pm", "6pm to 8pm"},
                {"8pm to 9pm", "9pm to 2am"},
                {"2am to 4am", "5am to 8am"}
            };

            String selected = (String) JOptionPane.showInputDialog(this,
                "Select time slot for " + targetRecep.getName() + ":",
                "Assign Shifts", JOptionPane.PLAIN_MESSAGE, null, groupLabels, groupLabels[0]);
            if (selected != null) {
                int idx = java.util.Arrays.asList(groupLabels).indexOf(selected);
                targetRecep.getShiftSlots().clear();
                for (String s : shiftGroups[idx]) targetRecep.addShiftSlot(s);
                syncReceptionistShifts(targetRecep);
                showReceptionistManagementDashboard();
            }
        });

        btnUpdate.addActionListener(e -> {
            int selectedIdx = recepTable.getSelectedRow();
            if (selectedIdx != -1) {
                Receptionist target = receptionistList.get(selectedIdx);
                String newName = JOptionPane.showInputDialog(this, "Modify Staff Name:", target.getName());
                if (newName != null && !newName.trim().isEmpty()) {
                    target.setName(newName.trim());
                    showReceptionistManagementDashboard();
                }
            }
        });

        btnFire.addActionListener(e -> {
            int selectedIdx = recepTable.getSelectedRow();
            if (selectedIdx != -1 && JOptionPane.showConfirmDialog(this, "Terminate employee desk account?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                String name = receptionistList.get(selectedIdx).getName();
                String id = receptionistList.get(selectedIdx).getId();
                receptionistList.remove(selectedIdx);
                ReceptionistRosterStore.removeReceptionist(id);
                showReceptionistManagementDashboard();
            }
        });

        displayPanel.add(consolidatedBox);
        displayPanel.add(Box.createVerticalStrut(12));
        displayPanel.add(controlDeck);
        displayPanel.revalidate(); displayPanel.repaint();
    }

    // =========================================================================
    //                         STANDARD DATA VIEWPORTS
    // =========================================================================
    private String fetchPatientCondition(String patientName) {
        // 1. Check doctor's diagnosis from doctorDataStore (set during consultation)
        String dsDiag = doctorDataStore.get()
            .getMedicalHistory(patientName).get("Diagnosis");
        if (dsDiag != null && !dsDiag.isEmpty() && !dsDiag.equals("--")) {
            return dsDiag;
        }
        // 2. Check fetchPatientProfile for Diagnosis or ChronicConditions
        java.util.LinkedHashMap<String, String> profile = HospitalSystem.fetchPatientProfile(patientName);
        if (profile != null) {
            if (profile.containsKey("Diagnosis")) {
                return profile.get("Diagnosis");
            }
            if (profile.containsKey("ChronicConditions")) {
                return profile.get("ChronicConditions");
            }
        }
        return "--";
    }

    private String fetchPatientWard(String patientName) {
        // Ward = the patient's department
        // 1. Check department from sharedPatientList (stored as "disease" field)
        for (Patient p : sharedPatientList) {
            if (p.getName().equalsIgnoreCase(patientName) && p.getDisease() != null
                && !p.getDisease().equalsIgnoreCase("General")) {
                return p.getDisease();
            }
        }
        // 2. Check department from shared appointments
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
            if (sa.patientName.equalsIgnoreCase(patientName) && sa.department != null
                && !sa.department.equalsIgnoreCase("General") && !sa.department.equals("--")) {
                return sa.department;
            }
        }
        // 3. Check department from shared emergencies
        for (HospitalSystem.SharedEmergencyCase sec : HospitalSystem.getSharedEmergencies()) {
            if (sec.patientName.equalsIgnoreCase(patientName) && sec.department != null
                && !sec.department.equalsIgnoreCase("General") && !sec.department.equals("--")) {
                return sec.department;
            }
        }
        return "General Ward";
    }

    private void showPatientsListLookup() {
        if (refreshTimer != null) refreshTimer.stop();
        displayPanel.removeAll();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        String[] cols = {"Patient ID", "Name", "Disease / Condition", "Ward"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        java.util.LinkedHashSet<String> seenNames = new java.util.LinkedHashSet<>();

        // Shared patient list (receptionist / patient registrations)
        for (Patient pat : sharedPatientList) {
            if (!seenNames.contains(pat.getName().toLowerCase())) {
                String condition = fetchPatientCondition(pat.getName());
                String ward = fetchPatientWard(pat.getName());
                tableModel.addRow(new Object[]{pat.getId(), pat.getName(), condition, ward});
                seenNames.add(pat.getName().toLowerCase());
            }
        }
        // 3. Appointments from HospitalSystem (doctor-approved patients)
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
            if (!seenNames.contains(sa.patientName.toLowerCase())) {
                String condition = fetchPatientCondition(sa.patientName);
                String ward = fetchPatientWard(sa.patientName);
                tableModel.addRow(new Object[]{"SHARED", sa.patientName, condition, ward});
                seenNames.add(sa.patientName.toLowerCase());
            }
        }

        JTable patientTable = createAdminTable(tableModel, 2);
        JScrollPane scroll = createCleanScrollPane(patientTable);

        // Build merged list for detail lookup on row selection
        java.util.List<Patient> mergedPatients = new java.util.ArrayList<>();
        java.util.LinkedHashSet<String> mergedSeen = new java.util.LinkedHashSet<>();
        for (Patient p : sharedPatientList) {
            if (!mergedSeen.contains(p.getName().toLowerCase())) { mergedPatients.add(p); mergedSeen.add(p.getName().toLowerCase()); }
        }
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
            if (!mergedSeen.contains(sa.patientName.toLowerCase())) {
                mergedPatients.add(new Patient("PAT-" + sa.patientName.replace(" ", ""), sa.patientName, sa.department, "General Ward"));
                mergedSeen.add(sa.patientName.toLowerCase());
            }
        }

        JPanel inspectionCard = new JPanel(new GridLayout(5, 1, 5, 5));
        inspectionCard.setBackground(ReceptionistUIHelper.C_WHITE);
        inspectionCard.setPreferredSize(new Dimension(300, 0));
        inspectionCard.setBorder(new CompoundBorder(new ReceptionistUIHelper.PanelShadowBorder(), new EmptyBorder(20, 20, 20, 20)));

        JLabel lblHeading = new JLabel("PATIENT DETAILS", SwingConstants.CENTER);
        lblHeading.setFont(ReceptionistUIHelper.F_HEAD); lblHeading.setForeground(ReceptionistUIHelper.C_NAVY);
        JLabel lblNameData = new JLabel("Patient: --", SwingConstants.LEFT);
        lblNameData.setForeground(ReceptionistUIHelper.C_DARK); lblNameData.setFont(ReceptionistUIHelper.F_BODY);
        JLabel lblDiseaseData = new JLabel("Condition: --", SwingConstants.LEFT);
        lblDiseaseData.setForeground(ReceptionistUIHelper.C_MID); lblDiseaseData.setFont(ReceptionistUIHelper.F_BODY);
        JLabel lblWardData = new JLabel("Ward: General Ward", SwingConstants.LEFT);
        lblWardData.setForeground(ReceptionistUIHelper.C_MID); lblWardData.setFont(ReceptionistUIHelper.F_BODY);
        JLabel lblMoreData = new JLabel("Select a patient row for details", SwingConstants.CENTER);
        lblMoreData.setForeground(ReceptionistUIHelper.C_MUTED); lblMoreData.setFont(ReceptionistUIHelper.F_SMALL);

        JPanel sep = new JPanel(); sep.setBackground(ReceptionistUIHelper.C_DIVIDER); sep.setPreferredSize(new Dimension(0, 1));
        inspectionCard.add(lblHeading); inspectionCard.add(sep);
        inspectionCard.add(lblNameData); inspectionCard.add(lblDiseaseData); inspectionCard.add(lblWardData); inspectionCard.add(lblMoreData);

        patientTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = patientTable.getSelectedRow();
                if (row != -1 && row < mergedPatients.size()) {
                    Patient target = mergedPatients.get(row);
                    String condition = fetchPatientCondition(target.getName());
                    String ward = fetchPatientWard(target.getName());
                    lblNameData.setText("Patient: " + target.getName());
                    lblDiseaseData.setText("Condition: " + condition);
                    lblWardData.setText("Ward: " + ward);
                }
            }
        });

        displayPanel.add(createDepartmentBox("Global Patient Directory (LIVE)", scroll));
        displayPanel.add(Box.createVerticalStrut(12));
        displayPanel.add(inspectionCard);
        displayPanel.revalidate(); displayPanel.repaint();
    }

    private javax.swing.Timer refreshTimer;

    private void showMasterAppointmentSchedule() {
        displayPanel.removeAll();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        String[] cols = {"Appt ID", "Patient", "Doctor", "Department", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        fillAppointmentModel(tableModel);

        JTable apptTable = createAdminTable(tableModel, 4);
        displayPanel.add(createDepartmentBox("Central Master Appointment Logs (LIVE)", createCleanScrollPane(apptTable)));
        displayPanel.revalidate(); displayPanel.repaint();

        if (refreshTimer != null) refreshTimer.stop();
        refreshTimer = new javax.swing.Timer(3000, e -> {
            if (!isVisible() || displayPanel.getComponentCount() == 0) return;
            DefaultTableModel fresh = new DefaultTableModel(cols, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            fillAppointmentModel(fresh);
            tableModel.setRowCount(0);
            for (int r = 0; r < fresh.getRowCount(); r++)
                tableModel.addRow(fresh.getDataVector().elementAt(r));
        });
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    private void fillAppointmentModel(DefaultTableModel model) {
        try {
            for (int r = 0; r < ReceptionistDataStore.apptModel.getRowCount(); r++) {
                String apptId = ReceptionistDataStore.apptModel.getValueAt(r, 0).toString();
                String patient= ReceptionistDataStore.apptModel.getValueAt(r, 1).toString();
                String doctor = ReceptionistDataStore.apptModel.getValueAt(r, 4).toString();
                String dept   = ReceptionistDataStore.apptModel.getValueAt(r, 5).toString();
                String raw = ReceptionistDataStore.appointmentStatusMap.getOrDefault(apptId, "Booked");
                String status = "Accepted".equals(raw) ? "Booked" : raw;
                model.addRow(new Object[]{apptId, patient, doctor, dept, status});
            }
        } catch (Exception e) {}
        if (model.getRowCount() == 0)
            model.addRow(new Object[]{"--", "No appointments scheduled", "--", "--", "--"});
    }

    private DefaultListModel<String> buildAppointmentListModel() {
        DefaultListModel<String> model = new DefaultListModel<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        // 1. Local admin appointments
        for (Appointment app : appointmentList) {
            String key = app.getDetails();
            if (!seen.contains(key)) { seen.add(key); model.addElement(" " + key + "  |  Status: Registered"); }
        }
        // 2. Shared appointments from HospitalSystem (receptionist + doctor cross-system)
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
            String detail = "Appt ID: " + sa.apptId + "  |  Patient: " + sa.patientName
                + "  -->  Doctor: " + sa.doctorName + "  |  Dept: " + sa.department
                + "  |  " + sa.date + "  " + sa.time;
            String statusBadge = "  |  Status: [" + sa.status + "]";
            String key = detail;
            if (!seen.contains(key)) { seen.add(key); model.addElement(" " + detail + statusBadge); }
        }
        if (model.isEmpty()) model.addElement(" No appointments scheduled yet.");
        return model;
    }

    private void setupRefreshTimer(Runnable onRefresh) {
        if (refreshTimer != null) { refreshTimer.stop(); }
        refreshTimer = new javax.swing.Timer(3000, e -> {
            if (isVisible()) onRefresh.run();
        });
        refreshTimer.setRepeats(true);
        refreshTimer.start();
    }

    private void showActiveEmergencies() {
        displayPanel.removeAll();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        String[] cols = {"Emergency ID", "Patient", "Department", "Complaint", "Severity", "Doctor"};
        DefaultTableModel tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        java.util.List<Object[]> emergRows = new java.util.ArrayList<>();
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();

        for (EmergencyCase em : emergencyList) {
            String docName = em.getAssignedDoctor() != null ? "Dr. " + em.getAssignedDoctor().getName() : "Unassigned";
            String dept = em.getAssignedDoctor() != null ? em.getAssignedDoctor().getDepartment() : "N/A";
            if (seen.add(em.patientName.toLowerCase())) {
                emergRows.add(new Object[]{em.caseId, em.patientName, dept, "--", em.getSeverityLevel(), docName});
            }
        }
        for (HospitalSystem.SharedEmergencyCase sec : HospitalSystem.getSharedEmergencies()) {
            if (seen.add(sec.patientName.toLowerCase())) {
                emergRows.add(new Object[]{sec.caseId, sec.patientName, sec.department, sec.complaint, sec.severity, sec.doctorName});
            }
        }
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments()) {
            if (sa.department != null && sa.department.equalsIgnoreCase("Emergency Medicine")) {
                if (seen.add(sa.patientName.toLowerCase())) {
                    emergRows.add(new Object[]{sa.apptId, sa.patientName, sa.department, "Appointment registration", "Medium", sa.doctorName});
                }
            }
        }
        System.out.println("[DEBUG Admin] emergencies: local=" + emergencyList.size() + " shared=" + HospitalSystem.getSharedEmergencies().size() + " sharedAppts=" + HospitalSystem.getSharedAppointments().size());
        if (emergRows.isEmpty())
            emergRows.add(new Object[]{"--", "No active emergency cases", "--", "--", "--", "--"});

        for (Object[] row : emergRows) tableModel.addRow(row);

        JTable emergencyTable = createAdminTable(tableModel, 4);
        JScrollPane scroll = createCleanScrollPane(emergencyTable);
        
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        controls.setOpaque(false); controls.setBorder(new EmptyBorder(12, 0, 0, 0));
        JButton btnAssignDoc = createAnimatedButton("RE-ASSIGN DOCTOR", ReceptionistUIHelper.C_MID, ReceptionistUIHelper.C_MUTED);
        btnAssignDoc.setPreferredSize(new Dimension(160, 32));
        JButton btnPrioritize = createAnimatedButton("CHANGE PRIORITY RANK", ReceptionistUIHelper.C_RED, ReceptionistUIHelper.C_RED.darker());
        btnPrioritize.setPreferredSize(new Dimension(190, 32));
        controls.add(btnAssignDoc); controls.add(btnPrioritize);

        btnAssignDoc.addActionListener(e -> {
            int selectedIdx = emergencyTable.getSelectedRow();
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

            if (selectedIdx < localEmergCount) {
                emergencyList.get(selectedIdx).setAssignedDoctor(newDoc);
            } else if (selectedIdx < localEmergCount + sharedEmergCount) {
                int emergIdx = selectedIdx - localEmergCount;
                HospitalSystem.SharedEmergencyCase sec = HospitalSystem.getSharedEmergencies().get(emergIdx);
                HospitalSystem.triggerAdminReassign(
                    sec.caseId, sec.patientName, sec.date, sec.time,
                    "Dr. " + newDoc.getName(), newDept);
            } else {
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

        displayPanel.add(createDepartmentBox("Active Live Emergency Trauma Stream (LIVE)", scroll));
        displayPanel.add(Box.createVerticalStrut(12));
        displayPanel.add(controls);
        displayPanel.revalidate(); displayPanel.repaint();

        setupRefreshTimer(() -> {
            if (displayPanel.getComponentCount() > 0 && isVisible()) {
                showActiveEmergencies();
            }
        });
    }

    // =========================================================================
    //                   INTERACTIVE DASHBOARD REPORT CARDS
    // =========================================================================
    private void showComprehensiveInsightReport() {
        if (refreshTimer != null) refreshTimer.stop();
        displayPanel.removeAll();
        displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS));

        // ── Live patient count from all sources ───────────────────────────
        java.util.Set<String> patNames = new java.util.LinkedHashSet<>();
        for (Patient p : sharedPatientList) patNames.add(p.getName().toLowerCase());
        for (HospitalSystem.SharedAppointment sa : HospitalSystem.getSharedAppointments())
            patNames.add(sa.patientName.toLowerCase());
        try {
            for (int i = 0; i < ReceptionistDataStore.apptModel.getRowCount(); i++)
                patNames.add(ReceptionistDataStore.apptModel.getValueAt(i, 1).toString().toLowerCase());
            for (int i = 0; i < ReceptionistDataStore.walkModel.getRowCount(); i++)
                patNames.add(ReceptionistDataStore.walkModel.getValueAt(i, 1).toString().toLowerCase());
        } catch (Exception e) {}
        javax.swing.table.DefaultTableModel overall = doctorDataStore.get().getOverallPatientModel();
        for (int i = 0; i < overall.getRowCount(); i++)
            patNames.add(overall.getValueAt(i, 0).toString().toLowerCase());
        int totalPatients = patNames.size();

        // ── Live appointment count (apptModel has ALL appointments, no double-count) ──
        int totalAppts = 0;
        try { totalAppts = ReceptionistDataStore.apptModel.getRowCount(); } catch (Exception e) {}

        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0)); stats.setOpaque(false);
        stats.add(createStatCard("👨‍⚕️", "Active Doctors", String.valueOf(doctorList.size()), "registered", ReceptionistUIHelper.C_BLUE, () -> showDoctorsManagementDashboard()));
        stats.add(createStatCard("👩‍💼", "Receptionists", String.valueOf(receptionistList.size()), "registered", ReceptionistUIHelper.C_NAVY, () -> showReceptionistManagementDashboard()));
        stats.add(createStatCard("🧑‍⚕️", "Total Patients", String.valueOf(totalPatients), "registered", ReceptionistUIHelper.C_GREEN, () -> showPatientsListLookup()));
        stats.add(createStatCard("📅", "Appointments", String.valueOf(totalAppts), "scheduled", ReceptionistUIHelper.C_AMBER, () -> showMasterAppointmentSchedule()));

        displayPanel.add(stats);
        displayPanel.add(Box.createVerticalStrut(22));

        JPanel stats2 = new JPanel(new GridLayout(1, 2, 16, 0)); stats2.setOpaque(false);
        // Live emergency count from all sources (deduplicated by patient name)
        java.util.Set<String> emergNames = new java.util.LinkedHashSet<>();
        javax.swing.table.DefaultTableModel emgModel = doctorDataStore.get().getEmergencyModel();
        for (int i = 0; i < emgModel.getRowCount(); i++)
            emergNames.add(emgModel.getValueAt(i, 1).toString().toLowerCase());
        for (HospitalSystem.SharedEmergencyCase sec : HospitalSystem.getSharedEmergencies())
            emergNames.add(sec.patientName.toLowerCase());
        int totalEmerg = emergNames.size();
        stats2.add(createStatCard("🚑", "Active Emergencies", String.valueOf(totalEmerg), "trauma cases", ReceptionistUIHelper.C_RED, () -> showActiveEmergencies()));
        stats2.add(createStatCard("🏥", "Clinical Units", "5 Departments", "operational", ReceptionistUIHelper.C_INDIGO, () -> showDoctorsManagementDashboard()));
        displayPanel.add(Box.createVerticalStrut(12));
        displayPanel.add(stats2);

        displayPanel.revalidate(); displayPanel.repaint();

        setupRefreshTimer(() -> {
            if (displayPanel.getComponentCount() > 0 && isVisible()) {
                showComprehensiveInsightReport();
            }
        });
    }

    private JPanel createStatCard(String icon, String title, String value, String sub, Color accent, Runnable onClick) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            boolean hover = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hover = true;  repaint(); }
                    public void mouseExited(MouseEvent e)  { hover = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { if (onClick != null) onClick.run(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hover ? ReceptionistUIHelper.C_SKY : ReceptionistUIHelper.C_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                if (hover) {
                    g2.setColor(ReceptionistUIHelper.C_BORDER);
                    g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                }
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(new ReceptionistUIHelper.PanelShadowBorder(), new EmptyBorder(16, 20, 16, 16)));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel ico = new JLabel(icon); ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel val = new JLabel(value); val.setFont(new Font("Segoe UI", Font.BOLD, 28)); val.setForeground(ReceptionistUIHelper.C_DARK);
        JLabel ttl = new JLabel(title); ttl.setFont(ReceptionistUIHelper.F_BOLD_S); ttl.setForeground(ReceptionistUIHelper.C_MID);
        JLabel s   = new JLabel(sub);   s.setFont(ReceptionistUIHelper.F_SMALL);    s.setForeground(ReceptionistUIHelper.C_MUTED);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(ico, BorderLayout.WEST); top.add(val, BorderLayout.EAST);
        JPanel bot = new JPanel(new GridLayout(2, 1, 0, 2)); bot.setOpaque(false);
        bot.add(ttl); bot.add(s);
        card.add(top, BorderLayout.NORTH); card.add(bot, BorderLayout.CENTER);
        return card;
    }

    private JPanel createReportCard(String title, String metrics, Color indicatorColor) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ReceptionistUIHelper.C_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(indicatorColor);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(new ReceptionistUIHelper.PanelShadowBorder(), new EmptyBorder(16, 20, 16, 16)));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JLabel lblMetrics = new JLabel(metrics, SwingConstants.CENTER);
        lblMetrics.setFont(new Font("Segoe UI", Font.BOLD, 28)); lblMetrics.setForeground(ReceptionistUIHelper.C_DARK);
        
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(ReceptionistUIHelper.F_BOLD_S); lblTitle.setForeground(ReceptionistUIHelper.C_MID);

        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(lblMetrics, BorderLayout.EAST);
        JPanel bot = new JPanel(new GridLayout(2,1,0,2)); bot.setOpaque(false);
        bot.add(lblTitle);
        card.add(top, BorderLayout.NORTH); card.add(bot, BorderLayout.CENTER);
        return card;
    }

    public static void preseedRosterStore() {
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
                String id = "DOC-" + dept.substring(0,3).toUpperCase() + "-0" + i;
                String name = docNames[nameIdx++];
                DoctorRosterStore.addDoctor(id, name, dept);
                if (i == 1) {
                    DoctorRosterStore.addShiftRange(id, "8 am to 11 am");
                    DoctorRosterStore.addShiftRange(id, "12 pm to 2 pm");
                } else if (i == 2) {
                    DoctorRosterStore.addShiftRange(id, "2pm to 5pm");
                    DoctorRosterStore.addShiftRange(id, "6pm to 8pm");
                } else if (i == 3) {
                    DoctorRosterStore.addShiftRange(id, "8pm to 9pm");
                    DoctorRosterStore.addShiftRange(id, "9pm to 2am");
                } else if (i == 4) {
                    DoctorRosterStore.addShiftRange(id, "2am to 4am");
                    DoctorRosterStore.addShiftRange(id, "5am to 8am");
                }
            }
        }
    }

    public static void preseedReceptionistStore() {
        String[] recepNames = {"Sarah Ahmed", "Fatima Khan", "Ahmed Raza", "Zainab Hassan"};
        for (int i = 0; i < recepNames.length; i++) {
            String id = "RECEP-" + String.format("%02d", i + 1);
            ReceptionistRosterStore.addReceptionist(id, recepNames[i]);
        }
    }

    private static void syncDoctorShifts(Doctor doc) {
        DoctorRosterStore.clearShiftRanges(doc.getId());
        for (String r : doc.getShiftSlots()) {
            DoctorRosterStore.addShiftRange(doc.getId(), r);
        }
    }

    private static void syncReceptionistShifts(Receptionist recep) {
        ReceptionistRosterStore.clearShiftRanges(recep.getId());
        for (String r : recep.getShiftSlots()) {
            ReceptionistRosterStore.addShiftRange(recep.getId(), r);
        }
    }

    // =========================================================================
    //                             MASTER RUNNER DATA
    // =========================================================================
    public static void main(String[] args) {
        ArrayList<Doctor> mockDoctors = new ArrayList<>();
        ArrayList<Receptionist> mockReceptionists = new ArrayList<>();
        ArrayList<Patient> mockPatients = new ArrayList<>();
        ArrayList<Appointment> mockAppointments = new ArrayList<>();
        ArrayList<EmergencyCase> mockEmergencies = new ArrayList<>();

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
                
                // Assigning precise, distinctive rotational timetables for each of the 4 doctors in a department
                if (i == 1) {
                    d.addShiftSlot("8 am to 11 am");
                    d.addShiftSlot("12 pm to 2 pm");
                } else if (i == 2) {
                    d.addShiftSlot("2pm to 5pm");
                    d.addShiftSlot("6pm to 8pm");
                } else if (i == 3) {
                    d.addShiftSlot("8pm to 9pm");
                    d.addShiftSlot("9pm to 2am");
                } else if (i == 4) {
                    d.addShiftSlot("2am to 4am");
                    d.addShiftSlot("5am to 8am");
                }
                mockDoctors.add(d);
            }
        }

        // Consolidated Hospital-Wide Registry: 4 Receptionists (must match preseedReceptionistStore)
        String[] generalReceps = {"Sarah Ahmed", "Fatima Khan", "Ahmed Raza", "Zainab Hassan"};
        for (int i = 0; i < 4; i++) {
            Receptionist r = new Receptionist("RECEP-0" + (i + 1), generalReceps[i]);
            if (i == 0) {
                r.addShiftSlot("8 am to 11 am");
                r.addShiftSlot("12 pm to 2 pm");
            } else if (i == 1) {
                r.addShiftSlot("2pm to 5pm");
                r.addShiftSlot("6pm to 8pm");
            } else if (i == 2) {
                r.addShiftSlot("8pm to 9pm");
                r.addShiftSlot("9pm to 2am");
            } else if (i == 3) {
                r.addShiftSlot("2am to 4am");
                r.addShiftSlot("5am to 8am");
            }
            mockReceptionists.add(r);
        }

        // Sync active status from DoctorRosterStore (persists across admin sessions)
        for (Doctor d : mockDoctors) {
            d.setActive(DoctorRosterStore.isDoctorActive(d.getId()));
        }

        // Use only patients seeded from Launcher (via sharedPatientList) and real registrations.
        mockPatients.addAll(sharedPatientList);
        mockAppointments.addAll(sharedAppointmentList);

        SwingUtilities.invokeLater(() -> {
            new HospitalAdmin(mockDoctors, mockReceptionists, mockPatients, mockAppointments, mockEmergencies).setVisible(true);
        });
    }
}