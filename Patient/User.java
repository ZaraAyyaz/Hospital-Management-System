package Patient;

public class User {
   private String userId;
   private String name;
   private String email;
   private String password;
   private String phoneNumber;

   public User(String var1, String var2, String var3, String var4, String var5) {
      this.userId = var1;
      this.name = var2;
      this.email = var3;
      this.password = var4;
      this.phoneNumber = var5;
   }

   public String getUserId() {
      return this.userId;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String var1) {
      this.name = var1;
   }

   public String getEmail() {
      return this.email;
   }

   public void setEmail(String var1) {
      this.email = var1;
   }

   public String getPassword() {
      return this.password;
   }

   public void setPassword(String var1) {
      this.password = var1;
   }

   public String getPhoneNumber() {
      return this.phoneNumber;
   }

   public void setPhoneNumber(String var1) {
      this.phoneNumber = var1;
   }

   public boolean login(String var1, String var2) {
      return this.email.equals(var1) && this.password.equals(var2);
   }

   public void logout() {
      System.out.println(this.name + " logged out.");
   }

   public String getDetails() {
      return "ID: " + userId + " | Name: " + name + " | Email: " + email + " | Phone: " + phoneNumber;
   }

   public void sendNotification(String message) {
      System.out.println("[Notification to " + name + "] " + message);
   }

   public void displayInfo() {
      System.out.println("ID    : " + this.userId);
      System.out.println("Name  : " + this.name);
      System.out.println("Email : " + this.email);
      System.out.println("Phone : " + this.phoneNumber);
   }
}