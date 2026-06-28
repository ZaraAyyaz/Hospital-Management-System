package Receptionist;

import System.HospitalSystem;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class AppointmentsPanel {

    private final JFrame owner;

    public AppointmentsPanel(JFrame owner) {
        this.owner = owner;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MAIN BUILD
    // ─────────────────────────────────────────────────────────────────────────
    public JPanel build() {
        JPanel page = ReceptionistUIHelper.page();

        JTextField search = ReceptionistUIHelper.styledField("Search patient name, ID or doctor…");
        search.setPreferredSize(new Dimension(320, 38));

        JTable table = makeTable(owner);
        TableRowSorter<DefaultTableModel> sorter =
                new TableRowSorter<>(ReceptionistDataStore.apptModel);
        table.setRowSorter(sorter);

        // Filter chips
        String[] fvs = {"All","Low","Medium","High","Critical"};
        final String[] activeFilter = {"All"};
        JPanel tabs = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tabs.setOpaque(false);
        JLabel[] chips = new JLabel[fvs.length];
        for (int i = 0; i < fvs.length; i++) {
            final String fv = fvs[i];
            JLabel chip = ReceptionistUIHelper.makeFilterChip(fv, fv.equals("All"));
            chips[i] = chip;
            chip.addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    activeFilter[0] = fv;
                    for (int j = 0; j < chips.length; j++) {
                        chips[j].putClientProperty("active", fvs[j].equals(fv));
                        chips[j].setForeground(fvs[j].equals(fv)
                            ? ReceptionistUIHelper.C_WHITE : ReceptionistUIHelper.C_MID);
                        chips[j].repaint();
                    }
                    applyFilter(sorter, search.getText(), fv);
                }
            });
            tabs.add(chip);
        }
        search.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                applyFilter(sorter, search.getText(), activeFilter[0]);
            }
        });

        JPanel topRow = new JPanel(new BorderLayout(14, 0));
        topRow.setOpaque(false);
        topRow.add(search, BorderLayout.WEST);

        JPanel sec = new JPanel(new BorderLayout(0, 14));
        sec.setBackground(ReceptionistUIHelper.C_WHITE);
        sec.setBorder(new CompoundBorder(
            new ReceptionistUIHelper.PanelShadowBorder(),
            new EmptyBorder(20, 20, 20, 20)));

        JLabel secTitle = new JLabel("📅  All Appointments  (" +
            ReceptionistDataStore.apptModel.getRowCount() + " total)");
        secTitle.setFont(ReceptionistUIHelper.F_HEAD);
        secTitle.setForeground(ReceptionistUIHelper.C_NAVY);
        sec.add(secTitle, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(new LineBorder(ReceptionistUIHelper.C_DIVIDER, 1));
        sp.setBackground(ReceptionistUIHelper.C_WHITE);
        sp.getViewport().setBackground(ReceptionistUIHelper.C_WHITE);
        sp.setPreferredSize(new Dimension(900,
            Math.max(200, ReceptionistDataStore.apptModel.getRowCount() * 38 + 40)));
        sec.add(sp, BorderLayout.CENTER);

        ReceptionistUIHelper.stack(page, topRow,
            ReceptionistUIHelper.vgap(10), tabs,
            ReceptionistUIHelper.vgap(14), sec);
        return page;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  SHARED TABLE FACTORY
    // ─────────────────────────────────────────────────────────────────────────
    public static JTable makeTable(JFrame dialogOwner) {

        JTable t = new JTable(ReceptionistDataStore.apptModel);
        t.setFont(ReceptionistUIHelper.F_BODY);
        t.setForeground(ReceptionistUIHelper.C_DARK);
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setBackground(ReceptionistUIHelper.C_WHITE);
        t.setSelectionBackground(ReceptionistUIHelper.C_SKY);
        t.setSelectionForeground(ReceptionistUIHelper.C_NAVY);
        t.setFillsViewportHeight(true);

        JTableHeader h = t.getTableHeader();
        h.setFont(ReceptionistUIHelper.F_BOLD_S);
        h.setBackground(ReceptionistUIHelper.C_BG);
        h.setForeground(ReceptionistUIHelper.C_MID);
        h.setBorder(new MatteBorder(0, 0, 1, 0, ReceptionistUIHelper.C_DIVIDER));
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(0, 34));

        DefaultTableCellRenderer textR = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, val, sel, foc, r, c);
                if (!sel) comp.setBackground(
                    r % 2 == 0 ? ReceptionistUIHelper.C_WHITE : new Color(249, 252, 255));
                comp.setForeground(ReceptionistUIHelper.C_DARK);
                ((JLabel) comp).setBorder(new EmptyBorder(0, 12, 0, 12));
                return comp;
            }
        };
        for (int c = 0; c <= 5; c++)
            t.getColumnModel().getColumn(c).setCellRenderer(textR);

        t.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(
                    JTable tbl, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel("  " + val + "  ", SwingConstants.CENTER);
                lbl.setFont(ReceptionistUIHelper.F_BOLD_S);
                lbl.setOpaque(true);
                String v = val == null ? "" : val.toString().toLowerCase();
                if      (v.contains("critical")){ lbl.setBackground(new Color(254, 226, 226)); lbl.setForeground(new Color(185, 28, 28));   }
                else if (v.contains("high"))    { lbl.setBackground(new Color(254, 215, 170)); lbl.setForeground(new Color(194, 65, 12));   }
                else if (v.contains("medium"))  { lbl.setBackground(ReceptionistUIHelper.C_AMBER_BG); lbl.setForeground(new Color(180, 83, 9));   }
                else if (v.contains("low"))     { lbl.setBackground(ReceptionistUIHelper.C_GREEN_BG);  lbl.setForeground(new Color(21, 128, 61));  }
                else                            { lbl.setBackground(ReceptionistUIHelper.C_WHITE);    lbl.setForeground(ReceptionistUIHelper.C_DARK);   }
                return lbl;
            }
        });

        t.getColumnModel().getColumn(7).setCellRenderer(new ManageButtonRenderer());
        t.getColumnModel().getColumn(7).setCellEditor(new ManageButtonEditor(t, dialogOwner));

        int[] widths = {80, 130, 100, 70, 150, 110, 110, 100};
        for (int i = 0; i < widths.length; i++)
            t.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        t.getColumnModel().getColumn(7).setMaxWidth(120);

        return t;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MANAGE BUTTON RENDERER
    // ─────────────────────────────────────────────────────────────────────────
    static class ManageButtonRenderer implements TableCellRenderer {
        private final JButton btn;
        ManageButtonRenderer() {
            btn = new JButton("Manage");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(new Color(30, 90, 210));
            btn.setOpaque(true);
            btn.setBackground(Color.WHITE);
            btn.setBorder(new LineBorder(new Color(30, 90, 210), 1, true));
            btn.setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            return btn;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MANAGE BUTTON EDITOR
    // ─────────────────────────────────────────────────────────────────────────
    static class ManageButtonEditor extends AbstractCellEditor implements TableCellEditor {
        private final JButton btn;
        private final JTable  table;
        private final JFrame  dialogOwner;
        private int           editingRow;

        ManageButtonEditor(JTable table, JFrame ownerFrame) {
            this.table       = table;
            this.dialogOwner = ownerFrame;
            btn = new JButton("Manage");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setForeground(new Color(30, 90, 210));
            btn.setOpaque(true);
            btn.setBackground(Color.WHITE);
            btn.setBorder(new LineBorder(new Color(30, 90, 210), 1, true));
            btn.setFocusPainted(false);
            btn.addActionListener(e -> {
                int modelRow = table.convertRowIndexToModel(editingRow);
                fireEditingStopped();
                showDialog(dialogOwner, modelRow);
            });
        }

        @Override
        public Component getTableCellEditorComponent(
                JTable tbl, Object value, boolean isSelected, int row, int column) {
            editingRow = row;
            return btn;
        }

        @Override public Object  getCellEditorValue()              { return ""; }
        @Override public boolean isCellEditable(EventObject e)     { return true; }
        @Override public boolean shouldSelectCell(EventObject e)   { return true; }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  MANAGE DIALOG
    // ─────────────────────────────────────────────────────────────────────────
    static void showDialog(JFrame passedOwner, int row) {

        JFrame frame = passedOwner;
        if (frame == null) {
            for (Frame f : Frame.getFrames())
                if (f instanceof JFrame jf && f.isVisible()) { frame = jf; break; }
        }

        String apptId  = ReceptionistDataStore.apptModel.getValueAt(row, 0).toString();
        String curDoc  = ReceptionistDataStore.apptModel.getValueAt(row, 4).toString().trim();
        String curDept = ReceptionistDataStore.apptModel.getValueAt(row, 5).toString().trim();

        JDialog dlg = new JDialog(frame, "Manage — " + apptId, true);
        dlg.setSize(700, 720);
        dlg.setLocationRelativeTo(frame);
        dlg.setLayout(new BorderLayout());

        JPanel body = new JPanel(new BorderLayout(0, 14));
        body.setBackground(ReceptionistUIHelper.C_WHITE);
        body.setBorder(new EmptyBorder(22, 26, 22, 26));

        // Info banner
        JLabel banner = new JLabel("<html><b>Patient:</b> " +
            ReceptionistDataStore.apptModel.getValueAt(row, 1) +
            " &nbsp;|&nbsp; <b>Appointment:</b> " + apptId + "</html>");
        banner.setFont(ReceptionistUIHelper.F_BODY);
        banner.setForeground(ReceptionistUIHelper.C_DARK);
        banner.setOpaque(true);
        banner.setBackground(ReceptionistUIHelper.C_SKY);
        banner.setBorder(new CompoundBorder(
            new LineBorder(ReceptionistUIHelper.C_BORDER, 1, true),
            new EmptyBorder(10, 14, 10, 14)));

        // ── Department combo ──────────────────────────────────────────────────
        DefaultComboBoxModel<String> deptModel =
            new DefaultComboBoxModel<>(ReceptionistDataStore.getDynamicDepartments());
        JComboBox<String> cbDept = new JComboBox<>(deptModel);
        cbDept.setFont(ReceptionistUIHelper.F_SMALL);
        cbDept.setBackground(ReceptionistUIHelper.C_WHITE);
        // Pre-select current dept
        for (int i = 0; i < cbDept.getItemCount(); i++)
            if (cbDept.getItemAt(i).equalsIgnoreCase(curDept)) { cbDept.setSelectedIndex(i); break; }

        // ── Doctor combo — filtered by dept ───────────────────────────────────
        DefaultComboBoxModel<String> docModel = new DefaultComboBoxModel<>();
        JComboBox<String> cbDoc = new JComboBox<>(docModel);
        cbDoc.setFont(ReceptionistUIHelper.F_SMALL);
        cbDoc.setBackground(ReceptionistUIHelper.C_WHITE);

        boolean[] updating = {false};

        Runnable fillDoctors = () -> {
            updating[0] = true;
            String dept = (String) cbDept.getSelectedItem();
            String[] depts = ReceptionistDataStore.getDynamicDepartments();
            List<String> docs = ReceptionistDataStore.getDynamicDoctorsByDept(
                dept != null ? dept : depts[0]);
            docModel.removeAllElements();
            for (String d : docs) docModel.addElement(d);
            // Try to re-select the original doctor if they belong to this dept
            boolean found = false;
            for (int i = 0; i < cbDoc.getItemCount(); i++) {
                if (cbDoc.getItemAt(i).equals(curDoc)) { cbDoc.setSelectedIndex(i); found = true; break; }
            }
            if (!found && !docs.isEmpty()) cbDoc.setSelectedIndex(0);
            updating[0] = false;
        };

        // ── Slot picker ───────────────────────────────────────────────────────
        SlotPickerPanel slotPicker = new SlotPickerPanel();
        String curTime = ReceptionistDataStore.apptModel.getValueAt(row, 3).toString().trim();
        slotPicker.setSelectedSlot(curTime);

        // ── Date field ────────────────────────────────────────────────────────
        Runnable[] refreshSlotRef = {null};
        JTextField tfDate = ReceptionistUIHelper.dateField("DD/MM/YYYY  or  DD MMM YYYY",
            () -> { if (refreshSlotRef[0] != null) refreshSlotRef[0].run(); });
        tfDate.setText(ReceptionistDataStore.apptModel.getValueAt(row, 2).toString());

        // ── Priority combo ────────────────────────────────────────────────────
        JComboBox<String> cbPriority = ReceptionistUIHelper.combo(new String[]{
            "Low","Medium","High","Critical"});
        String curPriority = ReceptionistDataStore.apptModel.getValueAt(row, 6).toString();
        for (int i = 0; i < cbPriority.getItemCount(); i++)
            if (cbPriority.getItemAt(i).equals(curPriority)) { cbPriority.setSelectedIndex(i); break; }

        // ── Refresh slots logic ───────────────────────────────────────────────
        // When status is Cancelled, the slot picker shows the freed slot as available.
        // We exclude this appointment's own ID so its current slot isn't counted as booked.
        Runnable refreshSlots = () -> {
            if (updating[0]) return;
            String doc   = (String) cbDoc.getSelectedItem();
            String dateN = ReceptionistDataStore.normaliseDate(tfDate.getText());
            if (doc == null || doc.isEmpty() || dateN.isEmpty()) return;

            slotPicker.clearSelection();
            slotPicker.refresh(doc, dateN, "");

            // Re-highlight the currently saved time slot, unless appointment is cancelled
            String lifecycleStatus = ReceptionistDataStore.appointmentStatusMap.get(apptId);
            String savedTime   = ReceptionistDataStore.apptModel.getValueAt(row, 3).toString().trim();
            if (!"Cancelled".equalsIgnoreCase(lifecycleStatus) && !savedTime.isEmpty()) {
                slotPicker.setSelectedSlot(savedTime);
            }
        };
        refreshSlotRef[0] = refreshSlots;

        // ── Wire listeners ────────────────────────────────────────────────────
        cbDept.addActionListener(e -> {
            if (updating[0]) return;
            fillDoctors.run();
            SwingUtilities.invokeLater(refreshSlots);
        });
        cbDoc.addActionListener(e -> {
            if (updating[0]) return;
            SwingUtilities.invokeLater(refreshSlots);
        });

        // Refresh slots when doctor/date changes
        cbPriority.addActionListener(e -> {
            SwingUtilities.invokeLater(refreshSlots);
        });

        // Initial fill — pre-selects current dept's doctors and highlights curDoc
        fillDoctors.run();
        // Initial slot grid load
        SwingUtilities.invokeLater(refreshSlots);

        // Listen for roster changes and refresh the dialog UI
        ReceptionistDataStore.addRosterListener(() -> {
            SwingUtilities.invokeLater(() -> {
                deptModel.removeAllElements();
                for (String d : ReceptionistDataStore.getDynamicDepartments()) {
                    deptModel.addElement(d);
                }
                // Re-select current dept
                for (int i = 0; i < cbDept.getItemCount(); i++)
                    if (cbDept.getItemAt(i).equalsIgnoreCase(curDept)) { cbDept.setSelectedIndex(i); break; }
                fillDoctors.run();
                refreshSlots.run();
            });
        });

        // ── Form grid ─────────────────────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(0, 2, 14, 12));
        grid.setOpaque(false);
        grid.add(ReceptionistUIHelper.fg("Department", cbDept));
        grid.add(ReceptionistUIHelper.fg("Doctor",     cbDoc));
        grid.add(ReceptionistUIHelper.fg("Date",       tfDate));
        grid.add(ReceptionistUIHelper.fg("Priority",     cbPriority));

        // ── Buttons ───────────────────────────────────────────────────────────
        JButton btnSave       = ReceptionistUIHelper.primary("✅  Save Changes");
        JButton btnCancelAppt = new JButton("✗  Cancel Appointment");
        btnCancelAppt.setFont(ReceptionistUIHelper.F_BOLD_S);
        btnCancelAppt.setForeground(ReceptionistUIHelper.C_RED);
        btnCancelAppt.setContentAreaFilled(false);
        btnCancelAppt.setBorder(new LineBorder(ReceptionistUIHelper.C_RED, 1, true));
        btnCancelAppt.setFocusPainted(false);
        btnCancelAppt.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JButton btnClose = ReceptionistUIHelper.ghost("Close");
        btnClose.addActionListener(e -> dlg.dispose());

        btnSave.addActionListener(e -> {
            String selPriority = cbPriority.getSelectedItem() != null
                ? cbPriority.getSelectedItem().toString() : "Low";

            String patientName = ReceptionistDataStore.apptModel.getValueAt(row, 1).toString();

            if (!ReceptionistUIHelper.validateDateField(tfDate, dlg)) return;

            String newDoc    = cbDoc.getSelectedItem() != null
                ? cbDoc.getSelectedItem().toString().trim() : "";
            String newDateDisplay = tfDate.getText().trim();
            String newDate   = ReceptionistDataStore.normaliseDate(newDateDisplay);
            String newTime   = slotPicker.getSelectedSlot();
            String newDept   = cbDept.getSelectedItem().toString();
            String newPriority = selPriority;

            // Read current model values for comparison
            String curDocM   = ReceptionistDataStore.apptModel.getValueAt(row, 4).toString().trim();
            String curDateM  = ReceptionistDataStore.apptModel.getValueAt(row, 2).toString().trim();
            String curTimeM  = ReceptionistDataStore.apptModel.getValueAt(row, 3).toString().trim();
            String curPriM   = ReceptionistDataStore.apptModel.getValueAt(row, 6).toString().trim();

            // Block changing date to a past date
            String dateYmd = ReceptionistDataStore.toYmd(tfDate.getText().trim());
            String curDateYmd = ReceptionistDataStore.toYmd(curDateM);
            if (!dateYmd.equals(curDateYmd)) {
                String todayYmd = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                if (dateYmd.compareTo(todayYmd) < 0) {
                    ReceptionistUIHelper.showError(dlg, "Cannot change to a past date.\nPlease choose today or a future date.");
                    return;
                }
            }

            // Fallback: if slot picker is empty, use current model time
            if (newTime == null || newTime.isEmpty()) newTime = curTimeM;

            // If only priority changed, save & sync immediately
            boolean onlyPriority = newDoc.equals(curDocM)
                && newDateDisplay.equalsIgnoreCase(curDateM)
                && newTime.equals(curTimeM);
            if (onlyPriority && !newPriority.equals(curPriM)) {
                ReceptionistDataStore.apptModel.setValueAt(newPriority, row, 6);
                if (newPriority.equalsIgnoreCase("High") || newPriority.equalsIgnoreCase("Critical")
                        || newPriority.equalsIgnoreCase("Medium")) {
                    String issue = JOptionPane.showInputDialog(dlg,
                        "Enter issue/notes for the emergency queue:", "Emergency Queue Note",
                        JOptionPane.PLAIN_MESSAGE);
                    if (issue == null) { dlg.dispose(); return; }
                    if (issue.trim().isEmpty()) issue = "Priority escalated to " + newPriority;
                    String emP, emS;
                    if (newPriority.equalsIgnoreCase("Critical")) { emP = "P1"; emS = "CRITICAL"; }
                    else if (newPriority.equalsIgnoreCase("High")) { emP = "P2"; emS = "HIGH PRIORITY"; }
                    else { emP = "P3"; emS = "MODERATE"; }
                    Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
                    if (!ds.isInEmergency(patientName)) {
                        ds.setDoctorDisplayName(ReceptionistDataStore.stripDr(newDoc));
                        ds.addEmergencyPatient(new Object[]{emP, patientName, "--", issue, newTime, "--", emS});
                        HospitalSystem.addSharedEmergency(apptId, patientName, issue, newPriority, ReceptionistDataStore.stripDr(newDoc), newDept, newDate, newTime);
                    }
                }
                JOptionPane.showMessageDialog(dlg,
                    "✅  Priority updated to " + newPriority + "\n\nPatient: " + patientName,
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
                dlg.dispose();
                return;
            }

            // Clear old slot from shared tracker if doc/date/time changed
            String oldDoc  = ReceptionistDataStore.apptModel.getValueAt(row, 4).toString().trim();
            String oldDate = ReceptionistDataStore.apptModel.getValueAt(row, 2).toString().trim();
            String oldTime = ReceptionistDataStore.apptModel.getValueAt(row, 3).toString().trim();
            if (!oldDoc.equals("Unassigned") && !oldDate.isEmpty() && !oldTime.isEmpty()
                    && (!oldDoc.equals(newDoc) || !oldDate.equalsIgnoreCase(newDateDisplay) || !oldTime.equals(newTime))) {
                String oldDateYmd = ReceptionistDataStore.toYmd(oldDate);
                ReceptionistDataStore.cancelInSharedStores(patientName, oldDateYmd, oldTime);
                HospitalSystem.clearSlot(
                    ReceptionistDataStore.stripDr(oldDoc),
                    oldDateYmd,
                    oldTime);
            }

            ReceptionistDataStore.apptModel.setValueAt(newDoc,       row, 4);
            ReceptionistDataStore.apptModel.setValueAt(newDept,      row, 5);
            ReceptionistDataStore.apptModel.setValueAt(newDate,      row, 2);
            ReceptionistDataStore.apptModel.setValueAt(newTime,      row, 3);
            ReceptionistDataStore.apptModel.setValueAt(newPriority,  row, 6);
            if (!newDoc.equals("Unassigned")) {
                ReceptionistDataStore.syncAppointmentToSharedStores(
                    patientName, newDoc, newDate, newTime, apptId, "");
                HospitalSystem.markSlotBooked(
                    ReceptionistDataStore.stripDr(newDoc),
                    ReceptionistDataStore.toYmd(newDate),
                    newTime);
            } else {
                // Still sync for shared tracking even if Unassigned
                String dept = ReceptionistDataStore.getDeptForDoctor(newDoc);
                HospitalSystem.addSharedAppointment(apptId, patientName, newDoc, dept, newDate, newTime, "");
            }
            // Sync to emergency queue if priority is High/Critical/Medium
            if (newPriority.equalsIgnoreCase("High") || newPriority.equalsIgnoreCase("Critical")
                    || newPriority.equalsIgnoreCase("Medium")) {
                String issue = JOptionPane.showInputDialog(dlg,
                    "Enter issue/notes for the emergency queue:", "Emergency Queue Note",
                    JOptionPane.PLAIN_MESSAGE);
                if (issue != null && issue.trim().isEmpty()) issue = "Priority set to " + newPriority;
                String emP, emS;
                if (newPriority.equalsIgnoreCase("Critical")) { emP = "P1"; emS = "CRITICAL"; }
                else if (newPriority.equalsIgnoreCase("High")) { emP = "P2"; emS = "HIGH PRIORITY"; }
                else { emP = "P3"; emS = "MODERATE"; }
                Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
                if (!ds.isInEmergency(patientName) && issue != null) {
                    ds.setDoctorDisplayName(ReceptionistDataStore.stripDr(newDoc));
                    ds.addEmergencyPatient(new Object[]{emP, patientName, "--", issue, newTime, "--", emS});
                    HospitalSystem.addSharedEmergency(apptId, patientName, issue, newPriority, ReceptionistDataStore.stripDr(newDoc), newDept, newDate, newTime);
                }
            }
            JOptionPane.showMessageDialog(dlg,
                "✅  Changes saved!\n\nPatient: " + patientName +
                "\nDoctor: " + newDoc + "\nDate: " + newDate +
                "  at  " + newTime + "\nPriority: " + newPriority,
                "Saved", JOptionPane.INFORMATION_MESSAGE);
            dlg.dispose();
        });

        btnCancelAppt.addActionListener(e -> {
            if (JOptionPane.showConfirmDialog(dlg,
                    "<html>Cancel this appointment?<br>" +
                    "<small>The time slot will be freed immediately.</small></html>",
                    "Confirm Cancellation",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {

                String oldTime = ReceptionistDataStore.apptModel.getValueAt(row, 3).toString().trim();
                String oldDate = ReceptionistDataStore.apptModel.getValueAt(row, 2).toString().trim();
                String patientName = ReceptionistDataStore.apptModel.getValueAt(row, 1).toString();
                String oldDoc  = ReceptionistDataStore.apptModel.getValueAt(row, 4).toString().trim();

                ReceptionistDataStore.appointmentStatusMap.put(apptId, "Cancelled");

                // Sync cancelled status to shared tracker
                HospitalSystem.updateSharedAppointmentStatus(apptId, "Cancelled");

                if (!oldDoc.equals("Unassigned") && !oldDate.isEmpty() && !oldTime.isEmpty()) {
                    ReceptionistDataStore.cancelInSharedStores(patientName, ReceptionistDataStore.toYmd(oldDate), oldTime);
                }

                // Remove from receptionist display
                ReceptionistDataStore.removeFromApptModel(patientName, ReceptionistDataStore.toYmd(oldDate), oldTime, ReceptionistDataStore.stripDr(oldDoc));

                slotPicker.clearSelection();
                String doc   = (String) cbDoc.getSelectedItem();
                String dateN = ReceptionistDataStore.normaliseDate(tfDate.getText());
                if (doc != null && !doc.isEmpty() && !dateN.isEmpty()) {
                    slotPicker.refresh(doc, dateN, apptId);
                }

                dlg.dispose();
            }
        });

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnRow.setOpaque(false);
        btnRow.add(btnClose); btnRow.add(btnCancelAppt); btnRow.add(btnSave);

        JPanel centre = new JPanel();
        centre.setLayout(new BoxLayout(centre, BoxLayout.Y_AXIS));
        centre.setOpaque(false);
        centre.add(grid);
        centre.add(Box.createVerticalStrut(12));
        centre.add(slotPicker);

        body.add(banner,  BorderLayout.NORTH);
        body.add(centre,  BorderLayout.CENTER);
        body.add(btnRow,  BorderLayout.SOUTH);
        dlg.add(body);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  FILTER
    // ─────────────────────────────────────────────────────────────────────────
    private void applyFilter(TableRowSorter<DefaultTableModel> sorter,
                             String text, String status) {
        List<RowFilter<DefaultTableModel, Object>> filters = new ArrayList<>();
        if (!text.isEmpty())
            filters.add(RowFilter.regexFilter("(?i)" + text));
        if (!status.equals("All"))
            filters.add(RowFilter.regexFilter("(?i)^" + status + "$", 6));
        if (filters.isEmpty())        sorter.setRowFilter(null);
        else if (filters.size() == 1) sorter.setRowFilter(filters.get(0));
        else                          sorter.setRowFilter(RowFilter.andFilter(filters));
    }
}