package com.herocraftonline.dev.heroes.damage;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.CreatureType;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.plugin.PluginManager;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.api.SkillUseInfo;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.skill.Skill;

public class DamageManager {

    private Heroes plugin;

    private HeroesDamageListener listener;
    private Map<Material, Integer> itemDamage;
    private Map<ProjectileType, Integer> projectileDamage;
    private Map<CreatureType, Integer> creatureHealth;
    private Map<CreatureType, Integer> creatureDamage;
    private Map<DamageCause, Double> environmentalDamage;
    private Map<Entity, SkillUseInfo> spellTargs = new HashMap<Entity, SkillUseInfo>();

    public DamageManager(Heroes plugin) {
        this.plugin = plugin;
        listener = new HeroesDamageListener(plugin, this);
    }

    public void addSpellTarget(Entity o, Hero hero, Skill skill) {
        SkillUseInfo skillInfo = new SkillUseInfo(hero, skill);
        spellTargs.put(o, skillInfo);
    }

    public boolean isSpellTarget(Entity o) {
        return spellTargs.containsKey(o);
    }

    public SkillUseInfo getSpellTargetInfo(Entity o) {
        return spellTargs.get(o);
    }

    public Integer getCreatureDamage(CreatureType type) {
        return creatureDamage.get(type);
    }

    public Integer getCreatureHealth(CreatureType type) {
        if (creatureHealth.containsKey(type)) {
            int health = creatureHealth.get(type);
            return health > 200 ? 200 : health < 0 ? 0 : health;
        } else
            return null;
    }

    public Double getEnvironmentalDamage(DamageCause cause) {
        return environmentalDamage.get(cause);
    }

    public Integer getItemDamage(Material item, HumanEntity entity) {
        if (entity != null && entity instanceof Player) {
            HeroClass heroClass = plugin.getHeroManager().getHero((Player) entity).getHeroClass();
            Integer classDamage = heroClass.getItemDamage(item);
            if (classDamage != null)
                return classDamage;
        }
        return itemDamage.get(item);
    }

    public Integer getProjectileDamage(ProjectileType type, HumanEntity entity) {
        if (entity != null && entity instanceof Player) {
            HeroClass heroClass = plugin.getHeroManager().getHero((Player) entity).getHeroClass();
            Integer classDamage = heroClass.getProjectileDamage(type);
            if (classDamage != null)
                return classDamage;
        }
        return projectileDamage.get(type);
    }

    public void load(Configuration config) {
        Set<String> keys;

        creatureHealth = new EnumMap<CreatureType, Integer>(CreatureType.class);
        ConfigurationSection section = config.getConfigurationSection("creature-health");
        keys = section.getKeys(false);
        if (keys != null) {
            for (String key : keys) {
                CreatureType type = CreatureType.fromName(key);
                if (type == null)
                    continue;

                creatureHealth.put(type, section.getInt(key, 10));
            }
        }

        creatureDamage = new EnumMap<CreatureType, Integer>(CreatureType.class);
        section = config.getConfigurationSection("creature-damage");
        keys = section.getKeys(false);
        if (keys != null) {
            for (String key : keys) {
                CreatureType type = CreatureType.fromName(key);
                if (type == null)
                    continue;

                creatureDamage.put(type, section.getInt(key, 10));
            }
        }

        itemDamage = new EnumMap<Material, Integer>(Material.class);
        section = config.getConfigurationSection("item-damage");
        keys = section.getKeys(false);
        if (keys != null) {
            for (String key : keys) {
                Material item = Material.matchMaterial(key);
                if (item == null)
                    continue;

                itemDamage.put(item, section.getInt(key, 2));
            }
        }

        environmentalDamage = new EnumMap<DamageCause, Double>(DamageCause.class);
        section = config.getConfigurationSection("environmental-damage");
        keys = section.getKeys(false);
        if (keys != null) {
            for (String key : keys) {
                try {
                    DamageCause cause = DamageCause.valueOf(key.toUpperCase());
                    double damage = section.getDouble(key, 0.0);
                    environmentalDamage.put(cause, damage);
                } catch (IllegalArgumentException e) {}
            }
        }

        projectileDamage = new EnumMap<ProjectileType, Integer>(ProjectileType.class);
        section = config.getConfigurationSection("projectile-damage");
        keys = section.getKeys(false);
        if (keys != null) {
            for (String key : keys) {
                ProjectileType type = ProjectileType.valueOf(key.toUpperCase());
                if (type == null)
                    continue;

                projectileDamage.put(type, section.getInt(key, 0));
            }
        }
    }

    /**
     * Register the events for the damage system
     */
    public void registerEvents() {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.registerEvent(Type.ENTITY_DAMAGE, listener, Priority.High, plugin);
        pluginManager.registerEvent(Type.ENTITY_REGAIN_HEALTH, listener, Priority.Highest, plugin);
        pluginManager.registerEvent(Type.CREATURE_SPAWN, listener, Priority.Highest, plugin);
    }

    public void removeSpellTarget(Entity o) {
        spellTargs.remove(o);
    }

    public enum ProjectileType {
        ARROW,
        EGG,
        SNOWBALL;

        public static ProjectileType matchProjectile(final String name) {
            if (name.equalsIgnoreCase("arrow"))
                return ARROW;
            else if (name.equalsIgnoreCase("snowball"))
                return SNOWBALL;
            else if (name.equalsIgnoreCase("egg"))
                return EGG;
            else
                return null;
        }

        public static ProjectileType valueOf(Entity entity) {
            if (entity instanceof Arrow)
                return ARROW;
            else if (entity instanceof Snowball)
                return SNOWBALL;
            else if (entity instanceof Egg)
                return EGG;
            else
                return null;
        }
    }
}
