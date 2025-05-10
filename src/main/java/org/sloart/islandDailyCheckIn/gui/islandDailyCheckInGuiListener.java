package org.sloart.islandDailyCheckIn.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.sloart.islandDailyCheckIn.config.islandDailyCheckInConfig;
import org.sloart.islandDailyCheckIn.handler.islandDailyCheckInHandler;
import org.sloart.islandDailyCheckIn.main.islandDailyCheckInMain;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class islandDailyCheckInGuiListener implements Listener {

    private final islandDailyCheckInConfig config;
    private final islandDailyCheckInHandler handler;
    private final islandDailyCheckInMain plugin;

    public islandDailyCheckInGuiListener(islandDailyCheckInConfig config, islandDailyCheckInHandler handler, islandDailyCheckInMain plugin) {
        this.config = config;
        this.handler = handler;
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();
        Player player = (Player) event.getPlayer();

        // "보상 설정: "으로 시작하는지 확인하고 날짜만 추출
        if (title.startsWith("보상 설정: ")) {
            String date = title.substring("보상 설정: ".length()); // "보상 설정: " 이후의 문자열만 추출 (날짜)

            List<ItemStack> rewards = new ArrayList<>();
            for (ItemStack item : inventory.getContents()) {
                if (item != null && item.getType() != Material.AIR) {
                    rewards.add(item);
                }
            }

                config.saveRewardsForDate(date, rewards);
                player.sendMessage("§a" + date + " 보상이 저장되었습니다!");
            }

            // "자세히보기" GUI를 닫았을 때만 처리
            if (title.contains(" (자세히보기)")) {
                // 스케줄러를 통해 지연 실행
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    String date = title.substring("보상 확인: ".length(), title.indexOf(" (자세히보기)"));
                    handler.handleBackSeeMore(player, date);
                }, 1L);  // 1틱(50ms) 후 실행
            }
        }




    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ClickType click = event.getClick();
        ItemStack clickedItem = event.getCurrentItem();
        String title = event.getView().getTitle();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        String clickedItemName = clickedItem.getItemMeta().getDisplayName();

        // 오늘과 내일 날짜
        LocalDate today = LocalDate.now();
        String todayDate = today.toString();
        String tomorrowDate = today.plusDays(1).toString();

        // "자세히 보기"가 포함된 제목일 경우 이벤트 취소
        if (title.contains(" (자세히보기)")) {
            event.setCancelled(true);
            return;
        }

        // 날짜와 보상 관련 아이템 검사를 배열로 처리
        Map<String, Consumer<String>> rewardActions = new HashMap<>();
        rewardActions.put("§6" + todayDate + " 보상", date -> handler.handleTodayRewardClick(player, clickedItem, click, date));
        rewardActions.put("§6" + tomorrowDate + " 보상", date -> handler.handleTomorrowRewardClick(player, click, date));
        rewardActions.put("§6" + todayDate + " 보상은 이미 수령하셨습니다.", date -> handler.handleTodayRewardClick(player, clickedItem, click, date));

        // 해당하는 보상 클릭 처리
        rewardActions.forEach((itemName, action) -> {
            if (clickedItemName.equals(itemName)) {
                event.setCancelled(true);
                action.accept(todayDate);  // 적절한 날짜를 전달
            }
        });
    }
}
