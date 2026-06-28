import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import Patient.*;
import System.HospitalSystem;
import Admin.AdminLoginScreen;
import Admin.HospitalAdmin;
import Doctor.doctorDataStore;
import Doctor.doctorSmartHealthcareLogin;
import Receptionist.ReceptionistLoginScreen;

public class Launcher {

    private static JFrame frame;
    public static void main(String[] args) {
        PatientGUI.patient = new Patient("PAT-SanaAhmed", "Sana Ahmed", "sana@email.com", "1234", "0300-9876543", 23, "A+");
        PatientGUI.allPatients.add(PatientGUI.patient);
        PatientGUI.patient.setGender("Female");
        PatientGUI.patient.addMedicalHistory("Chest Pain - Cardiac Evaluation", "Jan 2022", "Kamran Khan", "Atorvastatin 20mg", "Ongoing");
        PatientGUI.patient.addMedicalHistory("Atrial Fibrillation - Arrhythmia", "May 2019", "Faisal Qureshi", "Bisoprolol 5mg", "Managed");
        PatientGUI.patient.addMedicalHistory("Hypertension - Cardiac Imaging", "Aug 2023", "Anwar Latif", "Amlodipine 5mg", "Managed");
        PatientGUI.patient.addMedicalHistory("Chronic Condition: None", "Recorded", "Self", "--", "Ongoing");
        PatientGUI.patient.addMedicalHistory("Family History: Hypertension", "Recorded", "Self", "--", "Info");
        PatientGUI.patient.addMedicalHistory("Past Surgery: None", "Recorded", "Self", "--", "Surgery");
        PatientGUI.patient.addMedicalHistory("Vaccinations: Hepatitis B, Tetanus", "Recorded", "Self", "--", "Vaccine");
        // Sync demo patient's medical info to doctor data store
        doctorDataStore dds = doctorDataStore.get();
        dds.updateMedicalHistory("Sana Ahmed", "ChronicConditions", "None");
        dds.updateMedicalHistory("Sana Ahmed", "FamilyHistory",     "Hypertension");
        dds.updateMedicalHistory("Sana Ahmed", "Surgeries",         "None");
        dds.updateMedicalHistory("Sana Ahmed", "Vaccinations",      "Hepatitis B, Tetanus");
        PatientGUI.patient.bookAppointment("#A-0021", "Kamran Khan", "Cardiology", "2026-06-10", "10:00 AM");
        PatientGUI.patient.bookAppointment("#A-0022", "Faisal Qureshi", "Cardiology", "2026-06-10", "02:00 PM");
        PatientGUI.patient.bookAppointment("#A-0023", "Anwar Latif", "Cardiology", "2026-06-10", "10:00 PM");
        PatientGUI.patient.bookAppointment("#A-0024", "Bilal Javed", "Cardiology", "2026-06-20", "08:00 AM");
        PatientGUI.patient.cancelAppointment("#A-0023");
        HospitalSystem.showLauncher = Launcher::showLauncher;
        for (Appointment a : PatientGUI.patient.getAppointments()) {
            if (!a.getStatus().equals("Cancelled")) {
                doctorDataStore.get().setDoctorDisplayName(a.getDoctorName());
                break;
            }
        }
        // Sync demo appointments to doctor store so they appear in doctor interfaces
        for (Appointment a : PatientGUI.patient.getAppointments()) {
            if (!a.getStatus().equals("Cancelled")) {
                HospitalSystem.syncAppointmentToDoctorStore(a);
                HospitalSystem.markSlotBooked(a.getDoctorName(), a.getDate(), a.getTimeSlot());
            }
        }
        // Seed shared patient list with demo patients so admin dashboard sees them
        Admin.HospitalAdmin.registerPatient("PAT-SanaAhmed", "Sana Ahmed", "Cardiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-AliHassan", "Ali Hassan",  "Cardiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-ZaraMalik", "Zara Malik",  "Cardiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-ImranQureshi", "Imran Qureshi", "Electrophysiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-NadiaBano", "Nadia Bano",  "Cardiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-KhalidAhmed","Khalid Ahmed","Electrophysiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-SanaFatima", "Sana Fatima", "Cardiac Imaging", "General");
        Admin.HospitalAdmin.registerPatient("PAT-HamidRaza", "Hamid Raza",  "Emergency Medicine", "General");
        Admin.HospitalAdmin.registerPatient("PAT-RukhsanaBeg","Rukhsana Beg","Cardiac Imaging", "General");
        Admin.HospitalAdmin.registerPatient("PAT-DawoodYusuf","Dawood Yusuf","Interventional Cardiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-MariamShah", "Mariam Shah", "Emergency Medicine", "General");
        Admin.HospitalAdmin.registerPatient("PAT-AishaNoor", "Aisha Noor",  "Cardiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-BabarZaman", "Babar Zaman", "Electrophysiology", "General");
        Admin.HospitalAdmin.registerPatient("PAT-HinaRehman", "Hina Rehman", "Cardiac Imaging", "General");
        Admin.HospitalAdmin.registerPatient("PAT-UsmanTariq", "Usman Tariq", "Cardiology", "General");
        // Seed actual medical conditions for demo patients
        dds.updateMedicalHistory("Ali Hassan",   "Diagnosis", "Palpitations");
        dds.updateMedicalHistory("Zara Malik",   "Diagnosis", "Shortness of Breath - Asthma");
        dds.updateMedicalHistory("Imran Qureshi","Diagnosis", "Atrial Fibrillation");
        dds.updateMedicalHistory("Nadia Bano",   "Diagnosis", "Chest Pain - Angina");
        dds.updateMedicalHistory("Khalid Ahmed", "Diagnosis", "Bradycardia - Heart Block");
        dds.updateMedicalHistory("Sana Fatima",  "Diagnosis", "Coronary Artery Disease");
        dds.updateMedicalHistory("Hamid Raza",   "Diagnosis", "Chest Pain Observation");
        dds.updateMedicalHistory("Rukhsana Beg", "Diagnosis", "Myocardial Ischemia");
        dds.updateMedicalHistory("Dawood Yusuf", "Diagnosis", "Coronary Stenosis");
        dds.updateMedicalHistory("Mariam Shah",  "Diagnosis", "Syncope - Vasovagal");
        dds.updateMedicalHistory("Aisha Noor",   "Diagnosis", "Hypertension - Palpitations");
        dds.updateMedicalHistory("Babar Zaman",  "Diagnosis", "Tachycardia - SVT");
        dds.updateMedicalHistory("Hina Rehman",  "Diagnosis", "Pericarditis");
        dds.updateMedicalHistory("Usman Tariq",  "Diagnosis", "Chest Pain - Musculoskeletal");

        HospitalAdmin.preseedRosterStore();
        HospitalAdmin.preseedReceptionistStore();
        SwingUtilities.invokeLater(Launcher::showLauncher);
    }

