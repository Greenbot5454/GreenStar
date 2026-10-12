package ir.Greenbot.greenstar;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;

public class StaffJoinListener {

    private final GreenStar plugin;
    private final ProxyServer server;

    public StaffJoinListener(GreenStar plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();

        // فقط اگه پرم استف داشته باشه
        if (!player.hasPermission("greenstar.staff")) return;

        // چک کن توی config فعاله یا نه
        if (!plugin.getConfigBoolean("staff-join.enabled", true)) return;

        String serverName = "Unknown";
        Optional<RegisteredServer> srv = player.getCurrentServer()
                .map(conn -> conn.getServer());
        if (srv.isPresent()) {
            serverName = srv.get().getServerInfo().getName();
        }

        String prefix = plugin.getConfigString("prefix", "&a&lGreenStar &8» &r");
        String format = plugin.getConfigString("staff-join.message",
                "{prefix}&e{player} &7joined the network on &f{server}");

        String output = format
                .replace("{prefix}", prefix)
                .replace("{player}", player.getUsername())
                .replace("{server}", serverName);

        Component component = color(output);

        // فرستادن به همه استف‌ها (به جز خودش)
        for (Player p : server.getAllPlayers()) {
            if (p.hasPermission("greenstar.staff") && !p.getUniqueId().equals(player.getUniqueId())) {
                p.sendMessage(component);
            }
        }

        plugin.getLogger().info("[StaffJoin] " + player.getUsername() + " joined on " + serverName);
    }

    private Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
