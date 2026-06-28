package Patient;

import System.DoctorRosterStore;
import System.HospitalSystem;
import Receptionist.ReceptionistDataStore;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class BookAppointmentPanel {
   static final Color NAVY = new Color(4, 44, 83);
   static final Color BLUE = new Color(24, 95, 165);
   static final Color BLUE_MID = new Color(55, 138, 221);
   static final Color BLUE_LIGHT = new Color(181, 212, 244);
   static final Color BLUE_PALE = new Color(230, 241, 251);
   static final Color TEXT_DARK = new Color(4, 44, 83);
   static final Color TEXT_MID = new Color(24, 95, 165);
   static final Color TEXT_LIGHT = new Color(133, 183, 235);
    static final Color WHITE;
    static final Color BG;
    static String[][] DOCTORS;
    static String[] SPECIALTIES;
    static JPanel mainContainer;
    static String[] TIME_SLOTS;
    static String filterSpec;
    static String searchText;
    static String selectedSlot;
    static String[] currentDoc;
    static String[] currentSlots;
    static Set<String> bookedSlots = new HashSet<>();
    static Patient patient;
    static JFrame frame;
    static JButton[] slotBtns;
    static int selectedDay;
    static Calendar cal;
    static JLabel monthLbl;
    static JPanel daysGrid;
    static boolean showingConfirm;
    static JTextField searchField;
   static JScrollPane listScroll;

   public BookAppointmentPanel() {
   }

   static String slotKey(String doctorName, String date, String timeSlot) {
      return doctorName + "|" + date + "|" + timeSlot;
   }

   static void initBookedSlots() {
      bookedSlots.clear();
      for (Appointment a : patient.getAppointments()) {
         if (!a.getStatus().equals("Cancelled") && !a.getStatus().equals("Rejected") && !a.getStatus().equals("Completed")) {
            bookedSlots.add(slotKey(a.getDoctorName(), a.getDate(), a.getTimeSlot()));
         }
      }
      javax.swing.table.DefaultTableModel am = Doctor.doctorDataStore.get().getAppointmentModel();
      for (int i = 0; i < am.getRowCount(); i++) {
         Object d = am.getValueAt(i, 0);
         Object t = am.getValueAt(i, 1);
         Object dt = am.getValueAt(i, 2);
         Object dr = am.getValueAt(i, 3);
         if (d != null && t != null && dt != null && dr != null) {
            String statusKey = d.toString() + "|" + dt.toString() + "|" + t.toString() + "|" + dr.toString();
            String status = Doctor.doctorDataStore.get().patientAppointmentStatus.get(statusKey);
             if (!"Completed".equals(status) && !"Cancelled".equals(status) && !"Rejected".equals(status)) {
               bookedSlots.add(slotKey(dr.toString(), dt.toString(), t.toString()));
            }
         }
      }
      bookedSlots.addAll(HospitalSystem.getBookedSlotKeys());
   }

   public static JPanel build(Patient var0, JFrame var1) {
      patient = var0;
      frame = var1;
      filterSpec = null;
      searchText = "";
      selectedSlot = null;
      selectedDay = -1;
      cal = Calendar.getInstance();
      showingConfirm = false;
      currentDoc = null;
      initBookedSlots();
      mainContainer = new JPanel((LayoutManager)null) {
         public Dimension getPreferredSize() {
            int mx = 600, my = 0;
            for (java.awt.Component c : getComponents()) {
               mx = Math.max(mx, c.getX() + c.getWidth());
               my = Math.max(my, c.getY() + c.getHeight());
            }
            return new Dimension(mx + 14, my + 14);
         }
      };
      mainContainer.setBackground(BG);
      mainContainer.addComponentListener(new ComponentAdapter() {
         public void componentResized(ComponentEvent var1) {
            if (BookAppointmentPanel.showingConfirm && BookAppointmentPanel.currentDoc != null) {
               BookAppointmentPanel.layoutConfirmView(BookAppointmentPanel.currentDoc);
            } else {
               BookAppointmentPanel.layoutDoctorList();
            }

         }
      });
      showDoctorList();
      return mainContainer;
   }

   static int w() {
      return mainContainer.getWidth();
   }

   static void showDoctorList() {
      showingConfirm = false;
      currentDoc = null;
      mainContainer.removeAll();
      layoutDoctorList();
      mainContainer.revalidate();
      mainContainer.repaint();
   }

   static void layoutDoctorList() {
      mainContainer.removeAll();
      int var0 = Math.max(w(), 600);
      byte var1 = 14;
      searchField = new JTextField();
      searchField.setFont(new Font("Segoe UI", 0, 13));
      searchField.setForeground(TEXT_DARK);
      searchField.setBackground(WHITE);
      searchField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true), BorderFactory.createEmptyBorder(0, 12, 0, 12)));
      int var2 = Math.min(var0 - var1 * 2, 500);
      searchField.setBounds(var1, var1, var2, 40);
      mainContainer.add(searchField);
      if (searchText.isEmpty()) {
         searchField.setForeground(TEXT_LIGHT);
         searchField.setText("Search by name or specialty...");
      } else {
         searchField.setText(searchText);
      }

      searchField.addFocusListener(new FocusAdapter() {
         public void focusGained(FocusEvent var1) {
            if (BookAppointmentPanel.searchField.getText().equals("Search by name or specialty...")) {
               BookAppointmentPanel.searchField.setText("");
               BookAppointmentPanel.searchField.setForeground(BookAppointmentPanel.TEXT_DARK);
            }

         }

         public void focusLost(FocusEvent var1) {
            if (BookAppointmentPanel.searchField.getText().isEmpty()) {
               BookAppointmentPanel.searchField.setForeground(BookAppointmentPanel.TEXT_LIGHT);
               BookAppointmentPanel.searchField.setText("Search by name or specialty...");
            }

         }
      });
      searchField.getDocument().addDocumentListener(new DocumentListener() {
         public void insertUpdate(DocumentEvent var1) {
            this.onSearch();
         }

         public void removeUpdate(DocumentEvent var1) {
            this.onSearch();
         }

         public void changedUpdate(DocumentEvent var1) {
            this.onSearch();
         }

         void onSearch() {
            String var1 = BookAppointmentPanel.searchField.getText().trim();
            if (!var1.equals("Search by name or specialty...") && !var1.equals(BookAppointmentPanel.searchText)) {
               BookAppointmentPanel.searchText = var1.toLowerCase();
               BookAppointmentPanel.rebuildCardList();
            }

         }
      });
      int var3 = var1;

      for(int var4 = 0; var4 < SPECIALTIES.length; ++var4) {
         String var5 = SPECIALTIES[var4];
         boolean var7 = filterSpec == null && var4 == 0 || var5.equals(filterSpec);
         JButton var8 = new JButton(var5);
         var8.setFont(new Font("Segoe UI", var7 ? 1 : 0, 12));
         var8.setBackground(var7 ? BLUE : WHITE);
         var8.setForeground(var7 ? WHITE : TEXT_MID);
         var8.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
         var8.setFocusPainted(false);
         var8.setOpaque(true);
         var8.setCursor(Cursor.getPredefinedCursor(12));
         int var9 = var5.length() * 8 + 26;
         var8.setBounds(var3, 64, var9, 32);
         mainContainer.add(var8);
         var3 += var9 + 8;
          final int specIdx = var4;
          var8.addActionListener((var2x) -> {
             filterSpec = specIdx == 0 ? null : var5;
             searchText = "";
             showDoctorList();
          });
      }

      JLabel var10 = new JLabel("Available Doctors");
      var10.setFont(new Font("Segoe UI", 1, 14));
      var10.setForeground(TEXT_DARK);
      var10.setBounds(var1, 108, 400, 22);
      mainContainer.add(var10);
      JPanel var11 = new JPanel((LayoutManager)null);
      var11.setBackground(BG);
      buildDoctorCards(var11, var0);
      listScroll = new JScrollPane(var11);
      listScroll.setBounds(var1, 138, var0 - var1 * 2, Math.max(100, mainContainer.getHeight() - 170));
      listScroll.setBorder(BorderFactory.createEmptyBorder());
      listScroll.setOpaque(false);
      listScroll.getViewport().setOpaque(false);
      listScroll.getVerticalScrollBar().setUnitIncrement(16);
      mainContainer.add(listScroll);
      mainContainer.revalidate();
      mainContainer.repaint();
   }

   static void rebuildCardList() {
      if (listScroll != null) {
         JPanel var0 = new JPanel((LayoutManager)null);
         var0.setBackground(BG);
         buildDoctorCards(var0, w());
         listScroll.setViewportView(var0);
         listScroll.revalidate();
         listScroll.repaint();
      }
   }

   static void buildDoctorCards(JPanel var0, int var1) {
      int var2 = 0;
      int var3 = 0;
      int var4 = var1 - 28;

      for(String[] var8 : DOCTORS) {
         if ((filterSpec == null || var8[1].equals(filterSpec)) && (searchText.isEmpty() || searchText.equals("search by name or specialty...") || var8[0].toLowerCase().contains(searchText) || var8[1].toLowerCase().contains(searchText))) {
            JPanel var9 = new JPanel((LayoutManager)null);
            var9.setBounds(0, var2, var4, 74);
            var9.setBackground(WHITE);
            var9.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
            JPanel var10 = makeAvatar(var8[0], 44);
            var10.setBounds(12, 15, 44, 44);
            var9.add(var10);
            JLabel var11 = new JLabel(var8[0]);
            var11.setFont(new Font("Segoe UI", 1, 14));
            var11.setForeground(TEXT_DARK);
            var11.setBounds(68, 10, 500, 22);
            var9.add(var11);
            JLabel var12 = new JLabel(var8[1]);
            var12.setFont(new Font("Segoe UI", 0, 12));
            var12.setForeground(TEXT_MID);
            var12.setBounds(68, 32, 400, 18);
            var9.add(var12);
            JLabel var13 = new JLabel("Experience: " + var8[2]);
            var13.setFont(new Font("Segoe UI", 0, 11));
            var13.setForeground(TEXT_LIGHT);
            var13.setBounds(68, 50, 300, 16);
            var9.add(var13);
            int var14 = var4 - 140;
            if (var14 < 400) {
               var14 = var4 - 140;
            }

            final JButton var15 = new JButton("View & Book");
            var15.setFont(new Font("Segoe UI", 1, 12));
            var15.setBackground(BLUE);
            var15.setForeground(WHITE);
            var15.setBorderPainted(false);
            var15.setFocusPainted(false);
            var15.setOpaque(true);
            var15.setBounds(var14, 20, 118, 34);
            var15.setCursor(Cursor.getPredefinedCursor(12));
            var15.addMouseListener(new MouseAdapter() {
               public void mouseEntered(MouseEvent var1) {
                  var15.setBackground(BookAppointmentPanel.NAVY);
               }

               public void mouseExited(MouseEvent var1) {
                  var15.setBackground(BookAppointmentPanel.BLUE);
               }
            });
            var9.add(var15);
            var15.addActionListener((var1x) -> {
               selectedSlot = null;
               selectedDay = -1;
               cal = Calendar.getInstance();
               showConfirmView(var8);
            });
            var0.add(var9);
            var2 += 80;
            ++var3;
         }
      }

      if (var3 == 0) {
         JLabel var17 = new JLabel("No doctors found. Try a different search.", 0);
         var17.setFont(new Font("Segoe UI", 0, 14));
         var17.setForeground(TEXT_LIGHT);
         var17.setBounds(0, 30, var4, 30);
         var0.add(var17);
         var2 = 70;
      }

      var0.setPreferredSize(new Dimension(var4, var2 + 10));
   }

     static void showConfirmView(String[] var0) {
        showingConfirm = true;
        currentDoc = var0;
        selectedSlot = null;
        selectedDay = -1;
        HospitalSystem.syncDoctorStatusToPatient();
        initBookedSlots();
        java.util.List<String> slots = DoctorRosterStore.getExpandedTimeSlotsByName(var0[0]);
       currentSlots = slots.isEmpty() ? TIME_SLOTS : slots.toArray(new String[0]);
       mainContainer.removeAll();
       layoutConfirmView(var0);
       mainContainer.revalidate();
       mainContainer.repaint();
    }

   static void layoutConfirmView(String[] var0) {
      mainContainer.removeAll();
      int var1 = Math.max(w(), 600);
      byte var2 = 14;
      int var3 = (var1 - var2 * 3) / 2;
      int var4 = var1 - var2 * 2;
      JButton var5 = new JButton("  Back");
      var5.setFont(new Font("Segoe UI", 1, 12));
      var5.setBackground(BLUE_PALE);
      var5.setForeground(TEXT_DARK);
      var5.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var5.setFocusPainted(false);
      var5.setOpaque(true);
      var5.setBounds(var2, var2, 80, 32);
      var5.setCursor(Cursor.getPredefinedCursor(12));
      mainContainer.add(var5);
      var5.addActionListener((var0x) -> showDoctorList());
      JLabel var6 = new JLabel("Confirm Appointment");
      var6.setFont(new Font("Segoe UI", 1, 17));
      var6.setForeground(TEXT_DARK);
      var6.setBounds(110, var2 + 2, 400, 28);
      mainContainer.add(var6);
      JPanel var7 = new JPanel((LayoutManager)null);
      var7.setBounds(var2, 56, var4, 62);
      var7.setBackground(WHITE);
      var7.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      mainContainer.add(var7);
      JPanel var8 = makeAvatar(var0[0], 42);
      var8.setBounds(12, 10, 42, 42);
      var7.add(var8);
      JLabel var9 = new JLabel(var0[0]);
      var9.setFont(new Font("Segoe UI", 1, 14));
      var9.setForeground(TEXT_DARK);
      var9.setBounds(64, 10, 400, 22);
      var7.add(var9);
      JLabel var10 = new JLabel(var0[1] + "   Experience: " + var0[2]);
      var10.setFont(new Font("Segoe UI", 0, 12));
      var10.setForeground(TEXT_MID);
      var10.setBounds(64, 32, 400, 18);
      var7.add(var10);
      short var11 = 420;
      JPanel var12 = new JPanel((LayoutManager)null);
      var12.setBounds(var2, 130, var3, var11);
      var12.setBackground(WHITE);
      var12.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      mainContainer.add(var12);
      JLabel var14 = new JLabel("Select a Date");
      var14.setFont(new Font("Segoe UI", 1, 13));
      var14.setForeground(TEXT_DARK);
      var14.setBounds(var2, 12, 200, 20);
      var12.add(var14);
      monthLbl = new JLabel("", 0);
      monthLbl.setFont(new Font("Segoe UI", 1, 12));
      monthLbl.setForeground(TEXT_DARK);
      monthLbl.setBounds(var3 / 2 - 90, 12, 180, 20);
      var12.add(monthLbl);
      JButton var15 = new JButton("<");
      var15.setFont(new Font("Segoe UI", 1, 11));
      var15.setBackground(BLUE_PALE);
      var15.setForeground(TEXT_DARK);
      var15.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var15.setFocusPainted(false);
      var15.setBounds(var3 - 80, 10, 28, 24);
      var15.setCursor(Cursor.getPredefinedCursor(12));
      var12.add(var15);
      JButton var16 = new JButton(">");
      var16.setFont(new Font("Segoe UI", 1, 11));
      var16.setBackground(BLUE_PALE);
      var16.setForeground(TEXT_DARK);
      var16.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var16.setFocusPainted(false);
      var16.setBounds(var3 - 46, 10, 28, 24);
      var16.setCursor(Cursor.getPredefinedCursor(12));
      var12.add(var16);
      int var17 = var3 - var2 * 2;
      int var18 = var17 / 7;
      String[] var19 = new String[]{"Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"};

      for(int var20 = 0; var20 < 7; ++var20) {
         JLabel var21 = new JLabel(var19[var20], 0);
         var21.setFont(new Font("Segoe UI", 1, 11));
         var21.setForeground(TEXT_LIGHT);
         var21.setBounds(var2 + var20 * var18, 44, var18, 18);
         var12.add(var21);
      }

      daysGrid = new JPanel((LayoutManager)null);
      daysGrid.setBounds(var2, 66, var17, 214);
      daysGrid.setBackground(WHITE);
      var12.add(daysGrid);
      renderCalendar(var18);
      final JButton backBtn = var15;
      Calendar todayCal = Calendar.getInstance();
      if (cal.get(1) == todayCal.get(1) && cal.get(2) == todayCal.get(2)) {
         backBtn.setEnabled(false);
         backBtn.setBackground(Color.LIGHT_GRAY);
         backBtn.setForeground(Color.GRAY);
      }
      var15.addActionListener((var1x) -> {
         Calendar prev = (Calendar)cal.clone();
         prev.add(2, -1);
         Calendar now = Calendar.getInstance();
         if (prev.get(1) < now.get(1) || (prev.get(1) == now.get(1) && prev.get(2) < now.get(2))) {
            return;
         }
         cal.add(2, -1);
         selectedDay = -1;
         renderCalendar(var18);
         backBtn.setEnabled(true);
         backBtn.setBackground(BLUE_PALE);
         backBtn.setForeground(TEXT_DARK);
         if (cal.get(1) == now.get(1) && cal.get(2) == now.get(2)) {
            backBtn.setEnabled(false);
            backBtn.setBackground(Color.LIGHT_GRAY);
            backBtn.setForeground(Color.GRAY);
         }
         refreshSlotAvailability();
      });
      var16.addActionListener((var1x) -> {
         cal.add(2, 1);
         selectedDay = -1;
         renderCalendar(var18);
         backBtn.setEnabled(true);
         backBtn.setBackground(BLUE_PALE);
         backBtn.setForeground(TEXT_DARK);
         refreshSlotAvailability();
      });
      int var37 = var2 + var3 + var2;
      JPanel var38 = new JPanel((LayoutManager)null);
      var38.setBounds(var37, 130, var3, var11);
      var38.setBackground(WHITE);
      var38.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      mainContainer.add(var38);
      JLabel var22 = new JLabel("Time Slots");
      var22.setFont(new Font("Segoe UI", 1, 13));
      var22.setForeground(TEXT_DARK);
      var22.setBounds(var2, 12, 200, 20);
      var38.add(var22);
      int var24 = var3 - var2 * 2;
      int slotBtnW = (var24 - 12) / 3;  // 3 per row, 12px total horizontal gap
      String[] localSlots = currentSlots;
      slotBtns = new JButton[localSlots.length];
      int sx = var2;
      int sy = 42;

      for(int var27 = 0; var27 < localSlots.length; ++var27) {
         String slotTime = localSlots[var27];
          String dateKey = cal.get(1) + "-" + String.format("%02d", cal.get(2) + 1) + "-" + String.format("%02d", selectedDay);
          boolean alreadyBooked = selectedDay > 0 && (bookedSlots.contains(slotKey(var0[0], dateKey, slotTime)) || HospitalSystem.isSlotBooked(var0[0], dateKey, slotTime));
          JButton var29 = new JButton(slotTime);
          var29.setFont(new Font("Segoe UI", 0, 12));
          var29.setFocusPainted(false);
          var29.setOpaque(true);
          var29.setBounds(sx, sy, slotBtnW, 34);
          var29.setCursor(Cursor.getPredefinedCursor(12));
          var29.setHorizontalAlignment(JButton.CENTER);
          var29.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
          if (alreadyBooked) {
             var29.setEnabled(false);
             var29.setBackground(Color.LIGHT_GRAY);
             var29.setForeground(Color.GRAY);
          } else {
             var29.setBackground(BLUE_PALE);
             var29.setForeground(TEXT_DARK);
             // Block past time slots for today
             java.util.Calendar now = java.util.Calendar.getInstance();
             if (cal.get(1) == now.get(1) && cal.get(2) == now.get(2) && selectedDay == now.get(5)) {
                 try {
                     java.text.SimpleDateFormat sdf12 = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH);
                     java.util.Date sd = sdf12.parse(slotTime);
                     java.util.Calendar sc = java.util.Calendar.getInstance(); sc.setTime(sd);
                     int slotMin = sc.get(java.util.Calendar.HOUR_OF_DAY) * 60 + sc.get(java.util.Calendar.MINUTE);
                     int nowMin = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE);
                     if (slotMin <= nowMin) {
                         var29.setEnabled(false);
                         var29.setBackground(Color.LIGHT_GRAY);
                         var29.setForeground(Color.GRAY);
                     }
                 } catch (Exception e) {}
             }
          }
         slotBtns[var27] = var29;
         var38.add(var29);
         sx += slotBtnW + 6;
         if (var27 % 3 == 2) { sx = var2; sy += 42; }

         if (!alreadyBooked) {
            final int slotIdx = var27;
            var29.addActionListener((var2x) -> {
               selectedSlot = localSlots[slotIdx];

               for(JButton slotBtn : slotBtns) {
                  if (slotBtn.isEnabled()) {
                     slotBtn.setBackground(BLUE_PALE);
                     slotBtn.setForeground(TEXT_DARK);
                  }
               }

               var29.setBackground(BLUE);
               var29.setForeground(WHITE);
            });
         }
        }

       int var39 = 130 + var11 + var2;
      JPanel var28 = new JPanel((LayoutManager)null);
      var28.setBounds(var2, var39, var4, 140);
      var28.setBackground(WHITE);
      var28.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      mainContainer.add(var28);
      JLabel var40 = new JLabel("Patient Details");
      var40.setFont(new Font("Segoe UI", 1, 13));
      var40.setForeground(TEXT_DARK);
      var40.setBounds(var2, 10, 300, 20);
      var28.add(var40);
      int var30 = (var4 - var2 * 3) / 2;
      JLabel var31 = PatientGUI.fieldLabel("Patient Name");
      var31.setBounds(var2, 36, var30, 18);
      var28.add(var31);
      JTextField var32 = PatientGUI.styledField();
      var32.setText(patient.getName());
      var32.setBounds(var2, 56, var30, 38);
      var28.add(var32);
      JLabel var33 = PatientGUI.fieldLabel("Phone Number");
      var33.setBounds(var2 + var30 + var2, 36, var30, 18);
      var28.add(var33);
      JTextField var34 = PatientGUI.styledField();
      var34.setText(patient.getPhoneNumber());
      var34.setBounds(var2 + var30 + var2, 56, var30, 38);
      var28.add(var34);
      int var35 = var39 + 140 + var2;
      final JButton var36 = new JButton("Confirm Booking");
      var36.setFont(new Font("Segoe UI", 1, 14));
      var36.setBackground(BLUE);
      var36.setForeground(WHITE);
      var36.setBorderPainted(false);
      var36.setFocusPainted(false);
      var36.setOpaque(true);
      var36.setBounds(var2, var35, var4, 48);
      var36.setCursor(Cursor.getPredefinedCursor(12));
      var36.addMouseListener(new MouseAdapter() {
         public void mouseEntered(MouseEvent var1) {
            var36.setBackground(BookAppointmentPanel.NAVY);
         }

          public void mouseExited(MouseEvent var1) {
             var36.setBackground(BookAppointmentPanel.BLUE);
          }
        });
       mainContainer.add(var36);
      var36.addActionListener((var1x) -> {
         if (selectedDay == -1) {
            JOptionPane.showMessageDialog(frame, "Please select a date.", "Error", 0);
         } else if (selectedSlot == null) {
            JOptionPane.showMessageDialog(frame, "Please select a time slot.", "Error", 0);
         } else {
            int var10000 = cal.get(1);
            String dateStr = var10000 + "-" + String.format("%02d", cal.get(2) + 1) + "-" + String.format("%02d", selectedDay);
             ReceptionistDataStore.apptCounter++;
             String aptId = "#A-00" + ReceptionistDataStore.apptCounter;
            patient.bookAppointment(aptId, var0[0], var0[1], dateStr, selectedSlot);
             bookedSlots.add(slotKey(var0[0], dateStr, selectedSlot));
             HospitalSystem.markSlotBooked(var0[0], dateStr, selectedSlot);
             HospitalSystem.syncAppointmentToDoctorStore(new Appointment(aptId, var0[0], var0[1], dateStr, selectedSlot));
            Admin.HospitalAdmin.registerAppointment(aptId, patient.getName(), var0[0], dateStr + " " + selectedSlot);
            Dashboard.addAlert("Appointment " + aptId + " booked with " + var0[0] + " on " + dateStr + " at " + selectedSlot + " — Awaiting confirmation", Dashboard.AMBER_BG, Dashboard.AMBER_FG);
            JOptionPane.showMessageDialog(frame, "Appointment Confirmed!\n\nID      : " + aptId + "\nDoctor  : " + var0[0] + "\nDate    : " + dateStr + "\nTime    : " + selectedSlot + "\n\nSMS   sent to: " + patient.getPhoneNumber() + "\nEmail sent to: " + patient.getEmail(), "Booking Confirmed", 1);
            showDoctorList();
         }
        });
      mainContainer.revalidate();
      mainContainer.repaint();
   }

   static void renderCalendar(int var0) {
      daysGrid.removeAll();
      String[] var1 = new String[]{"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
      JLabel var10000 = monthLbl;
      String var10001 = var1[cal.get(2)];
      var10000.setText(var10001 + " " + cal.get(1));
      Calendar var2 = (Calendar)cal.clone();
      var2.set(5, 1);
      int var3 = var2.get(7);
      int var4 = var3 == 1 ? 6 : var3 - 2;
      int var5 = var2.getActualMaximum(5);
      Calendar var6 = Calendar.getInstance();
      boolean var7 = cal.get(2) == var6.get(2) && cal.get(1) == var6.get(1);
      boolean beforeMonth = cal.get(1) < var6.get(1) || (cal.get(1) == var6.get(1) && cal.get(2) < var6.get(2));
      int todayDay = var6.get(5);
      ArrayList<JButton> var8 = new ArrayList<>();

      for(int var9 = 0; var9 < 42; ++var9) {
         int var10 = var9 - var4 + 1;
         if (var10 >= 1 && var10 <= var5) {
            int var12 = var9 % 7;
            int var13 = var9 / 7;
            JButton var14 = new JButton(String.valueOf(var10));
            boolean isPast = beforeMonth || (var7 && var10 < todayDay);
            var14.setFont(new Font("Segoe UI", var7 && var10 == todayDay ? 1 : 0, 11));
            var14.setFocusPainted(false);
            var14.setBorderPainted(false);
            var14.setOpaque(true);
            var14.setCursor(isPast ? Cursor.getDefaultCursor() : Cursor.getPredefinedCursor(12));
            if (isPast) {
               var14.setBackground(Color.LIGHT_GRAY);
               var14.setForeground(Color.GRAY);
               var14.setEnabled(false);
            } else if (var10 == selectedDay) {
               var14.setBackground(BLUE);
               var14.setForeground(WHITE);
            } else if (var7 && var10 == todayDay) {
               var14.setBackground(BLUE_PALE);
               var14.setForeground(BLUE);
            } else {
               var14.setBackground(WHITE);
               var14.setForeground(TEXT_DARK);
            }

            var14.setBounds(var12 * var0, var13 * 40, var0 - 2, 32);
            var8.add(var14);
            daysGrid.add(var14);
            if (!isPast) {
               var14.addActionListener((var4x) -> {
                   selectedDay = var10;

                   for(JButton var6x : var8) {
                      int var7x = Integer.parseInt(var6x.getText());
                      if (var6x.isEnabled()) {
                         if (var7x == selectedDay) {
                            var6x.setBackground(BLUE);
                            var6x.setForeground(WHITE);
                         } else if (var7 && var7x == var6.get(5)) {
                            var6x.setBackground(BLUE_PALE);
                            var6x.setForeground(BLUE);
                         } else {
                            var6x.setBackground(WHITE);
                            var6x.setForeground(TEXT_DARK);
                         }
                      }
                   }

                   refreshSlotAvailability();
                });
            }
         }
      }

      daysGrid.revalidate();
      daysGrid.repaint();
   }

   public static void removeBookedSlot(String doctorName, String date, String timeSlot) {
      bookedSlots.remove(slotKey(doctorName, date, timeSlot));
   }

     static void refreshSlotAvailability() {
        if (currentDoc == null || slotBtns == null || currentSlots == null) return;
        for (int i = 0; i < slotBtns.length && i < currentSlots.length; i++) {
           String slotTime = currentSlots[i];
           String dateKey = cal.get(1) + "-" + String.format("%02d", cal.get(2) + 1) + "-" + String.format("%02d", selectedDay);
           boolean booked = selectedDay > 0 && (bookedSlots.contains(slotKey(currentDoc[0], dateKey, slotTime)) || HospitalSystem.isSlotBooked(currentDoc[0], dateKey, slotTime));
          if (booked) {
             slotBtns[i].setEnabled(false);
             slotBtns[i].setBackground(Color.LIGHT_GRAY);
             slotBtns[i].setForeground(Color.GRAY);
             slotBtns[i].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
          } else {
             slotBtns[i].setEnabled(true);
             slotBtns[i].setBackground(BLUE_PALE);
             slotBtns[i].setForeground(TEXT_DARK);
             slotBtns[i].setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
             // Block past time slots for today
             java.util.Calendar now = java.util.Calendar.getInstance();
             if (cal.get(1) == now.get(1) && cal.get(2) == now.get(2) && selectedDay == now.get(5)) {
                 try {
                     java.text.SimpleDateFormat sdf12 = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH);
                     java.util.Date sd = sdf12.parse(slotTime);
                     java.util.Calendar sc = java.util.Calendar.getInstance(); sc.setTime(sd);
                     int slotMin = sc.get(java.util.Calendar.HOUR_OF_DAY) * 60 + sc.get(java.util.Calendar.MINUTE);
                     int nowMin = now.get(java.util.Calendar.HOUR_OF_DAY) * 60 + now.get(java.util.Calendar.MINUTE);
                     if (slotMin <= nowMin) {
                         slotBtns[i].setEnabled(false);
                         slotBtns[i].setBackground(Color.LIGHT_GRAY);
                         slotBtns[i].setForeground(Color.GRAY);
                         slotBtns[i].setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
                     }
                 } catch (Exception e) {}
             }
          }
      }
   }

   static JPanel makeAvatar(String var0, final int var1) {
      String var2 = var0.replace("Dr. ", "");
      String[] var3 = var2.split(" ");
      final String var4 = var3.length >= 2 ? "" + var3[0].charAt(0) + var3[1].charAt(0) : "" + var3[0].charAt(0);
      return new JPanel((LayoutManager)null) {
         {
            this.setOpaque(false);
            this.setPreferredSize(new Dimension(var1, var1));
         }

         protected void paintComponent(Graphics var1x) {
            Graphics2D var2 = (Graphics2D)var1x;
            var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var2.setColor(BookAppointmentPanel.BLUE);
            var2.fillOval(0, 0, var1, var1);
            var2.setColor(BookAppointmentPanel.WHITE);
            var2.setFont(new Font("Segoe UI", 1, var1 / 3));
            FontMetrics var3 = var2.getFontMetrics();
            var2.drawString(var4, (var1 - var3.stringWidth(var4)) / 2, (var1 + var3.getAscent() - var3.getDescent()) / 2);
         }
      };
    }

    static void refreshFromRosterStore() {
        java.util.List<Object[]> activeDocs = DoctorRosterStore.getActiveDoctors();
        DOCTORS = new String[activeDocs.size()][3];
        java.util.Set<String> deptSet = new java.util.LinkedHashSet<>();
        deptSet.add("All");
        for (int i = 0; i < activeDocs.size(); i++) {
            Object[] d = activeDocs.get(i);
            String name = (String) d[1];
            String dept = (String) d[2];
            DOCTORS[i] = new String[]{name, dept, "General"};
            deptSet.add(dept);
        }
        SPECIALTIES = deptSet.toArray(new String[0]);
    }

    static {
        WHITE = Color.WHITE;
        BG = new Color(230, 241, 251);
        refreshFromRosterStore();
        TIME_SLOTS = new String[]{"08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM"};
        filterSpec = null;
        searchText = "";
        selectedSlot = null;
        selectedDay = -1;
        cal = Calendar.getInstance();
        showingConfirm = false;
        DoctorRosterStore.addListener(BookAppointmentPanel::refreshFromRosterStore);
    }
}