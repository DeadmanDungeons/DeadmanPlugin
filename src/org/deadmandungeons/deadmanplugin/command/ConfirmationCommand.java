package org.deadmandungeons.deadmanplugin.command;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.deadmandungeons.deadmanplugin.DeadmanPlugin;

/**
 * This abstract class is useful for actions where a player may need to confirm before the action is carried out.<br>
 * When an instance of this class is constructed and registered
 * using {@link DeadmanExecutor#registerConfirmationCommand(ConfirmationCommand, String, String)}, two {@link PseudoCommand}s are
 * registered for the specified acceptCmd and declineCmd strings. A player that has been prompted to accept/decline a certain action
 * should be added to the respective ConfirmationCommand by calling {@link #addPromptedPlayer(Player, Object)}. Once a player
 * has been declared to be 'prompted', if they execute either the acceptCmd or declineCmd PseudoCommands, the respective event
 * methods {@link #onAccept(Player, Object)} and {@link #onDecline(Player, Object)} will be called. <br>
 * If the ConfirmationCommand was constructed to specify a timeout, the 'prompted' player will be removed from the list of prompted players
 * if they failed to accept/decline within the timeout, and the {@link #onTimeout(Player, Object)} event method will be called.<br>
 * A player can only be 'prompted' by one ConfirmationCommand for all plugins. If {@link #addPromptedPlayer(Player, Object)} is
 * called for a player that is already 'prompted', they will be removed from the previous confirmation and added to the new one.<br>
 * If a player is 'prompted', and they quit, they will be removed from the promptedPlayers datastore.
 * @param <T> - The type of the data object that should be stored when a player is prompted
 * @author Jon
 */
public abstract class ConfirmationCommand<T> {
	
	// static map so that a player can only be prompted for one confirmation between all plugins
	private static final Map<UUID, ConfirmationInfo<?>> promptedPlayers = new HashMap<UUID, ConfirmationInfo<?>>();
	
	private static final PseudoCommand acceptCommand = new AcceptCommand();
	private static final PseudoCommand declineCommand = new DeclineCommand();
	
	private final DeadmanPlugin plugin;
	private final Class<T> type;
	private final int timeout;
	
	/**
	 * @param plugin - The {@link DeadmanPlugin} that this ConfirmationCommand belongs to
	 * @param type - The Class of the data object that should be stored when a player is prompted
	 * @throws IllegalArgumentException if plugin or type is null
	 */
	public ConfirmationCommand(DeadmanPlugin plugin, Class<T> type) {
		this(plugin, type, -1);
	}
	
