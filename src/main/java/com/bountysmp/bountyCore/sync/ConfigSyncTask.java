package com.bountysmp.bountyCore.sync;

import com.bountysmp.bountyCore.BountyCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Polls the config repo checked out in the server root and hard-resets onto
 * origin/&lt;branch&gt; whenever new commits arrive. Completely silent while
 * up to date; on git or network failure it logs once and keeps polling.
 */
public class ConfigSyncTask extends BukkitRunnable {

    private final BountyCore plugin;
    private final File serverRoot;
    private final String branch;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private boolean failureLogged = false;

    public ConfigSyncTask(BountyCore plugin, ConfigurationSection settings) {
        this.plugin = plugin;
        this.branch = settings.getString("branch", "main");
        String rootOverride = settings.getString("server-root", "");
        this.serverRoot = rootOverride.isEmpty()
            ? plugin.getDataFolder().getParentFile().getParentFile()
            : new File(rootOverride);
    }

    public void start(int pollIntervalSeconds) {
        long ticks = Math.max(1, pollIntervalSeconds) * 20L;
        runTaskTimerAsynchronously(plugin, ticks, ticks);
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            return; // previous poll still in flight
        }
        try {
            poll();
        } catch (Exception e) {
            logFailureOnce(e.getMessage());
        } finally {
            running.set(false);
        }
    }

    private void poll() throws Exception {
        GitResult fetch = git("fetch", "--quiet", "origin", branch);
        if (fetch.exitCode != 0) {
            logFailureOnce("git fetch failed: " + fetch.firstLine());
            return;
        }

        GitResult heads = git("rev-parse", "HEAD", "origin/" + branch);
        if (heads.exitCode != 0 || heads.lines.size() < 2) {
            logFailureOnce("git rev-parse failed: " + heads.firstLine());
            return;
        }
        String local = heads.lines.get(0);
        String remote = heads.lines.get(1);
        if (local.equals(remote)) {
            failureLogged = false; // healthy and up to date: say nothing
            return;
        }

        List<String> messages = git("log", "--pretty=%s", local + ".." + remote).lines;
        GitResult reset = git("reset", "--hard", "origin/" + branch);
        if (reset.exitCode != 0) {
            logFailureOnce("git reset failed: " + reset.firstLine());
            return;
        }

        failureLogged = false;
        String summary = messages.isEmpty() ? remote.substring(0, 7) : String.join(" | ", messages);
        plugin.getLogger().info("[config-sync] Applied " + Math.max(1, messages.size())
            + " config commit(s): " + summary
            + " — run /config reload <name> (or all) to apply in-game.");
    }

    private void logFailureOnce(String detail) {
        if (!failureLogged) {
            failureLogged = true;
            plugin.getLogger().warning("[config-sync] " + detail + " (will keep retrying quietly)");
        }
    }

    private GitResult git(String... args) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("git");
        Collections.addAll(command, args);

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.directory(serverRoot);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("git timed out: git " + String.join(" ", args));
        }
        return new GitResult(process.exitValue(), lines);
    }

    private record GitResult(int exitCode, List<String> lines) {
        String firstLine() {
            return lines.isEmpty() ? "(no output)" : lines.get(0);
        }
    }
}
