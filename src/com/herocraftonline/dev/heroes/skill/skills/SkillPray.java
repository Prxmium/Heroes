package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.HeroRegainHealthEvent;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SkillPray extends TargettedSkill {

    public SkillPray(Heroes plugin) {
        super(plugin, "Pray");
        setDescription("Heals the target");
        setUsage("/skill pray <target>");
        setArgumentRange(0, 1);
        setIdentifiers(new String[]{"skill pray"});
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("health", 10);
        node.setProperty(SETTING_MAXDISTANCE, 25);
        return node;
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target instanceof Player) {
            Hero targetHero = getPlugin().getHeroManager().getHero((Player) target);
            int hpPlus = getSetting(hero.getHeroClass(), "health", 10);
            double targetHealth = targetHero.getHealth();

            if (targetHealth >= targetHero.getMaxHealth()) {
                Messaging.send(player, "Target is already fully healed.");
                return false;
            }
            HeroRegainHealthEvent hrhEvent = new HeroRegainHealthEvent(targetHero, hpPlus, this);
            getPlugin().getServer().getPluginManager().callEvent(hrhEvent);
            if (hrhEvent.isCancelled()) {
                Messaging.send(player, "Unable to heal the target at this time!");
                return false;
            }
            
            targetHero.setHealth(targetHealth + hrhEvent.getAmount());
            targetHero.syncHealth();
            broadcastExecuteText(hero, target);
            return true;
        }
        return false;
    }
}
