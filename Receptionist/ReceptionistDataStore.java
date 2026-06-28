package Receptionist;

import System.DoctorRosterStore;
import System.HospitalSystem;
import javax.swing.table.DefaultTableModel;
import java.text.*;
import java.util.*;

/**
 * ReceptionistDataStore
 * Central data store shared across all receptionist panels.
 *
 * DOCTOR SHIFT SYSTEM (per department, 4 doctors, 24-hour coverage):
 * ─────────────────────────────────────────────────────────────────
 *  Slot#  Doctor  Shift Windows
 *    1    Doc 1   08:00–11:00  |  12:00–14:00
 *    2    Doc 2   14:00–17:00  |  18:00–20:00
 *    3    Doc 3   20:00–21:00  |  22:00–02:00  (crosses midnight)
 *    4    Doc 4   02:00–04:00  |  05:00–08:00
 *
 * Each patient slot = 15 minutes.
 * All :45 slots within shift windows are fully bookable (no hourly break).
 * Slots between shift windows (gap periods) are NOT bookable.
 */
public class ReceptionistDataStore {

    // ── Departments (matching HospitalAdmin) ────────────────────────────────
    public static final String[] DEPARTMENTS = {
        "Cardiology", "Electrophysiology", "Cardiac Imaging",
        "Interventional Cardiology", "Emergency Medicine"
    };

    // ── Doctor names keyed by "Dept|Slot" (matching HospitalAdmin) ──────────
    public static final int DOC_SLOTS = 4;
    public static final Map<String, String> DOC_NAMES = new LinkedHashMap<>();
    static {
        DOC_NAMES.put("Cardiology|1",                  "Dr. Kamran Khan");
        DOC_NAMES.put("Cardiology|2",                  "Dr. Faisal Qureshi");
        DOC_NAMES.put("Cardiology|3",                  "Dr. Anwar Latif");
        DOC_NAMES.put("Cardiology|4",                  "Dr. Bilal Javed");

        DOC_NAMES.put("Electrophysiology|1",           "Dr. Asif Malik");
        DOC_NAMES.put("Electrophysiology|2",           "Dr. Omer Shehzad");
        DOC_NAMES.put("Electrophysiology|3",           "Dr. Nabeel Shiraz");
        DOC_NAMES.put("Electrophysiology|4",           "Dr. Saad Ghafoor");

        DOC_NAMES.put("Cardiac Imaging|1",             "Dr. Haris Bilal");
        DOC_NAMES.put("Cardiac Imaging|2",             "Dr. Zubair Niazi");
        DOC_NAMES.put("Cardiac Imaging|3",             "Dr. Waqas Raza");
        DOC_NAMES.put("Cardiac Imaging|4",             "Dr. Yasir Arafat");

        DOC_NAMES.put("Interventional Cardiology|1",   "Dr. Zainab Raza");
        DOC_NAMES.put("Interventional Cardiology|2",   "Dr. Maryam Nawaz");
        DOC_NAMES.put("Interventional Cardiology|3",   "Dr. Amina Butt");
        DOC_NAMES.put("Interventional Cardiology|4",   "Dr. Sana Yousaf");

        DOC_NAMES.put("Emergency Medicine|1",          "Dr. Sarmad Ali");
        DOC_NAMES.put("Emergency Medicine|2",          "Dr. Hamza Tariq");
        DOC_NAMES.put("Emergency Medicine|3",          "Dr. Taimoor Hassan");
        DOC_NAMES.put("Emergency Medicine|4",          "Dr. Zeeshan Khan");
    }

    /** Flat list of all doctor names (for combo boxes). */
    public static final String[] DOCTOR_LIST;
    /** Maps doctor name → department. */
    public static final Map<String, String> DOC_DEPT = new LinkedHashMap<>();
    /** Maps doctor name → shift slot number (1–4). */
    public static final Map<String, Integer> DOC_SLOT_NUM = new LinkedHashMap<>();
    static {
        List<String> allDocs = new ArrayList<>();
        for (Map.Entry<String, String> e : DOC_NAMES.entrySet()) {
            String[] parts = e.getKey().split("\\|");
            String dept = parts[0];
            int    slot = Integer.parseInt(parts[1]);
            String name = e.getValue();
            allDocs.add(name);
            DOC_DEPT.put(name, dept);
            DOC_SLOT_NUM.put(name, slot);
        }
        allDocs.add("Unassigned");
        DOC_DEPT.put("Unassigned", "Cardiology");
        DOC_SLOT_NUM.put("Unassigned", 0);
        DOCTOR_LIST = allDocs.toArray(new String[0]);
    }

    // ── Shift windows (24-hour) ───────────────────────────────────────────────
    /**
     * Each entry: int[][] = { {startH, startM, endH, endM}, {startH, startM, endH, endM} }
     * End is EXCLUSIVE (i.e. 11:00 means up to but not including 11:00).
     * Slot 3 crosses midnight: 22:00–02:00 → stored as 22:00–26:00 (26 = 2 am next day).
     *
     *  Slot 1: 08:00–11:00  |  12:00–14:00
     *  Slot 2: 14:00–17:00  |  18:00–20:00
     *  Slot 3: 20:00–21:00  |  22:00–02:00
     *  Slot 4: 02:00–04:00  |  05:00–08:00
     */
    public static final Map<Integer, int[][]> SHIFT_WINDOWS = new LinkedHashMap<>();
    static {
        SHIFT_WINDOWS.put(1, new int[][]{{8,  0, 11, 0}, {12, 0, 14, 0}});
        SHIFT_WINDOWS.put(2, new int[][]{{14, 0, 17, 0}, {18, 0, 20, 0}});
        SHIFT_WINDOWS.put(3, new int[][]{{20, 0, 21, 0}, {22, 0, 26, 0}}); // 26:00 = 02:00 next day
        SHIFT_WINDOWS.put(4, new int[][]{{2,  0,  4, 0}, { 5, 0,  8, 0}});
    }

