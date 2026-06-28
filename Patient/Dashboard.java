package Patient;

import System.HospitalSystem;
import System.DoctorRosterStore;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class Dashboard {
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
    public static final Color GREEN_BG;
    public static final Color GREEN_FG;
    public static final Color RED_BG;
    public static final Color RED_FG;
   static final Color AMBER_BG;
   static final Color AMBER_FG;
   static final int SW = 240;
   static final int FW = 1450;
   static final int FH = 880;
   static final int TH = 58;
   static final int CW = 1210;
   static JFrame frame;
    public static Patient patient;
   static JPanel contentArea;
   static JButton[] navBtns;
   static JLabel topTitleLbl;
   static final String[] NAV_LABELS;
   static final String[] PANEL_TITLES;

   public Dashboard() {
   }

   public static void show(Patient var0) {
      patient = var0;
      frame = new JFrame("Smart Health - Patient Dashboard");
      frame.setSize(1450, 880);
      frame.setMinimumSize(new Dimension(1450, 880));
      frame.setLocationRelativeTo((Component)null);
      frame.setDefaultCloseOperation(3);
      frame.setLayout((LayoutManager)null);
      JPanel var1 = new JPanel((LayoutManager)null);
      var1.setBackground(BG);
      var1.setBounds(0, 0, 1450, 880);
      JPanel var2 = new JPanel((LayoutManager)null);
      var2.setBounds(0, 0, 240, 880);
      var2.setBackground(NAVY);
      var1.add(var2);
      JLabel var3 = new JLabel("Smart Health", 0);
      var3.setFont(new Font("Segoe UI", 1, 17));
      var3.setForeground(WHITE);
      var3.setBounds(0, 16, 240, 28);
      var2.add(var3);
      JPanel var4 = makeAvatar(initials(patient.getName()), 42);
      var4.setBounds(18, 56, 42, 42);
      var2.add(var4);
      JLabel var5 = new JLabel(patient.getName());
      var5.setFont(new Font("Segoe UI", 1, 13));
      var5.setForeground(WHITE);
      var5.setBounds(68, 58, 130, 18);
      var2.add(var5);
      JLabel var6 = new JLabel("Patient  " + patient.getPatientId());
      var6.setFont(new Font("Segoe UI", 0, 11));
      var6.setForeground(TEXT_LIGHT);
      var6.setBounds(68, 76, 130, 16);
      var2.add(var6);
      JPanel var7 = new JPanel();
      var7.setBounds(0, 108, 240, 1);
      var7.setBackground(new Color(255, 255, 255, 30));
      var2.add(var7);
      navBtns = new JButton[NAV_LABELS.length];

      for(int var8 = 0; var8 < NAV_LABELS.length; ++var8) {
         final int navIdx = var8;
         JButton var10 = makeNavBtn(NAV_LABELS[var8], var8 == 0);
         var10.setBounds(0, 116 + var8 * 54, 240, 50);
         navBtns[var8] = var10;
         var2.add(var10);
         var10.addActionListener((var1x) -> switchPanel(navIdx));
      }

      JPanel var12 = new JPanel();
      var12.setBounds(0, 116 + NAV_LABELS.length * 54 + 4, 240, 1);
      var12.setBackground(new Color(255, 255, 255, 30));
      var2.add(var12);
      JButton var9 = makeNavBtn("Log Out", false);
      var9.setBounds(0, 116 + NAV_LABELS.length * 54 + 12, 240, 50);
      var9.setForeground(new Color(240, 149, 149));
      var9.addMouseListener(hov(var9, new Color(80, 20, 20), NAVY));
      var9.addActionListener((var0x) -> {
         patient.logout();
         frame.dispose();
         HospitalSystem.showLauncher.run();
      });
      var2.add(var9);
      JPanel var13 = new JPanel((LayoutManager)null);
      var13.setBounds(240, 0, 1210, 58);
      var13.setBackground(WHITE);
      var13.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BLUE_LIGHT));
      var1.add(var13);
      topTitleLbl = new JLabel("Patient Dashboard");
      topTitleLbl.setFont(new Font("Segoe UI", 1, 18));
      topTitleLbl.setForeground(TEXT_DARK);
      topTitleLbl.setBounds(20, 0, 500, 58);
      var13.add(topTitleLbl);
      String var10002 = patient.getName();
      JLabel var11 = new JLabel(var10002 + "   |   " + patient.getPatientId() + "   |   " + patient.getBloodGroup() + "  ");
      var11.setFont(new Font("Segoe UI", 0, 12));
      var11.setForeground(TEXT_LIGHT);
      var11.setHorizontalAlignment(4);
      var11.setBounds(770, 0, 420, 58);
      var13.add(var11);
      contentArea = new JPanel((LayoutManager)null);
      contentArea.setBounds(240, 58, 1210, 822);
      contentArea.setBackground(BG);
      var1.add(contentArea);
      loadPanel(buildHome());
      frame.setContentPane(var1);
      frame.setVisible(true);
   }

   static JButton makeNavBtn(String var0, boolean var1) {
      JButton var2 = new JButton(var0);
      var2.setFont(new Font("Segoe UI", 0, 15));
      var2.setForeground(var1 ? WHITE : new Color(181, 212, 244));
      var2.setBackground(var1 ? new Color(24, 95, 165) : NAVY);
      var2.setHorizontalAlignment(0);
      var2.setFocusPainted(false);
      var2.setBorderPainted(false);
      var2.setOpaque(true);
      var2.setCursor(Cursor.getPredefinedCursor(12));
      var2.setRolloverEnabled(false);
      var2.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      if (var1) {
         var2.setBorder(BorderFactory.createMatteBorder(4, 0, 0, 0, BLUE_MID));
      }

      return var2;
   }

   static MouseAdapter hov(final JButton var0, final Color var1, final Color var2) {
      return new MouseAdapter() {
         public void mouseEntered(MouseEvent var1x) {
            var0.setBackground(var1);
         }

         public void mouseExited(MouseEvent var1x) {
            var0.setBackground(var2);
         }
      };
   }

   static JPanel buildUpdate() {
      HospitalSystem.syncDoctorStatusToPatient();
      JPanel var0 = new JPanel((LayoutManager)null);
      var0.setBackground(BG);
      short var1 = 1170;
      JPanel var2 = new JPanel((LayoutManager)null);
      var2.setBounds(20, 20, var1, 782);
      var2.setBackground(WHITE);
      var2.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var0.add(var2);
      JLabel var3 = label("Select Appointment", 15, true);
      var3.setBounds(20, 18, 500, 24);
      var2.add(var3);
      JLabel var4 = label("Click to choose an appointment to reschedule", 12, false);
      var4.setForeground(TEXT_MID);
      var4.setBounds(20, 44, 500, 18);
      var2.add(var4);
      ArrayList<Appointment> var5 = new ArrayList<>();

      for(Appointment var7 : patient.getAppointments()) {
         String s = var7.getStatus();
         System.out.println("[buildUpdate] Appointment " + var7.getAppointmentId() + " status=" + s);
         if (s.equals("Pending") || s.equals("Scheduled")) {
            var5.add(var7);
         }
      }

      final JPanel[] var23 = new JPanel[var5.size()];
      final int[] var24 = new int[]{-1};
      final Runnable[] refreshUpdSlots = { null };

      for(int var8 = 0; var8 < var5.size(); ++var8) {
         final int updIdx = var8;
         Appointment var9 = var5.get(var8);
         final JPanel var11 = new JPanel((LayoutManager)null);
         var11.setBounds(20, 72 + var8 * 72, var1 - 40, 62);
         var11.setBackground(BLUE_PALE);
         var11.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
         var11.setCursor(Cursor.getPredefinedCursor(12));
         int var12 = var1 - 40;
         JLabel var13 = label(var9.getDoctorName() + "  -  " + var9.getSpecialty(), 13, true);
         var13.setBounds(12, 8, var12 - 300, 22);
         var11.add(var13);
         JLabel var14 = label(var9.getDate() + "  " + var9.getTimeSlot(), 11, false);
         var14.setForeground(TEXT_MID);
         var14.setBounds(12, 32, var12 - 300, 18);
         var11.add(var14);
         JLabel var15 = new JLabel(var9.getAppointmentId(), 4);
         var15.setFont(new Font("Segoe UI", 0, 11));
         var15.setForeground(TEXT_LIGHT);
         var15.setBounds(var12 - 240, 8, 80, 18);
         var11.add(var15);
         JLabel var16 = PatientGUI.statusPill(var9.getStatus());
         var16.setBounds(var12 - 150, 16, 110, 26);
         var23[updIdx] = var11;
         var2.add(var11);
         var11.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent var1) {
               var24[0] = updIdx;

               for(int var2 = 0; var2 < var23.length; ++var2) {
                  var23[var2].setBackground(var2 == updIdx ? Dashboard.BLUE : Dashboard.BLUE_PALE);

                  for(Component var6 : var23[var2].getComponents()) {
                     if (var6 instanceof JLabel) {
                        ((JLabel)var6).setForeground(var2 == updIdx ? Dashboard.WHITE : Dashboard.TEXT_DARK);
                     }
                  }
               }
               if (refreshUpdSlots[0] != null) refreshUpdSlots[0].run();
            }

            public void mouseEntered(MouseEvent var1) {
               if (var24[0] != updIdx) {
                  var11.setBackground(Dashboard.BLUE_LIGHT);
               }
            }

            public void mouseExited(MouseEvent var1) {
               if (var24[0] != updIdx) {
                  var11.setBackground(Dashboard.BLUE_PALE);
               }
            }
         });
      }

      int var25 = 72 + var5.size() * 72 + 20;
      int var26 = (var1 - 60) / 2;
      int calH = 290;
      final Calendar[] updCal = {Calendar.getInstance()};
      final int[] updSelDay = {-1};
      final JLabel[] updMonthLbl = {new JLabel("", 0)};
      JPanel updCalPanel = new JPanel((LayoutManager)null);
      updCalPanel.setBounds(20, var25, var26, calH);
      updCalPanel.setBackground(WHITE);
      updCalPanel.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var2.add(updCalPanel);
      JLabel updCalTitle = label("Select New Date", 14, true);
      updCalTitle.setBounds(14, 12, var26 - 100, 20);
      updCalPanel.add(updCalTitle);
      updMonthLbl[0].setFont(new Font("Segoe UI", 1, 12));
      updMonthLbl[0].setForeground(TEXT_DARK);
      updMonthLbl[0].setBounds(var26 / 2 - 90, 12, 180, 20);
      updCalPanel.add(updMonthLbl[0]);
      JButton prevBtn = new JButton("<");
      prevBtn.setFont(new Font("Segoe UI", 1, 11));
      prevBtn.setBackground(BLUE_PALE);
      prevBtn.setForeground(TEXT_DARK);
      prevBtn.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      prevBtn.setFocusPainted(false);
      prevBtn.setBounds(var26 - 80, 10, 28, 24);
      prevBtn.setCursor(Cursor.getPredefinedCursor(12));
      updCalPanel.add(prevBtn);
      JButton nextBtn = new JButton(">");
      nextBtn.setFont(new Font("Segoe UI", 1, 11));
      nextBtn.setBackground(BLUE_PALE);
      nextBtn.setForeground(TEXT_DARK);
      nextBtn.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      nextBtn.setFocusPainted(false);
      nextBtn.setBounds(var26 - 46, 10, 28, 24);
      nextBtn.setCursor(Cursor.getPredefinedCursor(12));
      updCalPanel.add(nextBtn);
      int dayW = (var26 - 28) / 7;
      String[] dayNames = {"Mo","Tu","We","Th","Fr","Sa","Su"};
      for (int i = 0; i < 7; i++) {
         JLabel d = new JLabel(dayNames[i], 0);
         d.setFont(new Font("Segoe UI", 1, 11));
         d.setForeground(TEXT_LIGHT);
         d.setBounds(14 + i * dayW, 44, dayW, 18);
         updCalPanel.add(d);
      }
      JPanel updDaysGrid = new JPanel((LayoutManager)null);
      updDaysGrid.setBounds(14, 66, var26 - 28, 214);
      updDaysGrid.setBackground(WHITE);
      updCalPanel.add(updDaysGrid);
      Consumer<Integer> renderUpdCal = (Integer cellW) -> {
         updDaysGrid.removeAll();
         String[] months = {"January","February","March","April","May","June","July","August","September","October","November","December"};
         updMonthLbl[0].setText(months[updCal[0].get(2)] + " " + updCal[0].get(1));
         Calendar tmp = (Calendar)updCal[0].clone();
         tmp.set(5, 1);
         int startDay = tmp.get(7);
         int offset = startDay == 1 ? 6 : startDay - 2;
         int daysInMonth = tmp.getActualMaximum(5);
         Calendar today = Calendar.getInstance();
         boolean isCurrentMonth = updCal[0].get(2) == today.get(2) && updCal[0].get(1) == today.get(1);
         ArrayList<JButton> btns = new ArrayList<>();
         for (int d = 0; d < 42; d++) {
            int dayNum = d - offset + 1;
            if (dayNum >= 1 && dayNum <= daysInMonth) {
                int col = d % 7;
                int row = d / 7;
                JButton btn = new JButton(String.valueOf(dayNum));
                btn.setFont(new Font("Segoe UI", isCurrentMonth && dayNum == today.get(5) ? 1 : 0, 11));
                btn.setFocusPainted(false);
                btn.setBorderPainted(false);
                btn.setOpaque(true);
                btn.setCursor(Cursor.getPredefinedCursor(12));
                boolean beforeMonth = updCal[0].get(1) < today.get(1) || (updCal[0].get(1) == today.get(1) && updCal[0].get(2) < today.get(2));
                boolean isPastDay = beforeMonth || (isCurrentMonth && dayNum < today.get(5));
                if (isPastDay) {
                    btn.setEnabled(false);
                    btn.setBackground(Color.LIGHT_GRAY);
                    btn.setForeground(Color.GRAY);
                } else if (dayNum == updSelDay[0]) {
                    btn.setBackground(BLUE);
                    btn.setForeground(WHITE);
                } else if (isCurrentMonth && dayNum == today.get(5)) {
                    btn.setBackground(BLUE_PALE);
                    btn.setForeground(BLUE);
                } else {
                    btn.setBackground(WHITE);
                    btn.setForeground(TEXT_DARK);
                }
                btn.setBounds(col * cellW, row * 40, cellW - 2, 32);
                btns.add(btn);
                updDaysGrid.add(btn);
                final int fDay = dayNum;
                if (!isPastDay) {
                    btn.addActionListener((var0x) -> {
                        updSelDay[0] = fDay;
                        for (JButton b : btns) {
                            int val = Integer.parseInt(b.getText());
                            if (val == updSelDay[0]) {
                                b.setBackground(BLUE);
                                b.setForeground(WHITE);
                            } else if (isCurrentMonth && val == today.get(5)) {
                                b.setBackground(BLUE_PALE);
                                b.setForeground(BLUE);
                            } else {
                                b.setBackground(WHITE);
                                b.setForeground(TEXT_DARK);
                            }
                        }
                        refreshUpdSlots[0].run();
                    });
                }
             }
          }
          updDaysGrid.revalidate();
          updDaysGrid.repaint();
       };
       renderUpdCal.accept(dayW);
        prevBtn.addActionListener((var0x) -> {
            Calendar prev = (Calendar) updCal[0].clone();
            prev.add(2, -1);
            Calendar now = Calendar.getInstance();
            if (prev.get(1) < now.get(1) || (prev.get(1) == now.get(1) && prev.get(2) < now.get(2))) return;
            updCal[0].add(2, -1); updSelDay[0] = -1; renderUpdCal.accept(dayW); refreshUpdSlots[0].run();
        });
       nextBtn.addActionListener((var0x) -> { updCal[0].add(2, 1); updSelDay[0] = -1; renderUpdCal.accept(dayW); refreshUpdSlots[0].run(); });
       JLabel var29 = label("New Time Slot", 14, true);
       var29.setBounds(20 + var26 + 20, var25, var26, 22);
       var2.add(var29);

       JPanel slotContainer = new JPanel(null);
       slotContainer.setBounds(20 + var26 + 20, var25 + 24, var26, 200);
       slotContainer.setBackground(WHITE);
       var2.add(slotContainer);

       java.util.ArrayList<JButton> slotBtns = new java.util.ArrayList<>();
       String[] var32 = new String[]{""};

       // Collect all slot keys that are currently booked (from patient's own appointments + doctor store)
        java.util.function.BiFunction<String, String, Boolean> isSlotBooked = (String doc, String slot) -> {
           if (updSelDay[0] < 0) return false;
           String dateStr = String.format("%04d-%02d-%02d", updCal[0].get(1), updCal[0].get(2) + 1, updSelDay[0]);
           String key = BookAppointmentPanel.slotKey(doc, dateStr, slot);
           // Check bookedSlots set
           if (BookAppointmentPanel.bookedSlots.contains(key)) return true;
           // Also check patient's own appointments
           for (Appointment a : patient.getAppointments()) {
              if (a.getStatus().equals("Cancelled") || a.getStatus().equals("Rejected")) continue;
              String ak = BookAppointmentPanel.slotKey(a.getDoctorName(), a.getDate(), a.getTimeSlot());
              if (ak.equals(key)) return true;
           }
           // Check doctor store appointment model
           javax.swing.table.DefaultTableModel am = Doctor.doctorDataStore.get().getAppointmentModel();
           for (int r = 0; r < am.getRowCount(); r++) {
              Object p = am.getValueAt(r, 0), d = am.getValueAt(r, 2), t = am.getValueAt(r, 1);
              if (p != null && d != null && t != null) {
                 String ak = BookAppointmentPanel.slotKey(p.toString(), d.toString(), t.toString());
                 if (ak.equals(key)) return true;
              }
           }
           // Check cross-system shared slot tracker (catches receptionist demo appointments)
           if (HospitalSystem.isSlotBooked(doc, dateStr, slot)) return true;
           return false;
        };

       refreshUpdSlots[0] = () -> {
          slotContainer.removeAll();
          slotBtns.clear();

          // Determine which slots to show based on selected doctor's roster
          String[] slots;
          if (var24[0] >= 0) {
             String doc = var5.get(var24[0]).getDoctorName();
             java.util.List<String> roster = DoctorRosterStore.getExpandedTimeSlotsByName(doc);
             slots = roster.isEmpty()
                ? new String[]{"08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM"}
                : roster.toArray(new String[0]);
          } else {
             slots = new String[]{"08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM", "11:00 AM", "11:30 AM", "12:00 PM", "12:30 PM", "02:00 PM", "02:30 PM", "03:00 PM", "03:30 PM", "04:00 PM", "04:30 PM", "05:00 PM", "05:30 PM", "06:00 PM", "06:30 PM"};
          }

          if (slots.length == 0) {
             var32[0] = "";
             slotContainer.revalidate(); slotContainer.repaint();
             return;
          }

          int btnW = (var26 - 12) / 3;
          for (int i = 0; i < slots.length; i++) {
             final int idx = i;
             JButton btn = new JButton(slots[i]);
             btn.setFont(new Font("Segoe UI", 0, 12));
             btn.setBackground(BLUE_PALE);
             btn.setForeground(TEXT_DARK);
             btn.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
             btn.setFocusPainted(false);
             btn.setOpaque(true);
             int col = i % 3;
             int row = i / 3;
             btn.setBounds(col * (btnW + 6), row * 42, btnW, 34);
             btn.setCursor(Cursor.getPredefinedCursor(12));
             btn.addActionListener((var5x) -> {
                var32[0] = slots[idx];
                for (JButton b : slotBtns) {
                   b.setBackground(BLUE_PALE);
                   b.setForeground(TEXT_DARK);
                }
                btn.setBackground(BLUE);
                btn.setForeground(WHITE);
             });
             slotBtns.add(btn);
             slotContainer.add(btn);
          }

          // Apply booking filter if appointment + date selected
          if (var24[0] >= 0 && updSelDay[0] >= 0) {
             String doc = var5.get(var24[0]).getDoctorName();
             for (int i = 0; i < slots.length; i++) {
                boolean booked = isSlotBooked.apply(doc, slots[i]);
                slotBtns.get(i).setEnabled(!booked);
                if (booked) {
                   slotBtns.get(i).setBackground(Color.LIGHT_GRAY);
                   slotBtns.get(i).setForeground(Color.GRAY);
                }
             }
          }

           // Disable past time slots for today
           if (updSelDay[0] >= 0) {
               Calendar now = Calendar.getInstance();
               if (updCal[0].get(1) == now.get(1) && updCal[0].get(2) == now.get(2) && updSelDay[0] == now.get(5)) {
                   java.text.SimpleDateFormat sdf12 = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH);
                   for (int i = 0; i < slots.length; i++) {
                       try {
                           java.util.Date slotDate = sdf12.parse(slots[i]);
                           Calendar slotCal = Calendar.getInstance();
                           slotCal.setTime(slotDate);
                           int slotMin = slotCal.get(Calendar.HOUR_OF_DAY) * 60 + slotCal.get(Calendar.MINUTE);
                           int nowMin = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
                           if (slotMin <= nowMin) {
                               slotBtns.get(i).setEnabled(false);
                               slotBtns.get(i).setBackground(Color.LIGHT_GRAY);
                               slotBtns.get(i).setForeground(Color.GRAY);
                           }
                       } catch (Exception e) {}
                   }
               }
           }

           // Auto-select first enabled slot
          var32[0] = "";
          for (int i = 0; i < slotBtns.size(); i++) {
             if (slotBtns.get(i).isEnabled()) {
                var32[0] = slots[i];
                slotBtns.get(i).setBackground(BLUE);
                slotBtns.get(i).setForeground(WHITE);
                break;
             }
          }

          slotContainer.revalidate();
          slotContainer.repaint();
       };

       refreshUpdSlots[0].run();

      JButton var33 = PatientGUI.primaryBtn("Update Appointment");
      var33.setBounds(20, var25 + calH + 15, var1 - 40, 48);
      var2.add(var33);
      var33.addActionListener((var4x) -> {
         if (var24[0] < 0) {
            JOptionPane.showMessageDialog(frame, "Select an appointment.", "Error", 0);
          } else if (updSelDay[0] < 0) {
             JOptionPane.showMessageDialog(frame, "Select a date.", "Error", 0);
          } else if (var32[0] == null || var32[0].isEmpty()) {
             JOptionPane.showMessageDialog(frame, "No time slots available for this date.", "Error", 0);
           } else {
             Appointment updApt = var5.get(var24[0]);
             String var5x = updCal[0].get(1) + "-" + String.format("%02d", updCal[0].get(2) + 1) + "-" + String.format("%02d", updSelDay[0]);
             String var6 = updApt.getAppointmentId();
             String oldDate = updApt.getDate();
             String oldTime = updApt.getTimeSlot();
             String oldDoctor = updApt.getDoctorName();
               if (patient.updateAppointment(var6, var5x, var32[0])) {
                   BookAppointmentPanel.removeBookedSlot(oldDoctor, oldDate, oldTime);
                   BookAppointmentPanel.bookedSlots.add(BookAppointmentPanel.slotKey(oldDoctor, var5x, var32[0]));
                   HospitalSystem.clearSlot(oldDoctor, oldDate, oldTime);
                   HospitalSystem.markSlotBooked(oldDoctor, var5x, var32[0]);
                   HospitalSystem.cancelFromDoctorStore(patient.getName(), oldDate, oldTime);
                   HospitalSystem.syncAppointmentToDoctorStore(new Appointment(var6, oldDoctor, updApt.getSpecialty(), var5x, var32[0]));
                 addAlert("Appointment " + var6 + " rescheduled to " + var5x + " at " + var32[0] + " — awaiting doctor confirmation", AMBER_BG, AMBER_FG);
                 showNotif("Appointment " + var6 + " updated to " + var5x + " at " + var32[0]);
                 loadPanel(buildUpdate());
             }
          }
      });
      return var0;
   }

   static void loadPanel(JPanel panel) {
      contentArea.removeAll();
      panel.setBounds(0, 0, contentArea.getWidth(), contentArea.getHeight());
      contentArea.add(panel);
      contentArea.revalidate();
      contentArea.repaint();
   }

   static void switchPanel(int idx) {
      topTitleLbl.setText(PANEL_TITLES[idx]);
      for (int i = 0; i < navBtns.length; i++) {
         navBtns[i].setBackground(i == idx ? WHITE : NAVY);
         navBtns[i].setForeground(i == idx ? NAVY : Color.WHITE);
      }
      switch (idx) {
         case 0: loadPanel(buildHome()); break;
         case 1: loadPanel(new BookAppointmentPanel().build(patient, frame)); break;
         case 2: loadPanel(buildUpdate()); break;
         case 3: loadPanel(buildCancel()); break;
         case 4: loadPanel(buildHistory()); break;
         case 5: loadPanel(buildProfile()); break;
      }
   }

   static JPanel buildHome() {
      HospitalSystem.syncDoctorDataToPatient(patient.getName());
      HospitalSystem.syncDoctorStatusToPatient();
      JPanel panel = new JPanel((LayoutManager)null);
      panel.setBackground(BG);
      int W = 1210;
      int H = 822;
      int pad = 14;
      JLabel titleLbl = label("Dashboard", 15, true);
      titleLbl.setBounds(pad, 18, 300, 24);
      panel.add(titleLbl);
      int rowY = 54;
      int leftW = 500;
      int rightX = pad + leftW + 12;
      int rightW = W - 2 * pad - leftW - 12;
      int rowH = 530;
      int medH = rowH / 2 - 6;
      int quickH = rowH / 2 - 6;
      // ── LEFT: Upcoming Appointments ──
      JPanel upPanel = new JPanel((LayoutManager)null);
      upPanel.setBackground(WHITE);
      upPanel.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      upPanel.setBounds(pad, rowY, leftW, rowH);
      panel.add(upPanel);
      JLabel upTitle = label("Upcoming Appointments", 14, true);
      upTitle.setBounds(14, 12, 300, 22);
      upPanel.add(upTitle);
      int upY = 44;
      ArrayList<Appointment> upcoming = new ArrayList<>();
      for (Appointment a : patient.getAppointments()) {
         System.out.println("[buildHome] Appointment " + a.getAppointmentId() + " status=" + a.getStatus());
         if (!a.getStatus().equals("Cancelled") && !a.getStatus().equals("Completed")) {
            upcoming.add(a);
         }
      }
      if (upcoming.isEmpty()) {
         JLabel noApt = label("No upcoming appointments.", 12, false);
         noApt.setForeground(TEXT_MID);
         noApt.setBounds(14, upY, 300, 20);
         upPanel.add(noApt);
      } else {
         for (Appointment a : upcoming) {
            JPanel card = new JPanel((LayoutManager)null);
            card.setBounds(14, upY, leftW - 28, 48);
            card.setBackground(BLUE_PALE);
            card.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
            upPanel.add(card);
            JLabel docLbl = label(a.getDoctorName() + " - " + a.getSpecialty(), 13, true);
            docLbl.setBounds(10, 5, leftW - 180, 18);
            card.add(docLbl);
            JLabel dtLbl = label(a.getDate() + "  " + a.getTimeSlot(), 11, false);
            dtLbl.setForeground(TEXT_MID);
            dtLbl.setBounds(10, 25, leftW - 180, 16);
            card.add(dtLbl);
            JLabel statusLbl = PatientGUI.statusPill(a.getStatus());
            statusLbl.setBounds(leftW - 160, 9, 120, 28);
            card.add(statusLbl);
            upY += 56;
         }
      }
      // ── RIGHT TOP: Medical History ──
      JPanel medPanel = new JPanel((LayoutManager)null);
      medPanel.setBackground(WHITE);
      medPanel.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      medPanel.setBounds(rightX, rowY, rightW, medH);
      panel.add(medPanel);
      JLabel medTitle = label("Medical History", 14, true);
      medTitle.setBounds(14, 12, 200, 22);
      medPanel.add(medTitle);
      ArrayList<String[]> medHistory = patient.getMedicalHistory();
      int medY = 44;
      if (medHistory.isEmpty()) {
         JLabel noMed = label("No medical records yet.", 12, false);
         noMed.setForeground(TEXT_MID);
         noMed.setBounds(14, medY, 300, 20);
         medPanel.add(noMed);
      } else {
         int start = Math.max(0, medHistory.size() - 3);
         for (int i = start; i < medHistory.size(); i++) {
            String[] m = medHistory.get(i);
            JPanel medRow = new JPanel((LayoutManager)null);
            medRow.setBounds(14, medY, rightW - 28, 46);
            medRow.setBackground(WHITE);
            medRow.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BLUE_PALE));
            medPanel.add(medRow);
            JLabel condLbl = label(m[0], 12, true);
            condLbl.setBounds(8, 4, rightW - 100, 18);
            medRow.add(condLbl);
            String diagText = m[1].length() > 30 ? m[1].substring(0, 30) + "..." : m[1];
            JLabel diagLbl = label("Diagnosed: " + diagText, 10, false);
            diagLbl.setForeground(TEXT_MID);
            diagLbl.setBounds(8, 24, rightW - 100, 16);
            medRow.add(diagLbl);
            Color pillBg;
            Color pillFg;
            if (m[4].equals("Ongoing")) {
               pillBg = GREEN_BG;
               pillFg = GREEN_FG;
            } else if (m[4].equals("Allergy")) {
               pillBg = RED_BG;
               pillFg = RED_FG;
            } else {
               pillBg = AMBER_BG;
               pillFg = AMBER_FG;
            }
            JLabel statusPill = new JLabel(" " + m[4] + " ");
            statusPill.setFont(new Font("Segoe UI", 1, 10));
            statusPill.setOpaque(true);
            statusPill.setBackground(pillBg);
            statusPill.setForeground(pillFg);
            statusPill.setBounds(rightW - 100, 8, 80, 22);
            statusPill.setHorizontalAlignment(0);
            medRow.add(statusPill);
            medY += 52;
         }
      }
      // ── RIGHT BOTTOM: Quick Actions ──
      JPanel quickPanel = new JPanel((LayoutManager)null);
      quickPanel.setBackground(WHITE);
      quickPanel.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      quickPanel.setBounds(rightX, rowY + medH + 12, rightW, quickH);
      panel.add(quickPanel);
      JLabel quickTitle = label("Quick Actions", 14, true);
      quickTitle.setBounds(14, 12, 200, 22);
      quickPanel.add(quickTitle);
      int btnY = 48;
      int btnW = rightW - 28;
      int btnH = Math.min(quickH - btnY - 14, 42);
      int gap = btnH > 0 ? (quickH - btnY - btnH * 3 + 2) / 3 : 6;
      if (gap < 6) gap = 6;
      JButton bookBtn = PatientGUI.primaryBtn("Book Appointment");
      bookBtn.setBounds(14, btnY, btnW, btnH);
      bookBtn.addActionListener((e) -> switchPanel(1));
      quickPanel.add(bookBtn);
      btnY += btnH + gap;
      JButton updBtn = PatientGUI.secondaryBtn("Update Appointment");
      updBtn.setBounds(14, btnY, btnW, btnH);
      updBtn.addActionListener((e) -> switchPanel(2));
      quickPanel.add(updBtn);
      btnY += btnH + gap;
      JButton canBtn = new JButton("Cancel Appointment");
      canBtn.setFont(new Font("Segoe UI", 1, 13));
      canBtn.setBackground(RED_FG);
      canBtn.setForeground(WHITE);
      canBtn.setBorderPainted(false);
      canBtn.setFocusPainted(false);
      canBtn.setOpaque(true);
      canBtn.setBounds(14, btnY, btnW, btnH);
      canBtn.setCursor(Cursor.getPredefinedCursor(12));
      canBtn.addMouseListener(hov(canBtn, new Color(122, 31, 31), RED_FG));
      canBtn.addActionListener((e) -> switchPanel(3));
      quickPanel.add(canBtn);
      // ── BOTTOM: Notifications ──
      int notifY = rowY + rowH + 14;
      int notifH = H - notifY - pad;
      if (notifH < 60) notifH = 60;
      JPanel notifPanel = new JPanel((LayoutManager)null);
      notifPanel.setBackground(WHITE);
      notifPanel.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      notifPanel.setBounds(pad, notifY, W - 2 * pad, notifH);
      panel.add(notifPanel);
      JLabel notifTitle = label("Notifications", 14, true);
      notifTitle.setBounds(14, 12, 200, 22);
      notifPanel.add(notifTitle);
      int notifY2 = 44;
      if (alerts.isEmpty()) {
         JLabel noNotif = label("No new notifications.", 12, false);
         noNotif.setForeground(TEXT_MID);
         noNotif.setBounds(14, notifY2, 300, 20);
         notifPanel.add(noNotif);
      } else {
         for (Alert a : alerts) {
            if (notifY2 + 34 > notifH) {
               JLabel moreLbl = label("... and " + (alerts.size() - 4) + " more", 10, false);
               moreLbl.setForeground(TEXT_LIGHT);
               moreLbl.setBounds(14, notifY2, 200, 18);
               notifPanel.add(moreLbl);
               break;
            }
            JLabel al = new JLabel("  " + a.text);
            al.setFont(new Font("Segoe UI", 0, 12));
            al.setForeground(a.fg);
            al.setBackground(a.bg);
            al.setOpaque(true);
            al.setBounds(14, notifY2, W - 2 * pad - 28, 28);
            al.setBorder(BorderFactory.createLineBorder(a.fg.darker(), 1));
            notifPanel.add(al);
            notifY2 += 34;
         }
      }
      return panel;
   }

    static JPanel buildCancel() {
       HospitalSystem.syncDoctorStatusToPatient();
       JPanel var0 = new JPanel((LayoutManager)null);
      var0.setBackground(BG);
      short var1 = 1170;
      JPanel var2 = new JPanel((LayoutManager)null);
      var2.setBounds(20, 20, var1, 782);
      var2.setBackground(WHITE);
      var2.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var0.add(var2);
      JLabel var3 = label("Select Appointment to Cancel", 15, true);
      var3.setBounds(20, 18, 500, 24);
      var2.add(var3);
      JLabel var4 = label("Only scheduled appointments can be cancelled", 12, false);
      var4.setForeground(TEXT_MID);
      var4.setBounds(20, 44, 500, 18);
      var2.add(var4);
      ArrayList<Appointment> var5 = new ArrayList<>();

      for(Appointment var7 : patient.getAppointments()) {
         String s = var7.getStatus();
         System.out.println("[buildCancel] Appointment " + var7.getAppointmentId() + " status=" + s);
         if (s.equals("Pending") || s.equals("Scheduled")) {
            var5.add(var7);
         }
      }

      final JPanel[] var17 = new JPanel[var5.size()];
      final int[] var18 = new int[]{-1};

      for(int var8 = 0; var8 < var5.size(); ++var8) {
         final int cancelIdx = var8;
         Appointment var9 = var5.get(var8);
         final JPanel var11 = new JPanel((LayoutManager)null);
         var11.setBounds(20, 72 + var8 * 72, var1 - 40, 62);
         var11.setBackground(BLUE_PALE);
         var11.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
         var11.setCursor(Cursor.getPredefinedCursor(12));
         int var12 = var1 - 40;
         JLabel var13 = label(var9.getDoctorName() + "  -  " + var9.getSpecialty(), 13, true);
         var13.setBounds(12, 8, var12 - 300, 22);
         var11.add(var13);
         JLabel var14 = label(var9.getDate() + "  " + var9.getTimeSlot(), 11, false);
         var14.setForeground(TEXT_MID);
         var14.setBounds(12, 32, var12 - 300, 18);
         var11.add(var14);
         JLabel var15 = new JLabel(var9.getAppointmentId(), 4);
         var15.setFont(new Font("Segoe UI", 0, 11));
         var15.setForeground(TEXT_LIGHT);
         var15.setBounds(var12 - 240, 8, 80, 18);
         var11.add(var15);
         JLabel var16 = PatientGUI.statusPill(var9.getStatus());
         var16.setBounds(var12 - 150, 16, 110, 26);
         var17[cancelIdx] = var11;
         var2.add(var11);
         var11.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent var1) {
               var18[0] = cancelIdx;

               for(int var2 = 0; var2 < var17.length; ++var2) {
                  boolean var3 = var2 == cancelIdx;
                  var17[var2].setBackground(var3 ? Dashboard.RED_BG : Dashboard.BLUE_PALE);
                  var17[var2].setBorder(BorderFactory.createLineBorder(var3 ? new Color(240, 149, 149) : Dashboard.BLUE_LIGHT, 1, true));

                  for(Component var7 : var17[var2].getComponents()) {
                     if (var7 instanceof JLabel) {
                        ((JLabel)var7).setForeground(var3 ? Dashboard.RED_FG : Dashboard.TEXT_DARK);
                     }
                  }
               }

            }

            public void mouseEntered(MouseEvent var1) {
               if (var18[0] != cancelIdx) {
                  var11.setBackground(Dashboard.BLUE_LIGHT);
               }

            }

            public void mouseExited(MouseEvent var1) {
               if (var18[0] != cancelIdx) {
                  var11.setBackground(Dashboard.BLUE_PALE);
               }

            }
         });
      }

      int var19 = 72 + var5.size() * 72 + 20;
      JPanel var20 = new JPanel((LayoutManager)null);
      var20.setBounds(20, var19, var1 - 40, 58);
      var20.setBackground(AMBER_BG);
      var20.setBorder(BorderFactory.createLineBorder(new Color(250, 199, 117), 1, true));
      var2.add(var20);
      JLabel var10 = label("Are you sure you want to cancel?", 12, true);
      var10.setForeground(new Color(99, 56, 6));
      var10.setBounds(14, 8, var1 - 80, 18);
      var20.add(var10);
      JLabel var21 = label("This action cannot be undone. A notification will be sent.", 11, false);
      var21.setForeground(AMBER_FG);
      var21.setBounds(14, 28, var1 - 80, 18);
      var20.add(var21);
      int var22 = var19 + 68;
      JButton var23 = PatientGUI.secondaryBtn("Keep Appointment");
      var23.setBounds(20, var22, (var1 - 50) / 2, 44);
      var2.add(var23);
      JButton var24 = new JButton("Confirm Cancellation");
      var24.setFont(new Font("Segoe UI", 1, 13));
      var24.setBackground(RED_FG);
      var24.setForeground(WHITE);
      var24.setBorderPainted(false);
      var24.setFocusPainted(false);
      var24.setOpaque(true);
      var24.setBounds(30 + (var1 - 50) / 2, var22, (var1 - 50) / 2, 44);
      var24.setCursor(Cursor.getPredefinedCursor(12));
      var24.addMouseListener(hov(var24, new Color(122, 31, 31), RED_FG));
      var2.add(var24);
      var23.addActionListener((var0x) -> switchPanel(0));
      var24.addActionListener((var2x) -> {
         if (var18[0] < 0) {
            JOptionPane.showMessageDialog(frame, "Select an appointment.", "Error", 0);
         } else {
            Appointment cancelApt = var5.get(var18[0]);
            String cancelId = cancelApt.getAppointmentId();
            int confirm = JOptionPane.showConfirmDialog(frame, "Cancel appointment " + cancelId + "?", "Confirm", 0, 2);
            if (confirm == 0) {
                patient.cancelAppointment(cancelId);
                BookAppointmentPanel.removeBookedSlot(cancelApt.getDoctorName(), cancelApt.getDate(), cancelApt.getTimeSlot());
                HospitalSystem.clearSlot(cancelApt.getDoctorName(), cancelApt.getDate(), cancelApt.getTimeSlot());
                HospitalSystem.cancelFromDoctorStore(patient.getName(), cancelApt.getDate(), cancelApt.getTimeSlot());
                addAlert("Appointment " + cancelId + " has been cancelled — doctor notified", RED_BG, RED_FG);
                showNotif("Appointment " + cancelId + " has been cancelled.");
                loadPanel(buildCancel());
            }

         }
      });
      return var0;
   }

    static JPanel buildHistory() {
       JPanel var0 = new JPanel((LayoutManager)null);
       var0.setBackground(BG);
       int W = 1182;
       int H = 822 - 60 - 14;
       JButton apptBtn = new JButton("Appointments");
       apptBtn.setFont(new Font("Segoe UI", 1, 13));
       apptBtn.setBackground(BLUE);
       apptBtn.setForeground(WHITE);
       apptBtn.setBorderPainted(false);
       apptBtn.setFocusPainted(false);
       apptBtn.setOpaque(true);
       apptBtn.setBounds(14, 14, 160, 36);
       apptBtn.setCursor(Cursor.getPredefinedCursor(12));
       var0.add(apptBtn);
       JButton medBtn = new JButton("Medical History");
       medBtn.setFont(new Font("Segoe UI", 0, 13));
       medBtn.setBackground(WHITE);
       medBtn.setForeground(TEXT_MID);
       medBtn.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1));
       medBtn.setFocusPainted(false);
       medBtn.setOpaque(true);
       medBtn.setBounds(180, 14, 160, 36);
       medBtn.setCursor(Cursor.getPredefinedCursor(12));
       var0.add(medBtn);
       JPanel cardPanel = new JPanel(new CardLayout());
       cardPanel.setBounds(14, 60, W, H);
       cardPanel.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
       var0.add(cardPanel);
       // ── APPOINTMENTS CARD ──
       JPanel apptCard = new JPanel(new BorderLayout(0, 10));
       apptCard.setBackground(WHITE);
       JLabel apptTitle = label("Appointment History", 15, true);
       apptTitle.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
       apptCard.add(apptTitle, BorderLayout.NORTH);
        String[] apptCols = {"ID", "Doctor", "Specialty", "Date", "Time", "Status"};
        int totalAppts = patient.getAppointments().size();
        Object[][] apptData = new Object[totalAppts][6];
        int ai = 0;
        for(Appointment a : patient.getAppointments()) {
           apptData[ai++] = new Object[]{a.getAppointmentId(), a.getDoctorName(), a.getSpecialty(), a.getDate(), a.getTimeSlot(), a.getStatus()};
        }
       DefaultTableModel apptModel = new DefaultTableModel(apptData, apptCols) {
          public boolean isCellEditable(int r, int c) { return false; }
       };
       JTable apptTable = buildTable(apptModel);
       JScrollPane apptScroll = new JScrollPane(apptTable);
       apptScroll.setBorder(BorderFactory.createEmptyBorder());
       apptScroll.getVerticalScrollBar().setUnitIncrement(16);
       apptCard.add(apptScroll, BorderLayout.CENTER);
       // ── MEDICAL HISTORY CARD ──
       JPanel medCard = new JPanel(new BorderLayout(0, 10));
       medCard.setBackground(WHITE);
       JLabel medTitle = label("Medical History", 15, true);
       medTitle.setBorder(BorderFactory.createEmptyBorder(14, 14, 0, 14));
       medCard.add(medTitle, BorderLayout.NORTH);
       JPanel medList = new JPanel((LayoutManager)null);
       medList.setBackground(WHITE);
       int rowH = 80;
       int medH = Math.max(patient.getMedicalHistory().size() * rowH + 50, H - 60);
       medList.setPreferredSize(new Dimension(W - 28, medH));
        int my = 10;
        int mi = 0;
        for(String[] m : patient.getMedicalHistory()) {
           if ("Self".equals(m[2])) continue; // skip registration medical info (shown in Profile)
           Color barColor;
          switch (m[4]) {
             case "Ongoing" -> barColor = BLUE;
             case "Allergy" -> barColor = RED_FG;
             default -> barColor = AMBER_FG;
          }
          JPanel bar = new JPanel();
          bar.setBounds(8, my, 3, rowH - 4);
          bar.setBackground(barColor);
          medList.add(bar);
          JPanel rowPanel = new JPanel((LayoutManager)null);
          rowPanel.setBounds(14, my, W - 28, rowH - 4);
          rowPanel.setBackground(mi % 2 == 0 ? BLUE_PALE : WHITE);
          rowPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BLUE_PALE));
          medList.add(rowPanel);
          int rw = W - 28;
          JLabel titleLbl = label(m[0], 13, true);
          titleLbl.setBounds(10, 6, rw - 160, 20);
          rowPanel.add(titleLbl);
          JLabel statusPill = new JLabel("  " + m[4] + "  ", 0);
          statusPill.setFont(new Font("Segoe UI", 1, 11));
          statusPill.setOpaque(true);
          switch (m[4]) {
             case "Ongoing":
                statusPill.setBackground(GREEN_BG);
                statusPill.setForeground(GREEN_FG);
                break;
             case "Allergy":
                statusPill.setBackground(RED_BG);
                statusPill.setForeground(RED_FG);
                break;
             default:
                statusPill.setBackground(AMBER_BG);
                statusPill.setForeground(AMBER_FG);
          }
          statusPill.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
          statusPill.setBounds(rw - 130, 10, 110, 22);
          rowPanel.add(statusPill);
          String[] subLabels = {"Diagnosed", "Diagnosed by", "Medication"};
          String[] subVals = {m[1], m[2], m[3]};
          int subW = (rw - 30) / 3;
          for (int si = 0; si < 3; si++) {
             JLabel sl = label(subLabels[si], 10, false);
             sl.setForeground(TEXT_LIGHT);
             sl.setBounds(10 + si * subW, 34, subW, 14);
             rowPanel.add(sl);
             JLabel sv = label(subVals[si], 12, true);
             sv.setBounds(10 + si * subW, 50, subW, 18);
             rowPanel.add(sv);
          }
          my += rowH;
          mi++;
       }
       JScrollPane medScroll = new JScrollPane(medList);
       medScroll.setBorder(BorderFactory.createEmptyBorder());
       medScroll.getVerticalScrollBar().setUnitIncrement(16);
       medCard.add(medScroll, BorderLayout.CENTER);
       cardPanel.add(apptCard, "appointments");
       cardPanel.add(medCard, "medicalHistory");
       CardLayout cl = (CardLayout) cardPanel.getLayout();
       apptBtn.addActionListener(e -> {
          apptBtn.setBackground(BLUE);
          apptBtn.setForeground(WHITE);
          medBtn.setBackground(WHITE);
          medBtn.setForeground(TEXT_MID);
          cl.show(cardPanel, "appointments");
       });
       medBtn.addActionListener(e -> {
          medBtn.setBackground(BLUE);
          medBtn.setForeground(WHITE);
          apptBtn.setBackground(WHITE);
          apptBtn.setForeground(TEXT_MID);
          cl.show(cardPanel, "medicalHistory");
       });
        return var0;
     }

   static JPanel buildProfile() {
      JPanel var0 = new JPanel((LayoutManager)null);
      var0.setBackground(BG);
      short var1 = 1182;
      JPanel var2 = new JPanel((LayoutManager)null) {
         protected void paintComponent(Graphics var1) {
            Graphics2D var2 = (Graphics2D)var1;
            var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var2.setColor(Dashboard.BLUE);
            var2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), 12, 12);
         }
      };
      var2.setOpaque(false);
      var2.setBounds(14, 14, var1, 92);
      var0.add(var2);
      JPanel var3 = makeAvatar(initials(patient.getName()), 64);
      var3.setBounds(16, 14, 64, 64);
      var2.add(var3);
      JLabel var4 = new JLabel(patient.getName());
      var4.setFont(new Font("Segoe UI", 1, 22));
      var4.setForeground(WHITE);
      var4.setBounds(96, 14, 400, 28);
      var2.add(var4);
      JLabel var5 = new JLabel("Patient ID: " + patient.getPatientId());
      var5.setFont(new Font("Segoe UI", 0, 12));
      var5.setForeground(BLUE_LIGHT);
      var5.setBounds(96, 44, 300, 18);
      var2.add(var5);
      JLabel var6 = pill("Blood: " + patient.getBloodGroup());
      var6.setBounds(96, 66, 100, 18);
      var2.add(var6);
      JLabel var7 = pill("Age: " + patient.getAge());
      var7.setBounds(206, 66, 80, 18);
      var2.add(var7);
      int var8 = (var1 - 14) / 2;
      JPanel var9 = buildInfoCard("Personal Information", new String[][]{{"Patient ID", patient.getPatientId()}, {"Full Name", patient.getName()}, {"Age", patient.getAge() + " years"}, {"Blood Group", patient.getBloodGroup()}}, var8);
      var9.setBounds(14, 120, var8, 220);
      var0.add(var9);
      JPanel var10 = buildInfoCard("Contact Information", new String[][]{{"Email", patient.getEmail()}, {"Phone", patient.getPhoneNumber()}, {"Password", "**********"}}, var8);
      var10.setBounds(14 + var8 + 14, 120, var8, 220);
      var0.add(var10);
      JPanel var11 = new JPanel((LayoutManager)null);
      var11.setBounds(14, 354, var8, 210);
      var11.setBackground(WHITE);
      var11.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var0.add(var11);
      JLabel var12 = label("My Stats", 15, true);
      var12.setBounds(14, 12, 300, 22);
      var11.add(var12);
      int var13 = patient.getAppointments().size();
      long var14 = patient.getAppointments().stream().filter((var0x) -> var0x.getStatus().equals("Pending") || var0x.getStatus().equals("Scheduled")).count();
      int var16 = patient.getMedicalHistory().size();
      long var17 = patient.getAppointments().stream().filter((var0x) -> var0x.getStatus().equals("Cancelled")).count();
      String[] var19 = new String[]{String.valueOf(var13), String.valueOf(var14), String.valueOf(var16), String.valueOf(var17)};
      String[] var20 = new String[]{"Total", "Active", "Records", "Cancelled"};
      int var21 = (var8 - 35) / 2;
      int var22 = var8 - 35 - var21 * 2;

      for(int var23 = 0; var23 < 4; ++var23) {
         JPanel var24 = new JPanel((LayoutManager)null);
         var24.setBounds(14 + var23 % 2 * (var21 + 7), 44 + var23 / 2 * 72, var21 + (var23 % 2 == 1 ? var22 : 0), 62);
         var24.setBackground(BLUE_PALE);
         var24.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
         JLabel var25 = new JLabel(var19[var23], 0);
         var25.setFont(new Font("Segoe UI", 1, 28));
         var25.setForeground(TEXT_DARK);
         var25.setBounds(0, 6, var21, 34);
         JLabel var26 = new JLabel(var20[var23], 0);
         var26.setFont(new Font("Segoe UI", 0, 12));
         var26.setForeground(TEXT_MID);
         var26.setBounds(0, 40, var21, 18);
         var24.add(var25);
         var24.add(var26);
         var11.add(var24);
      }

      JPanel var28 = new JPanel((LayoutManager)null);
      var28.setBounds(14 + var8 + 14, 354, var8, 210);
      var28.setBackground(WHITE);
      var28.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      var0.add(var28);
      JLabel var29 = label("Account Actions", 15, true);
      var29.setBounds(14, 12, 300, 22);
      var28.add(var29);
      JPanel var30 = infRow("Member Since", "January 2026", var8, BLUE_PALE);
      var30.setBounds(10, 44, var8 - 20, 38);
      var28.add(var30);
      JPanel var31 = infRow("Last Login", "17 May 2026", var8, WHITE);
      var31.setBounds(10, 82, var8 - 20, 38);
      var28.add(var31);
      JButton var27 = PatientGUI.primaryBtn("Edit Profile");
      var27.setBounds(14, 136, var8 - 28, 44);
      var28.add(var27);
      var27.addActionListener((var0x) -> showEditDialog());

      // ── Medical Information card (from registration) ─────────────
      java.util.ArrayList<String[]> medRows = new java.util.ArrayList<>();
      for (String[] m : patient.getMedicalHistory()) {
         if ("Self".equals(m[2])) {
            String[] parts = m[0].split(":", 2);
            String label = parts[0].trim();
            String value = parts.length > 1 ? parts[1].trim() : m[0];
            medRows.add(new String[]{label, value});
         }
      }
      if (!medRows.isEmpty()) {
         String[][] medData = medRows.toArray(new String[0][]);
         JPanel medCard = buildInfoCard("Medical Information", medData, var1 - 28);
         medCard.setBounds(14, 578, var1 - 28, medData.length * 42 + 56);
         medCard.setBackground(WHITE);
         var0.add(medCard);
      }
      return var0;
   }

   static void showEditDialog() {
      JDialog var0 = new JDialog(frame, "Edit Profile", true);
      var0.setSize(420, 540);
      var0.setLocationRelativeTo(frame);
      var0.setLayout((LayoutManager)null);
      var0.getContentPane().setBackground(WHITE);
      JLabel var1 = new JLabel("Edit Profile", 0);
      var1.setFont(new Font("Segoe UI", 1, 18));
      var1.setForeground(TEXT_DARK);
      var1.setBounds(0, 16, 420, 28);
      var0.add(var1);
      JLabel var2 = PatientGUI.fieldLabel("Name");
      var2.setBounds(24, 52, 372, 18);
      var0.add(var2);
      JTextField var3 = PatientGUI.styledField();
      var3.setText(patient.getName());
      var3.setBounds(24, 72, 372, 38);
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
      var3.setDocument(nameDoc);
      var3.addKeyListener(new java.awt.event.KeyAdapter() {
          public void keyTyped(java.awt.event.KeyEvent e) {
              char c = e.getKeyChar();
              if (!Character.isLetter(c) && c != ' ' && !Character.isISOControl(c)) e.consume();
          }
      });
      var0.add(var3);
      JLabel var4 = PatientGUI.fieldLabel("Age");
      var4.setBounds(24, 118, 372, 18);
      var0.add(var4);
      JTextField var5 = PatientGUI.styledField();
      var5.setText(String.valueOf(patient.getAge()));
      var5.setBounds(24, 138, 372, 38);
      var0.add(var5);
      JLabel var6 = PatientGUI.fieldLabel("Blood Group");
      var6.setBounds(24, 184, 372, 18);
      var0.add(var6);
      JTextField var7 = PatientGUI.styledField();
      var7.setText(patient.getBloodGroup());
      var7.setBounds(24, 204, 372, 38);
      var0.add(var7);
      JLabel var8 = PatientGUI.fieldLabel("Email");
      var8.setBounds(24, 250, 372, 18);
      var0.add(var8);
      JTextField var9 = PatientGUI.styledField();
      var9.setText(patient.getEmail());
      var9.setBounds(24, 270, 372, 38);
      var0.add(var9);
      JLabel var10 = PatientGUI.fieldLabel("Phone Number");
      var10.setBounds(24, 316, 372, 18);
      var0.add(var10);
      JTextField var11 = PatientGUI.styledField();
      var11.setText(patient.getPhoneNumber());
      var11.setBounds(24, 336, 372, 38);
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
      var11.setDocument(phoneDoc);
      var11.addKeyListener(new java.awt.event.KeyAdapter() {
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
      var0.add(var11);
      JLabel var12 = PatientGUI.fieldLabel("Password");
      var12.setBounds(24, 382, 372, 18);
      var0.add(var12);
      JPasswordField var13 = PatientGUI.styledPass();
      var13.setText(patient.getPassword());
      var13.setBounds(24, 402, 372, 38);
      var0.add(var13);
      JButton var14 = PatientGUI.primaryBtn("Save Changes");
      var14.setBounds(24, 458, 372, 42);
      var0.add(var14);
      var14.addActionListener((var7x) -> {
         try {
            String newName = var3.getText().trim();
            String newPhone = var11.getText().trim();
            if (!newName.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(var0, "Name must contain only letters and spaces.", "Error", 0);
                return;
            }
            if (newPhone.length() != 11) {
                JOptionPane.showMessageDialog(var0, "Phone number must be exactly 11 digits.", "Error", 0);
                return;
            }
            patient.setName(newName);
            patient.setAge(Integer.parseInt(var5.getText().trim()));
            patient.setBloodGroup(var7.getText().trim());
            patient.setEmail(var9.getText().trim());
            patient.setPhoneNumber(newPhone);
            patient.setPassword((new String(var13.getPassword())).trim());
            JOptionPane.showMessageDialog(var0, "Profile updated successfully!", "Success", 1);
            var0.dispose();
            loadPanel(buildProfile());
         } catch (NumberFormatException var9x) {
            JOptionPane.showMessageDialog(var0, "Age must be a number.", "Error", 0);
         }

      });
      var0.setVisible(true);
   }

   static JPanel buildInfoCard(String var0, String[][] var1, int var2) {
      JPanel var3 = new JPanel((LayoutManager)null);
      var3.setBackground(WHITE);
      var3.setBorder(BorderFactory.createLineBorder(BLUE_LIGHT, 1, true));
      JLabel var4 = label(var0, 15, true);
      var4.setBounds(14, 12, var2 - 20, 22);
      var3.add(var4);

      for(int var5 = 0; var5 < var1.length; ++var5) {
         JPanel var6 = infRow(var1[var5][0], var1[var5][1], var2, var5 % 2 == 0 ? BLUE_PALE : WHITE);
         var6.setBounds(10, 44 + var5 * 42, var2 - 20, 38);
         var3.add(var6);
      }

      return var3;
   }

   static JPanel infRow(String var0, String var1, int var2, Color var3) {
      JPanel var4 = new JPanel((LayoutManager)null);
      var4.setBackground(var3);
      JLabel var5 = label(var0, 11, true);
      var5.setForeground(TEXT_LIGHT);
      var5.setBounds(10, 8, 130, 22);
      var4.add(var5);
      JLabel var6 = label(var1, 13, true);
      var6.setForeground(TEXT_DARK);
      var4.add(var6);
      short var7 = 140;
      int var8 = var2 - 20 - var7 - 10;
      if (var8 < 80) {
         var7 = 10;
         var6.setBounds(var7, 8, var8, 22);
      } else {
         var6.setBounds(var7, 8, var8, 22);
      }

      return var4;
   }

   static JLabel label(String var0, int var1, boolean var2) {
      JLabel var3 = new JLabel(var0);
      var3.setFont(new Font("Segoe UI", var2 ? 1 : 0, var1));
      var3.setForeground(TEXT_DARK);
      return var3;
   }

   static JLabel pill(String var0) {
      JLabel var1 = new JLabel("  " + var0 + "  ", 0);
      var1.setFont(new Font("Segoe UI", 0, 11));
      var1.setForeground(WHITE);
      var1.setOpaque(true);
      var1.setBackground(new Color(255, 255, 255, 40));
      var1.setBorder(BorderFactory.createLineBorder(new Color(255, 255, 255, 60), 1, true));
      return var1;
   }

   static JTable buildTable(DefaultTableModel var0) {
      JTable var1 = new JTable(var0);
      var1.setRowHeight(34);
      var1.setFont(new Font("Segoe UI", 0, 12));
      var1.setShowGrid(false);
      var1.setBackground(WHITE);
      var1.setForeground(TEXT_DARK);
      var1.setSelectionBackground(BLUE_PALE);
      var1.setIntercellSpacing(new Dimension(0, 0));
      var1.setCursor(Cursor.getPredefinedCursor(12));
      var1.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
         public Component getTableCellRendererComponent(JTable var1, Object var2, boolean var3, boolean var4, int var5, int var6) {
            super.getTableCellRendererComponent(var1, var2, var3, var4, var5, var6);
            if (!var3) {
               this.setBackground(var5 % 2 == 0 ? Dashboard.WHITE : Dashboard.BLUE_PALE);
            }

            this.setForeground(Dashboard.TEXT_DARK);
            this.setFont(new Font("Segoe UI", 0, 13));
            this.setHorizontalAlignment(0);
            this.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return this;
         }
      });
      int var2 = var0.getColumnCount() - 1;
      var1.getColumnModel().getColumn(var2).setCellRenderer(new DefaultTableCellRenderer() {
         public Component getTableCellRendererComponent(JTable var1, Object var2, boolean var3, boolean var4, int var5, int var6) {
            JLabel var7 = new JLabel(var2 != null ? var2.toString() : "", 0);
            var7.setFont(new Font("Segoe UI", 1, 11));
            var7.setOpaque(true);
            switch (var2 != null ? var2.toString() : "") {
                case "Scheduled":
                case "Accepted":
                   var7.setBackground(Dashboard.GREEN_BG);
                   var7.setForeground(Dashboard.GREEN_FG);
                   break;
                case "Pending":
                case "Rescheduled":
                   var7.setBackground(Dashboard.AMBER_BG);
                   var7.setForeground(Dashboard.AMBER_FG);
                   break;
                case "Cancelled":
                case "Rejected":
                   var7.setBackground(Dashboard.RED_BG);
                   var7.setForeground(Dashboard.RED_FG);
                   break;
                default:
                   var7.setBackground(Dashboard.AMBER_BG);
                   var7.setForeground(Dashboard.AMBER_FG);
             }

            return var7;
         }
      });
      JTableHeader var3 = var1.getTableHeader();
      var3.setFont(new Font("Segoe UI", 1, 14));
      var3.setBackground(NAVY);
      var3.setForeground(WHITE);
      var3.setPreferredSize(new Dimension(0, 40));
      var3.setCursor(Cursor.getPredefinedCursor(12));
      ((DefaultTableCellRenderer)var3.getDefaultRenderer()).setHorizontalAlignment(0);
      return var1;
   }

   static JPanel makeAvatar(final String var0, final int var1) {
      return new JPanel((LayoutManager)null) {
         {
            this.setOpaque(false);
            this.setPreferredSize(new Dimension(var1, var1));
         }

         protected void paintComponent(Graphics var1x) {
            Graphics2D var2 = (Graphics2D)var1x;
            var2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            var2.setColor(Dashboard.BLUE);
            var2.fillOval(0, 0, var1, var1);
            var2.setColor(Dashboard.WHITE);
            var2.setFont(new Font("Segoe UI", 1, var1 / 3));
            FontMetrics var3 = var2.getFontMetrics();
            var2.drawString(var0, (var1 - var3.stringWidth(var0)) / 2, (var1 + var3.getAscent() - var3.getDescent()) / 2);
         }
      };
   }

   static String initials(String var0) {
      String[] var1 = var0.split(" ");
      return var1.length >= 2 ? "" + var1[0].charAt(0) + var1[1].charAt(0) : "" + var1[0].charAt(0);
   }

   static final List<Alert> alerts = new ArrayList<>();
   static JPanel alertPanel;
   static JPanel alertContent;

   static class Alert {
      final String text;
      final Color bg;
      final Color fg;
      Alert(String text, Color bg, Color fg) {
         this.text = text;
         this.bg = bg;
         this.fg = fg;
      }
   }

    public static void addAlert(String text, Color bg, Color fg) {
      alerts.add(0, new Alert(text, bg, fg));
      if (alerts.size() > 20) alerts.remove(alerts.size() - 1);
      if (alertContent != null) {
         alertContent.removeAll();
         buildAlertList(alertContent);
         alertContent.revalidate();
         alertContent.repaint();
      }
   }

   static void buildAlertList(JPanel parent) {
      parent.removeAll();
      int y = 10;
      int w = Math.max(parent.getWidth() - 20, 800);
      for (Alert a : alerts) {
         JLabel lbl = new JLabel("  " + a.text);
         lbl.setFont(new Font("Segoe UI", 0, 12));
         lbl.setForeground(a.fg);
         lbl.setBackground(a.bg);
         lbl.setOpaque(true);
         lbl.setBounds(10, y, w, 28);
         lbl.setBorder(BorderFactory.createLineBorder(a.fg.darker(), 1));
         parent.add(lbl);
         y += 34;
      }
      parent.setPreferredSize(new Dimension(w + 20, y + 10));
   }

   static void showNotif(String var0) {
      JFrame var10000 = frame;
      String var10001 = patient.getPhoneNumber();
      JOptionPane.showMessageDialog(var10000, "Notification Sent!\n\nSMS   -> " + var10001 + "\nEmail -> " + patient.getEmail() + "\n\nMessage: " + var0, "Notification", 1);
   }

   static {
      WHITE = Color.WHITE;
      BG = new Color(230, 241, 251);
      GREEN_BG = new Color(234, 243, 222);
      GREEN_FG = new Color(59, 109, 17);
      RED_BG = new Color(252, 235, 235);
      RED_FG = new Color(163, 45, 45);
      AMBER_BG = new Color(250, 238, 218);
      AMBER_FG = new Color(133, 79, 11);
      NAV_LABELS = new String[]{"Dashboard", "Book Appointment", "Update Appointment", "Cancel Appointment", "View History", "My Profile"};
      PANEL_TITLES = new String[]{"Patient Dashboard", "Book Appointment", "Update Appointment", "Cancel Appointment", "View History", "My Profile"};
   }
}