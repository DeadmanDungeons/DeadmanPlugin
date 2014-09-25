package org.deadmandungeons.deadmanplugin.command;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.deadmandungeons.deadmanplugin.Result;
import org.deadmandungeons.deadmanplugin.command.ArgumentInfo.ArgType;
import org.deadmandungeons.deadmanplugin.command.DeadmanExecutor.CommandWrapper;
import org.deadmandungeons.deadmanplugin.filedata.DeadmanConfig.Converter;

/**
 * This class is used to pass a valid set of arguments to the {@link Command#execute(CommandSender, Arguments) execute()} method
 * of a {@link Command}. To get an instance of this class, a {@link Matcher} should be built with a valid array of arguments for the
 * intended Command based on its defined {@link CommandInfo} annotation. The array of arguments can be given as the valid argument
 * Objects, or as a string representation which will later be converted to the valid argument object. The {@link Matcher#findMatch() findMatch()}
 * method of the Matcher will return a {@link SubCommand} object which is used as a parameter to finally convert the given array of
 * arguments based on the matched SubCommand and return a {@link Result Result&lt;Arguments&gt;} object. This result will contain either
 * an error message of the reason the conversion failed, or an instance of this class with the converted argument objects. <br />
 * Example usage:<br />
 * 
 * <pre>
 * <code>
 * SubCommand subCmd = Arguments.matcher(executor).forCommand(command).withStringArgs(args).findMatch();
 * if (subCmd != null) {
 *     Result&lt;Arguments&gt; conversionResult = subCmd.convert();
 *     if (!conversionResult.isError()) {
 *         command.execute(sender, conversionResult.getResult());
 *     }
 * }
 * </code>
 * </pre>
 * 
 * This process is needed to guarantee that the arguments given to the Command are valid based on its CommandInfo annotation.
 * @author Jon
 */
public final class Arguments {
	
	private Command cmd;
	private Object[] args;
	
	private SubCommandInfo subCmd;
	private int subCmdIndex;
	
	private Arguments(SubCommand subCmd) {
		this.cmd = subCmd.cmd;
		this.args = subCmd.validArgs;
		this.subCmd = subCmd.info;
		this.subCmdIndex = subCmd.index;
	}
	
	/**
	 * @return the Command that this Arguments object is for
	 */
	public Command getCmd() {
		return cmd;
	}
	
	/**
	 * @return the array of valid argument objects for the Command this Arguments object is for
	 */
	public Object[] getArgs() {
		return args;
	}
	
	/**
	 * @return the SubCommandInfo that these argument objects match
	 */
	public SubCommandInfo getSubCmd() {
		return subCmd;
	}
	
	/**
	 * @return the index of the SubCommandInfo for this arguments object in the Commands {@link CommandInfo#subCommands()} array
	 */
	public int getSubCmdIndex() {
		return subCmdIndex;
	}
	
	/**
	 * This method is to be used by a Command to validate that the provided Arguments object is actually for that Command
	 * @param type - The Arguments object to validate
	 * @param clazz - The class of the Command to validate against
	 */
	public static void validateType(Arguments type, Class<? extends Command> clazz) {
		if (type.getCmd().getClass() != clazz) {
			String msg = "The provided Arguments is for Command type '" + type.getCmd().getClass() + "' but must be for type " + clazz;
			throw new IllegalArgumentException(msg);
		}
	}
	
	public static Matcher matcher(DeadmanExecutor executor) {
		return new Matcher(executor);
	}
	
	/**
	 * This class should be used to build a {@link SubCommand} object which will be used by the {@link Converter} builder class.
	 * This builder class will match the provided array of arguments against the provided Command's SubCommandInfo. <br />
	 * Required parameters before calling {@link #findMatch()}:<br />
	 * <ul>
	 * <li>{@link #withStringArgs(String args)} or {@link #withValidArgs(Object validArgs)}</li>
	 * <li>{@link #forCommand(Command cmd)}</li>
	 * </ul>
	 * @author Jon
	 */
	public static final class Matcher {
		