    // ── All 15-minute slots for 24 hours (96 total) ──────────────────────────
    public static final String[] TIME_SLOTS_ALL;

    /**
     * TIME_SLOTS is the full 96-slot array; same as TIME_SLOTS_ALL.
     * Referenced by SlotPickerPanel when doctor = "Unassigned".
     */
    public static final String[] TIME_SLOTS;

    /** Slots that fall completely outside every shift window (not bookable). */
    public static final Set<String> GAP_SLOTS = new LinkedHashSet<>();

    static {
        List<String> all = new ArrayList<>();
        for (int h = 0; h < 24; h++)
            for (int m = 0; m < 60; m += 15)
                all.add(String.format("%02d:%02d", h, m));
        TIME_SLOTS_ALL = all.toArray(new String[0]);
        TIME_SLOTS     = TIME_SLOTS_ALL;

        for (String s : TIME_SLOTS_ALL) {
            if (!isInAnyShiftStatic(s)) GAP_SLOTS.add(s);
        }
    }

    // ── Public helper methods ─────────────────────────────────────────────────

    /**
     * Returns only the slots that belong to a specific doctor (by shift number),
     * excluding break slots. "Unassigned" gets all non-gap slots.
     */
    public static String[] getSlotsForDoctor(String doctorName) {
        int slotNum = DOC_SLOT_NUM.getOrDefault(doctorName, 0);
        List<String> result = new ArrayList<>();
        for (String slot : TIME_SLOTS_ALL) {
            if (slotNum == 0) {
                if (!GAP_SLOTS.contains(slot)) result.add(slot);
            } else {
                if (slotInWindows(slot, SHIFT_WINDOWS.get(slotNum))) result.add(slot);
            }
        }
        return result.toArray(new String[0]);
    }

    /**
     * True if the slot should be disabled for this doctor:
     *   - it is a break slot  OR
     *   - it falls outside the doctor's shift windows  OR
     *   - it is a gap slot (outside all shifts)
     */
    public static boolean isSlotDisabled(String slot, String doctorName) {
        if (GAP_SLOTS.contains(slot)) return true;
        int slotNum = DOC_SLOT_NUM.getOrDefault(doctorName, 0);
        if (slotNum == 0) return false;
        return !slotInWindows(slot, SHIFT_WINDOWS.get(slotNum));
    }

    /** True if the slot falls between shifts (gap period — not bookable). */
    public static boolean isGap(String slot) {
        return GAP_SLOTS.contains(slot);
    }

    /** Human-readable shift description for a doctor. */
    public static String getShiftLabel(String doctorName) {
        int sn = DOC_SLOT_NUM.getOrDefault(doctorName, 0);
        return switch (sn) {
            case 1 -> "Shift 1:  08:00–11:00  ·  12:00–14:00";
            case 2 -> "Shift 2:  14:00–17:00  ·  18:00–20:00";
            case 3 -> "Shift 3:  20:00–21:00  ·  22:00–02:00";
            case 4 -> "Shift 4:  02:00–04:00  ·  05:00–08:00";
            default -> "No fixed shift";
        };
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    /** True if the slot falls within the given set of windows. */
    public static boolean slotInWindows(String slot, int[][] windows) {
        int[] hm   = parseSlot(slot);
        int   mins = hm[0] * 60 + hm[1];
        for (int[] w : windows) {
            int start = w[0] * 60 + w[1];
            int end   = w[2] * 60 + w[3]; // may be >1440 for midnight wrap
            if (end > 1440) {
                // Slot 3: 22:00(1320)–02:00(120 next day → stored as 26*60=1560)
                // A slot is inside if mins >= 1320 OR mins < 120
                int wrapEnd = end - 1440;
                if (mins >= start || mins < wrapEnd) return true;
            } else {
                if (mins >= start && mins < end) return true;
            }
        }
        return false;
    }

    private static boolean isInAnyShiftStatic(String slot) {
        for (int sn = 1; sn <= 4; sn++)
            if (slotInWindows(slot, SHIFT_WINDOWS.get(sn))) return true;
        return false;
    }

    public static int[] parseSlot(String slot) {
        String[] p = slot.split(":");
        return new int[]{ Integer.parseInt(p[0]), Integer.parseInt(p[1]) };
    }

    // ── Date formats ─────────────────────────────────────────────────────────
    public static final SimpleDateFormat FMT_DISPLAY =
        new SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH);

    private static final SimpleDateFormat[] ACCEPTED_FMTS;
    static {
        ACCEPTED_FMTS = new SimpleDateFormat[]{
            FMT_DISPLAY,
            new SimpleDateFormat("d MMM yyyy",  Locale.ENGLISH),
            new SimpleDateFormat("dd/MM/yyyy"),
            new SimpleDateFormat("d/M/yyyy"),
            new SimpleDateFormat("dd-MM-yyyy"),
            new SimpleDateFormat("dd/MM/yy"),
            new SimpleDateFormat("d/M/yy"),
        };
        for (SimpleDateFormat f : ACCEPTED_FMTS) f.setLenient(false);
        FMT_DISPLAY.setLenient(false);
    }

