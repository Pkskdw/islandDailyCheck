package org.sloart.islandDailyCheckIn.handler;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.sloart.islandDailyCheckIn.DataBase.islandDailyCheckInDB;
import org.sloart.islandDailyCheckIn.DataBase.islandDailyCheckInDBAsync;
import org.sloart.islandDailyCheckIn.config.islandDailyCheckInConfig;
import org.sloart.islandDailyCheckIn.gui.islandDailyCheckInGui;

import java.time.LocalDate;
import java.util.List;

public class islandDailyCheckInHandler {

    private final islandDailyCheckInConfig config;
    private final islandDailyCheckInGui gui;
    private final islandDailyCheckInDBAsync async;

    public islandDailyCheckInHandler(islandDailyCheckInConfig config, islandDailyCheckInGui gui, islandDailyCheckInDBAsync async) {
        this.config = config;
        this.gui = gui;
        this.async = async;
    }

    // 플레이어의 인벤토리에 날짜에 맞는 보상 아이템을 추가하는 메서드
    public boolean giveRewardToPlayer(Player player, String date) {
        // 해당 날짜의 보상 아이템 목록 가져오기
        List<ItemStack> rewardItems = config.getRewardsForDate(date);

        if (rewardItems == null || rewardItems.isEmpty()) {
            player.sendMessage("§c보상 아이템이 설정되지 않았습니다.");
            return false;
        }

        Inventory inventory = player.getInventory();

        // 인벤토리의 여유 공간 확인
        int freeSlots = getFreeSlots(inventory); // 빈 슬롯의 수를 계산

        // 빈 슬롯 수와 보상 아이템 수 비교
        if (freeSlots < rewardItems.size()) return false;

        // 빈 슬롯이 충분할 경우, 아이템을 인벤토리에 추가
        for (ItemStack item : rewardItems) inventory.addItem(item); // 아이템을 추가
        return true;
    }

    public void handleTodayRewardClick(Player player, ItemStack clickedItem, ClickType click, String date) {
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        if (click == ClickType.LEFT) {
            if (clickedItem.hasItemMeta() && clickedItem.getItemMeta().hasDisplayName()) {
                String displayName = clickedItem.getItemMeta().getDisplayName();
                if (displayName.contains("이미 수령")) {
                    player.sendMessage(ChatColor.RED + "이미 보상을 수령하셨습니다.");
                    return;
                }
            }

            boolean success = giveRewardToPlayer(player, date);
            if (success) {
                player.sendMessage(ChatColor.GREEN + "보상을 수령했습니다!");
                async.setRewardClaimed(player.getUniqueId());
                player.closeInventory();
            } else {
                player.sendMessage(ChatColor.RED + "보상을 수령할 수 없습니다.");
            }
        } else if (click == ClickType.RIGHT) {
            gui.openRewardSeeMoreGUI(player, date);
        }
    }


    public void handleBackSeeMore(Player player, String date) {
        LocalDate today = LocalDate.now();
        String todayStr = today.toString();

        if (date.equals(todayStr)) {
            async.hasClaimedToday(player.getUniqueId(), claimed -> {
                // 비동기 작업 완료 후 GUI를 여는 방식
                gui.openGetRewardGui(player, claimed, false, date);
            });
        } else {
            boolean isTomorrow = date.equals(today.plusDays(1).toString());
            gui.openGetRewardGui(player, false, isTomorrow, date);
        }
    }



    public void handleTomorrowRewardClick(Player player, ClickType click, String date) {
        if (click == ClickType.LEFT) {
            player.sendMessage(ChatColor.YELLOW + "내일 수령 가능합니다.");
        } else if (click == ClickType.RIGHT) {
            gui.openRewardSeeMoreGUI(player, date);
        }
    }

    // 인벤토리에서 비어있는 슬롯의 개수를 구하는 메서드
    private int getFreeSlots(Inventory inventory) {
        int freeSlots = 0;
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == org.bukkit.Material.AIR) {
                freeSlots++;
            }
        }
        return freeSlots;
    }
}
