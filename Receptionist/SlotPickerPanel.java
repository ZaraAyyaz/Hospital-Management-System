package Receptionist;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * SlotPickerPanel
 * Reusable time-slot grid. Shows only slots within the selected doctor's
 * shift windows. No hourly break — every slot in a shift window is bookable.
 *
 * Slot states:
 *  • Available (white/blue tint) – bookable
 *  • Selected  (blue)            – clicked by receptionist
 *  • Booked    (grey)            – already has an appointment on this date
 *
 * Key fix: isCancelled flag passed to refresh() ensures that when an
 * appointment is cancelled, its slot is never counted as booked in the grid.
 * getBookedSlots() already excludes cancelled rows from the data model, so
 * simply passing the apptId as excludeApptId is sufficient — the grid always
 * re-queries live data on every refresh() call.
 */
public class SlotPickerPanel extends JPanel {

    private final JPanel   slotContainer;
    private final JLabel   selectedLabel;
    private final JLabel   shiftInfoLabel;
    private final String[] selectedSlot = {""};

    public SlotPickerPanel() {
        setLayout(new BorderLayout(0, 6));
        setOpaque(false);

        selectedLabel  = new JLabel("  Select a doctor and date, then click a slot below");
        selectedLabel.setFont(ReceptionistUIHelper.F_BOLD_S);
        selectedLabel.setForeground(ReceptionistUIHelper.C_MUTED);

        shiftInfoLabel = new JLabel("");
        shiftInfoLabel.setFont(ReceptionistUIHelper.F_SMALL);
        shiftInfoLabel.setForeground(ReceptionistUIHelper.C_NAVY);

        slotContainer = new JPanel(new BorderLayout());
        slotContainer.setOpaque(false);

        setBorder(new CompoundBorder(
            new TitledBorder(
                new LineBorder(ReceptionistUIHelper.C_BORDER, 1, true),
                "  Available Time Slots  ",
                TitledBorder.LEFT, TitledBorder.TOP,
                ReceptionistUIHelper.F_BOLD_S, ReceptionistUIHelper.C_NAVY),
            new EmptyBorder(6, 8, 8, 8)
        ));

        JPanel topRow = new JPanel();
        topRow.setLayout(new BoxLayout(topRow, BoxLayout.Y_AXIS));
        topRow.setOpaque(false);
        topRow.add(selectedLabel);
        topRow.add(shiftInfoLabel);

        add(topRow,        BorderLayout.NORTH);
        add(slotContainer, BorderLayout.CENTER);
    }

    public String getSelectedSlot() { return selectedSlot[0]; }

    public void setSelectedSlot(String slot) {
        if (slot == null || slot.isEmpty()) {
            selectedSlot[0] = "";
            return;
        }
        // Normalise 24-hour "08:30" → 12-hour "08:30 AM" for match with display
        try {
            selectedSlot[0] = new SimpleDateFormat("hh:mm a", Locale.ENGLISH)
                .format(new SimpleDateFormat("HH:mm", Locale.ENGLISH).parse(slot));
        } catch (Exception e) {
            selectedSlot[0] = slot; // already 12-hour or unknown
        }
    }

    public void clearSelection() {
        selectedSlot[0] = "";
        selectedLabel.setText("  Select a doctor and date, then click a slot below");
        selectedLabel.setForeground(ReceptionistUIHelper.C_MUTED);
        shiftInfoLabel.setText("");
        slotContainer.removeAll();
        slotContainer.revalidate();
        slotContainer.repaint();
    }