    public static String normaliseDate(String raw) {
        if (raw == null) return "";
        String t = raw.trim();
        if (t.isEmpty()) return "";
        for (SimpleDateFormat f : ACCEPTED_FMTS) {
            try { return FMT_DISPLAY.format(f.parse(t)); } catch (ParseException ignored) {}
        }
        return "";
    }

    /** Convert "dd MMM yyyy" (display format) → "yyyy-MM-dd" (ISO format). */
    public static String toYmd(String displayDate) {
        if (displayDate == null || displayDate.isEmpty()) return "";
        try { return new java.text.SimpleDateFormat("yyyy-MM-dd").format(FMT_DISPLAY.parse(displayDate)); }
        catch (Exception e) { return ""; }
    }

    public static boolean isValidDate(String raw) { return !normaliseDate(raw).isEmpty(); }

    // ── Shared table models ──────────────────────────────────────────────────
    public static final DefaultTableModel apptModel = new DefaultTableModel(
        new Object[][]{
            {"#A-0041","Ali Hassan",    "10 June 2026","08:00 AM","Dr. Kamran Khan",    "Cardiology",              "Low",     ""},
            {"#A-0042","Zara Malik",    "10 June 2026","02:00 PM","Dr. Faisal Qureshi", "Cardiology",              "Low",     ""},
            {"#A-0043","Imran Qureshi", "10 June 2026","09:00 AM","Dr. Asif Malik",     "Electrophysiology",       "Low",     ""},
            {"#A-0044","Nadia Bano",    "10 June 2026","11:00 AM","Dr. Kamran Khan",    "Cardiology",              "Low",     ""},
            {"#A-0045","Khalid Ahmed",  "10 June 2026","03:00 PM","Dr. Omer Shehzad",   "Electrophysiology",       "Low",     ""},
            {"#A-0046","Sana Fatima",   "10 June 2026","06:00 PM","Dr. Zubair Niazi",   "Cardiac Imaging",         "Low",     ""},
            {"#A-0047","Hamid Raza",    "10 June 2026","08:00 PM","Dr. Taimoor Hassan", "Cardiology",              "Low",     ""},
            {"#A-0048","Rukhsana Beg",  "10 June 2026","10:00 PM","Dr. Waqas Raza",     "Cardiac Imaging",         "Low",     ""},
            {"#A-0049","Dawood Yusuf",  "11 June 2026","12:00 PM","Dr. Zainab Raza",    "Interventional Cardiology","Low",     ""},
            {"#A-0050","Mariam Shah",   "11 June 2026","08:00 AM","Dr. Sarmad Ali",     "Cardiology",              "Low",     ""},
            {"#A-0051","Aisha Noor",   "10 June 2026","09:00 AM","Dr. Zeeshan Khan",  "Emergency Medicine",      "Critical",""},
            {"#A-0052","Babar Zaman",  "10 June 2026","10:00 AM","Dr. Sarmad Ali",     "Emergency Medicine",      "Medium",  ""},
            {"#A-0053","Hina Rehman",  "10 June 2026","11:00 AM","Dr. Haris Bilal",    "Cardiac Imaging",         "Low",     ""},
        },
        new String[]{"Appt ID","Patient","Date","Time","Doctor","Dept.","Priority","Action"}
    ) {
        @Override public boolean isCellEditable(int r, int c) { return c == 7; }
    };

    /** Tracks the actual appointment lifecycle status separately from priority display. */
    public static final Map<String, String> appointmentStatusMap = new HashMap<>();

    static {
        for (int r = 0; r < apptModel.getRowCount(); r++) {
            String patient = apptModel.getValueAt(r, 1).toString().trim();
            String doctor  = apptModel.getValueAt(r, 4).toString().trim();
            String date    = apptModel.getValueAt(r, 2).toString().trim();
            String time    = apptModel.getValueAt(r, 3).toString().trim();
            String apptId  = apptModel.getValueAt(r, 0).toString().trim();
            String rawDoc  = stripDr(doctor);
            String dateYmd = toYmd(date);
            // Track internal appointment lifecycle status separately from priority
            appointmentStatusMap.put(apptId, "Booked");
            HospitalSystem.markSlotBooked(rawDoc, dateYmd, time);
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
            ds.setDoctorDisplayName(rawDoc);
            ds.addAppointment(patient, time, dateYmd, rawDoc);
            // All appointments start as Pending — doctor must explicitly accept before
            // they appear in the consultation panel
            ds.updatePatientAppointmentStatus(patient, dateYmd, time, "Pending");
            // Only add to overall if not already present (don't overwrite existing data)
            boolean overallExists = false;
            for (int oi = 0; oi < ds.getOverallPatientModel().getRowCount(); oi++) {
                if (patient.equals(ds.getOverallPatientModel().getValueAt(oi, 0))) {
                    overallExists = true; break;
                }
            }
            if (!overallExists) ds.ensureInOverall(patient, "--", "--", "--", "--");
            Admin.HospitalAdmin.registerAppointment(apptId, patient, doctor, date + " " + time);
            // Sync to shared appointment tracking
            String dept = apptModel.getValueAt(r, 5).toString().trim();
            HospitalSystem.addSharedAppointment(apptId, patient, doctor, dept, date, time, "Booked");
            if (dept.equalsIgnoreCase("Emergency Medicine")) {
                HospitalSystem.addSharedEmergency(
                    apptId, patient, "Emergency registration", "Medium", doctor, dept, date, time);
            }
            // Add to doctor's history model so the weekly schedule shows these appointments
            ds.getHistoryAppointmentModel().addRow(new Object[]{
                patient, "--", "--", "Appointment", date, time,
                "", "", "", "", rawDoc
            });
        }
    }

