package net.bridgesapis.bungeebridge.listeners;

import net.bridgesapis.bungeebridge.BungeeBridge;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.Title;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PlayerJoinEvent implements Listener {

	protected BungeeBridge plugin;

	public PlayerJoinEvent(BungeeBridge plugin) {
		this.plugin = plugin;
	}

	/**
	 * Event appelé a la connexion finie du joueur
	 * Permet de lui dire bonjour et de prévenir ses amis qu'il est là
	 * @param e
	 */
	@EventHandler
	public void onJoin(final PostLoginEvent e) {
		final ProxiedPlayer p = e.getPlayer();
		ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
			TextComponent welcome = new TextComponent("Bienvenue, " + p.getName() + " !");
			welcome.setColor(ChatColor.GOLD);

			if (plugin.hasFriends()) {
				TextComponent message = new TextComponent("Amis en ligne : ");
				message.setColor(ChatColor.AQUA);

				ArrayList<String> onLine = new ArrayList<>();
				try {
					onLine.addAll(plugin.getFriendsManagement().onlineAssociatedFriendsList(e.getPlayer().getUniqueId()).values().stream().collect(Collectors.toList()));
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				String lines = StringUtils.join(onLine, ", ");
				TextComponent line;

				if (lines != null && !lines.equals("")) {
					line = new TextComponent(lines);
					line.setColor(ChatColor.GREEN);
				} else {
					line = new TextComponent("Aucun");
					line.setColor(ChatColor.RED);
				}

				message.addExtra(line);
				p.sendMessage(message);
			}

			String key = "rejoinlist:" + e.getPlayer().getUniqueId().toString();
			Jedis cache = plugin.getConnector().getBungeeResource();
			String srv = cache.get(key);
			cache.close();
			if(srv != null) {
				final ServerInfo server = ProxyServer.getInstance().getServerInfo(srv);
				if(server != null)
					ProxyServer.getInstance().getScheduler().schedule(plugin, () -> e.getPlayer().connect(server, (aBoolean, throwable) -> {
						if (aBoolean) {
							p.sendMessage(new ComponentBuilder("").color(ChatColor.GREEN).append("Vous avez été remis en jeu.").create());
						}
					}), 200L, TimeUnit.MILLISECONDS);

			}
		});
	}
}
