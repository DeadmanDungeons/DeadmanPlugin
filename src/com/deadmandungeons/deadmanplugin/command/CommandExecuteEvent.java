package com.deadmandungeons.deadmanplugin.command;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;


public class CommandExecuteEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private final CommandSender sender;
    private final Command command;
    private final Arguments arguments;
    private boolean cancelled;

    public CommandExecuteEvent(CommandSender sender, Command command, Arguments arguments) {
        this.sender = sender;
        this.command = command;
        this.arguments = arguments;
    }

    public CommandSender getSender() {
        return sender;
    }

    public Command getCommand() {
        return command;
    }

    public Arguments getArguments() {
        return arguments;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
