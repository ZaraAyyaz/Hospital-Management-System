package Doctor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class doctorEmergencyPanel extends JPanel {

    private JTable emergencyTable;

    // Live summary labels — updated whenever DataStore changes
    private JLabel p1Val;
    private JLabel p2Val;
    private JLabel p3Val;
    private JLabel totalVal;

    private final Color HEADER_RED = new Color(178, 34, 34);
    private final Color TEXT_WHITE = Color.WHITE;
    private final Color CONTENT_BG = new Color(245, 247, 250);

    public doctorEmergencyPanel() {
        setLayout(new BorderLayout());
        setBackground(CONTENT_BG);

        JPanel northWrapper = new JPanel(new BorderLayout());
        northWrapper.setOpaque(false);
        northWrapper.add(createHeaderPanel(),  BorderLayout.NORTH);
        northWrapper.add(createSummaryPanel(), BorderLayout.CENTER);
        add(northWrapper,        BorderLayout.NORTH);
        add(createTablePanel(),  BorderLayout.CENTER);
        add(createActionPanel(), BorderLayout.SOUTH);

        // Register listener — summary cards and table both update automatically
        doctorDataStore.get().addListener(this::refreshSummary);
    }

    private JPanel createHeaderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_RED);
        panel.setPreferredSize(new Dimension(0, 80));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("EMERGENCY QUEUE MANAGEMENT");
        titleLabel.setForeground(TEXT_WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));
        panel.add(titleLabel, BorderLayout.WEST);

        JLabel timeLabel = new JLabel(new SimpleDateFormat("HH:mm:ss | dd MMM yyyy").format(new Date()));
        timeLabel.setForeground(TEXT_WHITE);
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        panel.add(timeLabel, BorderLayout.EAST);
        return panel;
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        p1Val    = makeBigLabel("0", new Color(178,  34,  34));
        p2Val    = makeBigLabel("0", new Color(184, 134,  11));
        p3Val    = makeBigLabel("0", new Color( 34, 139,  34));
        totalVal = makeBigLabel("0", new Color( 25,  75, 150));

        panel.add(createSummaryCard("P1 (Critical)",  p1Val,    new Color(255, 220, 220)));
        panel.add(createSummaryCard("P2 (High)",      p2Val,    new Color(255, 250, 205)));
        panel.add(createSummaryCard("P3 (Medium)",    p3Val,    new Color(220, 240, 220)));
        panel.add(createSummaryCard("Total Active",   totalVal, new Color(220, 235, 255)));

        refreshSummary(); // initial values
        return panel;
    }

    private void refreshSummary() {
        DefaultTableModel em = doctorDataStore.get().getEmergencyModel();
        int p1 = 0, p2 = 0, p3 = 0;
        for (int i = 0; i < em.getRowCount(); i++) {
            String pri = em.getValueAt(i, 0).toString();
            if      ("P1".equals(pri)) p1++;
            else if ("P2".equals(pri)) p2++;
            else                        p3++;
        }
        p1Val.setText(String.valueOf(p1));
        p2Val.setText(String.valueOf(p2));
        p3Val.setText(String.valueOf(p3));
        totalVal.setText(String.valueOf(p1 + p2 + p3));
    }

    private JLabel makeBigLabel(String text, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 32));
        l.setForeground(color);
        return l;
    }

    private JPanel createSummaryCard(String label, JLabel valueLabel, Color bgColor) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1));
        card.setPreferredSize(new Dimension(200, 100));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.insets = new Insets(10, 15, 10, 15);
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(60, 60, 60));
        gbc.gridy = 0; card.add(lbl, gbc);
        gbc.gridy = 1; card.add(valueLabel, gbc);
        return card;
    }

    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CONTENT_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        emergencyTable = new JTable(doctorDataStore.get().getEmergencyModel()) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        emergencyTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        emergencyTable.setRowHeight(40);
        emergencyTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        emergencyTable.getTableHeader().setBackground(new Color(30, 55, 105));
        emergencyTable.getTableHeader().setForeground(Color.WHITE);
        emergencyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        emergencyTable.setIntercellSpacing(new Dimension(0, 1));
        emergencyTable.getColumnModel().getColumn(0).setCellRenderer(new PriorityRenderer());
        emergencyTable.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        emergencyTable.setDefaultRenderer(Object.class, new RowColorRenderer());

        JScrollPane scrollPane = new JScrollPane(emergencyTable);
        scrollPane.getViewport().setBackground(CONTENT_BG);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 20));
        panel.setBackground(CONTENT_BG);
        JButton addBtn = new JButton("+ Add Emergency Patient");
        addBtn.setBackground(HEADER_RED);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        addBtn.addActionListener(e -> openAddEmergencyDialog());
        panel.add(addBtn);
        return panel;
    }

    private void openAddEmergencyDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                "Add Emergency Patient", true);
        dialog.setSize(420, 560);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(Color.WHITE);
        content.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel head = new JLabel("New Emergency Entry");
        head.setFont(new Font("SansSerif", Font.BOLD, 20));
        head.setAlignmentX(Component.LEFT_ALIGNMENT);

        JTextField nameF      = field();
        JTextField ageF       = field();
        JTextField genderF    = field();
        JTextField contactF   = field();
        JTextField bloodF     = field();
        JTextField conditionF = field();
        JTextField wardF      = field();

        String[] priorities = {"P1", "P2", "P3"};
        JComboBox<String> priorityCB = new JComboBox<>(priorities);
        priorityCB.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        priorityCB.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton saveBtn = new JButton("Add to Emergency Queue");
        saveBtn.setBackground(HEADER_RED);
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFocusPainted(false);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        saveBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        saveBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        saveBtn.addActionListener(e -> {
            String name      = nameF.getText().trim();
            String age       = ageF.getText().trim();
            String gender    = genderF.getText().trim();
            String contact   = contactF.getText().trim();
            String blood     = bloodF.getText().trim();
            String condition = conditionF.getText().trim();
            String ward      = wardF.getText().trim();
            String priority  = (String) priorityCB.getSelectedItem();
            String time      = new SimpleDateFormat("HH:mm").format(new Date());
            String status    = "P1".equals(priority) ? "CRITICAL"
                             : "P2".equals(priority) ? "HIGH PRIORITY" : "MODERATE";

            if (name.isEmpty() || condition.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Name and Condition are required.");
                return;
            }

            doctorDataStore ds = doctorDataStore.get();

            // 1. Emergency queue (triggers notifyListeners -> summary refresh + overview refresh)
            ds.addEmergencyPatient(new Object[]{priority, name, age, condition, time, ward, status});

            // 2. Patient History — Emergency tab
            ds.getHistoryEmergencyModel().addRow(
                    new Object[]{name, age, condition, priority, time, "Pending"});

            // 3. Overall Patients (dedup — also updates Total Patients count on Overview)
            ds.ensureInOverall(name,
                    age.isEmpty()     ? "--" : age,
                    gender.isEmpty()  ? "--" : gender,
                    contact.isEmpty() ? "--" : contact,
                    blood.isEmpty()   ? "--" : blood);

            // One final notify to push all counts to Overview
            ds.notifyListeners();
            dialog.dispose();
        });

        content.add(head);
        content.add(Box.createVerticalStrut(20));
        addGroup(content, "PATIENT NAME",  nameF);
        addGroup(content, "AGE",           ageF);
        addGroup(content, "GENDER",        genderF);
        addGroup(content, "CONTACT",       contactF);
        addGroup(content, "BLOOD GROUP",   bloodF);
        addGroup(content, "CONDITION",     conditionF);
        addGroup(content, "WARD / BED",    wardF);

        JLabel priLabel = new JLabel("PRIORITY");
        priLabel.setFont(new Font("SansSerif", Font.BOLD, 11));
        priLabel.setForeground(Color.GRAY);
        priLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(priLabel);
        content.add(Box.createVerticalStrut(5));
        content.add(priorityCB);
        content.add(Box.createVerticalStrut(15));
        content.add(saveBtn);

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        dialog.add(scroll);
        dialog.setVisible(true);
    }

    private JTextField field() {
        JTextField f = new JTextField();
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        f.setAlignmentX(Component.LEFT_ALIGNMENT);
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)));
        return f;
    }

    private void addGroup(JPanel p, String label, JTextField f) {
        JLabel l = new JLabel(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        l.setForeground(Color.GRAY);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(f);
        p.add(Box.createVerticalStrut(10));
    }

    private class PriorityRenderer extends JLabel implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            setText(String.valueOf(v)); setOpaque(true); setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            if      ("P1".equals(v)) { setBackground(new Color(255,220,220)); setForeground(new Color(102,0,0)); }
            else if ("P2".equals(v)) { setBackground(new Color(255,250,205)); setForeground(new Color(153,102,0)); }
            else                     { setBackground(new Color(220,240,220)); setForeground(new Color(0,102,0)); }
            return this;
        }
    }

    private class StatusRenderer extends JLabel implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            setText(String.valueOf(v)); setOpaque(true); setHorizontalAlignment(CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            if      ("CRITICAL".equals(v))      { setBackground(new Color(255,220,220)); setForeground(new Color(102,0,0)); }
            else if ("HIGH PRIORITY".equals(v)) { setBackground(new Color(255,250,205)); setForeground(new Color(153,102,0)); }
            else                                { setBackground(new Color(220,240,220)); setForeground(new Color(0,102,0)); }
            return this;
        }
    }

    private class RowColorRenderer extends JLabel implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object v,
                boolean sel, boolean foc, int row, int col) {
            setText(v != null ? v.toString() : ""); setOpaque(true);
            setFont(new Font("Segoe UI", Font.PLAIN, 12)); setHorizontalAlignment(CENTER);
            String pri = t.getValueAt(row, 0).toString();
            if      ("P1".equals(pri)) { setBackground(new Color(255,220,220)); setForeground(new Color(102,0,0)); }
            else if ("P2".equals(pri)) { setBackground(new Color(255,250,205)); setForeground(new Color(153,102,0)); }
            else                       { setBackground(new Color(220,240,220)); setForeground(new Color(0,102,0)); }
            return this;
        }
    }
}

















