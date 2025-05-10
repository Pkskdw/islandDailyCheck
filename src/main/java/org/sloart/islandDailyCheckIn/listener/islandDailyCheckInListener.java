package org.sloart.islandDailyCheckIn.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.sloart.islandDailyCheckIn.DataBase.islandDailyCheckInDBAsync;
import org.sloart.islandDailyCheckIn.config.islandDailyCheckInConfig;


public class islandDailyCheckInListener implements Listener {
    private final islandDailyCheckInDBAsync dbAsync;
    private final islandDailyCheckInConfig config;

    public islandDailyCheckInListener(islandDailyCheckInDBAsync dbAsync, islandDailyCheckInConfig config) {
        this.dbAsync = dbAsync;
        this.config = config;
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();


        dbAsync.hasClaimedToday(player.getUniqueId(), claimed -> {
            if (!claimed) player.sendMessage(config.getNoRewardCheckMessage());
        });
    }
}