    public static final DefaultTableModel walkModel = new DefaultTableModel(
        new Object[][]{
            {"W-01","Aisha Noor",  "10 June 2026","09:00 AM","Chest Pain",          "Critical","Dr. Zeeshan Khan", "5 min"},
            {"W-02","Babar Zaman", "10 June 2026","10:00 AM","Heart Palpitations",  "Medium",  "Dr. Sarmad Ali",   "18 min"},
            {"W-03","Hina Rehman", "10 June 2026","11:00 AM","Shortness of Breath", "Low",     "Dr. Haris Bilal",  "32 min"},
            {"W-04","Usman Tariq", "10 June 2026","02:00 PM","High Blood Pressure", "Low",     "Dr. Kamran Khan",  "40 min"},
        },
        new String[]{"Queue #","Patient","Date","Time","Complaint","Priority","Doctor","Wait Time"}
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };

    // Sync walk-in patients to the doctor's emergency model
    static {
        for (int r = 0; r < walkModel.getRowCount(); r++) {
            String walkDoc = str(walkModel.getValueAt(r, 6));
            String rawWalkDoc = stripDr(walkDoc);
            if (rawWalkDoc.isEmpty() || rawWalkDoc.equalsIgnoreCase("Unassigned")) continue;
            String walkPatient = str(walkModel.getValueAt(r, 1));
            String walkComplaint = str(walkModel.getValueAt(r, 4));
            String walkPriority = str(walkModel.getValueAt(r, 5));
            if ("Low".equalsIgnoreCase(walkPriority)) continue;
            String walkTime = str(walkModel.getValueAt(r, 3));
            String emPriority, emStatus;
            if (walkPriority.equalsIgnoreCase("Critical")) {
                emPriority = "P1"; emStatus = "CRITICAL";
            } else if (walkPriority.equalsIgnoreCase("Medium")) {
                emPriority = "P2"; emStatus = "HIGH PRIORITY";
            } else {
                emPriority = "P3"; emStatus = "MODERATE";
            }
            Doctor.doctorDataStore wds = Doctor.doctorDataStore.get();
            wds.setDoctorDisplayName(rawWalkDoc);
            wds.addEmergencyPatient(new Object[]{
                emPriority, walkPatient, "--", walkComplaint, walkTime, "--", emStatus
            });
        }
    }

    // ── Cross-system sync helpers ─────────────────────────────────────────────

    /** Convert "yyyy-MM-dd" → "dd MMM yyyy" */
    public static String ymdToDisplay(String ymd) {
        if (ymd == null || ymd.isEmpty()) return "";
        try {
            return FMT_DISPLAY.format(new java.text.SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(ymd));
        } catch (Exception e) {
            return ymd;
        }
    }

    /** Add a patient-self-booked appointment to apptModel (no-op if duplicate). */
    public static void addToApptModel(String apptId, String patient, String dateYmd, String time, String doctor, String dept, String status) {
        String displayDate = ymdToDisplay(dateYmd);
        if (displayDate.isEmpty()) return;
        String displayDoctor = addDr(doctor);
        // If same apptId exists, update its row instead of duplicating
        for (int r = 0; r < apptModel.getRowCount(); r++) {
            if (apptId.equals(str(apptModel.getValueAt(r, 0)))) {
                apptModel.setValueAt(displayDate, r, 2);
                apptModel.setValueAt(time, r, 3);
                apptModel.setValueAt(displayDoctor, r, 4);
                apptModel.setValueAt(dept, r, 5);
                appointmentStatusMap.put(apptId, status.isEmpty() ? "Booked" : status);
                return;
            }
        }
        // If patient has an "Unassigned" row, replace it with the real appointment
        for (int r = 0; r < apptModel.getRowCount(); r++) {
            String doc = str(apptModel.getValueAt(r, 4));
            if (patient.equalsIgnoreCase(str(apptModel.getValueAt(r, 1)))
                    && (doc.isEmpty() || doc.equals("Unassigned"))) {
                String oldId = str(apptModel.getValueAt(r, 0));
                apptModel.setValueAt(apptId, r, 0);
                apptModel.setValueAt(displayDate, r, 2);
                apptModel.setValueAt(time, r, 3);
                apptModel.setValueAt(displayDoctor, r, 4);
                apptModel.setValueAt(dept, r, 5);
                appointmentStatusMap.remove(oldId);
                appointmentStatusMap.put(apptId, status.isEmpty() ? "Booked" : status);
                return;
            }
        }
        // De-dup by patient+doctor+date+time (for fresh additions)
        for (int r = 0; r < apptModel.getRowCount(); r++) {
            if (patient.equalsIgnoreCase(str(apptModel.getValueAt(r, 1)))
                    && displayDoctor.equalsIgnoreCase(str(apptModel.getValueAt(r, 4)))
                    && displayDate.equals(str(apptModel.getValueAt(r, 2)))
                    && time.equals(str(apptModel.getValueAt(r, 3)))) {
                return;
            }
        }
        apptModel.addRow(new Object[]{apptId, patient, displayDate, time, displayDoctor, dept, "Low", ""});
        appointmentStatusMap.put(apptId, status.isEmpty() ? "Booked" : status);
    }

    /** Remove a matching row from apptModel (for completed/cancelled). */
    public static void removeFromApptModel(String patient, String dateYmd, String time, String doctor) {
        String displayDate = ymdToDisplay(dateYmd);
        String displayDoctor = addDr(doctor);
        for (int r = apptModel.getRowCount() - 1; r >= 0; r--) {
            if (patient.equalsIgnoreCase(str(apptModel.getValueAt(r, 1)))
                    && displayDoctor.equalsIgnoreCase(str(apptModel.getValueAt(r, 4)))
                    && displayDate.equals(str(apptModel.getValueAt(r, 2)))
                    && time.equals(str(apptModel.getValueAt(r, 3)))) {
                apptModel.removeRow(r);
                return;
            }
        }
    }

