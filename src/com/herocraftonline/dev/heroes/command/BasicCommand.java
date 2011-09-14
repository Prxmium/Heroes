package com.herocraftonline.dev.heroes.command;

import org.bukkit.command.CommandSender;

public abstract class BasicCommand implements Command {

    private String name;
    private String description = "";
    private String usage = "";
    private String permission = "";
    private String[] notes = new String[0];
    private String[] identifiers = new String[0];
    private int minArguments = 0;
    private int maxArguments = 0;

    public BasicCommand(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getUsage() {
        return usage;
    }

    @Override
    public String getPermission() {
        return permission;
    }

    @Override
    public String[] getNotes() {
        return notes;
    }

    @Override
    public int getMinArguments() {
        return minArguments;
    }

    @Override
    public int getMaxArguments() {
        return maxArguments;
    }

    @Override
    public boolean isInProgress(CommandSender executor) {
        return false;
    }

    @Override
    public void cancelInteraction(CommandSender executor) {
    }

    @Override
    public boolean isShownOnHelpMenu() {
        return true;
    }

    @Override
    public boolean isInteractive() {
        return false;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public void setNotes(String...notes) {
        this.notes = notes;
    }

    public void setArgumentRange(int min, int max) {
        this.minArguments = min;
        this.maxArguments = max;
    }

    public void setIdentifiers(String...identifiers) {
        this.identifiers = identifiers;
    }

    @Override
    public boolean isIdentifier(CommandSender executor, String input) {
        for (String identifier : identifiers) {
            if (input.equalsIgnoreCase(identifier)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getIdentifiers() {
        return identifiers;
    }

}
