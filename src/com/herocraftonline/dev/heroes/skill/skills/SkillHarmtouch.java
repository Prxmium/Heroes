package com.herocraftonline.dev.heroes.skill.skills;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.config.ConfigurationNode;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.skill.TargettedSkill;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Setting;

public class SkillHarmtouch extends TargettedSkill {

    public SkillHarmtouch(Heroes plugin) {
        super(plugin, "Harmtouch");
        setDescription("Deals direct damage to the target");
        setUsage("/skill harmtouch <target>");
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill harmtouch" });

        setTypes(SkillType.DARK, SkillType.SILENCABLE, SkillType.DAMAGING);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DAMAGE.node(), 10);
        return node;
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();
        if (target.equals(player) || hero.getSummons().contains(target)) {
            Messaging.send(player, "You need a target!");
            return false;
        }

        // Check if the target is damagable
        if (!damageCheck(player, target))
            return false;

        int damage = getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 10);
        addSpellTarget(target, hero);
        target.damage(damage, player);
        broadcastExecuteText(hero, target);
        return true;
    }
}
