package me.TechsCode.UltraPermissions.base.security;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;

/** At most 64 sync jobs process-wide; repeated notifications coalesce without losing the final refresh. */
public final class SyncBudget {
    private static final Semaphore GLOBAL = new Semaphore(64);
    private final Map<String, Pending> pending = new HashMap<>();
    private static final class Pending { boolean dirty; }

    public boolean submit(Consumer<Runnable> scheduler, String key, Runnable task) {
        Pending entry;
        synchronized (this) {
            entry = pending.get(key);
            if (entry != null) { entry.dirty = true; return true; }
            if (!GLOBAL.tryAcquire()) return false;
            entry = new Pending(); pending.put(key, entry);
        }
        Pending submitted = entry;
        try { scheduler.accept(() -> run(scheduler, key, task, submitted)); }
        catch (RuntimeException rejected) { release(key, submitted); throw rejected; }
        return true;
    }

    private void run(Consumer<Runnable> scheduler, String key, Runnable task, Pending entry) {
        try { task.run(); }
        catch (RuntimeException | Error failure) { release(key, entry); throw failure; }
        synchronized (this) {
            if (!entry.dirty) { release(key, entry); return; }
            entry.dirty = false;
        }
        try { scheduler.accept(() -> run(scheduler, key, task, entry)); }
        catch (RuntimeException rejected) { release(key, entry); throw rejected; }
    }

    private synchronized void release(String key, Pending entry) {
        if (pending.remove(key, entry)) GLOBAL.release();
    }
}