    /**
     * Rebuilds the slot grid for the given doctor / date combination.
     *
     * Only slots within the doctor's shift windows are shown.
     * Booked state is queried fresh from ReceptionistDataStore every call —
     * there is no caching. This means:
     *
     *   - After cancelling appointment X (which sets its status to "Cancelled"
     *     in apptModel), calling refresh() for the same doctor+date will show
     *     X's old slot as available, because getBookedSlots() skips cancelled rows.
     *
     *   - Passing the current appointment's own ID as excludeApptId ensures its
     *     own slot is never counted as booked (so it stays selectable/white).
     *
     * @param doctor        Display name of the doctor
     * @param dateN         Normalised date string (dd MMM yyyy)
     * @param excludeApptId Appointment ID to exclude from booked-slot lookup
     *                      (pass the current appointment's ID so its slot stays free)
     */
    public void refresh(String doctor, String dateN, String excludeApptId) {
        slotContainer.removeAll();

        if (doctor.equals("Unassigned") || dateN.isEmpty()) {
            JLabel hint = new JLabel("Select a doctor and date to view available slots",
                SwingConstants.CENTER);
            hint.setFont(ReceptionistUIHelper.F_SMALL);
            hint.setForeground(ReceptionistUIHelper.C_MUTED);
            slotContainer.add(hint, BorderLayout.CENTER);
            shiftInfoLabel.setText("");
            slotContainer.revalidate();
            slotContainer.repaint();
            return;
        }

        // Show shift label — prefers roster data, falls back to hardcoded
        shiftInfoLabel.setText("  " + ReceptionistDataStore.getShiftLabelDynamic(doctor));

        // Get available slots — uses DoctorRosterStore when available,
        // falls back to hardcoded shift-window logic
        String[] avail = ReceptionistDataStore.getAvailableSlotsForDoctor(doctor);
        List<String> displaySlots = new java.util.ArrayList<>(java.util.Arrays.asList(avail));

        // ── Live query — never cached ──────────────────────────────────────────
        // getBookedSlots skips rows whose status is "Cancelled", so cancelled
        // appointments will NOT appear as booked here, regardless of excludeApptId.
        // excludeApptId additionally skips the current appointment's own row so
        // its time slot remains selectable even if it was previously saved.
        Set<String> booked = ReceptionistDataStore.getBookedSlots(doctor, dateN, excludeApptId);

        // Normalise booked times to 12-hour AM/PM format for comparison,
        // since apptModel stores "08:30" (24-hour) while displaySlots use "08:30 AM".
        SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        java.util.Set<String> bookedNorm = new java.util.LinkedHashSet<>();
        for (String b : booked) {
            try {
                bookedNorm.add(sdf12.format(sdf24.parse(b)));
            } catch (Exception e) {
                bookedNorm.add(b); // already 12-hour or unknown format
            }
        }

        long available = displaySlots.stream().filter(s -> !bookedNorm.contains(s)).count();

        // ── Header row ────────────────────────────────────────────────────────
        JPanel hdrRow = new JPanel(new BorderLayout()); hdrRow.setOpaque(false);
        JLabel docLbl = new JLabel("  " + doctor + "  ·  " + dateN);
        docLbl.setFont(ReceptionistUIHelper.F_BOLD_S);
        docLbl.setForeground(ReceptionistUIHelper.C_NAVY);
        JLabel cntLbl = new JLabel(available + " slot" + (available == 1 ? "" : "s") + " available   ");
        cntLbl.setFont(ReceptionistUIHelper.F_SMALL);
        cntLbl.setForeground(available > 0 ? ReceptionistUIHelper.C_GREEN : ReceptionistUIHelper.C_RED);
        hdrRow.add(docLbl, BorderLayout.WEST);
        hdrRow.add(cntLbl, BorderLayout.EAST);

        // ── Legend ────────────────────────────────────────────────────────────
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 2)); legend.setOpaque(false);
        legend.add(ReceptionistUIHelper.legendItem(ReceptionistUIHelper.C_BLUE_MID, "Available"));
        legend.add(ReceptionistUIHelper.legendItem(ReceptionistUIHelper.C_GREEN,    "Selected"));
        legend.add(ReceptionistUIHelper.legendItem(ReceptionistUIHelper.C_MUTED,    "Booked"));

        // ── Slot grid ─────────────────────────────────────────────────────────
        int cols = 8;
        int rows = Math.max(1, (int) Math.ceil((double) displaySlots.size() / cols));

        JPanel grid = new JPanel(new GridLayout(rows, cols, 6, 6));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(10, 0, 4, 0));

        JButton[] buttons = new JButton[displaySlots.size()];

        for (int i = 0; i < displaySlots.size(); i++) {
            final String  slot  = displaySlots.get(i);
            // Block past time slots for today
            boolean slotIsPast = false;
            try {
                String ymd = ReceptionistDataStore.toYmd(dateN);
                String todayYmd = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                if (todayYmd.equals(ymd)) {
                    java.util.Date sd = sdf12.parse(slot);
                    java.util.Calendar sc = java.util.Calendar.getInstance(); sc.setTime(sd);
                    int sm = sc.get(java.util.Calendar.HOUR_OF_DAY) * 60 + sc.get(java.util.Calendar.MINUTE);
                    int nm = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) * 60 + java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE);
                    if (sm <= nm) slotIsPast = true;
                }
            } catch (Exception e) {}
            final boolean isBkd = bookedNorm.contains(slot) || slotIsPast;
            final int     idx   = i;

            boolean isSel = slot.equals(selectedSlot[0]);

            JButton btn = new JButton(slot);
            btn.setFont(ReceptionistUIHelper.F_SLOT);
            btn.setFocusPainted(false);
            btn.setEnabled(!isBkd);
            btn.setPreferredSize(new Dimension(72, 32));
            btn.setOpaque(true);

            if (isBkd) {
                btn.setBackground(new Color(240, 242, 245));
                btn.setForeground(new Color(180, 190, 205));
                btn.setToolTipText(slot + " — Already booked");
                btn.setCursor(Cursor.getDefaultCursor());
            } else {
                btn.setBackground(isSel ? ReceptionistUIHelper.C_BLUE : ReceptionistUIHelper.C_WHITE);
                btn.setForeground(isSel ? ReceptionistUIHelper.C_WHITE : ReceptionistUIHelper.C_DARK);
                btn.setToolTipText(slot + " — Click to select");
                btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                if (!isSel) {
                    btn.addMouseListener(new MouseAdapter() {
                        @Override public void mouseEntered(MouseEvent e) {
                            if (!slot.equals(selectedSlot[0])) {
                                btn.setBackground(ReceptionistUIHelper.C_SKY);
                                btn.setForeground(ReceptionistUIHelper.C_NAVY);
                            }
                        }
                        @Override public void mouseExited(MouseEvent e) {
                            if (!slot.equals(selectedSlot[0])) {
                                btn.setBackground(ReceptionistUIHelper.C_WHITE);
                                btn.setForeground(ReceptionistUIHelper.C_DARK);
                            }
                        }
                    });
                }
                btn.addActionListener(e -> {
                    selectedSlot[0] = slot;
                    selectedLabel.setText("  ✓  Slot selected:  " + slot);
                    selectedLabel.setForeground(ReceptionistUIHelper.C_GREEN);
                    for (JButton b : buttons) {
                        if (b != null) {
                            boolean s = b.getText().equals(slot);
                            b.setBackground(s ? ReceptionistUIHelper.C_BLUE : ReceptionistUIHelper.C_WHITE);
                            b.setForeground(s ? ReceptionistUIHelper.C_WHITE : ReceptionistUIHelper.C_DARK);
                        }
                    }
                });
            }

            buttons[idx] = btn;
            grid.add(btn);
        }

        // Fill trailing empty cells so the GridLayout stays aligned
        int remainder = displaySlots.size() % cols;
        if (remainder != 0) {
            for (int i = 0; i < cols - remainder; i++) {
                JPanel empty = new JPanel(); empty.setOpaque(false);
                grid.add(empty);
            }
        }

        JPanel top = new JPanel();
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.add(hdrRow);
        top.add(Box.createVerticalStrut(4));
        top.add(legend);

        slotContainer.add(top,  BorderLayout.NORTH);
        slotContainer.add(grid, BorderLayout.CENTER);
        slotContainer.revalidate();
        slotContainer.repaint();
    }
}