    /** Update the lifecycle status of a matching row (stored in status map, not the priority column). */
    public static void updateApptStatus(String patient, String dateYmd, String time, String doctor, String newStatus) {
        String displayDate = ymdToDisplay(dateYmd);
        String displayDoctor = addDr(doctor);
        for (int r = apptModel.getRowCount() - 1; r >= 0; r--) {
            if (patient.equalsIgnoreCase(str(apptModel.getValueAt(r, 1)))
                    && displayDoctor.equalsIgnoreCase(str(apptModel.getValueAt(r, 4)))
                    && displayDate.equals(str(apptModel.getValueAt(r, 2)))
                    && time.equals(str(apptModel.getValueAt(r, 3)))) {
                String apptId = apptModel.getValueAt(r, 0).toString().trim();
                if ("Completed".equals(newStatus) || "Cancelled".equals(newStatus)) {
                    apptModel.removeRow(r);
                } else {
                    appointmentStatusMap.put(apptId, newStatus);
                }
                return;
            }
        }
    }

    private static String str(Object o) { return o == null ? "" : o.toString().trim(); }

    // ── Counters ─────────────────────────────────────────────────────────────
    public static int apptCounter    = 53;
    public static int walkCounter    = 4;
    public static int patientCounter = 1000;

    // ── Slot availability query ───────────────────────────────────────────────
    public static Set<String> getBookedSlots(String doctor, String dateN, String excludeApptId) {
        Set<String> booked = new HashSet<>();
        for (int r = 0; r < apptModel.getRowCount(); r++) {
            String aid    = apptModel.getValueAt(r, 0).toString();
            String rdoc   = apptModel.getValueAt(r, 4).toString().trim();
            String rdate  = normaliseDate(apptModel.getValueAt(r, 2).toString());
            String rtime  = apptModel.getValueAt(r, 3).toString().trim();
            String status = apptModel.getValueAt(r, 6).toString();
            if (aid.equals(excludeApptId))           continue;
            String aidStatus = appointmentStatusMap.get(aid);
            if ("Cancelled".equalsIgnoreCase(aidStatus)) continue;
            if (rdoc.equals(doctor) && rdate.equals(dateN)) booked.add(rtime);
        }
        // Also check Doctor.doctorDataStore for self-service bookings
        try {
            String rawDoc = stripDr(doctor);
            javax.swing.table.DefaultTableModel dm = Doctor.doctorDataStore.get().getAppointmentModel();
            java.util.Map<String, String> statusMap = Doctor.doctorDataStore.get().patientAppointmentStatus;
            for (int r = 0; r < dm.getRowCount(); r++) {
                Object pObj = dm.getValueAt(r, 0);
                Object tObj = dm.getValueAt(r, 1);
                Object dObj = dm.getValueAt(r, 2);
                Object docObj = dm.getValueAt(r, 3);
                if (pObj == null || tObj == null || dObj == null || docObj == null) continue;
                String patientName = pObj.toString().trim();
                String rdoc  = docObj.toString().trim();
                String rtime = tObj.toString().trim();
                String rdate = dObj.toString().trim();
                if (rdoc.equals(rawDoc) && rdate.equals(toYmd(dateN))) {
                    String statusKey = patientName + "|" + rdate + "|" + rtime + "|" + rdoc;
                    String s = statusMap.get(statusKey);
                    if ("Completed".equals(s) || "Cancelled".equals(s)) continue;
                    booked.add(rtime);
                }
            }
        } catch (Exception e) {
            System.out.println("[Receptionist] getBookedSlots doctorDataStore: " + e.getMessage());
        }
        // Also check the shared HospitalSystem tracker (patient self-bookings)
        try {
            String rawDoc = stripDr(doctor);
            String dateYmd = toYmd(dateN); // convert "dd MMM yyyy" → "yyyy-MM-dd" for comparison
            java.util.Set<String> shared = HospitalSystem.getBookedSlotKeys();
            for (String key : shared) {
                String[] parts = key.split("\\|");
                if (parts.length == 3) {
                    String doc = parts[0];
                    String date = parts[1];
                    String time = parts[2];
                    if (doc.equals(rawDoc) && date.equals(dateYmd)) booked.add(time);
                }
            }
        } catch (Exception e) {
            System.out.println("[Receptionist] shared tracker check: " + e.getMessage());
        }
        return booked;
    }

    /** Get all doctors for a given department, in shift order 1→4. */
    public static List<String> getDoctorsByDept(String dept) {
        List<String> list = new ArrayList<>();
        for (int s = 1; s <= DOC_SLOTS; s++) {
            String name = DOC_NAMES.get(dept + "|" + s);
            if (name != null) list.add(name);
        }
        return list;
    }

    // ── Name format bridging ───────────────────────────────────────────────────
    /** Strips "Dr. " prefix. "Dr. Kamran Khan" → "Kamran Khan" */
    public static String stripDr(String name) {
        return name != null && name.startsWith("Dr. ") ? name.substring(4) : name;
    }

    /** Adds "Dr. " prefix. "Kamran Khan" → "Dr. Kamran Khan" */
    public static String addDr(String name) {
        return (name != null && !name.startsWith("Dr. ")) ? "Dr. " + name : name;
    }

