package Receptionist;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import System.HospitalSystem;
import Doctor.doctorDataStore;
import Patient.Patient;
import Patient.PatientGUI;

public class RegisterPatientPanel {

    public interface NavigationListener {
        void navigateTo(String section);
        void navigateToAssignDoctor(String prefilledName);
    }

    private final JFrame             owner;
    private final NavigationListener nav;

    public RegisterPatientPanel(JFrame owner, NavigationListener nav) {
        this.owner = owner;
        this.nav   = nav;
    }

    public JPanel build() {
        JPanel p = ReceptionistUIHelper.page();

        JTextField tfName   = ReceptionistUIHelper.styledField("Full Name");
        PlainDocument nameDoc = new PlainDocument() {
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null) return;
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (!Character.isLetter(c) && c != ' ') return;
                }
                super.insertString(offs, str, a);
            }
            public void replace(int offs, int len, String str, AttributeSet a) throws BadLocationException {
                if (str == null) return;
                for (int i = 0; i < str.length(); i++) {
                    char c = str.charAt(i);
                    if (!Character.isLetter(c) && c != ' ') return;
                }
                super.replace(offs, len, str, a);
            }
        };
        tfName.setDocument(nameDoc);
        tfName.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && c != ' ' && !Character.isISOControl(c)) e.consume();
            }
        });
        JTextField tfAge    = ReceptionistUIHelper.styledField("Age");
        JTextField tfEmail  = ReceptionistUIHelper.styledField("Email Address");
        JTextField tfPhone  = ReceptionistUIHelper.styledField("Phone Number");
        PlainDocument phoneDoc = new PlainDocument() {
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null || (getLength() + str.length()) > 11) return;
                for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
                super.insertString(offs, str, a);
            }
            public void replace(int offs, int len, String str, AttributeSet a) throws BadLocationException {
                if (str == null || (getLength() - len + str.length()) > 11) return;
                for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
                super.replace(offs, len, str, a);
            }
        };
        tfPhone.setDocument(phoneDoc);
        tfPhone.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && !Character.isISOControl(e.getKeyChar())) e.consume();
            }
        });
        phoneDoc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                Document d = e.getDocument();
                if (d.getLength() > 11) { try { d.remove(11, d.getLength() - 11); } catch (BadLocationException ex) {} }
            }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        });
        JPasswordField passFld = new JPasswordField(15);
        passFld.setFont(ReceptionistUIHelper.F_BODY);
        passFld.setForeground(ReceptionistUIHelper.C_DARK);
        passFld.setBorder(new CompoundBorder(
            new LineBorder(ReceptionistUIHelper.C_BORDER, 1, true),
            new EmptyBorder(8, 10, 8, 10)));
        JTextField tfBlood = ReceptionistUIHelper.styledField("A+ / B- / O+ ...");
        JComboBox<String> cbGender = ReceptionistUIHelper.combo(new String[]{"Male", "Female", "Other"});
        JTextField tfAddr  = ReceptionistUIHelper.styledField("Street, City");
        JTextField tfEmerg = ReceptionistUIHelper.styledField("Emergency Contact Phone");
        PlainDocument emergDoc = new PlainDocument() {
            public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
                if (str == null || (getLength() + str.length()) > 11) return;
                for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
                super.insertString(offs, str, a);
            }
            public void replace(int offs, int len, String str, AttributeSet a) throws BadLocationException {
                if (str == null || (getLength() - len + str.length()) > 11) return;
                for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
                super.replace(offs, len, str, a);
            }
        };
        tfEmerg.setDocument(emergDoc);
        tfEmerg.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()) && !Character.isISOControl(e.getKeyChar())) e.consume();
            }
        });
        emergDoc.addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                Document d = e.getDocument();
                if (d.getLength() > 11) { try { d.remove(11, d.getLength() - 11); } catch (BadLocationException ex) {} }
            }
            public void removeUpdate(DocumentEvent e) {}
            public void changedUpdate(DocumentEvent e) {}
        });

        JButton btnClear = ReceptionistUIHelper.ghost("Clear Form");
        JButton btnSave  = ReceptionistUIHelper.primary("Register Patient");

        Runnable clearAll = () -> {
            tfName.setText(""); tfAge.setText(""); tfEmail.setText(""); tfPhone.setText("");
            passFld.setText(""); tfBlood.setText(""); tfAddr.setText(""); tfEmerg.setText("");
            cbGender.setSelectedIndex(0);
        };

        btnClear.addActionListener(e -> clearAll.run());

        btnSave.addActionListener(e -> {
            String name  = tfName.getText().trim();
            String email = tfEmail.getText().trim();
            String pass  = new String(passFld.getPassword()).trim();
            String phone = tfPhone.getText().trim();
            String blood = tfBlood.getText().trim();
            String ageStr = tfAge.getText().trim();
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || phone.isEmpty() || ageStr.isEmpty()) {
                ReceptionistUIHelper.showError(owner, "Please fill all required fields.");
                return;
            }
            String emerg = tfEmerg.getText().trim();
            if (!name.matches("[a-zA-Z ]+")) {
                ReceptionistUIHelper.showError(owner, "Name must contain only letters and spaces.");
                return;
            }
            if (phone.length() != 11) {
                ReceptionistUIHelper.showError(owner, "Phone number must be exactly 11 digits.");
                return;
            }
            if (emerg.length() != 11) {
                ReceptionistUIHelper.showError(owner, "Emergency contact must be exactly 11 digits.");
                return;
            }
            int age;
            try { age = Integer.parseInt(ageStr); } catch (NumberFormatException ex) {
                ReceptionistUIHelper.showError(owner, "Age must be a number."); return; }
            if (ReceptionistDataStore.isDuplicatePatient(name, phone)) {
                ReceptionistUIHelper.showError(owner, "Patient \"" + name + "\" already exists.");
                return;
            }
            String patId = "PAT-" + name.replace(" ", "");
            Patient patient = new Patient(patId, name, email, pass, phone, age, blood);
            patient.setGender(cbGender.getSelectedItem().toString());
            PatientGUI.allPatients.add(patient);
            patient.registerPatient();
            String defaultDept = "Cardiology";
            Admin.HospitalAdmin.registerPatient(patId, name, defaultDept, "Unassigned");

            ReceptionistDataStore.patientCounter++;
            ReceptionistDataStore.apptCounter++;
            String pid    = patId;
            String apptId = "#A-00" + ReceptionistDataStore.apptCounter;
            String today = ReceptionistDataStore.FMT_DISPLAY.format(new java.util.Date());
            String now  = new java.text.SimpleDateFormat("HH:mm").format(new java.util.Date());
            ReceptionistDataStore.apptModel.addRow(
                new Object[]{apptId, name, today, now, "Unassigned", defaultDept, "Registered", ""});
            ReceptionistDataStore.syncAppointmentToSharedStores(name, "Unassigned", today, now, apptId, "Registered");
            doctorDataStore ds = doctorDataStore.get();
            ds.updateMedicalHistory(name, "Email", email);
            ds.updateMedicalHistory(name, "Gender", cbGender.getSelectedItem().toString());
            ds.ensureInOverall(name, ageStr, cbGender.getSelectedItem().toString(), phone, blood);

            showConfirmDialog(name, phone, pid, apptId);
            clearAll.run();
        });

        JPanel form = ReceptionistUIHelper.formCard("📋  New Patient Registration");
        JPanel body = new JPanel(new BorderLayout(0, 16)); body.setOpaque(false);

        JPanel grid = new JPanel(new GridLayout(0, 2, 16, 14));
        grid.setOpaque(false);
        grid.add(ReceptionistUIHelper.fg("Full Name *",    tfName));
        grid.add(ReceptionistUIHelper.fg("Age *",          tfAge));
        grid.add(ReceptionistUIHelper.fg("Email *",        tfEmail));
        grid.add(ReceptionistUIHelper.fg("Phone Number *", tfPhone));
        grid.add(ReceptionistUIHelper.fg("Password *",     passFld));
        grid.add(ReceptionistUIHelper.fg("Blood Group",    tfBlood));
        grid.add(ReceptionistUIHelper.fg("Gender",         cbGender));
        grid.add(ReceptionistUIHelper.fg("Address",        tfAddr));
        grid.add(ReceptionistUIHelper.fg("Emergency Contact", tfEmerg));

        body.add(grid, BorderLayout.CENTER);

        JPanel br = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); br.setOpaque(false);
        br.add(btnClear);
        br.add(btnSave);

        body.add(br, BorderLayout.SOUTH);
        form.add(body, BorderLayout.CENTER);
        ReceptionistUIHelper.stack(p, form);
        return p;
    }

    private void showConfirmDialog(String name, String phone, String pid, String apptId) {
        JDialog conf = new JDialog(owner, "Patient Registered", true);
        conf.setSize(460, 290); conf.setLocationRelativeTo(owner);
        conf.setLayout(new BorderLayout());
        conf.add(ReceptionistUIHelper.gradHeader("✅  Registration Successful"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, 16));
        body.setBackground(ReceptionistUIHelper.C_WHITE);
        body.setBorder(new EmptyBorder(18, 22, 18, 22));

        JLabel info = new JLabel("<html><b>Patient:</b> " + name +
            "<br><b>Phone:</b> " + phone +
            "<br><br><small><i>They can now login with their email & password.</i></small></html>");
        info.setFont(ReceptionistUIHelper.F_BODY); info.setForeground(ReceptionistUIHelper.C_DARK);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0)); btns.setOpaque(false);
        JButton bA = ReceptionistUIHelper.primary("👨‍⚕️  Assign Doctor");
        JButton bD = ReceptionistUIHelper.ghost("Done");
        bA.addActionListener(ev -> { conf.dispose(); nav.navigateToAssignDoctor(name); });
        bD.addActionListener(ev -> conf.dispose());
        btns.add(bA); btns.add(bD);

        body.add(info,  BorderLayout.CENTER);
        body.add(btns,  BorderLayout.SOUTH);
        conf.add(body, BorderLayout.CENTER);
        conf.setVisible(true);
    }
}
