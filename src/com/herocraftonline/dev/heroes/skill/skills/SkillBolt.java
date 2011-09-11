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

public class SkillBolt extends TargettedSkill {

    public SkillBolt(Heroes plugin) {
        super(plugin, "Bolt");
        setDescription("Calls a bolt of lightning down on the target");
        setUsage("/skill bolt <target>");
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill bolt" });
        
        setTypes(SkillType.LIGHTNING, SkillType.SILENCABLE, SkillType.DAMAGING);
    }

    @Override
    public ConfigurationNode getDefaultConfig() {
        ConfigurationNode node = super.getDefaultConfig();
        node.setProperty(Setting.DAMAGE.node(), 4);
        return node;
    }

    @Override
    public boolean use(Hero hero, LivingEntity target, String[] args) {
        Player player = hero.getPlayer();

        if (target.equals(player) || hero.getSummons().contains(target)) {
            Messaging.send(player, "Invalid target!");
            return false;
        }

        //Check if the target is damagable
        if (!damageCheck(player, target))
            return false;
        
        plugin.getDamageManager().addSpellTarget(target, hero, this);
        target.getWorld().strikeLightningEffect(target.getLocation());
        target.damage(getSetting(hero.getHeroClass(), Setting.DAMAGE.node(), 4), player);

        broadcastExecuteText(hero, target);
        return true;
    }
}
