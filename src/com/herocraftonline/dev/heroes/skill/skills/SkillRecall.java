package com.herocraftonline.dev.heroes.skill.skills;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.persistence.Hero;
import com.herocraftonline.dev.heroes.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.util.Messaging;

public class SkillRecall extends ActiveSkill {

    public SkillRecall(Heroes plugin) {
        super(plugin, "Recall");
        setDescription("Marks a location for use with recall, or recalls you to the marked Location");
        setUsage("/skill recall <set|mark|info>");
        setArgumentRange(0, 1);
        setIdentifiers(new String[] { "skill recall" });
    }

    @Override
    public boolean use(Hero hero, String[] args) {
        Player player = hero.getPlayer();
        Map<String, String> skillSetting = hero.getSkillSettings(this);

        if (args.length == 1) {
            String label = args[0].toLowerCase();
            if (!label.equals("mark") && !label.equals("info") && !label.equals("set")) {
                return false;
            } else if (label.equals("info")) {
                // Display the info about the current mark
                World world = validateLocation(skillSetting, player);
                if (world == null)
                    return false;
                double[] xyzyp = getStoredData(skillSetting);
                Messaging.send(player, "Your recall is currently marked on $1 at: $2, $3, $4", new Object[] { world.getName(), (int) xyzyp[0], (int) xyzyp[1], (int) xyzyp[2] });
                return true;
            } else {
                // Save a new mark
                Location loc = player.getLocation();
                hero.setSkillSetting(this, "world", loc.getWorld().getName());
                hero.setSkillSetting(this, "x", loc.getX());
                hero.setSkillSetting(this, "y", loc.getY());
                hero.setSkillSetting(this, "z", loc.getZ());
                hero.setSkillSetting(this, "yaw", (double) loc.getYaw());
                hero.setSkillSetting(this, "pitch", (double) loc.getPitch());
                Object[] obj = new Object[] { loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ() };
                Messaging.send(player, "You have marked a new location on $1 at: $2, $3, $4", obj);

                getPlugin().getHeroManager().saveHero(player);
                return true;
            }
        }
        // Try to teleport back to the location
        World world = validateLocation(skillSetting, player);
        if (world == null)
            return false;
        double[] xyzyp = getStoredData(skillSetting);
        broadcastExecuteText(hero);
        player.teleport(new Location(world, xyzyp[0], xyzyp[1], xyzyp[2], (float) xyzyp[3], (float) xyzyp[4]));
        return true;
    }

    private double[] getStoredData(Map<String, String> skillSetting) {
        double[] xyzyp = new double[5];

        xyzyp[0] = Double.valueOf(skillSetting.get("x"));
        xyzyp[1] = Double.valueOf(skillSetting.get("y"));
        xyzyp[2] = Double.valueOf(skillSetting.get("z"));
        xyzyp[3] = Double.valueOf(skillSetting.get("yaw"));
        xyzyp[4] = Double.valueOf(skillSetting.get("pitch"));

        return xyzyp;
    }

    private World validateLocation(Map<String, String> skillSetting, Player player) {
        if (skillSetting == null) {
            Messaging.send(player, "You do not have a recall location marked.");
            return null;
        }

        // Make sure the world setting isn't null - this lets us know the player has a location saved
        if (skillSetting.get("world") == null || skillSetting.get("world").equals("")) {
            Messaging.send(player, "You do not have a recall location marked.");
            return null;
        }
        // Get the world and make sure it's still available to return to
        World world = getPlugin().getServer().getWorld((String) skillSetting.get("world"));
        if (world == null) {
            Messaging.send(player, "You have an invalid recall location marked!");
            return null;
        }

        return world;
    }
}
