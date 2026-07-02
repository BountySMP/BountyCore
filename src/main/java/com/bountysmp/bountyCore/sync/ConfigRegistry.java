package com.bountysmp.bountyCore.sync;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central registry of reloadable configs. Each module registers its config
 * file with a callback that fully re-reads the file from disk; /config
 * resolves names against this registry.
 */
public class ConfigRegistry {

    public enum Result { RELOADED, UNKNOWN, FAILED }

    public static final class Entry {
        private final String name;
        private final File file;
        private final Runnable reloadCallback;
        private volatile long lastReloadMillis;

        private Entry(String name, File file, Runnable reloadCallback) {
            this.name = name;
            this.file = file;
            this.reloadCallback = reloadCallback;
            this.lastReloadMillis = System.currentTimeMillis();
        }

        public String getName() {
            return name;
        }

        /** True when the file on disk changed after the last reload (or startup). */
        public boolean hasUnappliedChanges() {
            return file.exists() && file.lastModified() > lastReloadMillis;
        }
    }

    private final Logger logger;
    private final Map<String, Entry> entries = new LinkedHashMap<>();

    public ConfigRegistry(Logger logger) {
        this.logger = logger;
    }

    public void register(String name, File file, Runnable reloadCallback) {
        entries.put(name.toLowerCase(), new Entry(name.toLowerCase(), file, reloadCallback));
    }

    public Result reload(String name) {
        Entry entry = entries.get(name.toLowerCase());
        if (entry == null) {
            return Result.UNKNOWN;
        }
        try {
            entry.reloadCallback.run();
            entry.lastReloadMillis = System.currentTimeMillis();
            return Result.RELOADED;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to reload config '" + entry.name + "'", e);
            return Result.FAILED;
        }
    }

    /** Reloads every registered config; returns the names that failed. */
    public List<String> reloadAll() {
        List<String> failed = new ArrayList<>();
        for (Entry entry : entries.values()) {
            if (reload(entry.name) != Result.RELOADED) {
                failed.add(entry.name);
            }
        }
        return failed;
    }

    public Collection<Entry> getEntries() {
        return Collections.unmodifiableCollection(entries.values());
    }

    public List<String> getNames() {
        return new ArrayList<>(entries.keySet());
    }
}