    // ── Cross-system sync helpers ──────────────────────────────────────────────
    /** Syncs an appointment to Doctor.doctorDataStore, Admin.HospitalAdmin, and HospitalSystem shared data. */
    public static void syncAppointmentToSharedStores(String patientName, String doctorName, String date, String time, String apptId) {
        syncAppointmentToSharedStores(patientName, doctorName, date, time, apptId, "");
    }

    /** Syncs an appointment with a specific status. */
    public static void syncAppointmentToSharedStores(String patientName, String doctorName, String date, String time, String apptId, String status) {
        try {
            String rawDoc = stripDr(doctorName);
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
            ds.setDoctorDisplayName(rawDoc);
            ds.addAppointment(patientName, time, date, rawDoc); // addAppointment already calls notifyListeners()
            ds.getHistoryAppointmentModel().addRow(new Object[]{
                patientName, "--", "--", "Appointment", date, time,
                "", "", "", "", rawDoc
            });
            // Only add to overall if not already present (don't overwrite existing data with "--")
            boolean overallExists = false;
            for (int i = 0; i < ds.getOverallPatientModel().getRowCount(); i++) {
                if (patientName.equals(ds.getOverallPatientModel().getValueAt(i, 0))) {
                    overallExists = true; break;
                }
            }
            if (!overallExists) ds.ensureInOverall(patientName, "--", "--", "--", "--");
            // Explicitly notify so doctorSchedulePanel refreshes immediately
            ds.notifyListeners();
        } catch (Exception e) {
            System.out.println("[Receptionist] syncAppointmentToSharedStores: " + e.getMessage());
        }
        Admin.HospitalAdmin.registerAppointment(apptId, patientName, doctorName, date + " " + time);
        // Sync to HospitalSystem shared tracking (cross-system connectivity)
        String dept = ReceptionistDataStore.getDeptForDoctor(doctorName);
        String effectiveStatus = status.isEmpty() ? getStatusForAppt(apptId) : status;
        HospitalSystem.addSharedAppointment(apptId, patientName, doctorName, dept, date, time, effectiveStatus);
        // If department is Emergency Medicine, also sync as shared emergency case
        if (dept.equalsIgnoreCase("Emergency Medicine")) {
            System.out.println("[DEBUG] syncAppointmentToSharedStores: Creating shared emergency for " + patientName + " with dept=" + dept + " doctor=" + doctorName + " date=" + date + " time=" + time);
            HospitalSystem.addSharedEmergency(
                apptId, patientName, "Emergency registration", "Medium", doctorName, dept, date, time);
        } else {
            System.out.println("[DEBUG] syncAppointmentToSharedStores: dept=" + dept + " for doctor=" + doctorName + " — NOT Emergency Medicine");
        }
    }

    /** Derive department from doctor name. */
    public static String getDeptForDoctor(String doctorName) {
        if (doctorName == null || doctorName.equals("Unassigned")) return "General";
        String dept = DOC_DEPT.get(doctorName);
        if (dept != null) return dept;
        // Fallback: try dynamic lookup
        return ReceptionistDataStore.getDynamicDocDept(doctorName);
    }

    /** Look up the current status of a given appointment ID from the apptModel. */
    public static String getStatusForAppt(String apptId) {
        String s = appointmentStatusMap.get(apptId);
        return s != null ? s : "Booked";
    }

