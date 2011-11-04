package com.herocraftonline.dev.heroes.api;

import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;

/**
 * Holds skill info for initiating a skill use event 
 * @author sleak
 *
 */
public class SkillUseInfo {

    private final Hero hero;
    private final Skill skill;

    public SkillUseInfo(Hero hero, Skill skill) {
        this.hero = hero;
        this.skill = skill;
    }

    /**
     * @return the hero
     */
    public Hero getHero() {
        return hero;
    }

    /**
     * @return the skill
     */
    public Skill getSkill() {
        return skill;
    }
}
