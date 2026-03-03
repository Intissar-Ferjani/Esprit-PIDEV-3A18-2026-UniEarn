package uniearn.services.users.security;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

//Tracks failed login attempts per email (in-memory)
//+ detects local IP + supports manual lock from the email "Not me" button.

public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final int LOCK_MINUTES = 15;

    private static final Map<String, AttemptState> states = new HashMap<>();

    private static class AttemptState {
        int           failCount   = 0;
        LocalDateTime lockedUntil = null;
    }

    private static AttemptState stateFor(String email) {
        return states.computeIfAbsent(email.toLowerCase(), k -> new AttemptState());
    }

    // ── Attempt tracking ──────────────────────────────────────────────────

    public static int recordFailure(String email) {
        AttemptState s = stateFor(email);
        s.failCount++;
        if (s.failCount >= MAX_ATTEMPTS) {
            s.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
            System.out.println("⚠ Auto-locked after 3 failures: " + email);
        }
        return s.failCount;
    }

    /** Called by SecurityCallbackServer when the user clicks "No, lock it" in the email. */
    public static void lockTemporarily(String email) {
        AttemptState s = stateFor(email);
        s.lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
        s.failCount   = MAX_ATTEMPTS;
        System.out.println("🔒 Manually locked via email button: " + email);
    }

    public static boolean isLocked(String email) {
        AttemptState s = stateFor(email);
        if (s.lockedUntil == null) return false;
        if (LocalDateTime.now().isAfter(s.lockedUntil)) { reset(email); return false; }
        return true;
    }

    public static String remainingLockTime(String email) {
        AttemptState s = stateFor(email);
        if (s.lockedUntil == null || LocalDateTime.now().isAfter(s.lockedUntil)) return "0:00";
        long sec = Duration.between(LocalDateTime.now(), s.lockedUntil).getSeconds();
        return String.format("%d:%02d", sec / 60, sec % 60);
    }

    public static int  getFailCount(String email) { return stateFor(email).failCount; }
    public static void reset(String email)         { states.remove(email.toLowerCase()); }
    public static int  maxAttempts()               { return MAX_ATTEMPTS; }
    public static int  lockMinutes()               { return LOCK_MINUTES; }

    // ── IP detection ──────────────────────────────────────────────────────

    public static String detectLocalIp() {
        try {
            InetAddress local = InetAddress.getLocalHost();
            if (!local.isLoopbackAddress()) return local.getHostAddress();

            Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface ni : Collections.list(ifaces)) {
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) continue;
                for (InetAddress addr : Collections.list(ni.getInetAddresses())) {
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().contains("."))
                        return addr.getHostAddress();
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ IP detection failed: " + e.getMessage());
        }
        return "Inconnue";
    }
}