    public static void showLauncher() {
        if (frame != null) {
            frame.setVisible(true);
            frame.toFront();
            return;
        }
        frame = new JFrame("Smart Health System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1600, 900);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());

        JPanel bg = new JPanel(new GridBagLayout()) {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(8, 12, 25),
                        getWidth(), getHeight(), new Color(10, 25, 60));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 160, 255, 25));
                g2.fillOval(150, 120, 400, 400);
                g2.setColor(new Color(0, 200, 255, 15));
                g2.fillOval(getWidth() - 500, 200, 450, 450);
            }
        };
        frame.setContentPane(bg);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(18, 30, 60, 230));
        card.setPreferredSize(new Dimension(500, 660));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 18, 12, 18);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SMART HEALTH SYSTEM", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        card.add(title, c);

        JLabel sub = new JLabel("Select your role to continue", SwingConstants.CENTER);
        sub.setForeground(new Color(180, 200, 230));
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        c.gridy = 1;
        card.add(sub, c);

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(55, 138, 221, 100));
        c.gridy = 2; c.insets = new Insets(30, 40, 30, 40);
        card.add(sep, c);
        c.insets = new Insets(18, 18, 18, 18);

        JButton patientBtn = new JButton("LOGIN AS PATIENT");
        styleBtn(patientBtn, new Color(0, 140, 255));
        c.gridy = 3; c.gridwidth = 2; c.ipady = 15;
        card.add(patientBtn, c);

        JButton doctorBtn = new JButton("LOGIN AS DOCTOR");
        styleBtn(doctorBtn, new Color(0, 180, 120));
        c.gridy = 4;
        card.add(doctorBtn, c);

        JButton receptionistBtn = new JButton("LOGIN AS RECEPTIONIST");
        styleBtn(receptionistBtn, new Color(0, 190, 160));
        c.gridy = 5;
        card.add(receptionistBtn, c);

        JButton adminBtn = new JButton("ADMIN DASHBOARD");
        styleBtn(adminBtn, new Color(140, 80, 200));
        c.gridy = 6;
        card.add(adminBtn, c);

        JButton exitBtn = new JButton("EXIT");
        styleBtn(exitBtn, new Color(150, 50, 50));
        c.gridy = 7; c.ipady = 10;
        card.add(exitBtn, c);

        patientBtn.addActionListener(e -> {
            JFrame f = frame;
            frame = null;
            f.dispose();
            SwingUtilities.invokeLater(() -> PatientGUI.showLogin());
        });

        HospitalSystem.onDoctorLogout = Launcher::showLauncher;

        doctorBtn.addActionListener(e -> {
            JFrame f = frame;
            frame = null;
            f.dispose();
            SwingUtilities.invokeLater(() -> {
                doctorSmartHealthcareLogin docLogin = new doctorSmartHealthcareLogin();
                docLogin.addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent we) {
                        if (!doctorSmartHealthcareLogin.transitioning) {
                            showLauncher();
                        }
                        doctorSmartHealthcareLogin.transitioning = false;
                    }
                });
            });
        });

        receptionistBtn.addActionListener(e -> {
            JFrame f = frame;
            frame = null;
            f.dispose();
            SwingUtilities.invokeLater(() -> {
                ReceptionistLoginScreen login = new ReceptionistLoginScreen();
                login.addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent we) {
                        if (!ReceptionistLoginScreen.transitioning) {
                            showLauncher();
                        }
                        ReceptionistLoginScreen.transitioning = false;
                    }
                });
            });
        });

        adminBtn.addActionListener(e -> {
            JFrame f = frame;
            frame = null;
            f.dispose();
            SwingUtilities.invokeLater(() -> {
                AdminLoginScreen login = new AdminLoginScreen();
                login.addWindowListener(new WindowAdapter() {
                    public void windowClosed(WindowEvent we) {
                        if (!AdminLoginScreen.transitioning) {
                            showLauncher();
                        }
                        AdminLoginScreen.transitioning = false;
                    }
                });
            });
        });

        exitBtn.addActionListener(e -> System.exit(0));

        GridBagConstraints main = new GridBagConstraints();
        main.gridx = 0; main.gridy = 0;
        bg.add(card, main);

        frame.setVisible(true);
    }

    private static void styleBtn(JButton b, Color bg) {
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }
}
