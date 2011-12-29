package com.herocraftonline.dev.heroes.spout;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
import org.getspout.spoutapi.event.inventory.InventoryEnchantEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SpoutInventoryListener extends InventoryListener {

    private Heroes plugin;

    public SpoutInventoryListener(Heroes heroes) {
        plugin = heroes;
    }
    
    @Override
    public void onInventoryCraft(InventoryCraftEvent event) {
        if (event.getResult() == null)
            return;
        
        if (event.isShiftClick() && event.getPlayer().getInventory().firstEmpty() == -1)
            return;
        
        if (!event.isShiftClick() && event.getCursor() != null && event.getCursor().getType().getMaxStackSize() == event.getCursor().getAmount())
            return;
        
        ItemStack result = event.getResult();
        int amountCrafted = result.getAmount();
        
        if (event.getCursor() != null && event.getCursor().getType() != result.getType())
            return;
        
        if (Heroes.properties.craftingExp.containsKey(result.getType())) {
            Player player = event.getPlayer();
            Hero hero = plugin.getHeroManager().getHero(player);
            if (!hero.canCraft(result)) {
                Messaging.send(hero.getPlayer(), "You don't know how to craft $1", result.getType().name().toLowerCase().replace("_", " "));
                event.setCancelled(true);
                return;
            }
            if (hero.canGain(ExperienceType.CRAFTING)) {
                hero.gainExp(Heroes.properties.craftingExp.get(result.getType()) * amountCrafted, ExperienceType.CRAFTING);
                return;
            }
        }
    }

    @Override
    public void onInventoryEnchant(InventoryEnchantEvent event) {
        if (event.isCancelled() || Heroes.properties.enchantXPMultiplier == 0)
            return;
        

        Hero hero = plugin.getHeroManager().getHero(event.getPlayer());
        if (!hero.hasExperienceType(ExperienceType.ENCHANTING)) {
            event.setCancelled(true);
            return;
        }
        
        double xpCost = Heroes.properties.enchantXPMultiplier * (event.getLevelBefore() - event.getLevelAfter());
        event.setLevelAfter(event.getLevelBefore());
        hero.gainExp(-xpCost, ExperienceType.ENCHANTING);
    }    
}