    /**
     * Admin doctor reassignment: updates the appointment row in apptModel and
     * syncs to all shared stores so the change appears in the receptionist
     * appointment panel and the admin dashboard.
     *
     * @param apptIdOrPatient  apptId (e.g. "#A-0041") to match by ID; if empty,
     *                         falls back to matching by patient+date+time
     * @param patientName  patient name (for fallback matching)
     * @param date         display-format date
     * @param time         time string
     * @param newDoctor    new doctor name (with "Dr. " prefix)
     * @param newDept      new department name
     */
    public static void adminReassignDoctor(String apptIdOrPatient, String patientName,
                                           String date, String time,
                                           String newDoctor, String newDept) {
        // 1. Find the row in apptModel
        int row = -1;
        // Try by apptId first
        if (apptIdOrPatient != null && !apptIdOrPatient.isEmpty()) {
            for (int r = 0; r < apptModel.getRowCount(); r++) {
                if (apptIdOrPatient.equals(apptModel.getValueAt(r, 0).toString())) {
                    row = r; break;
                }
            }
        }
        // Fallback to patient+date+time
        if (row == -1 && patientName != null) {
            for (int r = 0; r < apptModel.getRowCount(); r++) {
                String rPat = apptModel.getValueAt(r, 1).toString().trim();
                String rDate = ReceptionistDataStore.normaliseDate(apptModel.getValueAt(r, 2).toString());
                String rTime = apptModel.getValueAt(r, 3).toString().trim();
                if (rPat.equalsIgnoreCase(patientName) && rDate.equals(date) && rTime.equals(time)) {
                    row = r; break;
                }
            }
        }
        if (row == -1) {
            System.out.println("[ReceptionistDataStore] adminReassignDoctor: no matching row for patient=" + patientName);
            return;
        }

        // 2. Read old values before overwriting
        String oldDoctor  = apptModel.getValueAt(row, 4).toString().trim();
        String oldDept    = apptModel.getValueAt(row, 5).toString().trim();
        String oldDate    = apptModel.getValueAt(row, 2).toString().trim();
        String oldTime    = apptModel.getValueAt(row, 3).toString().trim();
        String apptId     = apptModel.getValueAt(row, 0).toString().trim();
        String lifecycleStatus = appointmentStatusMap.getOrDefault(apptId, "Booked");

        // 3. Free the old doctor's slot (if different from new)
        if (!oldDoctor.equals(newDoctor) && !oldDoctor.equals("Unassigned")) {
            String rawOldDoc = stripDr(oldDoctor);
            HospitalSystem.clearSlot(rawOldDoc, toYmd(oldDate), oldTime);
            try {
                HospitalSystem.cancelFromDoctorStore(patientName, oldDate, oldTime);
            } catch (Exception e) {
                System.out.println("[ReceptionistDataStore] adminReassignDoctor cancel old: " + e.getMessage());
            }
        }

        // 4. Update apptModel columns
        apptModel.setValueAt(newDoctor, row, 4);
        apptModel.setValueAt(newDept,   row, 5);

        // 5. Book the new doctor's slot
        if (!newDoctor.equals("Unassigned")) {
            String rawNewDoc = stripDr(newDoctor);
            HospitalSystem.markSlotBooked(rawNewDoc, toYmd(date), time);
        }

        // 6. Sync to all shared stores (replaces old entry)
        String effectiveStatus = lifecycleStatus.isEmpty() ? "Booked" : lifecycleStatus;
        HospitalSystem.addSharedAppointment(apptId, patientName, newDoctor, newDept, date, time, effectiveStatus);

        // 7. Sync to doctor's DataStore (for appointment model)
        try {
            String rawNewDoc = stripDr(newDoctor);
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
            ds.setDoctorDisplayName(rawNewDoc);
            ds.addAppointment(patientName, time, date, rawNewDoc);
            ds.ensureInOverall(patientName, "--", "--", "--", "--");
        } catch (Exception e) {
            System.out.println("[ReceptionistDataStore] adminReassignDoctor doctorDataStore: " + e.getMessage());
        }

        // 8. Sync to Admin.HospitalAdmin's registered appointments
        Admin.HospitalAdmin.registerAppointment(apptId, patientName, newDoctor, date + " " + time);

        // 9. Handle Emergency Medicine emergency cases
        if (newDept.equalsIgnoreCase("Emergency Medicine")) {
            HospitalSystem.addSharedEmergency(
                apptId, patientName, "Emergency registration", "Medium",
                newDoctor, newDept, date, time);
        } else {
            HospitalSystem.removeSharedEmergencyByPatient(patientName, date, time);
        }

        System.out.println("[ReceptionistDataStore] adminReassignDoctor: apptId=" + apptId
            + " patient=" + patientName + " oldDoc=" + oldDoctor + " newDoc=" + newDoctor
            + " oldDept=" + oldDept + " newDept=" + newDept);
    }

    /** Syncs a cancelled appointment to Doctor.doctorDataStore. */
    public static void cancelInSharedStores(String patientName, String date, String time) {
        try {
            String rawDoc = ""; // will be determined by cancelFromDoctorStore
            HospitalSystem.cancelFromDoctorStore(patientName, date, time);
        } catch (Exception e) {
            System.out.println("[Receptionist] cancelInSharedStores: " + e.getMessage());
        }
    }

    /** Registers a patient in Admin.HospitalAdmin's shared list. */
    public static void registerPatientInAdmin(String name, String phone, String pid) {
        registerPatientInAdmin(name, phone, pid, "General");
    }
    /** Registers a patient in Admin.HospitalAdmin's shared list with department as condition. */
    public static void registerPatientInAdmin(String name, String phone, String pid, String dept) {
        Admin.HospitalAdmin.registerPatient(pid, name, dept, "Unassigned");
    }

