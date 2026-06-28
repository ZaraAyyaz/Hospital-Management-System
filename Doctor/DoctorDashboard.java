package Doctor;

import javax.swing.*;
import java.awt.*;

public class DoctorDashboard extends JFrame {

    private JPanel contentPanel;
    private CardLayout cardLayout;

    private final Color SIDEBAR_BLUE = new Color(30, 55, 105);
    private final Color TEXT_WHITE   = Color.WHITE;

    private doctorOverviewPanel overviewPanel;
    private String doctorName;

    public DoctorDashboard(String doctorName) {
        this.doctorName = doctorName;
        doctorDataStore.get().seedData();

        setTitle("Doctor Management System - Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // ── SIDE MENU ──────────────────────────────────────────────────────
        JPanel sideMenu = new JPanel();
        sideMenu.setBackground(SIDEBAR_BLUE);
        sideMenu.setPreferredSize(new Dimension(250, 800));
        sideMenu.setLayout(new BoxLayout(sideMenu, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("DR. PORTAL");
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(30, 0, 40, 0));
        sideMenu.add(titleLabel);

        JButton btnOverview      = createMenuButton("Overview");
        JButton btnSchedule      = createMenuButton("My Schedule");
        JButton btnConsultation  = createMenuButton("Consultation");
        JButton btnEmergency     = createMenuButton("Emergency Queue");
        JButton btnPatients      = createMenuButton("Patient History");
        JButton btnLogout        = createMenuButton("Logout");

        sideMenu.add(btnOverview);
        sideMenu.add(btnSchedule);
        sideMenu.add(btnConsultation);
        sideMenu.add(btnEmergency);
        sideMenu.add(btnPatients);
        sideMenu.add(Box.createVerticalGlue());
        sideMenu.add(btnLogout);
        sideMenu.add(Box.createRigidArea(new Dimension(0, 20)));

        add(sideMenu, BorderLayout.WEST);

        // ── CONTENT AREA ───────────────────────────────────────────────────
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        overviewPanel = new doctorOverviewPanel(doctorName);
        contentPanel.add(overviewPanel,           "Overview");
        contentPanel.add(new doctorSchedulePanel(),     "Schedule");
        contentPanel.add(new doctorConsultationPanel(), "Consultation");
        contentPanel.add(new doctorEmergencyPanel(),    "Emergency");
        contentPanel.add(new doctorPatientHistoryPanel(),"Patients");

        add(contentPanel, BorderLayout.CENTER);

        // ── NAVIGATION ─────────────────────────────────────────────────────
        btnOverview.addActionListener(e -> {
            overviewPanel.refresh();
            cardLayout.show(contentPanel, "Overview");
        });
        btnSchedule.addActionListener(e     -> cardLayout.show(contentPanel, "Schedule"));
        btnConsultation.addActionListener(e -> cardLayout.show(contentPanel, "Consultation"));
        btnEmergency.addActionListener(e    -> cardLayout.show(contentPanel, "Emergency"));
        btnPatients.addActionListener(e     -> cardLayout.show(contentPanel, "Patients"));
        btnLogout.addActionListener(e -> dispose());

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                try {
                    Class<?> hs = Class.forName("System.HospitalSystem");
                    java.lang.reflect.Field f = hs.getField("onDoctorLogout");
                    Runnable r = (Runnable) f.get(null);
                    if (r != null) r.run();
                } catch (Exception ignored) {}
            }
        });
    }

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setMaximumSize(new Dimension(230, 52));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setFocusPainted(false);
        btn.setBackground(SIDEBAR_BLUE);
        btn.setForeground(TEXT_WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 10));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(new Color(45, 75, 140));
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(SIDEBAR_BLUE);
            }
        });
        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DoctorDashboard("Doctor").setVisible(true));
    }
}
