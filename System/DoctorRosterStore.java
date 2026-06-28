package System;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DoctorRosterStore {

    private static final List<Object[]> doctors = new CopyOnWriteArrayList<>();
    private static final Map<String, List<String>> shiftRanges = new LinkedHashMap<>();
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public static void addDoctor(String id, String name, String dept) {
        doctors.add(new Object[]{id, name, dept, true});
        shiftRanges.put(id, new CopyOnWriteArrayList<>());
        notifyListeners();
    }

    public static void removeDoctor(String id) {
        doctors.removeIf(d -> id.equals(d[0]));
        shiftRanges.remove(id);
        notifyListeners();
    }

    public static void setDoctorActive(String id, boolean active) {
        for (Object[] d : doctors) {
            if (id.equals(d[0])) { d[3] = active; break; }
        }
        notifyListeners();
    }

    public static boolean isDoctorActive(String id) {
        for (Object[] d : doctors) {
            if (id.equals(d[0])) return (Boolean) d[3];
        }
        return false;
    }

    public static String getDoctorNameById(String id) {
        for (Object[] d : doctors) if (id.equals(d[0])) return (String) d[1];
        return "";
    }

    public static String getDoctorIdByName(String name) {
        for (Object[] d : doctors) if (name.equals(d[1])) return (String) d[0];
        return "";
    }

    public static String getDoctorDepartmentByName(String name) {
        for (Object[] d : doctors) if (name.equals(d[1])) return (String) d[2];
        return null;
    }

    public static void addShiftRange(String doctorId, String range) {
        shiftRanges.computeIfAbsent(doctorId, k -> new CopyOnWriteArrayList<>()).add(range);
        notifyListeners();
    }

    public static void removeShiftRange(String doctorId, int index) {
        List<String> list = shiftRanges.get(doctorId);
        if (list != null && index >= 0 && index < list.size()) {
            list.remove(index);
            notifyListeners();
        }
    }

    public static void clearShiftRanges(String doctorId) {
        List<String> list = shiftRanges.get(doctorId);
        if (list != null) list.clear();
        notifyListeners();
    }

    public static void setShiftRanges(String doctorId, List<String> ranges) {
        shiftRanges.put(doctorId, new CopyOnWriteArrayList<>(ranges));
        notifyListeners();
    }

    public static List<Object[]> getActiveDoctors() {
        List<Object[]> out = new ArrayList<>();
        for (Object[] d : doctors) {
            if (Boolean.TRUE.equals(d[3])) out.add(Arrays.copyOf(d, d.length));
        }
        return out;
    }

    public static List<Object[]> getAllDoctors() {
        List<Object[]> out = new ArrayList<>();
        for (Object[] d : doctors) out.add(Arrays.copyOf(d, d.length));
        return out;
    }

    public static List<String> getShiftRanges(String doctorId) {
        List<String> s = shiftRanges.get(doctorId);
        return s == null ? Collections.emptyList() : new ArrayList<>(s);
    }

    public static List<String> getExpandedTimeSlots(String doctorId) {
        List<String> ranges = getShiftRanges(doctorId);
        Set<String> slots = new LinkedHashSet<>();
        for (String range : ranges) {
            slots.addAll(expandRange(range));
        }
        return new ArrayList<>(slots);
    }

    public static List<String> getExpandedTimeSlotsByName(String doctorName) {
        String id = getDoctorIdByName(doctorName);
        return id.isEmpty() ? Collections.emptyList() : getExpandedTimeSlots(id);
    }

    private static List<String> expandRange(String range) {
        List<String> out = new ArrayList<>();
        Pattern p = Pattern.compile("(\\d+)\\s*(am|pm)\\s*to\\s*(\\d+)\\s*(am|pm)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(range.trim());
        if (!m.matches()) return out;
        int s = Integer.parseInt(m.group(1));
        boolean sPm = m.group(2).equalsIgnoreCase("pm");
        int e = Integer.parseInt(m.group(3));
        boolean ePm = m.group(4).equalsIgnoreCase("pm");
        int s24 = sPm ? (s == 12 ? 12 : s + 12) : (s == 12 ? 0 : s);
        int e24 = ePm ? (e == 12 ? 12 : e + 12) : (e == 12 ? 0 : e);
        int cur = s24 * 60;
        int end = e24 * 60;
        while (true) {
            int h = cur / 60;
            int m2 = cur % 60;
            int h12 = h % 12; if (h12 == 0) h12 = 12;
            String ampm = h < 12 ? "AM" : "PM";
            out.add(String.format("%02d:%02d %s", h12, m2, ampm));
            if (cur == end) break;
            cur += 30;
            if (cur >= 1440) cur -= 1440;
        }
        return out;
    }

    public static int getActiveDoctorCount() {
        int count = 0;
        for (Object[] d : doctors) if (Boolean.TRUE.equals(d[3])) count++;
        return count;
    }

    public static Set<String> getDepartments() {
        Set<String> depts = new LinkedHashSet<>();
        for (Object[] d : doctors) {
            if (Boolean.TRUE.equals(d[3])) depts.add((String) d[2]);
        }
        return depts;
    }

    public static void addListener(Runnable r) {
        listeners.add(r);
    }

    public static void removeListener(Runnable r) {
        listeners.remove(r);
    }

    public static void notifyListeners() {
        for (Runnable r : listeners) {
            try { r.run(); } catch (Exception e) { e.printStackTrace(); }
        }
    }
}
