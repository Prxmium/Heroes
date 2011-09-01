package com.herocraftonline.dev.heroes.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.command.BasicCommand;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;

public class AdminClassCommand extends BasicCommand {

    private final Heroes plugin;

    public AdminClassCommand(Heroes plugin) {
        super("AdminClassCommand");
        this.plugin = plugin;
        setDescription("Changes a users class");
        setUsage("/hero admin class §9<player> <class>");
        setArgumentRange(2, 2);
        setIdentifiers(new String[] { "hero admin class" });
        setPermission("heroes.admin.classchange");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        Player player = plugin.getServer().getPlayer(args[0]);
        // Check the Player exists.
        if (player == null) {
            Messaging.send(sender, "Failed to find a matching Player for '$1'. Offline players are not supported!", args[0]);
            return false;
        }
        HeroClass heroClass = plugin.getClassManager().getClass(args[1]);
        // Check the HeroClass exists.
        if (heroClass == null) {
            Messaging.send(sender, "Failed to find a matching HeroClass for '$1'.", args[1]);
            return false;
        }
        // Check the Player is not the same HeroClass as we are trying to assign.
        Hero hero = plugin.getHeroManager().getHero(player);
        if (hero.getHeroClass().equals(heroClass)) {
            Messaging.send(sender, "$1 is already a $2.", player.getName(), heroClass.getName());
            return false;
        }
        // Change the Players HeroClass and reset their Bindings.
        hero.changeHeroClass(heroClass);
        // Alert both the Admin and the Player of the change.
        Messaging.send(sender, "You have successfully changed $1 HeroClass to $2.", player.getName(), heroClass.getName());
        Messaging.send(player, "Welcome to the path of the $1!", heroClass.getName());
        return true;
    }
}
