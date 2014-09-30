package org.deadmandungeons.deadmanplugin.command;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.scheduler.BukkitTask;

/**
 * This abstract class is useful for actions where a player or console user may need to confirm before the action is carried out.<br>
 * When an instance of this class is constructed and registered using the static method {@link #register(ConfirmationCommand, String, String)},
 * two {@link PseudoCommand}s are registered for the specified confirmCmd and declineCmd strings. A user that has been prompted to
 * confirm/decline a certain action should be added to the respective ConfirmationCommand by
 * calling {@link #addPromptedUser(CommandSender, Object)}. Once a user has been declared to be 'prompted', if they execute either
 * the confirmCmd or declineCmd PseudoCommands, the respective event methods {@link #onAccept(CommandSender, Object)} and
 * {@link #onDecline(CommandSender, Object)} will be called. If the ConfirmationCommand was constructed to specify a timeout,
 * the 'prompted' user will be removed from the list of prompted users if they failed to confirm/decline within the timeout, and
 * the {@link #onTimeout(CommandSender, Object)} event method will be called.
 * @param <T> - The type of the data object that should be stored when a user is prompted
 * @author Jon
 */
public abstract class ConfirmationCommand<T> {
	
	// TODO maybe have a lot of this stuff in DeadmanExecutor
	private static final Map<Class<?>, ConfirmationCommand<?>> commands = new HashMap<Class<?>, ConfirmationCommand<?>>();
	private static final Map<PluginCommand, Map<String, ConfirmationInfo<?>>> promptedUsers = new HashMap<PluginCommand, Map<String, ConfirmationInfo<?>>>();
	
	private final Map<String, ConfirmationInfo<?>> users;
	
	private final DeadmanExecutor executor;
	private final Class<T> type;
	private final int timeout;
	
	/**
	 * @param executor - The {@link DeadmanExecutor} that this ConfirmationCommand should be registered with when
	 * calling {@link #register(ConfirmationCommand, String, String)}
	 * @param type - The Class of the data object that should be stored when a user is prompted
	 */
	public ConfirmationCommand(DeadmanExecutor executor, Class<T> type) {
		this(executor, type, -1);
	}
	
	/**
	 * @param executor - The {@link DeadmanExecutor} that this ConfirmationCommand should be registered with when
	 * calling {@link #register(ConfirmationCommand, String, String)}
	 * @param type - The Class of the data object that should be stored when a user is prompted
	 * @param timeout - The time in seconds that a user has to either confirm or decline when they have been prompted
	 */
	public ConfirmationCommand(DeadmanExecutor executor, Class<T> type, int timeout) {
		Validate.notNull(executor, "executor cannot be null");
		Validate.notNull(type, "type cannot be null");
		this.executor = executor;
		this.type = type;
		this.timeout = timeout;
		boolean newCmd = !promptedUsers.containsKey(executor.getBukkitCmd());
		users = (newCmd ? new HashMap<String, ConfirmationInfo<?>>() : promptedUsers.get(executor.getBukkitCmd()));
		if (newCmd) {
			promptedUsers.put(executor.getBukkitCmd(), users);
		}
	}
	
	/**
	 * @param cmd - The instance of the ConfirmationCommand to register
	 * @param confirmCmd - The String for the confirm {@link PseudoCommand} to be registered
	 * @param declineCmd - The String for the decline {@link PseudoCommand} to be registered
	 */
	public static void register(ConfirmationCommand<?> cmd, String confirmCmd, String declineCmd) {
		Validate.notNull(cmd, "cmd cannot be null");
		Validate.notNull(confirmCmd, "confirmCmd cannot be null");
		Validate.notNull(declineCmd, "declineCmd cannot be null");
		
		cmd.executor.registerPseudoCommand(confirmCmd.toLowerCase(), ConfirmCommand.getInstance(cmd.executor.getBukkitCmd()));
		cmd.executor.registerPseudoCommand(declineCmd.toLowerCase(), DeclineCommand.getInstance(cmd.executor.getBukkitCmd()));
		commands.put(cmd.getClass(), cmd);
	}
	
	/**
	 * @param cmdClass - The class of the ConfirmationCommand to get
	 * @return the registered instance of the given ConfirmationCommand class
	 * @throws IllegalStateException if no instance of the given ConfirmationCommand class has been registered
	 */
	public static <T, C extends ConfirmationCommand<T>> C get(Class<C> cmdClass) throws IllegalStateException {
		Validate.notNull(cmdClass, "cmdClass cannot be null");
		C cmd = cmdClass.cast(commands.get(cmdClass));
		if (cmd == null) {
			throw new IllegalStateException("A ConfirmationCommand for type '" + cmdClass.getCanonicalName() + "' has not been registered!");
		}
		return cmd;
	}
	
