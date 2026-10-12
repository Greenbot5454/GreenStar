package ir.Greenbot.greenstar;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.Optional;

public class StaffChatCommand implements SimpleCommand {

    private final GreenStar plugin;
    private final ProxyServer server;

    public StaffChatCommand(GreenStar plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("greenstar.staff")) {
            invocation.source().sendMessage(color(plugin.getConfigString("messages.no-permission",
                    "&cYou don't have permission!")));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(color(plugin.getConfigString("messages.usage-staff",
                    "&eUsage: /staff <message>")));
            return;
        }

        String message = String.join(" ", args);
        message = message.replace("\\n", "\n");

        String senderName;
        String serverName = "Unknown";

        if (invocation.source() instanceof Player p) {
            senderName = p.getUsername();
            Optional<RegisteredServer> srv = p.getCurrentServer()
                    .map(conn -> conn.getServer());
            if (srv.isPresent()) {
                serverName = srv.get().getServerInfo().getName();
            }
        } else {
            senderName = "Console";
        }

        String prefix = plugin.getConfigString("prefix", "&a&lGreenStar &8» &r");
        String format = plugin.getConfigString("staff-chat.format",
                "{prefix}&b[Staff] &f{sender} &8» &7{message}");

        String output = format
                .replace("{prefix}", prefix)
                .replace("{message}", message)
                .replace("{sender}", senderName)
                .replace("{server}", serverName);

        Component component = color(output);

        // فرستادن فقط به کسایی که پرم استف دارن
        for (Player player : server.getAllPlayers()) {
            if (player.hasPermission("greenstar.staff")) {
                player.sendMessage(component);
            }
        }

        plugin.getLogger().info("[Staff] " + senderName + ": " + message);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("greenstar.staff");
    }

    private Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
