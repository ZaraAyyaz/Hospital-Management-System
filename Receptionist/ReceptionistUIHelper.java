package Receptionist;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * ReceptionistUIHelper
 * All shared colours, fonts, and UI factory methods used across every panel.
 * Panels extend or call this class statically so the look is identical everywhere.
 */
public class ReceptionistUIHelper {

    // ── Palette ──────────────────────────────────────────────────────────────
    public static final Color C_NAVY      = new Color(10, 36, 99);
    public static final Color C_BLUE      = new Color(30, 90, 210);
    public static final Color C_BLUE_MID  = new Color(55, 130, 245);
    public static final Color C_SKY       = new Color(225, 240, 255);
    public static final Color C_WHITE     = Color.WHITE;
    public static final Color C_BG        = new Color(245, 248, 252);
    public static final Color C_DARK      = new Color(15, 23, 42);
    public static final Color C_MID       = new Color(71, 85, 105);
    public static final Color C_MUTED     = new Color(148, 163, 184);
    public static final Color C_BORDER    = new Color(210, 228, 252);
    public static final Color C_DIVIDER   = new Color(226, 232, 240);
    public static final Color C_GREEN     = new Color(22, 163, 74);
    public static final Color C_GREEN_BG  = new Color(240, 253, 244);
    public static final Color C_AMBER     = new Color(217, 119, 6);
    public static final Color C_AMBER_BG  = new Color(255, 251, 235);
    public static final Color C_RED       = new Color(185, 28, 28);
    public static final Color C_RED_BG    = new Color(254, 242, 242);
    public static final Color C_INDIGO    = new Color(79, 70, 229);
    public static final Color C_INDIGO_BG = new Color(238, 242, 255);
    public static final Color C_SIDEBAR   = new Color(15, 28, 64);
    public static final Color C_SIDEBAR_H = new Color(30, 55, 120);
    public static final Color C_SIDEBAR_A = new Color(40, 80, 190);
    public static final Color C_BREAK     = new Color(250, 245, 255);
    public static final Color C_BREAK_FG  = new Color(139, 92, 246);
    public static final Color C_LUNCH     = new Color(255, 247, 237);
    public static final Color C_LUNCH_FG  = new Color(217, 119, 6);

    // ── Fonts ────────────────────────────────────────────────────────────────
    public static final Font F_TITLE  = new Font("Segoe UI", Font.BOLD,  20);
    public static final Font F_HEAD   = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font F_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font F_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font F_BOLD_S = new Font("Segoe UI", Font.BOLD,  11);
    public static final Font F_SLOT   = new Font("Segoe UI", Font.PLAIN, 10);

    // ── Border factories ─────────────────────────────────────────────────────
    public static Border fieldBorderOk()  { return new CompoundBorder(new LineBorder(C_GREEN,    2, true), new EmptyBorder(7,9,7,9)); }
    public static Border fieldBorderErr() { return new CompoundBorder(new LineBorder(C_RED,      2, true), new EmptyBorder(7,9,7,9)); }
    public static Border fieldBorderFoc() { return new CompoundBorder(new LineBorder(C_BLUE_MID, 2, true), new EmptyBorder(7,9,7,9)); }
    public static Border fieldBorderDef() { return new CompoundBorder(new LineBorder(C_BORDER,   1, true), new EmptyBorder(8,10,8,10)); }

    // ── Label helper ─────────────────────────────────────────────────────────
    public static JLabel lbl(String t, Font f, Color c) {
        JLabel l = new JLabel(t); l.setFont(f); l.setForeground(c); return l;
    }

    // ── Field group (label + component) ──────────────────────────────────────
    public static JPanel fg(String label, Component field) {
        JPanel g = new JPanel(new BorderLayout(0, 5)); g.setOpaque(false);
        g.add(lbl(label, F_SMALL, C_MID), BorderLayout.NORTH);
        g.add(field, BorderLayout.CENTER);
        return g;
    }

