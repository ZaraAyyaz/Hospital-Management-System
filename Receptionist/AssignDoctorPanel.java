package Receptionist;

import System.HospitalSystem;
import Doctor.doctorDataStore;          // ← bridge to Doctor module slot data
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.List;
import java.util.Set;

/**
 * AssignDoctorPanel
 *
 * UX flow:
 *  1. Pick Department  → Doctor combo shows ONLY that dept's 4 doctors.
 *  2. Pick Doctor      → slot grid refreshes for that doctor's shift.
 *  3. Pick Date        → slot grid refreshes.
 *  4. Click a slot     → slot is stored.
 *  5. Confirm          → appointment saved.
 */
public class AssignDoctorPanel {

    private final JFrame owner;

    public AssignDoctorPanel(JFrame owner) {
        this.owner = owner;
    }

    public JPanel build(String prefilledPatient) {
        JPanel p    = ReceptionistUIHelper.page();
        JPanel form = ReceptionistUIHelper.formCard("👨‍⚕️  Assign Doctor to Patient");

        // ── Patient / reference fields ────────────────────────────────────────
        JTextField tfPat  = ReceptionistUIHelper.styledField("Patient name or ID");
        JTextField tfAppt = ReceptionistUIHelper.styledField("#A-xxxx  (optional)");
        if (!prefilledPatient.isEmpty()) tfPat.setText(prefilledPatient);

        // ── Department combo ──────────────────────────────────────────────────
        // Build with a DefaultComboBoxModel so we control it cleanly.
        DefaultComboBoxModel<String> deptModel =
            new DefaultComboBoxModel<>(ReceptionistDataStore.getDynamicDepartments());
        JComboBox<String> cbDept = new JComboBox<>(deptModel);
        cbDept.setFont(ReceptionistUIHelper.F_SMALL);
        cbDept.setBackground(ReceptionistUIHelper.C_WHITE);

        // ── Doctor combo — populated dynamically from selected dept ───────────
        DefaultComboBoxModel<String> docModel = new DefaultComboBoxModel<>();
        JComboBox<String> cbDoc = new JComboBox<>(docModel);
        cbDoc.setFont(ReceptionistUIHelper.F_SMALL);
        cbDoc.setBackground(ReceptionistUIHelper.C_WHITE);

        // ── Slot picker ───────────────────────────────────────────────────────
        SlotPickerPanel slotPicker = new SlotPickerPanel();

        // Guard flag — prevents listener re-entrancy while we repopulate combos
        boolean[] updating = {false};

        // ── Helper: fill cbDoc with doctors of the currently selected dept ────
        Runnable fillDoctors = () -> {
            updating[0] = true;
            try {
                String dept = (String) cbDept.getSelectedItem();
                String[] depts = ReceptionistDataStore.getDynamicDepartments();
                List<String> docs = ReceptionistDataStore.getDynamicDoctorsByDept(
                    dept != null ? dept : depts[0]);
                docModel.removeAllElements();
                for (String d : docs) docModel.addElement(d);
                if (!docs.isEmpty()) cbDoc.setSelectedIndex(0);
            } finally {
                updating[0] = false;
            }
        };

        // Declared first so the refreshSlots lambda can capture it legally
        JTextField[] tfDate_ref = {null};

        // ── Helper: refresh slot grid ─────────────────────────────────────────
        Runnable refreshSlots = () -> {
            if (updating[0]) return;
            slotPicker.clearSelection();
            String doc   = (String) cbDoc.getSelectedItem();
            String dateN = ReceptionistDataStore.normaliseDate(tfDate_ref[0] != null
                ? tfDate_ref[0].getText() : "");
            if (doc == null || doc.isEmpty() || dateN.isEmpty()) return;
            slotPicker.refresh(doc, dateN, "");
        };

        // Use a one-element array so the lambda below can reference refreshSlotsWithSync
        // after it is defined (Java requires effectively-final but array contents are mutable).
        Runnable[] refreshSlotsWithSync_ref = {null};   // filled in below

        JTextField tfDate = ReceptionistUIHelper.dateField(
            "DD/MM/YYYY  or  DD MMM YYYY",
            () -> { if (refreshSlotsWithSync_ref[0] != null) refreshSlotsWithSync_ref[0].run(); });
        tfDate_ref[0] = tfDate;

        // Initial fill (no listeners fire during this because updating flag is set)
        fillDoctors.run();

        // ── Bridge: pull booked slots from doctorDataStore into ReceptionistDataStore ──
        // Called before every slot-grid refresh so that slots booked inside the
        // Doctor's own schedule panel appear as "already booked" here too.
        Runnable syncDoctorStoreSlots = () -> {
            String doc  = (String) cbDoc.getSelectedItem();
            String dateN = ReceptionistDataStore.normaliseDate(
                tfDate_ref[0] != null ? tfDate_ref[0].getText() : "");
            if (doc == null || doc.isEmpty() || dateN.isEmpty()) return;

            // Convert receptionist display name → doctor store name (strip "Dr. " prefix)
            String bareDocName = ReceptionistDataStore.stripDr(doc);
            // Convert normalised date (dd/MM/yyyy or dd MMM yyyy) → yyyy-MM-dd for doctorDataStore
            String ymdDate = ReceptionistDataStore.toYmd(dateN);

            doctorDataStore ds = doctorDataStore.get();
            // Only sync when the logged-in doctor matches the selected doctor
            if (!ds.getDoctorDisplayName().equalsIgnoreCase(bareDocName)
                    && !ds.getDoctorDisplayName().toLowerCase()
                            .contains(bareDocName.toLowerCase())) return;

            javax.swing.table.DefaultTableModel am = ds.getAppointmentModel();
            for (int i = 0; i < am.getRowCount(); i++) {
                String rowDate = String.valueOf(am.getValueAt(i, 2));
                String rowTime = String.valueOf(am.getValueAt(i, 1));
                if (ymdDate.equals(rowDate) && rowTime != null && !rowTime.isEmpty()) {
                    // Register the slot in ReceptionistDataStore's booked-slot set
                    // so SlotPickerPanel.refresh() will mark it as taken.
                    HospitalSystem.markSlotBooked(
                        ReceptionistDataStore.stripDr(doc),
                        ReceptionistDataStore.toYmd(dateN), rowTime);
                }
            }
        };

        // Wrap refreshSlots so it always syncs first
        Runnable refreshSlotsWithSync = () -> {
            syncDoctorStoreSlots.run();
            refreshSlots.run();
        };
        refreshSlotsWithSync_ref[0] = refreshSlotsWithSync;  // ← satisfy dateField's deferred ref

        // ── Wire listeners AFTER refreshSlotsWithSync is defined ─────────────
        cbDept.addActionListener(e -> {
            if (updating[0]) return;
            fillDoctors.run();              // repopulate doctors
            refreshSlotsWithSync.run();     // refresh grid for new first doctor
        });

        cbDoc.addActionListener(e -> {
            if (updating[0]) return;
            refreshSlotsWithSync.run();
        });

        // ── Listen for changes made inside the Doctor's schedule panel ────────
        // When the doctor books/edits/cancels a slot, re-sync and refresh our grid.
        doctorDataStore.get().addListener(() ->
            SwingUtilities.invokeLater(refreshSlotsWithSync));

        // Listen for roster changes and refresh department/doctor combos
        ReceptionistDataStore.addRosterListener(() -> {
            SwingUtilities.invokeLater(() -> {
                deptModel.removeAllElements();
                for (String d : ReceptionistDataStore.getDynamicDepartments()) {
                    deptModel.addElement(d);
                }
                fillDoctors.run();
                refreshSlots.run();
            });
        });

        // ── Form layout ───────────────────────────────────────────────────────
        JPanel formGrid = new JPanel(new GridLayout(0, 2, 16, 14)); formGrid.setOpaque(false);
        formGrid.add(ReceptionistUIHelper.fg("Patient Name / ID *", tfPat));
        formGrid.add(ReceptionistUIHelper.fg("Appointment Ref.",    tfAppt));
        formGrid.add(ReceptionistUIHelper.fg("Department *",        cbDept));
        formGrid.add(ReceptionistUIHelper.fg("Doctor *",            cbDoc));
        formGrid.add(ReceptionistUIHelper.fg("Date *",              tfDate));

        // ── Buttons ───────────────────────────────────────────────────────────
        JButton btnAssign = ReceptionistUIHelper.primary("Confirm Assignment");
        JButton btnClear  = ReceptionistUIHelper.ghost("Clear");

        btnClear.addActionListener(e -> {
            tfPat.setText(""); tfAppt.setText(""); tfDate.setText("");
            try { updating[0] = true; cbDept.setSelectedIndex(0); }
            finally { updating[0] = false; }
            fillDoctors.run();
            slotPicker.clearSelection();
        });

        btnAssign.addActionListener(e -> {
            String pat = tfPat.getText().trim();
            if (pat.isEmpty()) {
                ReceptionistUIHelper.showError(owner, "Please enter Patient Name or ID.");
                return;
            }
            if (!ReceptionistUIHelper.validateDateField(tfDate, owner)) return;
            if (cbDoc.getSelectedItem() == null) {
                ReceptionistUIHelper.showError(owner, "Please select a doctor.");
                return;
            }
            if (slotPicker.getSelectedSlot().isEmpty()) {
                ReceptionistUIHelper.showError(owner, "Please select a time slot from the grid.");
                return;
            }

            String date    = ReceptionistDataStore.normaliseDate(tfDate.getText().trim());
            String time    = slotPicker.getSelectedSlot();
            String docName = (String) cbDoc.getSelectedItem();

            // Double-check the slot isn't already booked for this doctor+date
            if (ReceptionistDataStore.getBookedSlots(docName, date, "").contains(time)) {
                ReceptionistUIHelper.showError(owner,
                    "Slot " + time + " on " + date + " is already booked for " + docName
                    + ".\nPlease choose another time.");
                return;
            }
            String dept    = (String) cbDept.getSelectedItem();
            String apptRef = tfAppt.getText().trim();
            boolean updated = false;
            String uid = "";

            if (!apptRef.isEmpty()) {
                for (int i = 0; i < ReceptionistDataStore.apptModel.getRowCount(); i++) {
                    if (ReceptionistDataStore.apptModel.getValueAt(i, 0).toString()
                            .equalsIgnoreCase(apptRef)) {
                        ReceptionistDataStore.apptModel.setValueAt(docName,     i, 4);
                        ReceptionistDataStore.apptModel.setValueAt(dept,        i, 5);
                        ReceptionistDataStore.apptModel.setValueAt(date,        i, 2);
                        ReceptionistDataStore.apptModel.setValueAt(time,        i, 3);
                        ReceptionistDataStore.apptModel.setValueAt("Confirmed", i, 6);
                        uid = apptRef; updated = true; break;
                    }
                }
            }
            if (!updated) {
                ReceptionistDataStore.apptCounter++;
                uid = "#A-00" + ReceptionistDataStore.apptCounter;
                ReceptionistDataStore.apptModel.addRow(
                    new Object[]{uid, pat, date, time, docName, dept, "Low", ""});
            }

            // Sync to Doctor.doctorDataStore and shared slot tracker
            if (!docName.equals("Unassigned") && !date.isEmpty() && !time.isEmpty()) {
                ReceptionistDataStore.syncAppointmentToSharedStores(pat, docName, date, time, uid);
                HospitalSystem.markSlotBooked(
                    ReceptionistDataStore.stripDr(docName),
                    ReceptionistDataStore.toYmd(date),
                    time);

                // ── Also push into doctorDataStore so the Doctor's schedule panel
                //    immediately shows this slot as booked (two-way sync). ──────
                doctorDataStore ds = doctorDataStore.get();
                String ymdDate = ReceptionistDataStore.toYmd(date);
                // Only add if the logged-in doctor matches the assigned doctor
                if (ds.getDoctorDisplayName().equalsIgnoreCase(
                            ReceptionistDataStore.stripDr(docName))
                        || ds.getDoctorDisplayName().toLowerCase().contains(
                            ReceptionistDataStore.stripDr(docName).toLowerCase())) {

                    // Prevent duplicate entries for same patient+time
                    javax.swing.table.DefaultTableModel am = ds.getAppointmentModel();
                    boolean alreadyInDs = false;
                    for (int i = 0; i < am.getRowCount(); i++) {
                        if (pat.equalsIgnoreCase(String.valueOf(am.getValueAt(i, 0)))
                                && time.equals(String.valueOf(am.getValueAt(i, 1)))
                                && ymdDate.equals(String.valueOf(am.getValueAt(i, 2)))) {
                            alreadyInDs = true; break;
                        }
                    }
                    if (!alreadyInDs) {
                        ds.addAppointment(pat, time, ymdDate);
                        ds.getHistoryAppointmentModel().addRow(new Object[]{
                            pat, "--", "--",
                            "Appointment", ymdDate, time,
                            "Assigned by Receptionist", "", "", "",
                            ds.getDoctorDisplayName()
                        });
                        ds.ensureInOverall(pat, "--", "--", "--", "--");
                        ds.notifyListeners();   // triggers doctorSchedulePanel to refresh
                    }
                }
                // The correct markSlotBooked call (with stripDr + toYmd) is
                // already made at lines 255-258 above.
            }

            JOptionPane.showMessageDialog(owner,
                "✅  Doctor Assigned!\n\n" +
                "Patient:        " + pat     + "\n" +
                "Doctor:         " + docName + "\n" +
                "Shift:          " + ReceptionistDataStore.getShiftLabelDynamic(docName) + "\n" +
                "Date & Time:    " + date + "  at  " + time + "\n" +
                "Department:     " + dept + "\n" +
                "Appointment ID: " + uid,
                "Assignment Confirmed", JOptionPane.INFORMATION_MESSAGE);

            tfPat.setText(""); tfAppt.setText(""); tfDate.setText("");
            try { updating[0] = true; cbDept.setSelectedIndex(0); }
            finally { updating[0] = false; }
            fillDoctors.run();
            slotPicker.clearSelection();
        });

        JPanel br = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0)); br.setOpaque(false);
        br.add(btnClear); br.add(btnAssign);

        JPanel body = new JPanel(new BorderLayout(0, 16)); body.setOpaque(false);
        body.add(formGrid,   BorderLayout.NORTH);
        body.add(slotPicker, BorderLayout.CENTER);
        body.add(br,         BorderLayout.SOUTH);
        form.add(body, BorderLayout.CENTER);
        ReceptionistUIHelper.stack(p, form);
        return p;
    }
}