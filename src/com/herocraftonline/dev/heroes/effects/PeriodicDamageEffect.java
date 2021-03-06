package com.herocraftonline.dev.heroes.effects;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;
import com.herocraftonline.dev.heroes.skill.SkillType;

public class PeriodicDamageEffect extends PeriodicExpirableEffect {

    protected int tickDamage;
    protected final Player applier;
    protected final Hero applyHero;

    public PeriodicDamageEffect(Skill skill, String name, long period, long duration, int tickDamage, Player applier) {
        super(skill, name, period, duration);
        this.tickDamage = tickDamage;
        this.applier = applier;
        this.applyHero = plugin.getHeroManager().getHero(applier);
        this.types.add(EffectType.HARMFUL);
    }

    public Player getApplier() {
        return applier;
    }

    public Hero getApplierHero() {
        return applyHero;
    }

    public int getTickDamage() {
        return tickDamage;
    }

    public void setTickDamage(int tickDamage) {
        this.tickDamage = tickDamage;
    }

    @Override
    public void tick(LivingEntity lEntity) {
        super.tick(lEntity);
        skill.addSpellTarget(lEntity, applyHero);
        Skill.damageEntity(lEntity, applier, tickDamage, skill.isType(SkillType.PHYSICAL) ? DamageCause.ENTITY_ATTACK : DamageCause.MAGIC);
    }

    @Override
    public void tick(Hero hero) {
        super.tick(hero);
        Player player = hero.getPlayer();

        // Check if the target is damagable
        if (!skill.damageCheck(applier, player))
            return;

        skill.addSpellTarget(player, applyHero);
        Skill.damageEntity(player, applier, tickDamage, skill.isType(SkillType.PHYSICAL) ? DamageCause.ENTITY_ATTACK : DamageCause.MAGIC);
    }

}
