package net.bridgesapis.bungeebridge.permissions;

import net.bridgesapis.bungeebridge.permissions.commands.CommandGroups;
import net.md_5.bungee.api.ProxyServer;
import net.bridgesapis.bungeebridge.BungeeBridge;
import net.bridgesapis.bungeebridge.permissions.commands.CommandRefresh;
import net.bridgesapis.bungeebridge.permissions.commands.CommandUsers;
import net.zyuiop.crosspermissions.api.PermissionsAPI;
import net.zyuiop.crosspermissions.api.database.Database;
import net.zyuiop.crosspermissions.api.rawtypes.RawPlayer;
import net.zyuiop.crosspermissions.api.rawtypes.RawPlugin;
import redis.clients.jedis.Jedis;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PermissionsBridge implements RawPlugin {

	private final BungeeBridge plugin;
	private final PermissionsAPI api;

	public PermissionsBridge(BungeeBridge plugin) {
		this.plugin = plugin;


		this.api = new PermissionsAPI(this, "Joueur", new Database() {
			@Override
			public Jedis getJedis() {
				return PermissionsBridge.this.plugin.getConnector().getResource();
			}
		});


		ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new CommandGroups(api));
		ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new CommandRefresh(api));
		ProxyServer.getInstance().getPluginManager().registerCommand(plugin, new CommandUsers(api));

		ProxyServer.getInstance().getPluginManager().registerListener(plugin, new PlayerListener(this, plugin));
	}

	public PermissionsAPI getApi() {
		return api;
	}

	@Override
	public void logSevere(String log) {
		plugin.getLogger().severe(log);
	}

	@Override
	public void logWarning(String log) {
		plugin.getLogger().warning(log);
	}

	@Override
	public void logInfo(String log) {
		plugin.getLogger().info(log);
	}

	@Override
	public void runRepeatedTaskAsync(Runnable task, long delay, long timeBeforeRun) {
		plugin.getProxy().getScheduler().schedule(plugin, task, timeBeforeRun * 50, delay * 50, TimeUnit.MILLISECONDS);
	}

	@Override
	public void runAsync(Runnable task) {
		plugin.getExecutor().addTask(task::run);
	}

	@Override
	public boolean isOnline(UUID player) {
		//return plugin.getNetworkBridge().isOnline(player);
		return ProxyServer.getInstance().getPlayer(player) != null;
	}

	@Override
	public RawPlayer getPlayer(UUID player) {
		return new VirtPlayer(player);
	}

	@Override
	public UUID getPlayerId(String name) {
		return BungeeBridge.getInstance().getUuidTranslator().getUUID(name, false);
	}

	@Override
	public String getPlayerName(UUID id) {
		return null;
	}

	public UUID getPrimary() {
		return UUID.fromString("d1da3865-75a7-4371-a3b6-eeace78d603f");
	}
}
