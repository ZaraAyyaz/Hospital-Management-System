package System;

import Patient.Appointment;
import Patient.Dashboard;

public class HospitalSystem {

    public static Runnable onDoctorLogout;
    public static Runnable showLauncher;

    private static final java.util.List<Appointment> allAppointments = new java.util.ArrayList<>();

    /**
     * Shared booked-slot key set: keys are "doctorName|yyyy-MM-dd|HH:mm".
     * Written by Patient and Receptionist booking flows.
     * Checked by both sides to prevent double-booking.
     */
    private static final java.util.Set<String> bookedSlotKeys = new java.util.LinkedHashSet<>();

    public static boolean isSlotBooked(String doctor, String dateYmd, String time) {
        return bookedSlotKeys.contains(doctor + "|" + dateYmd + "|" + time);
    }

    public static void markSlotBooked(String doctor, String dateYmd, String time) {
        bookedSlotKeys.add(doctor + "|" + dateYmd + "|" + time);
    }

    public static void clearSlot(String doctor, String dateYmd, String time) {
        bookedSlotKeys.remove(doctor + "|" + dateYmd + "|" + time);
    }

    public static java.util.Set<String> getBookedSlotKeys() {
        return java.util.Collections.unmodifiableSet(bookedSlotKeys);
    }

    public static void registerAppointment(Appointment apt) {
        allAppointments.add(apt);
    }

    public static void unregisterAppointment(String patientName, String date, String timeSlot) {
        if (Dashboard.patient != null) {
            allAppointments.removeIf(a ->
                Dashboard.patient.getName().equals(patientName)
                && a.getDate().equals(date)
                && a.getTimeSlot().equals(timeSlot));
        } else {
            allAppointments.removeIf(a ->
                (a.getPatient() != null && a.getPatient().getName().equals(patientName))
                && a.getDate().equals(date)
                && a.getTimeSlot().equals(timeSlot));
        }
    }

    public static Appointment findAppointment(String patientName, String date, String timeSlot) {
        for (Appointment a : allAppointments) {
            if (a.getDoctorName().equals(patientName)
                    && a.getDate().equals(date)
                    && a.getTimeSlot().equals(timeSlot)) {
                return a;
            }
        }
        return null;
    }

    public static void doctorAccept(String patientName, String date, String timeSlot) {
        Appointment apt = findAppointment(patientName, date, timeSlot);
        if (apt != null) {
            apt.setStatus("Scheduled");
            System.out.println("[HospitalSystem] Doctor ACCEPTED appointment: " + patientName + " on " + date + " at " + timeSlot);
        } else {
            System.out.println("[HospitalSystem] No matching appointment found for accept: " + patientName + " " + date + " " + timeSlot);
        }
    }

    public static void doctorReject(String patientName, String date, String timeSlot) {
        Appointment apt = findAppointment(patientName, date, timeSlot);
        if (apt != null) {
            apt.setStatus("Rejected");
            System.out.println("[HospitalSystem] Doctor REJECTED appointment: " + patientName + " on " + date + " at " + timeSlot);
        } else {
            System.out.println("[HospitalSystem] No matching appointment found for reject: " + patientName + " " + date + " " + timeSlot);
        }
    }