	/**
	 * @param sender - The {@link CommandSender} user to be declared as 'prompted'
	 * @param data - The data object of type T to be stored for the given prompted user
	 */
	public final void addPromptedUser(final CommandSender user, T data) {
		Validate.notNull(user, "user cannot be null");
		BukkitTask task = null;
		if (timeout > 0) {
			task = Bukkit.getScheduler().runTaskLater(executor.getPlugin(), new Runnable() {
				
				@Override
				public void run() {
					ConfirmationInfo<?> info = users.remove(user.getName());
					if (info != null) {
						onTimeout(user, type.cast(info.data));
					}
				}
			}, timeout * 20);
		}
		ConfirmationInfo<?> previousInfo = removeUser(executor.getBukkitCmd(), user);
		if (previousInfo != null) {
			decline(user, previousInfo);
		}
		
		ConfirmationInfo<T> info = new ConfirmationInfo<T>(this, data, task);
		users.put(user.getName(), info);
	}
	
	/**
	 * @param user - The {@link CommandSender} user to be no longer declared as 'prompted'
	 * @return the data object of type T that was stored for the removed user, or null if the given user was not declared as 'prompted'
	 */
	public final T removePromptedUser(CommandSender user) {
		ConfirmationInfo<?> info = removeUser(executor.getBukkitCmd(), user);
		if (info != null) {
			return type.cast(info.data);
		}
		return null;
	}
	
	/**
	 * @param user - The {@link CommandSender} user to check if they are declared as 'prompted'
	 * @return true if the given user is declared as 'prompted' for this ConfirmationCommand or false otherwise
	 */
	public final boolean isUserPrompted(CommandSender user) {
		ConfirmationInfo<?> info = users.get(user.getName());
		return info != null && info.confirmationCmd == this;
	}
	
	/**
	 * @param user - The {@link CommandSender} user to check if they are declared as 'prompted'
	 * @return true if the given user is declared as 'prompted' for any ConfirmationCommand of the same PluginCommand or false otherwise
	 */
	public final boolean isUserPromptedAny(CommandSender user) {
		return users.containsKey(user.getName());
	}
	
	
	/**
	 * @param user - The prompted {@link CommandSender} user that confirmed
	 * @param data - The data object of type T that was stored for this user when they were prompted
	 * @return true if the prompted user confirmed successfully and false otherwise. If false is returned,
	 * the confirm PsuedoCommand execution will be treaded as an invalid command
	 */
	protected abstract boolean onConfirm(CommandSender user, T data);
	
	/**
	 * @param user - The prompted {@link CommandSender} user that declined
	 * @param data - The data object of type T that was stored for this user when they were prompted
	 * @return true if the prompted user declined successfully and false otherwise. If false is returned,
	 * the decline PsuedoCommand execution will be treaded as an invalid command
	 */
	protected abstract boolean onDecline(CommandSender user, T data);
	
	/**
	 * @param user - The prompted {@link CommandSender} user that failed to confirm/decline in the specified timeout
	 * @param data - The data object of type T that was stored for this user when they were prompted
	 */
	protected abstract void onTimeout(CommandSender user, T data);
	
	
	static ConfirmationInfo<?> removeUser(PluginCommand cmd, CommandSender user) {
		Map<String, ConfirmationInfo<?>> users = promptedUsers.get(cmd);
		if (users != null) {
			ConfirmationInfo<?> info = users.remove(user.getName());
			if (info != null) {
				if (info.task != null) {
					Bukkit.getScheduler().cancelTask(info.task.getTaskId());
				}
				return info;
			}
		}
		return null;
	}
	
	private boolean confirm(CommandSender user, ConfirmationInfo<?> info) {
		return onConfirm(user, type.cast(info.data));
	}
	
	private boolean decline(CommandSender user, ConfirmationInfo<?> info) {
		return onDecline(user, type.cast(info.data));
	}
	
	
	private static final class ConfirmCommand implements PseudoCommand {
		
		private static final Map<PluginCommand, ConfirmCommand> instances = new HashMap<PluginCommand, ConfirmCommand>();
		
		private static ConfirmCommand getInstance(PluginCommand cmd) {
			ConfirmCommand confirmCommand = instances.get(cmd);
			if (confirmCommand == null) {
				instances.put(cmd, confirmCommand = new ConfirmCommand(cmd));
			}
			return confirmCommand;
		}
		
		private final PluginCommand cmd;
		
		private ConfirmCommand(PluginCommand cmd) {
			this.cmd = cmd;
		}
		
		@Override
		public boolean execute(CommandSender sender) {
			ConfirmationInfo<?> info = removeUser(cmd, sender);
			if (info != null) {
				return info.confirmationCmd.confirm(sender, info);
			}
			return false;
		}
	}
	
	private static final class DeclineCommand implements PseudoCommand {
		
		private static final Map<PluginCommand, DeclineCommand> instances = new HashMap<PluginCommand, DeclineCommand>();
		
		private static DeclineCommand getInstance(PluginCommand cmd) {
			DeclineCommand declineCommand = instances.get(cmd);
			if (declineCommand == null) {
				instances.put(cmd, declineCommand = new DeclineCommand(cmd));
			}
			return declineCommand;
		}
		
		private final PluginCommand cmd;
		
		private DeclineCommand(PluginCommand cmd) {
			this.cmd = cmd;
		}
		
		@Override
		public boolean execute(CommandSender sender) {
			ConfirmationInfo<?> info = removeUser(cmd, sender);
			if (info != null) {
				return info.confirmationCmd.decline(sender, info);
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
