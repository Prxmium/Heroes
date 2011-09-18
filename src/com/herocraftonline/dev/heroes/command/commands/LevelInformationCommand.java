package com.herocraftonline.dev.heroes.command.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.command.BasicCommand;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Properties;

public class LevelInformationCommand extends BasicCommand {
    private final Heroes plugin;

    public LevelInformationCommand(Heroes plugin) {
        super("Level Information");
        this.plugin = plugin;
        setDescription("Displays hero information");
        setUsage("/hero level");
        setArgumentRange(0, 0);
        setIdentifiers(new String[] { "hero level", "level", "lvl" });
    }

    @Override
    public boolean execute(CommandSender sender, String identifier, String[] args) {
        if (!(sender instanceof Player))
            return false;

        Player player = (Player) sender;
        Hero hero = plugin.getHeroManager().getHero(player);
        Properties prop = this.plugin.getConfigManager().getProperties();
        int exp = (int) hero.getExperience();
        int level = prop.getLevel(exp);
        int current = prop.getExperience(level);

        sender.sendMessage(ChatColor.RED + "-----[ " + ChatColor.WHITE + "Your Level Information" + ChatColor.RED + " ]-----");
        sender.sendMessage(ChatColor.GREEN + "  Class: " + ChatColor.WHITE + hero.getHeroClass().getName());
        sender.sendMessage(ChatColor.GREEN + "  Level: " + ChatColor.WHITE + level + ChatColor.GREEN + "/" + ChatColor.WHITE + hero.getHeroClass().getMaxLevel());
        sender.sendMessage(ChatColor.GREEN + "  Total Exp: " + ChatColor.WHITE + exp);
        if (!hero.isMaster()) {
            int next = (int) prop.getExperience(level + 1);
            sender.sendMessage(ChatColor.DARK_GREEN + "  EXP.  " + createExperienceBar(exp, current, next));
        } else {
            sender.sendMessage(ChatColor.YELLOW + "  MASTERED!");
        }
        sender.sendMessage(ChatColor.BLUE + "  MANA " + Messaging.createManaBar(hero.getMana()));

        return true;
    }

    private String createExperienceBar(int exp, int currentLevelExp, int nextLevelExp) {
        String expBar = ChatColor.RED + "[" + ChatColor.DARK_GREEN;
        int progress = (int) ((double) (exp - currentLevelExp) / (nextLevelExp - currentLevelExp) * 50);
        for (int i = 0; i < progress; i++) {
            expBar += "|";
        }
        expBar += ChatColor.DARK_RED;
        for (int i = 0; i < 50 - progress; i++) {
            expBar += "|";
        }
        expBar += ChatColor.RED + "]";
        expBar += " - " + ChatColor.DARK_GREEN + progress * 2 + "%  ";
        expBar += "" + ChatColor.DARK_GREEN + (exp - currentLevelExp) + ChatColor.RED + "/" + ChatColor.DARK_GREEN + (nextLevelExp - currentLevelExp);
        return expBar;
    }

}
