package ir.Greenbot.greenstar;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Plugin(
    id = "greenstar",
    name = "GreenStar",
    version = "1.0.0",
    description = "Broadcast & Staff Chat plugin for Velocity",
    authors = {"Greenbot"}
)
public class GreenStar {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private Map<String, Object> config;

    @Inject
    public GreenStar(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        logger.info("GreenStar loading...");

        if (!dataDirectory.toFile().exists()) {
            dataDirectory.toFile().mkdirs();
        }

        saveDefaultConfig();
        loadConfig();

        CommandManager cm = server.getCommandManager();

        // /bc
        CommandMeta bcMeta = cm.metaBuilder("bc")
                .aliases("broadcast")
                .plugin(this)
                .build();
        cm.register(bcMeta, new BroadcastCommand(this, server));

        // /staff
        CommandMeta staffMeta = cm.metaBuilder("staff")
                .aliases("sc")
                .plugin(this)
                .build();
        cm.register(staffMeta, new StaffChatCommand(this, server));

        // listener جوین استف
        server.getEventManager().register(this, new StaffJoinListener(this, server));

        logger.info("GreenStar enabled!");
    }

    // کپی کردن config.yml از JAR اگه وجود نداشت
    private void saveDefaultConfig() {
        Path configFile = dataDirectory.resolve("config.yml");
        if (Files.exists(configFile)) return;
        try (InputStream in = getClass().getResourceAsStream("/config.yml")) {
            if (in == null) {
                logger.warn("config.yml not found inside JAR!");
                return;
            }
            Files.copy(in, configFile);
            logger.info("Created default config.yml");
        } catch (IOException e) {
            logger.error("Failed to copy config.yml", e);
        }
    }

    // خواندن config.yml
    @SuppressWarnings("unchecked")
    public void loadConfig() {
        Path configFile = dataDirectory.resolve("config.yml");
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            Yaml yaml = new Yaml();
            Object data = yaml.load(reader);
            if (data instanceof Map) {
                this.config = (Map<String, Object>) data;
            } else {
                this.config = new HashMap<>();
            }
        } catch (Exception e) {
            logger.error("Failed to load config.yml", e);
            this.config = new HashMap<>();
        }
    }

    // گرفتن یه مقدار از config
    @SuppressWarnings("unchecked")
    public String getConfigString(String path, String def) {
        if (config == null) return def;
        String[] parts = path.split("\\.");
        Object current = config;
        for (String part : parts) {
            if (!(current instanceof Map)) return def;
            current = ((Map<String, Object>) current).get(part);
            if (current == null) return def;
        }
        return String.valueOf(current);
    }

    public boolean getConfigBoolean(String path, boolean def) {
        String val = getConfigString(path, String.valueOf(def));
        return Boolean.parseBoolean(val);
    }

    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }
}
