package com.herocraftonline.dev.heroes;

import net.minecraft.server.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.player.SpoutPlayer;

import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.command.Command;
import com.herocraftonline.dev.heroes.effects.Effect;
import com.herocraftonline.dev.heroes.effects.EffectType;
import com.herocraftonline.dev.heroes.effects.PeriodicEffect;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.hero.HeroManager;
import com.herocraftonline.dev.heroes.skill.SkillType;
import com.herocraftonline.dev.heroes.util.Messaging;
import com.herocraftonline.dev.heroes.util.Properties;
import com.herocraftonline.dev.heroes.util.Util;

public class HPlayerListener extends PlayerListener {

    public final Heroes plugin;

    public HPlayerListener(Heroes instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.isCancelled())
            return;
        
        switch (event.getState()) {
        case CAUGHT_FISH :
            Player player = event.getPlayer();
            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero.hasParty()) {
                hero.getParty().gainExp(Heroes.properties.fishingExp, ExperienceType.FISHING, event.getPlayer().getLocation());
            } else if (hero.canGain(ExperienceType.FISHING))
                hero.gainExp(Heroes.properties.fishingExp, ExperienceType.FISHING);
        case CAUGHT_ENTITY :
        default: 
            return;
        }
    }

    @Override
    public void onItemHeldChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        Hero hero = plugin.getHeroManager().getHero(player);
        if (hero.hasEffectType(EffectType.DISARM)) {
            Util.disarmCheck(hero, plugin);
        }
        hero.checkInventory();
    }

    @Override
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Properties props = Heroes.properties;
        if (event.isCancelled() || !props.bedHeal || props.disabledWorlds.contains(event.getPlayer().getWorld().getName()))
            return;

        Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
        long period = props.healInterval * 1000;
        double tickHealPercent = props.healPercent / 100.0;
        BedHealEffect bhEffect = new BedHealEffect(period, tickHealPercent);
        hero.addEffect(bhEffect);
    }

    @Override
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        if (!Heroes.properties.bedHeal)
            return;

        // This player is no longer in bed so remove them from the bedHealer set
        Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
        if (hero.hasEffect("BedHeal")) {
            hero.removeEffect(hero.getEffect("BedHeal"));
        }
    }

    @Override
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.isCancelled())
            return;
        
        Player player = event.getPlayer();
        Hero hero = plugin.getHeroManager().getHero(player);
        
        if (!hero.canEquipItem(player.getInventory().getHeldItemSlot())) {
            event.setCancelled(true);
            Util.syncInventory(player, plugin);
            return;
        }
    }

    @Override
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.useItemInHand() == Result.DENY)
            return;
        
        Player player = event.getPlayer();

        Hero hero = plugin.getHeroManager().getHero(player);
        if (hero.hasEffectType(EffectType.DISARM))
            Util.disarmCheck(hero, plugin);

        if (!hero.canEquipItem(player.getInventory().getHeldItemSlot())) {
            event.setCancelled(true);
            Util.syncInventory(player, plugin);
            return;
        }

        if (hero.hasEffectType(EffectType.STUN)) {
            event.setCancelled(true);
            return;
        }
        
        Block clickedBlock = event.getClickedBlock();
        if (clickedBlock != null) {
            switch (clickedBlock.getType()) {
            case DISPENSER:
            case BED:
            case FURNACE:
            case BURNING_FURNACE:
            case WOOD_DOOR:
            case LEVER:
            case IRON_DOOR:
            case JUKEBOX:
            case DIODE_BLOCK_OFF:
            case DIODE_BLOCK_ON:
            case CHEST:
            case LOCKED_CHEST:
            case TRAP_DOOR:
                hero.cancelDelayedSkill();
                return;
            }
        }

        boolean isStealthy = false;
        if (player.getItemInHand() != null) {
            Material material = player.getItemInHand().getType();
            if (hero.hasBind(material)) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    String[] args = hero.getBind(material);
                    plugin.onCommand(player, null, "skill", args);
                    isStealthy = plugin.getSkillManager().getSkill(args[0]).isType(SkillType.STEALTHY);
                } else {
                    hero.cancelDelayedSkill();
                }
            } else {
                hero.cancelDelayedSkill();
            }
        }
        // Remove effects dependant on non-interaction
        if (!isStealthy) {
            for (Effect effect : hero.getEffects()) {
                if (effect.isType(EffectType.INVIS))
                    hero.removeEffect(effect);
            }
        }
    }

    @Override
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        HeroManager hm = plugin.getHeroManager();
        final Hero hero = hm.getHero(player);
        hm.checkClasses(hero); // check to make sure player has permission for the classes
        hero.syncExperience();
        hero.syncHealth();
        hero.checkInventory();
        if (Heroes.properties.prefixClassName) {
            player.setDisplayName("[" + hero.getHeroClass().getName() + "]" + player.getName());
        }
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getHeroManager().performSkillChecks(hero);
                hero.checkInventory();
            }
        }, 5);
        
        //Spout stuff
        if (Heroes.useSpout()) {
            SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
            if (sPlayer.isSpoutCraftEnabled())
                plugin.getSpoutData().createPartyContainer(sPlayer);
        }
    }

    @Override
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.isCancelled() || Heroes.properties.disabledWorlds.contains(event.getPlayer().getWorld().getName()))
            return;

        final Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
        if (hero.hasEffectType(EffectType.DISARM) && Util.isWeapon(event.getItem().getItemStack().getType())) {
            event.setCancelled(true);
            return;
        }

        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                hero.checkInventory();
            }
        });
    }

    @Override
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        //Spout cleanup stuff
        if (Heroes.useSpout()) {
            SpoutPlayer sPlayer = SpoutManager.getPlayer(player);
            plugin.getSpoutData().removePartyContainer(sPlayer);
        }
        HeroManager heroManager = plugin.getHeroManager();
        Hero hero = heroManager.getHero(player);
        hero.cancelDelayedSkill();
        hero.clearEffects();
        heroManager.saveHero(hero);
        heroManager.removeHero(hero);
        for (Command command : plugin.getCommandHandler().getCommands()) {
            if (command.isInteractive()) {
                command.cancelInteraction(player);
            }
        }

    }

    @Override
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        final Hero hero = plugin.getHeroManager().getHero(player);
        hero.setHealth(hero.getMaxHealth());
        hero.setMana(0);
        CraftPlayer craftPlayer = (CraftPlayer) player;
        EntityPlayer entityPlayer = craftPlayer.getHandle();
        entityPlayer.exp = 0;
        entityPlayer.expTotal = 0;
        entityPlayer.expLevel = 0;
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getHeroManager().performSkillChecks(hero);
                hero.checkInventory();
                hero.syncExperience();
            }
        }, 20L);
    }

    @Override
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled() || event.getFrom().getWorld() == event.getTo().getWorld())
            return;

        final Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getHeroManager().performSkillChecks(hero);
                hero.checkInventory();
            }
        }, 5);
    }

    public class BedHealEffect extends PeriodicEffect {

        private final double tickHealPercent;

        public BedHealEffect(long period, double tickHealPercent) {
            super(null, "BedHeal", period);
            this.tickHealPercent = tickHealPercent;
        }

        @Override
        public void apply(Hero hero) {
            super.apply(hero);
            this.lastTickTime = System.currentTimeMillis();
        }

        @Override
        public void tick(Hero hero) {
            super.tick(hero);
            Player player = hero.getPlayer();
            double healAmount = hero.getMaxHealth() * tickHealPercent;
            hero.setHealth(hero.getHealth() + healAmount);
            hero.syncHealth();
            if (hero.isVerbose()) {
                player.sendMessage(Messaging.createFullHealthBar(hero.getHealth(), hero.getMaxHealth()));
            }
        }
    }
}