    public static void syncAppointmentToDoctorStore(Appointment apt) {
        try {
            registerAppointment(apt);
            Patient.Patient srcPatient = apt.getPatient() != null ? apt.getPatient() : Dashboard.patient;
            String patientName = srcPatient != null ? srcPatient.getName() : apt.getDoctorName();
            String patientEmail = srcPatient != null ? srcPatient.getEmail() : "--";
            String gender = srcPatient != null ? srcPatient.getGender() : "--";
            String contact = srcPatient != null ? srcPatient.getPhoneNumber() : "--";
            String age = srcPatient != null ? String.valueOf(srcPatient.getAge()) : "--";
            String blood = srcPatient != null ? srcPatient.getBloodGroup() : "--";
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
            ds.setDoctorDisplayName(apt.getDoctorName());
            // Remove any stale registration placeholder rows (doctor="Unassigned") for this patient
            javax.swing.table.DefaultTableModel am = ds.getAppointmentModel();
            for (int i = am.getRowCount() - 1; i >= 0; i--) {
                Object pn = am.getValueAt(i, 0);
                Object dc = am.getValueAt(i, 3);
                if (pn != null && pn.toString().equalsIgnoreCase(patientName)
                        && (dc == null || dc.toString().isEmpty() || dc.toString().equals("Unassigned"))) {
                    am.removeRow(i);
                }
            }
            ds.addAppointment(patientName, apt.getTimeSlot(), apt.getDate(), apt.getDoctorName());
            String aptStatus = apt.getStatus() != null ? apt.getStatus() : "Pending";
            ds.updatePatientAppointmentStatus(patientName, apt.getDate(), apt.getTimeSlot(), aptStatus);
            // Remove stale registration placeholder rows (doctor="Unassigned") from history model
            javax.swing.table.DefaultTableModel hm = ds.getHistoryAppointmentModel();
            for (int i = hm.getRowCount() - 1; i >= 0; i--) {
                Object hn = hm.getValueAt(i, 0);
                Object hdc = hm.getValueAt(i, 10);
                if (hn != null && hn.toString().equalsIgnoreCase(patientName)
                        && (hdc == null || hdc.toString().isEmpty() || hdc.toString().equals("Unassigned"))) {
                    hm.removeRow(i);
                }
            }
            // Remove old history rows for the same slot that were completed/cancelled
            String normDoc = (apt.getDoctorName() != null && apt.getDoctorName().startsWith("Dr. "))
                ? apt.getDoctorName().substring(4) : apt.getDoctorName();
            for (int i = hm.getRowCount() - 1; i >= 0; i--) {
                Object hn = hm.getValueAt(i, 0);
                Object hd = hm.getValueAt(i, 4);
                Object ht = hm.getValueAt(i, 5);
                Object hdc = hm.getValueAt(i, 10);
                if (hn != null && hd != null && ht != null && hdc != null
                        && hn.toString().equalsIgnoreCase(patientName)
                        && hd.toString().equals(apt.getDate())
                        && ht.toString().equals(apt.getTimeSlot())
                        && hdc.toString().equalsIgnoreCase(normDoc)) {
                    hm.removeRow(i);
                }
            }
            hm.addRow(new Object[]{
                patientName, age, contact,
                "Appointment", apt.getDate(), apt.getTimeSlot(),
                "", "", "", "", normDoc
            });
            ds.ensureInOverall(patientName, age, gender, contact, blood);
            // Sync email to doctor medical history
            if (!"--".equals(patientEmail))
                ds.updateMedicalHistory(patientName, "Email", patientEmail);
            // Sync patient medical history to doctor's medical history map
            Patient.Patient p = srcPatient;
            if (p != null) {
                java.util.ArrayList<String[]> hist = p.getMedicalHistory();
                if (hist != null) {
                    boolean diagnosisSet = false;
                    String allMeds = "";
                    String allAllergies = "";
                    for (String[] entry : hist) {
                        if (entry.length >= 5) {
                            String cond = entry[0] != null ? entry[0] : "";
                            String med  = entry[3] != null ? entry[3] : "";
                            String tag  = cond.toLowerCase();
                            if (tag.startsWith("vaccination"))
                                ds.updateMedicalHistory(patientName, "Vaccinations", cond.replaceAll("(?i)^vaccinations?:\\s*", ""));
                            else if (tag.startsWith("family history"))
                                ds.updateMedicalHistory(patientName, "FamilyHistory", cond.replaceAll("(?i)^family\\s*history:\\s*", ""));
                            else if (tag.startsWith("chronic condition") || tag.startsWith("chronic"))
                                ds.updateMedicalHistory(patientName, "ChronicConditions", cond.replaceAll("(?i)^chronic\\s*condition:\\s*", ""));
                            else if (tag.startsWith("past surgery") || tag.startsWith("surgery"))
                                ds.updateMedicalHistory(patientName, "Surgeries", cond.replaceAll("(?i)^past\\s*surgery:\\s*", ""));
                            else if (tag.contains("allergy"))
                                allAllergies += (allAllergies.isEmpty() ? "" : ", ") + cond;
                            else if (!cond.isEmpty() && !cond.equals("Recorded") && !diagnosisSet) {
                                ds.updateMedicalHistory(patientName, "Diagnosis", cond);
                                diagnosisSet = true;
                            }
                            if (!med.isEmpty() && !med.equals("--") && !med.equals("None"))
                                allMeds += (allMeds.isEmpty() ? "" : ", ") + med;
                        }
                    }
                    if (!allMeds.isEmpty())
                        ds.updateMedicalHistory(patientName, "Medications", allMeds);
            if (!allAllergies.isEmpty())
                ds.updateMedicalHistory(patientName, "Allergies", allAllergies);
                }
            }
            // Sync to receptionist's appointment view
            try {
                String dept = apt.getSpecialty() != null ? apt.getSpecialty() : "General";
                Receptionist.ReceptionistDataStore.addToApptModel(
                    apt.getAppointmentId(), patientName, apt.getDate(), apt.getTimeSlot(),
                    apt.getDoctorName(), dept, "Pending");
            } catch (Exception ignored) { }
        } catch (Exception e) {
            System.out.println("[HospitalSystem] syncAppointmentToDoctorStore: " + e.getMessage());
        }
    }

