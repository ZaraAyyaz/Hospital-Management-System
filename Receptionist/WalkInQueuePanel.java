package Receptionist;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * WalkInQueuePanel
 * Dedicated full-width view of the live walk-in patient queue.
 */
public class WalkInQueuePanel {

    public interface NavigationListener {
        void navigateTo(String section);
    }

    private final NavigationListener nav;

    public WalkInQueuePanel(NavigationListener nav) {
        this.nav = nav;
    }

    public JPanel build() {
        JPanel p = ReceptionistUIHelper.page();

        // Count by priority
        long critical = 0, high = 0, medium = 0, low = 0;
        for (int r = 0; r < ReceptionistDataStore.walkModel.getRowCount(); r++) {
            switch (ReceptionistDataStore.walkModel.getValueAt(r, 5).toString().toLowerCase()) {
                case "critical" -> critical++;
                case "high"     -> high++;
                case "medium"   -> medium++;
                default         -> low++;
            }
        }

        // Stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 14, 0)); stats.setOpaque(false);
        stats.add(ReceptionistUIHelper.queueStatCard("Total Waiting",
            String.valueOf(ReceptionistDataStore.walkModel.getRowCount()),
            "patients in queue", ReceptionistUIHelper.C_BLUE));
        stats.add(ReceptionistUIHelper.queueStatCard("Critical",
            String.valueOf(critical), "immediate attention", ReceptionistUIHelper.C_RED));
        stats.add(ReceptionistUIHelper.queueStatCard("High / Medium",
            (high+medium)+" ", "moderate priority", ReceptionistUIHelper.C_AMBER));
        stats.add(ReceptionistUIHelper.queueStatCard("Low Priority",
            String.valueOf(low), "routine cases", ReceptionistUIHelper.C_GREEN));

        // Toolbar
        JTextField search = ReceptionistUIHelper.styledField("Search patient name or queue number…");
        search.setPreferredSize(new Dimension(320, 38));
        JButton btnAdmitNew = ReceptionistUIHelper.primary("➕  Admit New Walk-in");
        btnAdmitNew.addActionListener(e -> nav.navigateTo("Walk-in Admission"));
        JPanel toolbar = new JPanel(new BorderLayout(12, 0)); toolbar.setOpaque(false);
        toolbar.add(search, BorderLayout.WEST);
        toolbar.add(btnAdmitNew, BorderLayout.EAST);

        // Priority filter chips
        String[]   pvs = {"All","Critical","High","Medium","Low"};
        final String[] af = {"All"};
        JPanel chipsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0)); chipsPanel.setOpaque(false);
        JLabel[] chipArr = new JLabel[pvs.length];
        JTable wt = buildWalkInTable();
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(ReceptionistDataStore.walkModel);
        wt.setRowSorter(sorter);

        for (int i = 0; i < pvs.length; i++) {
            final String pv = pvs[i];
            JLabel chip = ReceptionistUIHelper.makeFilterChip(pv, pv.equals("All"));
            chipArr[i] = chip;
            chip.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    af[0] = pv;
                    for (int j = 0; j < chipArr.length; j++) {
                        chipArr[j].putClientProperty("active", pvs[j].equals(pv));
                        chipArr[j].setForeground(pvs[j].equals(pv)
                            ? ReceptionistUIHelper.C_WHITE : ReceptionistUIHelper.C_MID);
                        chipArr[j].repaint();
                    }
                    applyQueueFilter(sorter, search.getText(), pv);
                }
            });
            chipsPanel.add(chip);
        }
        search.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                applyQueueFilter(sorter, search.getText(), af[0]);
            }
        });

        // Queue table section
        JPanel qSec = ReceptionistUIHelper.sectionCard(
            "📋  Walk-in Queue  (" + ReceptionistDataStore.walkModel.getRowCount() + " patients waiting)");
        qSec.add(ReceptionistUIHelper.tableScroll(wt), BorderLayout.CENTER);

        // Priority colour legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 4)); legend.setOpaque(false);
        legend.add(ReceptionistUIHelper.legendItem(ReceptionistUIHelper.C_RED,   "Critical — Immediate"));
        legend.add(ReceptionistUIHelper.legendItem(ReceptionistUIHelper.C_AMBER, "High — Urgent"));
        legend.add(ReceptionistUIHelper.legendItem(new Color(180, 83, 9),        "Medium — Soon"));
        legend.add(ReceptionistUIHelper.legendItem(ReceptionistUIHelper.C_GREEN, "Low — Routine"));

        ReceptionistUIHelper.stack(p,
            stats, ReceptionistUIHelper.vgap(20),
            toolbar, ReceptionistUIHelper.vgap(10),
            chipsPanel, ReceptionistUIHelper.vgap(4),
            legend, ReceptionistUIHelper.vgap(12),
            qSec);
        return p;
    }

    // ── Walk-in table with custom column renderers ────────────────────────────
    private JTable buildWalkInTable() {
        JTable t = new JTable(ReceptionistDataStore.walkModel);
        t.setFont(ReceptionistUIHelper.F_BODY);
        t.setForeground(ReceptionistUIHelper.C_DARK);
        t.setRowHeight(40);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(ReceptionistUIHelper.C_WHITE);
        t.setSelectionBackground(ReceptionistUIHelper.C_SKY);
        t.setSelectionForeground(ReceptionistUIHelper.C_NAVY);
        t.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JTableHeader h = t.getTableHeader();
        h.setFont(ReceptionistUIHelper.F_BOLD_S);
        h.setBackground(ReceptionistUIHelper.C_BG);
        h.setForeground(ReceptionistUIHelper.C_MID);
        h.setBorder(new MatteBorder(0, 0, 1, 0, ReceptionistUIHelper.C_DIVIDER));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 36));

        int[] colWidths = {70, 160, 110, 70, 200, 100, 160, 90};
        for (int i = 0; i < colWidths.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(colWidths[i]);
            t.getColumnModel().getColumn(i).setMinWidth(colWidths[i] - 20);
        }

        // Default row renderer
        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl,val,sel,foc,r,c);
                if (!sel) comp.setBackground(r%2==0
                    ? ReceptionistUIHelper.C_WHITE : new Color(249,252,255));
                comp.setForeground(ReceptionistUIHelper.C_DARK);
                ((JLabel)comp).setBorder(new EmptyBorder(0,14,0,14));
                return comp;
            }
        });

        // Priority column (col 5)
        t.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = new JLabel("  "+(val==null?"":val)+"  ", SwingConstants.CENTER);
                l.setFont(ReceptionistUIHelper.F_BOLD_S); l.setOpaque(true);
                String v = val==null ? "" : val.toString().toLowerCase();
                if      (v.equals("critical")) { l.setBackground(ReceptionistUIHelper.C_RED_BG);   l.setForeground(ReceptionistUIHelper.C_RED);   }
                else if (v.equals("high"))     { l.setBackground(new Color(255,237,213));           l.setForeground(new Color(194,65,12));          }
                else if (v.equals("medium"))   { l.setBackground(ReceptionistUIHelper.C_AMBER_BG); l.setForeground(ReceptionistUIHelper.C_AMBER);  }
                else                           { l.setBackground(ReceptionistUIHelper.C_GREEN_BG); l.setForeground(ReceptionistUIHelper.C_GREEN);  }
                return l;
            }
        });

        // Queue # column (col 0) — bold mono
        t.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = new JLabel(val!=null?val.toString():"", SwingConstants.CENTER);
                l.setFont(new Font("Consolas", Font.BOLD, 12));
                l.setForeground(ReceptionistUIHelper.C_NAVY); l.setOpaque(true);
                l.setBackground(r%2==0 ? ReceptionistUIHelper.C_WHITE : new Color(249,252,255));
                return l;
            }
        });

        // Wait time column (col 7) — colour by minutes
        t.getColumnModel().getColumn(7).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel l = new JLabel(val!=null?val.toString():"", SwingConstants.CENTER);
                l.setFont(ReceptionistUIHelper.F_BOLD_S); l.setOpaque(true);
                l.setBackground(r%2==0 ? ReceptionistUIHelper.C_WHITE : new Color(249,252,255));
                String wt = val!=null ? val.toString().replaceAll("[^0-9]","") : "0";
                int mins = wt.isEmpty() ? 0 : Integer.parseInt(wt);
                if      (mins <= 10) l.setForeground(ReceptionistUIHelper.C_GREEN);
                else if (mins <= 25) l.setForeground(ReceptionistUIHelper.C_AMBER);
                else                 l.setForeground(ReceptionistUIHelper.C_RED);
                return l;
            }
        });
        return t;
    }

    private void applyQueueFilter(TableRowSorter<DefaultTableModel> sorter,
                                   String text, String priority) {
        List<RowFilter<DefaultTableModel,Object>> filters = new ArrayList<>();
        if (!text.isEmpty())           filters.add(RowFilter.regexFilter("(?i)"+text));
        if (!priority.equals("All"))   filters.add(RowFilter.regexFilter("(?i)^"+priority+"$", 5));
        if (filters.isEmpty())         sorter.setRowFilter(null);
        else if (filters.size()==1)    sorter.setRowFilter(filters.get(0));
        else                           sorter.setRowFilter(RowFilter.andFilter(filters));
    }
}