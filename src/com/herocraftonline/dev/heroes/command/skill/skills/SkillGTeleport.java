package com.herocraftonline.dev.heroes.command.skill.skills;

import org.bukkit.entity.Player;

import com.herocraftonline.dev.heroes.Heroes;
import com.herocraftonline.dev.heroes.command.skill.ActiveSkill;
import com.herocraftonline.dev.heroes.persistence.Hero;

public class SkillGTeleport extends ActiveSkill {

	public SkillGTeleport(Heroes plugin) {
		super(plugin);
		name = "Group Teleport";
		description = "Skill - Group Teleport";
		usage = "/gteleport";
		minArgs = 0;
		maxArgs = 0;
		identifiers.add("gteleport");
	}

	@Override
	public void use(Hero hero, String[] args) {
		if (hero.getParty() != null && hero.getParty().getMembers().size() != 1) {
			Player player = hero.getPlayer();
			for (Player n : hero.getParty().getMembers()) {
				n.teleport(player);
			}
		}
	}
}