    /** Bridge: when a patient registers via the patient portal, make them visible to the receptionist. */
    public static void syncPatientRegistrationFromPortal(String name, String email, String phone, int age, String gender, String blood) {
        try {
            // Add to receptionist's apptModel
            Receptionist.ReceptionistDataStore.patientCounter++;
            Receptionist.ReceptionistDataStore.apptCounter++;
            String apptId = "#A-00" + Receptionist.ReceptionistDataStore.apptCounter;
            Receptionist.ReceptionistDataStore.apptModel.addRow(
                new Object[]{apptId, name, "Pending", "--", "Unassigned", "General", "Low", ""});
            Receptionist.ReceptionistDataStore.appointmentStatusMap.put(apptId, "Registered");
            // Sync to doctor data store so they also appear in consultation / overview
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
            ds.ensureInOverall(name, String.valueOf(age), gender, phone, blood);
            ds.updateMedicalHistory(name, "Email", email);
            ds.notifyListeners();
        } catch (Exception e) {
            System.out.println("[HospitalSystem] syncPatientRegistrationFromPortal: " + e.getMessage());
        }
    }

    public static void syncDoctorStatusToPatient() {
        try {
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
            int totalEntries = ds.patientAppointmentStatus.size();
            System.out.println("[Sync] syncDoctorStatusToPatient called. Map has " + totalEntries + " entries.");
            java.util.Set<String> processed = new java.util.LinkedHashSet<>();
            for (java.util.Map.Entry<String, String> entry : ds.patientAppointmentStatus.entrySet()) {
                String key = entry.getKey();
                String newStatus = entry.getValue();
                String[] parts = key.split("\\|");
                if (parts.length >= 3) {
                    String pName = parts[0];
                    String date = parts[1];
                    String time = parts[2];
                    String doctor = parts.length >= 4 ? parts[3] : "";
                    if (Dashboard.patient != null) {
                        boolean matched = false;
                        for (Appointment a : Dashboard.patient.getAppointments()) {
                            if (a.getStatus().equals("Cancelled") || a.getStatus().equals("Rejected")) continue;
                            if (!a.getDate().equals(date) || !a.getTimeSlot().equals(time)) continue;
                            if (!doctor.isEmpty() && !a.getDoctorName().equals(doctor)) continue;
                            a.setStatus(newStatus);
                            String msg = "Appointment with " + a.getDoctorName() + " on " + date + " at " + time;
                            if (newStatus.equals("Scheduled")) {
                                msg += " has been ACCEPTED by the doctor.";
                            } else if (newStatus.equals("Completed")) {
                                msg += " has been COMPLETED \u2014 consultation done.";
                            } else if (newStatus.equals("Rejected")) {
                                msg += " has been REJECTED by the doctor.";
                            } else {
                                msg += " status updated to " + newStatus;
                            }
                            Dashboard.patient.sendSMS(msg);
                            Dashboard.patient.sendEmail(msg);
                            Dashboard.addAlert(msg, newStatus.equals("Rejected") ? Dashboard.RED_BG : Dashboard.GREEN_BG, newStatus.equals("Rejected") ? Dashboard.RED_FG : Dashboard.GREEN_FG);
                            System.out.println("[Sync] Matched key=" + key + " set status=" + newStatus + " on patient appointment");
                            processed.add(key);
                            matched = true;
                            break;
                        }
                        if (!matched) {
                            System.out.println("[Sync] No match for key=" + key + " status=" + newStatus + " (patient=" + Dashboard.patient.getName() + ")");
                        }
                    } else {
                        System.out.println("[Sync] Dashboard.patient is null, skipping key=" + key);
                    }
                }
            }
            System.out.println("[Sync] Processed " + processed.size() + " entries. Cleaning up Rejected only (keeping Completed for filtering).");
            for (String k : processed) {
                String v = ds.patientAppointmentStatus.get(k);
                if ("Rejected".equals(v)) {
                    System.out.println("[Sync] Removing terminal entry: " + k + " -> " + v);
                    ds.patientAppointmentStatus.remove(k);
                }
            }
        } catch (Exception e) {
            System.out.println("[HospitalSystem] syncDoctorStatusToPatient: " + e.getMessage());
        }
    }

