package Patient;

import java.io.PrintStream;

public class Appointment {
   private String appointmentId;
   private String doctorName;
   private String specialty;
   private String date;
   private String timeSlot;
   private String status;
   private Patient patient;
    private String type;
    private String notes;

    public Appointment(String var1, String var2, String var3, String var4, String var5) {
       this.appointmentId = var1;
       this.doctorName = var2;
       this.specialty = var3;
       this.date = var4;
       this.timeSlot = var5;
       this.status = "Pending";
       this.type = "General";
       this.notes = "";
    }

   public String getAppointmentId() {
      return this.appointmentId;
   }

   public String getDoctorName() {
      return this.doctorName;
   }

   public String getSpecialty() {
      return this.specialty;
   }

   public String getDate() {
      return this.date;
   }

   public void setDate(String var1) {
      this.date = var1;
   }

   public String getTimeSlot() {
      return this.timeSlot;
   }

   public void setTimeSlot(String var1) {
      this.timeSlot = var1;
   }

   public String getStatus() {
      return this.status;
   }

   public void setStatus(String var1) {
      this.status = var1;
   }

   public String getDateTime() {
      return this.date + " " + this.timeSlot;
   }

   public Patient getPatient() {
      return this.patient;
   }

   public void setPatient(Patient var1) {
      this.patient = var1;
   }

   public String getType() {
      return this.type;
   }

   public void setType(String var1) {
      this.type = var1;
   }

   public String getNotes() {
      return this.notes;
   }

   public void setNotes(String var1) {
      this.notes = var1;
   }

   public void schedule() {
      this.status = "Pending";
   }

   public void cancel() {
      this.status = "Cancelled";
   }

   public void update(String var1, String var2) {
      this.date = var1;
      this.timeSlot = var2;
      if (this.status.equals("Scheduled")) {
         this.status = "Pending";
      }
   }

   public void sendReminder() {
      PrintStream var10000 = System.out;
      String var10001 = this.appointmentId;
      var10000.println("[Reminder] Appointment " + var10001 + " with " + this.doctorName + " on " + this.date + " at " + this.timeSlot);
   }
}