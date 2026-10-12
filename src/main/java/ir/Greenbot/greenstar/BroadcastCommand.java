package ir.Greenbot.greenstar;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class BroadcastCommand implements SimpleCommand {

    private final GreenStar plugin;
    private final ProxyServer server;

    public BroadcastCommand(GreenStar plugin, ProxyServer server) {
        this.plugin = plugin;
        this.server = server;
    }

    @Override
    public void execute(Invocation invocation) {
        if (!invocation.source().hasPermission("greenstar.broadcast")) {
            invocation.source().sendMessage(color(plugin.getConfigString("messages.no-permission",
                    "&cYou don't have permission!")));
            return;
        }

        String[] args = invocation.arguments();
        if (args.length == 0) {
            invocation.source().sendMessage(color(plugin.getConfigString("messages.usage-bc",
                    "&eUsage: /bc <message>")));
            return;
        }

        String message = String.join(" ", args);

        // پشتیبانی از \n برای خط جدید
        message = message.replace("\\n", "\n");

        String senderName = invocation.source() instanceof Player p ? p.getUsername() : "Console";
        String prefix = plugin.getConfigString("prefix", "&a&lGreenStar &8» &r");
        String format = plugin.getConfigString("broadcast.format",
                "{prefix}&6[Broadcast] &r{message}");

        // جایگزینی placeholderها
        String output = format
                .replace("{prefix}", prefix)
                .replace("{message}", message)
                .replace("{sender}", senderName);

        // تبدیل به Component (با پشتیبانی چند خط)
        Component component = color(output);

        // فرستادن به همه‌ی بازیکن‌ها
        for (Player player : server.getAllPlayers()) {
            player.sendMessage(component);
        }

        // لاگ توی کنسول
        plugin.getLogger().info("[Broadcast] " + senderName + ": " + message);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("greenstar.broadcast");
    }

    private Component color(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text);
    }
}
