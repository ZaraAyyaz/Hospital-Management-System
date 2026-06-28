package Receptionist;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.*;
import System.HospitalSystem;
 
/**
 * ReceptionistDashboard
 * Main application frame. Owns the top bar, sidebar, and content area.
 * Delegates rendering of each section to its dedicated panel class.
 * All shared data lives in ReceptionistDataStore.
 */
public class ReceptionistDashboard extends JFrame
        implements DashboardPanel.NavigationListener,
                   RegisterPatientPanel.NavigationListener,
                   WalkInPanel.NavigationListener,
                   WalkInQueuePanel.NavigationListener {
 
    // ── UI state ─────────────────────────────────────────────────────────────
    private JPanel contentArea;
    private JScrollPane contentScroll;
    private JLabel pageTitle;
    private String activeSection = "Dashboard";
    private final String receptionistName;
    private int notifCount = 0;
 
    // ── Panel builders (instantiated once, reused) ───────────────────────────
    private final DashboardPanel              dashboardPanel;
    private final RegisterPatientPanel        registerPanel;
    private final AppointmentsPanel           appointmentsPanel;
    private final AssignDoctorPanel           assignDoctorPanel;
    private final WalkInPanel                 walkInPanel;
    private final WalkInQueuePanel            walkInQueuePanel;
    private final ReceptionistPrescriptionPanel prescriptionPanel;
 
 
    // ════════════════════════════════════════════════════════════════════════
    //  CONSTRUCTOR
    // ════════════════════════════════════════════════════════════════════════
    public ReceptionistDashboard(String receptionistName) {
        this.receptionistName = receptionistName;
        dashboardPanel    = new DashboardPanel(this, this);
        registerPanel     = new RegisterPatientPanel(this, this);
        appointmentsPanel = new AppointmentsPanel(this);
        assignDoctorPanel = new AssignDoctorPanel(this);
        walkInPanel       = new WalkInPanel(this, this);
        walkInQueuePanel  = new WalkInQueuePanel(this);
        prescriptionPanel = new ReceptionistPrescriptionPanel(this);
 
        setTitle("Smart Portal — Receptionist Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1340, 840);
        setMinimumSize(new Dimension(1020, 660));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(ReceptionistUIHelper.C_BG);
 
        add(buildTopBar(),  BorderLayout.NORTH);
        add(buildSidebar(), BorderLayout.WEST);
 
        // Right content wrapper
        JPanel rw = new JPanel(new BorderLayout());
        rw.setBackground(ReceptionistUIHelper.C_BG);
 
        // Page header strip
        JPanel strip = new JPanel(new BorderLayout());
        strip.setBackground(ReceptionistUIHelper.C_WHITE);
        strip.setBorder(new CompoundBorder(
            new MatteBorder(0, 0, 1, 0, ReceptionistUIHelper.C_DIVIDER),
            new EmptyBorder(16, 28, 16, 28)
        ));
        pageTitle = new JLabel("Dashboard");
        pageTitle.setFont(ReceptionistUIHelper.F_TITLE);
        pageTitle.setForeground(ReceptionistUIHelper.C_DARK);
 
        JLabel tsLabel = new JLabel(
            new SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.ENGLISH).format(new Date()));
        tsLabel.setFont(ReceptionistUIHelper.F_SMALL);
        tsLabel.setForeground(ReceptionistUIHelper.C_MUTED);
        strip.add(pageTitle, BorderLayout.WEST);
        strip.add(tsLabel,   BorderLayout.EAST);
 
        contentArea = new JPanel(new BorderLayout());
        contentArea.setBackground(ReceptionistUIHelper.C_BG);
        contentScroll = new JScrollPane(contentArea);
        contentScroll.setBorder(BorderFactory.createEmptyBorder());
        contentScroll.getVerticalScrollBar().setUnitIncrement(16);
        contentScroll.getHorizontalScrollBar().setUnitIncrement(16);

        rw.add(contentScroll, BorderLayout.CENTER);
        add(rw, BorderLayout.CENTER);
 
        showDashboard();
        setVisible(true);
    }
 
    // ════════════════════════════════════════════════════════════════════════
    //  NavigationListener implementations
    // ════════════════════════════════════════════════════════════════════════
    @Override
    public void navigateTo(String section) {
        activeSection = section;
        pageTitle.setText(section);
        repaintSidebar();
        switch (section) {
            case "Dashboard"         -> showDashboard();
            case "Register Patient"  -> showRegisterPatient();
            case "Appointments"      -> showAppointments();
            case "Assign Doctor"     -> showAssignDoctor("");
            case "Walk-in Admission" -> showWalkIn();
            case "Walk-in Queue"     -> showWalkInQueue();
            case "Prescriptions"     -> showPrescriptions();
            default                  -> showPlaceholder(section);
        }
    }
 
    @Override
    public void navigateToAssignDoctor(String prefilledName) {
        activeSection = "Assign Doctor";
        pageTitle.setText("Assign Doctor");
        repaintSidebar();
        setContent(assignDoctorPanel.build(prefilledName));
    }
 
    // ── Section show methods ──────────────────────────────────────────────────
    private void showDashboard()       { setContent(dashboardPanel.build()); }
    private void showRegisterPatient() { setContent(registerPanel.build()); }
    private void showAppointments()    { setContent(appointmentsPanel.build()); }
    private void showWalkIn()          { setContent(walkInPanel.build()); }
    private void showWalkInQueue()     { setContent(walkInQueuePanel.build()); }
    private void showPrescriptions()   { setContent(prescriptionPanel.build()); }
 
    private void showAssignDoctor(String prefilled) {
        setContent(assignDoctorPanel.build(prefilled));
    }
 
    private void showPlaceholder(String name) {
        JPanel p = ReceptionistUIHelper.page();
        JLabel l = new JLabel(name + " — Coming Soon", SwingConstants.CENTER);
        l.setFont(ReceptionistUIHelper.F_HEAD);
        l.setForeground(ReceptionistUIHelper.C_MUTED);
        l.setPreferredSize(new Dimension(600, 300));
        ReceptionistUIHelper.stack(p, l);
        setContent(p);
    }
 
    private void setContent(JPanel panel) {
        contentArea.removeAll();
        contentArea.setLayout(new BorderLayout());
        contentArea.add(panel, BorderLayout.NORTH);
        contentArea.revalidate(); contentArea.repaint();
        SwingUtilities.invokeLater(() -> {
            if (contentScroll != null) {
                contentScroll.getVerticalScrollBar().setValue(0);
            }
        });
    }
 
    // ════════════════════════════════════════════════════════════════════════
    //  TOP BAR
    // ════════════════════════════════════════════════════════════════════════
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
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
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(new EmptyBorder(0,24,0,24));
 
        // Left: logo + title
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT,12,0)); left.setOpaque(false);
        JLabel cross = ReceptionistUIHelper.lbl("✚", new Font("Segoe UI",Font.BOLD,22), new Color(100,180,255));
        JLabel title = ReceptionistUIHelper.lbl("Smart Portal", new Font("Segoe UI",Font.BOLD,17), ReceptionistUIHelper.C_WHITE);
        JLabel sep   = ReceptionistUIHelper.lbl(" | ", new Font("Segoe UI",Font.PLAIN,16), new Color(255,255,255,50));
        JLabel sub   = ReceptionistUIHelper.lbl("Receptionist Portal", new Font("Segoe UI",Font.PLAIN,13), new Color(200,220,255));
        left.add(cross); left.add(title); left.add(sep); left.add(sub);
 
        // Right: user name + refresh + logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,14,0)); right.setOpaque(false);

        JLabel nameTag = new JLabel(receptionistName);
        nameTag.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameTag.setForeground(new Color(200,220,255));

        JButton refreshBtn = new JButton("⟳") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,20));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        refreshBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        refreshBtn.setForeground(new Color(200,220,255));
        refreshBtn.setContentAreaFilled(false); refreshBtn.setBorderPainted(false); refreshBtn.setFocusPainted(false);
        refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        refreshBtn.setPreferredSize(new Dimension(34,34));
        refreshBtn.setToolTipText("Refresh current view");
        refreshBtn.addActionListener(ev -> navigateTo(activeSection));

        JButton logoutBtn = new JButton("Logout →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if      (getModel().isPressed())  { g2.setColor(new Color(220,50,50)); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6); }
                else if (getModel().isRollover()) { g2.setColor(new Color(200,30,30)); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6); }
                else {
                    g2.setColor(new Color(255,255,255,18)); g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                    g2.setColor(new Color(255,255,255,60)); g2.setStroke(new BasicStroke(1f));
                    g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,6,6);
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        logoutBtn.setFont(ReceptionistUIHelper.F_BOLD_S); logoutBtn.setForeground(ReceptionistUIHelper.C_WHITE);
        logoutBtn.setContentAreaFilled(false); logoutBtn.setBorderPainted(false); logoutBtn.setFocusPainted(false);
        logoutBtn.setBorder(new EmptyBorder(7,14,7,14));
        logoutBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?", "Confirm Logout",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                dispose();
                if (HospitalSystem.showLauncher != null) {
                    HospitalSystem.showLauncher.run();
                }
            }
        });
 
        right.add(nameTag);

        // Notification bell
        JButton bellBtn = new JButton("🔔") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,20));
                    g2.fillRoundRect(0,0,getWidth(),getHeight(),6,6);
                }
                super.paintComponent(g);
                if (notifCount > 0) {
                    g2.setColor(new Color(220,50,50));
                    g2.fillOval(getWidth()-16, 2, 14, 14);
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                    String c = notifCount > 9 ? "9+" : String.valueOf(notifCount);
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(c, getWidth()-16+(14-fm.stringWidth(c))/2, 2+14-(14-fm.getHeight())/2+fm.getAscent()-2);
                }
                g2.dispose();
            }
        };
        bellBtn.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        bellBtn.setForeground(new Color(200,220,255));
        bellBtn.setContentAreaFilled(false); bellBtn.setBorderPainted(false); bellBtn.setFocusPainted(false);
        bellBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        bellBtn.setPreferredSize(new Dimension(34,34));
        bellBtn.setToolTipText("Notifications");

        // Notification popup list
        JDialog notifPopup = new JDialog(this, "Notifications", false);
        notifPopup.setUndecorated(true);
        JPanel notifPanel = new JPanel();
        notifPanel.setLayout(new BoxLayout(notifPanel, BoxLayout.Y_AXIS));
        notifPanel.setBackground(new Color(30,50,100));
        notifPanel.setBorder(BorderFactory.createLineBorder(new Color(80,130,200), 1));
        JScrollPane notifScroll = new JScrollPane(notifPanel);
        notifScroll.setPreferredSize(new Dimension(280, 200));
        notifScroll.setBorder(null);
        notifPopup.add(notifScroll);
        notifPopup.pack();

        bellBtn.addActionListener(ev -> {
            notifPanel.removeAll();
            java.util.List<String> msgs = HospitalSystem.getNotifications();
            if (msgs.isEmpty()) {
                notifPanel.add(new JLabel("  No notifications."));
            } else {
                for (String m : msgs) {
                    JLabel l = new JLabel("⚠  " + m);
                    l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    l.setForeground(Color.WHITE);
                    l.setBorder(new EmptyBorder(6,10,6,10));
                    l.setAlignmentX(LEFT_ALIGNMENT);
                    notifPanel.add(l);
                }
            }
            notifPopup.pack();
            Point p = bellBtn.getLocationOnScreen();
            notifPopup.setLocation(p.x + bellBtn.getWidth() - notifPopup.getWidth(), p.y + bellBtn.getHeight());
            notifPopup.setVisible(!notifPopup.isVisible());
        });

        // Listen for new notifications
        // Sync existing notifications and listen for new ones
        Runnable syncNotifs = () -> {
            SwingUtilities.invokeLater(() -> {
                java.util.List<String> msgs = HospitalSystem.getNotifications();
                notifCount = msgs.size();
                bellBtn.repaint();
                if (!msgs.isEmpty() && bellBtn.isShowing()) {
                    String latest = msgs.get(msgs.size() - 1);
                    JDialog toast = new JDialog(this, false);
                    toast.setUndecorated(true);
                    toast.setAlwaysOnTop(true);
                    JPanel tp = new JPanel(new BorderLayout());
                    tp.setBackground(new Color(40,70,140));
                    tp.setBorder(new EmptyBorder(10,16,10,16));
                    JLabel tl = new JLabel("⚠  " + latest);
                    tl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    tl.setForeground(Color.WHITE);
                    tp.add(tl);
                    toast.add(tp);
                    toast.pack();
                    Point bp = bellBtn.getLocationOnScreen();
                    toast.setLocation(bp.x + bellBtn.getWidth() - toast.getWidth(), bp.y - toast.getHeight() - 4);
                    toast.setVisible(true);
                    javax.swing.Timer t = new javax.swing.Timer(4000, ev -> { toast.dispose(); });
                    t.setRepeats(false);
                    t.start();
                }
            });
        };
        syncNotifs.run(); // sync any notifications that arrived before this dashboard opened
        HospitalSystem.addNotificationListener(syncNotifs);

        right.add(bellBtn); right.add(refreshBtn); right.add(logoutBtn);
        bar.add(left,  BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }
 
    // ════════════════════════════════════════════════════════════════════════
    //  SIDEBAR
    // ════════════════════════════════════════════════════════════════════════
    private JPanel sidebarRef; // kept to allow repaint from nav callbacks
 
    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(ReceptionistUIHelper.C_SIDEBAR);
        side.setPreferredSize(new Dimension(230, 0));
        sidebarRef = side;
 
        side.add(Box.createVerticalStrut(20));
 
        JLabel sectionLbl = new JLabel("  NAVIGATION");
        sectionLbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        sectionLbl.setForeground(new Color(100,130,200));
        sectionLbl.setAlignmentX(LEFT_ALIGNMENT);
        side.add(sectionLbl);
        side.add(Box.createVerticalStrut(8));
 
        String[][] items = {
            {"🏠", "Dashboard"},
            {"📋", "Register Patient"},
            {"📅", "Appointments"},
            {"👨‍⚕️", "Assign Doctor"},
            {"🚶", "Walk-in Admission"},
            {"📋", "Walk-in Queue"},
            {"💊", "Prescriptions"},
        };
        for (String[] it : items) side.add(navBtn(it[0], it[1], side));
 
        side.add(Box.createVerticalStrut(12));
        JPanel divider = new JPanel();
        divider.setBackground(new Color(255,255,255,20));
        divider.setMaximumSize(new Dimension(210,1));
        divider.setPreferredSize(new Dimension(210,1));
        divider.setAlignmentX(LEFT_ALIGNMENT);
        side.add(divider);
        side.add(Box.createVerticalStrut(10));
 
        // User card
        JPanel userCard = new JPanel(new BorderLayout(10,0));
        userCard.setOpaque(false);
        userCard.setBorder(new EmptyBorder(8,14,10,14));
        userCard.setMaximumSize(new Dimension(230,60));
        userCard.setAlignmentX(LEFT_ALIGNMENT);
 
        String[] nameParts = receptionistName.split(" ");
        String initials = nameParts.length >= 2
            ? (nameParts[0].substring(0,1) + nameParts[1].substring(0,1)).toUpperCase()
            : receptionistName.substring(0, Math.min(2, receptionistName.length())).toUpperCase();

        JPanel ava = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ReceptionistUIHelper.C_BLUE_MID); g2.fillOval(0,0,32,32);
                g2.setColor(ReceptionistUIHelper.C_WHITE);
                g2.setFont(new Font("Segoe UI",Font.BOLD,10));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(initials,(32-fm.stringWidth(initials))/2,(32+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        ava.setOpaque(false); ava.setPreferredSize(new Dimension(32,32));

        JPanel info = new JPanel(new GridLayout(2,1,0,1)); info.setOpaque(false);
        JLabel nameL = new JLabel(receptionistName);
        nameL.setFont(new Font("Segoe UI",Font.BOLD,11)); nameL.setForeground(ReceptionistUIHelper.C_WHITE);
        JLabel roleL = new JLabel("Receptionist");
        roleL.setFont(new Font("Segoe UI",Font.PLAIN,10)); roleL.setForeground(new Color(130,160,210));
        info.add(nameL); info.add(roleL);
        userCard.add(ava,  BorderLayout.WEST);
        userCard.add(info, BorderLayout.CENTER);
        side.add(userCard);
        side.add(Box.createVerticalGlue());
 
        JLabel ver = new JLabel("  Smart Portal v2.1");
        ver.setFont(new Font("Segoe UI",Font.PLAIN,10));
        ver.setForeground(new Color(80,110,170));
        ver.setAlignmentX(LEFT_ALIGNMENT);
        side.add(ver);
        side.add(Box.createVerticalStrut(14));
 
        return side;
    }
 
    private JButton navBtn(String icon, String label, JPanel sidebar) {
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
                boolean active = label.equals(activeSection);
                Color bg = active ? ReceptionistUIHelper.C_SIDEBAR_A
                         : hover  ? ReceptionistUIHelper.C_SIDEBAR_H : null;
                if (bg != null) { g2.setColor(bg); g2.fillRoundRect(8,2,getWidth()-16,getHeight()-4,8,8); }
                if (active) { g2.setColor(new Color(100,180,255)); g2.fillRoundRect(8,6,3,getHeight()-12,3,3); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Segoe UI",Font.PLAIN,13));
        btn.setForeground(label.equals(activeSection) ? ReceptionistUIHelper.C_WHITE : new Color(170,195,235));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false); btn.setContentAreaFilled(false); btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(230,44)); btn.setPreferredSize(new Dimension(230,44));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            activeSection = label;
            pageTitle.setText(label);
            for (Component c : sidebar.getComponents())
                if (c instanceof JButton jb) {
                    boolean active = jb.getText().contains(activeSection);
                    jb.setForeground(active ? ReceptionistUIHelper.C_WHITE : new Color(170,195,235));
                }
            sidebar.repaint();
            navigateTo(label);
        });
        return btn;
    }
 
    /** Repaints the sidebar to update the active highlight after programmatic navigation. */
    private void repaintSidebar() {
        if (sidebarRef == null) return;
        for (Component c : sidebarRef.getComponents())
            if (c instanceof JButton jb) {
                boolean active = jb.getText().contains(activeSection);
                jb.setForeground(active ? ReceptionistUIHelper.C_WHITE : new Color(170,195,235));
            }
        sidebarRef.repaint();
    }
 
    // ════════════════════════════════════════════════════════════════════════
    //  MAIN
    // ════════════════════════════════════════════════════════════════════════
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ReceptionistDashboard("Receptionist"));
    }
}