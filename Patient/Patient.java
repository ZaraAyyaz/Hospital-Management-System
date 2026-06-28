package Patient;

import java.io.PrintStream;
import java.util.ArrayList;

public class Patient extends User {
   private String patientId;
   private int age;
   private String dateOfBirth;
   private String bloodGroup;
   private String gender;
   private ArrayList<String[]> medicalHistory;
   private ArrayList<Appointment> appointments;

   public Patient(String var1, String var2, String var3, String var4, String var5, int var6, String var7) {
      super(var1, var2, var3, var4, var5);
      this.patientId = var1;
      this.age = var6;
      this.dateOfBirth = "";
      this.bloodGroup = var7;
      this.medicalHistory = new ArrayList<>();
      this.appointments = new ArrayList<>();
   }

   public String getPatientId() {
      return this.patientId;
   }

   public int getAge() {
      return this.age;
   }

   public void setAge(int var1) {
      this.age = var1;
   }

   public String getDateOfBirth() {
      return this.dateOfBirth;
   }

   public void setDateOfBirth(String var1) {
      this.dateOfBirth = var1;
   }

   public String getBloodGroup() {
      return this.bloodGroup;
   }

   public void setBloodGroup(String var1) {
      this.bloodGroup = var1;
   }

   public String getGender() {
      return this.gender;
   }

   public void setGender(String var1) {
      this.gender = var1;
   }

   public ArrayList<String[]> getMedicalHistory() {
      return this.medicalHistory;
   }

   public ArrayList<Appointment> getAppointments() {
      return this.appointments;
   }

   public ArrayList<String[]> viewHistory() {
      return this.medicalHistory;
   }

   public void updateRecord(String var1, String var2, String var3, String var4, String var5) {
      this.medicalHistory.add(new String[]{var1, var2, var3, var4, var5});
   }

   public void registerPatient() {
      System.out.println("Patient registered: " + this.getName());
      this.displayInfo();
   }

   public void bookAppointment(String var1, String var2, String var3, String var4, String var5) {
      Appointment a = new Appointment(var1, var2, var3, var4, var5);
      a.setPatient(this);
      this.appointments.add(a);
      this.sendSMS("Appointment requested with " + var2 + " on " + var4 + " at " + var5 + " — Awaiting confirmation");
      this.sendEmail("Your appointment request with " + var2 + " for " + var4 + " at " + var5 + " has been submitted for doctor approval");
      notifyDoctor("New appointment request from " + this.getName() + " (" + var1 + ") with " + var2 + " on " + var4 + " at " + var5);
   }

   public boolean updateAppointment(String var1, String var2, String var3) {
      for(Appointment var5 : this.appointments) {
          if (var5.getAppointmentId().equals(var1) && (var5.getStatus().equals("Pending") || var5.getStatus().equals("Scheduled"))) {
            var5.setDate(var2);
            var5.setTimeSlot(var3);
            if (var5.getStatus().equals("Scheduled")) {
               var5.setStatus("Pending");
            }
            this.sendSMS("Appointment " + var1 + " rescheduled to " + var2 + " at " + var3);
            this.sendEmail("Your appointment " + var1 + " has been rescheduled to " + var2 + " at " + var3);
            notifyDoctor("Appointment " + var1 + " rescheduled by " + this.getName() + " to " + var2 + " at " + var3 + " — pending approval");
            return true;
         }
      }

      return false;
   }

   public boolean cancelAppointment(String var1) {
      for(Appointment var3 : this.appointments) {
          if (var3.getAppointmentId().equals(var1) && (var3.getStatus().equals("Pending") || var3.getStatus().equals("Scheduled"))) {
            var3.setStatus("Cancelled");
            this.sendSMS("Appointment " + var1 + " has been cancelled.");
            this.sendEmail("Your appointment " + var1 + " has been cancelled successfully.");
            notifyDoctor("Appointment " + var1 + " cancelled by " + this.getName());
            return true;
         }
      }

      return false;
   }

   public void addMedicalHistory(String var1, String var2, String var3, String var4, String var5) {
      this.medicalHistory.add(new String[]{var1, var2, var3, var4, var5});
   }

   void notifyDoctor(String var1) {
      System.out.println("[Doctor Notification] " + var1);
   }

   public void sendSMS(String var1) {
      PrintStream var10000 = System.out;
      String var10001 = this.getPhoneNumber();
      var10000.println("[SMS -> " + var10001 + "] " + var1);
   }

   public void sendEmail(String var1) {
      PrintStream var10000 = System.out;
      String var10001 = this.getEmail();
      var10000.println("[Email -> " + var10001 + "] " + var1);
   }

   public void displayInfo() {
      super.displayInfo();
      System.out.println("Patient ID  : " + this.patientId);
      System.out.println("Age         : " + this.age);
      System.out.println("Gender      : " + this.gender);
      System.out.println("Blood Group : " + this.bloodGroup);
   }
}