    /** Checks if a patient name/phone already exists in any shared store. */
    public static boolean isDuplicatePatient(String name, String phone) {
        for (int r = 0; r < apptModel.getRowCount(); r++) {
            Object n = apptModel.getValueAt(r, 1);
            if (n != null && n.toString().equalsIgnoreCase(name)) return true;
        }
        for (int r = 0; r < walkModel.getRowCount(); r++) {
            Object n = walkModel.getValueAt(r, 1);
            if (n != null && n.toString().equalsIgnoreCase(name)) return true;
        }
        for (Admin.HospitalAdmin.Patient p : Admin.HospitalAdmin.sharedPatientList) {
            if (p.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  DYNAMIC ROSTER INTEGRATION  —  bridges DoctorRosterStore to
    //  existing receptionist code. Falls back to hardcoded data if
    //  DoctorRosterStore is empty.
    // ══════════════════════════════════════════════════════════════════════

    /** Listeners notified when the roster is refreshed. */
    private static final List<Runnable> rosterListeners = new ArrayList<>();
    public static void addRosterListener(Runnable r) { rosterListeners.add(r); }

    private static boolean rosterChecked = false;
    private static boolean rosterHasData = false;

    /** True if DoctorRosterStore has active doctors. */
    public static boolean rosterHasActiveDoctors() {
        if (!rosterChecked) {
            rosterHasData = !DoctorRosterStore.getActiveDoctors().isEmpty();
            rosterChecked = true;
        }
        return rosterHasData;
    }

    /** Invalidate the cached flag so next call re-queries the roster. */
    public static void invalidateRosterCache() { rosterChecked = false; }

    /** Departments from the roster, falling back to hardcoded if empty. */
    public static String[] getDynamicDepartments() {
        if (!rosterHasActiveDoctors()) return DEPARTMENTS;
        java.util.Set<String> depts = DoctorRosterStore.getDepartments();
        return depts.isEmpty() ? DEPARTMENTS : depts.toArray(new String[0]);
    }

    /** Active doctors in a given department from roster only, never falls back to hardcoded once roster has data. */
    public static List<String> getDynamicDoctorsByDept(String dept) {
        if (!rosterHasActiveDoctors()) return getDoctorsByDept(dept);
        List<String> result = new ArrayList<>();
        for (Object[] doc : DoctorRosterStore.getActiveDoctors()) {
            if (dept.equals(doc[2])) result.add((String) doc[1]);
        }
        return result;
    }

    /** All active doctor names + "Unassigned", falling back to hardcoded list. */
    public static String[] getDynamicDoctorList() {
        if (!rosterHasActiveDoctors()) return DOCTOR_LIST;
        List<String> result = new ArrayList<>();
        for (Object[] doc : DoctorRosterStore.getActiveDoctors()) {
            result.add((String) doc[1]);
        }
        result.add("Unassigned");
        return result.toArray(new String[0]);
    }

    /** Maps a doctor name to their department from the roster, falling back to hardcoded map. */
    public static String getDynamicDocDept(String doctorName) {
        if (!rosterHasActiveDoctors()) return DOC_DEPT.getOrDefault(doctorName, "Cardiology");
        String id = DoctorRosterStore.getDoctorIdByName(doctorName);
        if (!id.isEmpty()) {
            for (Object[] doc : DoctorRosterStore.getActiveDoctors()) {
                if (id.equals(doc[0])) return (String) doc[2];
            }
        }
        return DOC_DEPT.getOrDefault(doctorName, "Cardiology");
    }

    /**
     * Returns available hourly slot strings (e.g. "09:00 AM", "10:00 AM", "02:00 PM")
     * for a doctor based on their shift ranges in DoctorRosterStore.
     * Same format as the patient BookAppointmentPanel uses.
     * Falls back to hardcoded minimum-hourly representation if the roster has no data.
     */
    public static String[] getAvailableSlotsForDoctor(String doctorName) {
        if (rosterHasActiveDoctors()) {
            List<String> rosterSlots = DoctorRosterStore.getExpandedTimeSlotsByName(doctorName);
            if (!rosterSlots.isEmpty()) {
                return rosterSlots.toArray(new String[0]);
            }
        }
        // Fallback: keep only hourly (:00) slots from hardcoded shift windows,
        // then convert to 12-hour AM/PM format
        String[] fallback = getSlotsForDoctor(doctorName);
        Set<String> hourly = new LinkedHashSet<>();
        SimpleDateFormat sdf24 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat sdf12 = new SimpleDateFormat("hh:mm a", Locale.ENGLISH);
        for (String s : fallback) {
            if (!s.endsWith(":00") && !s.endsWith(":30")) continue; // only hourly and half-hourly slots
            try { hourly.add(sdf12.format(sdf24.parse(s))); } catch (Exception ignored) {}
        }
        return hourly.isEmpty() ? new String[0] : hourly.toArray(new String[0]);
    }

    /** Human-readable shift label from the roster, falling back to hardcoded. */
    public static String getShiftLabelDynamic(String doctorName) {
        if (rosterHasActiveDoctors()) {
            List<String> ranges = DoctorRosterStore.getExpandedTimeSlotsByName(doctorName);
            if (!ranges.isEmpty()) {
                return String.join(", ", ranges);
            }
        }
        return getShiftLabel(doctorName);
    }

    /**
     * True if a slot is within the available range for a doctor (from roster).
     * Falls back to isSlotDisabled if roster has no data.
     */
    public static boolean isSlotDisabledDynamic(String slot, String doctorName) {
        if (!rosterHasActiveDoctors()) return isSlotDisabled(slot, doctorName);
        String[] avail = getAvailableSlotsForDoctor(doctorName);
        for (String a : avail) if (a.equals(slot)) return false;
        return true;
    }

    // Register the admin reassignment callback so Admin/HospitalAdmin can trigger it via HospitalSystem
    static {
        HospitalSystem.setAdminReassignHandler(
            (apptId, patientName, date, time, newDoctor, newDept) -> {
                adminReassignDoctor(apptId, patientName, date, time, newDoctor, newDept);
            }
        );
    }

    // Register the shift-change time adjustment handler (called from Admin/HospitalAdmin)
    static {
        HospitalSystem.setTimeAdjustHandler(
            (doctorName, oldTime, newTime, date, apptId, patientName) -> {
                // Update apptModel
                for (int r = 0; r < apptModel.getRowCount(); r++) {
                    String id = apptModel.getValueAt(r, 0).toString().trim();
                    String pat = apptModel.getValueAt(r, 1).toString().trim();
                    if (id.equals(apptId) || pat.equalsIgnoreCase(patientName)) {
                        String rDate = normaliseDate(apptModel.getValueAt(r, 2).toString());
                        String rTime = apptModel.getValueAt(r, 3).toString().trim();
                        if (rDate.equals(date) && rTime.equals(oldTime)) {
                            apptModel.setValueAt(newTime, r, 3);
                            break;
                        }
                    }
                }
                // Also update doctorDataStore (add new appointment)
                try {
                    String dateYmd = toYmd(date);
                    Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
                    ds.setDoctorDisplayName(doctorName);
                    ds.addAppointment(patientName, newTime, dateYmd, doctorName);
                } catch (Exception e) {
                    System.out.println("[ReceptionistDataStore] timeAdjust handler: " + e.getMessage());
                }
            }
        );
    }

    // Register a listener on DoctorRosterStore to invalidate our cache and notify panels.
    static {
        DoctorRosterStore.addListener(() -> {
            invalidateRosterCache();
            rosterHasActiveDoctors(); // re-evaluate
            List<Runnable> copy;
            synchronized (rosterListeners) { copy = new ArrayList<>(rosterListeners); }
            for (Runnable r : copy) {
                try { r.run(); } catch (Exception e) { e.printStackTrace(); }
            }
        });
    }
}