package Patient;

import System.HospitalSystem;
import java.awt.BasicStroke;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class PatientGUI {
   public static Patient patient;
   public static java.util.ArrayList<Patient> allPatients = new java.util.ArrayList<>();
   static final Color NAVY = new Color(4, 44, 83);
   static final Color BLUE = new Color(24, 95, 165);
   static final Color BLUE_MID = new Color(55, 138, 221);
   static final Color BLUE_LIGHT = new Color(181, 212, 244);
   static final Color BLUE_PALE = new Color(230, 241, 251);
   static final Color TEXT_DARK = new Color(4, 44, 83);
   static final Color TEXT_MID = new Color(24, 95, 165);
   static final Color TEXT_LIGHT = new Color(133, 183, 235);
   static final Color WHITE;
   static BufferedImage heartBg;
   public static boolean transitioning = false;

   public PatientGUI() {
   }

   public static void main(String[] var0) {
      patient = new Patient("PAT-SanaAhmed", "Sana Ahmed", "sana@email.com", "1234", "0300-9876543", 23, "A+");
      allPatients.add(patient);
      patient.addMedicalHistory("Diabetes - Type 2", "Jan 2022", "Dr. Ahmed Raza", "Metformin 500mg", "Ongoing");
      patient.addMedicalHistory("Penicillin Allergy", "May 2019", "Dr. Fatima Malik", "None", "Allergy");
      patient.addMedicalHistory("Hypertension", "Aug 2023", "Dr. Sarah Khan", "Amlodipine 5mg", "Managed");
      patient.bookAppointment("#A-0001", "Dr. Sarah Khan", "Cardiology", "2026-06-10", "10:00 AM");
      patient.bookAppointment("#A-0002", "Dr. Ahmed Raza", "General Medicine", "2026-06-10", "02:00 PM");
      patient.bookAppointment("#A-0003", "Dr. Fatima Malik", "Pediatrics", "2026-06-10", "11:30 AM");
      patient.bookAppointment("#A-0004", "Dr. Usman Tariq", "Orthopedics", "2026-06-20", "03:00 PM");
      patient.cancelAppointment("#A-0003");
      SwingUtilities.invokeLater(PatientGUI::showLogin);
   }

   public static void showLogin() {
      JFrame var0 = new JFrame("Smart Health - Patient Login");
      var0.setSize(1600, 900);
      var0.setExtendedState(6);
      var0.setLocationRelativeTo((Component)null);
      var0.setDefaultCloseOperation(2);
      var0.setResizable(false);
      var0.setLayout((LayoutManager)null);
      JPanel var1 = new JPanel((LayoutManager)null) {
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (heartBg != null) {
               g.drawImage(heartBg, 0, 0, 1600, 900, this);
            } else {
               GradientPaint gp = new GradientPaint(0, 0, new Color(4, 44, 83),
                     getWidth(), getHeight(), new Color(10, 25, 60));
               g2.setPaint(gp);
               g2.fillRect(0, 0, getWidth(), getHeight());
               g2.setColor(new Color(0, 160, 255, 20));
               g2.fillOval(100, 80, 350, 350);
               g2.setColor(new Color(0, 200, 255, 12));
               g2.fillOval(getWidth() - 400, 150, 400, 400);
            }
         }
      };
      var1.setBackground(NAVY);
      var1.setBounds(0, 0, 1600, 900);
      JLabel var5 = new JLabel("Smart Health Portal", 0);
      var5.setFont(new Font("Segoe UI", 1, 52));
      var5.setForeground(WHITE);
      var5.setBounds(120, 80, 520, 70);
      var1.add(var5);
      JLabel var6 = new JLabel("Patient Management System", 0);
      var6.setFont(new Font("Segoe UI", 1, 22));
      var6.setForeground(TEXT_LIGHT);
      var6.setBounds(120, 155, 520, 30);
      var1.add(var6);

      JPanel var21 = new JPanel((LayoutManager)null);
      var21.setBounds(120, 200, 520, 600);
      var21.setBackground(new Color(15, 75, 150, 200));
      var21.setBorder(BorderFactory.createLineBorder(new Color(55, 138, 221, 100), 1, true));
      var1.add(var21);
      JLabel var22 = new JLabel("Welcome back", 0);
      var22.setFont(new Font("Segoe UI", 1, 22));
      var22.setForeground(WHITE);
      var22.setBounds(0, 45, 520, 32);
      var21.add(var22);
      JLabel var23 = new JLabel("Sign in to your patient account", 0);
      var23.setFont(new Font("Segoe UI", 0, 14));
      var23.setForeground(TEXT_LIGHT);
      var23.setBounds(0, 82, 520, 24);
      var21.add(var23);
      JSeparator var24 = new JSeparator();
      var24.setBounds(30, 120, 460, 1);
      var24.setForeground(new Color(55, 138, 221, 100));
      var21.add(var24);
      JLabel var25 = fieldLabel("Email address");
      var25.setForeground(WHITE);
      var25.setBounds(30, 140, 460, 20);
      var21.add(var25);
      JTextField var26 = styledField();
      var26.setText("sana@email.com");
      var26.setBounds(30, 164, 460, 48);
      var21.add(var26);
      JLabel var27 = fieldLabel("Password");
      var27.setForeground(WHITE);
      var27.setBounds(30, 232, 460, 20);
      var21.add(var27);
      JPasswordField var28 = styledPass();
      var28.setBounds(30, 256, 460, 48);
      var21.add(var28);
      JButton var17 = primaryBtn("Sign In");
      var17.setBounds(30, 330, 460, 50);
      var21.add(var17);
      JButton var18 = secondaryBtn("New patient? Register here");
      var18.setBounds(30, 395, 460, 48);
      var21.add(var18);
      JPanel var19 = new JPanel((LayoutManager)null);
      var19.setBounds(30, 460, 460, 42);
      var19.setBackground(new Color(55, 138, 221, 60));
      var19.setBorder(BorderFactory.createLineBorder(new Color(55, 138, 221, 100), 1, true));
      JLabel var20 = new JLabel("  Demo: sana@email.com / 1234");
      var20.setFont(new Font("Segoe UI", 2, 12));
      var20.setForeground(TEXT_LIGHT);
      var20.setBounds(0, 0, 460, 42);
      var19.add(var20);
      var21.add(var19);
       var17.addActionListener((var3x) -> {
          String email = var26.getText().trim();
          String pass = (new String(var28.getPassword())).trim();
          Patient matched = null;
          for (Patient p : allPatients) {
             if (p.login(email, pass)) { matched = p; break; }
          }
          if (matched != null) {
             patient = matched;
             transitioning = true;
             var0.dispose();
             Dashboard.show(patient);
          } else {
             JOptionPane.showMessageDialog(var0, "Invalid email or password.", "Login Failed", 0);
          }
       });
      var18.addActionListener((var1x) -> {
         transitioning = true;
         var0.dispose();
         showRegister();
      });
      var0.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosed(java.awt.event.WindowEvent e) {
            if (!transitioning) HospitalSystem.showLauncher.run();
            transitioning = false;
         }
      });
      var0.setContentPane(var1);
      var0.setVisible(true);
   }

   public static void showRegister() {
      JFrame var0 = new JFrame("Smart Health - Register");
      var0.setSize(1600, 900);
      var0.setExtendedState(6);
      var0.setLocationRelativeTo((Component)null);
      var0.setDefaultCloseOperation(2);
      var0.setResizable(false);
      var0.setLayout((LayoutManager)null);
      JPanel var1 = new JPanel((LayoutManager)null) {
         protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            GradientPaint gp = new GradientPaint(0, 0, new Color(4, 44, 83),
                  getWidth(), getHeight(), new Color(10, 25, 60));
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
         }
      };
      var1.setBounds(0, 0, 1600, 900);

      JPanel var2 = new JPanel((LayoutManager)null);
      var2.setBounds(0, 0, 280, 900);
      var2.setBackground(new Color(4, 44, 83, 200));
      var1.add(var2);

      JLabel var5 = new JLabel("Create Account");
      var5.setFont(new Font("Segoe UI", 1, 24));
      var5.setForeground(WHITE);
      var5.setBounds(28, 80, 250, 32);
      var2.add(var5);
      JLabel var6 = new JLabel("Fill in your details below");
      var6.setFont(new Font("Segoe UI", 0, 14));
      var6.setForeground(TEXT_LIGHT);
      var6.setBounds(28, 118, 250, 20);
      var2.add(var6);

      CardLayout cardLayout = new CardLayout();
      JPanel card = new JPanel(cardLayout);
      card.setBounds(300, 50, 1080, 800);
      card.setBackground(WHITE);
      card.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var1.add(card);

      int[] currentStep = {0};

      // ── Navigation buttons ───────────────────────────────────────
      JButton[] navBtns = new JButton[2];
      String[] navLabels = new String[]{"Personal Info", "Medical Info"};
      for (int i = 0; i < 2; ++i) {
         final int fi = i;
         JButton btn = new JButton(navLabels[i]);
         btn.setBounds(20, 172 + i * 56, 240, 44);
         btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
         btn.setHorizontalAlignment(SwingConstants.LEFT);
         btn.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 10));
         btn.setFocusPainted(false);
         btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
         btn.setOpaque(true);
         if (i == 0) {
            btn.setBackground(BLUE_MID);
            btn.setForeground(WHITE);
         } else {
            btn.setBackground(new Color(8, 48, 88));
            btn.setForeground(TEXT_LIGHT);
         }
         btn.addActionListener(e -> switchRegisterStep(fi, currentStep, cardLayout, card, navBtns));
         var2.add(btn);
         navBtns[i] = btn;
      }

      // ═══════════════════════════════════════════════════════════════
      //  PERSONAL INFO CARD
      // ═══════════════════════════════════════════════════════════════
      JPanel personalCard = new JPanel((LayoutManager)null);
      personalCard.setBackground(WHITE);

      JLabel title = new JLabel("Personal Information", 0);
      title.setFont(new Font("Segoe UI", 1, 24));
      title.setForeground(TEXT_DARK);
      title.setBounds(0, 28, 1080, 35);
      personalCard.add(title);

      JLabel subtitle = new JLabel("Enter your personal and contact details", 0);
      subtitle.setFont(new Font("Segoe UI", 0, 15));
      subtitle.setForeground(TEXT_MID);
      subtitle.setBounds(0, 66, 1080, 22);
      personalCard.add(subtitle);

      int lx = 30, rx = 560, cw = 490, lh = 22, fh = 42, y = 110, gap = 72;

      JLabel nameLbl = new JLabel("FULL NAME:");
      nameLbl.setFont(new Font("Segoe UI", 1, 15));
      nameLbl.setForeground(TEXT_DARK);
      nameLbl.setBounds(lx, y, cw, lh);
      personalCard.add(nameLbl);
      JLabel ageLbl = new JLabel("AGE:");
      ageLbl.setFont(new Font("Segoe UI", 1, 15));
      ageLbl.setForeground(TEXT_DARK);
      ageLbl.setBounds(rx, y, cw, lh);
      personalCard.add(ageLbl);

      JTextField nameFld = styledField();
      nameFld.setFont(new Font("Segoe UI", 0, 16));
      nameFld.setBounds(lx, y + lh + 4, cw, fh);
      javax.swing.text.PlainDocument nameDoc = new javax.swing.text.PlainDocument() {
          public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
              if (str == null) return;
              for (int i = 0; i < str.length(); i++) { char c = str.charAt(i); if (!Character.isLetter(c) && c != ' ') return; }
              super.insertString(offs, str, a);
          }
          public void replace(int offs, int len, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
              if (str == null) return;
              for (int i = 0; i < str.length(); i++) { char c = str.charAt(i); if (!Character.isLetter(c) && c != ' ') return; }
              super.replace(offs, len, str, a);
          }
      };
      nameFld.setDocument(nameDoc);
      nameFld.addKeyListener(new java.awt.event.KeyAdapter() {
          public void keyTyped(java.awt.event.KeyEvent e) {
              char c = e.getKeyChar();
              if (!Character.isLetter(c) && c != ' ' && !Character.isISOControl(c)) e.consume();
          }
      });
      personalCard.add(nameFld);
      JTextField ageFld = styledField();
      ageFld.setFont(new Font("Segoe UI", 0, 16));
      ageFld.setBounds(rx, y + lh + 4, cw, fh);
      personalCard.add(ageFld);

      y += gap;
      JLabel emailLbl = new JLabel("EMAIL ADDRESS:");
      emailLbl.setFont(new Font("Segoe UI", 1, 15));
      emailLbl.setForeground(TEXT_DARK);
      emailLbl.setBounds(lx, y, cw, lh);
      personalCard.add(emailLbl);
      JLabel phoneLbl = new JLabel("PHONE NUMBER:");
      phoneLbl.setFont(new Font("Segoe UI", 1, 15));
      phoneLbl.setForeground(TEXT_DARK);
      phoneLbl.setBounds(rx, y, cw, lh);
      personalCard.add(phoneLbl);

      JTextField emailFld = styledField();
      emailFld.setFont(new Font("Segoe UI", 0, 16));
      emailFld.setBounds(lx, y + lh + 4, cw, fh);
      personalCard.add(emailFld);
      JTextField phoneFld = styledField();
      phoneFld.setFont(new Font("Segoe UI", 0, 16));
      phoneFld.setBounds(rx, y + lh + 4, cw, fh);
      javax.swing.text.PlainDocument phoneDoc = new javax.swing.text.PlainDocument() {
          public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
              if (str == null || (getLength() + str.length()) > 11) return;
              for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
              super.insertString(offs, str, a);
          }
          public void replace(int offs, int len, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
              if (str == null || (getLength() - len + str.length()) > 11) return;
              for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
              super.replace(offs, len, str, a);
          }
      };
      phoneFld.setDocument(phoneDoc);
      phoneFld.addKeyListener(new java.awt.event.KeyAdapter() {
          public void keyTyped(java.awt.event.KeyEvent e) {
              if (!Character.isDigit(e.getKeyChar()) && !Character.isISOControl(e.getKeyChar())) e.consume();
          }
      });
      phoneDoc.addDocumentListener(new javax.swing.event.DocumentListener() {
          public void insertUpdate(javax.swing.event.DocumentEvent e) {
              javax.swing.text.Document d = e.getDocument();
              if (d.getLength() > 11) { try { d.remove(11, d.getLength() - 11); } catch (javax.swing.text.BadLocationException ex) {} }
          }
          public void removeUpdate(javax.swing.event.DocumentEvent e) {}
          public void changedUpdate(javax.swing.event.DocumentEvent e) {}
      });
      personalCard.add(phoneFld);

      y += gap;
      JLabel passLbl = new JLabel("PASSWORD");
      passLbl.setFont(new Font("Segoe UI", 1, 15));
      passLbl.setForeground(TEXT_DARK);
      passLbl.setBounds(lx, y, cw, lh);
      personalCard.add(passLbl);
      JLabel bloodLbl = new JLabel("BLOOD GROUP:");
      bloodLbl.setFont(new Font("Segoe UI", 1, 15));
      bloodLbl.setForeground(TEXT_DARK);
      bloodLbl.setBounds(rx, y, cw, lh);
      personalCard.add(bloodLbl);

      JPasswordField passFld = styledPass();
      passFld.setFont(new Font("Segoe UI", 0, 16));
      passFld.setBounds(lx, y + lh + 4, cw, fh);
      personalCard.add(passFld);
      JTextField bloodFld = styledField();
      bloodFld.setFont(new Font("Segoe UI", 0, 16));
      bloodFld.setBounds(rx, y + lh + 4, cw, fh);
      personalCard.add(bloodFld);

      y += gap;
      JLabel genderLbl = new JLabel("GENDER:");
      genderLbl.setFont(new Font("Segoe UI", 1, 15));
      genderLbl.setForeground(TEXT_DARK);
      genderLbl.setBounds(lx, y, cw, lh);
      personalCard.add(genderLbl);
      JLabel addrLbl = new JLabel("ADDRESS:");
      addrLbl.setFont(new Font("Segoe UI", 1, 15));
      addrLbl.setForeground(TEXT_DARK);
      addrLbl.setBounds(rx, y, cw, lh);
      personalCard.add(addrLbl);

      JComboBox<String> genderFld = new JComboBox<>(new String[]{"Male", "Female"});
      genderFld.setFont(new Font("Segoe UI", 0, 16));
      genderFld.setBounds(lx, y + lh + 4, cw, fh);
      genderFld.setBackground(Color.WHITE);
      personalCard.add(genderFld);
      JTextField addrFld = styledField();
      addrFld.setFont(new Font("Segoe UI", 0, 16));
      addrFld.setBounds(rx, y + lh + 4, cw, fh);
      personalCard.add(addrFld);

      y += gap;
      int emX = 190, emW = 700;
      JLabel emLbl = new JLabel("EMERGENCY CONTACT:");
      emLbl.setFont(new Font("Segoe UI", 1, 15));
      emLbl.setForeground(TEXT_DARK);
      emLbl.setBounds(emX, y, emW, lh);
      personalCard.add(emLbl);

      JTextField emFld = styledField();
      emFld.setFont(new Font("Segoe UI", 0, 16));
      emFld.setBounds(emX, y + lh + 4, emW, fh);
      javax.swing.text.PlainDocument emDoc = new javax.swing.text.PlainDocument() {
          public void insertString(int offs, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
              if (str == null || (getLength() + str.length()) > 11) return;
              for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
              super.insertString(offs, str, a);
          }
          public void replace(int offs, int len, String str, javax.swing.text.AttributeSet a) throws javax.swing.text.BadLocationException {
              if (str == null || (getLength() - len + str.length()) > 11) return;
              for (int i = 0; i < str.length(); i++) if (!Character.isDigit(str.charAt(i))) return;
              super.replace(offs, len, str, a);
          }
      };
      emFld.setDocument(emDoc);
      emFld.addKeyListener(new java.awt.event.KeyAdapter() {
          public void keyTyped(java.awt.event.KeyEvent e) {
              if (!Character.isDigit(e.getKeyChar()) && !Character.isISOControl(e.getKeyChar())) e.consume();
          }
      });
      emDoc.addDocumentListener(new javax.swing.event.DocumentListener() {
          public void insertUpdate(javax.swing.event.DocumentEvent e) {
              javax.swing.text.Document d = e.getDocument();
              if (d.getLength() > 11) { try { d.remove(11, d.getLength() - 11); } catch (javax.swing.text.BadLocationException ex) {} }
          }
          public void removeUpdate(javax.swing.event.DocumentEvent e) {}
          public void changedUpdate(javax.swing.event.DocumentEvent e) {}
      });
      personalCard.add(emFld);

      JButton nextBtn = primaryBtn("Next >");
      nextBtn.setFont(new Font("Segoe UI", 1, 17));
      nextBtn.setBounds(290, y + gap, 500, 52);
      personalCard.add(nextBtn);

      card.add(personalCard, "Personal");

      // ═══════════════════════════════════════════════════════════════
      //  MEDICAL INFO CARD
      // ═══════════════════════════════════════════════════════════════
      JPanel medicalCard = new JPanel((LayoutManager)null);
      medicalCard.setBackground(WHITE);

      JLabel medTitle = new JLabel("Medical Information", 0);
      medTitle.setFont(new Font("Segoe UI", 1, 24));
      medTitle.setForeground(TEXT_DARK);
      medTitle.setBounds(0, 28, 1080, 35);
      medicalCard.add(medTitle);

      JLabel medSub = new JLabel("Tell us about your medical background", 0);
      medSub.setFont(new Font("Segoe UI", 0, 15));
      medSub.setForeground(TEXT_MID);
      medSub.setBounds(0, 66, 1080, 22);
      medicalCard.add(medSub);

      int mlx = 80, mw = 920, my = 120, mgap = 80;
      JLabel chronicLbl = new JLabel("ANY CHRONIC CONDITION:");
      chronicLbl.setFont(new Font("Segoe UI", 1, 15));
      chronicLbl.setForeground(TEXT_DARK);
      chronicLbl.setBounds(mlx, my, mw, lh);
      medicalCard.add(chronicLbl);
      JTextField chronicFld = styledField();
      chronicFld.setFont(new Font("Segoe UI", 0, 16));
      chronicFld.setBounds(mlx, my + lh + 4, mw, fh);
      medicalCard.add(chronicFld);

      my += mgap;
      JLabel familyLbl = new JLabel("FAMILY HISTORY:");
      familyLbl.setFont(new Font("Segoe UI", 1, 15));
      familyLbl.setForeground(TEXT_DARK);
      familyLbl.setBounds(mlx, my, mw, lh);
      medicalCard.add(familyLbl);
      JTextField familyFld = styledField();
      familyFld.setFont(new Font("Segoe UI", 0, 16));
      familyFld.setBounds(mlx, my + lh + 4, mw, fh);
      medicalCard.add(familyFld);

      my += mgap;
      JLabel surgeryLbl = new JLabel("PAST SURGERY:");
      surgeryLbl.setFont(new Font("Segoe UI", 1, 15));
      surgeryLbl.setForeground(TEXT_DARK);
      surgeryLbl.setBounds(mlx, my, mw, lh);
      medicalCard.add(surgeryLbl);
      JTextField surgeryFld = styledField();
      surgeryFld.setFont(new Font("Segoe UI", 0, 16));
      surgeryFld.setBounds(mlx, my + lh + 4, mw, fh);
      medicalCard.add(surgeryFld);

      my += mgap;
      JLabel vaccLbl = new JLabel("VACCINATIONS:");
      vaccLbl.setFont(new Font("Segoe UI", 1, 15));
      vaccLbl.setForeground(TEXT_DARK);
      vaccLbl.setBounds(mlx, my, mw, lh);
      medicalCard.add(vaccLbl);
      JTextField vaccFld = styledField();
      vaccFld.setFont(new Font("Segoe UI", 0, 16));
      vaccFld.setBounds(mlx, my + lh + 4, mw, fh);
      medicalCard.add(vaccFld);

      my += mgap + 20;
      JButton prevBtn = secondaryBtn("<  Back");
      prevBtn.setFont(new Font("Segoe UI", 0, 16));
      prevBtn.setBounds(mlx, my, 300, 52);
      medicalCard.add(prevBtn);

      JButton regBtn = primaryBtn("Register");
      regBtn.setFont(new Font("Segoe UI", 1, 17));
      regBtn.setBounds(mlx + 320, my, 300, 52);
      medicalCard.add(regBtn);

      card.add(medicalCard, "Medical");

      // ── Actions ──────────────────────────────────────────────────
      nextBtn.addActionListener(e -> {
         switchRegisterStep(1, currentStep, cardLayout, card, navBtns);
      });

      prevBtn.addActionListener(e -> {
         switchRegisterStep(0, currentStep, cardLayout, card, navBtns);
      });

      regBtn.addActionListener((var7x) -> {
         String name = nameFld.getText().trim();
         String email = emailFld.getText().trim();
         String pass = (new String(passFld.getPassword())).trim();
         String phone = phoneFld.getText().trim();
         String blood = bloodFld.getText().trim();
         String ageStr = ageFld.getText().trim();
         String emerg = emFld.getText().trim();
         if (!name.isEmpty() && !email.isEmpty() && !pass.isEmpty() && !phone.isEmpty() && !blood.isEmpty() && !ageStr.isEmpty() && !emerg.isEmpty()) {
            if (!name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(var0, "Name must contain only letters and spaces.", "Error", 0);
                return;
            }
            if (phone.length() != 11) {
                JOptionPane.showMessageDialog(var0, "Phone number must be exactly 11 digits.", "Error", 0);
                return;
            }
            if (emerg.length() != 11) {
                JOptionPane.showMessageDialog(var0, "Emergency contact must be exactly 11 digits.", "Error", 0);
                return;
            }
            try {
                Patient newPatient = new Patient("PAT-" + name.replace(" ", ""), name, email, pass, phone, Integer.parseInt(ageStr), blood);
                newPatient.setGender((String) genderFld.getSelectedItem());
                // Save medical info
                String chronic = chronicFld.getText().trim();
                String family  = familyFld.getText().trim();
                String surgery = surgeryFld.getText().trim();
                String vacc    = vaccFld.getText().trim();
                if (!chronic.isEmpty()) newPatient.addMedicalHistory("Chronic Condition: " + chronic, "Recorded", "Self", "--", "Ongoing");
                if (!family.isEmpty())  newPatient.addMedicalHistory("Family History: " + family,    "Recorded", "Self", "--", "Info");
                if (!surgery.isEmpty()) newPatient.addMedicalHistory("Past Surgery: " + surgery,    "Recorded", "Self", "--", "Surgery");
                if (!vacc.isEmpty())    newPatient.addMedicalHistory("Vaccinations: " + vacc,       "Recorded", "Self", "--", "Vaccine");
                // Sync medical info to doctor data store
                Doctor.doctorDataStore dds = Doctor.doctorDataStore.get();
                dds.updateMedicalHistory(name, "ChronicConditions", chronic.isEmpty() ? "None" : chronic);
                dds.updateMedicalHistory(name, "FamilyHistory",     family.isEmpty()  ? "None" : family);
                dds.updateMedicalHistory(name, "Surgeries",         surgery.isEmpty() ? "None" : surgery);
                dds.updateMedicalHistory(name, "Vaccinations",      vacc.isEmpty()    ? "None" : vacc);
                newPatient.registerPatient();
                allPatients.add(newPatient);
                Admin.HospitalAdmin.registerPatient("PAT-" + name.replace(" ", ""), name, "General", "Unassigned");
                HospitalSystem.syncPatientRegistrationFromPortal(name, email, phone, Integer.parseInt(ageStr), (String) genderFld.getSelectedItem(), blood);
                JOptionPane.showMessageDialog(var0, "Account created successfully!\nPlease login.", "Success", 1);
               var0.dispose();
               showLogin();
            } catch (NumberFormatException var15x) {
               JOptionPane.showMessageDialog(var0, "Age must be a number.", "Error", 0);
            }
         } else {
            JOptionPane.showMessageDialog(var0, "Please fill all required fields.", "Error", 0);
         }
      });

      var0.addWindowListener(new java.awt.event.WindowAdapter() {
         public void windowClosed(java.awt.event.WindowEvent e) {
            if (!transitioning) HospitalSystem.showLauncher.run();
            transitioning = false;
         }
      });
      var0.setContentPane(var1);
      var0.setVisible(true);
   }

   private static void switchRegisterStep(int step, int[] currentStep, CardLayout cl, JPanel card,
         JButton[] btns) {
      currentStep[0] = step;
      cl.show(card, step == 0 ? "Personal" : "Medical");
      for (int i = 0; i < 2; i++) {
         boolean active = i <= step;
         btns[i].setBackground(active ? BLUE_MID : new Color(255, 255, 255, 15));
         btns[i].setForeground(active ? WHITE : TEXT_LIGHT);
         btns[i].setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
      }
   }

   public static JLabel fieldLabel(String var0) {
      JLabel var1 = new JLabel(var0);
      var1.setFont(new Font("Segoe UI", 1, 12));
      var1.setForeground(TEXT_MID);
      return var1;
   }

   public static JTextField styledField() {
      JTextField var0 = new JTextField();
      var0.setFont(new Font("Segoe UI", 0, 14));
      var0.setForeground(TEXT_DARK);
      var0.setBackground(BLUE_PALE);
      var0.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true), BorderFactory.createEmptyBorder(0, 12, 0, 12)));
      var0.setCursor(Cursor.getPredefinedCursor(2));
      return var0;
   }

   public static JPasswordField styledPass() {
      JPasswordField var0 = new JPasswordField();
      var0.setFont(new Font("Segoe UI", 0, 14));
      var0.setForeground(TEXT_DARK);
      var0.setBackground(BLUE_PALE);
      var0.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true), BorderFactory.createEmptyBorder(0, 12, 0, 12)));
      var0.setCursor(Cursor.getPredefinedCursor(2));
      return var0;
   }

   public static JButton primaryBtn(String var0) {
      final JButton var1 = new JButton(var0);
      var1.setFont(new Font("Segoe UI", 1, 14));
      var1.setBackground(BLUE);
      var1.setForeground(WHITE);
      var1.setBorderPainted(false);
      var1.setFocusPainted(false);
      var1.setOpaque(true);
      var1.setCursor(Cursor.getPredefinedCursor(12));
      var1.addMouseListener(new MouseAdapter() {
         public void mouseEntered(MouseEvent var1x) {
            var1.setBackground(new Color(12, 68, 124));
         }

         public void mouseExited(MouseEvent var1x) {
            var1.setBackground(PatientGUI.BLUE);
         }
      });
      return var1;
   }

   public static JButton secondaryBtn(String var0) {
      final JButton var1 = new JButton(var0);
      var1.setFont(new Font("Segoe UI", 0, 13));
      var1.setBackground(BLUE_PALE);
      var1.setForeground(TEXT_MID);
      var1.setFocusPainted(false);
      var1.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var1.setOpaque(true);
      var1.setCursor(Cursor.getPredefinedCursor(12));
      var1.addMouseListener(new MouseAdapter() {
         public void mouseEntered(MouseEvent var1x) {
            var1.setBackground(PatientGUI.BLUE_LIGHT);
         }

         public void mouseExited(MouseEvent var1x) {
            var1.setBackground(PatientGUI.BLUE_PALE);
         }
      });
      return var1;
   }

   public static JLabel statusPill(String var0) {
      JLabel var1 = new JLabel("  " + var0 + "  ", 0);
      var1.setFont(new Font("Segoe UI", 1, 11));
      var1.setOpaque(true);
      switch (var0) {
          case "Scheduled":
          case "Accepted":
          case "Completed":
            var1.setBackground(new Color(234, 243, 222));
            var1.setForeground(new Color(59, 109, 17));
            break;
         case "Pending":
         case "Rescheduled":
            var1.setBackground(new Color(250, 238, 218));
            var1.setForeground(new Color(133, 79, 11));
            break;
         case "Cancelled":
         case "Rejected":
            var1.setBackground(new Color(252, 235, 235));
            var1.setForeground(new Color(163, 45, 45));
            break;
         default:
            var1.setBackground(new Color(250, 238, 218));
            var1.setForeground(new Color(133, 79, 11));
      }

      var1.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
      var1.setCursor(Cursor.getPredefinedCursor(12));
      return var1;
   }

   public static JPanel whiteCard(int var0, int var1, int var2, int var3) {
      JPanel var4 = new JPanel((LayoutManager)null);
      var4.setBounds(var0, var1, var2, var3);
      var4.setBackground(WHITE);
      var4.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      return var4;
   }

   public static JLabel cardTitle(String var0) {
      JLabel var1 = new JLabel(var0);
      var1.setFont(new Font("Segoe UI", 1, 15));
      var1.setForeground(TEXT_DARK);
      return var1;
   }

   public static JLabel cardSub(String var0) {
      JLabel var1 = new JLabel(var0);
      var1.setFont(new Font("Segoe UI", 0, 12));
      var1.setForeground(TEXT_MID);
      return var1;
   }

   static {
      WHITE = Color.WHITE;
      try {
         heartBg = ImageIO.read(new File("Patient\\human.png"));
      } catch (Exception e) {
         heartBg = null;
      }
   }
}