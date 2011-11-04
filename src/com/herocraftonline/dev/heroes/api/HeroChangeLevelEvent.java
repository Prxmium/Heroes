package com.herocraftonline.dev.heroes.api;

import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.hero.Hero;


/**
 * Called when a Hero changes levels, either through admin commands or when Experience adjusts their level higher/lower..\
 * Data during this event is unable to be changed.
 */
@SuppressWarnings("serial")
public class HeroChangeLevelEvent extends HeroEvent {

    private final int from;
    private final int to;
    private final Hero hero;
    private final HeroClass heroClass;

    public HeroChangeLevelEvent(Hero hero, HeroClass heroClass, int from, int to) {
        super("HeroLevelEvent", HeroEventType.HERO_LEVEL_CHANGE);
        this.heroClass = heroClass;
        this.from = from;
        this.to = to;
        this.hero = hero;
    }

    /**
     * The level the hero is changing from
     * @return
     */
    public final int getFrom() {
        return from;
    }

    /**
     * Returns the hero being adjusted
     * @return
     */
    public Hero getHero() {
        return hero;
    }

    /**
     * Returns the level the hero will be after the event
     * @return
     */
    public final int getTo() {
        return to;
    }

    /**
     * Returns the class gaining the level
     * @return
     */
    public HeroClass getHeroClass() {
        return heroClass;
    }

}