		private DeadmanExecutor executor;
		
		private CommandWrapper<?> cmdWrapper;
		private String[] strArgs;
		private Object[] validArgs;
		
		private Matcher(DeadmanExecutor executor) {
			if (executor == null) {
				throw new IllegalArgumentException("executor must not be null");
			}
			this.executor = executor;
		}
		
		/**
		 * Only one set of arguments can be used. Any previously set array of argument objects will be nullified
		 * @param strArgs - The array of arguments in string form that should be converted to their appropriate types
		 * @return this Builder
		 */
		public Matcher withStringArgs(String... strArgs) {
			this.strArgs = strArgs;
			this.validArgs = null;
			return this;
		}
		
		/**
		 * Only one set of arguments can be used. Any previously set array of string arguments will be nullified
		 * @param objArgs - The array of arguments in their appropriate types for the command to be executed
		 * @return this Builder
		 */
		public Matcher withValidArgs(Object... validArgs) {
			this.validArgs = validArgs;
			this.strArgs = null;
			return this;
		}
		
		/**
		 * @param cmd - The Command that these arguments are for
		 * @return this Builder
		 */
		public Matcher forCommand(Class<? extends Command> cmd) {
			this.cmdWrapper = executor.getCommandWrapper(cmd);
			return this;
		}
		
		/**
		 * @param cmdWrapper - The CommandWrapper containing the Command that these arguments are for
		 * @return this builder
		 */
		public Matcher forCommand(CommandWrapper<?> cmdWrapper) {
			this.cmdWrapper = cmdWrapper;
			return this;
		}
		
		/**
		 * @return a SubCommand object containing the SubCommandInfo that the provided arguments matched,
		 * or null if the given arguments did not match any of the defined SubCommandInfo for the provided Command
		 * @throws IllegalStateException if a Command or a list of arguments was not given, or if the given Command
		 * does not define a CommandInfo annotation.
		 */
		public SubCommand findMatch() throws IllegalStateException {
			if (cmdWrapper == null) {
				throw new IllegalStateException("A non-null Command class bust be given");
			}
			if (strArgs == null && validArgs == null) {
				throw new IllegalStateException("An array of argument Strings or Objects must be given");
			}
			
			SubCommandInfo[] subCmds = cmdWrapper.getInfo().subCommands();
			
			int index = -1;
			SubCommandInfo subCmd = null;
			Object[] args = (strArgs != null ? strArgs : validArgs);
			if (subCmds.length > 0) {
				index = getSubCommandIndex(subCmds, args);
				if (index == -1) {
					return null;
				}
				subCmd = subCmds[index];
			} else if (args.length > 0) {
				return null;
			}
			
			return new SubCommand(executor, cmdWrapper.getCmd(), strArgs, validArgs, index, subCmd);
		}
		
		private static int getSubCommandIndex(SubCommandInfo[] subCommands, Object[] args) {
			int index = -1;
			// determine which command syntax the sender was using by matching the general syntax
			for (int i = 0; i < subCommands.length; i++) {
				boolean matchedCmd = true;
				ArgumentInfo[] arguments = subCommands[i].arguments();
				// if the amounts of arguments equal, or if there is only only 1 argument missing (for optional args)
				if (args.length == arguments.length || args.length + 1 == arguments.length) {
					for (int n = 0; n < arguments.length; n++) {
						// if the amount of given arguments is greater than the current index
						if (args.length > n) {
							// if the argType is a NON_VARIABLE, and the given argument doesn't match the argName
							if (arguments[n].argType() == ArgType.NON_VARIABLE && !arguments[n].argName().equalsIgnoreCase(args[n].toString())) {
								matchedCmd = false;
								break;
							}
						} else if (arguments[n].argType() == ArgType.OPT_VARIABLE) {
							// if this last argument is optional, set the index to the current, in case a better fit is found
							index = i;
						} else {
							// by this point, the amount of given args is less than the current
							// argument index for this subCommand, and the variable wasn't optional
							matchedCmd = false;
							break;
						}
					}
				} else {
					matchedCmd = false;
				}
				if (matchedCmd) {
					index = i;
					break;
				}
			}
			return index;
		}
		
	}
	
