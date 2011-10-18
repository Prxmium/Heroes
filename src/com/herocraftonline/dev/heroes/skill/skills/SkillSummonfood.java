package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import com.herocraftonline.util.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;

public class SkillSummonfood extends ActiveSkill {

    public SkillSummonfood(Heroes plugin) {
        super(plugin, "Summonfood");
        setDescription("Summons you food!");
        setUsage("/skill summonfood");
        setArgumentRange(0, 0);
        setIdentifiers("skill summonfood");
        setTypes(SkillType.ITEM, SkillType.SUMMON, SkillType.SILENCABLE);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("food-type", "BREAD");
        return node;
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        World world = player.getWorld();
        HeroClass heroClass = hero.getHeroClass();
        ItemStack dropItem = new ItemStack(Material.matchMaterial(getSetting(heroClass, "food-type", "BREAD")), 1);
        world.dropItem(player.getLocation(), dropItem);
        broadcastExecuteText(hero);
        return true;
    }

}
