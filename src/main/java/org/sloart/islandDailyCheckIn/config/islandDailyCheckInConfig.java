package org.sloart.islandDailyCheckIn.config;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.sloart.islandDailyCheckIn.main.islandDailyCheckInMain;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class islandDailyCheckInConfig {

    private final islandDailyCheckInMain plugin;
    private final FileConfiguration config;
    private final File datesFolder;

    public islandDailyCheckInConfig(islandDailyCheckInMain plugin) {
        this.plugin = plugin;
        this.plugin.saveDefaultConfig();
        this.config = plugin.getConfig(); // config.yml
        this.datesFolder = new File(plugin.getDataFolder(), "dates");

        if (!datesFolder.exists()) {
            datesFolder.mkdirs(); // "dates" 폴더 없으면 생성
        }
    }

    // 일반 설정값을 가져오는 메서드 예시
    public ItemStack getRewardCheckItem() {
        return new ItemStack(Material.valueOf(config.getString("setting.reward_check_item", "BOOK")));
    }

    public int getRewardCheckLoreMax() {
        return config.getInt("setting.reward_check_lore_max", 5);
    }

    public Sound getRewardCheckSound() {
        return Sound.valueOf(config.getString("setting.reward_check_sound", "ENTITY_PLAYER_LEVELUP"));
    }


    public String getNoRewardCheckMessage() {
        return ChatColor.translateAlternateColorCodes('&', config.getString("setting.no_reward_check_message", "님아 보상안받음"));
    }

    // 날짜별 보상을 저장
    public void saveRewardsForDate(String date, List<ItemStack> rewards) {
        File file = new File(datesFolder,date + ".yml");
        YamlConfiguration dateConfig = YamlConfiguration.loadConfiguration(file);
        dateConfig.set("rewards", rewards);
        try {
            dateConfig.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("[DailyCheckIn] 보상 저장 실패: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // 날짜별 보상을 불러오기
    public List<ItemStack> getRewardsForDate(String date) {
        File file = new File(datesFolder, date + ".yml");
        if (!file.exists()) return new ArrayList<>();

        YamlConfiguration dateConfig = YamlConfiguration.loadConfiguration(file);
        List<?> rawList = dateConfig.getList("rewards");
        List<ItemStack> itemStackList = new ArrayList<>();

        if (rawList != null) {
            for (Object obj : rawList) {
                if (obj instanceof ItemStack) {
                    itemStackList.add((ItemStack) obj);
                }
            }
        }

        return itemStackList;
    }

    public List<String> getColoredSavedRewardDates() {
        List<String> coloredDates = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        for (String dateStr : getAllSavedRewardDates()) {
            LocalDate date;
            try {
                date = LocalDate.parse(dateStr); // yyyy-MM-dd 포맷 가정
            } catch (Exception e) {
                coloredDates.add("§c날짜 형식 오류: " + dateStr);
                continue;
            }

            String prefix = "§7";
            if (date.equals(today)) {
                prefix = "§6";
            } else if (date.equals(yesterday)) {
                prefix = "§b";
            }

            coloredDates.add(prefix + dateStr);
        }

        return coloredDates;
    }

    public List<String> getAllSavedRewardDates() {
        List<String> dateList = new ArrayList<>();

        File[] files = datesFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                dateList.add(file.getName()); // 확장자 포함 전체 이름 반환
            }
        }

        return dateList;
    }

    public void deleteAllRewardData() {
        File[] files = datesFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files != null) {
            for (File file : files) {
                if (file.delete()) {
                    plugin.getLogger().info("[DailyCheckIn] 삭제됨: " + file.getName());
                } else {
                    plugin.getLogger().warning("[DailyCheckIn] 삭제 실패: " + file.getName());
                }
            }
        }
    }


    // 특정 날짜의 보상 데이터를 삭제
    public boolean deleteRewardDataForDate(String date) {
        File file = new File(datesFolder, date + ".yml");
        if (file.exists()) {
            return file.delete(); // 삭제 성공 시 true 반환
        }
        return false; // 파일이 존재하지 않으면 false 반환
    }
}
