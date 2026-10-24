package org.example.common;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginFailCounter {

    public static final int MAX_FAIL_COUNT = 5;
    public static final long LOCK_MILLIS = 30 * 60 * 1000L;

    private final Map<String, FailRecord> records = new ConcurrentHashMap<>();

    public synchronized void recordFail(String account) {
        FailRecord record = records.computeIfAbsent(account, k -> new FailRecord());
        record.count++;
        if (record.count >= MAX_FAIL_COUNT) {
            record.lockUntil = System.currentTimeMillis() + LOCK_MILLIS;
        }
    }

    public synchronized void clear(String account) {
        records.remove(account);
    }

    public boolean isLocked(String account) {
        FailRecord record = records.get(account);
        if (record == null) return false;
        if (record.lockUntil != 0 && System.currentTimeMillis() > record.lockUntil) {
            records.remove(account);
            return false;
        }
        return record.lockUntil != 0;
    }

    public long remainingLockMillis(String account) {
        FailRecord record = records.get(account);
        if (record == null || record.lockUntil == 0) return 0;
        return Math.max(0, record.lockUntil - System.currentTimeMillis());
    }

    public int failCount(String account) {
        FailRecord record = records.get(account);
        return record == null ? 0 : record.count;
    }

    private static class FailRecord {
        int count;
        long lockUntil;
    }
}