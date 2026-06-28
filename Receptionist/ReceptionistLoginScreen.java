package Receptionist;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;
import System.HospitalSystem;
import System.ReceptionistRosterStore;

public class ReceptionistLoginScreen extends JFrame {

    public static boolean transitioning = false;

    private JTextField usernameField;
    private JPasswordField passwordField;

    private static final String VALID_PASS = "1234";

    private static Map<String, String> buildReceptionistMap() {
        Map<String, String> map = new LinkedHashMap<>();
        java.util.List<Object[]> roster = ReceptionistRosterStore.getActiveReceptionists();
        if (!roster.isEmpty()) {
            for (Object[] r : roster) {
                String name = (String) r[1];
                map.put(generateUsername(name), name);
            }
            return map;
        }
        // Fallback hardcoded
        String[][] receps = {
            {"sarah_a","Sarah Ahmed"},{"fatima_k","Fatima Khan"},{"ahmed_r","Ahmed Raza"},
            {"zainab_h","Zainab Hassan"}
        };
        for (String[] rec : receps) map.put(rec[0], rec[1]);
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

    public ReceptionistLoginScreen() {
        Map<String, String> userToDisplay = buildReceptionistMap();
        setTitle("Smart Portal — Receptionist Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(false);

        JPanel bg = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(10, 36, 99),
                        getWidth(), getHeight(), new Color(20, 65, 170));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(255, 255, 255, 18));
                g2.fillOval(100, 80, 350, 350);
                g2.setColor(new Color(255, 255, 255, 10));
                g2.fillOval(getWidth() - 400, 150, 400, 400);
            }
        };
        setContentPane(bg);

        JPanel card = new JPanel(new GridBagLayout()) {
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

        JLabel title = new JLabel("RECEPTIONIST LOGIN", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        card.add(title, c);

        JLabel sub = new JLabel("Receptionist Portal Access", SwingConstants.CENTER);
        sub.setForeground(new Color(180, 200, 230));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        c.gridy = 1;
        card.add(sub, c);

        c.gridwidth = 1;

        JLabel u = new JLabel("Username");
        u.setForeground(Color.LIGHT_GRAY);
        u.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.gridy = 2; c.gridx = 0;
        card.add(u, c);

        usernameField = new JTextField();
        styleField(usernameField);
        c.gridx = 1;
        card.add(usernameField, c);

        JLabel p = new JLabel("Password");
        p.setForeground(Color.LIGHT_GRAY);
        p.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.gridy = 3; c.gridx = 0;
        card.add(p, c);

        passwordField = new JPasswordField();
        styleField(passwordField);
        c.gridx = 1;
        card.add(passwordField, c);

        JLabel errorLabel = new JLabel("Invalid username or password.");
        errorLabel.setForeground(new Color(255, 100, 100));
        errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        errorLabel.setVisible(false);
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        card.add(errorLabel, c);

        JButton login = new JButton("LOGIN");
        JButton reset = new JButton("RESET");
        JButton back = new JButton("BACK");
        styleButton(login, new Color(0, 180, 120));
        styleButton(reset, new Color(70, 90, 130));
        styleButton(back, new Color(150, 50, 50));

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        btnPanel.setOpaque(false);
        btnPanel.add(login);
        btnPanel.add(reset);
        btnPanel.add(back);
        c.gridy = 5; c.gridx = 0; c.gridwidth = 2;
        card.add(btnPanel, c);

        String hintUser = userToDisplay.keySet().iterator().next();
        JLabel hint = new JLabel("Hint: " + hintUser + " / 1234", SwingConstants.CENTER);
        hint.setForeground(new Color(150, 190, 230));
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        c.gridy = 6; c.insets = new Insets(6, 18, 12, 18);
        card.add(hint, c);

        login.addActionListener(e -> {
            String user = usernameField.getText().trim().toLowerCase();
            String pass = new String(passwordField.getPassword()).trim();
            if (userToDisplay.containsKey(user) && VALID_PASS.equals(pass)) {
                transitioning = true;
                String displayName = userToDisplay.get(user);
                dispose();
                SwingUtilities.invokeLater(() -> new ReceptionistDashboard(displayName));
            } else {
                errorLabel.setVisible(true);
            }
        });

        reset.addActionListener(e -> {
            usernameField.setText("");
            passwordField.setText("");
            errorLabel.setVisible(false);
        });

        back.addActionListener(e -> {
            transitioning = true;
            dispose();
            if (HospitalSystem.showLauncher != null) {
                HospitalSystem.showLauncher.run();
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (!transitioning && HospitalSystem.showLauncher != null) {
                    HospitalSystem.showLauncher.run();
                }
                transitioning = false;
            }
        });

        GridBagConstraints main = new GridBagConstraints();
        main.gridx = 0; main.gridy = 0;
        bg.add(card, main);

        setVisible(true);
    }

    private void styleField(JTextField f) {
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setForeground(Color.WHITE);
        f.setBackground(new Color(30, 50, 100, 200));
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 130, 200, 150), 1, true),
            new EmptyBorder(8, 12, 8, 12)
        ));
    }

    private void styleButton(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ReceptionistLoginScreen::new);
    }

    public static void launch(String displayName) {
        transitioning = true;
        SwingUtilities.invokeLater(() -> new ReceptionistDashboard(displayName));
    }
}
