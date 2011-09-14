package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.effects.Effect;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Setting;

import org.bukkit.Location;

public class SkillChakra extends ActiveSkill {

    public SkillChakra(Heroes plugin) {
        super(plugin, "Chakra");
        setDescription("Dispels and heals party members near you");
        setUsage("/skill chakra");
        setArgumentRange(0, 0);
        setIdentifiers("skill chakra");
        setTypes(SkillType.SILENCABLE, SkillType.HEAL, SkillType.LIGHT);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty("heal_amount", 10);
        node.setProperty(Setting.RADIUS.node(), 7);
        node.setProperty("max-removals", -1);
        return node;
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        HeroClass heroClass = hero.getHeroClass();
        Location castLoc = player.getLocation();
        int radiusSquared = (int) Math.pow(getSetting(heroClass, Setting.RADIUS.node(), 7), 2);
        int healAmount = getSetting(heroClass, "heal-amount", 10);
        int removals = getSetting(heroClass, "max-removals", -1);
        if (hero.hasParty()) {
            for (Hero p : hero.getParty().getMembers()) {
                if (castLoc.distanceSquared(p.getPlayer().getLocation()) <= radiusSquared) {
                    healDispel(p, removals, healAmount);
                }
            }
        } else {
            healDispel(hero, removals, healAmount);
        }
        broadcastExecuteText(hero);
        return true;
    }

    private void healDispel(Hero hero, int removals, int healAmount) {
        if (hero.getHealth() < hero.getMaxHealth()) {
            hero.setHealth(hero.getHealth() + healAmount);
            hero.syncHealth();
        }
        if (removals == 0)
            return;

        if (hero.getPlayer().getFireTicks() > 0) {
            removals--;
            hero.getPlayer().setFireTicks(0);
            if (removals == 0)
                return;
        }

        for (Effect effect : hero.getEffects()) {
            if (effect.isType(EffectType.HARMFUL)) {
                hero.removeEffect(effect);
                removals--;
                if (removals == 0)
                    break;
            }
        }
    }

}