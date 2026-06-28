package Doctor;

import javax.swing.table.DefaultTableModel;
import java.util.*;
import System.DoctorRosterStore;
import System.HospitalSystem;

/**
 * Singleton DataStore — single source of truth for all panels.
 */
public class doctorDataStore {

    private static doctorDataStore instance;
    public static doctorDataStore get() {
        if (instance == null) instance = new doctorDataStore();
        return instance;
    }

    // ── Change listeners ───────────────────────────────────────────────────
    public interface DataListener { void onDataChanged(); }
    private final List<DataListener> listeners = new ArrayList<>();
    /** Bridges appointment status from doctor actions back to patient side */
    public final java.util.Map<String, String> patientAppointmentStatus = new java.util.LinkedHashMap<>();
    private String doctorDisplayName = "Doctor";
    public void setDoctorDisplayName(String n) { this.doctorDisplayName = n; }
    public String getDoctorDisplayName() { return doctorDisplayName; }
    public void addListener(DataListener l) { listeners.add(l); }
    public void notifyListeners() {
        Runnable notify = () -> listeners.forEach(DataListener::onDataChanged);
        if (javax.swing.SwingUtilities.isEventDispatchThread()) notify.run();
        else javax.swing.SwingUtilities.invokeLater(notify);
    }

    /** Tracks which appointments have been accepted (to hide Accept/Reject buttons) */
    private final java.util.Set<String> acceptedAppointments = new java.util.HashSet<>();
    public boolean isAccepted(String patientName, String date, String time) {
        return acceptedAppointments.contains(patientName + "|" + date + "|" + time);
    }
    public void markAccepted(String patientName, String date, String time) {
        acceptedAppointments.add(patientName + "|" + date + "|" + time);
        updatePatientAppointmentStatus(patientName, date, time, "Accepted");
        HospitalSystem.updateSharedAppointmentStatusByPatient(patientName, time);
    }
    public String getAppointmentStatus(String patientName, String date, String timeSlot) {
        return patientAppointmentStatus.get(patientName + "|" + date + "|" + timeSlot + "|" + doctorDisplayName);
    }

    // ══════════════════════════════════════════════════════════════════════
    //  APPOINTMENTS
    // ══════════════════════════════════════════════════════════════════════
    private final DefaultTableModel appointmentModel =
            new DefaultTableModel(new String[]{"Patient", "Time", "Date", "Doctor"}, 0);
    public DefaultTableModel getAppointmentModel() { return appointmentModel; }
    public void addAppointment(String patient, String time, String date) {
        addAppointment(patient, time, date, doctorDisplayName);
    }
    public void addAppointment(String patient, String time, String date, String doctor) {
        // Normalize doctor name: strip "Dr. " prefix so it always matches
        // doctorDisplayName (which is stored without the prefix).
        String normalizedDoctor = (doctor != null && doctor.startsWith("Dr. "))
                ? doctor.substring(4) : doctor;
        // Remove stale completed/cancelled rows for the same slot so re-booking works
        for (int i = appointmentModel.getRowCount() - 1; i >= 0; i--) {
            Object p = appointmentModel.getValueAt(i, 0);
            Object t = appointmentModel.getValueAt(i, 1);
            Object d = appointmentModel.getValueAt(i, 2);
            Object dc = appointmentModel.getValueAt(i, 3);
            if (p != null && t != null && d != null && dc != null
                    && p.toString().equalsIgnoreCase(patient)
                    && t.toString().equalsIgnoreCase(time)
                    && d.toString().equalsIgnoreCase(date)
                    && dc.toString().equalsIgnoreCase(normalizedDoctor)) {
                String sk = patient + "|" + date + "|" + time + "|" + normalizedDoctor;
                String st = patientAppointmentStatus.get(sk);
                if ("Completed".equals(st) || "Cancelled".equals(st)) {
                    appointmentModel.removeRow(i);
                    patientAppointmentStatus.remove(sk);
                } else {
                    return; // active duplicate — skip
                }
            }
        }
        appointmentModel.addRow(new Object[]{patient, time, date, normalizedDoctor});
        notifyListeners();
    }

