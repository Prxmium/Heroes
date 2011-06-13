package com.herocraftonline.dev.heroes.party;

import java.util.List;

import org.bukkit.entity.Player;

public class HeroParty {
    private Player leader;
    private List<Player> members;
    private List<String> modes;
    private List<Player> invites;
    
    public Player getLeader() {
        return leader;
    }
    
    public void setLeader(Player leader) {
        this.leader = leader;
    }
    
    public boolean isPartyMember(Player player) {
        for(Player p : members) {
            if(p == player) {
                return true;
            }
        }
        return false;
    }
    
    public void removeMember(Player player) {
        for(Player p : members) {
            if(p == player) {
                members.remove(player);
            }
        }
    }
    
    public void addMember(Player player) {
        for(Player p : members) {
            if(p == player) {
                return;
            }
        }
        members.add(player);
    }
    
    public void addInvite(Player player) {
        for(Player p : invites) {
            if(p == player) {
                return;
            }
        }
        invites.add(player);
    }
    
    public void removeInvite(Player player) {
        for(Player p : invites) {
            if(p == player) {
                invites.remove(player);
            }
        }
    }
    
    public void addMode(String mode) {
        modes.add(mode);
    }
    
    public void removeMode(String mode) {
        modes.remove(mode);
    }
    
    
}
