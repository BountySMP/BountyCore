package com.bountysmp.bountyCore.sync;

import com.bountysmp.bountyCore.BountyCore;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Checks the GitHub Release of each configured repo and stages new plugin
 * jars into plugins/. Staged jars are picked up on the next restart. A repo
 * can be pinned to a specific release tag via the "pin" setting.
 */
public class JarSyncTask extends BukkitRunnable {

    private static final String API_BASE = "https://api.github.com/repos/";

    private final BountyCore plugin;
    private final String token;
    private final File stateFile;
    private final List<Source> sources = new ArrayList<>();
    private final Set<String> failedRepos = new HashSet<>(); // log-once tracking
    private final HttpClient http = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10))
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();

    private record Source(String repo, String pin) {}

    public JarSyncTask(BountyCore plugin, ConfigurationSection settings) {
        this.plugin = plugin;
        this.token = settings.getString("github-token", "");
        this.stateFile = new File(plugin.getDataFolder(), "jar-sync-state.yml");
        for (Map<?, ?> raw : settings.getMapList("sources")) {
            Object repo = raw.get("repo");
            if (repo == null || repo.toString().isEmpty()) {
                continue;
            }
            Object pin = raw.get("pin");
            sources.add(new Source(repo.toString(), pin == null ? "" : pin.toString()));
        }
    }

    public void start(int intervalMinutes) {
        long ticks = Math.max(1, intervalMinutes) * 60L * 20L;
        runTaskTimerAsynchronously(plugin, 20L * 10, ticks); // first check ~10s after boot
    }

    @Override
    public void run() {
        for (Source source : sources) {
            try {
                check(source);
                failedRepos.remove(source.repo());
            } catch (Exception e) {
                if (failedRepos.add(source.repo())) {
                    plugin.getLogger().warning("[jar-sync] " + source.repo() + ": " + e.getMessage()
                        + " (will keep retrying quietly)");
                }
            }
        }
    }

    private void check(Source source) throws Exception {
        String endpoint = source.pin().isEmpty()
            ? API_BASE + source.repo() + "/releases/latest"
            : API_BASE + source.repo() + "/releases/tags/" + source.pin();

        JsonObject release = JsonParser.parseString(getJson(endpoint)).getAsJsonObject();
        String tag = release.get("tag_name").getAsString();

        String stateKey = source.repo().replace('/', '_').replace('.', '_');
        YamlConfiguration state = YamlConfiguration.loadConfiguration(stateFile);
        if (tag.equals(state.getString(stateKey))) {
            return; // already staged or applied
        }

        JsonObject jarAsset = null;
        JsonArray assets = release.getAsJsonArray("assets");
        for (int i = 0; i < assets.size(); i++) {
            JsonObject asset = assets.get(i).getAsJsonObject();
            if (asset.get("name").getAsString().endsWith(".jar")) {
                jarAsset = asset;
                break;
            }
        }
        if (jarAsset == null) {
            throw new IllegalStateException("release " + tag + " has no jar asset");
        }

        String assetName = jarAsset.get("name").getAsString();
        File pluginsDir = plugin.getDataFolder().getParentFile();
        Path staging = new File(pluginsDir, assetName + ".sync-tmp").toPath();
        try {
            download(jarAsset.get("url").getAsString(), staging);
            Files.move(staging, new File(pluginsDir, assetName).toPath(),
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(staging);
        }

        state.set(stateKey, tag);
        state.save(stateFile);
        plugin.getLogger().info("[jar-sync] " + source.repo() + " " + tag + " staged (" + assetName
            + ") — applies on restart.");
    }

    private String getJson(String url) throws Exception {
        HttpResponse<String> response = http.send(
            request(url).header("Accept", "application/vnd.github+json").build(),
            HttpResponse.BodyHandlers.ofString());
        int status = response.statusCode();
        if (status == 403 || status == 429) {
            throw new IllegalStateException("GitHub API rate limited (HTTP " + status + ")");
        }
        if (status == 404) {
            throw new IllegalStateException("HTTP 404 — repo or release not found"
                + (token.isEmpty() ? " (private repo? set jar-sync.github-token)" : ""));
        }
        if (status != 200) {
            throw new IllegalStateException("GitHub API returned HTTP " + status);
        }
        return response.body();
    }

    /**
     * Asset downloads redirect from the API to signed storage URLs that
     * reject the Authorization header, so the redirect is followed manually
     * without credentials.
     */
    private void download(String assetUrl, Path target) throws Exception {
        HttpResponse<Path> response = http.send(
            request(assetUrl).header("Accept", "application/octet-stream").build(),
            HttpResponse.BodyHandlers.ofFile(target));

        int status = response.statusCode();
        if (status == 301 || status == 302 || status == 307) {
            String location = response.headers().firstValue("location")
                .orElseThrow(() -> new IllegalStateException("redirect without location header"));
            response = http.send(
                HttpRequest.newBuilder(URI.create(location)).timeout(Duration.ofMinutes(5)).build(),
                HttpResponse.BodyHandlers.ofFile(target));
            status = response.statusCode();
        }
        if (status != 200) {
            Files.deleteIfExists(target);
            throw new IllegalStateException("asset download returned HTTP " + status);
        }
    }

    private HttpRequest.Builder request(String url) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(60))
            .header("X-GitHub-Api-Version", "2022-11-28");
        if (!token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }
        return builder;
    }
}