    // ── Styled text field with placeholder ───────────────────────────────────
    public static JTextField styledField(String placeholder) {
        JTextField f = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(C_MUTED); g2.setFont(F_BODY.deriveFont(Font.ITALIC));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, 10, (getHeight()+fm.getAscent()-fm.getDescent())/2);
                }
            }
        };
        f.setFont(F_BODY); f.setForeground(C_DARK); f.setBackground(C_WHITE);
        f.setBorder(fieldBorderDef()); f.setPreferredSize(new Dimension(0, 38));
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { f.setBorder(fieldBorderFoc()); }
            @Override public void focusLost(FocusEvent e) {
                Border b = f.getBorder();
                if (b instanceof CompoundBorder cb) {
                    Border out = cb.getOutsideBorder();
                    if (out instanceof LineBorder lb &&
                        (lb.getLineColor().equals(C_RED) || lb.getLineColor().equals(C_GREEN))) return;
                }
                f.setBorder(fieldBorderDef());
            }
        });
        return f;
    }

    // ── Smart date field ─────────────────────────────────────────────────────
    public static JTextField dateField(String ph, Runnable onCommit) {
        JTextField f = styledField(ph);
        Runnable commit = () -> {
            String raw = f.getText().trim();
            if (raw.isEmpty()) return;
            String norm = ReceptionistDataStore.normaliseDate(raw);
            if (!norm.isEmpty()) {
                f.setText(norm);
                f.setBorder(fieldBorderOk());
                if (onCommit != null) onCommit.run();
            } else {
                f.setBorder(fieldBorderErr());
                f.setToolTipText("Use DD/MM/YYYY or DD MMM YYYY");
            }
        };
        f.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e)   { commit.run(); }
            @Override public void focusGained(FocusEvent e) { f.setToolTipText("e.g. 21/05/2026 or 21 May 2026"); }
        });
        f.addActionListener(e -> { commit.run(); f.transferFocus(); });
        return f;
    }

    public static JTextField dateField(String ph) { return dateField(ph, null); }

    public static boolean validateDateField(JTextField f, Component parent) {
        String raw = f.getText().trim();
        if (raw.isEmpty()) {
            showError(parent, "Date is required.\nUse DD/MM/YYYY or DD MMM YYYY");
            f.setBorder(fieldBorderErr()); return false;
        }
        if (!ReceptionistDataStore.isValidDate(raw)) {
            showError(parent, "\""+raw+"\" is not a valid date.\nUse DD/MM/YYYY or DD MMM YYYY\ne.g. 21/05/2026 or 21 May 2026");
            f.setBorder(fieldBorderErr()); return false;
        }
        return true;
    }

    // ── Combo box ─────────────────────────────────────────────────────────────
    public static <T> JComboBox<T> combo(T[] items) {
        JComboBox<T> cb = new JComboBox<>(items);
        cb.setFont(F_BODY); cb.setForeground(C_DARK); cb.setBackground(C_WHITE);
        cb.setPreferredSize(new Dimension(0, 38)); return cb;
    }

    // ── Primary (blue filled) button ──────────────────────────────────────────
    public static JButton primary(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if      (getModel().isPressed())  g2.setColor(C_NAVY);
                else if (getModel().isRollover()) g2.setColor(C_BLUE_MID);
                else                              g2.setColor(C_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(F_BOLD_S); btn.setForeground(C_WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return btn;
    }

    // ── Ghost (outlined) button ───────────────────────────────────────────────
    public static JButton ghost(String text) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) { g2.setColor(C_SKY); g2.fillRoundRect(0,0,getWidth(),getHeight(),8,8); }
                g2.setColor(C_BLUE); g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(F_BOLD_S); btn.setForeground(C_BLUE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(9, 16, 9, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); return btn;
    }

    // ── Error dialog ─────────────────────────────────────────────────────────
    public static void showError(Component p, String msg) {
        JOptionPane.showMessageDialog(p, msg, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }

    // ── Page container ────────────────────────────────────────────────────────
    public static JPanel page() {
        JPanel p = new JPanel(); p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_BG); p.setBorder(new EmptyBorder(26, 30, 34, 30)); return p;
    }

    public static void stack(JPanel page, Component... comps) {
        for (Component c : comps) {
            if (c instanceof JPanel jp) jp.setAlignmentX(Component.LEFT_ALIGNMENT);
            page.add(c);
        }
    }

    public static Component vgap(int h) { return Box.createVerticalStrut(h); }

    // ── Form card ─────────────────────────────────────────────────────────────
    public static JPanel formCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 16));
        card.setBackground(C_WHITE);
        card.setBorder(new CompoundBorder(new PanelShadowBorder(), new EmptyBorder(22, 24, 24, 24)));
        JLabel ttl = new JLabel(title); ttl.setFont(F_HEAD); ttl.setForeground(C_NAVY);
        JPanel sep = new JPanel(); sep.setBackground(C_DIVIDER); sep.setPreferredSize(new Dimension(0, 1));
        JPanel hdr = new JPanel(new BorderLayout(0, 12)); hdr.setOpaque(false);
        hdr.add(ttl, BorderLayout.NORTH); hdr.add(sep, BorderLayout.SOUTH);
        card.add(hdr, BorderLayout.NORTH);
        return card;
    }

    // ── Section card ──────────────────────────────────────────────────────────
    public static JPanel sectionCard(String title) {
        JPanel card = new JPanel(new BorderLayout(0, 14));
        card.setBackground(C_WHITE);
        card.setBorder(new CompoundBorder(new PanelShadowBorder(), new EmptyBorder(20, 20, 20, 20)));
        JLabel ttl = new JLabel(title); ttl.setFont(F_HEAD); ttl.setForeground(C_NAVY);
        card.add(ttl, BorderLayout.NORTH);
        return card;
    }

    // ── Gradient header (for dialogs) ─────────────────────────────────────────
    public static JPanel gradHeader(String title) {
        JPanel h = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, C_NAVY, getWidth(), 0, C_BLUE));
                g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        h.setPreferredSize(new Dimension(0, 48)); h.setBorder(new EmptyBorder(0, 20, 0, 20));
        JLabel t = new JLabel(title); t.setFont(F_HEAD); t.setForeground(C_WHITE);
        h.add(t, BorderLayout.CENTER); return h;
    }

    // ── Stat card ─────────────────────────────────────────────────────────────
    public static JPanel statCard(String icon, String title, String value, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);  g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(new PanelShadowBorder(), new EmptyBorder(16, 20, 16, 16)));
        JLabel ico = new JLabel(icon); ico.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 24));
        JLabel val = new JLabel(value); val.setFont(new Font("Segoe UI", Font.BOLD, 28)); val.setForeground(C_DARK);
        JLabel ttl = new JLabel(title); ttl.setFont(F_BOLD_S); ttl.setForeground(C_MID);
        JLabel s   = new JLabel(sub);   s.setFont(F_SMALL);    s.setForeground(C_MUTED);
        JPanel top = new JPanel(new BorderLayout()); top.setOpaque(false);
        top.add(ico, BorderLayout.WEST); top.add(val, BorderLayout.EAST);
        JPanel bot = new JPanel(new GridLayout(2,1,0,2)); bot.setOpaque(false);
        bot.add(ttl); bot.add(s);
        card.add(top, BorderLayout.NORTH); card.add(bot, BorderLayout.CENTER);
        return card;
    }

    // ── Queue stat card ───────────────────────────────────────────────────────
    public static JPanel queueStatCard(String title, String value, String sub, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE); g2.fillRoundRect(0,0,getWidth(),getHeight(),12,12);
                g2.setColor(accent);  g2.fillRoundRect(0,0,4,getHeight(),4,4);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new CompoundBorder(new PanelShadowBorder(), new EmptyBorder(14,18,14,16)));
        JLabel val = new JLabel(value); val.setFont(new Font("Segoe UI",Font.BOLD,26)); val.setForeground(C_DARK);
        JLabel ttl = new JLabel(title); ttl.setFont(F_BOLD_S); ttl.setForeground(C_MID);
        JLabel s   = new JLabel(sub);   s.setFont(F_SMALL);    s.setForeground(C_MUTED);
        JPanel bot = new JPanel(new GridLayout(2,1,0,2)); bot.setOpaque(false); bot.add(ttl); bot.add(s);
        card.add(val, BorderLayout.NORTH); card.add(bot, BorderLayout.CENTER);
        return card;
    }

    // ── Filter chip ───────────────────────────────────────────────────────────
    public static JLabel makeFilterChip(String text, boolean startActive) {
        JLabel c = new JLabel("  " + text + "  ") {
            @Override protected void paintComponent(Graphics g) {
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(active ? C_BLUE : C_WHITE);
                g2.fillRoundRect(0,0,getWidth(),getHeight(),16,16);
                if (!active) { g2.setColor(C_BORDER); g2.drawRoundRect(0,0,getWidth()-1,getHeight()-1,16,16); }
                g2.dispose(); super.paintComponent(g);
            }
        };
        c.putClientProperty("active", startActive);
        c.setFont(F_BOLD_S); c.setForeground(startActive ? C_WHITE : C_MID);
        c.setBorder(new EmptyBorder(6,4,6,4));
        c.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return c;
    }

    // ── Legend item ───────────────────────────────────────────────────────────
    public static JLabel legendItem(Color c, String text) {
        JLabel l = new JLabel("■ " + text);
        l.setFont(F_SMALL); l.setForeground(c); return l;
    }

    // ── Table scroll pane ─────────────────────────────────────────────────────
    public static JScrollPane tableScroll(JTable t) {
        JScrollPane sp = new JScrollPane(t);
        sp.setBorder(new LineBorder(C_DIVIDER, 1));
        sp.setBackground(C_WHITE); sp.getViewport().setBackground(C_WHITE); return sp;
    }

    // ── Generic styled table ──────────────────────────────────────────────────
    public static JTable buildTable(DefaultTableModel model, int statusCol) {
        JTable t = new JTable(model);
        t.setFont(F_BODY); t.setForeground(C_DARK); t.setRowHeight(38);
        t.setShowGrid(false); t.setIntercellSpacing(new Dimension(0,0));
        t.setBackground(C_WHITE); t.setSelectionBackground(C_SKY); t.setSelectionForeground(C_NAVY);

        JTableHeader h = t.getTableHeader();
        h.setFont(F_BOLD_S); h.setBackground(C_BG); h.setForeground(C_MID);
        h.setBorder(new MatteBorder(0,0,1,0,C_DIVIDER)); h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0,34));

        t.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl,val,sel,foc,r,c);
                if (!sel) comp.setBackground(r%2==0 ? C_WHITE : new Color(249,252,255));
                comp.setForeground(C_DARK);
                ((JLabel)comp).setBorder(new EmptyBorder(0,12,0,12));
                return comp;
            }
        });

        if (statusCol >= 0) t.getColumnModel().getColumn(statusCol).setCellRenderer(
            new DefaultTableCellRenderer() {
                @Override public Component getTableCellRendererComponent(JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                    JLabel l = new JLabel("  "+val+"  ", SwingConstants.CENTER);
                    l.setFont(F_BOLD_S); l.setOpaque(true);
                    String v = val==null ? "" : val.toString().toLowerCase();
                    if      (v.contains("confirm"))  { l.setBackground(C_GREEN_BG);  l.setForeground(new Color(21,128,61));  }
                    else if (v.contains("pending"))  { l.setBackground(C_AMBER_BG);  l.setForeground(new Color(180,83,9));   }
                    else if (v.contains("cancel"))   { l.setBackground(C_RED_BG);    l.setForeground(new Color(153,27,27));  }
                    else if (v.contains("walk"))     { l.setBackground(C_INDIGO_BG); l.setForeground(C_INDIGO);             }
                    else if (v.contains("register")) { l.setBackground(C_SKY);       l.setForeground(C_NAVY);               }
                    else if (v.equals("low"))        { l.setBackground(C_GREEN_BG);  l.setForeground(new Color(21,128,61));  }
                    else if (v.equals("medium"))     { l.setBackground(C_AMBER_BG);  l.setForeground(new Color(180,83,9));   }
                    else if (v.equals("high"))       { l.setBackground(new Color(255,237,213)); l.setForeground(new Color(194,65,12)); }
                    else if (v.equals("critical"))   { l.setBackground(C_RED_BG);    l.setForeground(C_RED);                }
                    else                             { l.setBackground(C_WHITE);      l.setForeground(C_DARK);               }
                    return l;
                }
            });
        return t;
    }

    // ── Shadow border for cards ───────────────────────────────────────────────
    public static class PanelShadowBorder extends AbstractBorder {
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            for (int i = 4; i >= 1; i--) {
                g2.setColor(new Color(30,80,180,6));
                g2.fillRoundRect(x+i,y+i,w-i*2,h-i*2,14,14);
            }
            g2.setColor(new Color(210,228,252)); g2.drawRoundRect(x,y,w-1,h-1,12,12);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(4,4,6,6); }
    }

    // ── Table button helpers ──────────────────────────────────────────────────
    public interface RowAction { void act(int row); }

    public static class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String l) {
            setFont(new Font("Segoe UI", Font.BOLD, 11));
            setForeground(new Color(30,90,210));
            setContentAreaFilled(false);
            setBorder(new LineBorder(new Color(30,90,210),1,true));
            setText(l);
        }
        @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) { return this; }
    }

    public static class ButtonEditor extends DefaultCellEditor {
        private final JButton btn;
        private int viewRow;
        private JTable currentTable;
        private final RowAction act;

        public ButtonEditor(JCheckBox cb, String label, RowAction act) {
            super(cb); this.act = act;
            btn = new JButton(label);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(new Color(30,90,210));
            btn.setContentAreaFilled(false);
            btn.setBorder(new LineBorder(new Color(30,90,210),1,true));
            btn.addActionListener(e -> {
                fireEditingStopped();
                int modelRow = (currentTable != null && currentTable.getRowSorter() != null)
                    ? currentTable.convertRowIndexToModel(viewRow) : viewRow;
                act.act(modelRow);
            });
        }
        @Override public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            currentTable = t; viewRow = r; return btn;
        }
        @Override public Object getCellEditorValue() { return ""; }
    }
}