	/**
	 * This class is used as a container for a matched array of arguments against a Commands {@link SubCommandInfo} annotation. The
	 * {@link Matcher#findMatch()} method of the Matcher builder will return an instance of this class
	 * when the Matcher is built with a valid array of arguments for the given Command. The instance of this class is
	 * then used as a parameter for the {@link Converter} builder.
	 * @author Jon
	 */
	public static final class SubCommand {
		
		private final DeadmanExecutor executor;
		private final Command cmd;
		private final String[] strArgs;
		private Object[] validArgs;
		private final int index;
		private final SubCommandInfo info;
		
		private SubCommand(DeadmanExecutor executor, Command cmd, String[] strArgs, Object[] validArgs, int index, SubCommandInfo info) {
			this.executor = executor;
			this.cmd = cmd;
			this.strArgs = strArgs;
			this.validArgs = validArgs;
			this.index = index;
			this.info = info;
		}
		
		/**
		 * @return the {@link SubCommandInfo} that provided arguments represent,
		 * or null if the provided {@link Command} does not define any SubCommandInfo
		 */
		public SubCommandInfo info() {
			return info;
		}
		
		/**
		 * @return a {@link Result Result&lt;Arguments&gt;} object that will contain either an error message of the reason the
		 * conversion failed, or a valid Arguments object with the converted argument objects. If the error message is null,
		 * the conversion was successful. The Arguments object may be null if the Command defined no SubCommandInfo annotation,
		 * meaning there were not command arguments.
		 * @throws IllegalStateException if a non-null SubCommand was not given.
		 * Or if a list of String arguments was given but a ArgumentConvertor does not exists for one of the arguments varType.
		 * Or if a list of Object arguments was given but one of the objects is an invalid type based on the respective argument varType.
		 */
		public Result<Arguments> convert() throws IllegalStateException {
			if (info != null) {
				if (strArgs != null) {
					List<Object> args = new ArrayList<Object>();
					Result<?> conversionResult = null;
					
					ArgumentInfo[] toArgs = info.arguments();
					for (int i = 0; i < strArgs.length; i++) {
						if (toArgs[i].argType() == ArgType.NON_VARIABLE) {
							args.add(strArgs[i]);
							continue;
						}
						ArgumentConverter<?> converter = executor.getConverter(toArgs[i].varType());
						if (converter != null) {
							conversionResult = converter.convertCommandArg(toArgs[i].argName(), strArgs[i]);
							if (conversionResult.isError()) {
								return new Result<Arguments>(conversionResult.getErrorMessage());
							}
							args.add(conversionResult.getResult());
						} else if (toArgs[i].varType() == String.class) {
							args.add(strArgs[i]);
						} else {
							String msg = "An ArgumentConverter was not found for arguments of type '%s'";
							throw new IllegalStateException(String.format(msg, toArgs[i].varType().getCanonicalName()));
						}
					}
					validArgs = args.toArray(new Object[args.size()]);
				} else {
					validateArgs(info.arguments(), validArgs);
				}
			}
			
			return new Result<Arguments>(new Arguments(this));
		}
		
		private static void validateArgs(ArgumentInfo[] arguments, Object[] givenArgs) {
			for (int i = 0; i < givenArgs.length; i++) {
				if (arguments[i].varType() != givenArgs[i].getClass()) {
					String msg = "An argument of type '%s' was expected, but instead got type '%s'";
					throw new IllegalStateException(String.format(msg, arguments[i].varType(), givenArgs[i].getClass()));
				}
			}
		}
		
	}
	
}
