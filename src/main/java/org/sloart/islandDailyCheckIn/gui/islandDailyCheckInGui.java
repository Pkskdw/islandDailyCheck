package org.sloart.islandDailyCheckIn.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.sloart.islandDailyCheckIn.config.islandDailyCheckInConfig;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class islandDailyCheckInGui {
    private final islandDailyCheckInConfig config;

    public islandDailyCheckInGui(islandDailyCheckInConfig config) {
        this.config = config;
    }

    // 보상 설정 GUI
    public void openRewardSetupGUI(Player player, String date) {
        Inventory gui = Bukkit.createInventory(null, 54, "보상 설정: " + date);

        List<ItemStack> rewards = config.getRewardsForDate(date);
        setRewardItemsInGui(gui, rewards);

        player.openInventory(gui);
    }

    public void openGetRewardGui(Player player, boolean hasAlreadyReceived, boolean isNext, String date) {
        List<ItemStack> rewardItems = config.getRewardsForDate(date);

        if (rewardItems == null || rewardItems.isEmpty()) {
            player.sendMessage("§c해당 날짜에 등록된 보상이 없습니다.");
            return;
        }

        String dateLabel = isNext ? "내일" : "오늘";
        String guiTitle = dateLabel + " 보상 확인: " + date;

        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);
        ItemStack rewardBook = createRewardBook(date, rewardItems, hasAlreadyReceived);
        gui.setItem(13, rewardBook);

        player.openInventory(gui);
    }

    public void openRewardSeeMoreGUI(Player player, String date) {
        List<ItemStack> rewardItems = config.getRewardsForDate(date);

        if (rewardItems == null || rewardItems.isEmpty()) {
            player.sendMessage("§c해당 날짜에 등록된 보상이 없습니다.");
            return;
        }

        Inventory gui = Bukkit.createInventory(null, 54,"보상 확인: " + date + " (자세히보기)");
        setRewardItemsInGui(gui, rewardItems);

        player.openInventory(gui);
    }

    // GUI에 보상 아이템 세팅
    private void setRewardItemsInGui(Inventory gui, List<ItemStack> rewards) {
        if (rewards == null) return;

        int slot = 0;
        for (ItemStack item : rewards) {
            if (item != null && item.getType() != Material.AIR && slot < gui.getSize()) {
                gui.setItem(slot++, item);
            }
        }
    }

    // 보상 책 생성
    public ItemStack createRewardBook(String date, List<ItemStack> rewardItems, boolean getAlready) {
        ItemStack book = config.getRewardCheckItem();
        ItemMeta bookMeta = book.getItemMeta();

        if (bookMeta != null) {
            String displayName = getAlready
                    ? "§6" + date + " 보상은 이미 수령하셨습니다."
                    : "§6" + date + " 보상";
            bookMeta.setDisplayName(displayName);

            List<String> lore = new ArrayList<>();
            int maxItemsToShow = Math.min(rewardItems.size(), config.getRewardCheckLoreMax());

            for (int i = 0; i < maxItemsToShow; i++) {
                ItemStack item = rewardItems.get(i);
                if (item == null || item.getType() == Material.AIR) continue;

                ItemMeta itemMeta = item.getItemMeta();
                String itemName;
                if (itemMeta != null && itemMeta.hasDisplayName()) {
                    itemName = itemMeta.getDisplayName();
                } else {
                    itemName = "§f" + item.getType().name().toLowerCase().replace("_", " ");
                }

                lore.add("§7" + itemName + " §8x" + item.getAmount());
            }

            if (rewardItems.size() > config.getRewardCheckLoreMax()) {
                int remainingAmount = rewardItems.stream()
                        .skip(config.getRewardCheckLoreMax())
                        .mapToInt(ItemStack::getAmount)
                        .sum();
                lore.add("§7그 외 §8x" + remainingAmount);
            }

            bookMeta.setLore(lore);

            if (getAlready) {
                bookMeta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            }

            book.setItemMeta(bookMeta);
        }

        return book;
    }
}