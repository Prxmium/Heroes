package com.herocraftonline.dev.heroes.inventory;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.event.inventory.InventoryClickEvent;
import org.getspout.spoutapi.event.inventory.InventoryCloseEvent;
import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
import org.getspout.spoutapi.event.inventory.InventoryListener;
import org.getspout.spoutapi.event.inventory.InventorySlotType;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.classes.HeroClass;
import com.herocraftonline.dev.heroes.classes.HeroClass.ExperienceType;
import com.herocraftonline.dev.heroes.hero.Hero;
import com.herocraftonline.dev.heroes.util.MaterialUtil;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SpoutInventoryListener extends InventoryListener {

    private Heroes plugin;

    public SpoutInventoryListener(Heroes heroes) {
        plugin = heroes;
    }

    @Override
    public void onInventoryClick(InventoryClickEvent event) {
        // Grab the Item attached to the Cursor.
        ItemStack item = event.getCursor();

        // Skip the checks if the cursor has no REAL Item in hand.
        if (item == null || item.getType() == null || item.getType() == Material.AIR)
            return;

        // Grab the Player involved in the Event.
        final Player player = event.getPlayer();
        // Grab the Players HeroClass.
        HeroClass clazz = plugin.getHeroManager().getHero(player).getHeroClass();

        // Allowing "*" to be added so we can allow all armor types
        if (!clazz.getAllowedArmor().contains("*")) {
            // Check if the Slot is an Armor Slot.
            if (event.getSlotType() == InventorySlotType.ARMOR) {
                // Perform Armor Check.
                String itemString = item.getType().toString();
                if (!clazz.getAllowedArmor().contains(itemString)) {
                    Messaging.send(player, "You are not trained to use a $1.", MaterialUtil.getFriendlyName(itemString));
                    event.setCancelled(true);
                    return;
                }
            }
        }

        // Allowing "*" to be added so we can allow all weapons
        if (!clazz.getAllowedWeapons().contains("*")) {
            // Check if the Slot is a Weapon Slot.
            if (event.getSlotType() == InventorySlotType.QUICKBAR) {
                // Perform Weapon Check.
                String itemString = item.getType().toString();
                // If it doesn't contain a '_' and it isn't a Bow then it definitely isn't a Weapon.
                if (!itemString.contains("_") && !itemString.equalsIgnoreCase("BOW"))
                    return;
                // Perform a check to see if what we have is a Weapon.
                if (!itemString.equalsIgnoreCase("BOW")) {
                    try {
                        // Get the value of the item.
                        HeroClass.WeaponItems.valueOf(itemString.substring(itemString.indexOf("_") + 1, itemString.length()));
                    } catch (IllegalArgumentException e1) {
                        // If it isn't a Weapon then we exit out here.
                        return;
                    }
                }
                // Check if the Players HeroClass allows this WEAPON to be equipped.
                if (!clazz.getAllowedWeapons().contains(itemString)) {
                    Messaging.send(player, "You are not trained to use a $1.", MaterialUtil.getFriendlyName(itemString));
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @Override
    public void onInventoryClose(InventoryCloseEvent event) {
        plugin.getInventoryChecker().checkInventory(event.getPlayer());
    }

    @Override
    public void onInventoryCraft(InventoryCraftEvent event) {
        if (event.getResult() == null)
            return;
        if (event.getPlayer().getInventory().firstEmpty() == -1)
            return;

        ItemStack result = event.getResult();
        if (event.getCursor() != null)
            return;
        if (plugin.getConfigManager().getProperties().craftingExp.containsKey(result.getType())) {
            Player player = event.getPlayer();
            Hero hero = plugin.getHeroManager().getHero(player);
            if (hero.getHeroClass().getExperienceSources().contains(ExperienceType.CRAFTING)) {
                hero.gainExp(plugin.getConfigManager().getProperties().craftingExp.get(result.getType()), ExperienceType.CRAFTING);
                return;
            }
        }
    }
}