	/**
	 * @param plugin - The {@link DeadmanPlugin} that this ConfirmationCommand belongs to
	 * @param type - The Class of the data object that should be stored when a player is prompted
	 * @param timeout - The time in seconds that a player has to either confirm or decline when they have been prompted
	 * @throws IllegalArgumentException if plugin or type is null
	 */
	public ConfirmationCommand(DeadmanPlugin plugin, Class<T> type, int timeout) {
		Validate.notNull(plugin, "plugin cannot be null");
		Validate.notNull(type, "type cannot be null");
		this.plugin = plugin;
		this.type = type;
		this.timeout = timeout;
	}
	
	
	/**
	 * @param sender - The {@link Player} to be declared as 'prompted' for this ConfirmationCommand
	 * @param data - The data object of type T to be stored for the given prompted player
	 * @throws IllegalArgumentException if player is null
	 */
	public final void addPromptedPlayer(final Player player, T data) {
		Validate.notNull(player, "player cannot be null");
		BukkitTask task = null;
		if (timeout > 0) {
			task = Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
				
				@Override
				public void run() {
					ConfirmationInfo<?> info = promptedPlayers.remove(player.getUniqueId());
					if (info != null) {
						// This is type safe because this task will be canceled if the player is added to a different ConfirmationCommand
						onTimeout(player, type.cast(info.data));
					}
				}
			}, timeout * 20);
		}
		ConfirmationInfo<?> previousInfo = removePlayer(player.getUniqueId());
		if (previousInfo != null) {
			decline(player, previousInfo);
		}
		
		ConfirmationInfo<T> info = new ConfirmationInfo<T>(this, data, task);
		promptedPlayers.put(player.getUniqueId(), info);
	}
	
	/**
	 * the {@link #onTerminate(Player, Object)} event method will be invoked for the given player
	 * if they were 'prompted' for this ConfirmationCommand
	 * @param player - The {@link Player} to be no longer declared as 'prompted'
	 * @return the data object of type T that was stored for the removed player, or null if the given player was not declared as 'prompted'
	 */
	public final T removePromptedPlayer(Player player) {
		ConfirmationInfo<?> info = promptedPlayers.get(player.getUniqueId());
		if (info != null && info.confirmationCmd == this) {
			T data = type.cast(removePlayer(player.getUniqueId()).data);
			onTerminate(player, data);
			return data;
		}
		return null;
	}
	
	/**
	 * the {@link #onTerminate(Player, Object)} event method will be invoked for the given player
	 * if they were 'prompted' for this ConfirmationCommand and had the given data object
	 * @param player - The {@link Player} to be no longer declared as 'prompted'
	 * @param data - The data object of type T to check against the stored data object for the prompted players
	 * @return true if the given player was declared as 'prompted' for this ConfirmationCommand and
	 * had the given data object
	 */
	public final boolean removePromptedPlayer(Player player, T data) {
		ConfirmationInfo<?> info = promptedPlayers.get(player.getUniqueId());
		if (info != null && info.confirmationCmd == this && ((info.data == null && data == null) || info.data.equals(data))) {
			onTerminate(player, type.cast(removePlayer(player.getUniqueId()).data));
			return true;
		}
		return false;
	}
	
	/**
	 * Remove all 'prompted' players for this ConfirmationCommand, and invoke {@link #onTerminate(Player, Object)} for each removed player
	 */
	public final void removePromptedPlayers() {
		Map<UUID, ConfirmationInfo<?>> copy = new HashMap<UUID, ConfirmationInfo<?>>(promptedPlayers);
		for (UUID uuid : copy.keySet()) {
			ConfirmationInfo<?> info = promptedPlayers.get(uuid);
			if (info.confirmationCmd == this) {
				removePlayer(uuid);
				Player player = Bukkit.getPlayer(uuid);
				if (player != null) {
					onTerminate(player, type.cast(info.data));
				}
			}
		}
	}
	
	/**
	 * Remove all 'prompted' players for this ConfirmationCommand that have the given data object,
	 * and invoke {@link #onTerminate(Player, Object)} for each removed player
	 */
	public final void removePromptedPlayers(T data) {
		Map<UUID, ConfirmationInfo<?>> copy = new HashMap<UUID, ConfirmationInfo<?>>(promptedPlayers);
		for (UUID uuid : copy.keySet()) {
			ConfirmationInfo<?> info = promptedPlayers.get(uuid);
			if (info.confirmationCmd == this && ((info.data == null && data == null) || info.data.equals(data))) {
				removePlayer(uuid);
				Player player = Bukkit.getPlayer(uuid);
				if (player != null) {
					onTerminate(player, type.cast(info.data));
				}
			}
		}
	}
	
	
	/**
	 * @param player - The {@link Player} to check if they are declared as 'prompted'
	 * @return true if the given player is declared as 'prompted' for this ConfirmationCommand or false otherwise
	 */
	public final boolean isPlayerPrompted(Player player) {
		ConfirmationInfo<?> info = promptedPlayers.get(player.getUniqueId());
		return info != null && info.confirmationCmd == this;
	}
	
	
	/**
	 * @param player - The {@link Player} to be no longer declared as 'prompted'
	 * @return the data object that was stored for the removed player,
	 * or null if the given player was not declared as 'prompted' for any ConfirmationCommands
	 */
	public static final Object removePromptedPlayerAll(Player player) {
		ConfirmationInfo<?> info = removePlayer(player.getUniqueId());
		if (info != null) {
			return info.data;
		}
		return null;
	}
	
	/**
	 * @param player - The {@link Player} to check if they are declared as 'prompted'
	 * @return true if the given player is declared as 'prompted' for any ConfirmationCommand of any plugin or false otherwise
	 */
	public static boolean isPlayerPromptedAny(Player player) {
		return promptedPlayers.containsKey(player.getUniqueId());
	}
	
	
	/**
	 * @param player - The prompted {@link Player} that confirmed
	 * @param data - The data object of type T that was stored for this player when they were prompted
	 */
	protected abstract void onAccept(Player player, T data);
	
	/**
	 * @param player - The prompted {@link Player} that declined
	 * @param data - The data object of type T that was stored for this player when they were prompted
	 */
	protected abstract void onDecline(Player player, T data);
	
	/**
	 * @param player - The prompted {@link Player} that failed to confirm/decline in the specified timeout
	 * @param data - The data object of type T that was stored for this player when they were prompted
	 */
	protected abstract void onTimeout(Player player, T data);
	
	protected abstract void onTerminate(Player player, T data);
	
	
	static ConfirmationInfo<?> removePlayer(UUID uuid) {
		ConfirmationInfo<?> info = promptedPlayers.remove(uuid);
		if (info != null) {
			if (info.task != null) {
				Bukkit.getScheduler().cancelTask(info.task.getTaskId());
			}
			return info;
		}
		return null;
	}
	
	static PseudoCommand getAcceptCommand() {
		return acceptCommand;
	}
	
	static PseudoCommand getDeclineCommand() {
		return declineCommand;
	}
	
	private void accept(Player player, ConfirmationInfo<?> info) {
		onAccept(player, type.cast(info.data));
	}
	
	private void decline(Player player, ConfirmationInfo<?> info) {
		onDecline(player, type.cast(info.data));
	}
	
	
	private static final class AcceptCommand implements PseudoCommand {
		
		@Override
		public boolean execute(CommandSender user) {
			if (user instanceof Player) {
				Player player = (Player) user;
				ConfirmationInfo<?> info = removePlayer(player.getUniqueId());
				if (info != null) {
					info.confirmationCmd.accept(player, info);
					return true;
				}
			}
			return false;
		}
	}
	
	private static final class DeclineCommand implements PseudoCommand {
		
		@Override
		public boolean execute(CommandSender user) {
			if (user instanceof Player) {
				Player player = (Player) user;
				ConfirmationInfo<?> info = removePlayer(player.getUniqueId());
				if (info != null) {
					info.confirmationCmd.decline(player, info);
					return true;
				}
			}
			return false;
		}
	}
	
	private static class ConfirmationInfo<T> {
		
		private final ConfirmationCommand<T> confirmationCmd;
		private final T data;
		private final BukkitTask task;
		
		private ConfirmationInfo(ConfirmationCommand<T> confirmationCmd, T data, BukkitTask task) {
			this.confirmationCmd = confirmationCmd;
			this.data = data;
			this.task = task;
		}
		
	}
	
}