    private final DefaultTableModel requestModel =
            new DefaultTableModel(new String[]{"Patient Name", "Requested Service"}, 0);
    public DefaultTableModel getRequestModel() { return requestModel; }
    public void addRequest(String patient, String service) {
        requestModel.addRow(new Object[]{patient, service});
        notifyListeners();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  EMERGENCY
    // ══════════════════════════════════════════════════════════════════════
    private final DefaultTableModel emergencyModel =
            new DefaultTableModel(
                    new String[]{"Priority","Patient Name","Age","Condition","Time Arrived","Ward/Bed","Status"}, 0);
    public DefaultTableModel getEmergencyModel() { return emergencyModel; }
    public void addEmergencyPatient(Object[] row) {
        emergencyModel.addRow(row);
        // row: [Priority, Patient Name, Age, Condition, Time Arrived, Ward/Bed, Status]
        historyEmergencyModel.addRow(new Object[]{
            row[1], row[2], row[3], row[0], row[4],
            "Pending", "--", "--", "--", "--"
        });
        // Sync to admin's shared emergency list
        try {
            String caseId = "ER-" + System.currentTimeMillis();
            String patientName = row[1] != null ? row[1].toString() : "";
            String complaint   = row[3] != null ? row[3].toString() : "";
            String severity    = row[0] != null ? row[0].toString() : "P3";
            String timeArr     = row[4] != null ? row[4].toString() : "";
            String today       = new java.text.SimpleDateFormat("yyyy-MM-dd")
                                    .format(new java.util.Date());
            String dept = DoctorRosterStore.getDoctorDepartmentByName(doctorDisplayName);
            if (dept == null) dept = "General";
            HospitalSystem.addSharedEmergency(caseId, patientName, complaint,
                                              severity, doctorDisplayName, dept,
                                              today, timeArr);
        } catch (Exception e) {
            System.out.println("[doctorDataStore] addEmergencyPatient shared sync: " + e.getMessage());
        }
        notifyListeners();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  PATIENT HISTORY — Appointments
    //  Columns: Name, Age, Contact, Type, Date, Time, Notes, Diagnosis, Medications, Allergies, Doctor
    // ══════════════════════════════════════════════════════════════════════
    private final DefaultTableModel historyAppointmentModel =
            new DefaultTableModel(
                    new String[]{"Name","Age","Contact","Type","Date","Time","Notes",
                                 "Diagnosis","Medications","Allergies","Doctor"}, 0);
    public DefaultTableModel getHistoryAppointmentModel() { return historyAppointmentModel; }

    // ══════════════════════════════════════════════════════════════════════
    //  PATIENT HISTORY — Emergency
    //  Columns: Name, Age, Condition, Priority, Time, Outcome, Treatment, Medications, Allergies, BP
    // ══════════════════════════════════════════════════════════════════════
    private final DefaultTableModel historyEmergencyModel =
            new DefaultTableModel(
                    new String[]{"Name","Age","Condition","Priority","Time","Outcome",
                                 "Treatment","Medications","Allergies","Blood Pressure"}, 0);
    public DefaultTableModel getHistoryEmergencyModel() { return historyEmergencyModel; }

    // ══════════════════════════════════════════════════════════════════════
    //  ADMITTED PATIENTS  Columns: Name, Age, Gender, Contact, Blood, Admit Date, Ward, Status
    // ══════════════════════════════════════════════════════════════════════
    private final DefaultTableModel admittedPatientModel =
            new DefaultTableModel(
                    new String[]{"Name","Age","Gender","Contact","Blood Group","Admit Date","Ward","Status"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
    public DefaultTableModel getAdmittedPatientModel() { return admittedPatientModel; }

    /** Set of admitted patient names (lower-case) for quick lookup */
    private final java.util.Set<String> admittedSet = new java.util.HashSet<>();

    public boolean isAdmitted(String name) {
        return name != null && admittedSet.contains(name.toLowerCase());
    }

    /**
     * Admits a patient: adds a row to the admitted model and records their name.
     * Pulls demographic data from the overall patient model when available.
     */
    public void admitPatient(String name) {
        if (name == null || name.isEmpty() || isAdmitted(name)) return;
        admittedSet.add(name.toLowerCase());

        // Pull existing demographics if available
        String age = "--", gender = "--", contact = "--", blood = "--";
        for (int i = 0; i < overallPatientModel.getRowCount(); i++) {
            if (name.equals(str(overallPatientModel.getValueAt(i, 0)))) {
                age     = str(overallPatientModel.getValueAt(i, 1));
                gender  = str(overallPatientModel.getValueAt(i, 2));
                contact = str(overallPatientModel.getValueAt(i, 3));
                blood   = str(overallPatientModel.getValueAt(i, 4));
                break;
            }
        }
        String admitDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        admittedPatientModel.addRow(new Object[]{name, age, gender, contact, blood, admitDate, "General", "Admitted"});
        notifyListeners();
    }

    /**
     * Discharges a patient: removes their row from the admitted model,
     * the live emergency queue, and the emergency history.
     */
    public void dischargePatient(String name) {
        if (name == null || name.isEmpty()) return;
        admittedSet.remove(name.toLowerCase());

        // Collect times before removing (for shared emergency sync)
        java.util.List<String> times = new java.util.ArrayList<>();
        for (int i = emergencyModel.getRowCount() - 1; i >= 0; i--) {
            if (name.equals(str(emergencyModel.getValueAt(i, 1)))) {
                String t = str(emergencyModel.getValueAt(i, 4));
                if (!t.isEmpty() && !t.equals("—")) times.add(t);
            }
        }

        // Remove from admitted model
        for (int i = admittedPatientModel.getRowCount() - 1; i >= 0; i--) {
            if (name.equals(str(admittedPatientModel.getValueAt(i, 0)))) {
                admittedPatientModel.removeRow(i);
                break;
            }
        }

        // Remove from live emergency queue (col 1 = Patient Name)
        for (int i = emergencyModel.getRowCount() - 1; i >= 0; i--) {
            if (name.equals(str(emergencyModel.getValueAt(i, 1)))) {
                emergencyModel.removeRow(i);
            }
        }

        // Remove from emergency history (col 0 = Name)
        for (int i = historyEmergencyModel.getRowCount() - 1; i >= 0; i--) {
            if (name.equals(str(historyEmergencyModel.getValueAt(i, 0)))) {
                historyEmergencyModel.removeRow(i);
            }
        }

        // Sync removal to admin's shared emergency list
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd")
                          .format(new java.util.Date());
        for (String t : times) {
            HospitalSystem.removeSharedEmergencyByPatient(name, today, t);
        }

        notifyListeners();
    }

    /** Returns true if the patient is currently in the live emergency queue. */
    public boolean isInEmergency(String name) {
        if (name == null) return false;
        for (int i = 0; i < emergencyModel.getRowCount(); i++) {
            if (name.equals(str(emergencyModel.getValueAt(i, 1)))) return true;
        }
        return false;
    }

    // ══════════════════════════════════════════════════════════════════════
    //  OVERALL PATIENTS  Columns: Name, Age, Gender, Contact, Blood, Visits
    // ══════════════════════════════════════════════════════════════════════
    private final DefaultTableModel overallPatientModel =
            new DefaultTableModel(
                    new String[]{"Name","Age","Gender","Contact","Blood","Visits"}, 0);
    public DefaultTableModel getOverallPatientModel() { return overallPatientModel; }

    // ══════════════════════════════════════════════════════════════════════
    //  PRESCRIPTIONS  — keyed by patient name
    //  Columns: Patient, Date, Medication, Dosage, Frequency, Duration, Instructions, Doctor Notes
    // ══════════════════════════════════════════════════════════════════════
    private final DefaultTableModel prescriptionModel =
            new DefaultTableModel(
                    new String[]{"Patient","Date","Medication","Dosage","Frequency","Duration","Instructions","Doctor Notes"}, 0);
    public DefaultTableModel getPrescriptionModel() { return prescriptionModel; }

    public void addPrescription(String patient, String date, String medication,
                                 String dosage, String frequency, String duration,
                                 String instructions, String doctorNotes) {
        prescriptionModel.addRow(new Object[]{patient, date, medication, dosage, frequency, duration, instructions, doctorNotes});
        notifyListeners();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  MEDICAL HISTORY MAP  — rich per-patient data
    // ══════════════════════════════════════════════════════════════════════
    /** Key = patient name (lower case), Value = map of field→value */
    private final Map<String, Map<String, String>> medicalHistory = new LinkedHashMap<>();

    public Map<String, String> getMedicalHistory(String name) {
        return medicalHistory.getOrDefault(name.toLowerCase(), new LinkedHashMap<>());
    }

    public void updateMedicalHistory(String name, String field, String value) {
        medicalHistory.computeIfAbsent(name.toLowerCase(), k -> new LinkedHashMap<>()).put(field, value);
        notifyListeners();
    }

    public void setMedicalHistoryAll(String name, Map<String, String> data) {
        medicalHistory.put(name.toLowerCase(), new LinkedHashMap<>(data));
        notifyListeners();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  SEED DATA
    // ══════════════════════════════════════════════════════════════════════
    public void seedData() {
        // No seed data — all patients are added at runtime only
        /*
        Object[][] appts = {
            {"John Mathew",   "9:30 PM",  "10 June 2026", 35, "Male",   "0300-1111111", "O+",  "Follow-up",    "Routine check",   "Hypertension","Metoprolol 50mg","Penicillin"},
            {"Sarah Ali",     "11:00 AM", "10 June 2026", 28, "Female", "0300-2222222", "A+",  "Consultation", "Recurring fever", "Typhoid","Paracetamol 500mg","None"},
            {"Aisha Malik",   "08:30 AM", "10 June 2026",  42, "Female", "0300-3333333", "B+",  "Follow-up",    "BP check",        "Hypertension","Amlodipine 5mg","Aspirin"},
            {"Tariq Hussain", "09:15 AM", "10 June 2026",  58, "Male",   "0300-4444444", "AB+", "Consultation", "Chest pain",      "Angina","Nitroglycerin","None"},
            {"Zara Sheikh",   "10:00 AM", "10 June 2026",  35, "Female", "0300-5555555", "O-",  "Check-up",     "ECG",             "Arrhythmia","Propranolol 40mg","Sulfa drugs"},
            {"Sara Noor",     "11:00 AM", "10 June 2026",  29, "Female", "0300-6666666", "A-",  "New Patient",  "ECG review",      "Palpitations","Beta-blockers","None"},
        };
        for (Object[] a : appts) {
            appointmentModel.addRow(new Object[]{a[0], a[1], a[2]});
            historyAppointmentModel.addRow(new Object[]{
                a[0], a[3], a[5], a[7], a[2], a[1], a[8], a[9], a[10], a[11]
            });
            ensureInOverall((String)a[0], a[3], (String)a[4], (String)a[5], (String)a[6]);
            // seed medical history
            Map<String, String> mh = new LinkedHashMap<>();
            mh.put("Diagnosis",   (String)a[9]);
            mh.put("Medications", (String)a[10]);
            mh.put("Allergies",   (String)a[11]);
            mh.put("Height",      "170 cm");
            mh.put("Weight",      "70 kg");
            mh.put("BP",          "120/80");
            mh.put("ChronicConditions", (String)a[9]);
            mh.put("FamilyHistory","Hypertension");
            mh.put("Surgeries",   "None");
            mh.put("Vaccinations","Hepatitis B, Tetanus");
            medicalHistory.put(((String)a[0]).toLowerCase(), mh);
        }

        requestModel.addRow(new Object[]{"Michael Scott", "General Checkup"});
        requestModel.addRow(new Object[]{"Pam Beezly",    "Dental Cleaning"});

        Object[][] emg = {
            {"P1","Omar Farooq",   45,"Cardiac Arrest",         "14:30","ICU - Bed 5", "CRITICAL",     "Stabilized", "Male",   "0300-7777777","O+",  "CPR + Defibrillation", "Epinephrine 1mg", "None",    "90/60"},
            {"P1","Fatima Iqbal",  38,"Respiratory Distress",   "14:45","ICU - Bed 7", "CRITICAL",     "Admitted",   "Female", "0300-8888888","A-",  "Oxygen therapy",        "Salbutamol",      "Aspirin", "100/70"},
            {"P2","Hassan Malik",  52,"Chest Pain",             "15:10","ER - 2",      "HIGH PRIORITY","Observation","Male",   "0300-9999999","B+",  "ECG + Troponin",        "Nitroglycerin",   "None",    "150/95"},
            {"P2","Zainab Ali",    28,"Severe Bleeding",        "15:25","ER - 3",      "HIGH PRIORITY","Surgery",    "Female", "0300-1010101","AB+", "Wound suturing",        "Morphine 4mg",    "None",    "80/50"},
            {"P2","Muhammad Usman",60,"Stroke Symptoms",        "15:40","ER - 4",      "HIGH PRIORITY","Pending",    "Male",   "0300-1020304","O-",  "CT Scan ordered",       "Aspirin 325mg",   "Penicillin","170/110"},
            {"P3","Ayesha Khan",   35,"Allergic Reaction",      "16:00","ER - 1",      "MODERATE",     "Discharged", "Female", "0300-1030405","A+",  "Antihistamine IV",      "Diphenhydramine", "None",    "110/70"},
            {"P3","Ali Raza",      42,"Fracture",               "16:15","Ortho - 2",   "MODERATE",     "Discharged", "Male",   "0300-1040506","B-",  "X-Ray + Casting",       "Ibuprofen 400mg", "None",    "120/80"},
            {"P3","Hina Butt",     29,"Acute Migraine",         "16:30","ER - 5",      "MODERATE",     "Discharged", "Female", "0300-1050607","O+",  "Dark room + IV fluids", "Sumatriptan",     "None",    "115/75"},
            {"P3","Bilal Qureshi", 55,"Hypertensive Crisis",    "16:45","ER - 6",      "MODERATE",     "Observation","Male",   "0300-1060708","AB-", "IV antihypertensives",  "Labetalol 20mg",  "Sulfa",   "200/120"},
            {"P3","Nadia Ahmed",   31,"Pregnancy Complication", "17:00","OB/GYN - 1",  "MODERATE",     "Admitted",   "Female", "0300-1070809","A+",  "Fetal monitoring",      "Magnesium Sulfate","None",   "130/85"},
        };
        for (Object[] e : emg) {
            emergencyModel.addRow(new Object[]{e[0],e[1],e[2],e[3],e[4],e[5],e[6]});
            historyEmergencyModel.addRow(new Object[]{e[1],e[2],e[3],e[0],e[4],e[7],e[11],e[12],e[13],e[14]});
            ensureInOverall((String)e[1], e[2], (String)e[8], (String)e[9], (String)e[10]);
            Map<String, String> mh = new LinkedHashMap<>();
            mh.put("Diagnosis",   (String)e[3]);
            mh.put("Medications", (String)e[12]);
            mh.put("Allergies",   (String)e[13]);
            mh.put("BP",          (String)e[14]);
            mh.put("Treatment",   (String)e[11]);
            mh.put("Height",      "165 cm");
            mh.put("Weight",      "68 kg");
            mh.put("ChronicConditions", (String)e[3]);
            mh.put("FamilyHistory","Unknown");
            mh.put("Surgeries",   "None");
            mh.put("Vaccinations","Unknown");
            medicalHistory.put(((String)e[1]).toLowerCase(), mh);
        }
        */
    }

    // ── Convenience counters ────────────────────────────────────────────────
    public int getTotalPatients()    { return overallPatientModel.getRowCount(); }
    public int getTotalAppointments(){ return appointmentModel.getRowCount(); }
    public int getEmergencyCount()   { return emergencyModel.getRowCount(); }

    /**
     * Counts how many appointments fall on a valid schedule slot (08:00-06:00 PM, excl. lunch).
     * This is the "Booked Today" number shown in both Overview and Schedule panels.
     */
    private static final String[] SCHEDULE_SLOTS_FALLBACK = {
        "08:00 AM","08:30 AM","09:00 AM","09:30 AM","10:00 AM","10:30 AM",
        "11:00 AM","11:30 AM","12:00 PM","12:30 PM",
        "01:00 PM","01:30 PM","02:00 PM","02:30 PM","03:00 PM","03:30 PM",
        "04:00 PM","04:30 PM","05:00 PM","05:30 PM","06:00 PM"
    };
    private String[] getScheduleSlots() {
        java.util.List<String> rosterSlots = DoctorRosterStore.getExpandedTimeSlotsByName(doctorDisplayName);
        if (!rosterSlots.isEmpty()) return rosterSlots.toArray(new String[0]);
        return SCHEDULE_SLOTS_FALLBACK;
    }

    public int getBookedSlotCount() {
        return getSlotAppointments().size();
    }

    public int getFreeSlotCount() {
        return Math.max(0, getScheduleSlots().length - getBookedSlotCount());
    }

    /**
     * Returns only today's appointments whose time maps to a valid schedule slot.
     * Each element is Object[]{patient, time, date}.
     */
    public java.util.List<Object[]> getSlotAppointments() {
        String[] validSlotsArray = getScheduleSlots();
        java.util.Set<String> validSlots = new java.util.LinkedHashSet<>(
            java.util.Arrays.asList(validSlotsArray));
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd")
                           .format(new java.util.Date());
        java.util.Set<String> seen = new java.util.LinkedHashSet<>();
        java.util.List<Object[]> result = new java.util.ArrayList<>();
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {
            String date = appointmentModel.getValueAt(i, 2) == null ? ""
                        : appointmentModel.getValueAt(i, 2).toString();
            if (!isSameDay(date, today)) continue;
            String doc = appointmentModel.getValueAt(i, 3) == null ? ""
                       : appointmentModel.getValueAt(i, 3).toString();
            if (!doc.equals(doctorDisplayName)) continue;
            String patient = appointmentModel.getValueAt(i, 0) == null ? ""
                           : appointmentModel.getValueAt(i, 0).toString();
            String raw  = appointmentModel.getValueAt(i, 1) == null ? ""
                        : appointmentModel.getValueAt(i, 1).toString();
            String norm = normalizeSlotTime(raw);
            // Skip cancelled or completed appointments
            String statusKey = patient + "|" + date + "|" + raw + "|" + doctorDisplayName;
            String aptStatus = patientAppointmentStatus.get(statusKey);
            if ("Cancelled".equals(aptStatus) || "Completed".equals(aptStatus)) continue;
            if (validSlots.contains(norm) && !seen.contains(norm)) {
                seen.add(norm);
                result.add(new Object[]{
                    appointmentModel.getValueAt(i, 0),
                    appointmentModel.getValueAt(i, 1),
                    appointmentModel.getValueAt(i, 2)
                });
            }
        }
        return result;
    }

    /** Matches dates stored as "yyyy-MM-dd", "MMM dd yyyy", "MMM d yyyy". */
    private boolean isSameDay(String stored, String todayYmd) {
        if (stored == null || stored.isEmpty()) return false;
        // Direct match e.g. "2026-05-29"
        if (stored.equals(todayYmd)) return true;
        // Try parsing common formats and compare
        String[] formats = {"yyyy-MM-dd", "MMM dd yyyy", "MMM d yyyy",
                            "dd MMM yyyy", "MM/dd/yyyy"};
        for (String fmt : formats) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(fmt, java.util.Locale.ENGLISH);
                sdf.setLenient(false);
                String parsed = new java.text.SimpleDateFormat("yyyy-MM-dd")
                                    .format(sdf.parse(stored));
                if (parsed.equals(todayYmd)) return true;
            } catch (Exception ignored) {}
        }
        return false;
    }

    private String normalizeSlotTime(String raw) {
        if (raw == null || raw.isEmpty()) return "";
        String[] slots = getScheduleSlots();
        for (String s : slots) if (s.equals(raw)) return s;
        if (raw.length() >= 5)
            for (String s : slots)
                if (s.startsWith(raw.substring(0, 5))) return s;
        for (String s : slots)
            if (s.contains(raw.replace("AM","").replace("PM","").trim())) return s;
        return raw;
    }

    public String getNextPatientName() {
        java.util.List<Object[]> slots = getSlotAppointments();
        return slots.isEmpty() ? "--" : slots.get(0)[0].toString();
    }
    public String getNextPatientTime() {
        java.util.List<Object[]> slots = getSlotAppointments();
        return slots.isEmpty() ? "--" : slots.get(0)[1].toString();
    }

    public String[] getPatientDetails(String name) {
        for (int i = 0; i < overallPatientModel.getRowCount(); i++) {
            if (name.equals(overallPatientModel.getValueAt(i, 0))) {
                return new String[]{
                    str(overallPatientModel.getValueAt(i, 0)),
                    str(overallPatientModel.getValueAt(i, 1)),
                    str(overallPatientModel.getValueAt(i, 2)),
                    str(overallPatientModel.getValueAt(i, 3)),
                    str(overallPatientModel.getValueAt(i, 4)),
                    str(overallPatientModel.getValueAt(i, 5))
                };
            }
        }
        return new String[]{name, "--", "--", "--", "--", "--"};
    }

    /**
     * Returns the unique patient names whose appointments belong to the given doctor.
     */
    public java.util.List<String> getPatientsForDoctor(String doctorName) {
        java.util.Set<String> patients = new java.util.LinkedHashSet<>();
        for (int i = 0; i < appointmentModel.getRowCount(); i++) {
            String doc = str(appointmentModel.getValueAt(i, 3));
            if (doc.equals(doctorName)) {
                patients.add(str(appointmentModel.getValueAt(i, 0)));
            }
        }
        return new java.util.ArrayList<>(patients);
    }

    private String str(Object o) { return o == null ? "--" : o.toString(); }

    public void updatePatientAppointmentStatus(String patientName, String date, String timeSlot, String newStatus) {
        String key = patientName + "|" + date + "|" + timeSlot + "|" + doctorDisplayName;
        patientAppointmentStatus.put(key, newStatus);
        System.out.println("[doctorDataStore] Appointment " + key + " → " + newStatus);
        // Sync to admin dashboard via HospitalSystem
        String dept = DoctorRosterStore.getDoctorDepartmentByName(doctorDisplayName);
        HospitalSystem.syncDoctorDecision(patientName, doctorDisplayName, dept != null ? dept : "General", date, timeSlot, newStatus);
        notifyListeners();
    }

    public void ensureInOverall(String name, Object age, String gender,
                                String contact, String blood) {
        for (int i = 0; i < overallPatientModel.getRowCount(); i++) {
            if (name.equals(overallPatientModel.getValueAt(i, 0))) {
                overallPatientModel.setValueAt(age,    i, 1);
                overallPatientModel.setValueAt(gender, i, 2);
                overallPatientModel.setValueAt(contact,i, 3);
                overallPatientModel.setValueAt(blood,  i, 4);
                Object visits = overallPatientModel.getValueAt(i, 5);
                int v = 0;
                try { v = Integer.parseInt(visits.toString()); } catch (Exception ignored) {}
                overallPatientModel.setValueAt(v + 1, i, 5);
                return;
            }
        }
        overallPatientModel.addRow(new Object[]{name, age, gender, contact, blood, 1});
    }
}