    public static void cancelFromDoctorStore(String patientName, String date, String timeSlot) {
        try {
            // Capture doctor name before unregistering
            String doctorName = "";
            synchronized (allAppointments) {
                for (Appointment a : allAppointments) {
                    String pn = a.getPatient() != null ? a.getPatient().getName() : "";
                    if (pn.equals(patientName) && a.getDate().equals(date) && a.getTimeSlot().equals(timeSlot)) {
                        doctorName = a.getDoctorName();
                        break;
                    }
                }
            }
            unregisterAppointment(patientName, date, timeSlot);
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();

            ds.updatePatientAppointmentStatus(patientName, date, timeSlot, "Cancelled");

            javax.swing.table.DefaultTableModel am = ds.getAppointmentModel();
            for (int i = am.getRowCount() - 1; i >= 0; i--) {
                Object d = am.getValueAt(i, 0);
                Object t = am.getValueAt(i, 1);
                Object dt = am.getValueAt(i, 2);
                if (d != null && d.toString().equals(patientName)
                        && t != null && t.toString().equals(timeSlot)
                        && dt != null && dt.toString().equals(date)) {
                    am.removeRow(i);
                    break;
                }
            }
            javax.swing.table.DefaultTableModel hm = ds.getHistoryAppointmentModel();
            for (int i = hm.getRowCount() - 1; i >= 0; i--) {
                Object n = hm.getValueAt(i, 0);
                Object td = hm.getValueAt(i, 4);
                Object tt = hm.getValueAt(i, 5);
                if (n != null && n.toString().equals(patientName)
                        && td != null && td.toString().equals(date)
                        && tt != null && tt.toString().equals(timeSlot)) {
                    hm.removeRow(i);
                    break;
                }
            }
            ds.notifyListeners();
            // Update receptionist apptModel status to Cancelled
            if (!doctorName.isEmpty()) {
                try {
                    Receptionist.ReceptionistDataStore.updateApptStatus(patientName, date, timeSlot, doctorName, "Cancelled");
                } catch (Exception ignored) { }
            }
        } catch (Exception e) {
            System.out.println("[HospitalSystem] cancelFromDoctorStore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═════════════════════════════════════════════════════════════════════
    //  SHARED APPOINTMENT & EMERGENCY TRACKING (cross-system connectivity)
    // ═════════════════════════════════════════════════════════════════════
    
    public static class SharedAppointment {
        public String apptId, patientName, doctorName, department, date, time, status;
        public SharedAppointment(String apptId, String patientName, String doctorName,
                                 String department, String date, String time, String status) {
            this.apptId = apptId; this.patientName = patientName; this.doctorName = doctorName;
            this.department = department; this.date = date; this.time = time; this.status = status;
        }
    }

    public static class SharedEmergencyCase {
        public String caseId, patientName, complaint, severity, doctorName, department, date, time;
        public SharedEmergencyCase(String caseId, String patientName, String complaint,
                                   String severity, String doctorName, String department,
                                   String date, String time) {
            this.caseId = caseId; this.patientName = patientName; this.complaint = complaint;
            this.severity = severity; this.doctorName = doctorName; this.department = department;
            this.date = date; this.time = time;
        }
    }

    private static final java.util.List<SharedAppointment> sharedAppointments = new java.util.ArrayList<>();
    private static final java.util.List<SharedEmergencyCase> sharedEmergencies = new java.util.ArrayList<>();

    /** Add or update a shared appointment (keyed by apptId). */
    public static void addSharedAppointment(String apptId, String patientName, String doctorName,
                                            String department, String date, String time, String status) {
        sharedAppointments.removeIf(a -> a.apptId.equals(apptId));
        sharedAppointments.add(new SharedAppointment(apptId, patientName, doctorName,
                                                     department, date, time, status));
        notifyAppointmentListeners();
    }

    /** Update just the status of an existing shared appointment. */
    public static void updateSharedAppointmentStatus(String apptId, String status) {
        for (SharedAppointment a : sharedAppointments) {
            if (a.apptId.equals(apptId)) { a.status = status; break; }
        }
        notifyAppointmentListeners();
    }

    /** Update shared appointment status by patient name + time (time format is consistent). */
    public static void updateSharedAppointmentStatusByPatient(String patientName, String time) {
        boolean found = false;
        for (SharedAppointment a : sharedAppointments) {
            if (a.patientName.equalsIgnoreCase(patientName) && a.time.equals(time)) {
                a.status = "Booked";
                found = true;
            }
        }
        if (found) notifyAppointmentListeners();
    }

    /** Get all shared appointments (unmodifiable). */
    public static java.util.List<SharedAppointment> getSharedAppointments() {
        return java.util.Collections.unmodifiableList(sharedAppointments);
    }

    /** Add an emergency case (shared across receptionist → admin). Replaces existing if same patient+date+time. */
    public static void addSharedEmergency(String caseId, String patientName, String complaint,
                                          String severity, String doctorName, String department,
                                          String date, String time) {
        System.out.println("[DEBUG HospitalSystem] addSharedEmergency called: caseId=" + caseId + " patient=" + patientName + " dept=" + department + " date=" + date + " time=" + time);
        int before = sharedEmergencies.size();
        sharedEmergencies.removeIf(e ->
            e.patientName.equalsIgnoreCase(patientName)
            && e.date.equals(date)
            && e.time.equals(time));
        sharedEmergencies.add(new SharedEmergencyCase(caseId, patientName, complaint,
                                                       severity, doctorName, department,
                                                       date, time));
        System.out.println("[DEBUG HospitalSystem] addSharedEmergency done: removed=" + (before - sharedEmergencies.size() + 1) + " new size=" + sharedEmergencies.size());
        notifyEmergencyListeners();
    }

    /** Get all shared emergency cases. */
    public static java.util.List<SharedEmergencyCase> getSharedEmergencies() {
        System.out.println("[DEBUG HospitalSystem] getSharedEmergencies called: size=" + sharedEmergencies.size());
        return java.util.Collections.unmodifiableList(sharedEmergencies);
    }

    /** Update status of a shared appointment by patient+date+time+doctor (for doctor panel sync). */
    public static void updateSharedApptStatusByDetails(String patientName, String date, String time, String doctorName, String status) {
        for (SharedAppointment a : sharedAppointments) {
            if (a.patientName.equalsIgnoreCase(patientName)
                    && a.date.equals(date)
                    && a.time.equals(time)
                    && a.doctorName.contains(doctorName)) {
                a.status = status;
                notifyAppointmentListeners();
                return;
            }
        }
        // If not found by exact match, try partial
        for (SharedAppointment a : sharedAppointments) {
            if (a.patientName.equalsIgnoreCase(patientName)
                    && a.date.equals(date)
                    && a.time.equals(time)) {
                a.status = status;
                notifyAppointmentListeners();
                return;
            }
        }
    }

    /** Update doctor and department for a shared appointment by apptId. */
    public static void updateSharedAppointmentDoctorDept(String apptId, String newDoctor, String newDept) {
        for (SharedAppointment a : sharedAppointments) {
            if (a.apptId.equals(apptId)) {
                a.doctorName = newDoctor;
                a.department = newDept;
                notifyAppointmentListeners();
                System.out.println("[HospitalSystem] updateSharedAppointmentDoctorDept: apptId=" + apptId + " doctor=" + newDoctor + " dept=" + newDept);
                return;
            }
        }
        System.out.println("[HospitalSystem] updateSharedAppointmentDoctorDept: apptId=" + apptId + " NOT FOUND");
    }

    /** Update doctor and department for a shared appointment by patient+date+time. */
    public static void updateSharedAppointmentDoctorDeptByPatient(String patientName, String date, String time, String newDoctor, String newDept) {
        for (SharedAppointment a : sharedAppointments) {
            if (a.patientName.equalsIgnoreCase(patientName) && a.date.equals(date) && a.time.equals(time)) {
                a.doctorName = newDoctor;
                a.department = newDept;
                notifyAppointmentListeners();
                System.out.println("[HospitalSystem] updateSharedAppointmentDoctorDeptByPatient: patient=" + patientName + " doctor=" + newDoctor + " dept=" + newDept);
                return;
            }
        }
        System.out.println("[HospitalSystem] updateSharedAppointmentDoctorDeptByPatient: patient=" + patientName + " NOT FOUND");
    }

    /** Remove a shared emergency case by patient+date+time. */
    public static void removeSharedEmergencyByPatient(String patientName, String date, String time) {
        boolean removed = sharedEmergencies.removeIf(e ->
            e.patientName.equalsIgnoreCase(patientName) && e.date.equals(date) && e.time.equals(time));
        if (removed) {
            notifyEmergencyListeners();
            System.out.println("[HospitalSystem] removeSharedEmergencyByPatient: patient=" + patientName + " date=" + date + " time=" + time);
        } else {
            System.out.println("[HospitalSystem] removeSharedEmergencyByPatient: NOT FOUND for patient=" + patientName);
        }
    }

    /** Remove a shared emergency case by caseId. */
    public static void removeSharedEmergencyByCaseId(String caseId) {
        boolean removed = sharedEmergencies.removeIf(e -> e.caseId.equals(caseId));
        if (removed) {
            notifyEmergencyListeners();
            System.out.println("[HospitalSystem] removeSharedEmergencyByCaseId: caseId=" + caseId);
        }
    }

    // ── Admin doctor reassignment callback (bridges package boundary) ─────
    @FunctionalInterface
    public interface AdminReassignHandler {
        void reassign(String apptId, String patientName, String date, String time,
                      String newDoctor, String newDept);
    }
    private static AdminReassignHandler adminReassignHandler;

    /** Register a handler for admin doctor reassignment (set by ReceptionistDataStore). */
    public static void setAdminReassignHandler(AdminReassignHandler handler) {
        adminReassignHandler = handler;
    }

    /** Trigger the admin reassignment handler from any package. */
    public static void triggerAdminReassign(String apptId, String patientName, String date,
                                            String time, String newDoctor, String newDept) {
        if (adminReassignHandler != null) {
            adminReassignHandler.reassign(apptId, patientName, date, time, newDoctor, newDept);
        } else {
            System.out.println("[HospitalSystem] triggerAdminReassign: no handler registered!");
        }
    }

    /** Clear all shared emergency cases (for admin re-assignment). */
    public static void clearSharedEmergencies() {
        sharedEmergencies.clear();
        notifyEmergencyListeners();
    }

    // ── Doctor shift-change time adjustment callback ────────────────────
    @FunctionalInterface
    public interface TimeAdjustHandler {
        void adjust(String doctorName, String oldTime, String newTime, String date, String apptId, String patientName);
    }
    private static TimeAdjustHandler timeAdjustHandler;

    public static void setTimeAdjustHandler(TimeAdjustHandler handler) {
        timeAdjustHandler = handler;
    }

    public static void triggerTimeAdjust(String doctorName, String oldTime, String newTime,
                                         String date, String apptId, String patientName) {
        if (timeAdjustHandler != null) {
            timeAdjustHandler.adjust(doctorName, oldTime, newTime, date, apptId, patientName);
        }
    }

    // ── Listeners for UI refresh ────────────────────────────────────────
    private static final java.util.List<Runnable> apptListeners = new java.util.ArrayList<>();
    private static final java.util.List<Runnable> emergListeners = new java.util.ArrayList<>();

    public static void addAppointmentListener(Runnable r) { apptListeners.add(r); }
    public static void addEmergencyListener(Runnable r)   { emergListeners.add(r); }
    private static void notifyAppointmentListeners() {
        for (Runnable r : apptListeners) { try { r.run(); } catch (Exception e) { e.printStackTrace(); } }
    }
    private static void notifyEmergencyListeners() {
        for (Runnable r : emergListeners) { try { r.run(); } catch (Exception e) { e.printStackTrace(); } }
    }

    // ── Notifications for receptionist dashboard ─────────────────────────
    private static final java.util.List<String> notifications = new java.util.ArrayList<>();
    private static final java.util.List<Runnable> notifListeners = new java.util.ArrayList<>();

    public static void addNotification(String message) {
        notifications.add(message);
        for (Runnable r : notifListeners) {
            try { r.run(); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    public static java.util.List<String> getNotifications() {
        return java.util.Collections.unmodifiableList(notifications);
    }

    public static void clearNotifications() {
        notifications.clear();
    }

    public static void addNotificationListener(Runnable r) {
        notifListeners.add(r);
    }

    public static void removeNotificationListener(Runnable r) {
        notifListeners.remove(r);
    }

    public static void syncDoctorDecision(String patientName, String doctorName,
                                           String department, String date, String timeSlot, String status) {
        try {
            // Look up the existing apptId from shared appointments
            String apptId = null;
            for (SharedAppointment sa : sharedAppointments) {
                if (sa.patientName.equalsIgnoreCase(patientName)
                        && sa.date.equals(date) && sa.time.equals(timeSlot)) {
                    apptId = sa.apptId;
                    break;
                }
            }
            if (apptId == null) {
                // Fallback: try receptionist apptModel
                try {
                    for (int i = 0; i < Receptionist.ReceptionistDataStore.apptModel.getRowCount(); i++) {
                        String pid = Receptionist.ReceptionistDataStore.apptModel.getValueAt(i, 0).toString().trim();
                        String pname = Receptionist.ReceptionistDataStore.apptModel.getValueAt(i, 1).toString().trim();
                        String pdate = Receptionist.ReceptionistDataStore.apptModel.getValueAt(i, 2).toString().trim();
                        String ptime = Receptionist.ReceptionistDataStore.apptModel.getValueAt(i, 3).toString().trim();
                        if (pname.equalsIgnoreCase(patientName) && pdate.equals(date) && ptime.equals(timeSlot)) {
                            apptId = pid;
                            break;
                        }
                    }
                } catch (Exception ignored) { }
            }
            if (apptId == null) {
                apptId = "#A-00" + (++Receptionist.ReceptionistDataStore.apptCounter);
            }
            addSharedAppointment(apptId, patientName, doctorName, department, date, timeSlot, status);
            Admin.HospitalAdmin.registerAppointment(apptId, patientName, doctorName, date + " " + timeSlot);
            boolean found = false;
            for (Admin.HospitalAdmin.Patient p : Admin.HospitalAdmin.sharedPatientList) {
                if (p.getName().equalsIgnoreCase(patientName)) { found = true; break; }
            }
            if (!found) {
                Admin.HospitalAdmin.registerPatient("PAT-" + patientName.replace(" ", ""), patientName, department, "Unassigned");
            }
            try {
                Receptionist.ReceptionistDataStore.updateApptStatus(patientName, date, timeSlot, doctorName, status);
            } catch (Exception ignored) { }
        } catch (Exception e) {
            System.out.println("[HospitalSystem] syncDoctorDecision: " + e.getMessage());
        }
    }

    /**
     * Look up a patient's profile from the Patient module's global registry.
     * Returns a map with keys: Name, Age, Gender, Contact, Blood, Email,
     * as well as medical fields extracted from the Patient object's history.
     * Returns an empty map if the patient is not found in PatientGUI.allPatients.
     */
    public static java.util.LinkedHashMap<String, String> fetchPatientProfile(String patientName) {
        java.util.LinkedHashMap<String, String> result = new java.util.LinkedHashMap<>();
        try {
            java.util.ArrayList<Patient.Patient> all = Patient.PatientGUI.allPatients;
            if (all == null) return result;
            for (Patient.Patient p : all) {
                if (p.getName().equalsIgnoreCase(patientName)) {
                    result.put("Name",   p.getName());
                    result.put("Age",    String.valueOf(p.getAge()));
                    result.put("Gender", p.getGender() != null ? p.getGender() : "");
                    result.put("Contact",p.getPhoneNumber() != null ? p.getPhoneNumber() : "");
                    result.put("Blood",  p.getBloodGroup() != null ? p.getBloodGroup() : "");
                    result.put("Email",  p.getEmail() != null ? p.getEmail() : "");
                    // Extract medical history fields
                    StringBuilder diag = new StringBuilder();
                    StringBuilder meds = new StringBuilder();
                    StringBuilder aller = new StringBuilder();
                    StringBuilder chronic = new StringBuilder();
                    StringBuilder family = new StringBuilder();
                    StringBuilder surgeries = new StringBuilder();
                    StringBuilder vaccs = new StringBuilder();
                    java.util.ArrayList<String[]> hist = p.getMedicalHistory();
                    if (hist != null) {
                        for (String[] entry : hist) {
                            if (entry.length < 5) continue;
                            String cond = entry[0] != null ? entry[0] : "";
                            String med  = entry[3] != null ? entry[3] : "";
                            String tag  = cond.toLowerCase();
                            if (tag.startsWith("vaccination") || tag.startsWith("vaccine")) {
                                String clean = cond.replaceAll("(?i)^vaccinations?:?\\s*", "")
                                                    .replaceAll("(?i)^vaccine:?\\s*", "");
                                vaccs.append(clean).append("; ");
                            }
                            else if (tag.startsWith("family history")) {
                                String clean = cond.replaceAll("(?i)^family\\s*history:?\\s*", "");
                                family.append(clean).append("; ");
                            } else if (tag.startsWith("chronic condition") || tag.startsWith("chronic")) {
                                String clean = cond.replaceAll("(?i)^chronic\\s*condition:?\\s*", "")
                                                    .replaceAll("(?i)^chronic:?\\s*", "");
                                chronic.append(clean).append("; ");
                            }
                            else if (tag.startsWith("past surgery") || tag.startsWith("surgery")) {
                                String clean = cond.replaceAll("(?i)^past\\s*surgery:?\\s*", "")
                                                    .replaceAll("(?i)^surgery:?\\s*", "");
                                surgeries.append(clean).append("; ");
                            }
                            else if (tag.contains("allergy"))
                                aller.append(cond).append("; ");
                            else if (!cond.isEmpty() && !cond.equals("Recorded") && diag.length() == 0)
                                diag.append(cond);
                            if (!med.isEmpty() && !med.equals("--") && !med.equals("None"))
                                meds.append(med).append("; ");
                        }
                    }
                    System.out.println("[Consult] fetchPatientProfile FOUND " + patientName + " in PatientGUI.allPatients"
                        + " Email=|" + result.get("Email") + "| Age=|" + result.get("Age") + "| Gender=|" + result.get("Gender") + "|");
                    if (diag.length() > 0)      result.put("Diagnosis",   diag.toString());
                    if (meds.length() > 0)      result.put("Medications", meds.substring(0, meds.length()-2));
                    if (aller.length() > 0)     result.put("Allergies",   aller.substring(0, aller.length()-2));
                    if (chronic.length() > 0)   result.put("ChronicConditions", chronic.substring(0, chronic.length()-2));
                    if (family.length() > 0)    result.put("FamilyHistory",     family.substring(0, family.length()-2));
                    if (surgeries.length() > 0) result.put("Surgeries",         surgeries.substring(0, surgeries.length()-2));
                    if (vaccs.length() > 0)     result.put("Vaccinations",      vaccs.substring(0, vaccs.length()-2));
                    break;
                }
            }
            if (result.isEmpty()) {
                System.out.println("[Consult] fetchPatientProfile NOT FOUND " + patientName + " in PatientGUI.allPatients (size=" + all.size() + ")");
            }
        } catch (Exception e) {
            System.out.println("[HospitalSystem] fetchPatientProfile: " + e.getMessage());
        }
        return result;
    }

    public static void syncDoctorDataToPatient(String patientName) {
        try {
            Doctor.doctorDataStore ds = Doctor.doctorDataStore.get();
            java.util.Map<String, String> mh = ds.getMedicalHistory(patientName);
            String doctorName = ds.getDoctorDisplayName();

            String diag = mh.getOrDefault("Diagnosis", "");
            String meds = mh.getOrDefault("Medications", "");
            String aller = mh.getOrDefault("Allergies", "");

            java.util.List<String[]> history = Dashboard.patient.getMedicalHistory();
            history.removeIf(e -> e[0].startsWith("Rx: "));

            boolean hasAller = history.stream().anyMatch(e -> e[0].startsWith("Allergies: " + aller));
            if (!aller.isEmpty() && !hasAller) {
                Dashboard.patient.addMedicalHistory("Allergies: " + aller, "Recorded", doctorName, "--", "Allergy");
            }

            javax.swing.table.DefaultTableModel pm = ds.getPrescriptionModel();
            boolean hasAnyRx = false;
            for (int i = 0; i < pm.getRowCount(); i++) {
                if (patientName.equals(pm.getValueAt(i, 0))) {
                    String rxMed = pm.getValueAt(i, 2) == null ? "" : pm.getValueAt(i, 2).toString();
                    if (rxMed.isEmpty()) continue;
                    hasAnyRx = true;
                    String rxDate = pm.getValueAt(i, 1) == null ? "" : pm.getValueAt(i, 1).toString();
                    String title = diag.isEmpty() ? rxMed : diag;
                    boolean hasRx = history.stream().anyMatch(e -> e[3].equals(rxMed) && e[2].equals(doctorName));
                    if (!hasRx) {
                        Dashboard.patient.addMedicalHistory(title, rxDate, doctorName, rxMed, "Prescribed");
                    }
                }
            }

            if (!hasAnyRx && !diag.isEmpty()) {
                boolean hasDiag = history.stream().anyMatch(e -> e[0].startsWith("Diagnosis: " + diag));
                if (!hasDiag) {
                    Dashboard.patient.addMedicalHistory("Diagnosis: " + diag, "Prescribed", doctorName, meds, "Managed");
                }
            }
            System.out.println("[HospitalSystem] Synced doctor data to patient: " + patientName);
        } catch (Exception e) {
            System.out.println("[HospitalSystem] syncDoctorDataToPatient: " + e.getMessage());
        }
    }
}
