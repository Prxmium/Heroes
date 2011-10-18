package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.Creature;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import com.herocraftonline.util.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicDamageEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;

public class SkillPoison extends TargettedSkill {

    private String expireText;

    public SkillPoison(Heroes plugin) {
        super(plugin, "Poison");
        setDescription("Poisons your target");
        setUsage("/skill poison <target>");
        setArgumentRange(0, 1);
        setIdentifiers("skill poison");
        setTypes(SkillType.DAMAGING, SkillType.SILENCABLE, SkillType.HARMFUL);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DURATION.node(), 10000); // in milliseconds
        node.setProperty(Setting.PERIOD.node(), 2000); // in milliseconds
        node.setProperty("tick-damage", 1);
        node.setProperty(Setting.EXPIRE_TEXT.node(), "%target% has recovered from the poison!");
        return node;
    }

    @Override
    public void init() {
        super.init();
        expireText = getSetting(null, Setting.EXPIRE_TEXT.node(), "%target% has recovered from the poison!").replace("%target%", "$1");
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        HeroClass heroClass = hero.getHeroClass();

        long duration = getSetting(heroClass, Setting.DURATION.node(), 10000);
        long period = getSetting(heroClass, Setting.PERIOD.node(), 2000);
        int tickDamage = getSetting(heroClass, "tick-damage", 1);
        PoisonSkillEffect pEffect = new PoisonSkillEffect(this, period, duration, tickDamage, player);
        if (target instanceof Player) {
            plugin.getHeroManager().getHero((Player) target).addEffect(pEffect);
        } else if (target instanceof Creature) {
            Creature creature = (Creature) target;
            plugin.getEffectManager().addCreatureEffect(creature, pEffect);
        } else {
            Messaging.send(player, "Invalid target!");
            return false;
        }

        broadcastExecuteText(hero, target);
        return true;
    }

    public class PoisonSkillEffect extends PeriodicDamageEffect {
        
        public PoisonSkillEffect(Skill skill, long period, long duration, int tickDamage, Player applier) {
            super(skill, "Poison", period, duration, tickDamage, applier);
            this.types.add(EffectType.POISON);
            addMobEffect(19, (int) (duration / 1000) * 20, 0, true);
        }

        @Override
        public void apply(Creature creature) {
            super.apply(creature);
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
        }

        @Override
        public void remove(Creature creature) {
            super.remove(creature);
            broadcast(creature.getLocation(), expireText, Messaging.getCreatureName(creature).toLowerCase());
        }

        @Override
        public void remove(Hero hero) {
            super.remove(hero);
            Player player = hero.getPlayer();
            broadcast(player.getLocation(), expireText, player.getDisplayName());
        }
    }
}
