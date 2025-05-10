package org.sloart.islandDailyCheckIn.command;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.sloart.islandDailyCheckIn.DataBase.islandDailyCheckInDBAsync;
import org.sloart.islandDailyCheckIn.gui.islandDailyCheckInGui;

import java.time.LocalDate;


public class islandDailyCheckInCommand implements CommandExecutor {
    private final islandDailyCheckInDBAsync async;
    private final islandDailyCheckInGui gui;

    public islandDailyCheckInCommand(islandDailyCheckInDBAsync async, islandDailyCheckInGui gui) {
        this.async = async;
        this.gui = gui;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("이 명령어는 플레이어만 사용할 수 있습니다.");
            return true;
        }

        String today = LocalDate.now().toString();
        String date = args.length > 0 ? args[0] : today;
        boolean isArgToday = date.equals(today);
        boolean isNext = !isArgToday; // 오늘이 아니면 '내일'로 처리

        async.hasClaimedToday(player.getUniqueId(), claimed -> {
            try {
                gui.openGetRewardGui(player, claimed, isNext, date);
            } catch (Exception e) {
                player.sendMessage("보상 GUI를 여는 도중 문제가 발생했습니다.");
                e.printStackTrace();
            }
        });

        return true;
    }
}
