package org.sloart.islandDailyCheckIn.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.sloart.islandDailyCheckIn.DataBase.islandDailyCheckInDBAsync;
import org.sloart.islandDailyCheckIn.config.islandDailyCheckInConfig;
import org.sloart.islandDailyCheckIn.gui.islandDailyCheckInGui;

import java.time.LocalDate;
import java.util.List;

public class islandDailyCheckInCommandManager implements CommandExecutor {

    private final islandDailyCheckInGui gui;
    private final islandDailyCheckInDBAsync async;
    private final islandDailyCheckInConfig config;

    public islandDailyCheckInCommandManager(islandDailyCheckInGui gui, islandDailyCheckInDBAsync async, islandDailyCheckInConfig config) {
        this.gui = gui;
        this.async = async;
        this.config = config;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length < 1) {
            return false;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "설정":
                if (args.length != 2 || !args[1].matches("\\d{4}-\\d{2}-\\d{2}")) {
                    sender.sendMessage("§c날짜 형식이 잘못되었습니다. 예: 2025-05-09");
                    return true;
                }
                handleSettingsCommand(player, args[1]);
                break;

            case "리셋":
                if (args.length != 2) return false;
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage("§c해당 플레이어를 찾을 수 없습니다.");
                    return true;
                }
                handleResetCommand(target, LocalDate.now().toString());
                sender.sendMessage("§a" + args[1] + "의 보상이 초기화되었습니다.");
                break;

            case "삭제":
                if (args.length != 2 || !args[1].matches("\\d{4}-\\d{2}-\\d{2}")) {
                    sender.sendMessage("§c날짜 형식이 잘못되었습니다. 예: 2025-05-09");
                    return true;
                }
                handleDelCommand(player, args[1]);
                break;

            case "전체삭제":
                if (!player.hasPermission("dailycheckin.admin")) {
                    sender.sendMessage("§c이 명령어를 사용할 권한이 없습니다.");
                    return true;
                }
                config.deleteAllRewardData();
                sender.sendMessage("§c모든 보상 데이터가 삭제되었습니다.");
                break;

            case "리스트":
                if (args.length == 2) {
                    // args[1]이 날짜 혹은 .yml 파일일 수 있으므로 확장자 제거
                    String inputDate = args[1].replace(".yml", "");

                    // 날짜 형식 검증 (예: 2025-05-10 형식)
                    if (!inputDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                        player.sendMessage("§c날짜 형식이 잘못되었습니다. 예: 2025-05-09");
                        return true;
                    }

                    // gui에서 해당 날짜의 보상 내용 확인을 위해 넘기기
                    gui.openRewardSeeMoreGUI(player, inputDate);
                } else {
                    List<String> allDates = config.getAllSavedRewardDates();
                    if (allDates.isEmpty()) {
                        player.sendMessage("§7저장된 보상 날짜가 없습니다.");
                    } else {
                        player.sendMessage("§e[저장된 날짜 목록]");
                        for (String date : allDates) {
                            player.sendMessage("§a- " + date);
                        }
                    }
                }
                break;
            default:
                return false;
        }

        return true;
    }

    private void handleSettingsCommand(Player player, String date) {
        gui.openRewardSetupGUI(player, date);
        player.sendMessage("§a" + date + "에 대한 보상 설정을 시작합니다.");
    }

    private void handleResetCommand(Player player, String date) {
        async.resetRewardClaim(player.getUniqueId());
        player.sendMessage("§a" + date + "의 보상이 리셋되었습니다.");
    }

    private void handleDelCommand(Player player, String date) {
        if (config.deleteRewardDataForDate(date)) {
            player.sendMessage("§a" + date + "의 보상이 삭제되었습니다.");
        } else {
            player.sendMessage("§a" + date + " 의 보상 설정 내역이 없거나 삭제에 실패하였습니다.");
        }
    }
}