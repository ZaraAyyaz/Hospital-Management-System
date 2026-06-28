package Doctor;

import javax.swing.*;
import java.awt.*;
import System.DoctorRosterStore;

public class doctorSmartHealthcareLogin extends JFrame {

    public static boolean transitioning = false;

    private JTextField usernameField;
    private JPasswordField passwordField;

    // Password is always "1234"; username is firstname_lastinitial (lowercase)
    private static final String VALID_PASS = "1234";

    // Build doctor map: prefer live DoctorRosterStore, fall back to hardcoded list
    private static java.util.Map<String, String> buildDoctorMap() {
        java.util.Map<String, String> map = new java.util.LinkedHashMap<>();
        java.util.List<Object[]> roster = DoctorRosterStore.getActiveDoctors();
        if (!roster.isEmpty()) {
            for (Object[] doc : roster) {
                String name = (String) doc[1];
                map.put(generateUsername(name), name);
            }
            return map;
        }
        // Fallback hardcoded
        String[][] docs = {
            {"kamran_k","Kamran Khan"},{"faisal_q","Faisal Qureshi"},{"anwar_l","Anwar Latif"},
            {"bilal_j","Bilal Javed"},{"asif_m","Asif Malik"},{"omer_s","Omer Shehzad"},
            {"nabeel_s","Nabeel Shiraz"},{"saad_g","Saad Ghafoor"},{"haris_b","Haris Bilal"},
            {"zubair_n","Zubair Niazi"},{"waqas_r","Waqas Raza"},{"yasir_a","Yasir Arafat"},
            {"zainab_r","Zainab Raza"},{"maryam_n","Maryam Nawaz"},{"amina_b","Amina Butt"},
            {"sana_y","Sana Yousaf"},{"sarmad_a","Sarmad Ali"},{"hamza_t","Hamza Tariq"},
            {"taimoor_h","Taimoor Hassan"},{"zeeshan_k","Zeeshan Khan"}
        };
        for (String[] d : docs) map.put(d[0], d[1]);
        return map;
    }

    private static String generateUsername(String fullName) {
        String clean = fullName.replaceFirst("^(Dr\\.?\\s*)+", "").trim();
        String[] parts = clean.toLowerCase().split(" ");
        if (parts.length >= 2) {
            return parts[0] + "_" + parts[1].charAt(0);
        }
        return parts[0];
    }

    public doctorSmartHealthcareLogin() {
        java.util.Map<String, String> userToDisplay = buildDoctorMap();
        setTitle("Smart Healthcare System - Login Portal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);

        setContentPane(new BackgroundPanel());
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // ── LOGIN CARD ───────────────────────────────────────────────────
        JPanel card = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setPreferredSize(new Dimension(420, 420));
        card.setOpaque(false);
        card.setBackground(new Color(18, 30, 60, 230));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 18, 12, 18);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SMART HEALTHCARE LOGIN");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        card.add(title, c);

        JLabel sub = new JLabel("Secure Medical Access Portal");
        sub.setForeground(new Color(180, 200, 230));
        c.gridy = 1;
        card.add(sub, c);

        c.gridwidth = 1;

        JLabel u = new JLabel("Username");
        u.setForeground(Color.LIGHT_GRAY);
        c.gridy = 2; c.gridx = 0;
        card.add(u, c);

        usernameField = new JTextField();
        styleField(usernameField);
        c.gridx = 1;
        card.add(usernameField, c);

        JLabel p = new JLabel("Password");
        p.setForeground(Color.LIGHT_GRAY);
        c.gridy = 3; c.gridx = 0;
        card.add(p, c);

        passwordField = new JPasswordField();
        styleField(passwordField);
        c.gridx = 1;
        card.add(passwordField, c);

        // ── Error label (hidden until login fails) ────────────────────────
        JLabel errorLabel = new JLabel("Invalid username or password.");
        errorLabel.setForeground(new Color(255, 100, 100));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setVisible(false);
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        card.add(errorLabel, c);

        // ── Buttons ───────────────────────────────────────────────────────
        JButton login = new JButton("LOGIN");
        JButton reset = new JButton("RESET");
        styleButton(login, new Color(0, 140, 255));
        styleButton(reset, new Color(70, 90, 130));

        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(login);
        btnPanel.add(reset);

        c.gridy = 5; c.gridx = 0; c.gridwidth = 2;
        card.add(btnPanel, c);

        // ── Actions ───────────────────────────────────────────────────────
        java.util.Map<String, String> userMap = userToDisplay;
        login.addActionListener(e -> attemptLogin(errorLabel, userMap));

        // Allow Enter key from password field to trigger login
        passwordField.addActionListener(e -> attemptLogin(errorLabel, userMap));

        reset.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            errorLabel.setVisible(false);
        });

        GridBagConstraints main = new GridBagConstraints();
        main.gridx = 0; main.gridy = 0;
        add(card, main);

        setVisible(true);
    }

    private void attemptLogin(JLabel errorLabel, java.util.Map<String, String> userToDisplay) {
        String user = usernameField.getText().trim();
        String pass = new String(passwordField.getPassword());

        if (!user.isEmpty() && VALID_PASS.equals(pass) && userToDisplay.containsKey(user)) {
            String displayName = userToDisplay.get(user);
            transitioning = true;
            dispose(); // close login window
            doctorDataStore.get().setDoctorDisplayName(displayName);
            SwingUtilities.invokeLater(() -> new DoctorDashboard(displayName).setVisible(true));
        } else {
            errorLabel.setVisible(true);
            passwordField.setText("");
        }
    }

    // ── Background panel ──────────────────────────────────────────────────
    class BackgroundPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, new Color(8, 12, 25),
                    getWidth(), getHeight(), new Color(10, 25, 60));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.setColor(new Color(0, 160, 255, 25));
            g2.fillOval(150, 120, 400, 400);
            g2.setColor(new Color(0, 200, 255, 15));
            g2.fillOval(getWidth() - 500, 200, 450, 450);
            g2.setColor(new Color(0, 180, 255, 12));
            g2.fillOval(getWidth() / 2 - 300, getHeight() - 400, 600, 600);
        }
    }

    private void styleField(JTextField f) {
        f.setBackground(new Color(25, 40, 80));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
    }

    private void styleButton(JButton b, Color c) {
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(doctorSmartHealthcareLogin::new);
    }
}

















