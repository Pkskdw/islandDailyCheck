package org.sloart.islandDailyCheckIn.DataBase;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class islandDailyCheckInDBAsync {

    private final islandDailyCheckInDB db;
    private final Plugin plugin;

    public islandDailyCheckInDBAsync(islandDailyCheckInDB db, Plugin plugin) {
        this.db = db;
        this.plugin = plugin;
    }

    public void getRewardClaimedDate(UUID uuid, islandDailyCheckInCallback<String> islandDailyCheckInCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String result = db.getRewardClaimedDate(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> islandDailyCheckInCallback.call(result));
        });
    }

    public void hasClaimedToday(UUID uuid, islandDailyCheckInCallback<Boolean> islandDailyCheckInCallback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean result = db.hasClaimedToday(uuid);
            Bukkit.getScheduler().runTask(plugin, () -> islandDailyCheckInCallback.call(result));
        });
    }

    public void setRewardClaimed(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> db.setRewardClaimed(uuid));
    }

    public void resetRewardClaim(UUID uuid) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> db.resetRewardClaim(uuid));
    }
}
