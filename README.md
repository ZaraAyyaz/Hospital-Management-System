# Hospital-Management-System
🏥 Excited to share our OOP semester project — Hospital Management System!



My team and I built a fully functional Java Swing desktop application that simulates a real hospital management workflow.



🔹 What it does:

• 4 role-based portals — Patient, Doctor, Receptionist & Admin — each with a dedicated dashboard

• Real-time appointment booking with conflict prevention across all modules

• Emergency queue management with priority triage (Critical → Low)

• Prescription issuance, patient medical history, and doctor roster management

• Live data sync across all modules — no double-bookings, no stale views



🔹 Tech & concepts applied:

• Java Swing with custom-painted components for a modern UI

• OOP pillars — Encapsulation, Inheritance, Polymorphism & Abstraction used throughout

• Observer/Listener pattern for cross-module data propagation

• Centralized HospitalSystem class as a shared data bus

• Singleton pattern for DoctorDataStore



One of the most challenging parts was ensuring live consistency across 4 independent packages without circular dependencies — solved using a centralized slot tracker and a listener-driven architecture.
