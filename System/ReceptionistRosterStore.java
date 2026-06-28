package System;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class ReceptionistRosterStore {

    private static final List<Object[]> receptionists = new CopyOnWriteArrayList<>();
    private static final Map<String, List<String>> shiftRanges = new LinkedHashMap<>();
    private static final List<Runnable> listeners = new CopyOnWriteArrayList<>();

    public static void addReceptionist(String id, String name) {
        receptionists.add(new Object[]{id, name, true});
        shiftRanges.put(id, new CopyOnWriteArrayList<>());
        notifyListeners();
    }

    public static void removeReceptionist(String id) {
        receptionists.removeIf(r -> id.equals(r[0]));
        shiftRanges.remove(id);
        notifyListeners();
    }

    public static void removeReceptionistByName(String name) {
        receptionists.removeIf(r -> name.equals(r[1]));
        notifyListeners();
    }

    public static void setReceptionistActive(String id, boolean active) {
        for (Object[] r : receptionists) {
            if (id.equals(r[0])) { r[2] = active; break; }
        }
        notifyListeners();
    }

    public static boolean isReceptionistActive(String id) {
        for (Object[] r : receptionists) {
            if (id.equals(r[0])) return (Boolean) r[2];
        }
        return false;
    }

    public static String getReceptionistNameById(String id) {
        for (Object[] r : receptionists) if (id.equals(r[0])) return (String) r[1];
        return "";
    }

    public static String getReceptionistIdByName(String name) {
        for (Object[] r : receptionists) if (name.equals(r[1])) return (String) r[0];
        return "";
    }

    public static void addShiftRange(String receptionistId, String range) {
        shiftRanges.computeIfAbsent(receptionistId, k -> new CopyOnWriteArrayList<>()).add(range);
        notifyListeners();
    }

    public static void removeShiftRange(String receptionistId, int index) {
        List<String> list = shiftRanges.get(receptionistId);
        if (list != null && index >= 0 && index < list.size()) {
            list.remove(index);
            notifyListeners();
        }
    }

    public static void clearShiftRanges(String receptionistId) {
        List<String> list = shiftRanges.get(receptionistId);
        if (list != null) list.clear();
        notifyListeners();
    }

    public static void setShiftRanges(String receptionistId, List<String> ranges) {
        shiftRanges.put(receptionistId, new CopyOnWriteArrayList<>(ranges));
        notifyListeners();
    }

    public static List<String> getShiftRanges(String receptionistId) {
        List<String> s = shiftRanges.get(receptionistId);
        return s == null ? Collections.emptyList() : new ArrayList<>(s);
    }

    public static List<Object[]> getActiveReceptionists() {
        List<Object[]> out = new ArrayList<>();
        for (Object[] r : receptionists) {
            if (Boolean.TRUE.equals(r[2])) out.add(Arrays.copyOf(r, r.length));
        }
        return out;
    }

    public static List<Object[]> getAllReceptionists() {
        List<Object[]> out = new ArrayList<>();
        for (Object[] r : receptionists) out.add(Arrays.copyOf(r, r.length));
        return out;
    }

    public static int getActiveReceptionistCount() {
        int count = 0;
        for (Object[] r : receptionists) if (Boolean.TRUE.equals(r[2])) count++;
        return count;
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
