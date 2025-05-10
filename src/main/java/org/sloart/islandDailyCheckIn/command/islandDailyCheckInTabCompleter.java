package org.sloart.islandDailyCheckIn.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sloart.islandDailyCheckIn.config.islandDailyCheckInConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class islandDailyCheckInTabCompleter implements TabCompleter {
    private final islandDailyCheckInConfig config;

    public islandDailyCheckInTabCompleter(islandDailyCheckInConfig config) {
        this.config = config;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("설정", "리셋", "삭제", "전체삭제", "리스트");
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "설정", "삭제", "리스트":
                    return config.getAllSavedRewardDates();
                case "리셋":
                    List<String> playerNames = new ArrayList<>();
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        playerNames.add(p.getName());
                    }
                    return playerNames;
            }
        }
        return Collections.emptyList();
    }
}
