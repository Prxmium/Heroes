package com.herocraftonline.dev.heroes.command.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.command.BasicCommand;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;

public class AdminExpCommand extends BasicCommand {

    private final Heroes plugin;

    public AdminExpCommand(Heroes plugin) {
        super("AdminExpCommand");
        this.plugin = plugin;
        setDescription("Changes a users class exp");
        setUsage("/hero admin exp §9<player> <class> <exp>");
        setArgumentRange(3, 3);
        setIdentifiers("hero admin exp");
        setPermission("heroes.admin.exp");
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        Player player = plugin.getServer().getPlayer(args[0]);
        // Check the Player exists.
        if (player == null) {
            Messaging.send(sender, "Failed to find a matching Player for '$1'.  Offline players are not supported!", args[0]);
            return false;
        }
        Hero hero = plugin.getHeroManager().getHero(player);
        HeroClass hc = plugin.getClassManager().getClass(args[1]);

        if (hc == null) {
            if (args[1].equalsIgnoreCase("prim")) {
                hc = hero.getHeroClass();
            } else if (args[1].equalsIgnoreCase("prof")) {
                hc = hero.getSecondClass();
            }
        }
        
        if (hc == null) {
            Messaging.send(sender, "$1 is not a valid HeroClass!", args[1]);
            return false;
        }
        
        try {
            double expChange = Integer.parseInt(args[2]);
            hero.addExp(expChange, hc);
            plugin.getHeroManager().saveHero(hero);
            Messaging.send(sender, "Experience changed.");
            Messaging.send(hero.getPlayer(), "You have been awarded $1 exp", expChange);
            return true;
        } catch (NumberFormatException e) {
            Messaging.send(sender, "Invalid experience value.");
            return false;
        }

    }
}
